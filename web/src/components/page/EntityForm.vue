<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item> <i class="el-icon-lx-cascades"></i> 实体表管理 </el-breadcrumb-item>
            </el-breadcrumb>
            <div class="container">
                <div class="container-fluid">
                    <el-input
                        @keyup.enter="handleSearch"
                        v-model="searchContent"
                        placeholder="实体名字或标签"
                        class="input-with-select"
                    >
                        <template #prepend>
                            <el-select v-model="IsSelected" clearable="true">
                                <el-option value="1" label="标签" class="labelClass"></el-option>
                                <el-option value="2" label="名字" class="labelClass"></el-option>
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
                :data="entityForm"
                header-cell-class-name="table-header"
                :cell-style="{ 'text-align': 'center' }"
                :header-cell-style="{ 'text-align': 'center' }"
                @selection-change="selected"
                @select-all="selectAll"
                class="table"
                ref="entityForm"
            >
                <el-table-column type="selection" width="55px"> </el-table-column>
                <el-table-column prop="id" label="id" width="150px"></el-table-column>
                <el-table-column prop="label" label="实体标签" width="400px"></el-table-column>
                <el-table-column prop="name" label="实体名称" width="400px"></el-table-column>
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
                    @selection-change="selected"
                ></el-pagination>
            </div>
            <el-dialog v-model="watchVisible" width="50%" @close="watchClose" title="查看实体">
                <graph-view
                    :nodes="graphNodes"
                    :edges="graphEdges"
                    height="400px"
                    :loading="graphLoading"
                    :error="graphError"
                    empty-text="暂无可展示的实体图谱数据"
                />
            </el-dialog>
            <el-dialog v-model="addVisible" title="添加实体" width="30%">
                <el-form ref="addEntity" :model="addEntityFrom">
                    <el-form-item label="实体标签" label-width="120px">
                        <el-input v-model="addEntityFrom.label" />
                    </el-form-item>
                    <el-form-item label="实体名字" label-width="120px">
                        <el-input v-model="addEntityFrom.name" />
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="addVisible = false">取 消</el-button>
                    <el-button type="primary" @click="add">确 定</el-button>
                </template>
            </el-dialog>
            <el-dialog v-model="editVisible" title="编辑实体" width="30%">
                <el-form ref="addEntity" :model="addEntityFrom">
                    <el-form-item label="实体id" label-width="120px">
                        <el-input v-model="addEntityFrom.id" disabled />
                    </el-form-item>
                    <el-form-item label="实体标签" label-width="120px">
                        <el-input v-model="addEntityFrom.label" />
                    </el-form-item>
                    <el-form-item label="实体名字" label-width="120px">
                        <el-input v-model="addEntityFrom.name" />
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="editVisible = false">取 消</el-button>
                    <el-button type="primary" @click="edit">确 定</el-button>
                </template>
            </el-dialog>
        </div>
    </div>
