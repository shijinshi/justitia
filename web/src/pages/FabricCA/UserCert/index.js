import React from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'dva';
import moment from 'moment';
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
  message,
  Modal
} from 'antd';
import PageHeaderLayout from '@/components/PageHeaderWrapper';
import TabPaneCon from '@/components/TabPaneCon';
import WrapReEnroll from './ReEnroll';
import WrapRevokeCert from './revokeCert';
import WrapCreateCrl from './createCrl';
import { downloadFile } from '@/utils/utils';
import styles from '../style.less';
import org from '@/assets/组织.png';

const { TabPane } = Tabs;
const FormItem = Form.Item;
const Option = Select.Option;
const { TextArea } = Input;

const host = process.env.NODE_ENV === "production" ? '/api' : `http://${window.hostIp}`;

@connect(({ CAUserManager, loading }) => {
  return {
    CAUserManager,
    loading: loading.effects['CAUserManager/handleGetCertInfo']
  };
})
class UserCert extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      activeKey: '1',   //展开的tab页
      visible: false,
      nowStamp: '',
      modalTitle: '',
      update: false,
    };
    this.modalContent = '';
    this.downloadEle = React.createRef();
    this.Modal = '';
  }

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'CAUserManager/handleGetCAUser'
    })
  }

  componentDidUpdate() {
    const { dispatch, CAUserManager } = this.props;
    const { getCAUser } = CAUserManager;
    const { nowStamp, update } = this.state;
    if (getCAUser && nowStamp !== getCAUser.stamp && getCAUser.length) {
      dispatch({
        type: 'CAUserManager/handleGetCertInfo',
        payload: {
          serverName: getCAUser[0].serverName,
          userId: getCAUser[0].userId
        }
      })
      this.setState({
        nowStamp: getCAUser.stamp,
        serverName: getCAUser[0].serverName,
        userId: getCAUser[0].userId
      })
    }
    if (update) {
      dispatch({
        type: 'CAUserManager/handleGetCAUser'
      })
      this.setState({
        update: false
      })
    }
  }

  handleChangeTabs = (key) => {
    this.setState({
      activeKey: key
    })
  }

  changeSelectServer = (key) => {
    const { CAUserManager, dispatch } = this.props;
    const { getCAUser } = CAUserManager;
    this.setState({
      serverName: key,
      userId: getCAUser[0].userId
    })
    dispatch({
      type: 'CAUserManager/handleGetCertInfo',
      payload: {
        serverName: key,
        userId: getCAUser[0].userId
      }
    })
  }

  changeSelectUser = (e) => {
    const { dispatch } = this.props;
    const { serverName } = this.state;
    this.setState({
      userId: e
    })
    dispatch({
      type: 'CAUserManager/handleGetCertInfo',
      payload: {
        serverName,
        userId: e
      }
    })
  }

  handleReEnroll = (serial, aki, serverName, userId) => {
    this.Modal = <WrapReEnroll serial={serial} aki={aki} serverName={serverName} userId={userId} />
    this.setState({
      modalTitle: '重新登记证书',
      visible: true
    })
  }

  revokeCert = (serial, aki, serverName, userId) => {
    this.Modal = <WrapRevokeCert serial={serial} aki={aki} serverName={serverName} userId={userId} shouldUpdate={this.shouldUpdate} />
    this.setState({
      modalTitle: '注销证书',
      visible: true
    })
  }

  createCls = (serial, aki, serverName, userId) => {
    this.Modal = <WrapCreateCrl serial={serial} aki={aki} serverName={serverName} userId={userId} />
    this.setState({
      modalTitle: '获取吊销列表',
      visible: true
    })
  }

  download = (serial, aki) => {
    const params = {
      ele: this.downloadEle.current,
      fileName: `${serial}.zip`,
      url: `${host}/ca/cert/download?serial=${serial}&aki=${aki}`,
      type: 'get'
    }
    downloadFile(params)
  }

  handleOk = (e) => {
    console.log(e);
    this.setState({
      visible: false,
    });
  }

  handleCancel = (e) => {
    console.log(e);
    this.setState({
      visible: false,
    });
  }

  shouldUpdate = () => {
    this.setState({
      update: true
    })
  }

  render() {
    const { CAUserManager } = this.props;
    const { getCertInfo, getCAUser } = CAUserManager;
    const { activeKey, serverName, userId, modalTitle } = this.state;

    let serverArr = [];
    let userIdArr = [];
    const detailInfo = (
      <div className={styles.peer}>用户本地证书</div>
    );

    getCAUser && getCAUser.map((ele, i) => {
      if (serverArr.indexOf(ele.serverName) < 0) {
        serverArr.push(ele.serverName)
      }
      if (userIdArr.indexOf(ele.userId) < 0 && ele.serverName === serverName) {
        userIdArr.push(ele.userId)
      }
    })
    getCertInfo && getCertInfo.map((ele, i) => {
      return ele.key = i;
    })

    const columns = [{
      dataIndex: 'serialNumber',
      title: '证书序列号',
    }, {
      dataIndex: 'authorityKeyIdentifier',
      title: '证书创建者身份编号'
    }, {
      dataIndex: 'caUserId',
      title: '证书所属的CA用户',
    }, {
      dataIndex: 'serverName',
      title: '证书所属的CA服务'
    }, {
      dataIndex: 'createTime',
      title: '证书创建时间',
      render: (text) => (<span>{moment(text * 1000).format('YYYY-MM-DD HH:mm:ss')}</span>)
    }, {
      dataIndex: 'type',
      title: '证书类型'
    }, {
      dataIndex: 'user',
      title: '证书使用者',
      render: (text) => {
        if (!text) {
          return '无'
        }
      }
    }, {
      dataIndex: 'reenroll',
      title: '重新登记证书',
      render: (text, item) => (<a onClick={this.handleReEnroll.bind(this, item.serialNumber, item.authorityKeyIdentifier, item.serverName, item.caUserId)}>重新登记</a>)
    }, {
      dataIndex: 'download',
      title: '下载证书',
      render: (text, item) => (<a onClick={this.download.bind(this, item.serialNumber, item.authorityKeyIdentifier)}>下载</a>)
    }, {
      dataIndex: 'revokeCert',
      title: '注销证书',
      render: (text, item) => (<a onClick={this.revokeCert.bind(this, item.serialNumber, item.authorityKeyIdentifier, item.serverName, item.caUserId)}>注销证书</a>)
    }, {
      dataIndex: 'cls',
      title: '生成注销列表',
      render: (text, item) => (<a onClick={this.createCls.bind(this, item.serialNumber, item.authorityKeyIdentifier, item.serverName, item.caUserId)}>生成注销列表</a>)
    }]

    function SelectServer(props) {
      return <Select
        showSearch
        style={{ width: 200 }}
        placeholder="请选择主机"
        defaultValue={props.serverName}
        optionFilterProp="children"
        onChange={props.changeSelectServer}
        filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
      >
        {
          props.serverData && props.serverData.length > 0 ? props.serverData.map((item) => {
            return (<Option value={item} key={item}>{item}</Option>)
          }) : ''
        }
      </Select>
    }

    function SelectUser({ userId, changeSelectUser, userIdArr }) {
      return <Select
        showSearch
        style={{ width: 200 }}
        placeholder="请选择CA用户"
        optionFilterProp="children"
        defaultValue={userId}
        onChange={changeSelectUser}
        filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
      >
        {
          userIdArr && userIdArr.length > 0 ? userIdArr.map((item) => {
            return (<Option value={item} key={item}>{item}</Option>)
          }) : ''
        }
      </Select>
    }

    return (
      <PageHeaderLayout detailInfo={detailInfo} toggleSwitch={<SelectServer changeSelectServer={this.changeSelectServer} serverData={serverArr} serverName={serverName} />} logo={org}>
        <Tabs onChange={this.handleChangeTabs} activeKey={activeKey} className={styles.tabs}>
          <TabPane
            className={styles.tabChildren}
            tab={<span><Icon type="file-text" />证书列表</span>}
            key="1"
          >
            <TabPaneCon title="证书列表"
              children={<Table bordered dataSource={getCertInfo} columns={columns} />}
              select={<SelectUser userId={userId} changeSelectUser={this.changeSelectUser} userIdArr={userIdArr} />}
            />
          </TabPane>
        </Tabs>
        <div ref={this.downloadEle}></div>
        <Modal
          title={modalTitle}
          visible={this.state.visible}
          onOk={this.handleOk}
          onCancel={this.handleCancel}
        >
          {this.Modal}
        </Modal>
      </PageHeaderLayout>
    )
  }
}
const WrapUserCert = Form.create({})(UserCert);
export default WrapUserCert;