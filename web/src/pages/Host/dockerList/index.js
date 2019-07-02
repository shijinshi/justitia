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
import WrapDockerInfo from './dockerInfo';
// import SelectHost from '../selectHost';
import styles from '../style.less';

import org from '@/assets/组织.png';

const { TabPane } = Tabs;
const Option = Select.Option;
const FormItem = Form.Item;
const RadioGroup = Radio.Group;

@connect(({ host, dockerList, loading }) => {
  return {
    host,
    dockerList,
    loading: loading.effects['dockerList/handleGetDockerList'],
  };
})
class DockerList extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      containerId: '',
      activeKey: '1',   //展开的tab页
    };

    this.handleChangeTabs = this.handleChangeTabs.bind(this);
    this.handleClickDockerName = this.handleClickDockerName.bind(this);
    this.changeSelectHost = this.changeSelectHost.bind(this);
    this.changeSelectImage = this.changeSelectImage.bind(this);
  }

  componentDidMount() {
    const { dispatch, location } = this.props;
    const { query } = location;
    dispatch({
      type: 'host/getHostHandle'
    });
    if (query.hostName) {
      dispatch({
        type: 'dockerList/handleGetDockerList',
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
    const { dispatch, host, dockerList } = this.props;
    let dockerListData = dockerList.dockerList;
    const { nowHost } = host;
    const { hostName, containerId } = this.state;
    if (nowHost && nowHost !== hostName) {
      this.setState({
        hostName: nowHost
      })
      dispatch({
        type: 'dockerList/handleGetDockerList',
        payload: { hostName: nowHost }
      });
    }
    if (dockerListData && dockerListData[0].Id !== containerId) {
      this.setState({
        containerId: dockerListData[0].Id,
        // dockerName: dockerListData[0].Image
      })
    }

  }


  handleChangeTabs = (key) => {
    this.setState({
      activeKey: key
    });
  }

  handleClickDockerName = (id, name) => {
    this.setState({
      activeKey: '2',
      containerId: id,
      dockerName: name
    })
  }

  changeSelectHost = (key) => {
    const { dispatch } = this.props;
    dispatch({
      type: 'dockerList/handleGetDockerList',
      payload: { hostName: key }
    });
    dispatch({
      type: 'host/saveNowHost',
      payload: key
    })
    this.setState({
      hostName: key
    })
  }

  changeSelectImage = (key) => {
    const { dispatch, dockerList } = this.props;
    const { hostName } = this.state;
    const dockerListData = dockerList.dockerList;
    dispatch({
      type: 'dockerList/handleGetDockerInfo',
      payload: {
        hostName,
        containerId: key
      }
    });
    let dockerName = '';
    dockerListData.forEach(ele => {
      if (ele.Id === key) {
        dockerName = ele.Image
      }
    })
    this.setState({
      dockerName
    })
  }



  render() {
    const { dockerList, host } = this.props;
    const { hostData } = host;
    let dockerListData = dockerList.dockerList;
    const { activeKey, containerId, hostName, dockerName } = this.state;
    const detailInfo = (
      <div className={styles.peer}>容器管理</div>
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

    function SelectImage({ containerId, changeSelectImage, dockerListData }) {
      return <Select
        showSearch
        style={{ width: 200, paddingTop: 5 }}
        placeholder="请选择镜像"
        optionFilterProp="children"
        defaultValue={containerId}
        onChange={changeSelectImage}
        filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
      >
        {
          dockerListData && dockerListData.length > 0 ? dockerListData.map((item) => {
            return (<Option value={item.Id} key={item.Id}>{item.Image}</Option>)
          }) : ''
        }
      </Select>
    }

    dockerListData && dockerListData.map((item, index) => {
      return item.key = index
    })

    const dockerColumns = [{
      dataIndex: 'Names',
      title: '容器名称',
      render: (text, item) => {
        return (
          <a onClick={this.handleClickDockerName.bind(this, item.Id, text)}>{text}</a>
        )
      }
    }, {
      dataIndex: 'Image',
      title: '镜像名称',
    }, {
      dataIndex: 'Command',
      title: '容器启动命令'
    }, {
      dataIndex: 'Created',
      title: '创建时间',
      sorter: (a, b) => (a.Created - b.Created),
      render: (text) => (<span>{moment(text * 1000).format('YYYY-MM-DD HH:mm:ss')}</span>)
    }, {
      dataIndex: 'Status',
      title: '当前状态'
    }, {
      dataIndex: 'Ports',
      title: '对外映射端口',
      render: (text) => {
        let ports = [];
        text.forEach((item, i) => {
          ports.push(`${item.PublicPort ? item.IP + ':' + item.PublicPort + '->' : ''}${item.PrivatePort ? item.PrivatePort : ''}/${item.Type ? item.Type + '\n' : ''}`)

        })
        return (<span>{ports.join(' , ')}</span>)
      }
    }]

    return (
      <PageHeaderLayout detailInfo={detailInfo} toggleSwitch={<SelectHost changeSelectHost={this.changeSelectHost} hostData={hostData} hostName={hostName} />} logo={org}>
        <Tabs onChange={this.handleChangeTabs} activeKey={activeKey} className={styles.tabs}>
          <TabPane
            className={styles.tabChildren}
            tab={<span><Icon type="file-text" />容器列表</span>}
            key="1"
          >
            <TabPaneCon children={<Table dataSource={dockerListData} columns={dockerColumns} />} title="容器列表" />
          </TabPane>
          <TabPane
            className={styles.tabChildren}
            tab={<span><Icon type="file-text" />docker详情</span>}
            key="2"
          >
            <TabPaneCon children={<WrapDockerInfo dockerName={dockerName} hostName={hostName} containerId={containerId} />} select={<SelectImage containerId={containerId} changeSelectImage={this.changeSelectImage} dockerListData={dockerListData} />} title="docker详情" />
          </TabPane>
        </Tabs>
      </PageHeaderLayout>
    );
  }
}

const WrapDockerList = Form.create({})(DockerList);
export default WrapDockerList;
