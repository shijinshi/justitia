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
  message,
  Button,
  Form,
  Input,
  Upload,
} from 'antd';
import { clearSpacing } from '@/utils/utils';

const { TabPane } = Tabs;
const Option = Select.Option;
const FormItem = Form.Item;
const RadioGroup = Radio.Group;

const host = process.env.NODE_ENV === "production" ? '/api' : `http://${window.hostIp}`;

@connect(({ network, loading }) => {
  return {
    network,
    loading: loading.effects['network/peerGetChannelList'],
  };
})
class PeerSetting extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      listSwitch: true,
      shouldCheck: true,
      disabled: false,
      fileList: [],
      addSet: true,
      peerGetSwitch: true,
      uploadSwitch: true,
      peerCertIdSwitch: true,
      setFieldSwitch: true,
    };
    this.changeDeployed = this.changeDeployed.bind(this);
    this.getCertId = this.getCertId.bind(this);
    this.beforeUpload = this.beforeUpload.bind(this);
    this.changeUpload = this.changeUpload.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.changeOper = this.changeOper.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    if (
      nextProps.currentPeer &&
      this.props.currentPeer &&
      nextProps.currentPeer.peerName !== this.props.currentPeer.peerName
    ) {
      this.props.form.setFieldsValue({
        certId: '',
        uploadSwitch: false,
        fileList: [],
      });
    }
  }

  componentDidUpdate(prevProps) {
    const prevNetwork = prevProps.network;
    const { network, form, updateTable } = this.props;
    const { peerCertIdSwitch, uploadSwitch, setFieldSwitch } = this.state;
    const prevPeerCertId = prevNetwork.peerCertId;
    const { peerCertId } = network;
    if (network && peerCertIdSwitch && prevNetwork) {
      if (peerCertId && !prevNetwork.peerCertId) {
        updateTable();
        form.setFieldsValue({
          certId: peerCertId.certId,
        });
      } else if (peerCertId && prevPeerCertId && prevPeerCertId.certId !== peerCertId.certId) {
        updateTable();
        form.setFieldsValue({
          certId: peerCertId.certId,
        });
      }
    }
  }

  changeDeployed = e => {
    const value = e.target.value;
    if (value === false) {
      this.setState(
        {
          shouldCheck: false,
          disabled: true,
        },
        () => {
          const { form } = this.props;
          form.resetFields([
            'ip',
            'requestPort',
            'chaincodePort',
            'blockPort',
            'ordererName',
          ]);
          form.setFieldsValue({ deployed: false });
          form.validateFields(
            ['ip', 'requestPort', 'chaincodePort', 'blockPort', 'ordererName'],
            { force: true }
          );
        }
      );
    } else {
      this.setState({
        shouldCheck: true,
        disabled: false,
      });
    }
  };

  getCertId = () => {
    const { dispatch, form } = this.props;
    form.validateFields(
      [
        'firstSet',
        'name',
        'orgName',
        'deployed',
        'ip',
        'requestPort',
        'chaincodePort',
        'blockPort',
        'hostName',
        'useTls',
        'ordererName',
        'imageVersion',
        'containerName',
        'couchdbIp',
        'couchdbPort',
      ],
      (err, values) => {
        if (!err) {
          dispatch({
            type: 'network/getPeerCertId',
            payload: clearSpacing(values),
          });
        }
      }
    );
    this.setState({
      peerCertIdSwitch: true,
      uploadSwitch: true,
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
    console.log('info', info);
    const { updateTable, form, updateList } = this.props;
    let { fileList, file } = info;
    fileList = fileList.slice(-1);
    this.setState({
      fileList,
      uploadSwitch: false,
    });

    if (file.status === 'done') {
      file.response && file.response.message && message.info(file.response.message);
      updateTable();
      updateList();
      form.setFieldsValue({
        certId: '',
      });
    }
    
  };

  changeOper = e => {
    const { network, form } = this.props;
    const { peerGet } = network;
    let deployed = null;
    let name = null;
    if(peerGet){
      deployed = peerGet.deployed;
      name = peerGet.name;
    }
    form.setFieldsValue({
      name:name
    })
    this.setState({
      addSet: e.target.value,
    },()=>{
      form.resetFields()
    });
    if(!deployed){
      this.setState({
        disabled: !deployed,
        shouldCheck: deployed,
      },()=>{
        form.resetFields()
      });
    }
  };

  handleSubmit = (e) => {
    e.preventDefault();
    const { dispatch, form } = this.props;
    form.validateFields(
      [
        'name',
        'orgName',
        'deployed',
        'ip',
        'requestPort',
        'chaincodePort',
        'blockPort',
        'hostName',
        'useTls',
        'ordererName',
        'firstSet',
        'imageVersion',
        'containerName',
        'couchdbIp',
        'couchdbPort',
      ],
      (err, values) => {
        if (!err) {
          
          dispatch({
            type: 'network/getPeerCertId',
            payload: clearSpacing(values),
          });
        }
      }
    );
  };

  checkName = (rule, value, callback) => {
    const { peerConfig } = this.props.network;
    let flag = false;
    if (peerConfig && peerConfig.length > 0) {
      peerConfig.forEach(item => {
        if (value === item.peerName) {
          flag = true;
        }
      });
    }
    if (flag) {
      callback('节点已存在！');
    } else {
      callback();
    }
  };

  render() {
    const { fileList, addSet, peerCertIdState, uploadSwitch } = this.state;
    const { network, currentPeer } = this.props;
    const {
      peerChannelList,
      peerConfig,
      ordererConfig,
      peerCertId,
      peerImageVersion,
      peerGet,
    } = network;
    const { getFieldDecorator } = this.props.form;
    console.log('peerSetting.props', this.props, 'peerSetting.state', this.state);
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

    ordererConfig &&
      ordererConfig.map((item, i) => {
        return (ordererConfig[i].key = i);
      });
    return (
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
            initialValue: !addSet && peerGet ? peerGet.name : '',
            rules: [
              {
                required: true,
                message: '请输入节点名称！',
              },
              {
                validator: addSet ? this.checkName : '',
                message: '节点名称已经存在！',
              },
              {
                pattern: /^[0-9a-zA-Z]+$/,message:'只能输入数字或者字母组成的名称'
              }
            ],
          })(<Input disabled={!addSet} />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="节点域名">
          {getFieldDecorator('hostName', {
            initialValue: !addSet && peerGet ? peerGet.hostName : '',
            rules: [
              {
                required: true,
                message: '请输入节点域名！',
              },
              {
                pattern: /^[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+.?$/,
                message: '输入的域名不正确！',
              },
            ],
          })(<Input />)}
        </FormItem>
        <FormItem {...formItemLayout} label="是否已部署">
          {getFieldDecorator('deployed', {
            initialValue: !addSet && peerGet ? peerGet.deployed : '',
            rules: [
              {
                required: true,
                message: '请选择是否已部署！',
              },
            ],
          })(
            <RadioGroup onChange={this.changeDeployed}>
              <Radio value={true}>是</Radio>
              <Radio value={false}>否</Radio>
            </RadioGroup>
          )}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="IP">
          {getFieldDecorator('ip', {
            initialValue: !addSet && peerGet ? peerGet.ip : '',
            rules: [
              {
                required: this.state.shouldCheck,
                message: '请输入可以访问到节点的IP！',
              },
              {
                pattern: /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/,
                message: '输入的IP不正确！',
              },
            ],
          })(<Input disabled={this.state.disabled} placeholder="输入可以访问到节点的IP" />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="节点请求端口">
          {getFieldDecorator('requestPort', {
            initialValue: !addSet && peerGet ? peerGet.requestPort : '',
            rules: [
              {
                required: this.state.shouldCheck,
                message: '请输入节点请求端口！',
              },
              {
                pattern: /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/,
                message: '输入的端口不正确！',
              },
            ],
          })(<Input disabled={this.state.disabled} />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="链码事件监听端口">
          {getFieldDecorator('chaincodePort', {
            initialValue: !addSet && peerGet ? peerGet.chaincodePort : '',
            rules: [
              {
                required: this.state.shouldCheck,
                message: '请输入链码事件监听端口！',
              },
              {
                pattern: /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/,
                message: '输入的端口不正确！',
              },
            ],
          })(<Input disabled={this.state.disabled} />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="区块事件监听端口">
          {getFieldDecorator('blockPort', {
            initialValue: !addSet && peerGet ? peerGet.blockPort : '',
            rules: [
              {
                required: this.state.shouldCheck,
                message: '请输入区块事件监听端口！',
              },
              {
                pattern: /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/,
                message: '输入的端口不正确！',
              },
            ],
          })(<Input disabled={this.state.disabled} />)}
        </FormItem>
        
        <FormItem {...formItemLayout} label="是否开启了tls">
          {getFieldDecorator('useTls', {
            initialValue: !addSet && peerGet ? peerGet.useTls : true,
          })(
            <RadioGroup disabled={this.state.disabled}>
              <Radio value={true}>是</Radio>
              <Radio value={false}>否</Radio>
            </RadioGroup>
          )}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="orderer节点名称">
          {getFieldDecorator('ordererName', {
            initialValue: !addSet && peerGet ? peerGet.ordererName : '',
            rules: [
              {
                required: this.state.shouldCheck,
                message: '请输入orderer节点名称！',
              },
            ],
          })(
            <Select disabled={this.state.disabled} placeholder="选择通道">
              {ordererConfig &&
                ordererConfig.map((item, i) => {
                  return (
                    <Option key={i} value={item.ordererName}>
                      {item.ordererName}
                    </Option>
                  );
                })}
            </Select>
          )}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="镜像版本">
          {getFieldDecorator('imageVersion', {
            initialValue: !addSet && peerGet ? peerGet.imageVersion : '',
          })(
            <Select
              showSearch
              placeholder="选择一个镜像版本"
              optionFilterProp="children"
              disabled={this.state.disabled}
              filterOption={(input, option) =>
                option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
              }
            >
              {peerImageVersion &&
                peerImageVersion.map((item, i) => {
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
          {getFieldDecorator('containerName', {
            initialValue: !addSet && peerGet ? peerGet.containerName : '',
          })(<Input disabled={this.state.disabled} />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="couchdb访问IP">
          {getFieldDecorator('couchdbIp', {
            initialValue: !addSet && peerGet ? peerGet.couchdbIp : '',
            rules: [
              {
                pattern: /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/,
                message: '输入的IP不正确！',
              },
            ],
          })(<Input disabled={this.state.disabled} />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="couchdb访问端口">
          {getFieldDecorator('couchdbPort', {
            initialValue: !addSet && peerGet ? peerGet.couchdbPort : '',
            rules: [
              {
                pattern: /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/,
                message: '输入的端口不正确！',
              },
            ],
          })(<Input disabled={this.state.disabled} />)}
        </FormItem>
        <FormItem
          {...formItemLayout}
          label="获取上传Id"
          style={{ display: addSet ? 'block' : 'none' }}
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
          extra="请先获取上传Id并上传zip压缩文件"
          style={{ display: addSet ? 'block' : 'none' }}
        >
          {getFieldDecorator('cert', {
            valuePropName: 'cert',
            getValueFromEvent: this.normFile,
            rules: [{ required: true }],
          })(
            <Upload
              disabled={peerCertId && uploadSwitch ? false : true}
              name="file"
              fileList={fileList}
              action={`${host}/network/peer/cert/upload/${
                peerCertId ? peerCertId.certId : ''
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
        <FormItem
          wrapperCol={{ span: 8, offset: 8 }}
          style={{ display: !addSet ? 'block' : 'none' }}
        >
          <Button type="primary" block htmlType="submit">
            确定
          </Button>
        </FormItem>
      </Form>
    );
  }
}

const PeerSettingForm = Form.create({})(PeerSetting);
export default PeerSettingForm;
