import { message } from 'antd';
import {
  getConfigPeer,
  getConfigOrderer,
  managePeer,
  manageOrderer,
  ordererDelete,
  peerDelete,
  generatePeerCert,
  peerDeploy,
  ordererDeploy,
  peerOrgGet,
  createOrderer,
  getCA
} from '@/services/api';

export default {
  namespace: 'network',

  state: {
    peerConfig: [],
    ordererConfig: [],
    loading: false,
    isFetching: true,
  },

  effects: {
    *getConfigPeer(_, { call, put }) {
      yield put({ type: 'fetch_getData' });
      const response = yield call(getConfigPeer);
      try {
        const { meta } = response;
        if (meta.success) {
          yield put({
            type: 'getData',
            payload: { peerConfig: response.data, },
          });
        } else {
          yield put({ type: 'err_getData' });
        }
      } catch (error) {
        yield put({ type: 'err_getData' });
      }
    },
    *getConfigOrderer(_, { call, put }) {
      yield put({ type: 'fetch_getData' });
      const response = yield call(getConfigOrderer);
      try {
        const { meta } = response;
        if (meta.success) {
          yield put({
            type: 'getData',
            payload: { ordererConfig: response.data, },
          });
        } else {
          yield put({ type: 'err_getData' });
        }
      } catch (error) {
        yield put({ type: 'err_getData' });
      }
    },
    *handleManagePeer({ payload }, { call, put }) {
      yield put({ type: 'fetch_manage', });
      const response = yield call(managePeer, payload);
      const { meta } = response;
      if (meta.success) {
        message.success(meta.message);
        yield put({ type: 'manage' });
      } else {
        yield put({ type: 'err_manage' });
      }
    },
    *handleManageOrderer({ payload }, { call, put }) {
      yield put({ type: 'fetch_manage', });
      const response = yield call(manageOrderer, payload);
      const { meta } = response;
      if (meta.success) {
        message.success(meta.message);
        yield put({ type: 'manage', });
      } else {
        yield put({ type: 'err_manage' });
      }
    },
    *ordererDelete({ payload }, { call, put }) {
      yield put({ type: 'fetch_save' });
      const response = yield call(ordererDelete, payload);
      const { meta } = response;
      if (meta.success) {
        message.success(meta.message);
        yield put({ type: 'save', });
        yield put({ type: 'getConfigOrderer' });
      } else {
        yield put({ type: 'err_save' });
      }
    },
    *peerDelete({ payload }, { call, put }) {
      yield put({ type: 'fetch_save' });
      const response = yield call(peerDelete, payload);
      const { meta } = response;
      if (meta.success) {
        message.success(meta.message);
        yield put({ type: 'save', });
        yield put({ type: 'getConfigPeer' });
      } else {
        yield put({ type: 'err_save' });
      }
    },
    *peerDeploy({ payload }, { call, put }) {
      yield put({ type: 'fetch_save' });
      const response = yield call(peerDeploy, payload);
      const { meta } = response;
      meta.stamp = new Date().getTime();
      if (meta.success) {
        message.success(meta.message);
        yield put({ type: 'save' });
        yield put({ type: 'getConfigPeer' });
      } else {
        yield put({ type: 'err_save' });
      }
    },
    *handleOrdererDeploy({ payload }, { call, put }) {
      yield put({ type: 'fetch_save' });
      const response = yield call(ordererDeploy, payload);
      const { meta } = response;
      meta.stamp = new Date().getTime();
      if (meta.success) {
        message.success(meta.message);
        yield put({ type: 'save' });
        yield put({ type: 'getConfigOrderer' });
      } else {
        yield put({ type: 'err_save' });
      }
    },
    *handleCreateOrderer({ payload }, { call, put }) {
      const response = yield call(createOrderer, payload);
      try {
        const { meta } = response;
        meta.stamp = new Date().getTime();
        yield put({
          type: 'save',
          payload: {
            createOrderer: meta,
          },
        });
      } catch (error) {
        console.log(error)
      }
    },
    *handleGetCA({ payload }, { call, put }) {
      const response = yield call(getCA, payload);
      try {
        const { data } = response;
        data.stamp = new Date().getTime();
        yield put({
          type: 'save',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }
    },
  },
  reducers: {
    getData(state, { payload }) {
      return {
        ...state,
        ...payload,
        isFetching: false,
      }
    },
    fetch_getData(state) {
      return {
        ...state,
        isFetching: true,
      }
    },
    err_getData(state) {
      return {
        ...state,
        isFetching: false,
      }
    },
    manage(state, { payload }) {
      return {
        ...state,
        isFetching: false,
      }
    },
    fetch_manage(state) {
      return {
        ...state,
        isFetching: true,
      }
    },
    err_manage(state) {
      return {
        ...state,
        isFetching: false,
      }
    },
    save(state) {
      return {
        ...state,
        loading: false,
      };
    },
    fetch_save(state) {
      return {
        ...state,
        loading: true,
      };
    },
    err_save(state) {
      return {
        ...state,
        loading: false,
      };
    },
  },
};
