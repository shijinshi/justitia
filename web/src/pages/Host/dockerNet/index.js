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
import WrapNetDetail from './netDetail';
// import SelectHost from '../selectHost';
import styles from '../style.less';

import org from '@/assets/组织.png';

const { TabPane } = Tabs;
const Option = Select.Option;
const FormItem = Form.Item;
const RadioGroup = Radio.Group;

@connect(({ host, dockerNet, loading }) => {
  return {
    host,
    dockerNet,
    loading: loading.effects['dockerNet/handleGetDockerNet'],
  };
})
class DockerNet extends React.Component {
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
        type: 'dockerNet/handleGetDockerNet',
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
    const { dockerNet, dispatch, host } = this.props;
    const { nowHost } = host;
    const { addNet } = dockerNet;
    const { nowStamp, hostName } = this.state;
    // if (addNet) {
    //   const { stamp } = addNet;
    //   if (stamp && stamp !== nowStamp) {
    //     const { Warnings } = addNet
    //     if (Warnings) {
    //       message.error(Warnings)
    //     } else {
    //       dispatch({
    //         type: 'dockerNet/handleGetDockerNet',
    //         payload: { hostName }
    //       });
    //     }
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
        type: 'dockerNet/handleGetDockerNet',
        payload: { hostName: nowHost }
      });
    }
  }


  handleChangeTabs = (key) => {
    this.setState({
      activeKey: key
    })
  }

  handleClickNet = (name) => {
    this.setState({
      activeKey: '2',
      networkName: name
    })
  }

  changeSelectHost = (key) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'dockerNet/handleGetDockerNet',
      payload: { hostName: key }
    })
    dispatch({
      type: 'host/saveNowHost',
      payload: key
    })
    this.setState({
      hostName: key
    })
  }

  changeSelectNet = (key) => {
    const { dispatch } = this.props;
    const { hostName } = this.state;
    dispatch({
      type: 'dockerNet/handleGetNetDetail',
      payload: {
        hostName,
        networkName: key
      }
    });
    this.setState({
      networkName: key
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
            type: 'dockerNet/handleAddNet',
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
    const { dockerNet, host, form } = this.props;
    const { getFieldDecorator } = form;
    const { hostData } = host;
    let { getNetList } = dockerNet;
    const { activeKey, networkName, hostName } = this.state;

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

    const selectNet = (
      <Select
        showSearch
        style={{ width: 200, paddingTop: 5 }}
        placeholder="请选择网络"
        optionFilterProp="children"
        onChange={this.changeSelectNet}
        filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
      >
        {
          getNetList && getNetList.length > 0 ? getNetList.map((item) => {
            return (<Option value={item.Name} key={item.Name}>{item.Name}</Option>)
          }) : ''
        }
      </Select>
    )

    const addNet = (
      <Form onSubmit={this.handleSubmit}>
        <FormItem {...formItemLayout} label="网络名称">
          {getFieldDecorator('networkName', {
            rules: [
              {
                required: true, message: '请输入网络名称！'
              },
              {
                pattern: /^[0-9a-zA-Z_]+$/, message: '只能输入数字或者字母下划线组成的名称'
              }
            ],
          })(<Input placeholder="输入数字或者字母下划线组成的名称" />)}
        </FormItem>
        <FormItem wrapperCol={{ span: 8, offset: 8 }}>
          <Button type="primary" block htmlType="submit">增加</Button>
        </FormItem>
      </Form>
    )


    getNetList && getNetList.map((item, index) => {
      return item.key = index
    })

    const netColumns = [{
      dataIndex: 'Name',
      title: '网络名称',
      render: (text) => (<a title={text} onClick={this.handleClickNet.bind(this, text)}>{text}</a>)
    }, {
      dataIndex: 'Scope',
      title: '网络范围',
      render: (text) => (<span>{text}</span>)
    }, {
      dataIndex: 'Driver',
      title: '网络模式',
      render: (text) => (<span>{text}</span>)
    }];

    return (
      <PageHeaderLayout detailInfo={detailInfo} toggleSwitch={<SelectHost hostName={hostName} hostData={hostData} changeSelectHost={this.changeSelectHost} />} logo={org}>
        <Tabs onChange={this.handleChangeTabs} activeKey={activeKey} className={styles.tabs}>
          <TabPane
            className={styles.tabChildren}
            tab={<span><Icon type="file-text" />网络列表</span>}
            key="1"
          >
            <TabPaneCon children={<Table dataSource={getNetList} columns={netColumns} />} title="网络列表" />
            <TabPaneCon children={addNet} title="增加网络" />
          </TabPane>
          <TabPane
            className={styles.tabChildren}
            tab={<span><Icon type="file-text" />网络详情</span>}
            key="2"
          >
            <TabPaneCon children={<WrapNetDetail hostName={hostName} networkName={networkName} />} select={selectNet} title="网络详情" />
          </TabPane>
        </Tabs>
      </PageHeaderLayout>
    );
  }
}

const WrapDockerNet = Form.create({})(DockerNet);
export default WrapDockerNet;
