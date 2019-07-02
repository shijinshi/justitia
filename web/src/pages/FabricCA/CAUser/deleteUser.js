import React from 'react';
import { connect } from 'dva';
import {
  Tabs,
  Button,
  Form,
  Input,
  Modal,
  Select,
  Radio,
} from 'antd';
import LableToolTip from '../labelToolTip';
import { toFile } from '@/utils/utils'


const FormItem = Form.Item;
const Option = Select.Option;
const { TextArea } = Input;
const RadioGroup = Radio.Group;

@connect(({ CAUserManager }) => {
  return {
    CAUserManager
  };
})
class DeleteUser extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      nowStamp: ''
    }
    this.downloadEle = React.createRef();
  }

  handleSubmit = () => {
    const { form, dispatch, serverName, userId } = this.props;
    const downloadEle = this.downloadEle.current;
    if (serverName) {
      form.validateFields((err, values) => {
        if (!err) {
          dispatch({
            type: 'CAUserManager/handleDeleteCAUser',
            payload: {
              ...values,
              revokee: userId,
              checked: false,
              genCRL: false,
              serverName,
              userId
            }
          });
        }
      })
    } else {
      message.error('请先选择CA服务！')
    }
  }

  // 关闭登记弹窗
  handleCancel = () => {
    this.props.onCloseModal();
  }

  render() {
    const { userData, form, userId, visible, modalTitle, CAUserManager } = this.props;
    const { getFieldDecorator } = form;
    const { isFetching } = CAUserManager.deleteCAUser;
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
      <Modal
        title={modalTitle}
        visible={visible}
        onOk={() => this.handleSubmit()}
        onCancel={() => this.handleCancel()}
        confirmLoading={isFetching}
      >
        <Form onSubmit={this.handleSubmit}>
          {/* <FormItem {...formItemLayout} label="用户ID">
            {getFieldDecorator('revokee', {
              initialValue: userId
            })(<Input readOnly />)}
          </FormItem> */}
          <FormItem {...formItemLayout} label="注销原因">
            {getFieldDecorator('reason', {
              initialValue: 'unspecified',
            })(<Select>
              <Option key={'unspecified'} value={'unspecified'}>unspecified</Option>
              <Option key={'keycompromise'} value={'keycompromise'}>keycompromise</Option>
              <Option key={'cacompromise'} value={'cacompromise'}>cacompromise</Option>
              <Option key={'affiliationchange'} value={'affiliationchange'}>affiliationchange</Option>
              <Option key={'superseded'} value={'superseded'}>superseded</Option>
              <Option key={'cessationofoperation'} value={'cessationofoperation'}>cessationofoperation</Option>
              <Option key={'certificatehold'} value={'certificatehold'}>certificatehold</Option>
              <Option key={'privilegewithdrawn'} value={'privilegewithdrawn'}>privilegewithdrawn</Option>
              <Option key={'aacompromise'} value={'aacompromise'}>aacompromise</Option>
            </Select>)}
          </FormItem>
          {/* <FormItem {...formItemLayout} label="生成证书吊销列表">
            {getFieldDecorator('genCRL', {
              initialValue: false
            })(<RadioGroup >
              <Radio value={true}>是</Radio>
              <Radio value={false}>否</Radio>
            </RadioGroup>)}
          </FormItem> */}
          <div ref={this.downloadEle}></div>
        </Form>
      </Modal>
    )
  }
}
const WrapDeleteUser = Form.create({})(DeleteUser);
export default WrapDeleteUser;