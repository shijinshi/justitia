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
  Upload,
  Tooltip,
  message,
} from 'antd';
import PageHeaderLayout from '@/components/PageHeaderWrapper';
import { clearSpacing } from '@/utils/utils';
import styles from '../index.less';

import org from '@/assets/组织.png';

const { TabPane } = Tabs;
const FormItem = Form.Item;
const RadioGroup = Radio.Group;

const host = process.env.NODE_ENV === "production" ? '/api' : `http://${window.hostIp}`;

@connect(({ network, loading }) => {
  return {
    network,
    loading: loading.effects['network/getConfigOrdererOrg'],
  };
})
class OrdererOrgNetwork extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      currentOrg: null,
      listSwitch: true,
      isGen: true,
      fileList: [],
      addSet: true,
      uploadSwitch: true,
      deleteSwitch: false,
    };
    this.downloadFile = React.createRef();
    this.changeGenerateCert = this.changeGenerateCert.bind(this);
    this.getCertId = this.getCertId.bind(this);
    this.beforeUpload = this.beforeUpload.bind(this);
    this.changeUpload = this.changeUpload.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.download = this.download.bind(this);
    this.delete = this.delete.bind(this);
    this.changeOper = this.changeOper.bind(this);
  }

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'network/getConfigOrdererOrg',
    });
    dispatch({
      type: 'network/ordererOrgGet',
    });
  }

  componentDidUpdate(prevProps, prevState) {
    if (!this.props.sessionId) return;
    const prevNetwork = prevProps.network;
    const prevPeerOrgDelete = prevNetwork.peerOrgDelete;
    const prevOrdererOrgCertId = prevNetwork.ordererOrgCertId;
    const { dispatch, network, form } = this.props;
    const { peerOrgDelete, ordererOrgCertId } = network;
    const { updateSwitch, certIdSwitch, deleteSwitch } = this.state;
    const { token } = this.props.sessionId;

    if (network.ordererOrgConfig && this.state.listSwitch) {
      this.setState({
        currentOrg: network.ordererOrgConfig,
        listSwitch: false,
      });
    }

    if (updateSwitch) {
      dispatch({
        type: 'network/getConfigOrdererOrg',
      });
      this.setState({
        updateSwitch: false,
      });
    }

    if (!prevPeerOrgDelete && peerOrgDelete) {
      this.uploadTable();
    } else if (
      prevPeerOrgDelete &&
      peerOrgDelete &&
      prevPeerOrgDelete.time !== peerOrgDelete.time
    ) {
      this.uploadTable();
    }

    if (!prevOrdererOrgCertId && ordererOrgCertId) {
      this.uploadTable();
      form.setFieldsValue({
        certId: ordererOrgCertId.certId,
      });
    } else if (
      prevOrdererOrgCertId &&
      ordererOrgCertId &&
      prevOrdererOrgCertId.certId !== ordererOrgCertId.certId
    ) {
      this.uploadTable();
      form.setFieldsValue({
        certId: ordererOrgCertId.certId,
      });
    }
  }

  uploadTable = () => {
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

  getCertId = () => {
    const { dispatch, form } = this.props;
    form.validateFields(['name', 'mspId', 'caHost', 'generateCert', 'firstSet'], (err, values) => {
      if (!err) {
        dispatch({
          type: 'network/getOrdererOrgCertId',
          payload: clearSpacing(values),
        });
        this.setState({
          uploadSwitch: true,
        });
      }
    });
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

  changeUpload = info => {
    const { form } = this.props;
    let { fileList, file } = info;
    fileList = fileList.slice(-1);
    this.setState({
      fileList,
    });
    file.response && file.response.message && message.info(file.response.message);
    if (file.status === 'done') {
      form.setFieldsValue({
        certId: '',
      });
      this.setState({
        uploadSwitch: false,
      });
      this.uploadTable();
    }
  };

  handleSubmit = e => {
    e.preventDefault();
    const { form, dispatch } = this.props;
    form.validateFields(['name', 'mspId', 'caHost', 'generateCert', 'firstSet'], (err, values) => {
      if (!err) {
        dispatch({
          type: 'network/getOrdererOrgCertId',
          payload: clearSpacing(values),
        });
      }
    });
  };

  downloadFileFunc(values, download, host, url) {
    const params = JSON.stringify(values);
    const option = {
      method: 'POST',
      body: params,
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json; charset=UTF-8',
      },
    };
    const fileName = `cert_${values.name}.zip`;
    fetch(`${host}${url}`, option)
      .then(res => {
        if (res.status !== 200) {
          message.error(res.message);
          return;
        }
        if (download) {
          return res.blob();
        } else {
          return res.json();
        }
      })
      .then(res => {
        if (!download) {
          message.info(res.message);
          return;
        }
        const downloadFile = this.downloadFile.current;
        const href = window.URL.createObjectURL(res);
        ReactDOM.render(<a href={href} download={fileName} />, downloadFile);
        downloadFile.querySelector('a').click();
      });
  }

  download = name => {
    const { sessionId } = this.props;
    const { token } = sessionId;
    this.downloadFileFunc({ name, token }, true, host, '/network/ordererOrg/cert/download');
  };

  delete = name => {
    const { dispatch, sessionId } = this.props;
    if (
      confirm(
        '删除现有Orderer组织的配置信息，与之关联的orderer节点信息也将被一并删除，继续删除吗？'
      )
    ) {
      dispatch({
        type: 'network/ordererOrgDelete',
        payload: {
          name,
          token: sessionId.token,
        },
      });
      this.setState({
        deleteSwitch: true,
      });
    }
  };

  changeOper = e => {
    const { form, network } = this.props;
    this.setState(
      {
        addSet: e.target.value,
        isGen: true,
      },
      () => {
        form.resetFields();
      }
    );
  };

  render() {
    const { currentOrg, fileList, addSet, uploadSwitch } = this.state;
    const { network, loading, sessionId } = this.props;
    const {
      ordererOrgConfig,
      ordererOrgCertId,
      peerOrgDownload,
      peerOrgDelete,
      ordererOrgGet,
    } = network;
    const { getFieldDecorator } = this.props.form;
    console.log('peerState', this.state, 'peerProps', this.props);

    const detailInfo = (
      <div className={styles.peer}>组织 - {currentOrg ? currentOrg.name : '当前没有组织'}</div>
    );

    ordererOrgConfig && (ordererOrgConfig.key = 0);
    const ordererOrgConfigCol = [
      {
        title: '名称',
        dataIndex: 'name',
      },
      {
        title: '组织的mspId',
        dataIndex: 'mspId',
      },
      {
        title: 'CA节点访问地址',
        dataIndex: 'caHost',
      },
      {
        title: '下载证书',
        dataIndex: 'download',
        render: (text, record) => {
          return (
            <a onClick={() => this.download(record.name)} href="javascript:;">
              下载
            </a>
          );
        },
      },
      {
        title: '删除',
        dataIndex: 'delete',
        render: (text, record) => {
          return (
            <a onClick={() => this.delete(record.name)} href="javascript:;">
              删除
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

    const cardLabel = (
      <span>
        caHost&nbsp;
        <Tooltip title="如果没有使用fabricCaHost可以为空">
          <Icon type="question-circle-o" />
        </Tooltip>
      </span>
    );

    return (
      <PageHeaderLayout detailInfo={detailInfo} logo={org}>
        <Tabs defaultActiveKey="1" className={styles.tabs}>
          <TabPane
            className={styles.tabChildren}
            tab={
              <span>
                <Icon type="file-text" />
                组织信息
              </span>
            }
            key="1"
          >
            <Row gutter={24}>
              <Col md={24}>
                <div className={styles.blockListTable}>
                  <div className={styles.blockTitle}>组织配置信息</div>
                  <Table
                    loading={loading}
                    pagination={{ pageSize: 10 }}
                    bordered
                    dataSource={ordererOrgConfig ? [ordererOrgConfig] : null}
                    columns={ordererOrgConfigCol}
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
                配置组织
              </span>
            }
            key="2"
          >
            <Row gutter={24}>
              <Col md={24}>
                <div className={styles.blockListTable}>
                  <div className={styles.blockTitle}>组织配置信息</div>
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
                    <FormItem {...formItemLayout} hasFeedback={true} label="组织名称">
                      {getFieldDecorator('name', {
                        initialValue: addSet ? '' : ordererOrgGet?ordererOrgGet.name:'',
                        rules: [
                          {
                            required: true,
                            message: '请输入组织名称！',
                          },
                          {
                            pattern: /^[a-zA-Z0-9]+$/g, message:'请输入字母或者数字的组合！'
                          }
                        ],
                      })(<Input disabled={!addSet} />)}
                    </FormItem>
                    <FormItem {...formItemLayout} hasFeedback={true} label="组织MSP">
                      {getFieldDecorator('mspId', {
                        initialValue: addSet ? '' : ordererOrgGet?ordererOrgGet.mspId:'',
                        rules: [
                          {
                            required: true,
                            message: '请输入组织所属组织的mspId！',
                          },
                        ],
                      })(<Input placeholder="请输入组织所属组织的mspId" />)}
                    </FormItem>
                    <FormItem {...formItemLayout} hasFeedback={true} label={cardLabel}>
                      {getFieldDecorator('caHost', {
                        initialValue: addSet ? '' : ordererOrgGet?ordererOrgGet.caHost:'',
                        rules: [
                          {
                            pattern: /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5]):([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/,
                            message: '输入的地址不正确！',
                          },
                        ],
                      })(<Input placeholder="请输入绑定的fabric节点访问地址！" />)}
                    </FormItem>
                    <FormItem {...formItemLayout} label="是否生成新证书">
                      {getFieldDecorator('generateCert', {
                        initialValue: addSet ? true : ordererOrgGet?ordererOrgGet.generateCert:'',
                      })(
                        <RadioGroup disabled={!addSet} onChange={this.changeGenerateCert}>
                          <Radio value={true}>是</Radio>
                          <Radio value={false}>否</Radio>
                        </RadioGroup>
                      )}
                    </FormItem>
                    <FormItem
                      wrapperCol={{ span: 8, offset: 8 }}
                      style={{ display: this.state.isGen ? 'block' : 'none' }}
                    >
                      <Button type="primary" block htmlType="submit">
                        确定
                      </Button>
                    </FormItem>
                    <FormItem
                      {...formItemLayout}
                      label="获取上传Id"
                      style={{ display: this.state.isGen ? 'none' : 'block' }}
                    >
                      <Row gutter={8}>
                        <Col span={12}>
                          {getFieldDecorator('certId', {
                            initialValue: '',
                            rules: [
                              {
                                required: true,
                                message: 'Id不能为空',
                              },
                            ],
                          })(<Input disabled={true} />)}
                        </Col>
                        <Col span={12}>
                          <Button onClick={this.getCertId} type="primary">
                            获取
                          </Button>
                        </Col>
                      </Row>
                    </FormItem>
                    <FormItem
                      {...formItemLayout}
                      label="上传证书"
                      extra="请先获取上传Id"
                      style={{ display: this.state.isGen ? 'none' : 'block' }}
                    >
                      {getFieldDecorator('cert', {
                        valuePropName: 'cert',
                        getValueFromEvent: this.normFile,
                        rules: [{ required: true }],
                      })(
                        <Upload
                          disabled={ordererOrgCertId && uploadSwitch ? false : true}
                          name="file"
                          fileList={fileList}
                          action={`http://${host}/network/ordererOrg/cert/upload/${
                            ordererOrgCertId ? ordererOrgCertId.certId : ''
                          }`}
                          beforeUpload={this.beforeUpload}
                          onChange={this.changeUpload}
                        >
                          <Button>
                            <Icon type="upload" /> 点击上传
                          </Button>
                        </Upload>
                      )}
                    </FormItem>
                  </Form>
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

const WrapOrdererOrgNetwork = Form.create({})(OrdererOrgNetwork);
export default WrapOrdererOrgNetwork;
