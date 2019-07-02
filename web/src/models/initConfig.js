import { router } from 'umi/router';
import { setORganiztion, getInitConfig, addHost, getConfigTpl, getHostData, addCA, createServer } from '@/services/api';

export default {
  namespace: 'initConfig',

  state: {
    getInitConfig: {
      step: 0,
      complete: true
    },
    setORganiztion: {
      isLoading: false,
      data: {
        orgName: '',
        orgMspId: '',
        orgType: 'ordererOrg',
        tlsEnable: false,
        ordererIp: '',
        ordererPort: ''
      }
    },
    addHostRes: {
      isLoading: false,
      data: {
        hostName: '',
        ip: '',
        port: '2375',
        protocol: 'tcp',
        tlsEnable: false,
        ca: '',
        cert: '',
        key: ''
      }
    },
    isShowTls: false,
  },

  effects: {
    // 获取初始化配置信息
    *handleGetInitConfig(_, { put, call }) {
      const res = yield call(getInitConfig);
      if (res && res.meta.success) {
        yield put({
          type: 'getInitConfig',
          payload: res.data
        });
      }
    },
    // 配置基本组织信息
    *handleSetOrganiztion({ payload }, { put, call }) {
      yield put({
        type: 'fetch_setORganiztion',
        payload
      });

      const res = yield call(setORganiztion, payload);
      if (res && res.meta.success) {
        yield put({
          type: 'setORganiztion',
          payload
        });
      } else {
        yield put({
          type: 'err_setORganiztion',
          payload
        });
      }
    },
    // 配置host主机信息
    *addHostHandle({ payload }, { call, put }) {
      const { values } = payload;
      yield put({
        type: 'fetch_addHostRes',
        payload: values
      });

      const res = yield call(addHost, payload);
      if (res && res.meta.success) {
        yield put({
          type: 'addHostRes',
          payload: values
        });
      } else {
        yield put({
          type: 'err_addHostRes',
          payload: values
        });
      }
    },
    // 返回上一步配置
    *handlePrevStep({ payload }, { put }) {
      yield put({
        type: 'pervStep',
        payload
      });
    },
    // 是否使用Tls证书
    *handleShowHostTls(_, { put }) {
      yield put({
        type: 'showHostTle'
      });
    },
    // 获取组织信息
    *getHostHandle(_, { call, put }) {
      const response = yield call(getHostData);
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
    // 获取CA服务配置
    *handleGetConfigTpl(_, { call, put }) {
      const res = yield call(getConfigTpl);
      try {
        const { data } = res;
        yield put({
          type: 'getConfigTpl',
          payload: data
        })
      } catch (error) {
        console.log(error)
      }
    },
    // 新增CA
    *handleAddCA({ payload }, { call, put }) {
      yield put({ type: 'fetch_addCA' });
      const response = yield call(addCA, payload);
      if (response.meta.success) {
        response.addStamp = new Date().getTime();
        yield put({
          type: 'addCA',
          payload: response
        })
      } else {
        yield put({ type: 'err_addCA' })
      }
    },
    // 创建CA服务
    *handleCreateServer({ payload }, { call, put }) {
      const res = yield call(createServer, payload);
      try {
        const { meta } = res;
        meta.stamp = new Date().getTime();
        yield put({
          type: 'createServer',
          payload: meta
        })
      } catch (error) {
        console.log(error)
      }
    }
  },

  reducers: {
    getInitConfig(state, { payload }) {
      let current = 0;
      switch (payload.step) {
        case 'organization':
          current = 0;
          break;
        case 'host':
          current = 1;
          break;
        case 'ca':
          current = 2;
          break;
        default:
          break;
      }
      return {
        ...state,
        getInitConfig: {
          step: current,
          complete: payload.complete,
        }
      }
    },
    fetch_setORganiztion(state, { payload }) {
      return {
        ...state,
        setORganiztion: {
          isLoading: true,
          data: payload,
        }
      }
    },
    setORganiztion(state, { payload }) {
      return {
        ...state,
        setORganiztion: {
          isLoading: false,
          data: payload,
        },
        getInitConfig: {
          step: 1,
        }
      }
    },
    err_setORganiztion(state, { payload }) {
      return {
        ...state,
        setORganiztion: {
          isLoading: false,
          data: payload,
        }
      }
    },
    fetch_addHostRes(state, { payload }) {
      return {
        ...state,
        addHostRes: {
          isLoading: true,
          data: payload,
        },
      };
    },
    addHostRes(state, { payload }) {
      return {
        ...state,
        addHostRes: {
          isLoading: false,
          data: payload,
        },
        getInitConfig: {
          step: 2,
        }
      };
    },
    err_addHostRes(state, { payload }) {
      return {
        ...state,
        addHostRes: {
          isLoading: false,
          data: payload,
        },
      };
    },
    pervStep(state, { payload }) {
      return {
        ...state,
        getInitConfig: {
          step: payload - 1,
        }
      };
    },
    showHostTle(state) {
      return {
        ...state,
        isShowTls: !state.isShowTls
      };
    },
    hostData(state, { payload }) {
      return {
        ...state,
        hostData: payload
      }
    },
    getConfigTpl(state, { payload }) {
      return {
        ...state,
        getConfigTpl: payload
      }
    },
    createServer(state, { payload }) {
      return {
        ...state,
        createServer: payload
      }
    },
    addCA(state, { payload }) {
      return {
        ...state,
        addCA: payload,
        isFetching: false,
      }
    },
  },
};
