import React from 'react';
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
  Upload,
  Tooltip,
  message,
} from 'antd';

const FormItem = Form.Item;
const RadioGroup = Radio.Group;
@connect(({ dockerImage, loading }) => {
  return {
    dockerImage,
    loading: loading.effects['dockerImage/handleGetImageDetail']
  };
})
class ImageDetail extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      nowId:'',
      shouldGetDetail: false,
    };

    this.handleClickDelete = this.handleClickDelete.bind(this);
  }

  componentDidMount() {
    const { imageId, dispatch, hostName } = this.props;
    if(imageId && hostName){
        dispatch({
            type: 'dockerImage/handleGetImageDetail',
            payload: {
              ImageId:imageId,
              hostName
            }
          })
          this.setState({
            nowId: imageId
          })
    }else if(hostName){
      dispatch({
        type: 'dockerImage/handleGetDockerImage',
        payload: {hostName}
      });
      this.setState({
        shouldGetDetail: true
      })
    }
    
  }


  componentDidUpdate() {
    const { dockerImage, imageId, dispatch, hostName } = this.props;
    const { getImageList } = dockerImage;
    const { nowId, shouldGetDetail } = this.state;
    if(imageId && nowId !== imageId){
      dispatch({
        type: 'dockerImage/handleGetImageDetail',
        payload: {
          ImageId:imageId,
          hostName
        }
      })
      this.setState({
        nowId: imageId
      })
    }

    if(getImageList && getImageList.length && shouldGetDetail){
      dispatch({
        type: 'dockerImage/handleGetImageDetail',
        payload: {
          ImageId:getImageList[0].Id.slice(7),
          hostName
        }
      })
      this.setState({
        shouldGetDetail: false,
        nowImageName: getImageList[0].RepoTags[0].slice(0,getImageList[0].RepoTags[0].indexOf(':'))
      })
    }
    
  }

  handleClickDelete = () => {
    const { imageId } = this.state;
    const { dispatch, hostName } = this.props;
    dispatch({
        type: 'dockerImage/handleDeleteImage',
        payload: {
          ImageId:imageId,
          hostName
        }
      })
  }


  render() {
    const { dockerImage, imageName } = this.props;
    const { imageDetail } = dockerImage;
    const { nowImageName } = this.state;
    
    let dataInfo = [];
    if(imageDetail && imageName){
        dataInfo = [{imageDetail,key:0,imageName}]
    }else if(nowImageName){
        dataInfo = [{imageDetail,key:0,imageName:nowImageName}]
    }
    const columns = [{
        dataIndex: 'imageName',
        title: '镜像名称'
    },{
      dataIndex: 'imageDetail',
      title: '镜像详情',
      width:'70%',
      render: (text)=>(<p style={{wordBreak:"break-all"}}>{JSON.stringify(text)}</p>)
    },{
      dataIndex:'delete',
      width:'10%',
      title:'删除镜像',
      render: ()=>(<a onClick={this.handleClickDelete}>删除镜像</a>)
    }]

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
        <Table dataSource={dataInfo} columns={columns} />
        
    );
  }
}

const WrapImageDetail = Form.create({})(ImageDetail);
export default WrapImageDetail;
