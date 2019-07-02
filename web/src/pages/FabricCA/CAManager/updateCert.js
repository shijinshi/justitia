import React from 'react';
import { connect } from 'dva';
import {
  Row,
  Col,
  Icon,
  Tabs,
  Table,
  Radio,
  Button,
  Form,
  Upload,
  Select,
  Tooltip,
  message,
} from 'antd';

const FormItem = Form.Item;
const Option = Select.Option;

@connect(({ CAManager }) => {
  return {
    CAManager
  };
})
class UpdateCert extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      certFileList: [],
      keyFileList: []
    };
  }



  certChange = (info) => {
    let certFileList = info.fileList;
    certFileList = certFileList.slice(-1);
    this.setState({
      certFileList
    })
  }

  keyChange = (info) => {
    let keyFileList = info.fileList;
    keyFileList = keyFileList.slice(-1);
    this.setState({
      keyFileList
    })
  }

  beforeUpload = (file) => {
    return false;
  }

  handleSubmit = (e) => {
    e.preventDefault();
    const { form, dispatch, serverName } = this.props;
    if (serverName) {
      form.validateFields((err, values) => {
        if (!err) {
          let formData = new FormData();
          Object.keys(values).forEach(key => {
            formData.append(key, values[key].file)
          })
          dispatch({
            type: 'CAManager/handleUpdateCert',
            payload: {
              formData,
              serverName
            }
          })
        }
      })
    } else {
      message.error('请先选择CA服务！')
    }
  }



  render() {
    const { getImageList, form, serverName } = this.props;
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
      <Form onSubmit={this.handleSubmit}>
        <FormItem {...formItemLayout} label="CA证书">
          {getFieldDecorator('cert', {
            rules: [{
              required: true,
              message: '请上传ca证书！'
            }]
          })(
            <Upload
              name="ca"
              fileList={this.state.certFileList}
              onChange={this.certChange}
              beforeUpload={this.beforeUpload}
            >
              <Button>
                <Icon type="upload" /> 上传CA证书
              </Button>
            </Upload>
          )}
        </FormItem>
        <FormItem {...formItemLayout} label="CA证书">
          {getFieldDecorator('key', {
            rules: [{
              required: true,
              message: '请上传ca证书！'
            }]
          })(
            <Upload
              name="ca"
              fileList={this.state.keyFileList}
              onChange={this.keyChange}
              beforeUpload={this.beforeUpload}
            >
              <Button>
                <Icon type="upload" /> 上传CA私钥
              </Button>
            </Upload>
          )}
        </FormItem>
        <FormItem wrapperCol={{ span: 8, offset: 8 }}>
          <Button disabled={serverName ? false : true} type="primary" block htmlType="submit">确定</Button>
        </FormItem>
      </Form>
    )
  }
}
const WrapUpdateCert = Form.create({})(UpdateCert);
export default WrapUpdateCert;