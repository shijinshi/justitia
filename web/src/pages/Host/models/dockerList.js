import { 
    getDockerList, 
    getDockerInfo, 
    deleteDocker, 
    startDocker, 
    pauseDocker, 
    unpauseDocker, 
    restartDocker, 
} from '@/services/api';


export default {
  namespace: 'dockerList',

  state: {
    token: localStorage.getItem('token')
  },

  effects: {  
    *handleGetDockerList({ payload }, { call, put}){
      const response = yield call(getDockerList, payload);
      try {
        const { data } = response;
        yield put({
            type: 'dockerList',
            payload: data
        })
      } catch (error) {
        console.log(error)
      }
      
    },
    *handleGetDockerInfo({ payload }, {call ,put}){
        const response = yield call(getDockerInfo, payload);
        try {
            const { data } = response;
            yield put({
                type: 'dockerData',
                payload: data
            })
        } catch (error) {
            console.log(error)
        }
        
    },
    *handleDeleteDocker({ payload }, { call ,put }){
        const response = yield call(deleteDocker, payload);
        try {
            const { data } = response;
            yield put({
                type: 'deleteDocker',
                payload: data
            })
        } catch (error) {
            console.log(error)
        }
        
    },
    *handleDockerStart({ payload },{ call, put}){
        const response = yield call(startDocker, payload);
        try {
            const { data } = response;
            yield put({
                type: 'startDocker',
                payload: data
            })
        } catch (error) {
            console.log(error)
        }
        
    },
    *handleDockerPause({ payload },{ call, put}){
        const response = yield call(pauseDocker, payload);
        try {
            const { data } = response;
            yield put({
                type: 'pauseDocker',
                payload: data
            })
        } catch (error) {
            console.log(error)
        }
        
    },
    *handleDockerUnpause({ payload },{ call, put}){
        const response = yield call(unpauseDocker, payload);
        try {
            const { data } = response;
            yield put({
                type: 'unpauseDocker',
                payload: data
            })
        } catch (error) {
            console.log(error)
        }
        
    },
    *handleDockerRestart({ payload },{ call, put}){
        const response = yield call(restartDocker, payload);
        try {
            const { data } = response;
            yield put({
                type: 'restartDocker',
                payload: data
            })
        } catch (error) {
            console.log(error)
        }
        
    },
  },

  reducers: {
    dockerList(state, { payload }){
      return {
        ...state,
        dockerList: payload
      }
    },
    dockerData(state, { payload }){
        return {
            ...state,
            dockerData: payload
        }
    },
    deleteDocker(state, { payload }){
        return {
            ...state,
            deleteDocker: payload
        }
    },
    startDocker(state, { payload }){
        return {
            ...state,
            startDocker: payload
        }
    },
    pauseDocker(state, { payload }){
        return {
            ...state,
            pauseDocker: payload
        }
    },
    unpauseDocker(state, { payload }){
        return {
            ...state,
            unpauseDocker: payload
        }
    },
    restartDocker(state, { payload }){
        return {
            ...state,
            restartDocker: payload
        }
    },
  },
};
