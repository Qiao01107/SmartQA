import { createStore } from 'vuex';

const store = createStore({
    state: {
        baseURL: '/api',
        token: undefined,
        userInfo: {
            adminType: '',
            name: '',
            account: '',
            phone: '',
            head: '',
            email: '',
            registerTime: '',
            password: ''
        }
    },
    mutations: {
        setUserinfomation(state, payload) {
            state.userInfo = payload;
        },
        setToken(state, payload) {
            state.token = payload;
        },
        getToken(state) {
            return state.token;
        },
        setHead(state, payload) {
            state.userInfo.head = payload;
        }
    }
});

export default store;
