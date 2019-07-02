import { message } from 'antd';
import { getConsortium, addConsortium, delConsortium } from '@/services/api';

export default {
  namespace: 'consortiumModel',

  state: {
    consortiumList: [],
    isFetching: false,
    isLoading: false,
    isClose: false
  },

  effects: {
    // 获取order节点下的全部联盟
    *handleGetConsortium({ payload }, { call, put }) {
      yield put({ type: 'fetch_getConsortium' });
      const response = yield call(getConsortium, payload);
      try {
        const { meta } = response;
        if (meta.success) {
          yield put({
            type: 'getConsortium',
            payload: response.data
          });
        } else {
          yield put({ type: 'err_getConsortium' });
        }
      } catch (error) {
        yield put({ type: 'err_getConsortium' });
      }
    },
    // 增加联盟成员
    *handelAddConsortium({ payload }, { call, put }) {
      yield put({ type: 'fetch_addConsortium' });
      const response = yield call(addConsortium, payload);
      const { meta } = response;
      if (meta.success) {
        message.success(meta.message);
        yield put({ type: 'addConsortium' });
        yield put({ type: 'network/getConfigOrderer' });
      } else {
        yield put({ type: 'err_addConsortium' });
      }
    },
    // 删除联盟成员
    *handelDelConsortium({ payload }, { call, put }) {
      yield put({ type: 'fetch_delConsortium' });
      const response = yield call(delConsortium, payload);
      const { meta } = response;
      if (meta.success) {
        message.success(meta.message);
        yield put({ type: 'delConsortium' });
        yield put({ type: 'network/getConfigOrderer' });
      } else {
        yield put({ type: 'err_delConsortium' });
      }
    },
  },

  reducers: {
    getConsortium(state, { payload }) {
      return {
        ...state,
        consortiumList: payload,
        isLoading: false
      }
    },
    fetch_getConsortium(state) {
      return {
        ...state,
        isLoading: true,
        isClose: false
      }
    },
    err_getConsortium(state) {
      return {
        ...state,
        isLoading: false
      }
    },
    addConsortium(state) {
      return {
        ...state,
        isFetching: false
      }
    },
    fetch_addConsortium(state) {
      return {
        ...state,
        isFetching: true
      }
    },
    err_addConsortium(state) {
      return {
        ...state,
        isFetching: false
      }
    },
    delConsortium(state) {
      return {
        ...state,
        isFetching: false,
        isClose: true
      }
    },
    fetch_delConsortium(state) {
      return {
        ...state,
        isFetching: true,
        isClose: false
      }
    },
    err_delConsortium(state) {
      return {
        ...state,
        isFetching: false,
        isClose: true
      }
    },
  },
};
