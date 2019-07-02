import React from 'react';
import { connect } from 'dva';
import {
  Table
} from 'antd';


@connect(({ CAManager }) => {
  return {
    CAManager
  };
})
class OperateCA extends React.Component {
  constructor(props) {
    super(props);
    this.state = {

    };
  }


  handleClickOper = (oper, text) => {
    const { dispatch, serverName } = this.props;
    if (typeof text === 'string' && confirm(`确定要${text}吗？`)) {
      dispatch({
        type: 'CAManager/handleOperCA',
        payload: { oper, serverName }
      });
    } else if (typeof text === 'object') {
      dispatch({
        type: 'CAManager/handleOperCA',
        payload: { oper, serverName }
      });
    }
  }

  render() {
    const { serverName } = this.props;

    let dataInfo = [];
    if (serverName) {
      dataInfo = [{ key: 0, serverName }];
    }

    const CAColumns = [{
      dataIndex: 'serverName',
      title: '服务名称'
    }, {
      dataIndex: 'start',
      title: '启动',
      render: (text) => (<a onClick={this.handleClickOper.bind(this, 'start')}>启动</a>)
    }, {
      dataIndex: 'restart',
      title: '重启',
      render: (text) => (<a onClick={this.handleClickOper.bind(this, 'restart', '重启')}>重启</a>)
    }, {
      dataIndex: 'pause',
      title: '暂停',
      render: (text) => (<a onClick={this.handleClickOper.bind(this, 'pause', '暂停')}>暂停</a>)
    }, {
      dataIndex: 'unpause',
      title: '继续运行',
      render: (text) => (<a onClick={this.handleClickOper.bind(this, 'unpause')}>继续运行</a>)
    }]





    return (
      <div>
        <Table dataSource={dataInfo} bordered columns={CAColumns} />
      </div>

    );
  }
}

const WrapOperateCA = OperateCA;
export default WrapOperateCA;
