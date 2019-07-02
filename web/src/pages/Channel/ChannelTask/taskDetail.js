import React, { Component, Fragment } from 'react';
import moment from 'moment';
import { connect } from 'dva';
import { Table, Modal } from 'antd';

const resloveStatus = (status) => {
  switch (status) {
    case 'signing':
      return "进行中"
    case 'invalid':
      return "已失效"
    case 'end':
      return "已结束"
    default:
      break;
  }
}

class TaskDetail extends Component {
  state = {
    isShow: false,
    content: ''
  }
  // 打开内容详情弹窗
  handleShowContent = (content) => {
    this.setState({
      isShow: true,
      content
    });
  }
  // 关闭内容详情弹窗
  handleCloseContent = () => {
    this.setState({
      isShow: false,
      content: ''
    });
  }

  render() {
    const columns = [
      {
        title: '请求ID', key: 'requestId', dataIndex: 'requestId', width: '25%',
        render: (record) => (<span style={{ wordBreak: 'break-all' }}>{record}</span>)
      },
      { title: '任务描述', key: 'description', dataIndex: 'description' },
      { title: '所属通道', key: 'channelId', dataIndex: 'channelId', width: '10%' },
      {
        title: '状态', key: 'status', dataIndex: 'status', width: '8%',
        render: (value) => (
          <span>{resloveStatus(value)}</span>
        )
      },
      { title: '类型', key: 'requestType', dataIndex: 'requestType', width: '8%' },
      {
        title: '创建时间', key: 'date', dataIndex: 'date', width: '13%',
        render: (value) => (
          <span>{moment(value * 1000).format('YYYY-MM-DD HH:mm:ss')}</span>
        )
      },
      {
        title: '任务内容', key: 'content', dataIndex: 'content', width: '130px',
        render: (record) => (
          <a onClick={() => this.handleShowContent(record)}>查看任务详情</a>
        )
      },
    ];
    const { isLoading, taskDetail } = this.props;
    const { isShow, content } = this.state;

    return (
      <Fragment>
        <Table bordered rowKey="requestId" loading={isLoading} columns={columns} dataSource={taskDetail} />
        <Modal
          width={800}
          title="任务内容详情"
          visible={isShow}
          onCancel={this.handleCloseContent}
          footer={null}
        >
          <pre style={{ height: 500 }}>{content}</pre>
        </Modal>
      </Fragment>
    )
  }

}

export default TaskDetail;
