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
  Select,
  Menu,
  Dropdown,
  Button,
  Form,
  message,
  Upload,
} from 'antd';
import PageHeaderLayout from '@/components/PageHeaderWrapper';
import TabPaneCon from '@/components/TabPaneCon';
import WrapGenerateCert from '../generateCert';
import WrapOrdererDeploy from './ordererDeploy';
import WrapAddConsortium from './creatConsortium';
import WrapDelConsortium from './delConsortium';
import styles from '../index.less';

import peer from '@/assets/节点.png';

const { TabPane } = Tabs;
const Option = Select.Option;
const FormItem = Form.Item;
const RadioGroup = Radio.Group;

@connect(({ network, loading, consortiumModel }) => {
  return {
    network,
    consortiumModel,
    loading: loading.effects['network/getConfigOrderer'],
  };
})
class OrdererNetwork extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      currentOrderer: null,
      listSwitch: true,
      certId: null,
      shouldCheck: true,
      disabled: false,
      certTypeFlag: true,
      certType: 'tls',
      tlsFileList: [],
      addSet: true,
      isShow: false,
      ordererName: "",
    };
    this.downloadFile = React.createRef();
    this.deleteOrderer = this.deleteOrderer.bind(this);
    this.updateTable = this.updateTable.bind(this);
  }

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'network/getConfigOrderer',
    });
  }

  componentDidUpdate(prevProps, prevState) {
    const prevNetwork = prevProps.network;
    const prevOrdererDelete = prevNetwork.ordererDelete;

    const { dispatch, network, consortiumModel } = this.props;
    const { ordererConfig, ordererDelete } = network;
    const { isFetching, isClose } = consortiumModel;
    const { updateSwitch, isShow } = this.state;
    if (ordererConfig && this.state.listSwitch) {
      this.setState({
        currentOrderer: ordererConfig[0],
        listSwitch: false,
      });
    }

    if (ordererDelete && !prevOrdererDelete) {
      this.updateTable();
      ordererConfig.length === 1 && this.setState({
        currentOrderer: null
      })
    } else if (
      prevOrdererDelete &&
      ordererDelete &&
      ordererDelete.time !== prevOrdererDelete.time
    ) {
      this.updateTable();
      ordererConfig.length === 1 && this.setState({
        currentOrderer: null
      })
    }

    if (updateSwitch) {
      dispatch({
        type: 'network/getConfigOrderer',
      });
      this.setState({
        updateSwitch: false,
      });
    }
    // 删除成功后关闭弹窗
    if (isShow && prevState.isShow && isClose) {
      this.setState({
        isShow: false
      });
    }
  }

  updateTable() {
    this.setState({
      updateSwitch: true,
    });
  }

  beforeUpload = file => {
    const fileName = file.name;
    const matchArr = fileName.split('.');
    if (!matchArr) {
      message.error('只能上传zip格式的文件！');
      return false;
    } else {
      const type = matchArr[matchArr.length - 1];
      if (type !== 'zip') {
        message.error('只能上传zip格式的文件！');
        return false;
      }
    }
  };

  deleteOrderer = ordererName => {
    const { dispatch } = this.props;
    if (confirm('确定删除吗？')) {
      dispatch({
        type: 'network/ordererDelete',
        payload: { ordererName },
      });
    }
  };

  managePeer = (name, oper, message) => {
    const { dispatch } = this.props;
    if (typeof message !== 'undefined') {
      if (confirm(message)) {
        dispatch({
          type: 'network/handleManageOrderer',
          payload: {
            ordererName: name,
            oper,
          },
        });
      }
    } else {
      dispatch({
        type: 'network/handleManageOrderer',
        payload: {
          ordererName: name,
          oper,
        },
      });
    }
  };
  // 打开删除成员弹窗
  showDelModal = (value) => {
    this.setState({
      isShow: true,
      ordererName: value
    });
  }
  // 关闭删除弹窗
  closeDelModal = () => {
    this.setState({
      isShow: false
    });
  }

  render() {
    const { currentOrderer, tlsFileList, addSet, isShow, ordererName } = this.state;
    const { network, loading } = this.props;
    const {
      ordererConfig,
      ordererDelete,
      isFetching
    } = network;
    const { getFieldDecorator } = this.props.form;

    const detailInfo = (
      <div className={styles.peer}>
        Orderer节点
      </div>
    );

    ordererConfig &&
      ordererConfig.map((item, i) => {
        return (ordererConfig[i].key = i);
      });
    const ordererConfigCol = [
      { title: '名称', key: 'ordererName', dataIndex: 'ordererName', },
      { title: '节点所属主机', key: 'hostName', dataIndex: 'hostName' },
      {
        title: '容器ID', key: 'containerId', dataIndex: 'containerId', width: '18%',
        render: (text) => {
          if (text) {
            return <span title={text}>{text.slice(0, 30) + '...'}</span>
          } else {
            return '-'
          }
        }
      },
      { title: '创建者', key: 'creator', dataIndex: 'creator' },
      { title: '节点所在网络', key: 'dockerNetwork', dataIndex: 'dockerNetwork' },
      {
        title: '是否使用了TLS', key: 'tlsEnable', dataIndex: 'tlsEnable',
        render: (text) => {
          if (text) {
            return '是'
          } else {
            return '否'
          }
        }
      },
      {
        title: '操作', key: 'delete', dataIndex: 'delete', width: 180,
        render: (text, record) => {
          return (
            <span className={styles.delAction}>
              <a href="javascript:;" onClick={() => this.deleteOrderer(record.ordererName)}>
                删除节点
              </a>
              <a href="javascript:;" onClick={() => this.showDelModal(record.ordererName)}>
                删除成员
              </a>
            </span>
          );
        },
      },
    ];

    const managePeerCol = [
      { title: '名称', key: 'ordererName', dataIndex: 'ordererName', },
      {
        title: '启动服务', key: 'start', dataIndex: 'start',
        render: (text, record) => {
          return (
            <a href="javascript:;" onClick={() => this.managePeer(record.ordererName, 'start')}>
              启动
            </a>
          );
        },
      },

      {
        title: '重启服务', key: 'restart', dataIndex: 'restart',
        render: (text, record) => {
          return (
            <a
              href="javascript:;"
              onClick={() => this.managePeer(record.ordererName, 'restart', '确定重启吗？')}
            >
              重启
            </a>
          );
        },
      },
      {
        title: '停止服务', key: 'pause', dataIndex: 'pause',
        render: (text, record) => {
          return (
            <a
              href="javascript:;"
              onClick={() => this.managePeer(record.ordererName, 'pause', '确定停止吗？')}
            >
              停止
            </a>
          );
        },
      },
      {
        title: '继续运行服务', key: 'unpause', dataIndex: 'unpause',
        render: (text, record) => {
          return (
            <a
              href="javascript:;"
              onClick={() => this.managePeer(record.ordererName, 'unpause')}
            >
              继续运行服务
            </a>
          );
        },
      },

    ];

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
      <PageHeaderLayout detailInfo={detailInfo} logo={peer}>
        <Tabs defaultActiveKey="1" className={styles.tabs}>
          <TabPane
            className={styles.tabChildren}
            tab={<span><Icon type="file-text" />节点信息</span>}
            key="1"
          >
            <TabPaneCon children={<Table bordered loading={isFetching} dataSource={ordererConfig} columns={ordererConfigCol} />} title="Orderer列表" />
            <TabPaneCon children={<Table bordered loading={isFetching} dataSource={ordererConfig} columns={managePeerCol} />} title="管理Orderer节点" />
            <WrapDelConsortium isShow={isShow} ordererName={ordererName} onCloseModal={this.closeDelModal} />
          </TabPane>
          <TabPane
            className={styles.tabChildren}
            tab={<span><Icon type="setting" />创建节点</span>}
            key="2"
          >
            <TabPaneCon children={<WrapOrdererDeploy updateTable={this.updateTable} />} title="创建Orderer节点" />
          </TabPane>
          <TabPane
            className={styles.tabChildren}
            tab={<span><Icon type="setting" />添加联盟成员</span>}
            key="3"
          >
            <TabPaneCon children={<WrapAddConsortium />} title="添加联盟成员" />
          </TabPane>
        </Tabs>
        <div ref={this.downloadFile} style={{ display: 'none' }} />
      </PageHeaderLayout>
    );
  }
}

const WrapOrdererNetwork = Form.create({})(OrdererNetwork);
export default WrapOrdererNetwork;
