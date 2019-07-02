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
  Select,
  Menu,
  Dropdown,
  Button,
  Form,
  message,
  Steps,
  Input,
  Upload
} from 'antd';

import styles from './index.less';


const { TabPane } = Tabs;
const Option = Select.Option;
const FormItem = Form.Item;
const RadioGroup = Radio.Group;
const Step = Steps.Step;

@connect(({ initConfig }) => {
  return {
    initConfig
  };
})
class Organization extends React.Component {
  state = {
    isPeer: false,
    isTls: false,
    orgFileList: '',
  }
  // 选择组织类型
  changeOrgType = (e) => {
    this.setState({
      isPeer: !this.state.isPeer
    });
  }
  // 选择是否使用TLS
  changeTLSEnable = () => {
    this.setState({
      isTls: this.state.isTls ? false : true
    });
  }
  // 文件上传
  uploadChange = (info) => {
    let fileList = info.fileList;
    fileList = fileList.slice(-1);
    this.setState({
      orgFileList: fileList
    });
  }
  // 上传文件前的钩子函数
  // 返回false为手动上传文件
  beforeUpload = () => {
    return false;
  }

  handleSubmit = (e) => {
    e.preventDefault();
    const { form, dispatch } = this.props;
    form.validateFields((err, values) => {
      if (!err) {
        console.log(values)
        let formData = new FormData();
        Object.keys(values).forEach(key => {
          formData.append(key, values[key].file ? values[key].file : values[key]);
        });
        dispatch({
          type: 'initConfig/handleSetOrganiztion',
          payload: formData
        });
      }
    })

  }

  render() {
    const { network, loading, initConfig } = this.props;
    const { getFieldDecorator } = this.props.form;
    const { isLoading, data } = initConfig.setORganiztion;
    const { isPeer, isTls, orgFileList } = this.state;

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
      <Form encType="multipart/form-data" onSubmit={this.handleSubmit}>
        <FormItem {...formItemLayout} hasFeedback={true} label="组织名称">
          {getFieldDecorator('orgName', {
            initialValue: data.orgName,
            rules: [{
              required: true,
              message: '请输入组织名称！'
            }, {
              pattern: /^[0-9a-zA-Z_]{1,64}$/g,
              message: '请输入不超过64位的数字、字母、下划线的组合!',
            }]
          })(<Input placeholder="请输入不超过64位的数字、字母、下划线的组合" />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="组织MSPID">
          {getFieldDecorator('orgMspId', {
            initialValue: data.orgMspId,
            rules: [{
              required: true,
              message: '请输入组织MSPID！'
            }]
          })(<Input />)}
        </FormItem>
        <FormItem {...formItemLayout} label="组织类型">
          {getFieldDecorator('orgType', {
            initialValue: data.orgType,
            rules: []
          })(<RadioGroup onChange={this.changeOrgType}>
            <Radio value="ordererOrg">ordererOrg</Radio>
            <Radio value="peerOrg">peerOrg</Radio>
          </RadioGroup>)}
        </FormItem>
        <FormItem {...formItemLayout} label="是否使用tls">
          {getFieldDecorator('tlsEnable', {
            initialValue: false,
            rules: []
          })(<RadioGroup onChange={this.changeTLSEnable}>
            <Radio value={true}>是</Radio>
            <Radio value={false}>否</Radio>
          </RadioGroup>)}
        </FormItem>
        {
          isPeer ?
            <React.Fragment>
              <FormItem {...formItemLayout} hasFeedback={true} label="orderer节点IP">
                {getFieldDecorator('ordererIp', {
                  initialValue: data.ordererIp,
                  rules: [{
                    required: true,
                    message: '请输入orderer节点IP！'
                  }]
                })(<Input />)}
              </FormItem>
              <FormItem {...formItemLayout} hasFeedback={true} label="orderer节点服务端口">
                {getFieldDecorator('ordererPort', {
                  initialValue: data.ordererPort,
                  rules: [{
                    required: true,
                    message: '请输入orderer节点服务端口！'
                  }]
                })(<Input />)}
              </FormItem>
              {isTls ?
                <FormItem {...formItemLayout} label={"orderer节点TLS证书"}>
                  {getFieldDecorator('ordererTlsCert', {
                    rules: [
                      {
                        required: true,
                        message: '证书文件不能为空！',
                      }
                    ],
                  })(
                    <Upload
                      name="cert"
                      fileList={orgFileList}
                      onChange={this.uploadChange}
                      beforeUpload={this.beforeUpload}
                    >
                      <Button>
                        <Icon type="upload" /> 上传TLS证书
                    </Button>
                    </Upload>
                  )}
                </FormItem> : null
              }
            </React.Fragment> :
            null
        }
        <FormItem wrapperCol={{ span: 8, offset: 8 }}>
          <Button type="primary" block htmlType="submit" loading={isLoading}>确定</Button>
        </FormItem>
      </Form>
    );
  }
}

const WrapOrganization = Form.create({})(Organization);
export default WrapOrganization;
