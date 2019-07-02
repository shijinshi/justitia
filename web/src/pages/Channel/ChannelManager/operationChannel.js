import React, { Component } from 'react';
import { Modal, Select, Form, Input } from 'antd';
import { connect } from 'dva';

const FormItem = Form.Item;
const Option = Select.Option;
const TextArea = Input.TextArea;

@connect(({ ChannelManager, network }) => {
  return {
    ChannelManager,
    network
  };
})
class Operation extends Component {

  componentDidUpdate(prevProps, prevState) {
    const { type, params, isShow, dispatch } = this.props;
    if (isShow && isShow !== prevProps.isShow && type === 'delOrg') {
      dispatch({
        type: 'ChannelManager/handleGetChannelMspId',
        payload: {
          channelName: params
        }
      });
    }
  }
  // 关闭弹窗
  onhandleCloseModal = () => {
    this.props.onCloseModal();
  }
  // 提交表单
  handleSubmit = () => {
    const { form, dispatch, type, params } = this.props;
    let action = '', payload = {};
    form.validateFields((err, values) => {
      if (err) return false;
      switch (type) {
        case 'peer':
          action = 'ChannelManager/handleAddPeer';
          payload = {
            ...values,
            channelName: params
          }
          break;
        case 'delOrg':
          action = 'ChannelManager/handleDelOrg';
          payload = {
            ...values,
            channelName: params
          }
          break;
        default:
          break;
      }
      dispatch({
        type: action,
        payload
      });
    });
  }

  render() {
    // Form表单layout
    const formItemLayout = {
      labelCol: {
        xs: { span: 27 },
        sm: { span: 5 },
      },
      wrapperCol: {
        xs: { span: 16 },
        sm: { span: 16 },
      },
    };
    const { form, isShow, title, type, network, ChannelManager } = this.props;
    const { isFetching, getChannelMspId } = ChannelManager;
    const { peerConfig } = network;
    const { getFieldDecorator } = form;

    return (
      <Modal title={title} visible={isShow} confirmLoading={isFetching} onCancel={() => this.onhandleCloseModal()} onOk={() => this.handleSubmit()}>
        <Form>
          {type === "peer" ?
            <React.Fragment>
              <FormItem {...formItemLayout} hasFeedback={true} label={"peer节点"}>
                {getFieldDecorator('peerName', {
                  rules: [
                    {
                      required: true,
                      message: 'peer节点不能为空！',
                    }
                  ],
                })(
                  <Select>
                    {peerConfig.map(ele => (
                      <Option key={ele.containerId} value={ele.peerName}>{ele.peerName}</Option>
                    ))}
                  </Select>
                )}
              </FormItem>
            </React.Fragment> :
            <React.Fragment>
              <FormItem {...formItemLayout} hasFeedback={true} label={"组织"}>
                {getFieldDecorator('orgName', {
                  rules: [
                    {
                      required: true,
                      message: '组织不能为空！',
                    }
                  ],
                })(
                  <Select>
                    {getChannelMspId.map(ele => (
                      <Option key={ele} value={ele}>{ele}</Option>
                    ))}
                  </Select>
                )}
              </FormItem>
              <FormItem {...formItemLayout} hasFeedback={true} label={"删除原因"}>
                {getFieldDecorator('description', {
                  rules: [
                    {
                      required: true,
                      message: '原因不能为空！',
                    }
                  ],
                })(
                  <TextArea row={6} />
                )}
              </FormItem>
            </React.Fragment>
          }
        </Form>
      </Modal>
    )
  }
}

const OperationChannel = Form.create({})(Operation);
export default OperationChannel;
