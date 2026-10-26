<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item> <i class="el-icon-lx-cascades"></i> 关系表管理 </el-breadcrumb-item>
            </el-breadcrumb>
            <div class="container">
                <div class="container-fluid">
                    <el-input
                        @keyup.enter="handleSearch"
                        v-model="searchContent"
                        placeholder="关系名字或标签"
                        class="input-with-select"
                    >
                        <template #prepend>
                            <el-select v-model="IsSelected">
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
                :data="relationForm"
                header-cell-class-name="table-header"
                :cell-style="{ 'text-align': 'center' }"
                :header-cell-style="{ 'text-align': 'center' }"
                @selection-change="selected"
                @select-all="selectAll"
                class="table"
                ref="relationFormsa"
            >
                <el-table-column type="selection" width="55px" label="全选"> </el-table-column>
                <el-table-column prop="startEntityLabel" label="起始实体标签" width="210px"></el-table-column>
                <el-table-column prop="startEntityName" label="起始实体名字" width="210px"></el-table-column>
                <el-table-column prop="relationLabel" label="关系标签" width="210px"></el-table-column>
                <el-table-column prop="relationName" label="关系名称" width="210px"></el-table-column>
                <el-table-column prop="endEntityLabel" label="结束实体标签" width="210px"></el-table-column>
                <el-table-column prop="endEntityName" label="结束实体名称" width="210px"></el-table-column>
                <el-table-column
                    fixed="right"
                    label="操作"
                    width="120px
                "
                >
                    <template #default="scope">
                        <el-button @click="handleClick(scope.row)" type="text" size="small" icon="el-icon-view">查看</el-button>
                        <el-button type="text" size="small" @click="compile(scope.row)" icon="el-icon-edit">编辑</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <el-dialog v-model="watchVisible" width="50%" @close="watchClose" title="查看关系">
                <graph-view
                    :nodes="graphNodes"
                    :edges="graphEdges"
                    height="400px"
                    :loading="graphLoading"
                    :error="graphError"
                    empty-text="暂无可展示的关系图谱数据"
                />
            </el-dialog>
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
            <el-dialog v-model="addVisible" title="添加关系" width="30%">
                <el-form ref="addRelation" :model="addRelation">
                    <el-form-item label="起始实体标签" label-width="120px">
                        <el-select v-model="addRelation.startEntityLabel" style="width: 150px" clearable="true" placeholder="请选择">
                            <el-option v-for="item in entitySelect" :key="item" :label="item" :value="item"> </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="起始实体名字" label-width="120px">
                        <el-input v-model="addRelation.startEntityName" />
                    </el-form-item>
                    <el-form-item label="关系标签" label-width="120px">
                        <!-- <el-select v-model="addRelation.label" style="width: 150px" clearable="true" placeholder="请选择" filterable>
                            <el-option v-for="item in relationSelect" :key="item" :label="item" :value="item"> </el-option>
                        </el-select> -->
                        <el-input v-model="addRelation.label" />
                    </el-form-item>
                    <el-form-item label="关系名称" label-width="120px">
                        <el-input v-model="addRelation.relation" />
                    </el-form-item>
                    <el-form-item label="结束实体标签" label-width="120px">
                        <el-select v-model="addRelation.endEntityLabel" style="width: 150px" clearable="true" placeholder="请选择">
                            <el-option v-for="item in entitySelect" :key="item" :label="item" :value="item"> </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="结束关系名称" label-width="120px">
                        <el-input v-model="addRelation.endEntityName" />
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="addVisible = false">取 消</el-button>
                    <el-button type="primary" @click="add">确 定</el-button>
                </template>
            </el-dialog>
            <el-dialog v-model="compileVisible" title="编辑关系" width="30%">
                <el-form ref="oneRelation" :model="oneRelation">
                    <el-form-item label="起始实体标签" label-width="120px">
                        <el-input v-model="oneRelation.startEntityLabel" disabled></el-input>
                    </el-form-item>
                    <el-form-item label="起始实体名字" label-width="120px">
                        <el-input v-model="oneRelation.startEntityName" disabled />
                    </el-form-item>
                    <el-form-item label="关系标签" label-width="120px">
                        <!-- <el-select v-model="oneRelation.relationLabel" style="width: 150px" clearable="true" placeholder="请选择">
                            <el-option v-for="item in relationSelect" :key="item" :label="item" :value="item"> </el-option>
                        </el-select> -->
                        <el-input v-model="oneRelation.relationLabel" />
                    </el-form-item>
                    <el-form-item label="关系名称" label-width="120px">
                        <el-input v-model="oneRelation.relationName" />
                    </el-form-item>
                    <el-form-item label="结束实体标签" label-width="120px">
                        <el-input v-model="oneRelation.endEntityLabel" disabled></el-input>
                    </el-form-item>
                    <el-form-item label="结束关系名称" label-width="120px">
                        <el-input v-model="oneRelation.endEntityName" disabled />
                    </el-form-item>
                </el-form>
                <template #footer>
                    <el-button @click="compileVisible = false">取 消</el-button>
                    <el-button type="primary" @click="changeRelation()">确 定</el-button>
                </template>
            </el-dialog>
        </div>
    </div>
