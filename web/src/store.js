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
        },
        // 退出登录：清空 token 与用户信息
        clearLogin(state) {
            state.token = undefined;
            state.userInfo = {
                adminType: '',
                name: '',
                account: '',
                phone: '',
                head: '',
                email: '',
                registerTime: '',
                password: ''
            };
        }
    }
});

export default store;
