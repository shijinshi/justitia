import { message } from 'antd';
import { getChannelList, getChannelConfig, createChannel, joinChannel, addChannelOrg, getChannelMspId, delChannelOrg } from '@/services/api';


export default {
  namespace: 'ChannelManager',

  state: {
    token: localStorage.getItem('token'),
    loading: false,
    isFetching: false,
    isClose: false,
    createChannel: {
      isFetching: false,
    },
    getChannelConfig: {},
    getChannelList: [],
    getChannelMspId: [],
  },

  effects: {
    // 获取channel通道信息
    *handleGetChannelList(_, { call, put }) {
      yield put({ type: 'fetch_getChannelList' });
      const response = yield call(getChannelList);
      try {
        const { meta } = response;
        if (meta.success) {
          yield put({
            type: 'getChannelList',
            payload: response.data
          });
        } else {
          yield put({ type: 'err_getChannelList' });
        }
      } catch (error) {
        yield put({ type: 'err_getChannelList' });
      }
    },
    // 获取channel通道配置文件
    *handleGetChannelConfig(_, { call, put }) {
      const response = yield call(getChannelConfig);
      // const { meta } = response;
      // if (meta.success) {
      //   yield put({
      //     type: 'getChannelConfig',
      //     payload: response.data
      //   })
      // }
    },
    // 创建通道
    *handleAddChannel({ payload }, { call, put }) {
      yield put({ type: 'fetch_createChannel' });
      const response = yield call(createChannel, payload);
      const { meta } = response;
      if (meta.success) {
        message.success(meta.message);
        yield put({ type: 'createChannel' });
        yield put({ type: 'handleGetChannelList' });
      } else {
        yield put({ type: 'err_createChannel' });
      }
    },
    // 申请添加组织
    *handleAddOrg({ payload }, { call, put }) {
      yield put({ type: 'fetch_addChannelOrg' });
      const response = yield call(addChannelOrg, payload);
      const { meta } = response;
      if (meta.success) {
        message.success(meta.message);
        yield put({ type: 'addChannelOrg' });
        yield put({ type: 'handleGetChannelList' });
      } else {
        yield put({ type: 'err_addChannelOrg' });
      }
    },
    // 申请删除组织
    *handleDelOrg({ payload }, { call, put }) {
      yield put({ type: 'fetch_delChannelOrg' });
      const response = yield call(delChannelOrg, payload);
      const { meta } = response;
      if (meta.success) {
        message.success(meta.message);
        yield put({ type: 'delChannelOrg' });
        yield put({ type: 'handleGetChannelList' });
      } else {
        yield put({ type: 'err_delChannelOrg' });
      }
    },
    // 获取指定通道的全部成员
    *handleGetChannelMspId({ payload }, { call, put }) {
      const response = yield call(getChannelMspId, payload);
      const { meta } = response;
      try {
        if (meta.success) {
          yield put({
            type: 'getChannelMspId',
            payload: response.data
          });
        }
      } catch (error) {
        console.log(error);
      }
    },
  },

  reducers: {
    getChannelList(state, { payload }) {
      return {
        ...state,
        loading: false,
        getChannelList: payload
      }
    },
    fetch_getChannelList(state) {
      return {
        ...state,
        loading: true
      }
    },
    err_getChannelList(state) {
      return {
        ...state,
        loading: false
      }
    },
    getChannelConfig(state, { payload }) {
      return {
        ...state,
        getChannelConfig: payload
      }
    },
    createChannel(state, { payload }) {
      return {
        ...state,
        createChannel: {
          isFetching: false,
        }
      }
    },
    fetch_createChannel(state) {
      return {
        ...state,
        createChannel: {
          isFetching: true,
        }
      }
    },
    err_createChannel(state, { payload }) {
      return {
        ...state,
        createChannel: {
          isFetching: false,
        }
      }
    },
    delChannelOrg(state) {
      return {
        ...state,
        loading: false,
        isClose: true,
      }
    },
    fetch_delChannelOrg(state) {
      return {
        ...state,
        loading: true,
        isClose: false,
      }
    },
    err_delChannelOrg(state) {
      return {
        ...state,
        loading: false,
        isClose: true,
      }
    },
    getChannelMspId(state, { payload }) {
      return {
        ...state,
        getChannelMspId: payload,
        isClose: false,
      }
    }
  }
};
