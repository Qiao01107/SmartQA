<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item> <i class="el-icon-lx-cascades"></i> 管理员管理 </el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="container">
            <div class="handle-box">
                <!-- <el-input @keyup.enter.native="handleSearch" v-model="searchContent" placeholder="姓名或ID" class="handle-input mr10">
                </el-input> -->
                <el-button type="primary" icon="el-icon-search" @click="selectSuperAd()">超级管理员</el-button>
                <el-button type="primary" icon="el-icon-search" @click="selectAd()">普通管理员</el-button>
                <el-button type="primary" icon="el-icon-refresh" @click="handleRefresh">刷新</el-button>
                <!-- <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button> -->
                <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增</el-button>
            </div>
            <el-table :data="adminList" border class="table" ref="multipleTable" header-cell-class-name="table-header">
                <el-table-column prop="account" label="账号" width="280" align="center"></el-table-column>
                <el-table-column prop="name" label="姓名" align="center"></el-table-column>
                <el-table-column prop="phone" label="手机号" width="250" align="center"></el-table-column>
                <el-table-column label="账号类型" width="140" align="center">
                    <template #default="scope">
                        <el-tag>{{ scope.row.adminType === '1' ? '超级管理员' : '管理员' }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="registerTime" label="创建时间" width="200" align="center"></el-table-column>
                <el-table-column label="操作" width="180" align="center">
                    <template #default="scope">
                        <el-button type="text" icon="el-icon-edit" class="blue" @click="handleEdit(scope.$index, scope.row)"
                            >修改</el-button
                        >
                        <el-button type="text" icon="el-icon-delete" class="red" @click="handleDelete(scope.$index, scope.row)"
                            >删除</el-button
                        >
                    </template>
                </el-table-column>
            </el-table>
            <div class="pagination" v-if="pd == true">
                <el-pagination
                    background
                    layout="total, prev, pager, next"
                    :current-page="currentPage"
                    :page-size="10"
                    :total="pageTotal"
                    @current-change="handlePageChange"
                    @prev-click="handlePagePrev"
                    @next-click="handlePageNext"
                ></el-pagination>
            </div>
        </div>

        <el-dialog title="编辑" v-model="editVisible" width="30%">
            <el-form ref="form" :model="form" :rules="rules" label-width="120px">
                <el-form-item label="管理员姓名" prop="adminName">
                    <el-input v-model="form.name" disabled></el-input>
                </el-form-item>
                <el-form-item label="管理员账号" prop="adminAccount">
                    <el-input v-model="form.account" disabled></el-input>
                </el-form-item>
                <el-form-item label="管理员手机号" prop="phone">
                    <el-input v-model="form.phone" oninput="value=value.replace(/[^\d]/g,'')" maxlength="11"></el-input>
                </el-form-item>
                <el-form-item label="选择管理员类型" prop="adminType">
                    <el-radio-group v-model="form.adminType">
                        <el-radio :value="'0'">普通管理员</el-radio>
                        <el-radio :value="'1'">超级管理员</el-radio>
                    </el-radio-group>
                </el-form-item>
            </el-form>
            <template #footer class="dialog-footer">
                <el-button @click="editVisible = false">取 消</el-button>
                <el-button type="primary" @click="saveEdit">确 定</el-button>
            </template>
        </el-dialog>

        <el-dialog title="新增" v-model="addVisible" width="30%">
            <el-form ref="addForm" :model="addForm" :rules="rules" label-width="120px">
                <el-form-item label="管理员姓名" prop="name">
                    <el-input v-model="addForm.name"></el-input>
                </el-form-item>
                <el-form-item label="管理员账号" prop="account">
                    <el-input v-model="addForm.account"></el-input>
                </el-form-item>
                <el-form-item label="管理员手机号" prop="phone">
                    <el-input v-model="addForm.phone" oninput="value=value.replace(/[^\d]/g,'')" maxlength="11"></el-input>
                </el-form-item>
                <el-form-item label="管理员密码" prop="password">
                    <el-input type="password" placeholder="至少 8 位，需包含字母和数字" v-model="addForm.password"></el-input>
                </el-form-item>
                <el-form-item label="确认管理员密码" prop="againPassword">
                    <el-input type="password" placeholder="againPassword" v-model="addForm.againPassword"></el-input>
                </el-form-item>
                <el-form-item label="选择管理员类型" prop="adminType">
                    <el-radio-group v-model="addForm.adminType">
                        <el-radio :value="0">普通管理员</el-radio>
                        <el-radio :value="1">超级管理员</el-radio>
                    </el-radio-group>
                </el-form-item>
            </el-form>
            <template #footer class="dialog-footer">
                <el-button @click="addVisible = false">取 消</el-button>
                <el-button type="primary" @click="add">确 定</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script>
import store from '../../store';

export default {
    name: 'basetable',
    data() {
        return {
            searchContent: '',
            pd: true,
            allData: [],
            adminList: [],
            currentPage: 1,
            pageTotal: 0,
            currentMode: 'AllTypeList',
            form: {},
            account: store.state.userInfo.account,
            idx: -1,
            id: -1,
            editVisible: false,
            addVisible: false,
            addForm: {},
            radio: 1,
            rules: {
                name: [{ required: true, message: '请输入管理员姓名', trigger: 'blur' }],
                account: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
                password: [{ required: true, message: '请输入管理员密码', trigger: 'blur' }],
                phone: [{ required: true, message: '请输入管理员手机号码,长度为11位', trigger: 'blur', min: 11, max: 11 }],
                againPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }],
                adminType: [{ required: true, message: '请选择账号类型', trigger: 'blur' }]
            }
        };
    },
    created() {
        let that = this;
        this.$axios
            .post('/api/get_web_user', {
                start: '1',
                number: '10'
            })
            .then(response => {
                console.log(response);
                that.adminList = response.data;
                that.adminList.forEach((index, data) => {
                    console.log(index);
                    that.adminList[data].registerTime = index.registerTime.substring(0, 19);
                    that.adminList[data].registerTime = that.adminList[data].registerTime.replace('T', ' ');
                });
            });
        this.$axios.post('/api/count_web_user').then(response => {
            that.pageTotal = response.data;
        });
    },

    methods: {
        handleRefresh() {
            let that = this;
            this.pd = true;
            this.$axios
                .post('/api/get_web_user', {
                    start: '1',
                    number: '10'
                })
                .then(response => {
                    console.log(response);
                    that.adminList = response.data;
                    that.adminList.forEach((index, data) => {
                        console.log(index);
                        that.adminList[data].registerTime = index.registerTime.substring(0, 19);
                        that.adminList[data].registerTime = that.adminList[data].registerTime.replace('T', ' ');
                    });
                });
        },
        selectAd() {
            let that = this;
            this.$axios
                .post('/api/screen_web_user', {
                    adminType: 0
                })
                .then(response => {
                    that.adminList = response.data;
                    that.adminList.forEach((index, data) => {
                        console.log(index);
                        that.adminList[data].registerTime = index.registerTime.substring(0, 19);
                        that.adminList[data].registerTime = that.adminList[data].registerTime.replace('T', ' ');
                    });
                    that.pd = false;
                });
        },
        selectSuperAd() {
            let that = this;
            this.$axios
                .post('/api/screen_web_user', {
                    adminType: 1
                })
                .then(response => {
                    that.adminList = response.data;
                    that.adminList.forEach((index, data) => {
                        console.log(index);
                        that.adminList[data].registerTime = index.registerTime.substring(0, 19);
                        that.adminList[data].registerTime = that.adminList[data].registerTime.replace('T', ' ');
                    });
                    that.pd = false;
                });
        },
        handleEdit(index, e) {
            this.form = JSON.parse(JSON.stringify(e));
            this.editVisible = true;
        },
        saveEdit() {
            console.log(this.form);
            let that = this;
            if (this.form.phone.length != 11) {
                this.$message({
                    message: '请输入正确的手机号',
                    type: 'error'
                });
            } else {
                this.$axios
                    .post('/api/change_web_user', {
                        account: this.form.account,
                        phone: that.form.phone,
                        adminType: that.form.adminType
                    })
                    .then(() => {
                        that.$message({
                            message: '修改成功',
                            type: 'success'
                        });
                        that.handleRefresh();
                        that.editVisible = false;
                    });
            }
        },
        handleDelete(index, e) {
            let that = this;
            this.$confirm('确定删除该管理员?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'success',
                center: true
            })
                .then(() => {
                    that.$axios
                        .post('/api/remove_web_user', {
                            account: e.account
                        })
                        .then(response => {
                            if (response.data === true) {
                                that.$message({
                                    message: '删除成功',
                                    type: 'success'
                                });
                            } else {
                                // 常见原因：该管理员已产生操作日志（外键 RESTRICT 保护），
                                // 或试图删除自己、或试图删除最后一个超级管理员。
                                that.$message({
                                    message: '删除失败：该账号存在关联日志，或不允许删除自己/最后一个超级管理员',
                                    type: 'error'
                                });
                            }
                            that.handleRefresh();
                        });
                })
                .catch(() => {
                    that.$message({
                        message: '已取消',
                        type: 'info'
                    });
                });
        },
        handleAdd() {
            this.addForm = {};
            this.addVisible = true;
        },
        add() {
            let that = this;
            console.log(this.addForm);
            if (
                this.addForm.password === this.addForm.againPassword &&
                this.addForm.againPassword != undefined &&
                this.addForm.phone.length == 11
            ) {
                this.$axios
                    .post('/api/add_web_user', {
                        name: that.addForm.name,
                        account: that.addForm.account,
                        password: that.addForm.password,
                        phone: that.addForm.phone,
                        adminType: that.addForm.adminType.toString()
                    })
                    .then(function(response) {
                        if (response.data == true) {
                            that.$message({
                                message: '新增管理员成功',
                                type: 'success'
                            });
                            that.addVisible = false;
                        } else {
                            that.$message({
                                message: '新增管理员失败：账号可能已存在，或密码不满足“至少 8 位且含字母和数字”',
                                type: 'error'
                            });
                        }
                        that.handleRefresh();
                    });
            } else if (this.addForm.name == undefined) {
                this.$message({
                    message: '请输入姓名',
                    type: 'error'
                });
            } else if (this.addForm.phone.length != 11) {
                this.$message({
                    message: '请输入正确的手机号',
                    type: 'error'
                });
            } else {
                this.$message({
                    message: '两次密码输入不正确',
                    type: 'error'
                });
            }
        }
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
