<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item> <i class="el-icon-lx-cascades"></i> 问题表管理 </el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="container">
            <div class="container-fluid">
                <el-input @keyup.enter="handleSearch" v-model="searchContent" placeholder="问题名称" class="input-with-select">
                </el-input>
                <el-button type="primary" icon="el-icon-refresh" @click="handleRefresh" style="margin-left: 15px">刷新</el-button>
                <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
                <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增</el-button>
                <el-button type="danger" icon="el-icon-close" @click="handleDelete">删除</el-button>
            </div>
            <div class="container-fluid">
                <el-button type="info" icon="el-icon-search" @click="selectUnexamine()">未审查</el-button>
                <el-button type="info" icon="el-icon-search" @click="selectExamine()">审查</el-button>
            </div>
        </div>
        <el-table
            :data="questionForm"
            header-cell-class-name="table-header"
            :cell-style="{ 'text-align': 'center' }"
            :header-cell-style="{ 'text-align': 'center' }"
            @selection-change="selected"
            @select-all="selectAll"
            ref="questionForms"
            class="table"
        >
            <el-table-column type="selection" width="75px"> </el-table-column>
            <el-table-column prop="question" label="问题" width="450px"></el-table-column>
            <el-table-column prop="answer" label="答案" width="350px"></el-table-column>
            <el-table-column prop="questionStatus" label="状态" width="240px">
                <template #default="scope">
                    <el-tag v-if="scope.row.questionStatus === '0'" type="info">未审查</el-tag>
                    <el-tag v-else-if="scope.row.questionStatus === '1'" type="success">正常</el-tag>
                </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="150px">
                <template #default="scope">
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
                @selection-change="selected"
            ></el-pagination>
        </div>
        <el-dialog v-model="addVisible" title="添加问题" width="30%">
            <el-form ref="addEntity" :model="addQuestion">
                <el-form-item label="问题:" label-width="120px">
                    <el-input v-model="addQuestion.question" />
                </el-form-item>
                <el-form-item label="答案:" label-width="120px">
                    <el-input v-model="addQuestion.answer" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="addVisible = false">取 消</el-button>
                <el-button type="primary" @click="add">确 定</el-button>
            </template>
        </el-dialog>
        <el-dialog v-model="complieVisible" title="编辑问题" width="30%">
            <el-form ref="addEntity" :model="complieQuestion">
                <el-form-item label="问题:" label-width="120px">
                    <el-input v-model="complieQuestion.question" />
                </el-form-item>
                <el-form-item label="答案:" label-width="120px">
                    <el-input v-model="complieQuestion.answer" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="complieVisible = false">取 消</el-button>
                <el-button type="primary" @click="complieAdd">确 定</el-button>
            </template>
        </el-dialog>
    </div>
