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
  Select,
  message,
  InputNumber
} from 'antd';
import LableToolTip from '../labelToolTip';


const FormItem = Form.Item;
const Option = Select.Option;
const { TextArea } = Input;

@connect(({ CAUserManager, initConfig, global }) => {
  return {
    CAUserManager,
    initConfig,
    global
  };
})
class RegisterUser extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      nowStamp: 0,
      isDisable: true,
      userDataArr: [],
    };
  }

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'CAUserManager/handleGetChildrenUser'
    });
  }

  componentDidUpdate(prevProps) {
    const { form, CAUserManager } = this.props;
    const { isFetching } = CAUserManager.registerCAUser;
    const oldFetching = prevProps.CAUserManager.registerCAUser.isFetching;
    if (oldFetching && !isFetching) {
      form.setFieldsValue({
        secret: '',
        confirm: ''
      });
    }
  }

  confirmPassword = (rule, value, callback) => {
    const { form } = this.props;
    if (value && form.getFieldValue('secret') !== value) {
      callback('两次密码输入不一致！');
    } else {
      callback();
    }
  }

  handleSubmit = (e) => {
    e.preventDefault();
    const { form, dispatch } = this.props;
    form.validateFields((err, values) => {
      if (!err) {
        if (values.attributes) {
          values.attributes = JSON.parse(values.attributes);
        }
        dispatch({
          type: 'CAUserManager/handleRegisterCAUser',
          payload: {
            registerInfo: {
              identityId: values.identityId,
              owner: values.owner,
              secret: values.secret,
              userType: values.userType,
              identityType: values.identityType,
              maxEnrollments: values.maxEnrollments,
              attributes: values.attributes || {},
              affiliation: values.affiliation || ""
            },
            serverName: values.serverName,
            caUserId: values.caUserId
          }
        });
      }
    })
  }

  validateIsJson = (rule, value, callback) => {
    try {
      JSON.parse(value);
      callback();
    } catch (error) {
      if (value) {
        callback('josn数据不合法！');
      } else {
        callback();
      }
    }
  }
  // 选择CA服务
  handleSelectServer = (value) => {
    const { getCAUser } = this.props.CAUserManager;
    let userDataArr = [];
    getCAUser && getCAUser.map((item) => {
      if (userDataArr.indexOf(item.userId) < 0 && item.type === 'client' && item.serverName === value) {
        userDataArr.push(item.userId)
      }
    });
    this.setState({
      isDisable: false,
      userDataArr
    });
  }

  render() {
    const { userDataArr, isDisable } = this.state;
    const { form, getCA, CAUserManager, global } = this.props;
    const { childrenUser, getCAUser, registerCAUser } = CAUserManager;
    const { getOrganiztion } = global
    const { getFieldDecorator } = form;
    const { isFetching } = registerCAUser;
    const orgType = getOrganiztion.orgType;

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
        <FormItem {...formItemLayout} hasFeedback={true} label="选择CA服务">
          {getFieldDecorator('serverName', {
            rules: [
              {
                required: true,
                message: '请选择CA服务！',
              }
            ],
          })(<Select placeholder="请选择CA服务" onSelect={this.handleSelectServer}>
            {
              getCA && getCA.length > 0 ? getCA.map((item) => {
                return (<Option value={item.serverName} key={item.serverName}>{item.serverName}</Option>)
              }) : ''
            }
          </Select>)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="注册发起用户">
          {getFieldDecorator('caUserId', {
            rules: [
              {
                required: true,
                message: '请选择注册发起用户！',
              }
            ],
          })(<Select placeholder="请选择注册发起用户" disabled={isDisable}>
            {
              userDataArr && userDataArr.length && userDataArr.map(ele => {
                return <Option key={ele} value={ele}>{ele}</Option>
              })
            }
          </Select>)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="用户ID">
          {getFieldDecorator('identityId', {
            rules: [
              {
                required: true,
                message: '请输入用户ID！',
              }
            ],
          })(<Input autoComplete="off" />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="ca用户所有者">
          {getFieldDecorator('owner', {
            initialValue: localStorage.getItem('userId'),
          })(<Select>
            {childrenUser && childrenUser.map(item => {
              return <Option key={item.userId} value={item.userId}>{item.userId}</Option>
            })}
          </Select>)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="密码">
          {getFieldDecorator('secret', {
            rules: [
              {
                required: true,
                message: '请输入密码！',
              }
            ],
          })(<Input type="password" />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="确认密码">
          {getFieldDecorator('confirm', {
            rules: [
              {
                required: true,
                message: '请输入密码！',
              }, {
                validator: this.confirmPassword
              }
            ],
          })(<Input type="password" />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="所属关系">
          {getFieldDecorator('affiliation', {
          })(<Input />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="用户类型">
          {getFieldDecorator('userType', {
            initialValue: 'user',
          })(<Select>
            <Option key="user" value="user">user</Option>
            {orgType === "ordererOrg" ?
              <Option key="orderer" value="orderer">orderer</Option> :
              <Option key="peer" value="peer">peer</Option>
            }
            <Option key="client" value="client">client</Option>
          </Select>)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="身份类型">
          {getFieldDecorator('identityType', {
            initialValue: 'member',
          })(<Select>
            <Option key="peer" value="admin">admin</Option>
            <Option key="orderer" value="member">member</Option>
          </Select>)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label={<LableToolTip name="最大登记次数" text="只能输入-1或者正整数，-1表示无限制" />}>
          {getFieldDecorator('maxEnrollments', {
            rules: [
              {
                required: true,
                message: '请输入最大登记次数！',
              },{
                pattern: /^(-1|[1-9]+[0-9]*)$/g,
                message: '只能输入正整数和-1'
              }
            ],
          })(<InputNumber style={{width: '100%'}} min={-1} />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label={<LableToolTip name="用户属性" text="输入json数据。需要包含到证书中的属性，用户自定义" />}>
          {getFieldDecorator('attributes', {
            rules: [{
              validator: this.validateIsJson,
            }],
          })(<TextArea placeholder="输入json数据" autosize={{ minRows: 2, maxRows: 6 }} />)}
        </FormItem>
        <FormItem wrapperCol={{ span: 8, offset: 8 }}>
          <Button type="primary" block htmlType="submit" loading={isFetching}>确定</Button>
        </FormItem>
      </Form>
    )
  }
}
const WrapRegisterUser = Form.create({})(RegisterUser);
export default WrapRegisterUser;