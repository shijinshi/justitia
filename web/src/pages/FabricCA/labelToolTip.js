import { Tooltip, Icon } from 'antd';

export default function LableToolTip({name, text}){
    return <span>
                {name}
                &nbsp;
                <Tooltip title={text}>
                    <Icon type="question-circle-o" />
                </Tooltip>
            </span>
}