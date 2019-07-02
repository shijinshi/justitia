import React, { Component } from 'react';
import { Icon, Select, Form, Input, Button, Upload } from 'antd';
import { connect } from 'dva';

const FormItem = Form.Item;
const Option = Select.Option;
const TextArea = Input.TextArea;

@connect(({ consortiumModel, network }) => {
  return {
    consortiumModel,
    network
  };
})
class AddConsortium extends Component {
  constructor(props) {
    super(props);
    this.state = {
      orgFileList: [],
    }
  }
  // 选择orderer节点，获取联盟成员
  handleSelect = (e) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'consortiumModel/handleGetConsortium',
      payload: {
        ordererName: e
      }
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
  // 提交表单
  handleSubmit = (e) => {
    e.preventDefault();
    const { form, dispatch } = this.props;
    form.validateFields((err, values) => {
      if (err) return false;
      let formData = new FormData();
      Object.keys(values).forEach(key => {
        formData.append(key, values[key].file ? values[key].file : values[key]);
      });
      dispatch({
        type: 'consortiumModel/handelAddConsortium',
        payload: formData
      });
    });
  }

  render() {
    // Form表单layout
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
    const { orgFileList } = this.state;
    const { form, network, consortiumModel } = this.props;
    const { ordererConfig } = network;
    const { consortiumList, isFetching, isLoading } = consortiumModel;
    const { getFieldDecorator } = form;

    return (
      <Form onSubmit={this.handleSubmit}>
        <FormItem {...formItemLayout} hasFeedback={true} label={"所属orderer节点"}>
          {getFieldDecorator('ordererName', {
            rules: [
              {
                required: true,
                message: 'order不能为空！',
              }
            ],
          })(
            <Select onSelect={this.handleSelect} placeholder="请选择想要添加联盟的order节点">
              {ordererConfig.map(ele => (
                <Option key={ele.ordererName} value={ele.ordererName}>{ele.ordererName}</Option>
              ))}
            </Select>
          )}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label={"联盟名称"}>
          {getFieldDecorator('consortiumName', {
            rules: [
              {
                required: true,
                message: '联盟名称不能为空！',
              }
            ],
          })(
            <Select loading={isLoading} placeholder="请选择想要加入的成员">
              {Object.keys(consortiumList).map(ele => (
                <Option key={ele} value={ele}>{ele}</Option>
              ))}
            </Select>
          )}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label={"组织名称"}>
          {getFieldDecorator('orgName', {
            rules: [
              {
                required: true,
                message: '请输入被增加组织的名称！',
              }
            ],
          })(
            <Input placeholder="被增加组织的名称！" /> 
          )}
        </FormItem>
        <FormItem {...formItemLayout} label={"组织配置文件"}>
          {getFieldDecorator('orgConfig', {
            rules: [
              {
                required: true,
                message: '配置文件不能为空！',
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
                <Icon type="upload" /> 上传组织配置文件
              </Button>
            </Upload>
          )}
        </FormItem>
        <FormItem wrapperCol={{ span: 8, offset: 8 }}>
          <Button type="primary" block htmlType="submit" loading={isFetching}>确定</Button>
        </FormItem>
      </Form>
    )
  }
}

const WrapAddConsortium = Form.create({})(AddConsortium);
export default WrapAddConsortium;
