import React from 'react';
import { connect } from 'dva';
import {
    Row,
    Col,
    Icon,
    Tabs,
    DatePicker,
    Radio,
    Button,
    Form,
    Input,
    Select,
    message,
  } from 'antd';
import LableToolTip from '../labelToolTip';
import {toFile} from '@/utils/utils';


  const FormItem = Form.Item;
  const Option = Select.Option;
  const { TextArea } = Input;
  const RadioGroup = Radio.Group;

  @connect(({ CAUserManager }) => {
    return {
        CAUserManager
    };
  })
  class CreateCrl extends React.Component {
    constructor(props) {
        super(props);
        this.state={
            nowStamp: '',
            startValue: null,
            endValue: null,
            expireBefore: null,
            expireAfter: null,
            endOpen: false,
        }
        this.downloadEle = React.createRef();
      }

    componentDidUpdate(){
        const { CAUserManager, userId, serverName } = this.props;
        const { createCrl } = CAUserManager;
        const { nowStamp } = this.state;
        if(createCrl && createCrl.crl && createCrl.stamp !== nowStamp){
            const params = {
                ele: this.downloadEle.current,
                fileName: `certCrl_${userId}_${serverName}.pem`,
                object: createCrl.crl
            };
            toFile(params);
            this.setState({
                nowStamp: createCrl.stamp
            })
        }
    }


    disabledStartDate = (startValue) => {
        const endValue = this.state.endValue;
        if (!startValue || !endValue) {
          return false;
        }
        return startValue.valueOf() > endValue.valueOf();
    }
    
    disabledEndDate = (endValue) => {
        const startValue = this.state.startValue;
        if (!endValue || !startValue) {
          return false;
        }
        return endValue.valueOf() <= startValue.valueOf();
    }

    onChange = (field, value) => {
        this.setState({
          [field]: value,
        });
    }

    onStartChange = (value) => {
        this.onChange('startValue', value);
    }
    
    onEndChange = (value) => {
        this.onChange('endValue', value);
    }

    onExpireChange = (field, value) => {
        this.setState({
          [field]: value,
        });
    }

    onStartChangeExpire = (value) => {
        this.onExpireChange('expireAfter', value);
    }
    
    onEndChangeExpire = (value) => {
        this.onExpireChange('expireBefore', value);
    }

    disabledStartDateExpire = (expireAfter) => {
        const expireBefore = this.state.expireBefore;
        if (!expireAfter || !expireBefore) {
          return false;
        }
        return expireAfter.valueOf() > expireBefore.valueOf();
    }
    
    disabledEndDateExpire = (expireBefore) => {
        const expireAfter = this.state.expireAfter;
        if (!expireBefore || !expireAfter) {
          return false;
        }
        return expireBefore.valueOf() <= expireAfter.valueOf();
    }


    handleSubmit = (e) => {
        e.preventDefault();
        const { form, dispatch, serverName, userId } = this.props;
        if(serverName){
            form.validateFields((err, values)=>{
                if(!err){
                    dispatch({
                        type: 'CAUserManager/handleCreateCrl',
                        payload:{
                            crlInfo: values,
                            serverName,
                            userId
                        }
                    })
                }
            })
        }else{
            message.error('请先选择CA服务！')
        }
    }


    render(){
        const { serial, aki, form, userId } = this.props;
        const { getFieldDecorator } = form;
        const { endValue, startValue, expireAfter, expireBefore} = this.state;
        const formItemLayout = {
            labelCol: {
              xs: { span: 24 },
              sm: { span: 10 },
            },
            wrapperCol: {
              xs: { span: 24 },
              sm: { span: 10 },
            },
          };
        return (
            <Form onSubmit={this.handleSubmit}>
                <FormItem {...formItemLayout} label="在此时间之后被撤销的证书">
                    {getFieldDecorator('revokedAfter', {
                        initialValue: startValue
                    })(<DatePicker 
                        disabledDate={this.disabledStartDate}
                        placeholder="在此之后"
                        onChange={this.onStartChange}
                    />)}
                </FormItem>
                <FormItem {...formItemLayout} label="在此时间之前被撤销的证书">
                    {getFieldDecorator('revokedBefore', {
                        initialValue: endValue
                    })(<DatePicker 
                        disabledDate={this.disabledEndDate}
                        placeholder="在此之前"
                        onChange={this.onEndChange}
                    />)}
                </FormItem>
                
                

                <FormItem {...formItemLayout} label="有效期在此之后的证书">
                    {getFieldDecorator('expireAfter', {
                        initialValue: expireAfter
                    })(<DatePicker 
                        disabledDate={this.disabledStartDateExpire}
                        placeholder="在此之后"
                        onChange={this.onStartChangeExpire}
                    />)}
                </FormItem>
                <FormItem {...formItemLayout} label="有效期在此之前的证书">
                    {getFieldDecorator('expireBefore', {
                        initialValue: expireBefore
                    })(<DatePicker 
                        disabledDate={this.disabledEndDateExpire}
                        placeholder="在此之前"
                        onChange={this.onEndChangeExpire}
                    />)}
                </FormItem>
                
                <FormItem wrapperCol={{ span: 8, offset: 8 }}>
                    <Button type="primary" block htmlType="submit">确定</Button>
                </FormItem>
                <div ref={this.downloadEle}></div>
            </Form>
        )
    }
  }
  const WrapCreateCrl = Form.create({})(CreateCrl);
  export default WrapCreateCrl;