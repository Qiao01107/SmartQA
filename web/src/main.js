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
import axios from 'axios';
import qs from 'qs';

const app = createApp(App);

// 全局注册 axios / qs
app.config.globalProperties.$axios = axios;
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

// 全局路由守卫
router.beforeEach((to, from, next) => {
    if (store.state.token == undefined && to.path !== '/login' && to.path !== '/forgetPassword') {
        next('/login');
    } else {
        next();
    }
});

app.mount('#app');
