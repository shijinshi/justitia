import { routerRedux } from 'dva/router';
import { stringify } from 'qs';
import { message } from 'antd';
import { handleLogin, getFakeCaptcha, logoutUser } from '@/services/api';
import { setAuthority } from '@/utils/authority';
import { getPageQuery } from '@/utils/utils';
import { reloadAuthorized } from '@/utils/Authorized';

export default {
  namespace: 'login',

  state: {
    status: undefined,
  },

  effects: {
    *login({ payload }, { call, put }) {
      const response = yield call(handleLogin, payload);
      yield put({
        type: 'changeLoginStatus',
        payload: response
      });
      // Login successfully
      const { data, meta } = response;
      if (meta.success) {
        localStorage.setItem('token', data.token);
        setAuthority(data.user.identity ? data.user.identity : 'guest');
        reloadAuthorized();
        localStorage.setItem('userId', payload.userId);
        const urlParams = new URL(window.location.href);
        const params = getPageQuery();
        let { redirect } = params;
        if (redirect) {
          const redirectUrlParams = new URL(redirect);
          if (redirectUrlParams.origin === urlParams.origin) {
            redirect = redirect.substr(urlParams.origin.length);
            if (redirect.match(/^\/.*#/)) {
              redirect = redirect.substr(redirect.indexOf('#') + 1);
            }
          } else {
            window.location.href = redirect;
            return;
          }
        }
        yield put(routerRedux.replace(redirect || '/'));
      }
    },

    *getCaptcha({ payload }, { call }) {
      yield call(getFakeCaptcha, payload);
    },

    *logout({ payload }, { call, put }) {
      if (payload === undefined) {
        yield put({
          type: 'changeLoginStatus',
          payload: {
            status: false,
            currentAuthority: 'guest',
          },
        });
      } else {
        const response = yield call(logoutUser, payload);
        const { meta } = response;
        try {
          if (meta.success) {
            yield put({
              type: 'logoutCallback',
              payload: response
            });
          }
        } catch (error) {
          message.error(error)
        }

      }
      setAuthority('guest')
      reloadAuthorized();
      localStorage.removeItem('token');
      yield put(
        routerRedux.push('/user/login')
      );
    },
  },

  reducers: {
    logoutCallback(state, { payload }) {
      return {
        ...state,
        logout: payload
      }
    },
    changeLoginStatus(state, { payload }) {
      return {
        ...state,
        status: payload.meta ? payload.meta.message : status,
        type: 'account',
      };
    },
  },
};
