<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item> <i class="el-icon-lx-cascades"></i> neo4j图形界面 </el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="handle-box">
            <el-input
                @keyup.enter="handleRelation"
                v-model="entityContent"
                placeholder="请输入要查询的实体名称"
                class="handle-input mr10"
                style="width: 400px"
            >
                <template #prepend>
                    <el-select v-model="IsSelected" style="width: 100px" clearable="true">
                        <el-option v-for="(item, index) in select" :key="item" :value="item" :label="item" class="labelClass"></el-option>
                    </el-select>
                </template>
            </el-input>

            <el-input
                @keyup.enter="handleRelation"
                v-model="relationContent"
                placeholder="请输入要查询的关系名称"
                class="handle-input mr10"
                style="margin-left: 30px; width: 400px"
            >
                <template #prepend>
                    <el-select v-model="relationIsSelected" style="width: 100px" clearable="true">
                        <el-option v-for="(item, index) in relationSelect" :key="item" :value="item" :label="item" class="labelClass"></el-option>
                    </el-select>
                </template>
            </el-input>

            <el-button type="primary" icon="el-icon-search" @click="handleRelation">查找</el-button>
            <el-input-number v-model="num" :step="10" style="margin-left: 20px" :min="20" :max="200"></el-input-number>
        </div>
        <div class="graph-tip">
            图谱数据由后端只读接口提供（实体/关系表），浏览器不再直接连接 Neo4j，也不保存数据库密码。
        </div>
        <graph-view
            :nodes="graphNodes"
            :edges="graphEdges"
            height="500px"
            :loading="graphLoading"
            :error="graphError"
            empty-text="暂无可展示的图谱数据；请先在实体表/关系表中维护数据"
        />
    </div>
</template>
<script>
import GraphView from '../common/GraphView.vue';
import { loadGraph, filterGraph } from '../../utils/graphData';

export default {
    name: 'Neo4j',
    components: { GraphView },
    data() {
        return {
            relationContent: '',
            entityContent: '',
            num: 20,
            select: [],
            relationSelect: [],
            relationIsSelected: '',
            IsSelected: '',
            graphNodes: [],
            graphEdges: [],
            graphLoading: false,
            graphError: '',
            fullGraph: { nodes: [], edges: [] }
        };
    },
    methods: {
        loadLabels() {
            this.$axios
                .post('/api/get_entity_label')
                .then(res => {
                    this.select = res.data || [];
                })
                .catch(() => {
                    this.select = [];
                });
            this.$axios
                .post('/api/get_relation_label', {})
                .then(res => {
                    this.relationSelect = res.data || [];
                })
                .catch(() => {
                    this.relationSelect = [];
                });
        },
        reloadGraph() {
            this.graphLoading = true;
            this.graphError = '';
            loadGraph(this.$axios)
                .then(graph => {
                    this.fullGraph = graph;
                    this.applyFilter();
                })
                .catch(() => {
                    this.fullGraph = { nodes: [], edges: [] };
                    this.graphNodes = [];
                    this.graphEdges = [];
                    this.graphError = '图谱数据加载失败，请确认后端已启动并已登录。';
                })
                .then(() => {
                    this.graphLoading = false;
                });
        },
        applyFilter() {
            const graph = filterGraph(this.fullGraph, {
                entityLabel: this.IsSelected,
                entityName: this.entityContent,
                relationLabel: this.relationIsSelected,
                relationName: this.relationContent,
                limit: this.num
            });
            this.graphNodes = graph.nodes;
            this.graphEdges = graph.edges;
        },
        handleRelation() {
            this.applyFilter();
        },
        handleLabel() {
            this.applyFilter();
        }
    },
    mounted() {
        this.loadLabels();
        this.reloadGraph();
    }
};
</script>

<style scoped>
.selectClass {
    width: 70px;
}
.handle-input {
    width: 300px;
}
.labelClass {
    color: #011608;
}
.graph-tip {
    margin: 12px 0;
    color: #909399;
    font-size: 13px;
}
.red {
    color: #ff0000;
}
.mr10 {
    margin-right: 10px;
}
</style>
