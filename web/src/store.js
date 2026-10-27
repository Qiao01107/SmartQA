import { createStore } from 'vuex';

/**
 * 前端登录态。
 *
 * 安全约定：
 * - `token` 是后端 JWT 接口签发的真实令牌，浏览器只做存储与携带，不解析、不伪造；
 * - `userInfo` 只保存展示所需字段，**绝不保存密码或密码哈希**（后端也已 @JsonIgnore）；
 * - 刷新页面后从 localStorage 恢复，避免"刷新即掉线"；
 * - 退出登录时同时清空内存与 localStorage，并调用后端吊销令牌。
 */
const TOKEN_KEY = 'smartqa_token';
const USER_KEY = 'smartqa_user';

const emptyUser = () => ({
    adminType: '',
    name: '',
    account: '',
    phone: '',
    head: '',
    email: '',
    registerTime: ''
});

function readUser() {
    try {
        const raw = localStorage.getItem(USER_KEY);
        if (!raw) {
            return emptyUser();
        }
        const parsed = JSON.parse(raw);
        // 兼容旧版本可能残留的密码字段：读取时直接丢弃。
        delete parsed.password;
        return Object.assign(emptyUser(), parsed);
    } catch (e) {
        return emptyUser();
    }
}

const store = createStore({
    state: {
        baseURL: '/api',
        token: localStorage.getItem(TOKEN_KEY) || undefined,
        userInfo: readUser()
    },
    mutations: {
        setUserinfomation(state, payload) {
            const safe = Object.assign(emptyUser(), payload || {});
            // 双保险：任何时候都不把密码写进前端状态。
            delete safe.password;
            state.userInfo = safe;
            localStorage.setItem(USER_KEY, JSON.stringify(safe));
        },
        setToken(state, payload) {
            state.token = payload;
            if (payload) {
                localStorage.setItem(TOKEN_KEY, payload);
            } else {
                localStorage.removeItem(TOKEN_KEY);
            }
        },
        getToken(state) {
            return state.token;
        },
        setHead(state, payload) {
            state.userInfo.head = payload;
            localStorage.setItem(USER_KEY, JSON.stringify(state.userInfo));
        },
        // 退出登录 / 登录失效：清空 token 与用户信息
        clearLogin(state) {
            state.token = undefined;
            state.userInfo = emptyUser();
            localStorage.removeItem(TOKEN_KEY);
            localStorage.removeItem(USER_KEY);
        }
    }
});

export default store;
