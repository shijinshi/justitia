import React from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'dva';
import {
  Row,
  Col,
  Icon,
  Tabs,
  Table,
  Menu,
  Dropdown,
  Button,
  message,
  Modal,
  Input,
  Form,
} from 'antd';
import PageHeaderLayout from '@/components/PageHeaderWrapper';
import WrapDeployPeer from './peerDeploy';
import WrapGenerateCert from '../generateCert';
import styles from '../index.less';

import peer from '@/assets/节点.png';

const { TabPane } = Tabs;

@connect(({ network, loading, peerModel }) => {
  return {
    network,
    peerModel,
    loading: loading.effects['network/getConfigPeer'],
  };
})
export default class PeerNetwork extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      currentPeer: null,
      listSwitch: true,
      updateSwitch: false,
      isShow: false,
      peerName: '',
      channelName: '',
      isError: '',
    };
    this.downloadFile = React.createRef();
    this.managePeer = this.managePeer.bind(this);
    this.peerDelete = this.peerDelete.bind(this);
    this.checkName = this.checkName.bind(this);
    this.updateTable = this.updateTable.bind(this);
  }

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'network/getConfigPeer',
    });
  }

  componentDidUpdate(prevProps) {
    const prevNetwork = prevProps.network;
    const prevPeerDelete = prevNetwork.peerDelete;
    const prevManagePeer = prevNetwork.managePeer;
    const prevFetching = prevProps.peerModel.isFetching;
    const { updateSwitch } = this.state;
    const { dispatch, network, peerModel } = this.props;
    const { peerDelete, managePeer } = network;
    const { isFetching } = peerModel;

    network.peerConfig &&
      this.state.listSwitch &&
      this.setState({
        currentPeer: network.peerConfig[0],
        listSwitch: false,
      });

    if (managePeer && !prevManagePeer) {
      dispatch({
        type: 'network/getConfigPeer',
      });
    } else if (prevManagePeer && managePeer && managePeer.time !== prevManagePeer.time) {
      dispatch({
        type: 'network/getConfigPeer',
      });
    }

    if (peerDelete && !prevPeerDelete) {
      this.updateTable();
    } else if (prevPeerDelete && peerDelete && peerDelete.time !== prevPeerDelete.time) {
      this.updateTable();
    }

    if (updateSwitch) {
      dispatch({
        type: 'network/getConfigPeer',
      });
      this.setState({
        updateSwitch: false,
      });
    }

    if (!isFetching && prevFetching) {
      this.setState({
        isShow: false,
      });
    }
  }

  updateTable() {
    this.setState({
      updateSwitch: true,
    });
  }

  managePeer = (name, oper, message) => {
    const { dispatch } = this.props;
    if (typeof message !== 'undefined') {
      if (confirm(message)) {
        dispatch({
          type: 'network/handleManagePeer',
          payload: {
            peerName: name,
            oper,
          },
        });
      }
    } else {
      dispatch({
        type: 'network/handleManagePeer',
        payload: {
          peerName: name,
          oper,
        },
      });
    }
  };

  peerDelete = peerName => {
    const { dispatch } = this.props;
    dispatch({
      type: 'network/peerDelete',
      payload: {
        peerName,
      },
    });
  };

  checkName = (rule, value, callback) => {
    const { data, type } = this.props;
    data.map((item, i) => {
      if (type === 'peer') {
        if (item.peerName === value) {
          callback('节点已经存在！');
        } else {
          callback();
        }
      } else {
        if (item.ordererName === value) {
          callback('节点已经存在！');
        } else {
          callback();
        }
      }
    });
  };
  // 打开加入通道Modal
  openJoinModal = peerName => {
    this.setState({
      isShow: true,
      peerName
    });
  }
  // 关闭Modal
  handleCloseModal = () => {
    this.setState({
      isShow: false,
      channelName: '',
      isError: false,
    });
  }
  // 获取通道名称
  handleChange = e => {
    const value = e.target.value;
    this.setState({
      channelName: value,
      isError: false,
    });
  }
  // 提交modal内容，加入通道
  handleSubmit = () => {
    const { dispatch } = this.props;
    const { channelName, peerName } = this.state;

    if (!channelName) {
      this.setState({
        isError: true,
      });
      return false;
    }
    dispatch({
      type: 'peerModel/handleAddPeer',
      payload: {
        channelName,
        peerName
      }
    });
  }

  render() {
    const { currentPeer, isShow, channelName, isError } = this.state;
    const { network, peerModel, loading, list } = this.props;
    const { peerConfig, isFetching } = network;

    const detailInfo = (
      <div className={styles.peer}>
        Peer节点
      </div>
    );

    peerConfig && peerConfig.map((ele, i) => {
      return ele.key = i
    })

    const peerConfigCol = [
      {
        title: '名称',
        dataIndex: 'peerName',
      },
      {
        title: '所属主机',
        dataIndex: 'hostName'
      },
      {
        title: '容器ID',
        dataIndex: 'containerId',
        width: '450px',
        render: (record) => (
          <div>{record}</div>
        )
      },
      // {
      //   title: 'orderer节点名称',
      //   dataIndex: 'ordererName',
      //   render: (text) => {
      //     if (!text) {
      //       return '-'
      //     } else {
      //       return text
      //     }
      //   }
      // },
      {
        title: 'couchdb容器名称',
        dataIndex: 'couchdbName',
        render: (text) => {
          if (!text) {
            return '-'
          } else {
            return text
          }
        }
      },
      {
        title: '是否使用TLS',
        dataIndex: 'tlsEnable',
        render: (text) => {
          if (text) {
            return '是'
          } else {
            return '否'
          }
        }
      },
      {
        title: '操作',
        dataIndex: 'delete',
        width: '150px',
        render: (text, item) => (
          <div className={styles.tableAction}>
            <a onClick={() => this.openJoinModal(item.peerName)}>加入通道</a>
            <a onClick={this.peerDelete.bind(this, item.peerName)}>删除</a>
          </div>
        )
      }
    ];

    const managePeerCol = [
      {
        title: '名称',
        dataIndex: 'peerName',
      },
      {
        title: '启动服务',
        dataIndex: 'start',
        render: (text, record) => {
          return (
            <a href="javascript:;" onClick={() => this.managePeer(record.peerName, 'start')}>
              启动
            </a>
          );
        },
      },

      {
        title: '重启服务',
        dataIndex: 'restart',
        render: (text, record) => {
          return (
            <a
              href="javascript:;"
              onClick={() => this.managePeer(record.peerName, 'restart', '确定重启吗？')}
            >
              重启
            </a>
          );
        },
      },
      {
        title: '停止服务',
        dataIndex: 'pause',
        render: (text, record) => {
          return (
            <a
              href="javascript:;"
              onClick={() => this.managePeer(record.peerName, 'pause', '确定停止吗？')}
            >
              停止
            </a>
          );
        },
      },
      {
        title: '继续运行服务',
        dataIndex: 'unpause',
        render: (text, record) => {
          return (
            <a
              href="javascript:;"
              onClick={() => this.managePeer(record.peerName, 'unpause')}
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
      <PageHeaderLayout
        detailInfo={detailInfo}
        logo={peer}
      >
        <Tabs defaultActiveKey="1" className={styles.tabs}>
          <TabPane
            className={styles.tabChildren}
            tab={
              <span>
                <Icon type="file-text" />
                节点信息
              </span>
            }
            key="1"
          >
            <Row gutter={24}>
              <Col md={24}>
                <div className={styles.blockListTable}>
                  <div className={styles.blockTitle}>节点列表</div>
                  <Table
                    loading={loading}
                    bordered
                    dataSource={peerConfig}
                    columns={peerConfigCol}
                  />
                </div>
              </Col>
              <Col md={24} style={{ marginTop: '24px' }}>
                <div className={styles.blockListTable}>
                  <div className={styles.blockTitle}>管理节点</div>
                  <Table
                    loading={loading}
                    bordered
                    dataSource={peerConfig}
                    columns={managePeerCol}
                  />
                </div>
              </Col>
            </Row>
            <Modal title="加入通道" visible={isShow} confirmLoading={isFetching} onCancel={() => this.handleCloseModal()} onOk={() => this.handleSubmit()}>
              <Row gutter={24} type="flex" align="middle">
                <Col span={4}>通道名称</Col>
                <Col span={20}>
                  <Input value={channelName} onChange={this.handleChange} style={{ border: isError ? '1px solid red' : '1px solid #d9d9d9' }} />
                </Col>
              </Row>
              <Row>
                <span style={{ marginLeft: 80, color: 'red', display: isError ? 'block' : 'none' }}>通道名称不能为空</span>
              </Row>
            </Modal>
          </TabPane>
          <TabPane
            className={styles.tabChildren}
            tab={
              <span>
                <Icon type="cloud-upload" />
                创建节点
              </span>
            }
            key="3"
          >
            <Row gutter={24}>
              <Col md={24}>
                <div className={styles.blockListTable}>
                  <div className={styles.blockTitle}>创建节点</div>
                  <WrapDeployPeer
                    updateTable={this.updateTable}
                  />
                </div>
              </Col>
            </Row>
          </TabPane>
        </Tabs>
        <div ref={this.downloadFile} style={{ display: 'none' }} />
      </PageHeaderLayout>
    );
  }
}
