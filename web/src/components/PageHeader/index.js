import React, { PureComponent, createElement } from 'react';
import PropTypes from 'prop-types';
import pathToRegexp from 'path-to-regexp';
import { Breadcrumb, Tabs } from 'antd';
import classNames from 'classnames';
import styles from './index.less';
import { urlToList } from '../_utils/pathTools';

const { TabPane } = Tabs;
export default class PageHeader extends PureComponent {
  state = {
    breadcrumb: null,
  };

  componentDidMount() {}

  componentDidUpdate(preProps) {}

  render() {
    const { className, detailInfo, toggleSwitch, logo, leftContent, rightContent } = this.props;

    const clsString = classNames(styles.pageHeader, className);

    return (
      <div className={clsString}>
        <div className={styles.leftCon}>
          <div className={styles.logoWrap}>
            <img src={logo} />
          </div>
          {detailInfo}
          {leftContent}
        </div>
        <div className={styles.rightCon}>
          {toggleSwitch}
          {rightContent}
        </div>
      </div>
    );
  }
}
