import React, { Component } from 'react';
import { connect } from 'dva';
import { Modal, Input } from 'antd';
import styles from '../style.less';

const { TextArea } = Input;

@connect(({ ChannelTask }) => {
  return {
    ChannelTask,
  };
})
class RejectModal extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isError: false,
      text: "",
    };
  }
  // 文本输入框输入
  handleChange = (e) => {
    this.setState({
      text: e.target.value,
      isError: false,
    });
  }
  // 关闭处理任务弹窗
  handleCloseModal = () => {
    this.setState({
      isError: false,
      text: '',
    });
    this.props.onCloseModal();
  }
  // 拒绝添加组织
  handleRejectTask = () => {
    const { text } = this.state;
    const { dispatch, taskId } = this.props;
    if (text.length !== 0) {
      dispatch({
        type: 'ChannelTask/handelDealTask',
        payload: {
          reason: text,
          reject: true,
          taskId
        }
      });
    } else {
      this.setState({
        isError: true,
      });
    }
  }

  render() {
    const { isError, text } = this.state;
    const { ChannelTask, isShow } = this.props;
    const { isFetching, taskList } = ChannelTask;

    return (
      <Modal title="拒绝该任务请求" visible={isShow} confirmLoading={isFetching} onCancel={() => this.handleCloseModal()} onOk={() => this.handleRejectTask()}>
        <div className={styles.rejectModal}>
          <div className={styles.content}>
            <span>拒绝原因：</span>
            <TextArea style={{ height: 100 }} value={text} onChange={this.handleChange} />
          </div>
          {isError ? <p>请填写拒绝原因！</p> : null}
        </div>
      </Modal>
    )
  }
}

export default RejectModal;
