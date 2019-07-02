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
    message,
  } from 'antd';
import LableToolTip from '../labelToolTip';
import {toFile} from '@/utils/utils'


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
            nowStamp: ''
        }
        this.downloadEle = React.createRef();
      }

    componentDidUpdate(){
        const { dispatch, CAUserManager, serverName, userId, form, shouldUpdate } = this.props;
        const { revokeCert } = CAUserManager;
        const { nowStamp } = this.state;
        const ele = this.downloadEle.current;
        if(revokeCert && revokeCert.meta && revokeCert.meta.success && revokeCert.stamp !== nowStamp){
            const { data } = revokeCert;
            const { meta } = revokeCert;
            if(data.type === 'msg'){
                if(confirm(`${data.message},确定任然删除吗？`)){
                    form.validateFields((err, values)=>{
                        if(!err){
                            dispatch({
                                type: 'CAUserManager/handleRevokeCert',
                                payload: {
                                    ...values,
                                    checked: true,
                                    serverName,
                                    userId
                                }
                            })
                        }
                    })
                }
            }else{
                const genCRL = form.getFieldValue('genCRL');
                if(genCRL){
                    const params = {
                        ele,
                        fileName:`revokeCert_${userId}_${serverName}.pem`,
                        object: data.crl
                    }
                    toFile(params)
                }else{
                    message.success('已注销')
                }
                shouldUpdate()
            }
            this.setState({
                nowStamp: revokeCert.stamp
            })
        }
        
    }

    handleSubmit = (e) => {
        e.preventDefault();
        const { form, dispatch, serverName, userId } = this.props;
        if(serverName){
            form.validateFields((err, values)=>{
                if(!err){
                    console.log(values)
                    dispatch({
                        type: 'CAUserManager/handleRevokeCert',
                        payload: {
                            ...values,
                            checked: false,
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
        return (
            <Form onSubmit={this.handleSubmit}>
                
                <FormItem {...formItemLayout} label="注销原因">
                    {getFieldDecorator('reason', {
                        initialValue:'unspecified',
                    })(<Select>
                        <Option key={'unspecified'} value={'unspecified'}>unspecified</Option>
                        <Option key={'keycompromise'} value={'keycompromise'}>keycompromise</Option>
                        <Option key={'cacompromise'} value={'cacompromise'}>cacompromise</Option>
                        <Option key={'affiliationchange'} value={'affiliationchange'}>affiliationchange</Option>
                        <Option key={'superseded'} value={'superseded'}>superseded</Option>
                        <Option key={'cessationofoperation'} value={'cessationofoperation'}>cessationofoperation</Option>
                        <Option key={'certificatehold'} value={'certificatehold'}>certificatehold</Option>
                        <Option key={'privilegewithdrawn'} value={'privilegewithdrawn'}>privilegewithdrawn</Option>
                        <Option key={'aacompromise'} value={'aacompromise'}>aacompromise</Option>
                    </Select>)}
                </FormItem>
                <FormItem {...formItemLayout} label="生成证书吊销列表">
                    {getFieldDecorator('genCRL', {
                        initialValue: true
                    })(<RadioGroup >
                        <Radio value={true}>是</Radio>
                        <Radio value={false}>否</Radio>
                    </RadioGroup>)}
                </FormItem>
                <FormItem style={{display: 'none'}} {...formItemLayout} label="证书序列号">
                    {getFieldDecorator('serial', {
                        initialValue: serial,
                        rules: [
                            {
                                requierd: true,
                                message: '请输入证书序列号！'
                            }
                        ]
                    })(<Input />)}
                </FormItem>
                <FormItem style={{display: 'none'}} {...formItemLayout} label="证书身份编号">
                    {getFieldDecorator('aki', {
                        initialValue: aki,
                        rules: [
                            {
                                requierd: true,
                                message: '请输证书身份编号！'
                            }
                        ]
                    })(<Input />)}
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