</template>
<script>
import Axios from 'axios';
import store from '../../store';
export default {
    name: 'QuestionForm',
    data() {
        return {
            addVisible: false,
            complieVisible: false,
            haveSelected: [],
            addQuestion: {},
            complieQuestion: {},
            currentPage: 1,
            oldComplieQuestion: {},
            searchContent: '',
            account: store.state.userInfo.account,
            pageTotal: 32,
            questionForm: []
        };
    },
    methods: {
        selected(selection) {
            if (selection.length > 1) {
                this.haveSelected[0] = selection[1];
                let k = selection.shift();
                this.$refs.questionForms.toggleRowSelection(k, false);
            } else {
                this.haveSelected[0] = selection[0];
            }
            console.log(this.haveSelected);
        },
        selectAll() {
            this.$refs.questionForms.clearSelection();
            this.haveSelected = [];
        },
        selectUnexamine() {
            let that = this;
            this.$axios.post('/api/screen_question', { status: '0' }).then(response => {
                that.questionForm = response.data;
            });
        },
        selectExamine() {
            let that = this;
            this.$axios.post('/api/screen_question', { status: '1' }).then(response => {
                that.questionForm = response.data;
            });
        },
        handleDelete() {
            let that = this;
            console.log(that.haveSelected);
            if (this.haveSelected[0] != undefined) {
                this.$confirm('确定删除该问题?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'success',
                    center: true
                })
                    .then(() => {
                        that.$axios
                            .post('/api/remove_question', {
                                question: that.haveSelected[0].question,
                                account: that.account
                            })
                            .then(response => {
                                if (response.data == false) {
                                    that.$confirm('问题模板库中含有该问题的映射，此操作将同时删除问题模板库中的数据, 是否继续?', '提示', {
                                        confirmButtonText: '确定',
                                        cancelButtonText: '取消',
                                        type: 'warning',
                                        center: true
                                    })
                                        .then(() => {
                                            that.$axios
                                                .post('/api/insist_remove_question', {
                                                    question: that.haveSelected[0].question,
                                                    account: that.account
                                                })
                                                .then(() => {
                                                    that.$message({
                                                        message: '删除成功',
                                                        type: 'success'
                                                    });
                                                    that.handleRefresh();
                                                })
                                                .catch(() => {
                                                    that.$message({
                                                        message: '删除失败',
                                                        type: 'error'
                                                    });
                                                });
                                        })
                                        .catch(() => {
                                            that.$message({
                                                type: 'info',
                                                message: '已取消'
                                            });
                                        });
                                } else {
                                    that.$message({
                                        message: '删除成功',
                                        type: 'success'
                                    });
                                    that.handleRefresh();
                                }
                            });
                    })
                    .catch(() => {
                        that.$message({
                            message: '已取消',
                            type: 'info'
                        });
                    });
            }
        },
        handleAdd() {
            this.addVisible = true;
        },
        handleSearch() {
            let that = this;
            this.$axios
                .post('/api/search_question', {
                    inquire: that.searchContent
                })
                .then(response => {
                    that.questionForm = response.data;
                });
        },
        add() {
            let that = this;
            this.$axios
                .post('/api/add_question', {
                    question: that.addQuestion.question,
                    answer: that.addQuestion.answer,
                    account: that.account
                })
                .then(response => {
                    console.log(response);
                    if (response.data) {
                        that.$message({
                            message: '插入成功',
                            type: 'success'
                        });
                        that.handleRefresh();
                    }
                });
            this.addVisible = false;
            this.addQuestion = {};
        },
        handleEdit(e) {
            this.complieQuestion = JSON.parse(JSON.stringify(e));
            this.complieVisible = true;
            this.oldComplieQuestion = JSON.parse(JSON.stringify(e));
        },
        complieAdd() {
            let old = this.oldComplieQuestion;
            let newQu = this.complieQuestion;
            let that = this;
            if (newQu.answer != old.answer) {
                this.$axios
                    .post('/api/change_answer', {
                        questionId: newQu.id,
                        newAnswer: newQu.answer,
                        account: that.account
                    })
                    .then(response => {
                        that.$message({
                            type: 'success',
                            message: '修改答案成功'
                        });
                        that.handleRefresh();
                    });
            }
            if (newQu.question != old.question) {
                this.$axios
                    .post('/api/change_question', {
                        questionId: newQu.id,
                        newQuestion: newQu.question,
                        account: that.account
                    })
                    .then(response => {
                        if (response.data == '由于问题模板表产生的外键约束，修改失败!') {
                            that.$confirm('问题模板库中含有该问题的映射，此操作将同时更改问题模板库中的数据, 是否继续?', '提示', {
                                confirmButtonText: '确定',
                                cancelButtonText: '取消',
                                type: 'warning',
                                center: true
                            })
                                .then(() => {
                                    that.$axios
                                        .post('/api/insist_change_question', {
                                            questionId: newQu.id,
                                            newQuestion: newQu.question,
                                            account: that.account
                                        })
                                        .then(() => {
                                            that.$message({
                                                type: 'success',
                                                message: '修改问题成功'
                                            });
                                            that.handleRefresh();
                                        });
                                })
                                .catch(() => {
                                    that.$message({
                                        type: 'info',
                                        message: '已取消'
                                    });
                                });
                        } else {
                            that.$message({
                                type: 'success',
                                message: '修改问题成功'
                            });
                            that.handleRefresh();
                        }
                    });
            }
            this.complieVisible = false;
        },
        handleRefresh() {
            let that = this;
            this.$axios
                .post('/api/get_question', {
                    start: '1',
                    number: '10'
                })
                .then(response => {
                    console.log(response);
                    that.questionForm = response.data;
                });
            this.$axios.post('/api/count_question').then(response => {
                that.pageTotal = response.data;
            });
            this.searchContent = '';
        }
    },
    mounted() {
        let that = this;
        this.$axios
            .post('/api/get_question', {
                start: '1',
                number: '10'
            })
            .then(response => {
                console.log(response);
                that.questionForm = response.data;
            });
        this.$axios.post('/api/count_question').then(response => {
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