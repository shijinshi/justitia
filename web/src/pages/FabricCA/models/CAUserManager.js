import { message } from 'antd';
import {
  getCAUser,
  registerCAUser,
  enrollCAUser,
  reenrollCAUser,
  deleteCAUser,
  createCrl,
  getCert,
  getCertInfo,
  downloadAllCert,
  downloadCert,
  revokeCert,
  getChildrenUser
} from '@/services/api';


export default {
  namespace: 'CAUserManager',

  state: {
    token: localStorage.getItem('token'),
    isLoading: true,
    enrollCAUser: {
      isShow: true,
    },
    deleteCAUser: {
      isShow: true,
    },
    registerCAUser: {
      isFetching: false,
    },
    getCAUser: []
  },

  effects: {
    *handleGetCAUser({ payload }, { call, put }) {
      yield put({ type: 'fetch_getCAUser' });
      const response = yield call(getCAUser, payload);
      try {
        const { meta, data } = response;
        data.stamp = new Date().getTime();
        if (meta.success) {
          yield put({
            type: 'getCAUser',
            payload: data
          });
        } else {
          yield put({ type: 'err_getCAUser' });
        }
      } catch (error) {
        yield put({ type: 'err_getCAUser' });
      }
    },
    *handleRegisterCAUser({ payload }, { call, put }) {
      yield put({ type: 'fetch_registerCAUser' });
      const response = yield call(registerCAUser, payload);
      const { meta } = response;
      if (meta.success) {
        message.success(meta.message);
        yield put({ type: 'registerCAUser' });
        yield put({ type: 'handleGetCAUser' });
      } else {
        yield put({ type: 'err_registerCAUser' });
      }
    },
    *handleEnrollCAUser({ payload }, { call, put }) {
      yield put({ type: 'fetch_enrollCAUser' });
      const response = yield call(enrollCAUser, payload);
      const { meta } = response;
      if (meta.success) {
        yield put({ type: 'enrollCAUser' });
        message.success(meta.message);
      } else {
        yield put({ type: 'err_enrollCAUser' });
      }
    },
    *handleReenrollCAUser({ payload }, { call, put }) {
      yield put({ type: 'fetch_reenrollCAUser' });
      const response = yield call(reenrollCAUser, payload);
      const { meta } = response;
      if (meta.success) {
        yield put({ type: 'reenrollCAUser' });
        message.success(meta.message);
      } else {
        yield put({ type: 'err_reenrollCAUser' });
      }
    },
    *handleDeleteCAUser({ payload }, { call, put }) {
      yield put({ type: 'fetch_deleteCAUser' });
      const response = yield call(deleteCAUser, payload);
      const { meta } = response;
      if (meta.success) {
        response.stamp = new Date().getTime();
        yield put({ type: 'deleteCAUser' });
        yield put({ type: 'handleGetCAUser' });
        message.success(meta.message);
      } else {
        yield put({ type: 'err_deleteCAUser' });
      }
    },
    *handleRevokeCert({ payload }, { call, put }) {
      const response = yield call(revokeCert, payload);
      try {
        response.stamp = new Date().getTime();
        yield put({
          type: 'revokeCert',
          payload: response
        })
      } catch (error) {
        console.log(error)
      }
    },
    *handleCreateCrl({ payload }, { call, put }) {
      const response = yield call(createCrl, payload);
      try {
        const { data } = response;
        data.stamp = new Date().getTime();
        yield put({
          type: 'createCrl',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }
    },
    *handleGetCert({ payload }, { call, put }) {
      const response = yield call(getCert, payload);
      try {
        const { data } = response;
        yield put({
          type: 'getCert',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }
    },
    *handleGetCertInfo({ payload }, { call, put }) {
      const response = yield call(getCertInfo, payload);
      try {
        const { data } = response;
        yield put({
          type: 'getCertInfo',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }
    },
    *handleDownloadAllCert({ payload }, { call, put }) {
      const response = yield call(downloadAllCert, payload);
      try {
        const { data } = response;
        yield put({
          type: 'downloadAllCert',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }
    },
    *handleDownloadCert({ payload }, { call, put }) {
      const response = yield call(downloadCert, payload);
      try {
        const { data } = response;
        yield put({
          type: 'downloadCert',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }
    },
    *handleGetChildrenUser({ payload }, { call, put }) {
      const response = yield call(getChildrenUser, payload);
      try {
        const { data } = response;
        yield put({
          type: 'childrenUser',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }
    },
  },

  reducers: {
    getCAUser(state, { payload }) {
      return {
        ...state,
        getCAUser: payload,
        isLoading: false,
      }
    },
    fetch_getCAUser(state) {
      return {
        ...state,
        isLoading: true,
      }
    },
    err_getCAUser(state) {
      return {
        ...state,
        isLoading: false,
      }
    },
    registerCAUser(state) {
      return {
        ...state,
        registerCAUser: {
          isFetching: false,
        }
      }
    },
    fetch_registerCAUser(state) {
      return {
        ...state,
        registerCAUser: {
          isFetching: true,
        }
      }
    },
    err_registerCAUser(state) {
      return {
        ...state,
        registerCAUser: {
          isFetching: false,
        }
      }
    },
    enrollCAUser(state) {
      return {
        ...state,
        isLoading: false,
      }
    },
    fetch_enrollCAUser(state) {
      return {
        ...state,
        isLoading: true,
      }
    },
    err_enrollCAUser(state) {
      return {
        ...state,
        isLoading: false,
      }
    },
    reenrollCAUser(state, { payload }) {
      return {
        ...state,
        isLoading: false,
      }
    },
    fetch_reenrollCAUser(state) {
      return {
        ...state,
        isLoading: true,
      }
    },
    err_reenrollCAUser(state) {
      return {
        ...state,
        isLoading: false,
      }
    },
    deleteCAUser(state) {
      return {
        ...state,
        deleteCAUser: {
          isFetching: false,
          isShow: false,
        }
      }
    },
    fetch_deleteCAUser(state) {
      return {
        ...state,
        deleteCAUser: {
          isFetching: true,
          isShow: false,
        }
      }
    },
    err_deleteCAUser(state) {
      return {
        ...state,
        deleteCAUser: {
          isFetching: false,
          isShow: false,
        }
      }
    },
    createCrl(state, { payload }) {
      return {
        ...state,
        createCrl: payload
      }
    },
    getCert(state, { payload }) {
      return {
        ...state,
        getCert: payload
      }
    },
    getCertInfo(state, { payload }) {
      return {
        ...state,
        getCertInfo: payload
      }
    },
    downloadAllCert(state, { payload }) {
      return {
        ...state,
        downloadAllCert: payload
      }
    },
    downloadCert(state, { payload }) {
      return {
        ...state,
        downloadCert: payload
      }
    },
    revokeCert(state, { payload }) {
      return {
        ...state,
        revokeCert: payload
      }
    },
    childrenUser(state, { payload }) {
      return {
        ...state,
        childrenUser: payload
      }
    }
  },
};
