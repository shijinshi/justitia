import React, { Component } from 'react';
import { Select, Form, Input, Modal } from 'antd';
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
class DelConsortium extends Component {
  constructor(props) {
    super(props);
    this.state = {
      member: ''
    }
  }

  componentDidUpdate(prevProps, prevState) {
    const { isShow, dispatch, ordererName } = this.props;
    if (isShow && isShow !== prevProps.isShow) {
    dispatch({
      type: 'consortiumModel/handleGetConsortium',
      payload: {
        ordererName
      }
    });
    }
  }
  // 选择的联盟成员
  handleSelect = (e) => {
    this.setState({
      member: e
    });
  }
  // 关闭弹窗
  handleCloseModal = () => {
    this.props.onCloseModal();
  }
  // 提交表单
  handleSubmit = () => {
    const { form, dispatch } = this.props;
    form.validateFields((err, values) => {
      if (err) return false;
      dispatch({
        type: 'consortiumModel/handelDelConsortium',
        payload: values
      });
    });
  }

  render() {
    // Form表单layout
    const formItemLayout = {
      labelCol: {
        xs: { span: 25 },
        sm: { span: 7 },
      },
      wrapperCol: {
        xs: { span: 16 },
        sm: { span: 16 },
      },
    };
    const { form, network, consortiumModel, ordererName, isShow } = this.props;
    const { ordererConfig } = network;
    const { consortiumList, isFetching, isLoading } = consortiumModel;
    const { getFieldDecorator } = form;
    const { member } = this.state;

    return (
      <Modal title="删除orderer节点联盟成员" width={600} visible={isShow} confirmLoading={isFetching} onCancel={() => this.handleCloseModal()} onOk={() => this.handleSubmit()}>
        <Form>
          <FormItem {...formItemLayout} hasFeedback={true} label={"所属orderer节点"}>
            {getFieldDecorator('ordererName', {
              initialValue: ordererName,
              rules: [
                {
                  required: true,
                  message: 'order不能为空！',
                }
              ],
            })(
              <Input disabled />
            )}
          </FormItem>
          <FormItem {...formItemLayout} hasFeedback={true} label={"联盟名称"}>
            {getFieldDecorator('consortium', {
              rules: [
                {
                  required: true,
                  message: '请选择要删除成员的联盟！',
                }
              ],
            })(
              <Select onSelect={this.handleSelect} loading={isLoading} placeholder="请选择要删除成员的联盟">
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
                  message: '请选择要删除的组织名称！',
                }
              ],
            })(
              <Select loading={isLoading} placeholder="请选择要删除的组织名称">
                {consortiumList[member]&&consortiumList[member].map(ele => (
                  <Option key={ele} value={ele}>{ele}</Option>
                ))}
              </Select>
            )}
          </FormItem>
        </Form>
      </Modal>
    )
  }
}

const WrapDelConsortium = Form.create({})(DelConsortium);
export default WrapDelConsortium;
