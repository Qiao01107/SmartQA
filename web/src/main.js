import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import ElementPlus from 'element-plus';
import { createI18n } from 'vue-i18n';
import { messages } from './components/common/i18n';
import store from './store';
import 'element-plus/dist/index.css'; // Element Plus 默认主题
import './assets/css/theme-green/index.css'; // 兼容层：保留旧 Element UI 字体图标（el-icon-* 类名映射）
import './assets/css/icon.css';
import directives from './components/common/directives';
import http from './utils/http';
import qs from 'qs';

const app = createApp(App);

// 全局注册 HTTP 客户端（已内置 Bearer 令牌注入与 401/403 处理）/ qs
app.config.globalProperties.$axios = http;
app.config.globalProperties.qs = qs;

// Element Plus（Vue3 不再需要 Vue.use，直接 app.use）
app.use(ElementPlus, {
    size: 'small'
});

// Vue I18n
const i18n = createI18n({
    legacy: false,
    locale: 'zh',
    messages
});
app.use(i18n);

app.use(store);
app.use(router);

// 注册全局自定义指令（v-dialogDrag）
app.directive('dialogDrag', directives.dialogDrag);

/**
 * 全局路由守卫。
 *
 * 职责仅限"体验层"：未登录进登录页、角色不匹配进 403。
 * 真正的访问控制在后端 SecurityFilterChain 上，前端隐藏菜单/拦截路由不构成安全边界。
 */
router.beforeEach((to, from, next) => {
    const isPublic = to.path === '/login' || to.path === '/forgetPassword';
    if (isPublic) {
        next();
        return;
    }

    if (!store.state.token) {
        next('/login');
        return;
    }

    const requiredRoles = to.meta && to.meta.roles;
    if (requiredRoles && requiredRoles.length > 0) {
        const adminType = store.state.userInfo && store.state.userInfo.adminType;
        if (requiredRoles.indexOf(String(adminType)) === -1) {
            next('/403');
            return;
        }
    }

    next();
});

app.mount('#app');
