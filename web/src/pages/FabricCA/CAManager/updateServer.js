import React from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'dva';
import {
  Tabs,
  Radio,
  Button,
  Form,
  Input,
  Select,
} from 'antd';
import ServerConfig from '../serverConfig';
import LableToolTip from '../labelToolTip';

const { TabPane } = Tabs;
const Option = Select.Option;
const FormItem = Form.Item;
const RadioGroup = Radio.Group;

@connect(({ CAManager, loading }) => {
  return {
    CAManager,
    loading: loading.effects['CAManager/handleGetConfigCA'],
  };
})
class UpdateServer extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      nowServer: '',
    };
  }

  componentDidMount() {
    const { dispatch, serverName } = this.props;
    if (serverName) {
      dispatch({
        type: 'CAManager/handleGetConfigCA',
        payload: { serverName }
      });
      this.setState({
        nowServer: serverName
      });
    }
  }

  componentDidUpdate() {
    const { dispatch, serverName } = this.props;
    const { nowServer } = this.state;
    if (serverName && serverName !== nowServer) {
      dispatch({
        type: 'CAManager/handleGetConfigCA',
        payload: { serverName }
      });
      this.setState({
        nowServer: serverName
      });
    }
  }

  handleSubmit = (e) => {
    e.preventDefault();
    const { form, dispatch, serverName } = this.props;
    if (serverName) {
      form.validateFields((err, values) => {
        if (!err) {
          dispatch({
            type: 'CAManager/handleSetConfigCA',
            payload: { values, serverName }
          });
        }
      })
    } else {
      message.error('请先选择CA服务！')
    }
  }

  render() {
    const { CAManager, serverName, form } = this.props;
    const { getConfigCA, setConfigCA } = CAManager;
    const { getFieldDecorator } = form;
    const { couildEdit } = this.state;
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
        <FormItem {...formItemLayout} label="是否开启debug">
          {getFieldDecorator('debug', {
            initialValue: getConfigCA.debug,
          })(
            <RadioGroup>
              <Radio value={true}>是</Radio>
              <Radio value={false}>否</Radio>
            </RadioGroup>)}
        </FormItem>
        <FormItem {...formItemLayout} label={<LableToolTip name="CRL有效期" text="时间单位默认为小时，只能输入整数，不能超过65535" />}>
          {getFieldDecorator('crlExpiry', {
            initialValue: getConfigCA.crlExpiry.slice(0, getConfigCA.crlExpiry.length - 1),
            rules: [
              {
                validator: this.validateExpiry
              }
            ],
          })(<Input addonAfter="h" />)}
        </FormItem>
        <FormItem {...formItemLayout} label={<LableToolTip name="证书有效期" text="时间单位默认为小时，只能输入整数，不能超过65535" />}>
          {getFieldDecorator('certExpiry', {
            initialValue: getConfigCA.certExpiry.slice(0, getConfigCA.certExpiry.length - 1),
            rules: [
              {
                validator: this.validateExpiry
              }
            ],
          })(<Input addonAfter="h" />)}
        </FormItem>
        <FormItem wrapperCol={{ span: 8, offset: 8 }}>
          <Button type="primary" block htmlType="submit" loading={setConfigCA.isFetching}>确定</Button>
        </FormItem>
      </Form>
    );
  }
}

const WrapUpdateServer = Form.create({})(UpdateServer);
export default WrapUpdateServer;
