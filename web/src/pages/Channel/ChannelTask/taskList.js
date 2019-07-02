import React, { Component, Fragment } from 'react';
import moment from 'moment';
import { connect } from 'dva';
import { Icon, Table, Button, Form, Input, Modal, Popconfirm } from 'antd';
import PageHeaderLayout from '@/components/PageHeaderWrapper';
import TabPaneCon from '@/components/TabPaneCon';
import RejectModal from './rejectModal';
import styles from '../style.less';

const FormItem = Form.Item;
const { TextArea } = Input;

@connect(({ ChannelTask }) => {
  return {
    ChannelTask,
  };
})
class TaskList extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isShow: false,
      isError: false,
      isReject: false,
      taskId: null,
      text: '',
    };
  }

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'ChannelTask/handleGetTaskList',
    });
  }
  componentDidUpdate(prevProps, prevState) {
    const { isShow } = this.state;
    if (isShow && prevState.isShow) {
      this.setState({
        isShow: false,
      });
    }
  }
  // 打开处理任务弹窗
  handleShowModal = (value) => {
    this.setState({
      isShow: true,
      taskId: value
    });
  }
  // 关闭处理任务弹窗
  handleCloseModal = () => {
    this.setState({
      isShow: false,
    });
  }
  // 删除任务
  handleDeleteTask = (value) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'ChannelTask/handleDelTask',
      payload: { taskId: value }
    });
  }
  // 撤销任务
  handleRecallTask = (value) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'ChannelTask/handleResTask',
      payload: { taskId: value }
    });
  }
  // 提交任务至orderer
  handleSubmitTask = (value) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'ChannelTask/handleSubTask',
      payload: { taskId: value }
    });
  }
  // 同意添加组织
  handleAgreeTask = (value) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'ChannelTask/handelDealTask',
      payload: {
        reject: false,
        taskId: value,
        reason: ''
      }
    });
  }

  render() {
    const columns = [
      {
        title: '任务ID', key: 'taskId', dataIndex: 'taskId', width: '20%', render: (record) => (
          <a onClick={() => this.props.onhandleClickName(record)} style={{ wordBreak: 'break-all' }}>{record}</a>
        )
      },
      { title: '任务描述', key: 'description', dataIndex: 'description' },
      {
        title: '发起人MSPID', key: 'requester', dataIndex: 'requester', width: '20%',
        filters: [
          { text: '自己', value: 'true' },
          { text: '其他', value: 'false' },
        ],
        filterMultiple: false,
        onFilter: (value, record) => record.owner.toString() === value,
      },
      {
        title: '创建时间', key: 'date', dataIndex: 'date', width: '18%',
        sorter: (a, b) => new Date(a.date) - new Date(b.date),
        render: (value) => (
          <span>{moment(value * 1000).format('YYYY-MM-DD HH:mm:ss')}</span>
        )
      },
      {
        title: '操作', key: 'action', width: '150px', render: (params) => (
          <p className={styles.tableAction}>
            {params.status === 'signing' ?
              <React.Fragment>
                {params.owner ?
                  <React.Fragment>
                    <Popconfirm title="是否将该任务提交至orderer？" onConfirm={() => this.handleSubmitTask(params.taskId)}>
                      <a>提交任务</a>
                    </Popconfirm>
                    <Popconfirm title="是否撤销该任务？" onConfirm={() => this.handleRecallTask(params.taskId)} >
                      <a>撤销任务</a>
                    </Popconfirm>
                  </React.Fragment> :
                  <Popconfirm title="是否同意通过该任务？" okText="同意" cancelText="拒绝" onConfirm={() => this.handleAgreeTask(params.taskId)} onCancel={() => this.handleShowModal(params.taskId)} >
                    <a>处理任务</a>
                  </Popconfirm>
                }
              </React.Fragment> :
              <Popconfirm title="是否删除该任务？" onConfirm={() => this.handleDeleteTask(params.taskId)} >
                <a>删除任务</a>
              </Popconfirm>
            }
          </p>
        )
      },
    ];
    const { isShow, taskId } = this.state;
    const { ChannelTask } = this.props;
    const { isLoading, taskList } = ChannelTask;

    return (
      <Fragment>
        <Table bordered rowKey="taskId" loading={isLoading} columns={columns} dataSource={taskList} />
        <RejectModal taskId={taskId} isShow={isShow} onCloseModal={() => this.handleCloseModal()} />
      </Fragment>
    );
  }
}

export default Form.create({})(TaskList);
