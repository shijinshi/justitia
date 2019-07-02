import React from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'dva';
import { Radio, Button, Form, Input } from 'antd';
import { clearSpacing } from '@/utils/utils';

const FormItem = Form.Item;
const RadioGroup = Radio.Group;
const host = process.env.NODE_ENV === "production" ? '/api' : `http://${window.hostIp}`;

@connect(({ network }) => {
  return {
    network,
  };
})
class GenerateCert extends React.Component {
  constructor(props) {
    super(props);
    this.downloadFile = React.createRef();
    this.handleSubmitGen = this.handleSubmitGen.bind(this);
    this.checkName = this.checkName.bind(this);
  }

  handleSubmitGen = e => {
    e.preventDefault();
    const { dispatch, form, type, downloadFileFunc, token } = this.props;

    form.validateFields((err, values) => {
      if (!err) {
        const valuesNoSpacing = clearSpacing(values);
        values.name = valuesNoSpacing.name_;
        values.token = token;
        const { download } = valuesNoSpacing;
        if (type === 'orderer') {
          downloadFileFunc(values, download, host, '/network/orderer/cert/generate');
          // dispatch({
          //     type: 'network/generateOrdererCert',
          //     payload: values
          // })
        } else {
          downloadFileFunc(values, download, host, '/network/peer/cert/generate');
          // dispatch({
          //     type: 'network/generatePeerCert',
          //     payload: values
          // })
        }
      }
    });
  };

  checkName = (rule, value, callback) => {
    const { data, type } = this.props;
    let flag = false;
    if (data && data.length > 0) {
      for (var i = 0, len = data.length; i < len; i++) {
        if (type === 'peer') {
          if (data[i].peerName === value) {
            flag = true;
            break;
          }
        } else {
          if (data[i].ordererName === value) {
            flag = true;
            break;
          }
        }
      }
    }

    if (flag) {
      callback('节点已存在！');
      flag = false;
    } else {
      callback();
    }
  };

  render() {
    const { network, form, type } = this.props;
    const { generateOrdererCert } = network;
    const { getFieldDecorator } = form;

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
      <Form onSubmit={this.handleSubmitGen}>
        <FormItem
          {...formItemLayout}
          hasFeedback={true}
          label={type && type === 'peer' ? 'peer节点名称' : 'orderer节点名称'}
        >
          {getFieldDecorator('name_', {
            rules: [
              {
                required: true,
                message: '请输入节点名称！',
              },
              {
                validator: this.checkName,
              },
              {
                pattern: /^[0-9a-zA-Z]+$/g, message: '请输入数字或者字母组成的名称'
              }
            ],
          })(<Input />)}
        </FormItem>
        <FormItem {...formItemLayout} label="是否下载证书">
          {getFieldDecorator('download', {
            initialValue: true,
          })(
            <RadioGroup>
              <Radio value={true}>是</Radio>
              <Radio value={false}>否</Radio>
            </RadioGroup>
          )}
        </FormItem>
        <FormItem wrapperCol={{ span: 8, offset: 8 }}>
          <Button type="primary" block htmlType="submit">
            确定
          </Button>
        </FormItem>
        <div ref={this.downloadFile} style={{ display: 'none' }} />
      </Form>
    );
  }
}

const WrapGenerateCert = Form.create({})(GenerateCert);
export default WrapGenerateCert;
