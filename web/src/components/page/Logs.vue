<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item> <i class="el-icon-lx-cascades"></i> 日志 </el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="container">
            <div class="handle-box">
                <el-input
                    @keyup.enter="handleSearch"
                    v-model="searchContent"
                    placeholder="账号或姓名"
                    class="handle-input mr10"
                ></el-input>
                <el-button type="primary" icon="el-icon-refresh" @click="handleRefresh">刷新</el-button>
                <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
            </div>
            <el-table :data="logsForm" border class="table" ref="multipleTable" header-cell-class-name="table-header">
                <el-table-column prop="account" label="账号" width="150px" align="center"></el-table-column>
                <el-table-column prop="name" label="姓名" width="150px" align="center"></el-table-column>
                <el-table-column prop="operation" label="操作详情" width="400px" align="center"></el-table-column>
                <el-table-column prop="usedInterface" label="调用的接口" width="150px" align="center"></el-table-column>
                <el-table-column label="操作状态" width="80px" align="center">
                    <template #default="scope">
                        <el-tag :type="scope.row.operationStatus === 'true' ? 'success' : 'danger'">{{
                            scope.row.operationStatus === 'true' ? '正常' : '失败'
                        }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="operationTime" label="操作时间" align="center" width="255px"></el-table-column>
            </el-table>
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
export default {
    name: 'logs',
    data() {
        return {
            logsForm: [
                {
                    account: 'afln',
                    name: '朱佳达',
                    operation: '新增了一个名字为zjd的实体',
                    operationTime: '2021-12-17 14:58:20',
                    usedInterface: 'add_entity',
                    operationStatus: true
                }
            ],
            currentPage: 1,
            pageTotal: 10,
            searchContent: '',
            pd: true
        };
    },
    methods: {
        handlePagePrev() {
            if (this.currentPage != 1) {
                this.currentPage -= 1;
                this.handleRefresh();
            }
        },
        handlePageNext() {
            this.currentPage += 1;
            this.handleRefresh();
        },
        handlePageChange(e) {
            this.currentPage = e;
            this.handleRefresh();
        },
        handleSearch() {
            let that = this;
            this.$axios
                .post('/api/search_log', {
                    inquire: this.searchContent
                })
                .then(response => {
                    that.logsForm = response.data;
                    that.logsForm.forEach(i => {
                        i.operationTime = i.operationTime.substring(0, 19).replace('T', ' ');
                    });
                    that.pd = false;
                });
        },
        handleRefresh() {
            let that = this;
            this.$axios.post('/api/get_log', { start: this.currentPage * 10 - 9, number: 10 }).then(response => {
                that.logsForm = response.data;
                that.logsForm.forEach(i => {
                    i.operationTime = i.operationTime.substring(0, 19).replace('T', ' ');
                });
            });
            this.$axios.post('/api/count_log').then(response => {
                that.pageTotal = response.data;
            });
            this.pd = true;
        }
    },
    mounted() {
        let that = this;
        this.$axios.post('/api/get_log', { start: 1, number: 10 }).then(response => {
            that.logsForm = response.data;
            that.logsForm.forEach(i => {
                i.operationTime = i.operationTime.substring(0, 19).replace('T', ' ');
            });
        });
        this.$axios.post('/api/count_log').then(response => {
            that.pageTotal = response.data;
        });
        this.pd = true;
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