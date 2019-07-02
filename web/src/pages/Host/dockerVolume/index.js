import React from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'dva';
import {
  Row,
  Col,
  Icon,
  Tabs,
  Table,
  Radio,
  Button,
  Form,
  Input,
  Select,
  Tooltip,
  message,
} from 'antd';
import PageHeaderLayout from '@/components/PageHeaderWrapper';
import TabPaneCon from '@/components/TabPaneCon';
import WrapVolDetail from './volDetail';
// import SelectHost from '../selectHost';
import styles from '../style.less';

import org from '@/assets/组织.png';

const { TabPane } = Tabs;
const Option = Select.Option;
const FormItem = Form.Item;
const RadioGroup = Radio.Group;

@connect(({ host, dockerVol, loading }) => {
  return {
    host,
    dockerVol,
    loading: loading.effects['dockerVol/handleGetDockerVol'],
  };
})
class DockerVol extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      activeKey: '1',   //展开的tab页
      nowStamp: 0,
    };
  }

  componentDidMount() {
    const { dispatch, location } = this.props;
    const { query } = location;
    dispatch({
      type: 'host/getHostHandle'
    });
    if (query.hostName) {
      dispatch({
        type: 'dockerVol/handleGetDockerVol',
        payload: { hostName: query.hostName }
      });
      dispatch({
        type: 'host/saveNowHost',
        payload: query.hostName
      })
      this.setState({
        hostName: query.hostName
      })
    }
  }

  componentDidUpdate() {
    const { dockerVol, dispatch, host } = this.props;
    const { nowHost } = host;
    const { addVol } = dockerVol;
    const { nowStamp, hostName } = this.state;
    // if (addVol) {
    //   const { stamp } = addVol;
    //   if (stamp && stamp !== nowStamp) {
    //     dispatch({
    //       type: 'dockerVol/handleGetDockerVol',
    //       payload: { hostName }
    //     });
    //     this.setState({
    //       nowStamp: stamp
    //     })
    //   }
    // }
    if (nowHost && nowHost !== hostName) {
      this.setState({
        hostName: nowHost
      })
      dispatch({
        type: 'dockerVol/handleGetDockerVol',
        payload: { hostName: nowHost }
      });
    }
  }

  handleChangeTabs = (key) => {
    this.setState({
      activeKey: key
    })
  }

  handleClickVol = (name) => {
    this.setState({
      activeKey: '2',
      volumeName: name
    });
  }

  changeSelectHost = (key) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'dockerVol/handleGetDockerVol',
      payload: { hostName: key }
    });
    dispatch({
      type: 'host/saveNowHost',
      payload: key
    });
    this.setState({
      hostName: key
    })
  }

  changeSelectVol = (key) => {
    const { dispatch } = this.props;
    const { hostName } = this.state;
    dispatch({
      type: 'dockerVol/handleGetVolDetail',
      payload: {
        hostName,
        volumeName: key
      }
    });
    this.setState({
      volumeName: key
    })
  }

  handleSubmit = (e) => {
    e.preventDefault();
    const { form, dispatch } = this.props;
    const { hostName } = this.state;
    if (hostName) {
      form.validateFields((err, values) => {
        if (!err) {
          dispatch({
            type: 'dockerVol/handleAddVol',
            payload: {
              ...values,
              hostName
            }
          })
        }
      })
    } else {
      message.error('请先选择主机！')
    }
  }


  render() {
    const { dockerVol, host, form } = this.props;
    const { getFieldDecorator } = form;
    const { hostData } = host;
    let { getVolList } = dockerVol;
    const { activeKey, volumeName, hostName } = this.state;

    const formItemLayout = {
      labelCol: {
        xs: { span: 24 },
        sm: { span: 8 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 8 },
      },
    };

    const detailInfo = (
      <div className={styles.peer}>网络管理</div>
    );

    function SelectHost(props) {
      return <Select
        showSearch
        style={{ width: 200 }}
        placeholder="请选择主机"
        defaultValue={props.hostName}
        optionFilterProp="children"
        onChange={props.changeSelectHost}
        filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
      >
        {
          props.hostData && props.hostData.length > 0 ? props.hostData.map((item) => {
            return (<Option value={item.hostName} key={item.hostName}>{item.hostName}</Option>)
          }) : ''
        }
      </Select>
    }

    const selectVol = (
      <Select
        showSearch
        style={{ width: 200, paddingTop: 5 }}
        placeholder="请选择网络"
        optionFilterProp="children"
        onChange={this.changeSelectVol}
        filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
      >
        {
          getVolList && getVolList.length > 0 ? getVolList.map((item) => {
            return (<Option value={item.Name} key={item.Id}>{item.Name}</Option>)
          }) : ''
        }
      </Select>
    )

    const addVol = (
      <Form onSubmit={this.handleSubmit}>
        <FormItem {...formItemLayout} label="卷名称">
          {getFieldDecorator('volumeName', {
            rules: [
              {
                required: true, message: '请输入卷名称！'
              },
              {
                pattern: /^[0-9a-zA-Z_]+$/, message: '只能输入数字或者字母下划线组成的名称'
              }
            ],
          })(<Input placeholder="输入数字或者字母下划线组成的名称" />)}
        </FormItem>
        <FormItem wrapperCol={{ span: 8, offset: 8 }}>
          <Button type="primary" block htmlType="submit">
            增加
          </Button>
        </FormItem>
      </Form>
    )

    getVolList && getVolList.map((item, index) => {
      return item.key = index
    });

    const netColumns = [{
      dataIndex: 'Name',
      title: '卷名称',
      width: '35%',
      render: (text) => (<a title={text} style={{ wordBreak: 'break-all' }} onClick={this.handleClickVol.bind(this, text)}>{text}</a>)
    }, {
      dataIndex: 'Driver',
      title: '数据卷驱动',
      render: (text) => (<span>{text}</span>)
    }, {
      dataIndex: 'Mountpoint',
      title: '挂载点',
      render: (text) => (<span style={{ wordBreak: 'break-all' }}>{text}</span>)
    }];

    return (
      <PageHeaderLayout detailInfo={detailInfo} toggleSwitch={<SelectHost changeSelectHost={this.changeSelectHost} hostData={hostData} hostName={hostName} />} logo={org}>
        <Tabs onChange={this.handleChangeTabs} activeKey={activeKey} className={styles.tabs}>
          <TabPane
            className={styles.tabChildren}
            tab={
              <span>
                <Icon type="file-text" />
                卷列表
                </span>
            }
            key="1"
          >
            <TabPaneCon children={<Table dataSource={getVolList} columns={netColumns} />} title="卷列表" />
            <TabPaneCon children={addVol} title="增加卷" />
          </TabPane>
          <TabPane
            className={styles.tabChildren}
            tab={
              <span>
                <Icon type="file-text" />
                卷详情
                </span>
            }
            key="2"
          >
            <TabPaneCon children={<WrapVolDetail hostName={hostName} volumeName={volumeName} />} select={selectVol} title="卷详情" />

          </TabPane>

        </Tabs>
      </PageHeaderLayout>
    );
  }
}

const WrapDockerVol = Form.create({})(DockerVol);
export default WrapDockerVol;
