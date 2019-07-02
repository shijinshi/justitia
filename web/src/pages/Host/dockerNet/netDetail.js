import React from 'react';
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
  Upload,
  Tooltip,
  message,
} from 'antd';

const FormItem = Form.Item;
const RadioGroup = Radio.Group;
@connect(({ dockerNet, loading }) => {
  return {
    dockerNet,
    loading: loading.effects['dockerNet/handleGetNetDetail']
  };
})
class NetDetail extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      nowName: ''
    };

    this.handleClickDelete = this.handleClickDelete.bind(this);
  }

  componentDidMount() {
    const { networkName, dispatch, hostName } = this.props;
    if (networkName) {
      dispatch({
        type: 'dockerNet/handleGetNetDetail',
        payload: {
          networkName,
          hostName
        }
      });
      this.setState({
        nowName: networkName
      });
    }
  }

  componentWillReceiveProps(nextProps, nextState) {
    const { networkName, dispatch, hostName } = nextProps;
    const { nowName } = this.state;
    if (networkName && networkName !== nowName) {
      dispatch({
        type: 'dockerNet/handleGetNetDetail',
        payload: {
          networkName: networkName,
          hostName
        }
      });
      this.setState({
        nowName: networkName
      });
    }
  }

  handleClickDelete = () => {
    const { nowName } = this.state;
    const { dispatch, hostName } = this.props;
    dispatch({
      type: 'dockerNet/handleDeleteNet',
      payload: {
        networkName: nowName,
        hostName
      }
    })
  }

  render() {
    const { dockerNet } = this.props;
    const { netDetail } = dockerNet;
    const { nowName } = this.state;

    let dataInfo = [];
    if (netDetail) {
      dataInfo = [{ netDetail, key: 0, networkName: nowName }]
    }
    const columns = [{
      dataIndex: 'networkName',
      title: '网络名称'
    }, {
      dataIndex: 'netDetail',
      title: '网络详情',
      width: '70%',
      render: (text) => (<span style={{ wordBreak: 'break-all' }}>{JSON.stringify(text)}</span>)
    }, {
      dataIndex: 'delete',
      title: '删除网络',
      render: () => (<a onClick={this.handleClickDelete}>删除镜像</a>)
    }]

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

    return (
      <Table dataSource={dataInfo} columns={columns} />
    );
  }
}

const WrapNetDetail = Form.create({})(NetDetail);
export default WrapNetDetail;
