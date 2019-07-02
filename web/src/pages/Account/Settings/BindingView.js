import React, { Component, Fragment } from 'react';
import { connect } from 'dva';
import { formatMessage, FormattedMessage } from 'umi/locale';
import { Icon, List, Input, Button, Row, Col } from 'antd';

@connect(({ user })=>({
  user
}))
class BindingView extends Component {
  constructor(props){
    super(props);
  }

  componentDidUpdate(){
    
  }
  
  getRegisterCode = () => {
    const { dispatch } = this.props;
    dispatch({
      type: 'user/registerCode',
      payload: {
        token: localStorage.getItem('token')
      }
    })
  }
  render() {
    const { user } = this.props;
    const { registerCode } = user;

    return (
      <Fragment>
        <Row gutter={8}>
          <Col span={6}>
            <Input disabled style={{cursor: 'text', background:'transparent'}} value={registerCode ? registerCode.code : ''}/>
          </Col>
          <Col span={4}>
            <Button onClick={this.getRegisterCode}>获取</Button>
          </Col>
        </Row>
        
      </Fragment>
    );
  }
}

export default BindingView;
