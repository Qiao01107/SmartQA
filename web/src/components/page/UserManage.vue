<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item> <i class="el-icon-lx-cascades"></i> 用户管理 </el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="container">
            <div class="handle-box">
                <el-input
                    @keyup.enter="handleSearch"
                    v-model="searchContent"
                    placeholder="用户手机号"
                    class="handle-input mr10"
                    oninput="value=value.replace(/[^\d]/g,'')"
                    maxlength="11"
                ></el-input>
                <el-button type="primary" icon="el-icon-refresh" @click="reflesh()">刷新</el-button>
                <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
                <el-button type="primary" icon="el-icon-search" @click="sceen">被禁用用户</el-button>
            </div>
            <el-table :data="UserManageForm" border class="table" ref="multipleTable" header-cell-class-name="table-header">
                <el-table-column prop="weChatName" label="微信昵称" width="250px" align="center"></el-table-column>
                <el-table-column prop="phone" label="手机号" width="180px" align="center"></el-table-column>
                <el-table-column label="用户状态" width="100px" align="center">
                    <template #default="scope">
                        <el-tag :type="scope.row.userStatus === '0' ? 'success' : 'danger'">{{
                            scope.row.userStatus === '0' ? '正常' : '禁用'
                        }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="cause" label="禁用原因" width="265px" align="center"></el-table-column>
                <el-table-column prop="registerTime" label="注册时间" align="center" width="255px"></el-table-column>
                <el-table-column
                    fixed="right"
                    label="操作"
                    width="120px
                "
                >
                    <template #default="scope">
                        <el-button
                            v-if="scope.row.userStatus === '0'"
                            @click="bindForbind(scope.row)"
                            type="text"
                            size="small"
                            style="color: red"
                            icon="el-icon-error"
                            >禁用</el-button
                        >
                        <el-button
                            v-else
                            @click="handleRecoverClick(scope.row)"
                            type="text"
                            size="small"
                            style="color: #07a515"
                            icon="el-icon-success"
                            >恢复</el-button
                        >
                        <!-- <el-button type="text" size="small" @click="compile(scope.row)">编辑</el-button> -->
                    </template>
                </el-table-column>
            </el-table>
            <el-dialog v-model="forbiddenVisible" title="禁用原因" width="30%">
                <el-form :model="forbiddenReason" class="formClass">
                    <el-form-item label="用户昵称:" label-width="120px">
                        <el-input v-model="forbiddenReason.weChatName" disabled></el-input>
                    </el-form-item>
                    <el-form-item label="手机号:" label-width="120px">
                        <el-input v-model="forbiddenReason.phone" disabled></el-input>
                    </el-form-item>
                    <el-form-item label="禁用原因:" label-width="120px">
                        <el-input v-model="forbiddenReason.cause"></el-input>
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="forbiddenVisible = false">取 消</el-button>
                    <el-button type="primary" @click="handleForbiddenClick()">确 定</el-button>
                </template>
            </el-dialog>
            <div class="pagination">
                <el-pagination
                    background
                    layout="total, prev, pager, next"
                    :current-page="currentPage"
                    :page-size="10"
                    :total="pageTotal"
                    @current-change="handlePageChange"
                    @prev-click="handlePagePrev"
                    @next-click="handlePageNext"
                    v-if="pd == true"
                ></el-pagination>
            </div>
        </div>
    </div>
</template>
<script>
import store from '../../store';
export default {
    name: 'UserManage',
    data() {
        return {
            UserManageForm: [],
            currentPage: 1,
            pageTotal: 10,
            account: store.state.userInfo.account,
            searchContent: '',
            pd: true,
            forbiddenVisible: false,
            forbiddenReason: {}
        };
    },
    methods: {
        bindForbind(e) {
            this.forbiddenReason = e;
            this.forbiddenVisible = true;
        },
        handleForbiddenClick() {
            let that = this;
            let e = this.forbiddenReason;

            this.$axios
                .post('/api/disable_user', {
                    phone: e.phone,
                    cause: e.cause,
                    account: that.account
                })
                .then(() => {
                    that.$message({
                        message: '禁用用户成功',
                        type: 'success'
                    });
                    that.reflesh();
                    that.forbiddenVisible = false;
                });
        },
        sceen() {
            let that = this;
            this.$axios
                .post('/api/screen_user', {
                    status: 1
                })
                .then(response => {
                    that.UserManageForm = response.data;
                    that.UserManageForm = response.data;
                    that.UserManageForm.forEach(i => {
                        i.registerTime = i.registerTime.substring(0, 19).replace('T', ' ');
                    });
                    that.pd = false;
                });
        },
        handleRecoverClick(e) {
            let that = this;
            that.$confirm('确定恢复该用户?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'success',
                center: true
            })
                .then(() => {
                    that.$axios
                        .post('/api/able_user', {
                            phone: e.phone,
                            account: this.account
                        })
                        .then(() => {
                            that.$message({
                                message: '恢复用户成功',
                                type: 'success'
                            });
                            that.reflesh();
                        });
                })
                .catch(() => {
                    that.$message({
                        message: '已取消',
                        type: 'info'
                    });
                });
        },
        handleSearch() {
            let that = this;
            this.$axios.post('/api/search_user', { phone: this.searchContent }).then(response => {
                that.UserManageForm = response.data;
                that.UserManageForm.forEach(i => {
                    i.registerTime = i.registerTime.substring(0, 19).replace('T', ' ');
                });
                that.pd = false;
            });
        },
        reflesh() {
            let that = this;
            that.pd = true;
            this.$axios.post('/api/get_wechat_user', { start: 1, number: 10 }).then(response => {
                that.UserManageForm = response.data;
                that.UserManageForm.forEach(i => {
                    i.registerTime = i.registerTime.substring(0, 19).replace('T', ' ');
                });
            });
            this.$axios.post('/api/count_wechat_user').then(response => {
                that.pageTotal = response.data;
            });
            that.pd = true;
            that.searchContent = '';
        }
    },
    mounted() {
        let that = this;
        this.$axios.post('/api/get_wechat_user', { start: 1, number: 10 }).then(response => {
            that.UserManageForm = response.data;
            that.UserManageForm.forEach(i => {
                i.registerTime = i.registerTime.substring(0, 19).replace('T', ' ');
            });
        });
        this.$axios.post('/api/count_wechat_user').then(response => {
            that.pageTotal = response.data;
        });
        that.pd = true;
    }
};
</script>
<style scoped>
.handle-box {
    margin-bottom: 20px;
}

.handle-select {
    width: 120px;
}

.handle-input {
    width: 300px;
    display: inline-block;
}
.table {
    width: 100%;
    font-size: 14px;
}
.red {
    color: #ff0000;
}
.mr10 {
    margin-right: 10px;
}
.table-td-thumb {
    display: block;
    margin: auto;

    width: 40px;
    height: 40px;
}
</style>