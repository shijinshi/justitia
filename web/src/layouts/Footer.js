import React, { Fragment } from 'react';
import { Layout, Icon } from 'antd';
import GlobalFooter from '@/components/GlobalFooter';

const { Footer } = Layout;
const FooterView = () => (
  <Footer style={{ padding: 0 }}>
    <GlobalFooter
              copyright={
                <Fragment>
                  Copyright <Icon type="copyright" /> 2018 试金石信用
                </Fragment>
              }
            />
  </Footer>
);
export default FooterView;
