import React, { Fragment } from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'dva';
import {
  Row,
  Col,
  Icon,
  Select,
  Table,
  Radio,
  Button,
  Form,
  Input,
  Upload,
  Tooltip,
  message,
  Switch
} from 'antd';


const FormItem = Form.Item;
const RadioGroup = Radio.Group;
const Option = Select.Option;

@connect(({ host, loading }) => {
  return {
    host
  };
})
class HostUpdate extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      tls: true,
      caFileList: [],
      certFileList: [],
      keyFileList: [],
      couldSubmit: false,
      getOneStamp: 0,
      nowHostName: ''    //当前展示的host
    }

    this.caChange = this.caChange.bind(this);
    this.certChange = this.certChange.bind(this);
    this.keyChange = this.keyChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.beforeUpload = this.beforeUpload.bind(this);
    this.handleChangeHostName = this.handleChangeHostName.bind(this);
    this.changeSwitch = this.changeSwitch.bind(this);
  }

  changeTls = (e) => {
    this.setState({
      tls: e.target.value
    })
  }

  componentDidMount() {
    const { hostName, dispatch } = this.props;
    dispatch({
      type: 'host/getOneHostHandle',
      payload: {
        hostName
      }
    })
  }


  componentWillReceiveProps(nextProps, nextState) {
    const { hostName, dispatch, couldUpdate } = nextProps;
    const { nowHostName } = this.state;
    if (hostName && nowHostName !== hostName) {
      dispatch({
        type: 'host/getOneHostHandle',
        payload: {
          hostName
        }
      })
      this.setState({
        nowHostName: hostName
      })
    }
  }

  changeSwitch = (checked) => {
    const { form } = this.props;
    if (checked) {
      this.setState({
        couldSubmit: true
      })
    } else {
      form.resetFields();
      this.setState({
        couldSubmit: false
      })
    }
  }

  handleChangeHostName = (name) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'host/getOneHostHandle',
      payload: {
        hostName: name
      }
    })
  }

  caChange = (info) => {
    console.log(info);
    let fileList = info.fileList;
    fileList = fileList.slice(-1);
    this.setState({
      caFileList: fileList
    })
  }

  certChange = (info) => {
    let fileList = info.fileList;
    fileList = fileList.slice(-1);
    this.setState({
      certFileList: fileList
    })
  }

  keyChange = (info) => {
    let fileList = info.fileList;
    fileList = fileList.slice(-1);
    this.setState({
      keyFileList: fileList
    })
  }

  beforeUpload = (file) => {
    return false;
  }

  handleSubmit = (e) => {
    e.preventDefault();
    const { form, dispatch } = this.props;
    form.validateFields((err, values) => {
      if (!err) {
        let formData = new FormData();
        Object.keys(values).forEach((key) => {
          if (values[key] !== undefined) {
            formData.append(key, values[key] && values[key].file ? values[key].file : values[key]);
          }
        })
        dispatch({
          type: 'host/updateHostHandle',
          payload: {
            hostName: values.hostName,
            formData
          }
        });
      }
    })
  }

  render() {
    const { form, host } = this.props;
    const { hostData, oneHost, updateHost } = host;
    const { getFieldDecorator } = form;
    const { isLoading } = updateHost;
    const { couldSubmit, tls } = this.state;

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
        {/* <FormItem {...formItemLayout} label="编辑">
          {getFieldDecorator('edit', {
            initialValue: false,
          })(
            <Switch onChange={this.changeSwitch} />
          )}
        </FormItem> */}
        <FormItem {...formItemLayout} hasFeedback={true} label="宿主机名称">
          {getFieldDecorator('hostName', {
            initialValue: oneHost ? oneHost.hostName : '',
            rules: [
              {
                required: true,
                message: '请选择主机名称！',
              }
            ],
          })(
            <Select
              showSearch
              placeholder="选择主机"
              optionFilterProp="children"
              onChange={this.handleChangeHostName}
              filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
            >
              {
                hostData && hostData.map((ele) => {
                  return (
                    <Option key={ele.hostName} value={ele.hostName}>{ele.hostName}</Option>
                  )
                })
              }
            </Select>
          )}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="docker端口">
          {getFieldDecorator('port', {
            initialValue: oneHost ? oneHost.port : '2375',
            rules: [
              {
                required: true,
                message: '端口不能为空！',
              },
              {
                pattern: /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/,
                message: '输入的端口不正确！',
              },
            ],
          })(<Input placeholder="请输入服务端口" />)}
        </FormItem>
        <FormItem {...formItemLayout} label="是否使用tls">
          {getFieldDecorator('tlsEnable', {
            initialValue: true,
          })(
            <RadioGroup onChange={this.changeTls}>
              <Radio value={true}>是</Radio>
              <Radio value={false}>否</Radio>
            </RadioGroup>
          )}
        </FormItem>
        <FormItem {...formItemLayout} label="protocol">
          {getFieldDecorator('protocol', {
            initialValue: 'tcp',
          })(
            <RadioGroup>
              <Radio value={'tcp'}>tcp</Radio>
              <Radio value={'udp'}>udp</Radio>
            </RadioGroup>
          )}
        </FormItem>
        {tls ?
          <Fragment>
            <FormItem {...formItemLayout} label="CA证书">
              {getFieldDecorator('ca', {
              })(
                <Upload
                  name="ca"
                  fileList={this.state.caFileList}
                  onChange={this.caChange}
                  beforeUpload={this.beforeUpload}

                >
                  <Button>
                    <Icon type="upload" /> 上传CA证书
                </Button>
                </Upload>
              )}
            </FormItem>
            <FormItem {...formItemLayout} label="客户端证书">
              {getFieldDecorator('cert', {
              })(
                <Upload
                  name="cert"
                  fileList={this.state.certFileList}
                  onChange={this.certChange}
                  beforeUpload={this.beforeUpload}

                >
                  <Button>
                    <Icon type="upload" /> 上传客户端证书
                </Button>
                </Upload>
              )}
            </FormItem>
            <FormItem {...formItemLayout} label="客户端私钥">
              {getFieldDecorator('key', {
              })(
                <Upload
                  name="key"
                  fileList={this.state.keyFileList}
                  onChange={this.keyChange}
                  beforeUpload={this.beforeUpload}

                >
                  <Button>
                    <Icon type="upload" /> 上传客户端私钥
                </Button>
                </Upload>
              )}
            </FormItem>

          </Fragment>
          : null}
        <FormItem wrapperCol={{ span: 8, offset: 8 }}>
          <Button type="primary" block htmlType="submit" loading={isLoading}>
            更新
          </Button>
        </FormItem>
      </Form>
    );
  }
}

const WrapHostUpdate = Form.create({})(HostUpdate);
export default WrapHostUpdate;
