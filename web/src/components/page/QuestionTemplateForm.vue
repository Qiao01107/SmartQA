<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item> <i class="el-icon-lx-cascades"></i> 问题模板表管理 </el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="container">
            <div class="container-fluid">
                <el-input @keyup.enter="handleSearch" v-model="searchContent" placeholder="问题名称" class="input-with-select">
                    <template #prepend>
                        <el-select v-model="IsSelected" clearable="true">
                            <el-option value="1" label="模板" class="labelClass"></el-option>
                            <el-option value="2" label="问题" class="labelClass"></el-option>
                        </el-select>
                    </template>
                </el-input>
                <el-button type="primary" icon="el-icon-refresh" @click="handleRefresh" style="margin-left: 15px">刷新</el-button>
                <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
                <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增</el-button>
                <el-button type="danger" icon="el-icon-close" @click="handleDelete">删除</el-button>
            </div>
        </div>
        <el-table
            :data="questionTemplateForm"
            header-cell-class-name="table-header"
            :cell-style="{ 'text-align': 'center' }"
            :header-cell-style="{ 'text-align': 'center' }"
            @selection-change="selected"
            @select-all="selectAll"
            :total="pageTotal"
            @current-change="handlePageChange"
            @prev-click="handlePagePrev"
            @next-click="handlePageNext"
            ref="questionTem"
            class="table"
        >
            <el-table-column type="selection" width="75px"> </el-table-column>
            <el-table-column prop="id" label="id" width="50px"> </el-table-column>
            <el-table-column prop="questionTemplate" label="问题权值模板" width="500px"></el-table-column>
            <el-table-column prop="preciseQuestion" label="准确的问题" width="400px"></el-table-column>
            <el-table-column fixed="right" label="操作" width="150px">
                <template #default="scope">
                    <!-- <el-button @click="handleClick(scope.row)" type="text" size="small">查看</el-button> -->
                    <el-button type="text" size="small" @click="handleEdit(scope.row)" icon="el-icon-edit">编辑</el-button>
                </template>
            </el-table-column>
        </el-table>
        <el-dialog v-model="addVisible" title="添加模板问题" width="30%">
            <el-form ref="addEntity" :model="addQuestionTemplate">
                <el-form-item label="问题模板:" label-width="120px">
                    <el-input v-model="addQuestionTemplate.questionTemplate" />
                </el-form-item>
                <el-form-item label="准确问题:" label-width="120px">
                    <el-input v-model="addQuestionTemplate.preciseQuestion" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="addVisible = false">取 消</el-button>
                <el-button type="primary" @click="add">确 定</el-button>
            </template>
        </el-dialog>
        <el-dialog v-model="changeVisible" title="编辑模板问题" width="30%">
            <el-form ref="changeEntity" :model="changeQuestionTemplate">
                <el-form-item label="问题模板:" label-width="120px">
                    <el-input v-model="changeQuestionTemplate.questionTemplate" />
                </el-form-item>
                <el-form-item label="准确问题:" label-width="120px">
                    <el-input v-model="changeQuestionTemplate.preciseQuestion" disabled />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="changeVisible = false">取 消</el-button>
                <el-button type="primary" @click="changeTemplate">确 定</el-button>
            </template>
        </el-dialog>
    </div>
</template>
<script>
import Axios from 'axios';
import store from '../../store';
export default {
    name: 'QuestionTemplateForm',
    data() {
        return {
            addVisible: false,
            changeVisible: false,
            questionTemplateForm: [],
            oldTemplate: '',
            pageTotal: 32,
            searchContent: '',
            haveSelect: [],
            IsSelected: '',
            account: store.state.userInfo.account,
            changeQuestionTemplate: {},
            addQuestionTemplate: {
                questionTemplate: '',
                preciseQuestion: ''
            }
        };
    },
    methods: {
        handleSearch() {
            let that = this;
            console.log(this.IsSelected);
            this.$axios
                .post('/api/search_template', {
                    field: that.IsSelected.toString(),
                    inquire: that.searchContent
                })
                .then(response => {
                    that.questionTemplateForm = response.data;
                });
        },
        handleDelete() {
            let that = this;
            if (this.haveSelect[0] != undefined) {
                this.$confirm('确定删除该问题?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'success',
                    center: true
                })
                    .then(() => {
                        that.$axios
                            .post('/api/remove_template', {
                                questionTemplate: that.haveSelect[0].questionTemplate,
                                account: that.account
                            })
                            .then(response => {
                                that.$message({
                                    message: '删除数据成功',
                                    type: 'success'
                                });
                                that.handleRefresh();
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
        changeTemplate() {
            let that = this;
            this.$axios
                .post('/api/change_template', {
                    questionId: that.changeQuestionTemplate.id,
                    oldTemplate: that.oldTemplate,
                    newTemplate: that.changeQuestionTemplate.questionTemplate,
                    account: that.account
                })
                .then(() => {
                    that.$message({
                        message: '修改成功',
                        type: 'success'
                    });
                    that.changeVisible = false;
                    that.handleRefresh();
                });
        },
        handleEdit(e) {
            console.log(e);
            this.changeQuestionTemplate = e;
            this.oldTemplate = e.questionTemplate;
            this.changeVisible = true;
        },
        selected(selection) {
            if (selection.length > 1) {
                this.haveSelect[0] = selection[1];
                let k = selection.shift();
                this.$refs.questionTem.toggleRowSelection(k, false);
            } else {
                this.haveSelect[0] = selection[0];
            }
            // console.log(this.haveSelect);
        },
        selectAll() {
            this.$refs.questionTem.clearSelection();
            this.haveSelect = [];
        },
        handleAdd() {
            this.addVisible = true;
        },
        handleRefresh() {
            let that = this;
            this.$axios
                .post('/api/get_template', {
                    start: '1',
                    number: '10'
                })
                .then(response => {
                    console.log(response);
                    that.questionTemplateForm = response.data;
                });
            this.$axios.post('/api/count_template').then(response => {
                that.pageTotal = response.data;
            });
            this.searchContent = '';
            this.IsSelected = '';
        },
        add() {
            let that = this;
            if (this.addQuestionTemplate.preciseQuestion != '' && this.addQuestionTemplate.questionTemplate != '') {
                this.$axios
                    .post('/api/add_template', {
                        questionTemplate: that.addQuestionTemplate.questionTemplate,
                        preciseQuestion: that.addQuestionTemplate.preciseQuestion,
                        account: that.account
                    })
                    .then(response => {
                        if (response.data == true) {
                            that.$message({
                                message: '插入数据成功',
                                type: 'success'
                            });
                            that.addVisible = false;
                            that.addQuestionTemplate = {};
                            that.handleRefresh();
                        } else {
                            that.$message({
                                message: '插入数据失败，数据库已经存在',
                                type: 'danger'
                            });
                        }
                    });
            } else {
                this.$message({
                    message: '请填写必要信息',
                    type: 'danger'
                });
            }
        }
    },
    mounted() {
        let that = this;
        this.$axios
            .post('/api/get_template', {
                start: '1',
                number: '10'
            })
            .then(response => {
                console.log(response);
                that.questionTemplateForm = response.data;
            });
        this.$axios.post('/api/count_template').then(response => {
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