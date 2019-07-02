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
  Input,
  Select,
  Tooltip,
  message,
} from 'antd';

const FormItem = Form.Item;
const Option = Select.Option;

@connect(({ dockerImage }) => {
  return {
    dockerImage
  };
})
class TagForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      nowStamp: 0
    };
  }

  componentDidUpdate() {
    const { dockerImage, hostName, dispatch } = this.props;
    const { imageTag } = dockerImage;
    const { nowStamp } = this.state;
    if (imageTag) {
      const { stamp } = imageTag;
      if (stamp && stamp !== nowStamp) {
        dispatch({
          type: 'dockerImage/handleGetDockerImage',
          payload: { hostName }
        });
        this.setState({
          nowStamp: stamp
        })
      }
    }

  }

  handleSubmit = (e) => {
    e.preventDefault();
    const { form, dispatch, hostName } = this.props;
    if (hostName) {
      form.validateFields((err, values) => {
        if (!err) {
          dispatch({
            type: 'dockerImage/handleImageTag',
            payload: {
              ...values,
              hostName
            }
          })
        }
      })
    } else {
      message.error('请先选择主机！')
    }
  }

  changeHostName = (key) => {
    const { getImageList } = this.props;
    let imageNameWithRepository = null;
    getImageList.forEach(element => {
      if (element.Id.slice(7) === key) {
        imageNameWithRepository = element.RepoTags[element.RepoTags.length - 1]
      }
    });
    this.setState({
      imageNameWithRepository
    })
  }

  render() {
    const { getImageList, form, dockerImage } = this.props;
    const { getFieldDecorator } = form;
    const { isLoading } = dockerImage.imageTag;
    const { imageNameWithRepository } = this.state;
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
        <FormItem {...formItemLayout} hasFeedback={true} label="镜像名称">
          {getFieldDecorator('imageId', {
            rules: [
              {
                required: true,
                message: '请选择镜像！',
              }
            ],
          })(<Select
            showSearch
            placeholder="请选择镜像"
            optionFilterProp="children"
            onChange={this.changeHostName}
            filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
          >
            {
              getImageList && getImageList.length > 0 ? getImageList.map((item) => {
                return (<Option value={item.Id.slice(7)} key={item.Id.slice(7)}>{item.RepoTags[0].slice(0, item.RepoTags[0].indexOf(':'))}</Option>)
              }) : ''
            }
          </Select>)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} style={{ display: 'none' }}>
          {getFieldDecorator('imageNameWithRepository', {
            initialValue: imageNameWithRepository ? imageNameWithRepository : ''
          })(<Input type="hidden" />)}
        </FormItem>
        <FormItem {...formItemLayout} hasFeedback={true} label="镜像版本">
          {getFieldDecorator('tag', {
            rules: [
              {
                required: true,
                message: '请输入版本号！',
              },
              {
                pattern: /^[0-9a-zA-Z][0-9a-zA-Z.]*$/, message: '输入的版本号不合法'
              }
            ],
          })(<Input placeholder="输入版本号" />)}
        </FormItem>
        <FormItem wrapperCol={{ span: 8, offset: 8 }}>
          <Button type="primary" block htmlType="submit" loading={isLoading}>确定</Button>
        </FormItem>
      </Form>
    )
  }
}
const WrapTagForm = Form.create({})(TagForm);
export default WrapTagForm;