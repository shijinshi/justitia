import { router } from 'umi/router';
import { getOrganiztion, setORganiztion, getInitConfig } from '@/services/api';

export default {
  namespace: 'global',

  state: {
    collapsed: false,
    notices: [],
    getOrganiztion: {
      orgType: null
    }
  },

  effects: {
    *handleGetOrganiztion(_, { put, call }) {
      const res = yield call(getOrganiztion);
      try {
        res.stamp = new Date().getTime();
        yield put({
          type: 'getOrganiztion',
          payload: res.data,
        });
      } catch (error) {
        console.log(error)
      }
    },
  },

  reducers: {
    getOrganiztion(state, { payload }) {
      return {
        ...state,
        getOrganiztion: payload
      }
    },
  },

  subscriptions: {
    setup({ history }) {
      // Subscribe history(url) change, trigger `load` action if pathname is `/`
      return history.listen(({ pathname, search }) => {
        if (typeof window.ga !== 'undefined') {
          window.ga('send', 'pageview', pathname + search);
        }
      });
    },
  },
};
