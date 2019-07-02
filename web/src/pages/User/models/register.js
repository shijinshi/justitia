import router from 'umi/router';
import { message } from 'antd';
import { register } from '@/services/api';
import { setAuthority } from '@/utils/authority';
import { reloadAuthorized } from '@/utils/Authorized';

export default {
  namespace: 'register',

  state: {
    status: undefined,
  },

  effects: {  
    *submit({ payload }, { call, put }) {
      const response = yield call(register, payload);
      const { data, meta } = response;
      yield put({
        type: 'registerHandle',
        payload: response,
      });
      if(meta.success){
        localStorage.setItem('token',data.token);
        setAuthority(data.user.identity?data.user.identity:'guest');
        reloadAuthorized();
        localStorage.setItem('userId',payload.userId);
        router.push('/');
      }
    },
  },

  reducers: {
    registerHandle(state, { payload }) {
      
      return {
        ...state,
        status: payload.status,
      };
    },
  },
};
