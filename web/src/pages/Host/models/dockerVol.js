import { message } from 'antd';
import {
  getDockerVol,
  getVolDetail,
  deleteVol,
  addVol
} from '@/services/api';


export default {
  namespace: 'dockerVol',

  state: {
    token: localStorage.getItem('token')
  },

  effects: {
    *handleGetDockerVol({ payload }, { call, put }) {
      const response = yield call(getDockerVol, payload);
      try {
        const { data } = response;
        yield put({
          type: 'getVolList',
          payload: data.Volumes
        })
      } catch (error) {
        console.log(error)
      }

    },
    *handleGetVolDetail({ payload }, { call, put }) {
      const response = yield call(getVolDetail, payload);
      try {
        const { data } = response;
        yield put({
          type: 'volDetail',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }

    },
    *handleDeleteVol({ payload }, { call, put }) {
      const response = yield call(deleteVol, payload);
      try {
        const { meta } = response;
        yield put({
          type: 'deleteVol',
          payload: meta
        })
      } catch (error) {
        console.log(error)
      }
    },
    *handleAddVol({ payload }, { call, put }) {
      const response = yield call(addVol, payload);
      try {
        const { data, meta } = response;
        if (meta.success) {
          message.success(meta.message);
          // data.stamp = new Date().getTime();
          yield put({
            type: 'addVol',
            payload: data
          });
          yield put({
            type: 'handleGetDockerVol',
            payload
          })
        }
        yield put({
          type: 'addVol',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }
    }
  },

  reducers: {
    getVolList(state, { payload }) {
      return {
        ...state,
        getVolList: payload
      }
    },
    volDetail(state, { payload }) {
      return {
        ...state,
        volDetail: payload
      }
    },
    deleteVol(state, { payload }) {
      return {
        ...state,
        deleteVol: payload
      }
    },
    addVol(state, { payload }) {
      return {
        ...state,
        addVol: payload
      }
    }
  },
};
