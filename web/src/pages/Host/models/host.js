import { message } from 'antd';
import { getHostData, addHost, updateHost, getOneHost, deleteHost } from '@/services/api';

export default {
  namespace: 'host',

  state: {
    title: '主机信息及设置',
    token: localStorage.getItem('token'),
    addHostRes: {
      isLoading: false,
    },
    updateHost: {
      isLoading: false,
      data: []
    }
  },

  effects: {
    *getHostHandle(_, { call, put }) {
      const response = yield call(getHostData);
      try {
        const { data } = response;
        data.stamp = new Date().getTime();
        yield put({
          type: 'hostData',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }

    },
    *addHostHandle({ payload }, { call, put }) {
      yield put({
        type: 'fetch_addHostRes'
      });
      const res = yield call(addHost, payload);
      if (res && res.meta.success) {
        message.success(res.meta.message);
        yield put({
          type: 'addHostRes'
        });
      } else {
        yield put({
          type: 'err_addHostRes'
        });
      }
    },
    *updateHostHandle({ payload }, { call, put }) {
      yield put({
        type: 'fetch_updateHost'
      });
      const res = yield call(updateHost, payload);
      if (res && res.meta.success) {
        message.success(res.meta.message);
        yield put({
          type: 'updateHost'
        });
      } else {
        yield put({
          type: 'err_updateHost'
        });
      }
    },
    *getOneHostHandle({ payload }, { call, put }) {
      const response = yield call(getOneHost, payload);
      try {
        const { data, meta } = response;
        if (meta.success) {
          data.stamp = new Date().getTime();
        }
        yield put({
          type: 'getOneHost',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }

    },
    *deleteHostHandle({ payload }, { call, put }) {
      const response = yield call(deleteHost, payload);
      try {
        const { meta } = response;
        meta.stamp = new Date().getTime();
        yield put({
          type: 'deleteHost',
          payload: meta
        })
      } catch (error) {
        console.log(error)
      }

    },
    *saveNowHost({ payload }, { _, put }) {
      yield put({
        type: 'nowHost',
        payload
      })
    }

  },

  reducers: {
    hostData(state, { payload }) {
      return {
        ...state,
        hostData: payload
      }
    },
    addHostRes(state) {
      return {
        ...state,
        addHostRes: {
          isLoading: false
        }
      }
    },
    fetch_addHostRes(state) {
      return {
        ...state,
        addHostRes: {
          isLoading: true
        }
      }
    },
    err_addHostRes(state) {
      return {
        ...state,
        addHostRes: {
          isLoading: false
        }
      }
    },
    updateHost(state) {
      return {
        ...state,
        updateHost: {
          isLoading: false
        }
      }
    },
    fetch_updateHost(state) {
      return {
        ...state,
        updateHost: {
          isLoading: true
        }
      }
    },
    err_updateHost(state) {
      return {
        ...state,
        updateHost: {
          isLoading: false
        }
      }
    },
    getOneHost(state, { payload }) {
      return {
        ...state,
        oneHost: payload
      }
    },
    deleteHost(state, { payload }) {
      return {
        ...state,
        deleteHost: payload
      }
    },
    nowHost(state, { payload }) {
      return {
        ...state,
        nowHost: payload
      }
    }
  },
};
