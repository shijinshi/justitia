import router from 'umi/router';
import { message } from 'antd';
import { query as queryUsers, queryCurrent, remarkName, getUserQuestions, modifiedPassword, modifiedInformation, getChildrenInformation, getRegisterCode } from '@/services/user';

export default {
  namespace: 'user',

  state: {
    list: [],
    currentUser: {},
  },

  effects: {
    *fetch(_, { call, put }) {
      const response = yield call(queryUsers);
      yield put({
        type: 'save',
        payload: response,
      });
    },
    *fetchCurrent({ payload }, { call, put }) {
      const response = yield call(queryCurrent, payload);
      yield put({
        type: 'saveCurrentUser',
        payload: response,
      });
    },
    *remark({payload}, {call, put}){
      const response = yield call(remarkName, payload);
      yield put({
        type: 'saveRemark',
        payload: response
      })
    },
    *questions({ payload }, { call, put }){
      const response = yield call(getUserQuestions, payload);
      yield put({
        type: 'saveQuestions',
        payload: response.data
      })
    },
    *password({ payload }, { call, put}){
      const response = yield call(modifiedPassword, payload);
      try {
        const { data, meta } = response;
        meta.stamp = new Date().getTime();
        yield put({
          type: 'savePassword',
          payload: meta
        })
        if(meta.success){
          router.push('/user/login')
        }
      } catch (error) {
        console.log(error)
      }
      
    },
    *information({ payload }, { call, put}){
      const response = yield call(modifiedInformation, payload);
      const { data } = response;
      yield put({
        type: 'saveInformation',
        payload: data
      })
    },
    *childrenInformation({ payload }, {call, put}){
      const response = yield call( getChildrenInformation , payload);
      const { data } = response;
      yield put({
        type: 'saveChildrenInformation',
        payload: data
      })
    },
    *registerCode({ payload }, {call, put}){
      const response = yield call(getRegisterCode, payload);
      const { data } = response;
      yield put({
        type: 'saveRegisterCode',
        payload: data
      })
    }
  },

  reducers: {
    saveRegisterCode(state, { payload }){
      return {
        ...state,
        registerCode: payload
      }
    },
    saveChildrenInformation(state, { payload }){
      return {
        ...state,
        childrenInformation: payload
      }
    },
    saveInformation(state, {payload}){
      return {
        ...state,
        information: payload
      }
    },
    savePassword(state, { payload }){
      return {
        ...state,
        password: payload
      }
    },
    saveQuestions(state, {payload}){
      return {
        ...state,
        questions: payload
      }
    },
    saveRemark(state, action){
      return {
        ...state,
        remark: action.payload
      }
    },
    save(state, action) {
      return {
        ...state,
        list: action.payload,
      };
    },
    saveCurrentUser(state, action) {
      return {
        ...state,
        currentUser: action.payload || {},
      };
    },
    changeNotifyCount(state, action) {
      return {
        ...state,
        currentUser: {
          ...state.currentUser,
          notifyCount: action.payload.totalCount,
          unreadCount: action.payload.unreadCount,
        },
      };
    },
  },
};
