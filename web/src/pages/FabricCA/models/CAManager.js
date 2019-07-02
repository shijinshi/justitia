import { message } from 'antd';
import {
  addCA,
  getCA,
  getOneCA,
  deleteCA,
  operCA,
  getConfigCA,
  setConfigCA,
  getConfigTpl,
  updateCert,
  getHostData,
  createServer
} from '@/services/api';


export default {
  namespace: 'CAManager',

  state: {
    token: localStorage.getItem('token'),
    isFetching: false,
    getConfigCA: {
      debug: false,
      certExpiry: '0h',
      crlExpiry: '0h'
    },
    setConfigCA: {
      isFetching: false
    }
  },

  effects: {
    *getHostHandle(_, { call, put }) {
      const response = yield call(getHostData);
      try {
        const { data } = response;
        yield put({
          type: 'hostData',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }
    },
    *handleAddCA({ payload }, { call, put }) {
      yield put({ type: 'fetch_addCA' });
      const response = yield call(addCA, payload);
      if (response.meta.success) {
        response.addStamp = new Date().getTime();
        yield put({
          type: 'addCA',
          payload: response
        })
      } else {
        yield put({ type: 'err_addCA' })
      }

    },
    *handleGetCA({ payload }, { call, put }) {
      const response = yield call(getCA, payload);
      try {
        const { data } = response;
        yield put({
          type: 'getCA',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }
    },
    *handleGetOneCA({ payload }, { call, put }) {
      const response = yield call(getOneCA, payload);
      try {
        const { data } = response;
        yield put({
          type: 'getOneCA',
          payload: data
        })
      } catch (error) {
        console.log(error);
      }
    },
    *handleDeleteCA({ payload }, { call, put }) {
      const response = yield call(deleteCA, payload);
      try {
        response.deleteStamp = new Date().getTime();
        yield put({
          type: 'deleteCA',
          payload: response
        })
      } catch (error) {
        console.log(error)
      }
    },
    *handleOperCA({ payload }, { call, put }) {
      const res = yield call(operCA, payload);
      try {
        if (res.meta.success) {
          const { data } = res;
          yield put({
            type: 'operCA',
            payload: data
          });
          message.success(res.meta.message);
        }
      } catch (error) {
        console.log(error)
      }
    },
    *handleGetConfigCA({ payload }, { call, put }) {
      const response = yield call(getConfigCA, payload);
      try {
        const { data } = response;
        yield put({
          type: 'getConfigCA',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }
    },
    *handleSetConfigCA({ payload }, { call, put }) {
      yield put({ type: 'fetch_setConfigCA' });
      const res = yield call(setConfigCA, payload);
      if (res.meta.success) {
        yield put({ type: 'setConfigCA' });
        message.success(res.meta.message);
      } else {
        yield put({ type: 'err_setConfigCA' });
      }
    },
    *handleGetConfigTpl(_, { call, put }) {
      const res = yield call(getConfigTpl);
      try {
        const { data } = res;
        yield put({
          type: 'getConfigTpl',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }
    },
    *handleUpdateCert({ payload }, { call, put }) {
      const res = yield call(updateCert, payload);
      try {
        const { meta } = res;
        yield put({
          type: 'updateCert',
          payload: meta
        })
      } catch (error) {
        console.log(error)
      }
    },
    *handleCreateServer({ payload }, { call, put }) {
      const res = yield call(createServer, payload);
      try {
        const { meta } = res;
        meta.stamp = new Date().getTime();
        yield put({
          type: 'createServer',
          payload: meta
        })
      } catch (error) {
        console.log(error)
      }
    }
  },

  reducers: {
    hostData(state, { payload }) {
      return {
        ...state,
        hostData: payload
      }
    },
    addCA(state, { payload }) {
      return {
        ...state,
        addCA: payload,
        isFetching: false,
      }
    },
    fetch_addCA(state) {
      return {
        ...state,
        isFetching: true,
      }
    },
    err_addCA(state) {
      return {
        ...state,
        isFetching: false,
      }
    },
    getCA(state, { payload }) {
      return {
        ...state,
        getCA: payload
      }
    },
    getOneCA(state, { payload }) {
      return {
        ...state,
        getOneCA: payload
      }
    },
    deleteCA(state, { payload }) {
      return {
        ...state,
        deleteCA: payload
      }
    },
    operCA(state, { payload }) {
      return {
        ...state,
        operCA: payload
      }
    },
    getConfigCA(state, { payload }) {
      return {
        ...state,
        getConfigCA: payload
      }
    },
    setConfigCA(state) {
      return {
        ...state,
        setConfigCA: {
          isFetching: false
        }
      }
    },
    fetch_setConfigCA(state) {
      return {
        ...state,
        setConfigCA: {
          isFetching: true,
        }
      }
    },
    err_setConfigCA(state) {
      return {
        ...state,
        setConfigCA: {
          isFetching: false,
        }
      }
    },
    getConfigTpl(state, { payload }) {
      return {
        ...state,
        getConfigTpl: payload
      }
    },
    updateCert(state, { payload }) {
      return {
        ...state,
        updateCert: payload
      }
    },
    createServer(state, { payload }) {
      return {
        ...state,
        createServer: payload
      }
    },
  },
};
