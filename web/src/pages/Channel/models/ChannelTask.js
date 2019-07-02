import { message } from 'antd';
import { getChannelTask, getTaskDetail, dealChannelTask, delChannelTask, submitChannelTask, resChannelTask } from '@/services/api';

export default {
  namespace: 'ChannelTask',

  state: {
    isLoading: false,
    taskList: [],
    taskDetail: [],
  },

  effects: {
    // 待处理任务列表
    *handleGetTaskList(_, { call, put }) {
      yield put({ type: 'fetch_getChannelTask' });
      const response = yield call(getChannelTask);
      try {
        const { meta } = response;
        if (meta.success) {
          yield put({
            type: 'getChannelTask',
            payload: response.data
          });
        } else {
          yield put({ type: 'err_getChannelTask' });
        }
      } catch (error) {
        yield put({ type: 'err_getChannelTask' });
      }
    },
    // 任务详情
    *handleGetTaskDetail({ payload }, { call, put }) {
      yield put({ type: 'fetch_getTaskDetail' });
      const response = yield call(getTaskDetail, payload);
      try {
        const { meta } = response;
        if (meta.success) {
          yield put({
            type: 'getTaskDetail',
            payload: response.data
          });
        } else {
          yield put({ type: 'err_getTaskDetail' });
        }
      } catch (error) {
        yield put({ type: 'err_getTaskDetail' });
      }
    },
    // 处理任务
    *handelDealTask({ payload }, { call, put }) {
      yield put({ type: 'fetch_dealTask' });
      const response = yield call(dealChannelTask, payload);
      const { meta } = response;
      if (meta.success) {
        message.success(meta.message);
        yield put({ type: 'dealTask' });
        yield put({ type: 'handleGetTaskList' });
      } else {
        yield put({ type: 'err_dealTask' });
      }
    },
    // 删除任务
    *handleDelTask({ payload }, { call, put }) {
      yield put({ type: 'fetch_delTask' });
      const response = yield call(delChannelTask, payload);
      const { meta } = response;
      if (meta.success) {
        message.success(meta.message);
        yield put({ type: 'handleGetTaskList' });
      } else {
        yield put({ type: 'err_delTask' });
      }
    },
    // 撤销任务
    *handleResTask({ payload }, { call, put }) {
      yield put({ type: 'fetch_resTask' });
      const response = yield call(resChannelTask, payload);
      const { meta } = response;
      if (meta.success) {
        message.success(meta.message);
        yield put({ type: 'handleGetTaskList' });
      } else {
        yield put({ type: 'err_resTask' });
      }
    },
    // 提交任务至orderer
    *handleSubTask({ payload }, { call, put }) {
      yield put({ type: 'fetch_submitTask' });
      const response = yield call(submitChannelTask, payload);
      const { meta } = response;
      if (meta.success) {
        message.success(meta.message);
        yield put({ type: 'handleGetTaskList' });
      } else {
        yield put({ type: 'err_submitTask' });
      }
    },
  },

  reducers: {
    /* 列表 */
    getChannelTask(state, { payload }) {
      return {
        ...state,
        isLoading: false,
        taskList: payload
      }
    },
    fetch_getChannelTask(state) {
      return {
        ...state,
        isLoading: true,
      }
    },
    err_getChannelTask(state) {
      return {
        ...state,
        isLoading: false,
      }
    },
    /* 详情 */
    getTaskDetail(state, { payload }) {
      return {
        ...state,
        isLoading: false,
        taskDetail: [payload],
      }
    },
    fetch_getTaskDetail(state) {
      return {
        ...state,
        isLoading: true,
      }
    },
    err_getTaskDetail(state) {
      return {
        ...state,
        isLoading: false,
      }
    },
    /* 处理 */
    dealTask(state, { payload }) {
      return {
        ...state,
        isFetching: false,
      }
    },
    fetch_dealTask(state) {
      return {
        ...state,
        isLoading: true,
        isFetching: true,
      }
    },
    err_dealTask(state) {
      return {
        ...state,
        isLoading: false,
        isFetching: false,
      }
    },
    /* 删除 */
    fetch_delTask(state) {
      return {
        ...state,
        isLoading: true,
      }
    },
    err_delTask(state) {
      return {
        ...state,
        isLoading: false,
      }
    },
    /* 撤销 */
    fetch_resTask(state) {
      return {
        ...state,
        isLoading: true,
      }
    },
    err_resTask(state) {
      return {
        ...state,
        isLoading: false,
      }
    },
    /* 提交 */
    fetch_submitTask(state) {
      return {
        ...state,
        isLoading: true,
      }
    },
    err_submitTask(state) {
      return {
        ...state,
        isLoading: false,
      }
    },
  }
};
