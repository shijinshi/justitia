import React from 'react';
import { connect } from 'dva';
import { Icon, Radio, Select, Button, Form, Input, Tooltip, Upload, Row, Col } from 'antd';
import { clearSpacing } from '@/utils/utils';
import LableToolTip from '../../FabricCA/labelToolTip';
import styles from '../index.less';

const { TextArea } = Input;
const Option = Select.Option;
const FormItem = Form.Item;
const RadioGroup = Radio.Group;

@connect(({ network, peerModel }) => {
  return {
    network,
    peerModel
  };
})
class OrdererDeploy extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      nowStamp: 0,
      blockFileList: [],
      createId: '',
      createStamp: 0,
      certStamp: 0,
      isDisabled: true,
    };
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'peerModel/getHostHandle'
    })
    dispatch({
      type: 'peerModel/handleGetCAUser'
    })
    dispatch({
      type: 'peerModel/handleGetCA'
    })
  }

  componentDidUpdate() {
    const { network, updateTable, form, peerModel } = this.props;
    const { certInfo } = peerModel;
    const { ordererDeploy, createOrderer } = network;
    const { nowStamp, createStamp, certStamp } = this.state;

    if (ordererDeploy && ordererDeploy.stamp !== nowStamp) {
      if (ordererDeploy.createId) {
        this.setState({
          createId: ordererDeploy.createId
        })
      }
      this.setState({
        nowStamp: ordererDeploy.stamp,
      })
    }

    if (createOrderer && createOrderer.stamp !== createStamp) {
      if (createOrderer.success) {
        updateTable();
        form.resetFields();
      }
      this.setState({
        createStamp: createOrderer.stamp
      })
    }

    if (certInfo && certInfo.stamp !== certStamp) {
      if (certInfo.length > 0) {
        const type = certInfo[0].type;
        if (type === 'orderer') {
          this.setState({
            ordererCertArr: certInfo
          })
        } else {
          this.setState({
            userCertArr: certInfo
          })
        }
      }
      this.setState({
        certStamp: certInfo.stamp
      })
    }
  }

  validateIsJson = (rule, value, callback) => {
    try {
      JSON.parse(value);
      callback()
    } catch (error) {
      callback('josn数据不合法！')
    }
  }

  changeCaServerName = (e) => {
    this.setState({
      caServerName: e,
      isDisabled: false,
    })
  }

  // changePeerUserId = (e) => {
  //   const { form, dispatch } = this.props;
  //   setTimeout(() => {
  //     form.validateFields(['caServerName', 'ordererUserId'], (err, values) => {
  //       if (!err) {
  //         dispatch({
  //           type: 'peerModel/handleGetCertInfo',
  //           payload: {
  //             serverName: values.caServerName,
  //             userId: values.ordererUserId
  //           }
  //         })
  //       }
  //     })
  //   })
  // }

  changeUserId = (e) => {
    const { form, dispatch } = this.props;
    setTimeout(() => {
      form.validateFields(['caServerName', 'userId'], (err, values) => {
        if (!err) {
          dispatch({
            type: 'peerModel/handleGetCertInfo',
            payload: {
              serverName: values.caServerName,
              userId: values.userId
            }
          })
        }
      })
    })
  }

  blockChange = (info) => {
    let fileList = info.fileList;
    fileList = fileList.slice(-1);
    this.setState({
      blockFileList: fileList
    });
  }

  beforeUpload = (file) => {
    return false;
  }

  handleSubmit = (e) => {
    e.preventDefault();
    const { dispatch, form } = this.props;
    form.validateFields((err, values) => {
      if (!err) {
        values.env = JSON.parse(values.env);
        values.exposedPorts = JSON.parse(values.exposedPorts);
        values.volumes = JSON.parse(values.volumes);
        values.extraHosts = typeof values.extraHosts !== 'string' || values.extraHosts === null ? null : values.extraHosts.split(',');
        values.genesisBlock = null;
        dispatch({
          type: 'network/handleOrdererDeploy',
          payload: values,
        });
      }
    });
  };

  render() {
    const { network, form, peerModel } = this.props;
    const { hostData, CAUser, getCA, certInfo } = peerModel;
    const { getFieldDecorator } = form;
    const { loading } = network;
    const { caServerName, blockFileList, createId, isDisabled } = this.state;

    let ordererIdArr = []; //peer类型用户ID数组
    caServerName && CAUser && CAUser.map(ele => {
      if (ele.type === 'orderer' && ele.serverName === caServerName && ordererIdArr.indexOf(ele.userId) < 0) {
        ordererIdArr.push(ele.userId)
      }
    });

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
      <Form onSubmit={this.handleSubmit}>
        {/* 节点配置 */}
        <FormItem {...formItemLayout} style={{ marginBottom: 10 }} className={styles.createCALabel} label="节点配置"></FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="Orderer名称">
          {getFieldDecorator('ordererName', {
            rules: [
              {
                required: true,
                message: '请输入节点名称！',
              },
              {
                pattern: /^[0-9a-zA-Z_]{1,64}$/g,
                message: '请输入不超过16位的数字、字母、下划线的组合!',
              },
            ],
          })(<Input />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="Orderer节点服务端口号">
          {getFieldDecorator('serverPort', {
            initialValue: 7050,
            rules: [
              {
                required: true,
                message: '请输入节点服务端口号！',
              }
            ],
          })(<Input disabled />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="节点所属主机">
          {getFieldDecorator('hostName', {
            rules: [
              {
                required: true,
                message: '请选择一个主机！',
              }
            ],
          })(<Select
            showSearch
            placeholder="选择一个主机"
            optionFilterProp="children"
            filterOption={(input, option) =>
              option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
            }
          >
            {hostData &&
              hostData.map((item) => {
                return (
                  <Option key={item.hostName} value={item.hostName}>
                    {item.hostName}
                  </Option>
                );
              })}
          </Select>)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label='CA服务名称' >
          {getFieldDecorator('caServerName', {
            rules: [
              {
                required: true,
                message: '请选择CA服务！',
              },
            ],
          })(<Select
            showSearch
            placeholder="选择一个CA服务"
            optionFilterProp="children"
            onChange={this.changeCaServerName}
            filterOption={(input, option) =>
              option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
            }
          >
            {getCA &&
              getCA.map((item) => {
                return (
                  <Option key={item.serverName} value={item.serverName}>
                    {item.serverName}
                  </Option>
                );
              })}
          </Select>)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label='Orderer用户ID' >
          {getFieldDecorator('ordererUserId', {
            rules: [
              {
                required: true,
                message: '请选择Orderer用户ID！',
              },
            ],
          })(
            <Select
              showSearch
              disabled={isDisabled}
              placeholder="请先选择一个CA服务，然后选择Orderer用户ID"
              optionFilterProp="children"
              filterOption={(input, option) =>
                option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
              }
            >
              {ordererIdArr &&
                ordererIdArr.map((item) => {
                  return (
                    <Option key={item} value={item}>
                      {item}
                    </Option>
                  );
                })}
            </Select>
          )}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="系统通道名称">
          {getFieldDecorator('systemChainId', {
            initialValue: 'testchainid',
            rules: [
              {
                required: true,
                message: '请输入系统通道名称！',
              },
              {
                pattern: /^[0-9a-z_]{1,64}$/g,
                message: '请输入不超过64位的数字、小写字母、下划线的组合!',
              },
            ],
          })(<Input placeholder="请输入系统通道名称！" />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="联盟名称">
          {getFieldDecorator('consortiumName', {
            initialValue: 'SampleConsortium',
            rules: [
              {
                required: true,
                message: '请输入联盟名称！',
              },
            ],
          })(<Input placeholder="请输入联盟名称！" />)}
        </FormItem>
        {/* <FormItem {...formItemLayout} hasFeedback={true} label='Orderer签名证书序列号' >
          {getFieldDecorator('ordererCertSerial', {
            rules: [
              {
                required: true,
                message: '请选择Orderer签名证书序列号！',
              },
            ],
          })(<Select
            showSearch
            placeholder="选择一个Orderer签名证书序列号"
            optionFilterProp="children"
            filterOption={(input, option) =>
              option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
            }
          >
            {ordererCertArr &&
              ordererCertArr.map((item) => {
                return (
                  <Option key={item.serialNumber} value={item.serialNumber}>
                    {item.serialNumber}
                  </Option>
                );
              })}
          </Select>)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label='Orderer签名证书身份标识' >
          {getFieldDecorator('ordererCertAki', {
            rules: [
              {
                required: true,
                message: '请选择Orderer签名证书身份标识！',
              },
            ],
          })(<Select
            showSearch
            placeholder="选择一个Orderer签名证书身份标识"
            optionFilterProp="children"
            filterOption={(input, option) =>
              option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
            }
          >
            {ordererAki &&
              ordererAki.map((item) => {
                return (
                  <Option key={item} value={item}>
                    {item}
                  </Option>
                );
              })}
          </Select>)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label='User用户ID' >
          {getFieldDecorator('userId', {
            rules: [
              {
                required: true,
                message: '请选择User用户ID！',
              },
            ],
          })(<Select
            showSearch
            placeholder="选择一个User用户ID"
            optionFilterProp="children"
            onChange={this.changeUserId}
            filterOption={(input, option) =>
              option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
            }
          >
            {userIdArr &&
              userIdArr.map((item) => {
                return (
                  <Option key={item} value={item}>
                    {item}
                  </Option>
                );
              })}
          </Select>)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label='Orderer用户证书序列号' >
          {getFieldDecorator('userCertSerial', {
            rules: [
              {
                required: true,
                message: '请选择Orderer用户证书序列号！',
              },
            ],
          })(<Select
            showSearch
            placeholder="选择一个Orderer用户证书序列号"
            optionFilterProp="children"
            filterOption={(input, option) =>
              option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
            }
          >
            {userCertArr &&
              userCertArr.map((item) => {
                return (
                  <Option key={item.serialNumber} value={item.serialNumber}>
                    {item.serialNumber}
                  </Option>
                );
              })}
          </Select>)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label='Orderer用户证书身份标识' >
          {getFieldDecorator('userCertAki', {
            rules: [
              {
                required: true,
                message: '请选择Orderer用户证书身份标识！',
              },
            ],
          })(<Select
            showSearch
            placeholder="选择一个Orderer用户证书身份标识"
            optionFilterProp="children"
            filterOption={(input, option) =>
              option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
            }
          >
            {userAki &&
              userAki.map((item) => {
                return (
                  <Option key={item} value={item}>
                    {item}
                  </Option>
                );
              })}
          </Select>)}
        </FormItem> */}

        {/* 容器配置 */}
        <FormItem {...formItemLayout} style={{ marginBottom: 10 }} className={styles.createCALabel} label="Orderer容器配置"></FormItem>
        <FormItem {...formItemLayout} label="镜像名称">
          {getFieldDecorator('image', {
            initialValue: 'hyperledger/fabric-orderer',
            rules: [
              {
                required: true,
                message: '请输入镜像名称！'
              }
            ]
          })(
            <Input />
          )}
        </FormItem>
        <FormItem {...formItemLayout} label="镜像版本">
          {getFieldDecorator('tag', {
            initialValue: 'latest',
            rules: [
              {
                required: true,
                message: '请输入镜像版本！'
              }
            ]
          })(
            <Input />
          )}
        </FormItem>
        <FormItem {...formItemLayout} label="容器中工作路径">
          {getFieldDecorator('workingDir', {
            initialValue: '/opt/gopath/src/github.com/hyperledger/fabric',
            rules: [
              {
                required: true,
                message: '请输入容器中工作路径！'
              }
            ]
          })(
            <Input />
          )}
        </FormItem>
        <FormItem {...formItemLayout} label="容器名称">
          {getFieldDecorator('containerName', {
            rules: [
              {
                required: true,
                message: '请输入容器名称！'
              }
            ]
          })(
            <Input />
          )}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label={<LableToolTip name="容器中环境变量" text="输入json数据，ORDERER_GENERAL_LOCALMSPID（组织MSPID）必须用户输入" />}>
          {getFieldDecorator('env', {
            initialValue: '{"FABRIC_CA_HOME":"/ect/hyperledger/fabric-ca-server"}',
            rules: [
              {
                required: true,
                message: '请输入环境变量！',
              },
              {
                validator: this.validateIsJson
              }
            ],
          })(<TextArea placeholder="输入json数据" autosize={{ minRows: 2, maxRows: 6 }} />)}
        </FormItem>
        <FormItem {...formItemLayout} label="容器启动命令">
          {getFieldDecorator('cmd', {
            initialValue: 'orderer',
            rules: [
              {
                required: true,
                message: '请输入容器启动命令！'
              }
            ]
          })(
            <Input />
          )}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label={<LableToolTip name="容器对外映射端口" text="输入json数据，key为容器内部端口号，Value为映射出来的端口号。" />}>
          {getFieldDecorator('exposedPorts', {
            rules: [
              {
                required: true,
                message: '请输入对外映射端口！',
              },
              {
                validator: this.validateIsJson
              }
            ],
          })(<TextArea placeholder="形如port:port,port:prot的参数" autosize={{ minRows: 2, maxRows: 6 }} />)}
        </FormItem>
        <FormItem {...formItemLayout} label={<LableToolTip name="主机名映射" text="格式为：容器名:IP地址。多个映射端口用英文','隔开" />}>
          {getFieldDecorator('extraHosts', {
            initialValue: null
          })(
            <Input />
          )}
        </FormItem>
        <FormItem {...formItemLayout} label="容器所属的docker网络">
          {getFieldDecorator('networkMode', {
            rules: [
              {
                required: true,
                message: '请输入docker网络！'
              }
            ]
          })(
            <Input />
          )}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label={<LableToolTip name="本地目录或数据卷" text="输入json数据，key为本地路径或数据卷名，value为容器内部路径。如果输入了不存在的数据卷则会自动创建。" />}>
          {getFieldDecorator('volumes', {
            initialValue: null,
            rules: [
              {
                validator: this.validateIsJson
              }
            ],
          })(<TextArea placeholder="输入json数据" autosize={{ minRows: 2, maxRows: 6 }} />)}
        </FormItem>
        {/* <FormItem
          {...formItemLayout}
          label="获取上传ID"
        >
          <Row gutter={8}>
            <Col span={12}>
              {getFieldDecorator('createId', {
                initialValue: createId,
              })(
                <Input readOnly onChange={this.changeCreateId} />
              )}
            </Col>
            <Col span={12}>
              <Button onClick={this.handleGetCreateId} type='primary'>获取</Button>
            </Col>
          </Row>
        </FormItem>
        <FormItem {...formItemLayout} label="上传区块">
          {getFieldDecorator('genesisBlock', {
            rules: [{
              required: createId === '' ? false : true,
              message: '请上传区块！'
            }]
          })(
            <Upload
              name="block"
              fileList={blockFileList}
              onChange={this.blockChange}
              beforeUpload={this.beforeUpload}
              disabled={!createId}
            >
              <Button>
                <Icon type="upload" /> 上传区块
                </Button>
            </Upload>
          )}
        </FormItem> */}
        <FormItem wrapperCol={{ span: 8, offset: 8 }}>
          <Button type="primary" block htmlType="submit" loading={loading}>确定</Button>
        </FormItem>
      </Form>
    );
  }
}

const WrapOrdererDeploy = Form.create()(OrdererDeploy);
export default WrapOrdererDeploy;
