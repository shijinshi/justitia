import React from 'react';
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
  Input,
} from 'antd';
import PageHeaderLayout from '@/components/PageHeaderWrapper';
import { clearSpacing } from '@/utils/utils';
import CouchdbDeploy from './couchdbDeploy';
import styles from '../index.less';

import database from '@/assets/数据库.png';

const { TabPane } = Tabs;
const Option = Select.Option;
const FormItem = Form.Item;
const RadioGroup = Radio.Group;

@connect(({ network, loading }) => {
  return {
    network,
    loading: loading.effects['network/getConfigCouchdb'],
  };
})
class CouchdbNetwork extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      currentCouchdb: null,
      listSwitch: true,
      isGen: true,
      setCouchdb: false,
      addSet: true,
      couchdbSwitch: true,
      updateSwitch: false,
    };
    this.changeGenerateCert = this.changeGenerateCert.bind(this);
    this.beforeUpload = this.beforeUpload.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.delete = this.delete.bind(this);
    this.toggleCouchdb = this.toggleCouchdb.bind(this);
    this.manageCouchdb = this.manageCouchdb.bind(this);
    this.changeOper = this.changeOper.bind(this);
    this.updateTable = this.updateTable.bind(this);
  }

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'network/getConfigCouchdb',
    });
    dispatch({
      type: 'network/couchdbImageVersion',
    });
  }

  componentDidUpdate(prevProps, prevState) {
    if (!this.props.sessionId) return;
    const prevNetwork = prevProps.network;
    const prevCouchdbDelete = prevNetwork.couchdbDelete;
    const prevManageCouchdb = prevNetwork.manageCouchdb;
    const prevSetCouchdb = prevNetwork.setCouchdb;
    const { dispatch, network } = this.props;
    const { couchdbDelete, manageCouchdb, setCouchdb } = network;
    const { updateSwitch } = this.state;

    if (network.couchdbConfig && this.state.listSwitch) {
      this.setState({
        currentCouchdb: network.couchdbConfig[0],
        listSwitch: false,
      });
    }

    if(setCouchdb && !prevSetCouchdb){
      this.updateTable();
    }else if(setCouchdb && prevSetCouchdb && setCouchdb.time !== prevSetCouchdb.time){
      this.updateTable();
    }

    if(manageCouchdb && !prevManageCouchdb){
      dispatch({
        type: 'network/getConfigCouchdb',
      });
    }else if(prevManageCouchdb && manageCouchdb && manageCouchdb.time !== prevManageCouchdb.time){
      dispatch({
        type: 'network/getConfigCouchdb',
      });
    }

    if (this.state.currentCouchdb && this.state.couchdbSwitch) {
      dispatch({
        type: 'network/couchdbGet',
        payload: {
          couchdbName: this.state.currentCouchdb.couchdbName,
        },
      });
      this.setState({
        couchdbSwitch: false,
      });
    }

    if (couchdbDelete && !prevCouchdbDelete) {
      this.updateTable();
    } else if (
      prevCouchdbDelete &&
      couchdbDelete &&
      couchdbDelete.time !== prevCouchdbDelete.time
    ) {
      this.updateTable();
    }

    if (updateSwitch) {
      dispatch({
        type: 'network/getConfigCouchdb',
      });
      this.setState({
        updateSwitch: false,
      });
    }
  }

  updateTable = () => {
    this.setState({
      updateSwitch: true,
    });
  };

  changeGenerateCert = e => {
    const value = e.target.value;
    if (value === false) {
      this.setState({
        isGen: false,
      });
    } else {
      this.setState({
        isGen: true,
      });
    }
  };

  beforeUpload = file => {
    const fileName = file.name;
    const matchArr = fileName.split('.');
    if(!matchArr){
        message.error('只能上传zip格式的文件！');
        return false;
    }else{
        const type = matchArr[matchArr.length-1];
        if (type !== 'zip') {
            message.error('只能上传zip格式的文件！');
            return false;
        }
    }
  };

  handleSubmit = e => {
    e.preventDefault();
    const { form, dispatch } = this.props;
    form.validateFields((err, values) => {
      if (!err) {
        dispatch({
          type: 'network/setCouchdb',
          payload: clearSpacing(values),
        });
        dispatch({
          type: 'network/getConfigCouchdb',
        });
      }
    });
  };

  delete = name => {
    const { dispatch, sessionId } = this.props;
    if (confirm('将要删除系统管理的couchdb节点信息，继续删除吗？')) {
      dispatch({
        type: 'network/couchdbDelete',
        payload: {
          couchdbName: name,
          token: sessionId.token,
        },
      });
    }
  };

  toggleCouchdb = ({ key }) => {
    console.log(key);
    const { couchdbConfig } = this.props.network;
    couchdbConfig.map((item, i) => {
      if (item.couchdbName === key) {
        this.setState({
          currentCouchdb: item,
          couchdbSwitch: true,
        });
      }
    });
  };

  manageCouchdb = (name, oper, message) => {
    const { dispatch } = this.props;
    if (typeof message !== 'undefined') {
      if (confirm(message)) {
        dispatch({
          type: 'network/manageCouchdb',
          payload: {
            couchdbName: name,
            oper,
          },
        });
      }
    } else {
      dispatch({
        type: 'network/manageCouchdb',
        payload: {
          couchdbName: name,
          oper,
        },
      });
    }
  };

  changeOper = e => {
    const { form } = this.props;
    this.setState(
      {
        addSet: e.target.value,
      },
      () => {
        form.resetFields();
      }
    );
  };

  render() {
    const { currentCouchdb, addSet } = this.state;
    const { network, loading, sessionId } = this.props;
    const {
      couchdbConfig,
      manageCouchdb,
      couchdbDelete,
      setCouchdb,
      couchdbGet,
      couchdbImageVersion,
    } = network;
    const { getFieldDecorator } = this.props.form;
    let token = null;
    if (sessionId) {
      token = sessionId.token;
    }
    console.log('couchdbState', this.state, 'couchdbProps', this.props);

    const detailInfo = (
      <div className={styles.peer}>
        Couchdb - {currentCouchdb ? currentCouchdb.couchdbName : '当前还没有节点'}
      </div>
    );

    const ToggleMenu = (
      <Menu onClick={this.toggleCouchdb}>
        {couchdbConfig &&
          couchdbConfig.map((item, i) => {
            return (
              <Menu.Item key={item.couchdbName}>
                <a style={{ color: '#008dff', cursor: 'pointer' }}>{item.couchdbName}</a>
              </Menu.Item>
            );
          })}
      </Menu>
    );
    const toggleSwitch = (
      <div className={styles.toggleSwitch}>
        <Dropdown overlay={ToggleMenu} placement="bottomLeft">
          <Button>
            切换couchdb <Icon type="down" />
          </Button>
        </Dropdown>
      </div>
    );

    let couchdbMan = [];
    couchdbConfig &&
      couchdbConfig.map((item, i) => {
        if (item.deployInfo !== null && item.deployedBySystem === true) {
          couchdbMan.push(item);
        }
        return (couchdbConfig[i].key = i);
      });
    const couchdbConfigCol = [
      {
        title: '名称',
        dataIndex: 'couchdbName',
      },
      {
        title: '节点是否被使用',
        dataIndex: 'used',
        render: text => {
          if (text === true) return '是';
          else return '否';
        },
      },
      {
        title: '使用该节点的peer',
        dataIndex: 'peers',
      },
      {
        title: '部署信息',
        children: [
          {
            title: 'ip',
            dataIndex: 'deployInfo.ip',
          },
          {
            title: '服务请求端口',
            dataIndex: 'deployInfo.requestPort',
          },
          {
            title: '容器名称',
            dataIndex: 'deployInfo.couchdbName',
          },
          {
            title: '镜像版本',
            dataIndex: 'deployInfo.imageVersion',
          },
        ],
      },
      {
        title: '删除',
        dataIndex: 'delete',
        render: (text, record) => {
          return (
            <a onClick={() => this.delete(record.couchdbName)} href="javascript:;">
              删除
            </a>
          );
        },
      },
    ];

    const manageCouchdbCol = [
      {
        title: '名称',
        dataIndex: 'couchdbName',
      },
      {
        title: '创建节点',
        dataIndex: 'create',
        render: (text, record) => {
          return (
            <a href="javascript:;" onClick={() => this.manageCouchdb(record.couchdbName, 'create')}>
              启动
            </a>
          );
        },
      },
      {
        title: '启动服务',
        dataIndex: 'start',
        render: (text, record) => {
          return (
            <a href="javascript:;" onClick={() => this.manageCouchdb(record.couchdbName, 'start')}>
              启动
            </a>
          );
        },
      },
      {
        title: '停止服务',
        dataIndex: 'stop',
        render: (text, record) => {
          return (
            <a
              href="javascript:;"
              onClick={() => this.manageCouchdb(record.couchdbName, 'stop', '确定停止服务吗？')}
            >
              停止
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
              onClick={() => this.manageCouchdb(record.couchdbName, 'restart', '确定重启服务吗？')}
            >
              重启
            </a>
          );
        },
      },
      {
        title: '移除',
        dataIndex: 'remove',
        render: (text, record) => {
          return (
            <a
              href="javascript:;"
              onClick={() => this.manageCouchdb(record.couchdbName, 'remove', '确定移除吗？')}
            >
              移除
            </a>
          );
        },
      },
      {
        title: '销毁',
        dataIndex: 'destroy',
        render: (text, record) => {
          return (
            <a
              href="javascript:;"
              onClick={() => this.manageCouchdb(record.couchdbName, 'destroy', '确定销毁吗？')}
            >
              销毁
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
      <PageHeaderLayout detailInfo={detailInfo} logo={database} toggleSwitch={toggleSwitch}>
        <Tabs defaultActiveKey="1" className={styles.tabs}>
          <TabPane
            className={styles.tabChildren}
            tab={
              <span>
                <Icon type="file-text" />
                Couchdb信息
              </span>
            }
            key="1"
          >
            <Row gutter={24}>
              <Col md={24}>
                <div className={styles.blockListTable}>
                  <div className={styles.blockTitle}>Couchdb信息</div>
                  <Table
                    loading={loading}
                    pagination={{ pageSize: 10 }}
                    bordered
                    dataSource={couchdbConfig}
                    columns={couchdbConfigCol}
                  />
                </div>
              </Col>
              <Col md={24} style={{ marginTop: '24px' }}>
                <div className={styles.blockListTable}>
                  <div className={styles.blockTitle}>管理Couchdb</div>
                  <Table
                    loading={loading}
                    pagination={{ pageSize: 10 }}
                    bordered
                    dataSource={couchdbMan}
                    columns={manageCouchdbCol}
                  />
                </div>
              </Col>
            </Row>
          </TabPane>
          <TabPane
            className={styles.tabChildren}
            tab={
              <span>
                <Icon type="setting" />
                配置Couchdb
              </span>
            }
            key="2"
          >
            <Row gutter={24}>
              <Col md={24}>
                <div className={styles.blockListTable}>
                  <div className={styles.blockTitle}>Couchdb配置信息</div>
                  <Form onSubmit={this.handleSubmit}>
                    <FormItem {...formItemLayout} label="选择操作">
                      {getFieldDecorator('firstSet', {
                        initialValue: addSet,
                      })(
                        <RadioGroup onChange={this.changeOper}>
                          <Radio value={true}>新增配置</Radio>
                          <Radio value={false}>修改配置</Radio>
                        </RadioGroup>
                      )}
                    </FormItem>
                    <FormItem {...formItemLayout} hasFeedback={true} label="节点名称">
                      {getFieldDecorator('name', {
                        initialValue: !addSet && couchdbGet ? couchdbGet.name : '',
                        rules: [
                          {
                            required: true,
                            message: '请输入节点名称！',
                          },
                          {
                            pattern: /^[0-9a-zA-Z]+$/,message:'只能输入数字或者字母组成的名称'
                          }
                        ],
                      })(<Input disabled={!addSet} />)}
                    </FormItem>
                    <FormItem {...formItemLayout} hasFeedback={true} label="节点访问ip">
                      {getFieldDecorator('ip', {
                        initialValue: !addSet && couchdbGet ? couchdbGet.ip : '',
                        rules: [
                          {
                            required: true,
                            message: 'ip不能为空！',
                          },
                          {
                            pattern: /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/,
                            message: '输入的ip不正确！',
                          },
                        ],
                      })(<Input placeholder="请输入节点访的ip" disabled={!addSet} />)}
                    </FormItem>
                    <FormItem {...formItemLayout} hasFeedback={true} label="服务端口">
                      {getFieldDecorator('port', {
                        initialValue: !addSet && couchdbGet ? couchdbGet.port : '',
                        rules: [
                          {
                            required: true,
                            message: '端口不能为空！',
                          },
                          {
                            pattern: /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/,
                            message: '输入的端口不正确！',
                          },
                        ],
                      })(<Input placeholder="请输入服务端口" disabled={!addSet} />)}
                    </FormItem>
                    <FormItem {...formItemLayout} hasFeedback={true} label="镜像版本">
                      {getFieldDecorator('imageVersion', {
                        initialValue: !addSet && couchdbGet ? couchdbGet.imageVersion : '',
                      })(
                        <Select
                          showSearch
                          placeholder="选择一个镜像版本"
                          optionFilterProp="children"
                          filterOption={(input, option) =>
                            option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
                          }
                        >
                          {couchdbImageVersion &&
                            couchdbImageVersion.map((item, i) => {
                              return (
                                <Option key={i} value={item}>
                                  {item}
                                </Option>
                              );
                            })}
                        </Select>
                      )}
                    </FormItem>
                    <FormItem {...formItemLayout} hasFeedback={true} label="容器名称">
                      {getFieldDecorator('couchdbName', {
                        initialValue: !addSet && couchdbGet ? couchdbGet.couchdbName : '',
                      })(<Input />)}
                    </FormItem>
                    <FormItem wrapperCol={{ span: 8, offset: 8 }}>
                      <Button type="primary" block htmlType="submit">
                        确定
                      </Button>
                    </FormItem>
                  </Form>
                </div>
              </Col>
            </Row>
          </TabPane>
          <TabPane
            className={styles.tabChildren}
            tab={
              <span>
                <Icon type="cloud-upload" />
                部署Couchdb
              </span>
            }
            key="3"
          >
            <Row gutter={24}>
              <Col md={24}>
                <div className={styles.blockListTable}>
                  <div className={styles.blockTitle}>Couchdb信息</div>
                  {couchdbImageVersion && token ? (
                    <CouchdbDeploy token={token} updateTable={this.updateTable} couchdbImageVersion={couchdbImageVersion} />
                  ) : (
                    ''
                  )}
                </div>
              </Col>
            </Row>
          </TabPane>
        </Tabs>
      </PageHeaderLayout>
    );
  }
}

const WrapCouchdbNetwork = Form.create({})(CouchdbNetwork);
export default WrapCouchdbNetwork;
