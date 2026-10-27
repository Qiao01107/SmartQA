import { createRouter, createWebHistory } from 'vue-router';

const routes = [
    {
        path: '/',
        redirect: '/Dashboard'
    },
    {
        path: '/',
        component: () => import('../components/common/Home.vue'),
        meta: { title: '自述文件' },
        children: [
            {
                path: '/Dashboard',
                component: () => import('../components/page/Dashboard.vue'),
                meta: { title: '首页' }
            },
            {
                path: '/UserManage',
                component: () => import('../components/page/UserManage.vue'),
                meta: { title: '用户管理' }
            },
            {
                path: '/AdminManage',
                component: () => import('../components/page/AdminManage.vue'),
                // 前端角色标记仅用于体验（提前跳 403）；后端 /add_web_user 等接口
                // 已限定 ROLE_SUPER_ADMIN，普通管理员即使手改路由也拿不到数据。
                meta: { title: '管理员管理', roles: ['1'] }
            },
            {
                path: '/404',
                component: () => import('../components/page/404.vue'),
                meta: { title: '404' }
            },
            {
                path: '/403',
                component: () => import('../components/page/403.vue'),
                meta: { title: '403' }
            },
            {
                path: '/neo4j',
                component: () => import('../components/page/Neo4j.vue'),
                meta: { title: '数据库图形界面' }
            },
            {
                path: '/entityForm',
                component: () => import('../components/page/EntityForm.vue'),
                meta: { title: '实体表管理' }
            },
            {
                path: '/relationForm',
                component: () => import('../components/page/RelationForm.vue'),
                meta: { title: '关系表管理' }
            },
            {
                path: '/questionForm',
                component: () => import('../components/page/QuestionForm.vue'),
                meta: { title: '问题表管理' }
            },
            {
                path: '/questionTemplateForm',
                component: () => import('../components/page/QuestionTemplateForm.vue'),
                meta: { title: '问题模板表管理' }
            },
            {
                path: '/feedbackForm',
                component: () => import('../components/page/FeedbackForm.vue'),
                meta: { title: '用户反馈管理' }
            },
            {
                path: '/userInformation',
                component: () => import('../components/page/UserInformation.vue'),
                meta: { title: '个人信息修改' }
            },
            {
                path: '/logs',
                component: () => import('../components/page/Logs.vue'),
                meta: { title: '后台管理系统日志' }
            }
        ]
    },
    {
        path: '/login',
        component: () => import('../components/page/Login.vue'),
        meta: { title: '登录' }
    },
    {
        path: '/forgetPassword',
        component: () => import('../components/page/ForgetPassword.vue'),
        meta: { title: '忘记密码' }
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/404'
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

export default router;
