import React from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'dva';
import moment from 'moment';
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
import PageHeaderLayout from '@/components/PageHeaderWrapper';
import TabPaneCon from '@/components/TabPaneCon';
import WrapImageDetail from './imageDetail';
import WrapTagForm from './tagForm';
// import SelectHost from '../selectHost';
import styles from '../style.less';

import org from '@/assets/组织.png';

const { TabPane } = Tabs;
const Option = Select.Option;
const FormItem = Form.Item;
const RadioGroup = Radio.Group;

@connect(({ host, dockerImage, loading }) => {
  return {
    host,
    dockerImage,
    loading: loading.effects['dockerImage/handleGetDockerImage'],
  };
})
class DockerImage extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      activeKey: '1',   //展开的tab页
      nowStamp: 0,
    };

  }

  componentDidMount() {
    const { dispatch, location } = this.props;
    const { query } = location;

    dispatch({
      type: 'host/getHostHandle'
    });
    if (query.hostName) {
      dispatch({
        type: 'dockerImage/handleGetDockerImage',
        payload: { hostName: query.hostName }
      });
      dispatch({
        type: 'host/saveNowHost',
        payload: query.hostName
      })
      this.setState({
        hostName: query.hostName
      })
    }

  }

  componentDidUpdate() {
    const { dockerImage, dispatch, host } = this.props;
    const { nowHost } = host;
    const { postImageList } = dockerImage;
    const { hostName, nowStamp } = this.state;
    if (postImageList) {   //拉取镜像后刷新镜像列表
      const { stamp } = postImageList
      if (stamp && stamp !== nowStamp) {
        this.setState({
          nowStamp: stamp
        });
        dispatch({
          type: 'dockerImage/handleGetDockerImage',
          payload: { hostName }
        })
      }
    }
    if (nowHost && nowHost !== hostName) {
      this.setState({
        hostName: nowHost
      })
      dispatch({
        type: 'dockerImage/handleGetDockerImage',
        payload: { hostName: nowHost }
      });
    }

  }


  handleChangeTabs = (key) => {
    this.setState({
      activeKey: key
    })
  }

  handleClickId = (id) => {
    const { dockerImage } = this.props;
    const { getImageList } = dockerImage;
    let imageName = '';
    getImageList.forEach(ele => {
      if (ele.Id === id) {
        imageName = ele.RepoTags[0].slice(0, ele.RepoTags[0].indexOf(':'))
      }
    })
    this.setState({
      activeKey: '2',
      imageId: id,
      imageName
    })
  }

  changeSelectHost = (key) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'dockerImage/handleGetDockerImage',
      payload: { hostName: key }
    })
    dispatch({
      type: 'host/saveNowHost',
      payload: key
    })
    this.setState({
      hostName: key
    })
  }

  changeSelectImage = (key) => {
    const { dispatch, dockerImage } = this.props;
    const { getImageList } = dockerImage;
    const { hostName } = this.state;
    dispatch({
      type: 'dockerImage/handleGetImageDetail',
      payload: {
        hostName,
        ImageId: key
      }
    });
    let imageName = '';
    getImageList.forEach(ele => {
      if (ele.Id.slice(7) === key) {
        imageName = ele.RepoTags[0].slice(0, ele.RepoTags[0].indexOf(':'))
      }
    })
    this.setState({
      imageName
    })
  }

  handleSubmit = (e) => {
    e.preventDefault();
    const { form, dispatch } = this.props;
    const { hostName } = this.state;
    if (!hostName) {
      message.error('请先选择主机！');
      return;
    }
    form.validateFields((err, values) => {
      if (!err) {
        dispatch({
          type: 'dockerImage/handlePostDockerImage',
          payload: {
            ...values,
            hostName
          }
        });
      }
    })
  }



  render() {
    const { dockerImage, host, form } = this.props;
    const { getFieldDecorator } = form;
    const { hostData } = host;
    let { getImageList, postImageList } = dockerImage;
    const { activeKey, imageId, hostName, imageName } = this.state;

    const detailInfo = (
      <div className={styles.peer}>镜像管理</div>
    );

    function SelectHost(props) {
      return <Select
        showSearch
        style={{ width: 200 }}
        placeholder="请选择主机"
        defaultValue={props.hostName}
        optionFilterProp="children"
        onChange={props.changeSelectHost}
        filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
      >
        {
          props.hostData && props.hostData.length > 0 ? props.hostData.map((item) => {
            return (<Option value={item.hostName} key={item.hostName}>{item.hostName}</Option>)
          }) : ''
        }
      </Select>
    }

    const selectImage = (
      <Select
        showSearch
        style={{ width: 200 }}
        placeholder="请选择镜像"
        optionFilterProp="children"
        onChange={this.changeSelectImage}
        filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
      >
        {
          getImageList && getImageList.length > 0 ? getImageList.map((item) => {
            return (<Option value={item.Id.slice(7)} key={item.Id.slice(7)}>{item.RepoTags[0].slice(0, item.RepoTags[0].indexOf(':'))}</Option>)
          }) : ''
        }
      </Select>
    )

    const pull = (
      <Form onSubmit={this.handleSubmit} layout={'inline'}>
        <FormItem label="镜像名称或编号">
          {getFieldDecorator('imageName', {
            rules: [
              // {
              //     pattern: /^[0-9a-zA-Z]+$/,message:'只能输入数字或者字母组成的名称'
              // }
            ],
          })(<Input placeholder="请输入镜像名称或编号" />)}
        </FormItem>
        <FormItem label="镜像版本">
          {getFieldDecorator('tag', {
            initialValue: 'latest',
            rules: [
              // {
              //     pattern: /^[0-9a-zA-Z]+$/,message:'只能输入数字或者字母组成的名称'
              // }
            ],
          })(<Input placeholder="请输入镜像版本" />)}
        </FormItem>
        <FormItem>
          <Button type="primary" block htmlType="submit">
            拉取
          </Button>
        </FormItem>
      </Form>
    )


    getImageList && getImageList.map((item, index) => {
      return item.key = index
    })

    const imageColumns = [{
      dataIndex: 'Id',
      title: '镜像名称',
      render: (text, item) => (<a title={text} onClick={this.handleClickId.bind(this, text)}>{item.RepoTags[0].slice(0, item.RepoTags[0].indexOf(':'))}</a>)
    }, {
      dataIndex: 'RepoTags',
      title: '镜像版本',
      render: (text) => {
        let tag = []
        text.map((ele) => {
          tag.push(ele.slice(ele.indexOf(':') + 1))
        })
        return tag.join(' , ')
      }
    }, {
      dataIndex: 'Created',
      title: '创建时间',
      sorter: (a, b) => (a.Created - b.Created),
      render: (text) => (<span>{moment(text * 1000).format('YYYY-MM-DD HH:mm:ss')}</span>)
    }, {
      dataIndex: 'Size',
      title: '镜像大小',
      sorter: (a, b) => (a.Size - b.Size),
    }, {
      dataIndex: 'VirtualSize',
      title: 'VirtualSize',
      render: (text) => (<span>{JSON.stringify(text)}</span>)
    }];

    return (
      <PageHeaderLayout detailInfo={detailInfo} toggleSwitch={<SelectHost changeSelectHost={this.changeSelectHost} hostData={hostData} hostName={hostName} />} logo={org}>
        <Tabs onChange={this.handleChangeTabs} activeKey={activeKey} className={styles.tabs}>
          <TabPane
            className={styles.tabChildren}
            tab={
              <span>
                <Icon type="file-text" />
                容器列表
                </span>
            }
            key="1"
          >
            <TabPaneCon children={<Table dataSource={getImageList} columns={imageColumns} />} select={pull} title="镜像列表" />
            <TabPaneCon children={<WrapTagForm hostName={hostName} getImageList={getImageList} />} title="更新镜像版本" />
          </TabPane>
          <TabPane
            className={styles.tabChildren}
            tab={
              <span>
                <Icon type="file-text" />
                镜像详情
                </span>
            }
            key="2"
          >
            <TabPaneCon children={<WrapImageDetail imageName={imageName} hostName={hostName} imageId={imageId} />} select={selectImage} title="镜像详情" />

          </TabPane>

        </Tabs>
      </PageHeaderLayout>
    );
  }
}

const WrapDockerImage = Form.create({})(DockerImage);
export default WrapDockerImage;
