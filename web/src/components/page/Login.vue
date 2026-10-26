<template>
    <div class="login-wrap">
        <div class="ms-login">
            <div class="ms-title">后台管理系统</div>
            <el-form :model="param" :rules="rules" ref="login" label-width="0px" class="ms-content">
                <el-form-item prop="username">
                    <el-input v-model="param.username" placeholder="username">
                        <template #prepend>
                            <el-button icon="el-icon-lx-people"></el-button>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input
                        type="password"
                        placeholder="password"
                        show-password="true"
                        v-model="param.password"
                        @keyup.enter="submitForm()"
                    >
                        <template #prepend>
                            <el-button icon="el-icon-lx-lock"></el-button>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item class="radio_flex">
                    <el-radio v-model="radio" value="0" class="typeClasss">普通管理员</el-radio>
                    <el-radio v-model="radio" value="1" class="typeClasss">超级管理员</el-radio>
                </el-form-item>
                <div class="login-btn">
                    <el-button type="primary" @click="submitForm()">登录</el-button>
                </div>
                <div class="forgetClass" @click="forgetForm()">忘记密码?</div>
            </el-form>
        </div>
    </div>
</template>

<script>
import store from '../../store';
export default {
    data: function() {
        return {
            param: {
                username: '',
                password: ''
            },
            radio: '1',
            rules: {
                username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
                password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
            }
        };
    },
    created() {},
    methods: {
        submitForm() {
            // if (this.$data.param.username == 'test' && this.$data.param.password == 'test') {
            //     console.log(this.GLOBAL);
            //     this.GLOBAL.token = '3123ashjsajkdasj';
            //     this.$router.replace('/UserManage');
            //     if (this.$data.radio == '1') {
            //         this.GLOBAL.userInfo.admin_type = '1'; //SuperAdmin
            //     } else {
            //         this.GLOBAL.userInfo.admin_type = '2';
            //     }
            // }
            let that = this;
            console.log(store);
            this.$axios
                .post('/api/web_login', {
                    account: that.param.username,
                    password: that.param.password,
                    type: that.radio
                })
                .then(response => {
                    console.log(response);
                    const body = response.data;
                    if (body && body.success === true && body.user) {
                        // 保存会话令牌（本次为本地会话标记，后续如需服务端鉴权可替换为真实 token）
                        store.commit('setToken', 'session-' + Date.now());
                        store.commit('setUserinfomation', body.user);

                        console.log(store.state.userInfo);
                        that.$message({
                            message: '登录成功',
                            type: 'success'
                        });

                        that.$router.push('/');
                    } else {
                        that.$message({
                            message: (body && body.message) || '登录失败',
                            type: 'error'
                        });
                    }
                })
                .catch(() => {
                    that.$message({
                        message: '登录失败，请检查账号密码',
                        type: 'error'
                    });
                });
        },
        forgetForm() {
            this.$router.replace('/forgetPassword');
        }
    }
};
</script>

<style scoped>
.login-wrap {
    position: relative;
    width: 100%;
    height: 100%;
    background-image: url(../../assets/img/login-bg.jpg);
    background-size: 100%;
}
.ms-title {
    width: 100%;
    line-height: 50px;
    text-align: center;
    font-size: 20px;
    color: #fff;
    border-bottom: 1px solid #ddd;
}
.ms-login {
    position: absolute;
    left: 50%;
    top: 50%;
    width: 350px;
    margin: -190px 0 0 -175px;
    border-radius: 5px;
    background: rgba(255, 255, 255, 0.068);
    overflow: hidden;
}
.ms-content {
    padding: 30px 30px;
}
.login-btn {
    text-align: center;
}
.typeClasss {
    color: rgb(77, 77, 77);
    font-weight: 550;
}
.forgetClass {
    font-size: 12px;
    text-align: right;
    color: rgb(77, 77, 77);
    font-weight: 600;
    cursor: pointer;
}
.forgetClass:hover {
    color: #0f76ca;
}
.radio_flex {
    display: flex;
    justify-content: between;
}
.login-btn button {
    width: 100%;
    height: 36px;
    margin-bottom: 10px;
}
.login-tips {
    font-size: 12px;
    line-height: 30px;
    color: #fff;
}
</style>