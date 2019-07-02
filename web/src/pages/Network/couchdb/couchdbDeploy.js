import React from 'react';
import { connect } from 'dva';
import { Icon, Tabs, Radio, DatePicker, Select, Button, Form, Input, Tooltip } from 'antd';
import { clearSpacing } from '@/utils/utils';

const Option = Select.Option;
const FormItem = Form.Item;

@connect(({ network, loading }) => {
  return {
    network,
    loading: loading.effects['network/getConfigCouchdb'],
  };
})
class DeployCouchdb extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      useCouchdb: true,
    };
    this.changeCoucndb = this.changeCoucndb.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  componentDidMount() {
    const { dispatch } = this.props;
  }

  componentDidUpdate(prevProps){
    const prevNetwork = prevProps.network;
    const prevCouchdbDeploy = prevNetwork.couchdbDeploy;
    const { network, updateTable } = this.props;
    const { couchdbDeploy } = network;

    if(couchdbDeploy && !prevCouchdbDeploy){
      updateTable()
    }else if(couchdbDeploy && prevCouchdbDeploy && couchdbDeploy.time !== prevCouchdbDeploy.time){
      updateTable()
    }

  }

  changeCoucndb = e => {
    const value = e.target.value;
    if (value === false) {
      this.setState(
        {
          useCouchdb: false,
        },
        () => {
          const { form } = this.props;
          form.resetFields(['couchdbName']);
          form.validateFields(['couchdbName'], { force: true });
        }
      );
    } else {
      this.setState({
        useCouchdb: true,
      });
    }
  };

  handleSubmit = (e) => {
    e.preventDefault();
    const { dispatch, form, token } = this.props;
    form.validateFields((err, values) => {
      if (!err) {
        const valuesNoSpacing = clearSpacing(values);
        dispatch({
          type: 'network/couchdbDeploy',
          payload: {
            token,
            ip: valuesNoSpacing.ip_,
            port: valuesNoSpacing.port_,
            userName: valuesNoSpacing.userName_,
            password: valuesNoSpacing.password_,
            name: valuesNoSpacing.name_,
            containerName: valuesNoSpacing.containerName_,
            imageVersion: valuesNoSpacing.imageVersion_,
            requestPort: valuesNoSpacing.requestPort_,
          },
        });
      }
    });
  };

  render() {
    const { network, form, couchdbImageVersion } = this.props;
    const { getFieldDecorator } = form;

    console.log('deployPeerState', this.state, 'deployPeerProps', this.props);

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

    const CardLabel = ({ name, text }) => (
      <span>
        {name}
        &nbsp;
        <Tooltip title={text}>
          <Icon type="question-circle-o" />
        </Tooltip>
      </span>
    );
    return (
      <Form onSubmit={this.handleSubmit}>
        <FormItem {...formItemLayout} hasFeedback={true} label="服务器ip">
          {getFieldDecorator('ip_', {
            rules: [
              {
                required: true,
                message: '请输入节点名称！',
              },
              {
                pattern: /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/,
                message: '输入的IP不正确！',
              },
            ],
          })(<Input />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="服务器端口">
          {getFieldDecorator('port_', {
            initialValue: '22',
            rules: [
              {
                required: true,
                message: '请输入节点请求端口！',
              },
              {
                pattern: /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/,
                message: '输入的端口不正确！',
              },
            ],
          })(<Input />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="服务器用户名">
          {getFieldDecorator('userName_', {
            initialValue: '',
            rules: [
              {
                required: true,
                message: '请输入部署服务器的用户名！',
              },
            ],
          })(<Input />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="服务器密码">
          {getFieldDecorator('password_', {
            initialValue: '',
            rules: [
              {
                required: true,
                message: '请输入部署服务器的密码！',
              },
            ],
          })(<Input type="password" />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="docker-compose版本">
          {getFieldDecorator('composeFileFormat', {
            initialValue: '',
            rules: [
              {
                required: true,
                message: '请输入docker-compose版本！',
              },
            ],
          })(<Input />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="节点名称">
          {getFieldDecorator('name_', {
            initialValue: '',
            rules: [
              {
                required: true,
                message: '请输入容器名称！',
              },
            ],
          })(<Input />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="容器名称">
          {getFieldDecorator('containerName_', {
            initialValue: '',
            rules: [
              {
                required: true,
                message: '请输入容器名称！',
              },
            ],
          })(<Input />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="镜像版本">
          {getFieldDecorator('imageVersion_', {
            initialValue: '',
            rules: [
              {
                required: true,
                message: '请选择容器版本！',
              },
            ],
          })(
            <Select
              showSearch
              placeholder="选择一个镜像版本"
              optionFilterProp="children"
              filterOption={(input, option) =>
                option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
              }
            >
              {couchdbImageVersion &&
                couchdbImageVersion.map((item, i) => {
                  return (
                    <Option key={i} value={item}>
                      {item}
                    </Option>
                  );
                })}
            </Select>
          )}
        </FormItem>

        <FormItem
          {...formItemLayout}
          hasFeedback={true}
          label={<CardLabel name="对外请求端口" text="推荐使用默认端口" />}
        >
          {getFieldDecorator('requestPort_', {
            initialValue: '5984',
            rules: [
              {
                required: true,
                message: '请输入端口！',
              },
              {
                pattern: /^([0-9]|[1-9]\d{1,3}|[1-5]\d{4}|6[0-4]\d{4}|65[0-4]\d{2}|655[0-2]\d|6553[0-5])$/,
                message: '输入的端口不正确！',
              },
            ],
          })(<Input />)}
        </FormItem>

        <FormItem wrapperCol={{ span: 8, offset: 8 }}>
          <Button type="primary" block htmlType="submit">
            部署
          </Button>
        </FormItem>
      </Form>
    );
  }
}

const WrapDeployCouchdb = Form.create()(DeployCouchdb);
export default WrapDeployCouchdb;
