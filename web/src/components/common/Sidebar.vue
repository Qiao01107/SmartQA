<template>
    <div class="sidebar">
        <el-menu
            class="sidebar-el-menu"
            :default-active="onRoutes"
            :collapse="collapse"
            background-color="#324157"
            text-color="#bfcbd9"
            active-text-color="#20a0ff"
            unique-opened
            router
        >
            <template v-for="item in visibleItems">
                <template v-if="item.subs">
                    <el-sub-menu :index="item.index" :key="item.index">
                        <template #title>
                            <i :class="item.icon"></i>
                            <span>{{ item.title }}</span>
                        </template>
                        <template v-for="subItem in item.subs">
                            <el-sub-menu v-if="subItem.subs" :index="subItem.index" :key="subItem.index">
                                <template #title>{{ subItem.title }}</template>
                                <el-menu-item v-for="(threeItem, i) in subItem.subs" :key="i" :index="threeItem.index">{{
                                    threeItem.title
                                }}</el-menu-item>
                            </el-sub-menu>
                            <el-menu-item v-else :index="subItem.index" :key="subItem.index">{{ subItem.title }}</el-menu-item>
                        </template>
                    </el-sub-menu>
                </template>
                <template v-else>
                    <el-menu-item :index="item.index" :key="item.index">
                        <i :class="item.icon"></i>
                        <span>{{ item.title }}</span>
                    </el-menu-item>
                </template>
            </template>
        </el-menu>
    </div>
</template>

<script>
import bus from '../common/bus';
import store from '../../store';
export default {
    data() {
        return {
            collapse: false,
            items: [
                {
                    icon: 'el-icon-data-analysis',
                    index: 'Dashboard',
                    title: '首页'
                },
                {
                    icon: 'el-icon-user',
                    index: 'UserManage',
                    title: '用户管理'
                },
                {
                    icon: 'el-icon-s-custom',
                    index: 'AdminManage',
                    title: '管理员管理',
                    // 仅超级管理员可见（adminType='1'）。
                    // 注意：菜单隐藏只是体验优化，真正的拦截在后端 ROLE_SUPER_ADMIN 规则上。
                    roles: ['1']
                },
                {
                    icon: 'el-icon-s-tools',
                    index: 'Neo4j',
                    title: '图形界面'
                },
                {
                    icon: 'el-icon-view',
                    index: 'EntityForm',
                    title: '实体表管理'
                },
                {
                    icon: 'el-icon-share',
                    index: 'RelationForm',
                    title: '关系表管理'
                },
                {
                    icon: 'el-icon-notebook-1',
                    index: 'QuestionForm',
                    title: '问题表管理'
                },
                {
                    icon: 'el-icon-notebook-2',
                    index: 'QuestionTemplateForm',
                    title: '问题模板表管理'
                },
                {
                    icon: 'el-icon-edit-outline',
                    index: 'FeedbackForm',
                    title: '用户反馈表'
                },
                {
                    icon: 'el-icon-document-remove',
                    index: 'Logs',
                    title: '日志'
                }
                // {
                //     title: '问题表管理',
                //     icon: 'el-icon-coin',
                //     subs: [
                //         {
                //             icon: 'el-icon-coin',
                //             index: 'questionForm',
                //             title: '问题表管理'
                //         }
                //     ]
                // }
            ]
        };
    },
    computed: {
        onRoutes() {
            return this.$route.path.replace('/', '');
        },
        userInfo() {
            return store.state.userInfo;
        },
        /** 按当前角色过滤菜单项；未声明 roles 的项对所有已登录管理员可见。 */
        visibleItems() {
            const adminType = String((this.userInfo && this.userInfo.adminType) || '');
            return this.items.filter(
                item => !item.roles || item.roles.indexOf(adminType) !== -1
            );
        }
    },
    created() {
        // 通过 Event Bus 进行组件间通信，来折叠侧边栏
        bus.$on('collapse', msg => {
            this.collapse = msg;
            bus.$emit('collapse-content', msg);
        });
    }
};
</script>

<style scoped>
.sidebar {
    display: block;
    position: absolute;
    left: 0;
    top: 70px;
    bottom: 0;
    overflow-y: scroll;
}
.sidebar::-webkit-scrollbar {
    width: 0;
}
.sidebar-el-menu:not(.el-menu--collapse) {
    width: 250px;
}
.sidebar > ul {
    height: 100%;
}
</style>
