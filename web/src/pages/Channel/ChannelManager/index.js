import React from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'dva';
import { Icon, Tabs, Table, Button, Form, Input, List } from 'antd';
import PageHeaderLayout from '@/components/PageHeaderWrapper';
import TabPaneCon from '@/components/TabPaneCon';
import { downloadFile } from '@/utils/utils';
import OperationChannel from './operationChannel';
import AddOrgToChannel from './addOrgToChannel';
import styles from '../style.less';

import org from '@/assets/组织.png';

const { TabPane } = Tabs;
const FormItem = Form.Item;
const ListItem = List.Item;
const Meta = List.Item.Meta;

const host = process.env.NODE_ENV === "production" ? '/api' : `http://${window.hostIp}`;

@connect(({ ChannelManager, network }) => {
  return {
    ChannelManager,
    network
  };
})
class ChannelManager extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      activeKey: '1',
      title: '',
      type: '',
      params: '',
      isShow: false,
    };
    this.downloadEle = React.createRef();
  }

  componentDidMount() {
    const { dispatch } = this.props;
    // 获取通道信息列表
    dispatch({
      type: 'ChannelManager/handleGetChannelList'
    });
    dispatch({
      type: 'network/getConfigPeer',
    });
  }

  componentDidUpdate(prevProps, prevState) {
    const { isClose } = this.props.ChannelManager;
    const { isShow } = this.state;
    if (isShow && prevState.isShow && isClose) {
      this.setState({
        isShow: false
      });
    }
  }
  // 切换tabs
  handleChangeTabs = (key) => {
    this.setState({
      activeKey: key === "1" ? "1" : "2"
    });
  }
  // 打开弹窗
  handleShowModal = (type, title, params) => {
    this.setState({
      isShow: true,
      title,
      type,
      params,
    });
  }
  // 关闭弹窗
  handleCloseModal = () => {
    this.setState({
      isShow: false,
    });
  }
  // 添加通道
  onAddChannel = (e) => {
    const { dispatch } = this.props;
    const value = e.target.value;
    if (value !== "") {
      dispatch({
        type: 'ChannelManager/handleAddChannel',
        payload: { channelName: value }
      });
    }
  }
  // 下载通道组织信息
  handleDownLoadConfig = () => {
    const params = {
      ele: this.downloadEle.current,
      fileName: '通道组织配置文件.json',
      url: `${host}/channel/organization/config`,
      type: 'get'
    }
    downloadFile(params);
  }
  // 创建通道
  handleCreateChannel = (e) => {
    e.preventDefault();
    const { form, dispatch } = this.props;
    form.validateFields((err, values) => {
      if (err) return false;
      dispatch({
        type: 'ChannelManager/handleAddChannel',
        payload: values
      });
    });
  }

  render() {
    const DetailInfo = (
      <div className={styles.peer}>通道信息管理</div>
    );
    // 下载文件按钮
    const DownLoadBtn = (
      <Button className={styles.downloadBtn} type="primary" onClick={this.handleDownLoadConfig}>
        下载通道组织配置文件
      </Button>
    );
    // table格式
    const columns = [
      { title: '通道名称', key: 'channelName', dataIndex: 'channelName', width: '15%' },
      {
        title: 'peer节点', key: 'peers', dataIndex: 'peers', render: (peersList) => (
          <span>
            {peersList.map((ele, index) => (
              <span key={index}>{ele}，</span>
            ))}
          </span>
        )
      },
      {
        title: '组织MSPID', key: 'orgs', dataIndex: 'orgs', render: (orgsList) => (
          <span>
            {orgsList.map((ele, index) => (
              <span key={index}>{ele.msp}，</span>
            ))}
          </span>
        )
      },
      {
        title: '操作', key: 'action', width: '100px', render: (params) => (
          <p className={styles.tableAction}>
            {/* <a onClick={() => this.handleShowModal('peer', '添加节点', params.channelName)}>添加节点</a>
            <a onClick={() => this.handleShowModal('addOrg', '添加组织', params.channelName)}>添加组织</a> */}
            <a onClick={() => this.handleShowModal('delOrg', '删除组织', params.channelName)}>删除组织</a>
          </p>
        )
      },
    ];
    // Form表单layout
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
    const { activeKey, isShow, title, type, params } = this.state;
    const { form, ChannelManager, network } = this.props;
    const { loading, createChannel, getChannelList } = ChannelManager;
    const { isFetching } = createChannel;
    const { peerConfig } = network;
    const { getFieldDecorator } = form;
    // 将获取的channel通道数据对象转换成数组格式
    let dataSource = [];
    let index = 0;
    for (const key in getChannelList) {
      if (getChannelList.hasOwnProperty(key)) {
        const element = getChannelList[key];
        element.channelName = key;
        element.key = index++;
        dataSource.push(element);
      }
    }

    return (
      <PageHeaderLayout detailInfo={DetailInfo} logo={org}>
        <Tabs onChange={(key) => this.handleChangeTabs(key)} activeKey={activeKey} className={styles.tabs}>
          <TabPane
            className={styles.tabChildren}
            tab={<span><Icon type="file-text" />通道列表</span>}
            key="1"
          >
            <TabPaneCon title="通道列表" select={DownLoadBtn}>
              <Table bordered loading={loading} columns={columns} dataSource={dataSource}
                expandedRowRender={record =>
                  <List
                    size="small"
                    header={<div>通道内组织详情</div>}
                    dataSource={record.orgs}
                    renderItem={item => (
                      <ListItem>
                        <Meta
                          title={item.msp}
                          description={`锚节点：${item.anchorPeers ? item.anchorPeers : '""'}`}
                        />
                      </ListItem>
                    )}
                  />
                }
              />
              <OperationChannel title={title} type={type} params={params} isShow={isShow} onCloseModal={() => this.handleCloseModal()} />
              <div ref={this.downloadEle}></div>
            </TabPaneCon>
            <TabPaneCon title="创建通道">
              <Form onSubmit={this.handleCreateChannel}>
                <FormItem {...formItemLayout} hasFeedback={true} label={"通道名称"}>
                  {getFieldDecorator('channelName', {
                    rules: [
                      {
                        required: true,
                        message: '请填写通道名称！',
                      },
                      {
                        pattern: /^[0-9a-z_]{1,64}$/g,
                        message: '请输入不超过64位的数字、小写字母、下划线的组合!',
                      },
                    ],
                  })(
                    <Input placeholder="请填写新增通道名称！" />
                  )}
                </FormItem>
                <FormItem {...formItemLayout} hasFeedback={true} label={"联盟名称"}>
                  {getFieldDecorator('consortiumName', {
                    rules: [
                      {
                        required: true,
                        message: '请填写联盟名称！',
                      }
                    ],
                  })(
                    <Input placeholder="请填写联盟名称！" />
                  )}
                </FormItem>
                <FormItem wrapperCol={{ span: 8, offset: 8 }}>
                  <Button type="primary" block htmlType="submit" loading={isFetching}>确认</Button>
                </FormItem>
              </Form>
            </TabPaneCon>
          </TabPane>
          <TabPane
            className={styles.tabChildren}
            tab={<span><Icon type="file-text" />添加组织</span>}
            key="2"
          >
            <TabPaneCon title="添加组织" children={<AddOrgToChannel dataChannel={dataSource} />} />
          </TabPane>
        </Tabs>
      </PageHeaderLayout>
    );
  }
}

const wrapperChannelManger = Form.create({})(ChannelManager);
export default wrapperChannelManger;
