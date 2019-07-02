import React, { Component } from 'react';
import { connect } from 'dva';
import { formatMessage, FormattedMessage } from 'umi/locale';
import Link from 'umi/link';
import router from 'umi/router';
import { Form, Input, Button, Select, Row, Col, Popover, Progress, Radio, Tooltip, Icon } from 'antd';
import styles from './Register.less';

const FormItem = Form.Item;
const { Option } = Select;
const InputGroup = Input.Group;
const RadioGroup = Radio.Group;

const passwordStatusMap = {
  ok: (
    <div className={styles.success}>
      <FormattedMessage id="validation.password.strength.strong" />
    </div>
  ),
  pass: (
    <div className={styles.warning}>
      <FormattedMessage id="validation.password.strength.medium" />
    </div>
  ),
  poor: (
    <div className={styles.error}>
      <FormattedMessage id="validation.password.strength.short" />
    </div>
  ),
};

const passwordProgressMap = {
  ok: 'success',
  pass: 'normal',
  poor: 'exception',
};

@connect(({ register, loading }) => ({
  register,
  submitting: loading.effects['register/submit'],
}))
@Form.create()
class Register extends Component {
  state = {
    count: 0,
    confirmDirty: false,
    visible: false,
    help: '',
    prefix: '86',
    isUserRoot: false,
  };

  constructor(props){
    super(props);
    this.changeIsUserRoot = this.changeIsUserRoot.bind(this);
  }