</template>
<script>
import GraphView from '../common/GraphView.vue';
import { loadGraph, entityNeighborhood } from '../../utils/graphData';
import store from '../../store';
export default {
    name: 'EntityForm',
    components: { GraphView },
    data() {
        return {
            haveSelected: [],
            addVisible: false,
            editVisible: false,
            pd: true,
            IsSelected: '',
            currentPage: 1,
            searchContent: '',
            watchVisible: false,
            graphNodes: [],
            graphEdges: [],
            graphLoading: false,
            graphError: '',
            addEntityFrom: {
                id: 1,
                label: '',
                name: ''
            },
            pageTotal: 32,
            account: store.state.userInfo.account,
            entityForm: [
                {
                    id: 15,
                    label: 'Person',
                    name: 'Al Pacino'
                },
                {
                    id: 7,
                    label: 'Person',
                    name: 'Andy Wachowski'
                },
                {
                    id: 3,
                    label: 'Person',
                    name: '朱佳达'
                },
                {
                    id: 4,
                    label: 'Person',
                    name: '朱佳达'
                }
            ]
        };
    },
    methods: {
        handleAdd() {
            this.$data.addVisible = true;
            this.$data.addEntityFrom = {
                id: 1,
                label: '',
                name: ''
            };
        },
        handleRefresh() {
            let that = this;
            this.$axios.post('/api/get_entity', { start: 1, number: 10 }).then(res => {
                console.log(res);
                that.$data.entityForm = res.data;
            });
            this.$data.searchContent = '';
            this.$data.IsSelected = null;
            this.$axios.post('/api/count_entity').then(res => {
                console.log(res);
                that.$data.pageTotal = res.data;
            });
            this.$data.currentPage = 1;
            that.pd = true;
        },
        handleEdit(res) {
            console.log(res);
            this.$data.editVisible = true;
            this.$data.addEntityFrom = res;
        },
        add() {
            console.log(this.$data.addEntityFrom);
            this.$data.addVisible = false;
            let that = this;
            this.$axios
                .post('/api/add_entity', {
                    label: that.$data.addEntityFrom.label,
                    name: that.$data.addEntityFrom.name,
                    account: this.account
                })
                .then(res => {
                    if (res.data == -1) {
                        that.$message({
                            message: '插入数据成功',
                            type: 'success'
                        });
                    } else {
                        that.$message({
                            message: '插入数据失败,数据库中已存在该实体，实体Id为' + res.data,
                            type: 'error'
                        });
                    }
                });
        },
        edit() {
            console.log(this.$data.addEntityFrom);
            this.$data.editVisible = false;
            let that = this;
            this.$axios
                .post('/api/change_entity', {
                    label: that.$data.addEntityFrom.label,
                    name: that.$data.addEntityFrom.name,
                    id: that.$data.addEntityFrom.id,
                    account: this.account
                })
                .then(res => {
                    if (res.data == -1) {
                        that.$message({
                            message: '修改数据成功',
                            type: 'success'
                        });
                    } else {
                        that.$message({
                            message: '修改后的实体已在数据库中存在',
                            type: 'error'
                        });
                    }
                })
                .catch(err => {
                    console.log(err);
                    that.$message({
                        message: '修改数据失败',
                        type: 'danger'
                    });
                });
        },
        handleSearch() {
            console.log(this.$data.searchContent);
            console.log(this.$data.IsSelected);
            let that = this;
            this.$axios
                .post('/api/search_entity', { field: that.$data.IsSelected, inquire: that.$data.searchContent })
                .then(res => {
                    console.log(res);
                    that.$data.entityForm = res.data;
                    that.pd = false;
                })
                .catch(err => {
                    console.log(err);
                });
        },
        handleClick(res) {
            this.$data.watchVisible = true;
            this.$data.graphLoading = true;
            this.$data.graphError = '';
            this.$data.graphNodes = [];
            this.$data.graphEdges = [];
            loadGraph(this.$axios)
                .then(graph => {
                    const subgraph = entityNeighborhood(graph, res.id, 200);
                    this.graphNodes = subgraph.nodes;
                    this.graphEdges = subgraph.edges;
                    if (subgraph.nodes.length === 0) {
                        this.graphError = '未找到该实体对应的图谱数据，请确认实体表已同步。';
                    }
                })
                .catch(() => {
                    this.graphError = '实体图谱加载失败，请确认后端已启动并已登录。';
                })
                .then(() => {
                    this.graphLoading = false;
                });
        },
        handlePageChange(e) {
            this.$data.currentPage = e;
            let that = this;
            this.$axios.post('/api/get_entity', { start: that.$data.currentPage * 10 - 9, number: 10 }).then(res => {
                console.log(res);
                that.$data.entityForm = res.data;
            });
        },
        handlePageNext() {
            console.log(this.$data.currentPage);
            this.$data.currentPage += 1;
            let that = this;
            this.$axios.post('/api/get_entity', { start: that.$data.currentPage * 10 - 9, number: 10 }).then(res => {
                console.log(res);
                that.$data.entityForm = res.data;
            });
        },
        handlePagePrev() {
            console.log(this.$data.currentPage);
            this.$data.currentPage -= 1;
            let that = this;
            this.$axios.post('/api/get_entity', { start: that.$data.currentPage * 10 - 9, number: 10 }).then(res => {
                console.log(res);
                that.$data.entityForm = res.data;
            });
        },
        handleDelete() {
            let that = this;
            if (this.haveSelected[0] != undefined) {
                this.$confirm('确定删除该实体?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'success',
                    center: true
                })
                    .then(() => {
                        that.$axios.post('/api/remove_entity', { id: that.$data.haveSelected[0].id, account: that.account }).then(res => {
                            if (res.data == 0) {
                                that.$message({
                                    message: '删除数据成功',
                                    type: 'success'
                                });
                                that.handleRefresh();
                            } else {
                                that.$confirm(`删除数据失败,该实体存在${res.data}条关系,请确认是否继续删除?`, '提示', {
                                    confirmButtonText: '确定',
                                    cancelButtonText: '取消',
                                    type: 'warning',
                                    center: true
                                })
                                    .then(() => {
                                        that.$axios
                                            .post('/api/insist_remove_entity', {
                                                account: that.account,
                                                id: that.$data.haveSelected[0].id
                                            })
                                            .then(() => {
                                                that.$message({
                                                    type: 'success',
                                                    message: '删除实体成功'
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
        watchClose() {
            this.$data.graphNodes = [];
            this.$data.graphEdges = [];
            this.$data.graphError = '';
        },
        selected(selection) {
            if (selection.length > 1) {
                this.haveSelected[0] = selection[1];
                let k = selection.shift();
                this.$refs.entityForm.toggleRowSelection(k, false);
            } else {
                this.haveSelected[0] = selection[0];
            }
            //console.log(selection);
        },
        selectAll(selection) {
            this.haveSelected = [];
            this.$refs.entityForm.clearSelection();
        }
    },
    mounted() {
        // this.$data.entityForm;
        console.log(this);
        let that = this;
        this.$axios.post('/api/get_entity', { start: that.$data.currentPage * 10 - 9, number: 10 }).then(res => {
            console.log(res);
            that.$data.entityForm = res.data;
        });
        this.$axios.post('/api/count_entity').then(res => {
            that.$data.pageTotal = res.data;
        });
        that.pd = true;
    }
};
</script>
<style scoped >
.container {
    display: flex;
    flex-direction: column;
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
