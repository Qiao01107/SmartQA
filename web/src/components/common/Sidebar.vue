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
            <template v-for="item in items">
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
                    <div
                        :index="item.index"
                        :key="item.index"
                        v-if="item.index != 'AdminManage' || (item.index == 'AdminManage' && userInfo.adminType == '1')"
                    >
                        <el-menu-item :index="item.index" :key="item.index">
                            <i :class="item.icon"></i>
                            <span>{{ item.title }}</span>
                        </el-menu-item>
                    </div>
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
                    title: '管理员管理'
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
