import { message } from 'antd';
import {
  getDockerImage,
  postDockerImage,
  getImageDetail,
  deleteImage,
  imageTag
} from '@/services/api';


export default {
  namespace: 'dockerImage',

  state: {
    token: localStorage.getItem('token'),
    imageTag: {
      isLoading: false,
    },
    postImageList: {}
  },

  effects: {
    *handleGetDockerImage({ payload }, { call, put }) {
      const response = yield call(getDockerImage, payload);
      try {
        const { data } = response;
        yield put({
          type: 'getImage',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }

    },
    *handlePostDockerImage({ payload }, { call, put }) {
      const response = yield call(postDockerImage, payload);
      try {
        const { meta } = response;
        if (meta.success) {
          meta.stamp = new Date().getTime();
          message.success(response.meta.message);
          yield put({
            type: 'imagePost',
            payload: meta
          });
        }
      } catch (error) {
        console.log(error)
      }

    },
    *handleGetImageDetail({ payload }, { call, put }) {
      const response = yield call(getImageDetail, payload);
      try {
        const { data } = response;
        yield put({
          type: 'imageDetail',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }

    },
    *handleDeleteImage({ payload }, { call, put }) {
      const response = yield call(deleteImage, payload);
      try {
        const { data } = response;
        yield put({
          type: 'deleteImage',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }

    },
    *handleImageTag({ payload }, { call, put }) {
      yield put({
        type: 'fetch_imageTag'
      });
      const response = yield call(imageTag, payload);
      try {
        const { meta } = response;
        if (meta.success) {
          message.success(meta.message);
          meta.stamp = new Date().getTime()
          yield put({
            type: 'imageTag',
            payload: meta
          });
        } else {
          yield put({
            type: 'err_imageTag'
          });
        }
      } catch (error) {
        console.log(error)
      }
    }
  },

  reducers: {
    getImage(state, { payload }) {
      return {
        ...state,
        getImageList: payload
      }
    },
    imagePost(state, { payload }) {
      return {
        ...state,
        postImageList: payload
      }
    },
    imageDetail(state, { payload }) {
      return {
        ...state,
        imageDetail: payload
      }
    },
    deleteImage(state, { payload }) {
      return {
        ...state,
        deleteImage: payload
      }
    },
    imageTag(state) {
      return {
        ...state,
        imageTag: {
          isLoading: false
        }
      }
    },
    fetch_imageTag(state) {
      return {
        ...state,
        imageTag: {
          isLoading: true
        }
      }
    },
    err_imageTag(state) {
      return {
        ...state,
        imageTag: {
          isLoading: false
        }
      }
    }
  },
};
