import React from 'react';
import { connect } from 'dva';
import { Icon, Radio, Select, Button, Form, Input, Tooltip } from 'antd';
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
class DeployPeer extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      couchdb: true,
      nowStamp: 0,
      certStamp: 0,
      isDisable: true,
    };
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'peerModel/getHostHandle'
    });
    dispatch({
      type: 'peerModel/handleGetCAUser'
    });
    dispatch({
      type: 'peerModel/handleGetCA'
    });
  }

  componentDidUpdate() {
    const { network, updateTable, peerModel } = this.props;
    const { certInfo } = peerModel;
    const { peerDeploy } = network;
    const { nowStamp, certStamp } = this.state;

    if (peerDeploy && peerDeploy.stamp !== nowStamp) {
      updateTable();
      this.setState({
        nowStamp: peerDeploy.stamp
      })
    }

    if (certInfo && certInfo.stamp !== certStamp) {
      if (certInfo.length > 0) {
        const type = certInfo[0].type;
        if (type === 'peer') {
          this.setState({
            peerCertArr: certInfo
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

  handleBlurPeerName = (e) => {
    const value = e.target.value;
    this.setState({
      peerName: value
    });
  }

  validateIsJson = (rule, value, callback) => {
    try {
      JSON.parse(value);
      callback()
    } catch (error) {
      if (value) {
        callback('josn数据不合法！')
      } else {
        callback();
      }
    }
  }

  changeCaServerName = (e) => {
    this.setState({
      caServerName: e,
      isDisable: false,
    });
  }

  handleChangeCouchdb = (e) => {
    const { form } = this.props;
    const value = e.target.value;
    this.setState({
      couchdb: value
    }, () => {
      form.resetFields(['couchdbImage', 'couchdbTag', 'couchdbContainerName', 'couchdbExposedPort'])
    });
  }

  changePeerUserId = (e) => {
    const { form, dispatch } = this.props;
    setTimeout(() => {
      form.validateFields(['caServerName', 'peerUserId'], (err, values) => {
        if (!err) {
          dispatch({
            type: 'peerModel/handleGetCertInfo',
            payload: {
              serverName: values.caServerName,
              userId: values.peerUserId
            }
          });
        }
      })
    })
  }

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
          });
        }
      });
    })
  }

  handleSubmit = (e) => {
    e.preventDefault();
    const { dispatch, form, token } = this.props;
    form.validateFields((err, values) => {
      if (!err) {
        values.env = values.env ? JSON.parse(values.env) : null;
        values.exposedPorts = JSON.parse(values.exposedPorts);
        values.volumes = JSON.parse(values.volumes);
        values.extraHosts = values.extraHosts ? values.extraHosts.split(',') : null;

        dispatch({
          type: 'network/peerDeploy',
          payload: values,
        });
      }
    });
  };

  render() {
    const { network, form, peerModel } = this.props;
    const { hostData, CAUser, getCA, certInfo } = peerModel;
    const { loading } = network;
    const { getFieldDecorator } = form;
    const { peerName, couchdb, caServerName, peerCertArr, userCertArr, isDisable } = this.state;
    let peerIdArr = []; // peer类型用户ID数组
    caServerName && CAUser && CAUser.map(ele => {
      if (ele.type === 'peer' && ele.serverName === caServerName && peerIdArr.indexOf(ele.userId) < 0) {
        peerIdArr.push(ele.userId)
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
        <FormItem {...formItemLayout} hasFeedback={true} label="Peer名称">
          {getFieldDecorator('peerName', {
            rules: [
              {
                required: true,
                message: '请输入节点名称！',
              },
              {
                pattern: /^[0-9a-zA-Z_]{1,64}$/g,
                message: '请输入不超过64位的数字、字母、下划线的组合!',
              },
            ],
          })(<Input onBlur={this.handleBlurPeerName} />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="Peer节点服务端口">
          {getFieldDecorator('serverPost', {
            initialValue: '7051',
            rules: [
              {
                required: true,
                message: '请输入Peer节点服务端口！',
              },
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
        <FormItem {...formItemLayout} label="是否使用couchdb">
          {getFieldDecorator('couchdbEnable', {
            initialValue: true,
            rules: [
            ],
          })(<RadioGroup onChange={this.handleChangeCouchdb}>
            <Radio value={true}>是</Radio>
            <Radio value={false}>否</Radio>
          </RadioGroup>)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="couchdb镜像名称">
          {getFieldDecorator('couchdbImage', {
            initialValue: 'hyperledger/fabric-couchdb',
            rules: [
              {
                required: couchdb,
                message: '请输入couchdb镜像名称！',
              },
            ],
          })(<Input disabled={!couchdb} />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="couchdb镜像版本">
          {getFieldDecorator('couchdbTag', {
            initialValue: 'latest',
            rules: [
              {
                required: couchdb,
                message: '请输入couchdb镜像版本！',
              },
            ],
          })(<Input disabled={!couchdb} />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="couchdb容器名称">
          {getFieldDecorator('couchdbContainerName', {
            initialValue: `${peerName}-couchdb`,
            rules: [
              {
                required: couchdb,
                message: '请输入couchdb容器名称！',
              },
            ],
          })(
            <Input disabled={!couchdb} />
          )}
        </FormItem>

        <FormItem
          {...formItemLayout}
          hasFeedback={true}
          label="couchdb对外映射端口"
        >
          {getFieldDecorator('couchdbExposedPort', {
            rules: [
              {
                required: couchdb,
                message: '请输入端口！',
              },
              {
                pattern: /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/,
                message: '输入的端口不正确！',
              },
            ],
          })(<Input disabled={!couchdb} />)}
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
        <FormItem {...formItemLayout} hasFeedback={true} label='peer用户ID' >
          {getFieldDecorator('peerUserId', {
            rules: [
              {
                required: true,
                message: '请选择peer用户ID！',
              },
            ],
          })(<Select
            showSearch
            disabled={isDisable}
            placeholder="请先选择一个CA服务，再选择peer用户ID！"
            optionFilterProp="children"
            onChange={this.changePeerUserId}
            filterOption={(input, option) =>
              option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
            }
          >
            {peerIdArr && peerIdArr.map((item) => {
              return (
                <Option key={item} value={item}>
                  {item}
                </Option>
              );
            })}
          </Select>
          )}
        </FormItem>
        {/*<FormItem {...formItemLayout} hasFeedback={true} label='peer签名证书序列号' >
          {getFieldDecorator('peerCertSerial', {
            rules: [
              {
                required: true,
                message: '请选择peer签名证书序列号！',
              },
            ],
          })(<Select
            showSearch
            placeholder="选择一个peer签名证书序列号"
            optionFilterProp="children"
            filterOption={(input, option) =>
              option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
            }
          >
            {peerCertArr &&
              peerCertArr.map((item) => {
                return (
                  <Option key={item.serialNumber} value={item.serialNumber}>
                    {item.serialNumber}
                  </Option>
                );
              })}
          </Select>)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label='peer签名证书身份标识' >
          {getFieldDecorator('peerCertAki', {
            rules: [
              {
                required: true,
                message: '请选择peer签名证书身份标识！',
              },
            ],
          })(<Select
            showSearch
            placeholder="选择一个peer签名证书身份标识"
            optionFilterProp="children"
            filterOption={(input, option) =>
              option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
            }
          >
            {peerAki &&
              peerAki.map((item) => {
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
        <FormItem {...formItemLayout} hasFeedback={true} label='peer用户证书序列号' >
          {getFieldDecorator('userCertSerial', {
            rules: [
              {
                required: true,
                message: '请选择peer用户证书序列号！',
              },
            ],
          })(<Select
            showSearch
            placeholder="选择一个peer用户证书序列号"
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
        <FormItem {...formItemLayout} hasFeedback={true} label='peer用户证书身份标识' >
          {getFieldDecorator('userCertAki', {
            rules: [
              {
                required: true,
                message: '请选择peer用户证书身份标识！',
              },
            ],
          })(<Select
            showSearch
            placeholder="选择一个peer用户证书身份标识"
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
        </FormItem>*/}

        {/* 容器配置 */}
        <FormItem {...formItemLayout} style={{ marginBottom: 10 }} className={styles.createCALabel} label="Peer容器配置"></FormItem>

        <FormItem {...formItemLayout} label="镜像名称">
          {getFieldDecorator('image', {
            initialValue: 'hyperledger/fabric-peer',
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
            rules: [
              {
                validator: this.validateIsJson
              }
            ],
          })(<TextArea placeholder="输入json数据" autosize={{ minRows: 2, maxRows: 6 }} />)}
        </FormItem>
        <FormItem {...formItemLayout} label="容器启动命令">
          {getFieldDecorator('cmd', {
            initialValue: 'peer node start',
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
          })(<TextArea placeholder="输入json数据" autosize={{ minRows: 2, maxRows: 6 }} />)}
        </FormItem>
        <FormItem {...formItemLayout} label={<LableToolTip name="主机名映射" text="格式为：容器名:IP地址。多个映射端口用英文','隔开" />}>
          {getFieldDecorator('extraHosts')(
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
            initialValue: '{"/var/run":"/host/var/run/"}',
            rules: [
              {
                required: true,
                message: '请输入对外映射端口！',
              },
              {
                validator: this.validateIsJson
              }
            ],
          })(<TextArea placeholder="输入json数据" autosize={{ minRows: 2, maxRows: 6 }} />)}
        </FormItem>
        <FormItem wrapperCol={{ span: 8, offset: 8 }}>
          <Button type="primary" block htmlType="submit" loading={loading}>部署</Button>
        </FormItem>
      </Form>
    );
  }
}

const WrapDeployPeer = Form.create()(DeployPeer);
export default WrapDeployPeer;
