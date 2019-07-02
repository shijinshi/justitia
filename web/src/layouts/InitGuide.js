import React, { Suspense } from 'react';
import { Modal } from 'antd';
import WrapGuide from '../pages/Guide/index';
import { connect } from 'dva';
import router from 'umi/router';


@connect(({ initConfig }) => ({
  initConfig
}))
export default class InitGuide extends React.Component {

  componentDidMount() {
    const { dispatch } = this.props;
    dispatch({
      type: 'initConfig/handleGetInitConfig'
    });
  }

  complete = () => {
    const { location } = this.props;
    window.location.reload(location.pathname)
  }

  render() {
    const { initConfig } = this.props;
    const { step, complete } = initConfig.getInitConfig;

    return <div>
      <Modal
        title={"初始化环境配置"}
        visible={!complete}
        width='60%'
        footer={null}
        closable={false}
      >
        <WrapGuide complete={this.complete} />
      </Modal>
    </div>
  }

}