<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item> <i class="el-icon-lx-cascades"></i> 用户反馈管理 </el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="container">
            <div class="container-fluid">
                <el-input v-model="searchContent" placeholder="请输入需要查询的问题"></el-input>
                <el-button type="primary" icon="el-icon-search" @click="handleSearch" style="margin-left: 15px">查找</el-button>
                <el-button type="primary" icon="el-icon-refresh" @click="handleRefresh" style="margin-left: 15px">刷新</el-button>
            </div>
            <div class="container-fluid">
                <el-select v-model="isSelect" style="width: 150px" @change="valueChange">
                    <el-option value="未审批">未审批</el-option>
                    <el-option value="审批未通过">审批未通过</el-option>
                    <el-option value="审批且通过">审批且通过</el-option>
                    <el-option value="全部">全部</el-option>
                </el-select>
            </div>
        </div>
        <el-table
            :data="feedbackForm"
            header-cell-class-name="table-header"
            :cell-style="{ 'text-align': 'center' }"
            :header-cell-style="{ 'text-align': 'center' }"
            @selection-change="selected"
            class="table"
        >
            <el-table-column prop="phone" label="反馈用户号码" width="200px"></el-table-column>
            <el-table-column prop="question" label="问题" width="300px"></el-table-column>
            <el-table-column prop="opinion" label="用户观点" width="300px"></el-table-column>
            <el-table-column prop="status" label="状态" width="100px">
                <template #default="scope">
                    <el-tag v-if="scope.row.status === '0'" type="info">未审核</el-tag>
                    <el-tag v-else-if="scope.row.status === '1'" type="success">审核通过</el-tag>
                    <el-tag v-else-if="scope.row.status === '2'" type="danger">审核未通过</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="feedbackTime" label="反馈时间" width="200px" sortable></el-table-column>
            <el-table-column fixed="right" label="操作" width="150px">
                <template #default="scope">
                    <el-button @click="handleClick(scope.row)" type="text" size="small" icon="el-icon-view">查看</el-button>
                    <el-button type="text" size="small" @click="handleEdit(scope.row)" icon="el-icon-edit">编辑</el-button>
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
        <el-dialog v-model="edit" title="问题反馈" width="30%">
            <el-form ref="editFeedback" :model="editFeedback">
                <el-form-item label="用户手机号:" label-width="120px">
                    <el-input v-model="editFeedback.phone" disabled />
                </el-form-item>
                <el-form-item label="用户反馈问题:" label-width="120px">
                    <el-input v-model="editFeedback.question" disabled />
                </el-form-item>
                <el-form-item label="用户观点:" label-width="120px">
                    <el-input type="textarea" v-model="editFeedback.opinion" disabled />
                </el-form-item>
                <el-form-item label="反馈回复:" label-width="120px">
                    <el-input type="textarea" v-model="editFeedback.evaluation" />
                </el-form-item>
                <el-form-item label="用户反馈问题:" label-width="120px">
                    <el-radio v-model="feedStatus" value="1" class="typeClasss">审核通过</el-radio>
                    <el-radio v-model="feedStatus" value="2" class="typeClasss">审核未通过</el-radio>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="edit = false">取 消</el-button>
                <el-button type="primary" @click="comfireEdit">确 定</el-button>
            </template>
        </el-dialog>
        <el-dialog v-model="userWatch" title="用户信息" width="30%">
            <el-form ref="userInformation" :model="userInformation">
                <el-form-item label="用户昵称:" label-width="120px">
                    <el-input v-model="userInformation.weChatName" disabled />
                </el-form-item>
                <el-form-item label="用户手机号:" label-width="120px">
                    <el-input v-model="userInformation.phone" disabled />
                </el-form-item>
                <el-form-item label="用户注册时间:" label-width="120px">
                    <el-input v-model="userInformation.registerTime" disabled />
                </el-form-item>
            </el-form>
        </el-dialog>
    </div>