</template>
<script>
import GraphView from '../common/GraphView.vue';
import { loadGraph, relationSubgraph } from '../../utils/graphData';
import store from '../../store';
export default {
    name: 'RelationForm',
    components: { GraphView },
    data() {
        return {
            addVisible: false,
            watchVisible: false,
            pd: true,
            compileVisible: false,
            IsSelected: '', //查询的选择是标签还是实体
            current: 1,
            searchContent: '',
            haveSelected: [],
            graphNodes: [],
            graphEdges: [],
            graphLoading: false,
            graphError: '',
            relationSelect: [], //所有标签类型
            entitySelect: [], //所有实体标签
            pageTotal: 32, //当前页面有多少条信息。
            account: store.state.userInfo.account,
            oneRelation: {},
            addRelation: {
                startEntityName: '',
                startEntityLabel: '',
                startEntityId: '',
                endEntityName: '',
                endEntityLabel: '',
                endEntityId: '',
                relation: '',
                label: ''
            },
            relationForm: [
                {
                    startEntityName: '发际线和我作队',
                    startEntityLabel: 'troops',
                    startEntityId: 366,
                    endEntityName: '朱佳达，张真艇，林霖，廖雨欣，张婧，周燏',
                    endEntityLabel: 'group',
                    endEntityId: 2,
                    relationName: '的成员',
                    relationLabel: 'constitute'
                }
            ]
        };
    },
    methods: {
        handlePageChange(val) {
            console.log(val);
            this.current = val;
            let that = this;
            this.$axios
                .post('/api/get_relation', {
                    start: this.current,
                    number: '10'
                })
                .then(response => {
                    console.log(response);
                    that.relationForm = response.data;
                });
        },
        handlePagePrev() {
            this.current = this.current - 1;
        },
        handlePageNext() {
            this.current = this.current + 1;
        },
        handleRefresh() {
            let that = this;
            this.searchContent = '';
            this.haveSelected = [];
            this.IsSelected = '';
            this.$axios
                .post('/api/get_relation', {
                    start: '1',
                    number: '10'
                })
                .then(response => {
                    console.log(response);
                    that.relationForm = response.data;
                });
            this.$axios.post('/api/get_relation_label', {}).then(response => {
                console.log(response);
                that.relationSelect = response.data;
            });
            this.$axios.post('/api/count_relation', {}).then(response => {
                console.log(response);
                that.pageTotal = response.data;
            });
            this.$axios.post('/api/get_entity_label').then(response => {
                that.entitySelect = response.data;
            });
            that.pd = true;
        },
        handleAdd() {
            this.$data.addVisible = true;
            this.$data.addRelation = {
                startEntityName: '',
                startEntityLabel: '',
                startEntityId: '',
                endEntityName: '',
                endEntityLabel: '',
                endEntityId: '',
                relation: '',
                label: ''
            };
        },
        selected(selection) {
            if (selection.length > 1) {
                this.haveSelected[0] = selection[1];
                let k = selection.shift();
                this.$refs.relationFormsa.toggleRowSelection(k, false);
            } else {
                this.haveSelected[0] = selection[0];
            }
            console.log(this.haveSelected);
        },
        selectAll(selection) {
            this.$refs.relationFormsa.clearSelection();
        },
        handleDelete() {
            let that = this;
            if (this.haveSelected[0] != undefined) {
                that.$confirm('确定删除该关系?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'success',
                    center: true
                })
                    .then(() => {
                        that.$axios
                            .post('/api/remove_relation', {
                                startEntityId: that.haveSelected[0].startEntityId,
                                endEntityId: that.haveSelected[0].endEntityId,
                                label: that.haveSelected[0].relationLabel,
                                relation: that.haveSelected[0].relationName,
                                account: that.account
                            })
                            .then(() => {
                                that.$message({
                                    message: '删除关系成功',
                                    type: 'success'
                                });
                            })
                            .catch(() => {
                                that.$message({
                                    message: '删除关系失败',
                                    type: 'error'
                                });
                            });
                        that.handleRefresh();
                    })
                    .catch(() => {
                        that.$message({
                            message: '已取消',
                            type: 'info'
                        });
                    });
            }
        },
        changeRelation() {
            let that = this;
            this.compileVisible = false;
            console.log(this.oneRelation);
            this.$axios
                .post('/api/change_relation', {
                    startEntityId: that.oneRelation.startEntityId,
                    endEntityId: that.oneRelation.endEntityId,
                    oldLabel: that.oldRelation.relationLabel,
                    oldName: that.oldRelation.relationName,
                    newLabel: that.oneRelation.relationLabel,
                    newName: that.oneRelation.relationName,
                    account: that.account
                })
                .then(response => {
                    if (response.data == null) {
                        that.$message({
                            message: '修改关系成功',
                            type: 'success'
                        });
                    } else {
                        that.$message({
                            message: '修改后的关系数据库中已经存在',
                            type: 'error'
                        });
                    }
                });
        },
        add() {
            let that = this;
            console.log(this.addRelation);
            this.addVisible = false;
            this.$axios
                .post('/api/add_relation', {
                    startLabel: that.addRelation.startEntityLabel,
                    startName: that.addRelation.startEntityName,
                    endLabel: that.addRelation.endEntityLabel,
                    endName: that.addRelation.endEntityName,
                    label: that.addRelation.label,
                    relation: that.addRelation.relation,
                    account: that.account
                })
                .then(response => {
                    if (response.data != null) {
                        that.$message({
                            message: '插入数据成功',
                            type: 'success'
                        });
                    } else {
                        that.$message({
                            message: '数据库中已存在该关系',
                            type: 'danger'
                        });
                    }
                    that.handleRefresh();
                })
                .catch(response => {
                    that.$message({
                        message: '插入数据失败',
                        type: 'danger'
                    });
                });
        },
        handleSearch() {
            let that = this;
            this.$axios
                .post('/api/search_relation', {
                    field: that.IsSelected,
                    inquire: that.searchContent
                })
                .then(response => {
                    that.relationForm = response.data;
                    that.pd = false;
                    for (let i = 0; i < response.data.length; ++i) {
                        that.relationForm[i].label = response.data[i].relationLabel;
                        that.relationForm[i].relation = response.data[i].relationName;
                    }
                });
        },
        compile(e) {
            this.oneRelation = e;
            this.oldRelation = JSON.parse(JSON.stringify(e));
            console.log(e);
            this.compileVisible = true;
        },
        handleClick(res) {
            this.$data.watchVisible = true;
            this.$data.graphLoading = true;
            this.$data.graphError = '';
            this.$data.graphNodes = [];
            this.$data.graphEdges = [];
            loadGraph(this.$axios)
                .then(graph => {
                    const subgraph = relationSubgraph(graph, res, 20);
                    this.graphNodes = subgraph.nodes;
                    this.graphEdges = subgraph.edges;
                    if (subgraph.edges.length === 0) {
                        this.graphError = '未找到该关系的图谱数据，请确认关系表已同步。';
                    }
                })
                .catch(() => {
                    this.graphError = '关系图谱加载失败，请确认后端已启动并已登录。';
                })
                .then(() => {
                    this.graphLoading = false;
                });
        },
        watchClose() {
            this.$data.graphNodes = [];
            this.$data.graphEdges = [];
            this.$data.graphError = '';
        }
    },
    mounted() {
        let that = this;
        this.$axios
            .post('/api/get_relation', {
                start: '1',
                number: '10'
            })
            .then(response => {
                console.log(response);
                that.relationForm = response.data;
            });
        this.$axios.post('/api/get_relation_label', {}).then(response => {
            console.log(response);
            that.relationSelect = response.data;
        });
        this.$axios.post('/api/count_relation', {}).then(response => {
            console.log(response);
            that.pageTotal = response.data;
        });
        this.$axios.post('/api/get_entity_label').then(response => {
            that.entitySelect = response.data;
        });
        that.pd = true;
    }
};
</script>
<style scoped>
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
.el-select {
    width: 100px;
}
.handle-input {
    width: 300px;
    height: 32px;
}
.input-with-select {
    width: 300px;
}
.table {
    width: 100%;
    font-size: 14px;
}
.fixContainer {
    /* visibility: hidden */
    width: 500px;
    height: 400px;
    background-color: #a70404;
    position: fixed;
}
.table {
    width: 100%;
    font-size: 14px;
}
/* .changeCellStyle {
    background-color: #feffa9;
} */
</style>
