import React from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'dva';
import { Button, Steps, } from 'antd';
import WrapOrganization from './organization';
import WrapHostCA from './initCAConfig';
import WrapCreateCA from '../FabricCA/CAManager/createCA';

import styles from './index.less';

const Step = Steps.Step;

@connect(({ network, initConfig }) => {
  return {
    network,
    initConfig,
  };
})
class WrapGuide extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      isRootCA: false,
    };
  }

  next = () => {
    const { dispatch, initConfig } = this.props;
    const { step } = initConfig.getInitConfig;
    dispatch({
      type: 'initConfig/handleNextStep',
      payload: step
    });
  }

  prev = () => {
    const { dispatch, initConfig } = this.props;
    const { step } = initConfig.getInitConfig;
    dispatch({
      type: 'initConfig/handlePrevStep',
      payload: step
    });
  }


  render() {
    const { isRootCA } = this.state;
    const { network, initConfig, complete } = this.props;
    const { step } = initConfig.getInitConfig;

    const steps = [{
      title: '配置组织',
      content: <WrapOrganization />,
    }, {
      title: '配置CA主机',
      content: <WrapHostCA />,
    }, {
      title: '配置CA服务',
      content: <WrapCreateCA complete={complete} isRootCA={isRootCA} />,
    }];

    return (
      <React.Fragment>
        <Steps current={step}>
          {steps.map(item => <Step key={item.title} title={item.title} />)}
        </Steps>
        <div className={styles.stepsContent}>{steps[step].content}</div>
        <div className="steps-action">
          {
            step > 0
            && (
              <Button style={{ marginLeft: 8 }} onClick={() => this.prev()}>
                上一步
              </Button>
            )
          }
        </div>
      </React.Fragment>
    );
  }
}

export default WrapGuide;
