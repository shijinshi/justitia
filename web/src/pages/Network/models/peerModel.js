import { message } from 'antd';
import { getHostData, getCAUser, getCertInfo, getCA, deleteHost, joinChannel } from '@/services/api';

export default {
  namespace: 'peerModel',

  state: {
    title: '主机信息及设置',
    token: localStorage.getItem('token'),
    isFetching: false,
  },

  effects: {
    *getHostHandle({ payload }, { call, put }) {  //获取主机信息
      const response = yield call(getHostData, payload);
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
    *handleGetCA({ payload }, { call, put }) {
      const response = yield call(getCA, payload);
      try {
        const { data } = response;
        data.stamp = new Date().getTime();
        yield put({
          type: 'getCA',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }
    },
    *handleGetCAUser({ payload }, { call, put }) {
      const response = yield call(getCAUser, payload);
      try {
        const { data } = response;
        // data.stamp = new Date().getTime();
        yield put({
          type: 'CAUser',
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
        data.stamp = new Date().getTime();
        yield put({
          type: 'getCertInfo',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }
    },
    // 添加节点至指定通道
    *handleAddPeer({ payload }, { call, put }) {
      yield put({ type: 'fetch_joinChannel' });
      const res = yield call(joinChannel, payload);
      const { meta } = res;
      if (meta.success) {
        message.success(meta.message);
        yield put({ type: 'joinChannel' });
      } else {
        yield put({ type: 'err_joinChannel' });
      }
    },
  },

  reducers: {
    hostData(state, { payload }) {
      return {
        ...state,
        hostData: payload
      }
    },
    CAUser(state, { payload }) {
      return {
        ...state,
        CAUser: payload
      }
    },
    getCertInfo(state, { payload }) {
      return {
        ...state,
        certInfo: payload
      }
    },
    getCA(state, { payload }) {
      return {
        ...state,
        getCA: payload
      }
    },
    joinChannel(state) {
      return {
        ...state,
        isFetching: false,
      }
    },
    fetch_joinChannel(state) {
      return {
        ...state,
        isFetching: true,
      }
    },
    err_joinChannel(state) {
      return {
        ...state,
        isFetching: false,
      }
    },
  },
};
