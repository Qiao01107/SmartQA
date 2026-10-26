<template>
    <div>
        <el-row :gutter="20">
            <el-col :span="8">
                <el-card shadow="hover" class="mgb20" style="height: 252px">
                    <div class="user-info">
                        <img :src="getASD" class="user-avator" alt="" />
                        <div class="user-info-cont">
                            <div class="user-info-name">{{ name }}</div>
                            <div>{{ role }}</div>
                        </div>
                    </div>
                    <div class="user-info-list">
                        上次登录时间:<span>{{ endTime }}</span>
                    </div>
                </el-card>
                <div class="container">
                    <div v-for="index in imageForm" :key="index.id" @click="changeImg">
                        <div class="imageClass" v-if="index.id != getAFLN">
                            <img :src="index.imageUrl" alt="" style="width: 100%; height: 100%; border-radius: 50%" :id="index.id" />
                        </div>
                    </div>
                </div>
            </el-col>
            <el-col :span="16">
                <el-row :gutter="20" class="mgb20">
                    <el-col :span="8">
                        <el-card shadow="hover" :body-style="{ padding: '0px' }">
                            <div class="grid-content grid-con-1">
                                <i class="el-icon-lx-people grid-con-icon"></i>
                                <div class="grid-cont-right">
                                    <div class="grid-num">{{ users }}</div>
                                    <div>微信用户量</div>
                                </div>
                            </div>
                        </el-card>
                    </el-col>
                    <el-col :span="8">
                        <el-card shadow="hover" :body-style="{ padding: '0px' }">
                            <div class="grid-content grid-con-2">
                                <i class="el-icon-delete-location grid-con-icon"></i>
                                <div class="grid-cont-right">
                                    <div class="grid-num">{{ entityNumber }}</div>
                                    <div>实体数量</div>
                                </div>
                            </div>
                        </el-card>
                    </el-col>
                    <el-col :span="8">
                        <el-card shadow="hover" :body-style="{ padding: '0px' }">
                            <div class="grid-content grid-con-3">
                                <i class="el-icon-connection grid-con-icon"></i>
                                <div class="grid-cont-right">
                                    <div class="grid-num">{{ relationNumber }}</div>
                                    <div>关系数量</div>
                                </div>
                            </div>
                        </el-card>
                    </el-col>
                </el-row>
                <el-card shadow="hover" style="height: 403px">
                    <template #header>
                        <div class="clearfix">
                            <span>热搜榜</span>
                            <!-- <el-button style="float: right; padding: 3px 0" type="text">添加</el-button> -->
                        </div>
                    </template>
                    <el-table :data="todoList" :show-header="false" height="304" style="width: 100%; font-size: 14px">
                        <el-table-column>
                            <template #default="scope">
                                <div class="todo-item">问题: {{ scope.row.question }}</div>
                                <div class="todo-item">答案: {{ scope.row.reply }}</div>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script>
