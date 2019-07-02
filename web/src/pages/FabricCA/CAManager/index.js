import React from 'react';
import ReactDOM from 'react-dom';
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
  Modal,
} from 'antd';
import PageHeaderLayout from '@/components/PageHeaderWrapper';
import TabPaneCon from '@/components/TabPaneCon';
import WrapCAInfo from './CAInfo';
import WrapCreateCA from './createCA';
import WrapOperateCA from './operateCA';
import WrapUpdateServer from './updateServer';
import styles from '../style.less';

import org from '@/assets/组织.png';

const { TabPane } = Tabs;
const Option = Select.Option;
const FormItem = Form.Item;
const RadioGroup = Radio.Group;

@connect(({ CAManager, global, loading }) => {
  return {
    CAManager,
    global,
    loading: loading.effects['CAManager/handleGetCA'],
  };
})
class CAManager extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      activeKey: '1',   //展开的tab页
      update: false, // 刷新CA列表
      deleteStamp: 0,
      deleteFlag: false,  //解决bug：每次进入当前页面会重复出现message
      serverName: '',
      selectFlag: true,
    };

    this.handleChangeTabs = this.handleChangeTabs.bind(this);
  }

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'CAManager/handleGetCA'
    });
  }

  componentDidUpdate() {
    const { CAManager, dispatch } = this.props;
    const { deleteCA, getCA } = CAManager;
    const { update, deleteStamp, serverName, deleteFlag, selectFlag } = this.state;


    if (deleteFlag && deleteCA && deleteCA.deleteStamp !== deleteStamp) {
      const { data } = deleteCA;
      if (data) {
        if (data.deleted) {
          message.success(data.msg);

        } else {
          if (confirm(data.msg)) {
            dispatch({
              type: 'CAManager/handleDeleteCA',
              payload: { serverName, checked: true }
            });
          }
        }
      }
      this.setState({
        update: true,
        deleteStamp: deleteCA.deleteStamp,
        deleteFlag: false
      });
    }

    if (selectFlag && getCA && getCA.length !== 0) {
      this.setState({
        selectFlag: false,
        serverName: getCA[0].serverName
      });
    }

    if (update) {
      dispatch({
        type: 'CAManager/handleGetCA'
      })
      this.setState({
        update: false
      })
    }
  }

  updateTable = () => {
    this.setState({
      update: true
    });
  }

  handleChangeTabs = (key) => {
    this.setState({
      activeKey: key
    })
  }

  handleClickName = (name) => {
    console.log(name)
    this.setState({
      serverName: name,
      activeKey: '2'
    })
  }

  handleDeleteServer = (serverName) => {
    if (confirm("确定删除吗")) {
      const { dispatch } = this.props;
      dispatch({
        type: 'CAManager/handleDeleteCA',
        payload: { serverName, checked: false }
      })
      this.setState({
        serverName,
        deleteFlag: true
      })
    }

  }

  changeSelectCA = (e) => {
    this.setState({
      serverName: e
    })
  }


  render() {
    const { CAManager } = this.props;
    const { getCA } = CAManager;
    const { activeKey, modalTitle, isRootCA, current, serverName } = this.state;
    // const defaultServerName = getCA ? getCA[0].serverName : '';

    const detailInfo = (
      <div className={styles.peer}>CA管理</div>
    );

    function SelectCA({ getCA, changeSelectCA }) {
      return <Select
        showSearch
        style={{ width: 200 }}
        placeholder="请选择服务"
        optionFilterProp="children"
        defaultValue={serverName}
        onChange={changeSelectCA}
        filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
      >
        {
          getCA && getCA.length > 0 ? getCA.map((item) => {
            return (<Option value={item.serverName} key={item.serverName}>{item.serverName}</Option>)
          }) : ''
        }
      </Select>
    }


    getCA && getCA.map((item, index) => {
      return item.key = index
    })

    const CAColumns = [{
      dataIndex: 'serverName',
      title: '服务名称',
      render: (text) => (<a onClick={this.handleClickName.bind(this, text)}>{text}</a>)
    }, {
      dataIndex: 'hostName',
      title: '宿主机名称',
      render: (text) => (<span>{text}</span>)
    }, {
      dataIndex: 'creator',
      title: '创建者'
    }, {
      dataIndex: 'port',
      title: '容器内服务端口'
    }, {
      dataIndex: 'exposedPort',
      title: '服务端口对外映射端口',
      render: (text) => (<span>{text}</span>)
    }, {
      dataIndex: 'home',
      title: '服务器运行路径',
      render: (text) => (<span>{text}</span>)
    }, {
      dataIndex: 'tlsEnable',
      title: '是否使用TLS',
      render: (text) => {
        if (text === true) {
          return '是'
        } else {
          return '否'
        }
      }
    }, {
      // dataIndex: 'delete',
      // title: '删除',
      // render: (text, item) => (<a onClick={this.handleDeleteServer.bind(this, item.serverName)}>删除</a>)
    }];

    return (
      <PageHeaderLayout detailInfo={detailInfo} logo={org}>
        <Tabs onChange={this.handleChangeTabs} activeKey={activeKey} className={styles.tabs}>
          <TabPane
            className={styles.tabChildren}
            tab={
              <span>
                <Icon type="file-text" />
                CA列表
                </span>
            }
            key="1"
          >
            <TabPaneCon children={<Table dataSource={getCA} columns={CAColumns} />} title="CA列表" />
          </TabPane>
          <TabPane
            className={styles.tabChildren}
            tab={
              <span>
                <Icon type="file-text" />
                CA详情
                </span>
            }
            key="2"
          >
            <TabPaneCon children={<WrapCAInfo serverName={serverName} />} select={<SelectCA serverName={serverName} getCA={getCA} changeSelectCA={this.changeSelectCA} />} title="CA信息" />
            <TabPaneCon children={<WrapOperateCA serverName={serverName} />} title="CA操作" />
            {/* <TabPaneCon children={<WrapUpdateCert serverName={serverName} />}  title="更新CA证书"/> */}
          </TabPane>
          {/* <TabPane
            className={styles.tabChildren}
            tab={
              <span><Icon type="file-text" /> 更新CA服务配置</span>
            }
            key="3"
          >
            <TabPaneCon children={<WrapUpdateServer serverName={serverName} />} select={<SelectCA serverName={serverName} getCA={getCA} changeSelectCA={this.changeSelectCA} />} title="CA服务配置" />
          </TabPane> */}
          {/* <TabPane
            className={styles.tabChildren}
            tab={
              <span>
                <Icon type="file-text" />
                创建CA容器
                </span>
            }
            key="4"
          >
            {getCA ? <TabPaneCon children={<WrapCreateCA updateTable={this.updateTable} />} title="创建CA容器" /> : ''}

          </TabPane> */}
        </Tabs>
      </PageHeaderLayout>
    );
  }
}

const WrapCAManager = Form.create({})(CAManager);
export default WrapCAManager;
