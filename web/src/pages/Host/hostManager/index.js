import React from 'react';
import { connect } from 'dva';
import router from 'umi/router';
import {
  Icon,
  Tabs,
  Table,
  Radio,
  Form,
} from 'antd';
import PageHeaderLayout from '@/components/PageHeaderWrapper';
import TabPaneCon from '@/components/TabPaneCon';
import WrapHostAdd from './add';
import WrapHostUpdate from './update';
import styles from '../style.less';

import org from '@/assets/组织.png';

const { TabPane } = Tabs;
@connect(({ host, docker, loading }) => {
  return {
    host,
    docker,
    loading: loading.effects['host/getHostHandle'],
  };
})
class Host extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      updateHostInfo: false, // 更新host信息标志
      activeKey: "1",   // 展开的tab页
      deleteStamp: 0,
    };

    this.updateList = this.updateList.bind(this);
    this.handleClickHostName = this.handleClickHostName.bind(this);
    this.handleClickDockerInfo = this.handleClickDockerInfo.bind(this);
    this.handleClickDockerImage = this.handleClickDockerImage.bind(this);
    this.handleClickDockerNet = this.handleClickDockerNet.bind(this);
    this.handleClickDockerVolume = this.handleClickDockerVolume.bind(this);
  }

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'host/getHostHandle'
    });
  }


  componentDidUpdate() {
    const { dispatch, host } = this.props;
    const { deleteHost } = host;
    const { updateHostInfo, deleteStamp } = this.state;
    if (updateHostInfo) {
      dispatch({
        type: 'host/getHostHandle'
      });
      this.setState({
        updateHostInfo: false
      })
    }

    if (deleteHost && deleteHost.stamp !== deleteStamp) {
      this.setState({
        updateHostInfo: true,
        deleteStamp: deleteHost.stamp
      })
    }
  }

  updateList = () => {
    this.setState({
      updateHostInfo: true
    })
  }

  handleClickHostName = (hostName) => {
    this.setState({
      activeKey: '3',
      activeHostName: hostName
    })
  }

  handleChangeTabs = (key) => {
    this.setState({
      activeKey: key
    })
  }

  handleClickDockerInfo = (hostName) => {
    router.push("/host/docker/list?hostName=" + hostName);
  }

  handleClickDockerImage = (hostName) => {
    router.push("/host/docker/imageManager?hostName=" + hostName);
  }

  handleClickDockerNet = (hostName) => {
    router.push("/host/docker/networkManager?hostName=" + hostName);
  }

  handleClickDockerVolume = (hostName) => {
    router.push("/host/docker/volumeManager?hostName=" + hostName);
  }

  handleDeleteHost = (hostName) => {
    const { dispatch } = this.props;
    if (confirm('确定要删除主机吗？')) {
      dispatch({
        type: 'host/deleteHostHandle',
        payload: { hostName }
      })
    }
  }


  render() {
    const { host } = this.props;
    let { hostData } = host;
    const { activeKey, activeHostName } = this.state;

    const detailInfo = (
      <div className={styles.peer}>{host ? host.title : ''}</div>
    );

    hostData && hostData.map((item, index) => {
      return item.key = index
    })

    const hostColumns = [{
      dataIndex: 'hostName',
      title: '主机名',
      render: (text) => {
        return (
          <a onClick={this.handleClickHostName.bind(this, text)}>{text}</a>
        )
      }
    }, {
      dataIndex: 'ip',
      title: 'IP地址'
    }, {
      dataIndex: 'port',
      title: 'docker端口'
    }, {
      dataIndex: 'tlsEnable',
      title: '是否启用tls',
      render: (text) => {
        if (text === true) {
          return '是'
        } else {
          return '否'
        }
      }
    }, {
      dataIndex: 'certPath',
      title: '证书存放路径'
    }, {
      dataIndex: 'dockerInfo',
      title: '容器管理',
      render: (text, item) => (<a onClick={this.handleClickDockerInfo.bind(this, item.hostName)}>容器管理</a>)
    }, {
      dataIndex: 'dokerImage',
      title: '镜像管理',
      render: (text, item) => (<a onClick={this.handleClickDockerImage.bind(this, item.hostName)}>镜像管理</a>)
    }, {
      dataIndex: 'dockerNet',
      title: '网络管理',
      render: (text, item) => (<a onClick={this.handleClickDockerNet.bind(this, item.hostName)}>网络管理</a>)
    }, {
      dataIndex: 'dockerVolume',
      title: '卷管理',
      render: (text, item) => (<a onClick={this.handleClickDockerVolume.bind(this, item.hostName)}>卷管理</a>)
    }, {
      dataIndex: 'delete',
      title: '删除主机',
      render: (text, item) => (<a onClick={this.handleDeleteHost.bind(this, item.hostName)}>删除</a>)
    }]


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
      <PageHeaderLayout detailInfo={detailInfo} logo={org}>
        <Tabs onChange={this.handleChangeTabs} activeKey={activeKey} className={styles.tabs}>
          <TabPane
            className={styles.tabChildren}
            tab={
              <span>
                <Icon type="file-text" />
                主机列表
              </span>
            }
            key="1"
          >
            <TabPaneCon children={<Table dataSource={hostData} columns={hostColumns} />} title="主机列表" />
          </TabPane>
          <TabPane
            className={styles.tabChildren}
            tab={
              <span>
                <Icon type="file-text" />
                增加主机
              </span>
            }
            key="2"
          >
            <TabPaneCon children={<WrapHostAdd updateList={this.updateList} />} title="增加主机" />

          </TabPane>
          <TabPane
            className={styles.tabChildren}
            tab={
              <span>
                <Icon type="file-text" />
                更新主机
              </span>
            }
            key="3"
          >

            <TabPaneCon children={<WrapHostUpdate hostName={activeHostName ? activeHostName : ''} />} title="更新主机" />
          </TabPane>
        </Tabs>
      </PageHeaderLayout>
    );
  }
}

const WrapHost = Form.create({})(Host);
export default WrapHost;
