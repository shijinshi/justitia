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
import { downloadFile } from '@/utils/utils'


const FormItem = Form.Item;
const Option = Select.Option;
const { TextArea } = Input;
const RadioGroup = Radio.Group;

const host = process.env.NODE_ENV === "production" ? '/api' : `http://${window.hostIp}`;

@connect(({ CAUserManager }) => {
  return {
    CAUserManager
  };
})
class Enroll extends React.Component {
  constructor(props) {
    super(props);
    this.downloadEle = React.createRef();
  }


  handleSubmit = () => {
    const { form, dispatch, serverName, userId } = this.props;
    const downloadEle = this.downloadEle.current;
    if (serverName) {
      form.validateFields((err, values) => {
        if (!err) {
          if (values.download) {
            const params = {
              ele: downloadEle,
              fileName: `enroll_${userId}_${serverName}.zip`,
              url: `${host}/ca/user/enroll/${serverName}/${userId}`,
              type: 'post',
              params: { download: values.download },
              headers: { 'Content-Type': 'application/json' }
            }
            downloadFile(params);
          } else {
            dispatch({
              type: 'CAUserManager/handleEnrollCAUser',
              payload: {
                download: values.download,
                serverName,
                userId
              }
            })
          }
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
    const { isFetching } = CAUserManager.enrollCAUser;
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
        <Form>
          <FormItem {...formItemLayout} label="用户ID">
            {getFieldDecorator('userId', {
              initialValue: userId,
            })(<Input readOnly />)}
          </FormItem>
          <FormItem {...formItemLayout} label="是否下载证书">
            {getFieldDecorator('download', {
              initialValue: false,
            })(<RadioGroup>
              <Radio value={true}>是</Radio>
              <Radio value={false}>否</Radio>
            </RadioGroup>)}
          </FormItem>
          <div ref={this.downloadEle}></div>
        </Form>
      </Modal>
    )
  }
}
const WrapEnroll = Form.create({})(Enroll);
export default WrapEnroll;