</template>
<script>
import Axios from 'axios';
import store from '../../store';
export default {
    name: 'FeedbackForm',
    data() {
        return {
            edit: false,
            userWatch: false,
            userInformation: {},
            isSelect: '全部',
            feedStatus: '0',
            editFeedback: {},
            searchContent: '',
            account: store.state.userInfo.account,
            pageTotal: 10,
            currentPage: 1,
            pd: true,
            feedbackForm: [
                // {
                //     phone: '17352638120',
                //     opinion: '你看我应有几分的欢喜',
                //     status: '0',
                //     question: '咩咩咩咩咩咩咩咩咩咩咩咩咩咩',
                //     feedbackTime: '2021/11/27 10:51:30'
                // },
                // {
                //     phone: '17352638120',
                //     opinion: '你看我应有几分的欢喜',
                //     status: '1',
                //     question: '咩咩咩咩咩咩咩咩咩咩咩咩咩咩',
                //     feedbackTime: '2021/11/27 10:51:30'
                // },
                // {
                //     phone: '17352638120',
                //     opinion: '你看我应有几分的欢喜',
                //     status: '2',
                //     question: '咩咩咩咩咩咩咩咩咩咩咩咩咩咩',
                //     feedbackTime: '2021/11/27 10:51:30'
                // },
                // {
                //     phone: '17352638120',
                //     opinion: '你看我应有几分的欢喜',
                //     status: '0',
                //     question: '咩咩咩咩咩咩咩咩咩咩咩咩咩咩',
                //     feedbackTime: '2021/11/27 10:51:30'
                // }
            ]
        };
    },
    methods: {
        handleClick(e) {
            console.log(e);
            let that = this;
            this.$axios
                .post('/api/get_info', {
                    phone: e.phone
                })
                .then(response => {
                    that.userInformation = response.data;
                    that.userInformation.registerTime = that.userInformation.registerTime.substring(0, 19).replace('T', ' ');
                    that.userWatch = true;
                });
        },
        valueChange() {
            let that = this;
            let s;
            switch (this.isSelect) {
                case '审批且通过':
                    s = 1;
                    break;
                case '审批未通过':
                    s = 2;
                    break;
                case '未审批':
                    s = 0;
                    break;
                case '全部':
                    this.handleRefresh();
                    return;
            }
            this.$axios.post('/api/screen_feedback', { status: s }).then(response => {
                that.feedbackForm = response.data;
                that.feedbackForm.forEach(i => {
                    i.feedbackTime = i.feedbackTime.substring(0, 19).replace('T', ' ');
                });
                that.pd = false;
            });
        },
        handleEdit(e) {
            this.edit = true;
            this.editFeedback = e;
        },
        handleSearch() {
            let that = this;
            this.$axios
                .post('/api/search_feedback', {
                    inquire: this.searchContent
                })
                .then(response => {
                    that.feedbackForm = response.data;
                    that.feedbackForm.forEach(i => {
                        i.feedbackTime = i.feedbackTime.substring(0, 19).replace('T', ' ');
                    });
                    that.pd = false;
                });
        },
        comfireEdit() {
            console.log(this.editFeedback);
            let that = this;
            this.$axios
                .post('/api/change_feedback', {
                    phone: this.editFeedback.phone,
                    opinion: this.editFeedback.opinion,
                    question: this.editFeedback.question,
                    status: this.feedStatus,
                    feedbackTime: this.editFeedback.feedbackTime,
                    evaluation: this.editFeedback.evaluation,
                    account: this.account
                })
                .then(response => {
                    that.$message({
                        message: '审批成功',
                        type: 'success'
                    });
                    that.edit = false;
                    that.handleRefresh();
                });
        },
        handleRefresh() {
            let that = this;
            this.$axios
                .post('/api/get_feedback', {
                    start: 1,
                    number: 10
                })
                .then(response => {
                    that.feedbackForm = response.data;
                    that.feedbackForm.forEach(i => {
                        i.feedbackTime = i.feedbackTime.substring(0, 19).replace('T', ' ');
                    });
                });
            this.$axios.post('/api/count_feedback').then(response => {
                that.pageTotal = response.data;
            });
            that.pd = true;
            that.searchContent = '';
            this.isSelect = '全部';
        }
    },
    mounted() {
        let that = this;
        this.$axios
            .post('/api/get_feedback', {
                start: 1,
                number: 10
            })
            .then(response => {
                that.feedbackForm = response.data;
                that.feedbackForm.forEach(i => {
                    i.feedbackTime = i.feedbackTime.substring(0, 19).replace('T', ' ');
                });
            });
        this.$axios.post('/api/count_feedback').then(response => {
            that.pageTotal = response.data;
        });
    }
};
</script>
<style scoped>
.container {
    display: flex;
    flex-direction: row;
    justify-content: space-between;

    margin-top: 20px;
}
.table-header {
    text-align: center;
}
.container-fluid {
    display: flex;
    flex-direction: row;
}
.handle-input {
    width: 300px;
    height: 32px;
    /* display: inline-block; */
}
.table {
    width: 100%;
    font-size: 14px;
}
.input-with-select {
    width: 300px;
    background-color: #fff;
}
.fixContainer {
    /* visibility: hidden */
    width: 500px;
    height: 400px;
    background-color: #a70404;
    position: fixed;
}
.labelClass {
    color: #011608;
}
.el-select {
    width: 100px;
}
.table {
    width: 100%;
    font-size: 14px;
}
</style>