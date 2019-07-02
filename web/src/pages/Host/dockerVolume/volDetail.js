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
@connect(({ dockerVol, loading }) => {
  return {
    dockerVol,
    loading: loading.effects['dockerVol/handleGetVolDetail']
  };
})
class VolDetail extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      nowName: ''
    };

    this.handleClickDelete = this.handleClickDelete.bind(this);
  }

  componentDidMount() {
    const { volumeName, dispatch, hostName } = this.props;
    if (volumeName) {
      dispatch({
        type: 'dockerVol/handleGetVolDetail',
        payload: {
          volumeName,
          hostName
        }
      })
      this.setState({
        nowName: volumeName
      })
    }
  }

  componentWillReceiveProps(nextProps, nextState) {
    const { volumeName, dispatch, hostName } = nextProps;
    const { nowName } = this.state;

    if (volumeName && volumeName !== nowName) {
      dispatch({
        type: 'dockerVol/handleGetVolDetail',
        payload: {
          volumeName: volumeName,
          hostName
        }
      })
      this.setState({
        nowName: volumeName
      });
    }
  }

  handleClickDelete = () => {
    const { nowName } = this.state;
    const { dispatch, hostName } = this.props;
    dispatch({
      type: 'dockerVol/handleDeleteVol',
      payload: {
        volumeName: nowName,
        hostName
      }
    })
  }

  render() {
    const { dockerVol } = this.props;
    const { volDetail } = dockerVol;
    const { nowName } = this.state;

    let dataInfo = [];
    if (volDetail) {
      dataInfo = [{ volDetail, key: 0, volumeName: nowName }]
    }
    const columns = [{
      dataIndex: 'volumeName',
      title: '卷名称'
    }, {
      dataIndex: 'volDetail',
      title: '卷详情',
      width: '70%',
      render: (text) => (<span style={{ wordBreak: 'break-all' }}>{JSON.stringify(text)}</span>)
    }, {
      dataIndex: 'delete',
      title: '删除卷',
      width: '10%',
      render: () => (<a onClick={this.handleClickDelete}>删除卷</a>)
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

const WrapVolDetail = Form.create({})(VolDetail);
export default WrapVolDetail;
