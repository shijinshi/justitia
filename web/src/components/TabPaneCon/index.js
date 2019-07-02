import React from 'react';
import styles from './index.less';


export default class TabPaneCon extends React.Component{
    render(){
        const { children, title, select } = this.props;
        return (
            <div className={styles.blockListTable}>
                <div className={styles.blockTitle}>{title}</div>
                <div className={styles.select}>{select}</div>
                
                {children}
            </div>
        )
    }
} 