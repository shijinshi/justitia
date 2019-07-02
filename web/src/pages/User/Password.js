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

@connect(({ user, loading }) => ({
    user,
  submitting: loading.effects['user/password'],
}))
@Form.create()
class Password extends Component {
  state = {
    confirmDirty: false,
    visible: false,
    help: '',
    canGetQustions: false, //获取密保问题的按钮可用标志
    noUse: true, //答案输入框不可输入标志
    canFetchQs: true, //可以发送密保问题请求标志
    nowStamp: 0,
  };

  constructor(props){
    super(props);
  }

  componentDidUpdate() {
    const { form, register, user } = this.props;
    const { canFetchQs, nowStamp } = this.state;
    const { questions, password } = user;
    if(questions && canFetchQs){
      this.setState({
        canFetchQs: false,
        noUse: false,
        questions
      })
    }

    if(password && password.stamp !== nowStamp){
      form.resetFields();
      this.setState({
        nowStamp: password.stamp
      })
    }

  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }


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
        console.log(values)
        dispatch({
          type: 'user/password',
          payload: {
            ...values
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
      if (value.length < 6) {
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

  handleChangeUserId = (e) =>{
    const value = e.target.value;
    if(value.length > 0){
      this.setState({
        canGetQustions: true
      })
    }else{
      this.setState({
        canGetQustions: false
      })
    }
  }

  getQuestions = () => {
    const { form, dispatch } = this.props;
    form.validateFields(['userId'],(err,values)=>{
      if(!err){
        dispatch({
          type: 'user/questions',
          payload: {
            ...values
          }
        })
      }
    })
    
  }

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





  render() {
    const { form, submitting, user } = this.props;
    const { getFieldDecorator } = form;
    const { help, visible, canGetQustions, noUse } = this.state;

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
          span: 12,
          offset: 8,
        },
      }
    }
    return (
      <div className={styles.main}>
        <h3>
          修改密码
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
          <Row type="flex" justify="space-between">
            <Col span={14}>
              {getFieldDecorator('userId', {
                rules: [
                  {
                    required: true,
                    message: "请输入登录ID",
                  },
                  {
                    pattern:/^\S+$/g, message: '不能输入空格!'
                  },
                  {
                    validator: this.checkUserId
                  },
                ],
              })(
                <Input onChange={this.handleChangeUserId} size="large" placeholder="请输入登录ID" />
              )}
            </Col>
            <Col span={8}>
              <Button disabled={!canGetQustions} onClick={this.getQuestions} size="large" style={{width: '100%'}}>获取密保问题</Button>
            </Col>
          </Row>
            
          </FormItem>
          <Row>
            <Col xs={{span: 24}} sm={{span: 8}}></Col>
          </Row>
          <FormItem {...formItemLayout} label="密保问题一">
            {getFieldDecorator('question1', {
              initialValue: user.questions ? user.questions.question1 : '未获取'
            })(
              <Input
                size="large"
                className={styles.noInputStyle}
                disabled
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
                placeholder="请输入密保问题一的答案"
                disabled={noUse}
              />
            )}
          </FormItem>
          <FormItem {...formItemLayout} label="密保问题二">
            {getFieldDecorator('question2', {
              initialValue: user.questions ? user.questions.question2 : '未获取'
            })(
              <Input
                size="large"
                className={styles.noInputStyle}
                disabled
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
                placeholder="请输入密保问题二的答案"
                disabled={noUse}
              />
            )}
          </FormItem>
          <FormItem {...formItemLayout} label="密保问题三">
            {getFieldDecorator('question3', {
              initialValue: user.questions ? user.questions.question3 : '未获取'
            })(
              <Input
                size="large"
                className={styles.noInputStyle}
                disabled
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
                placeholder="请输入密保问题三的答案"
                disabled={noUse}
              />
            )}
          </FormItem>
          <FormItem help={help} {...formItemLayout} label="新密码">
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
                  placeholder={formatMessage({ id: 'form.password.placeholder' })}
                  disabled={noUse}
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
                placeholder={formatMessage({ id: 'form.confirm-password.placeholder' })}
                disabled={noUse}
              />
            )}
          </FormItem>
         
          <FormItem {...btnLayout}>
            <Button
              size="large"
              loading={submitting}
              className={styles.submit}
              type="primary"
              htmlType="submit"
              disabled={noUse}
            >
              确认
            </Button>
            <Link className={styles.login} to="/User/Login">
              返回登录
            </Link>
          </FormItem>
        </Form>
      </div>
    );
  }
}

export default Password;
