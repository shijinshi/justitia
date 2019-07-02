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
    Tooltip,
    message,
} from 'antd';
import { downloadFile } from '@/utils/utils';

const { TabPane } = Tabs;
const Option = Select.Option;
const FormItem = Form.Item;
const RadioGroup = Radio.Group;

const host = process.env.NODE_ENV === "production" ? '/api' : `http://${window.hostIp}`;

@connect(({ CAManager, loading }) => {
    return {
        CAManager,
        loading: loading.effects['CAManager/handleGetOneCA'],
    };
})
class CAInfo extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            nowServer: '',
        };
        this.clipBoard = React.createRef();
        this.ele = React.createRef();
    }

    componentDidMount() {
        const { dispatch, serverName } = this.props;
        if (serverName) {
            dispatch({
                type: 'CAManager/handleGetOneCA',
                payload: { serverName }
            })
            this.setState({
                nowServer: serverName
            })
        }

    }

    componentDidUpdate() {
        const { dispatch, serverName } = this.props;
        const { nowServer } = this.state;
        if (serverName && serverName !== nowServer) {
            dispatch({
                type: 'CAManager/handleGetOneCA',
                payload: { serverName }
            })
            this.setState({
                nowServer: serverName
            })
        }

    }

    handleClickName = (name, id) => {
        console.log(name, id)
    }

    handleClickOper = (oper) => {
        const { dispatch } = this.props;
        const { nowServer } = this.state;
        dispatch({
            type: 'CAManager/handleOperCA',
            payload: { oper, serverName: nowServer }
        })
    }

    handleCopyText = (text) => {
        const clipBoard = this.clipBoard.current;
        clipBoard.value = '';
        clipBoard.value = text;
        clipBoard.select();
        window.document.execCommand("Copy");
        window.document.execCommand("Copy");
        message.success('复制成功')
    }

    handleDownloadFile = () => {
        const ele = this.ele.current;
        const { serverName } = this.props;
        const params = {
            ele,
            fileName: `${serverName}.pem`,
            url: `${host}/ca/cert/root/${serverName}`,
            type: 'get'
        }
        downloadFile(params)
    }



    render() {
        const { CAManager, serverName } = this.props;
        const { getOneCA } = CAManager;

        let dataInfo = [];
        if (getOneCA) {
            dataInfo = [{ ...getOneCA, key: 0, serverName }]
        }

        const CAColumns = [{
            dataIndex: 'serverName',
            title: '服务名称'
        }, {
            dataIndex: 'caName',
            title: 'CA名称',
            render: (text) => (text ? text : '无')
        }, {
            dataIndex: 'version',
            title: 'CA版本'
        }, {
            dataIndex: 'cacertificateChain',
            title: 'CA链',
            width: '20%',
            render: (text) => (<span title={text}>{text.slice(0, 20) + '...'}<a onClick={this.handleCopyText.bind(this, text)}>复制文本</a></span>)
        }, {
            dataIndex: 'idemixIssuerPublicKey',
            title: 'Idemix公钥',
            width: '20%',
            render: (text) => (<span title={text}>{text.slice(0, 20) + '...'}<a onClick={this.handleCopyText.bind(this, text)}>复制文本</a></span>)
        }, {
            dataIndex: 'idemixIssuerRevocationPublicKey',
            title: 'Idemix发行人撤销私钥',
            width: '20%',
            render: (text) => (<span title={text}>{text.slice(0, 20) + '...'}<a onClick={this.handleCopyText.bind(this, text)}>复制文本</a></span>)
        }, /* {
            dataIndex: 'download',
            title: '下载CA证书',
            render: () => <a onClick={this.handleDownloadFile}>下载CA证书</a>
        } */]





        return (
            <div>
                <Table bordered dataSource={dataInfo} columns={CAColumns} />
                <input ref={this.clipBoard} style={{ position: 'absolute', zIndex: -1, top: 0, height: '1px', border: 'none' }} />
                <div ref={this.ele}></div>
            </div>

        );
    }
}

const WrapCAInfo = Form.create({})(CAInfo);
export default WrapCAInfo;
