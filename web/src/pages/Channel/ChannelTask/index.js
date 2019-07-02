import React, { Component } from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'dva';
import { Icon, Tabs } from 'antd';
import PageHeaderLayout from '@/components/PageHeaderWrapper';
import TabPaneCon from '@/components/TabPaneCon';
import TaskList from './taskList';
import TaskDetail from './taskDetail';
import styles from '../style.less';

import org from '@/assets/组织.png';

const { TabPane } = Tabs;

@connect(({ ChannelTask }) => {
  return {
    ChannelTask,
  };
})
class ChannelTask extends Component {
  constructor(props) {
    super(props);
    this.state = {
      activeKey: '1',
      taskId: null,
    };
  }

  // 切换tabs
  handleChangeTabs = (key) => {
    this.setState({
      activeKey: key === "1" ? "1" : "2"
    });
  }
  // 跳转到任务详情
  handlePushDetail = (value) => {
    const { dispatch } = this.props;
    this.setState({
      activeKey: '2',
      taskId: value
    });
    dispatch({
      type: 'ChannelTask/handleGetTaskDetail',
      payload: { taskId: value }
    });
  }

  render() {
    const detailInfo = (<div className={styles.peer}>通道任务管理</div>);
    const { activeKey, taskId } = this.state;
    const { ChannelTask } = this.props;
    const { isLoading, taskDetail } = ChannelTask;

    return (
      <PageHeaderLayout detailInfo={detailInfo} logo={org}>
        <Tabs onChange={(key) => this.handleChangeTabs(key)} activeKey={activeKey} className={styles.tabs}>
          <TabPane
            className={styles.tabChildren}
            tab={<span><Icon type="file-text" />任务列表</span>}
            key="1"
          >
            <TabPaneCon title="任务列表">
              <TaskList onhandleClickName={(value) => this.handlePushDetail(value)} />
            </TabPaneCon>
          </TabPane>
          <TabPane
            className={styles.tabChildren}
            tab={<span><Icon type="file-text" />任务详情</span>}
            key="2"
          >
            <TabPaneCon title="任务详情">
              <TaskDetail isLoading={isLoading} taskDetail={taskDetail} />
            </TabPaneCon>
          </TabPane>
        </Tabs>
      </PageHeaderLayout>
    );
  }
}

export default ChannelTask;
