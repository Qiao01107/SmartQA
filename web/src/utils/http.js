import axios from 'axios';
import { ElMessage } from 'element-plus';
import store from '../store';
import router from '../router';

/**
 * 全局 HTTP 客户端。
 *
 * 与旧实现的区别：
 * 1. 请求自动带上 `Authorization: Bearer <token>`，令牌由后端登录接口签发，不再伪造；
 * 2. 401（未认证/令牌过期/已注销）统一清理本地登录态并跳转登录页；
 * 3. 403（已登录但角色不够）只提示，不清登录态——这是"权限不足"而不是"掉线"；
 * 4. 响应体原样返回（`response.data` 仍是后端 JSON），保持所有页面既有写法可用。
 *
 * 注意：保留调用侧的 `/api` 前缀，因此这里不设置 baseURL，由 Vite 代理转发到 8081。
 */
const http = axios.create({
    timeout: 15000
});

http.interceptors.request.use(
    config => {
        const token = store.state.token;
        if (token) {
            config.headers = config.headers || {};
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    error => Promise.reject(error)
);

/** 登录接口自身的 401 属于"账号密码错误"，交给页面提示，不触发跳转。 */
function isLoginRequest(error) {
    const url = (error.config && error.config.url) || '';
    return url.indexOf('web_login') !== -1;
}

http.interceptors.response.use(
    response => response,
    error => {
        const response = error.response;
        if (!response) {
            return Promise.reject(error);
        }

        if (response.status === 401 && !isLoginRequest(error)) {
            store.commit('clearLogin');
            const current = router.currentRoute.value;
            if (current && current.path !== '/login') {
                ElMessage.error('登录已失效，请重新登录');
                router.push('/login');
            }
        } else if (response.status === 403) {
            const message = (response.data && response.data.message) || '当前角色无权访问该功能';
            ElMessage.error(message);
        }

        return Promise.reject(error);
    }
);

export default http;