import store from '../../store';
export default {
    name: 'dashboard',
    data() {
        return {
            type: store.state.userInfo.adminType,
            name: store.state.userInfo.name,
            entityNumber: 0,
            wechatNumber: 0,
            timeDIN: '',
            users: null,
            endTime: '',
            relationNumber: 0,
            afln: '1',
            imageForm: [
                {
                    id: 0,
                    imageUrl: new URL('../../assets/img/0.jpg', import.meta.url).href
                },
                {
                    id: 1,
                    imageUrl: new URL('../../assets/img/1.jpg', import.meta.url).href
                },
                {
                    id: 2,
                    imageUrl: new URL('../../assets/img/2.jpg', import.meta.url).href
                },
                {
                    id: 3,
                    imageUrl: new URL('../../assets/img/3.jpg', import.meta.url).href
                },
                {
                    id: 4,
                    imageUrl: new URL('../../assets/img/4.jpg', import.meta.url).href
                },
                {
                    id: 5,
                    imageUrl: new URL('../../assets/img/5.jpg', import.meta.url).href
                },
                {
                    id: 6,
                    imageUrl: new URL('../../assets/img/6.jpg', import.meta.url).href
                },
                {
                    id: 7,
                    imageUrl: new URL('../../assets/img/7.jpg', import.meta.url).href
                },
                {
                    id: 8,
                    imageUrl: new URL('../../assets/img/8.jpg', import.meta.url).href
                },
                {
                    id: 9,
                    imageUrl: new URL('../../assets/img/9.jpg', import.meta.url).href
                },
                {
                    id: 10,
                    imageUrl: new URL('../../assets/img/10.jpg', import.meta.url).href
                },
                {
                    id: 11,
                    imageUrl: new URL('../../assets/img/11.jpg', import.meta.url).href
                },
                {
                    id: 12,
                    imageUrl: new URL('../../assets/img/12.jpg', import.meta.url).href
                }
            ],
            todoList: []
        };
    },
    computed: {
        role() {
            return this.type === '1' ? '超级管理员' : '普通用户';
        },
        getAFLN: {
            get() {
                return store.state.userInfo.head;
            }
        },
        getASD: {
            get() {
                return new URL('../../assets/img/' + store.state.userInfo.head + '.jpg', import.meta.url).href;
            }
        }
    },

    methods: {
        changeImg(e) {
            console.log(e);
            if (e.target.localName === 'img') {
                clearTimeout(this.timeDIN);
                this.timeDIN = setTimeout(() => {
                    store.commit('setHead', e.target.id);
                    this.$axios.post('/api/change_head', {
                        account: store.state.userInfo.account,
                        head: e.target.id
                    });
                }, 400);
            }
        }
    },
    mounted() {
        let that = this;
        this.$axios.post('/api/count_wechat_user').then(response => {
            that.users = response.data;
        });
        this.$axios.post('/api/count_entity').then(res => {
            that.$data.entityNumber = res.data;
        });
        this.$axios.post('/api/count_relation').then(res => {
            that.$data.relationNumber = res.data;
        });
        this.$axios
            .post('/api/last_login', {
                account: store.state.userInfo.account
            })
            .then(response => {
                that.endTime = response.data;
            });
        this.$axios.post('/api/hotSearch', {}).then(response => {
            that.todoList = response.data;
        });
    }
};
</script>


<style scoped>
.el-row {
    margin-bottom: 20px;
}
.container {
    width: 85%;
    height: 100%;
    background-color: rgb(255, 255, 255);
    display: flex;
    flex-wrap: wrap;
}
.imageClass {
    width: 60px;
    height: 60px;
    margin: 15px 12px;
}
.grid-content {
    display: flex;
    align-items: center;
    height: 100px;
}

.grid-cont-right {
    flex: 1;
    text-align: center;
    font-size: 14px;
    color: #999;
}

.grid-num {
    font-size: 30px;
    font-weight: bold;
}

.grid-con-icon {
    font-size: 50px;
    width: 100px;
    height: 100px;
    text-align: center;
    line-height: 100px;
    color: #fff;
}

.grid-con-1 .grid-con-icon {
    background: rgb(45, 140, 240);
}

.grid-con-1 .grid-num {
    color: rgb(45, 140, 240);
}

.grid-con-2 .grid-con-icon {
    background: rgb(100, 213, 114);
}

.grid-con-2 .grid-num {
    color: rgb(45, 140, 240);
}

.grid-con-3 .grid-con-icon {
    background: rgb(242, 94, 67);
}

.grid-con-3 .grid-num {
    color: rgb(242, 94, 67);
}

.user-info {
    display: flex;
    align-items: center;
    padding-bottom: 20px;
    border-bottom: 2px solid #ccc;
    margin-bottom: 20px;
}

.user-avator {
    width: 120px;
    height: 120px;
    border-radius: 50%;
}

.user-info-cont {
    padding-left: 50px;
    flex: 1;
    font-size: 14px;
    color: #999;
}
.imageClass img:hover {
    cursor: pointer;
}
.user-info-cont div:first-child {
    font-size: 30px;
    color: #222;
}

.user-info-list {
    font-size: 14px;
    color: #999;
    line-height: 25px;
}

.user-info-list span {
    margin-left: 70px;
}

.mgb20 {
    margin-bottom: 20px;
}

.todo-item {
    font-size: 14px;
}

.todo-item-del {
    text-decoration: line-through;
    color: #999;
}

.schart {
    width: 100%;
    height: 300px;
}
</style>