  componentDidUpdate() {
    const { form, register } = this.props;
    const account = form.getFieldValue('mail');
    if (register.status === 'ok') {
      router.push({
        pathname: '/user/register-result',
        state: {
          account,
        },
      });
    }
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  onGetCaptcha = () => {
    let count = 59;
    this.setState({ count });
    this.interval = setInterval(() => {
      count -= 1;
      this.setState({ count });
      if (count === 0) {
        clearInterval(this.interval);
      }
    }, 1000);
  };

  getPasswordStatus = () => {
    const { form } = this.props;
    const value = form.getFieldValue('password');
    if (value && value.length > 9) {
      return 'ok';
    }
    if (value && value.length > 5) {
      return 'pass';
    }
    return 'poor';
  };

  handleSubmit = e => {
    e.preventDefault();
    const { form, dispatch } = this.props;
    form.validateFields({ force: true }, (err, values) => {
      if (!err) {
        const { prefix } = this.state;
        console.log(values)
        dispatch({
          type: 'register/submit',
          payload: {
            ...values,
            prefix,
          },
        });
      }
    });
  };

  handleConfirmBlur = e => {
    const { value } = e.target;
    const { confirmDirty } = this.state;
    this.setState({ confirmDirty: confirmDirty || !!value });
  };

  checkConfirm = (rule, value, callback) => {
    const { form } = this.props;
    if (value && value !== form.getFieldValue('password')) {
      callback(formatMessage({ id: 'validation.password.twice' }));
    } else {
      callback();
    }
  };

  checkPassword = (rule, value, callback) => {
    const { visible, confirmDirty } = this.state;
    if (!value) {
      this.setState({
        help: formatMessage({ id: 'validation.password.required' }),
        visible: !!value,
      });
      callback('error');
    } else {
      this.setState({
        help: '',
      });
      if (!visible) {
        this.setState({
          visible: !!value,
        });
      }
      if (value.length < 6 || value.length > 16) {
        callback('error');
      } else {
        const { form } = this.props;
        if (value && confirmDirty) {
          form.validateFields(['confirm'], { force: true });
        }
        callback();
      }
    }
  };

  changePrefix = value => {
    this.setState({
      prefix: value,
    });
  };

  renderPasswordProgress = () => {
    const { form } = this.props;
    const value = form.getFieldValue('password');
    const passwordStatus = this.getPasswordStatus();
    return value && value.length ? (
      <div className={styles[`progress-${passwordStatus}`]}>
        <Progress
          status={passwordProgressMap[passwordStatus]}
          className={styles.progress}
          strokeWidth={6}
          percent={value.length * 10 > 100 ? 100 : value.length * 10}
          showInfo={false}
        />
      </div>
    ) : null;
  };

  changeIsUserRoot = (e) => {
    console.log(e);
    const value = e.target.value;
    this.setState({
      isUserRoot: value
    })
  }

  questionIsEquel = (rule, value, callback) => {
    const { form } = this.props;
    const questions = form.getFieldsValue(['question1','question2','question3']);
    let count = 0;
    for (var index in questions) {
      const element = questions[index];
      if(value === element){
        count++;
      }
    }
    if(count > 1){
      callback('密保问题不能相同！')
    }else{
      callback();
    }
  }

  render() {
    const { form, submitting } = this.props;
    const { getFieldDecorator } = form;
    const { count, prefix, help, visible, isUserRoot } = this.state;

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

    const btnLayout = {
      wrapperCol: {
        xs: {
          span: 24,
          offset: 0,
        },
        sm: {
          span: 10,
          offset: 8,
        },
      }
    }
    return (
      <div className={styles.main}>
        <h3>
          <FormattedMessage id="app.register.register" />
        </h3>
        <Form onSubmit={this.handleSubmit}>
          <FormItem {...formItemLayout} label={(
            <span>
              登录ID&nbsp;
              <Tooltip title="长度不超过16，半角英文，不能包含“.”符号">
                <Icon type="question-circle-o" />
              </Tooltip>
            </span>
          )}>
            {getFieldDecorator('userId', {
              rules: [
                {
                  required: true,
                  message: "请输入登录ID",
                },
                {
                  pattern: /^[0-9a-zA-Z_]{1,16}$/g,message:"不合格的名称！"
                },
              ],
            })(
              <Input autoComplete="off" size="large" placeholder="请输入登录ID" />
            )}
          </FormItem>
          <FormItem help={help} {...formItemLayout} label="密码">
            <Popover
              getPopupContainer={node => node.parentNode}
              content={
                <div style={{ padding: '4px 0' }}>
                  {passwordStatusMap[this.getPasswordStatus()]}
                  {this.renderPasswordProgress()}
                  <div style={{ marginTop: 10 }}>
                    <FormattedMessage id="validation.password.strength.msg" />
                  </div>
                </div>
              }
              overlayStyle={{ width: 240 }}
              placement="right"
              visible={visible}
            >
              {getFieldDecorator('password', {
                rules: [
                  {
                    required: true,
                    message: '请输入密码！'
                  },
                  {
                    validator: this.checkPassword,
                  },
                ],
              })(
                <Input
                  size="large"
                  type="password"
                  autoComplete="off"
                  placeholder={formatMessage({ id: 'form.password.placeholder' })}
                />
              )}
            </Popover>
          </FormItem>
          <FormItem  {...formItemLayout} label="确认密码">
            {getFieldDecorator('confirm', {
              rules: [
                {
                  required: true,
                  message: formatMessage({ id: 'validation.confirm-password.required' }),
                },
                {
                  validator: this.checkConfirm,
                },
              ],
            })(
              <Input
                size="large"
                type="password"
                autoComplete="off"
                placeholder={formatMessage({ id: 'form.confirm-password.placeholder' })}
              />
            )}
          </FormItem>
          <FormItem {...formItemLayout} label="密保问题一">
            {getFieldDecorator('question1', {
              rules: [
                {
                  required: true,
                  message: "请输入密保问题！",
                },{
                  validator: this.questionIsEquel
                }
              ],
            })(
              <Input
                size="large"
                placeholder="自定义密保问题一"
                autoComplete="off"
              />
            )}
          </FormItem>
          <FormItem {...formItemLayout} label="答案一">
            {getFieldDecorator('answer1', {
              rules: [
                {
                  required: true,
                  message: "请输入密保问题一的答案！",
                }
              ],
            })(
              <Input
                size="large"
                autoComplete="off"
                placeholder="请输入密保问题一的答案"
              />
            )}
          </FormItem>
          <FormItem {...formItemLayout} label="密保问题二">
            {getFieldDecorator('question2', {
              rules: [
                {
                  required: true,
                  message: "请输入密保问题！",
                },{
                  validator: this.questionIsEquel
                }
              ],
            })(
              <Input
                size="large"
                autoComplete="off"
                placeholder="自定义密保问题二"
              />
            )}
          </FormItem>
          <FormItem {...formItemLayout} label="答案二">
            {getFieldDecorator('answer2', {
              rules: [
                {
                  required: true,
                  message: "请输入密保问题二的答案！",
                }
              ],
            })(
              <Input
                size="large"
                autoComplete="off"
                placeholder="请输入密保问题二的答案"
              />
            )}
          </FormItem>
          <FormItem {...formItemLayout} label="密保问题三">
            {getFieldDecorator('question3', {
              rules: [
                {
                  required: true,
                  message: "请输入密保问题！",
                },{
                  validator: this.questionIsEquel
                }
              ],
            })(
              <Input
                size="large"
                autoComplete="off"
                placeholder="自定义密保问题三"
              />
            )}
          </FormItem>
          <FormItem {...formItemLayout} label="答案三">
            {getFieldDecorator('answer3', {
              rules: [
                {
                  required: true,
                  message: "请输入密保问题三的答案！",
                }
              ],
            })(
              <Input
                size="large"
                autoComplete="off"
                placeholder="请输入密保问题三的答案"
              />
            )}
          </FormItem>
          <FormItem {...formItemLayout} label="是否是根用户">
            {getFieldDecorator('rootUser', {
              rules: [
                {
                  required: true,
                  message: "请选择！",
                }
              ],
            })(
              <RadioGroup onChange={this.changeIsUserRoot}>
                <Radio value={true}>是</Radio>
                <Radio value={false}>否</Radio>
              </RadioGroup>
            )}
          </FormItem>
          <FormItem {...formItemLayout} label="注册码" style={{display: isUserRoot ? 'none': 'block'}}>
            {getFieldDecorator('registerCode', {
              rules: [
                {
                  required: !isUserRoot,
                  message: "请输入注册码！",
                }
              ],
            })(
              <Input
                size="large"
                autoComplete="off"
                placeholder="请输入注册码"
              />
            )}
          </FormItem>
          {/* <FormItem>
            <InputGroup compact>
              <Select
                size="large"
                value={prefix}
                onChange={this.changePrefix}
                style={{ width: '20%' }}
              >
                <Option value="86">+86</Option>
                <Option value="87">+87</Option>
              </Select>
              {getFieldDecorator('mobile', {
                rules: [
                  {
                    required: true,
                    message: formatMessage({ id: 'validation.phone-number.required' }),
                  },
                  {
                    pattern: /^\d{11}$/,
                    message: formatMessage({ id: 'validation.phone-number.wrong-format' }),
                  },
                ],
              })(
                <Input
                  size="large"
                  style={{ width: '80%' }}
                  placeholder={formatMessage({ id: 'form.phone-number.placeholder' })}
                />
              )}
            </InputGroup>
          </FormItem>
          <FormItem>
            <Row gutter={8}>
              <Col span={16}>
                {getFieldDecorator('captcha', {
                  rules: [
                    {
                      required: true,
                      message: formatMessage({ id: 'validation.verification-code.required' }),
                    },
                  ],
                })(
                  <Input
                    size="large"
                    placeholder={formatMessage({ id: 'form.verification-code.placeholder' })}
                  />
                )}
              </Col>
              <Col span={8}>
                <Button
                  size="large"
                  disabled={count}
                  className={styles.getCaptcha}
                  onClick={this.onGetCaptcha}
                >
                  {count
                    ? `${count} s`
                    : formatMessage({ id: 'app.register.get-verification-code' })}
                </Button>
              </Col>
            </Row>
          </FormItem> */}
          <FormItem {...btnLayout}>
            <Button
              size="large"
              loading={submitting}
              className={styles.submit}
              type="primary"
              htmlType="submit"
            >
              <FormattedMessage id="app.register.register" />
            </Button>
            <Link className={styles.login} to="/User/Login">
              <FormattedMessage id="app.register.sign-in" />
            </Link>
          </FormItem>
        </Form>
      </div>
    );
  }
}

export default Register;
