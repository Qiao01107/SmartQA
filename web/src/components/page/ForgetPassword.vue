<template>
    <div class="container">
        <div class="formas">
            <div v-if="pd === true">
                <el-form :model="userInformation" class="formClass" label-position="left" ref="userInformation">
                    <el-form-item>
                        <div class="title">验证用户</div>
                    </el-form-item>
                    <el-form-item label="账号:" label-width="100px" style="margin-top: 30px">
                        <el-input v-model="userInformation.account" style="width: 300px" placeholder="请输入账号"></el-input>
                    </el-form-item>
                    <el-form-item label="手机号:" label-width="100px" style="margin-top: 30px">
                        <el-input
                            v-model="userInformation.phone"
                            style="width: 300px"
                            placeholder="请输入手机号"
                            oninput="value=value.replace(/[^\d]/g,'')"
                            maxlength="11"
                        ></el-input>
                    </el-form-item>
                    <el-form-item label="验证码:" label-width="100px" style="margin-top: 30px">
                        <el-input
                            v-model="userInformation.verification"
                            style="width: 200px"
                            placeholder="请输入验证码"
                            oninput="value=value.replace(/[^\d]/g,'')"
                            maxlength="6"
                        ></el-input>
                        <el-button
                            type="info"
                            @click="onSubmit()"
                            style="margin-left: 10px; background-color: rgba(53, 54, 54, 0.368)"
                            :disabled="wpd"
                            >发送验证码</el-button
                        >
                    </el-form-item>
                    <div style="display: flex; justify-content: space-between; width: 100%">
                        <el-button style="margin-left: 30px; width: 100px; margin: 5px auto" @click="returnLogin()">返回</el-button>
                        <el-button style="margin-left: 30px; width: 100px; margin: 5px auto" @click="confirmChangeAccount()" type="info"
                            >确认</el-button
                        >
                    </div>
                </el-form>
            </div>
            <div v-else>
                <el-form :model="userInformation" class="formClass" label-position="left" ref="userInformation">
                    <el-form-item>
                        <div class="title">重置密码</div>
                    </el-form-item>
                    <el-form-item label="新密码:" label-width="100px" style="margin-top: 30px">
                        <el-input
                            v-model="userInformation.password"
                            type="password"
                            style="width: 300px"
                            show-password="true"
                            placeholder="请输入新密码"
                        ></el-input>
                    </el-form-item>
                    <el-form-item label="新密码验证:" label-width="100px" style="margin-top: 30px">
                        <el-input
                            v-model="userInformation.newPassword"
                            type="password"
                            style="width: 300px"
                            show-password="true"
                            placeholder="请再次输入新密码"
                        ></el-input>
                    </el-form-item>
                    <div style="display: flex; justify-content: space-between; width: 100%">
                        <el-button style="margin-left: 30px; width: 100px; margin: 5px auto" @click="returnLogin()">返回</el-button>
                        <el-button style="margin-left: 30px; width: 100px; margin: 5px auto" @click="confirmChange()" type="info"
                            >确认修改</el-button
                        >
                    </div>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script>
export default {
    name: 'ForgetPassword',
    data() {
        return {
            userInformation: {
                account: '',
                phone: '',
                password: '',
                newPassword: '',
                verification: ''
            },
            verification: '',
            pd: true,
            wpd: false,
            cxk: false
        };
    },
    methods: {
        returnLogin() {
            this.$router.push('/Login');
        },
        onSubmit() {
            let that = this;
            if (this.userInformation.account === '') {
                this.$message({
                    message: '请输入账号',
                    type: 'error'
                });
            } else if (this.userInformation.phone.length !== 11) {
                this.$message({
                    message: '请输入正确的手机号',
                    type: 'error'
                });
            } else {
                this.$axios
                    .post('/api/forget_password', {
                        account: that.userInformation.account,
                        phoneNumber: that.userInformation.phone
                    })
                    .then(function(response) {
                        if (response.data == '账号与手机号不匹配!') {
                            that.$message({
                                message: '账号与手机号不匹配,请重新输入!',
                                type: 'error'
                            });
                            that.userInformation.account = '';
                            that.userInformation.phone = '';
                        } else {
                            that.$message({
                                message: '发送短信成功',
                                type: 'success'
                            });
                            console.log(response.data);
                            that.cxk = true;
                            that.verification = response.data;
                            that.wpd = true;
                        }
                    });
            }
        },
        confirmChangeAccount() {
            if (this.userInformation.account == '') {
                this.$message({
                    message: '请输入用户账号',
                    type: 'error'
                });
            } else if (this.userInformation.phone == '') {
                this.$message({
                    message: '请输入用户手机号',
                    type: 'error'
                });
            } else if (this.userInformation.phone.length !== 11) {
                this.$message({
                    message: '请输入正确的手机号',
                    type: 'error'
                });
            } else if (this.cxk == false) {
                this.$message({
                    message: '请发送验证码',
                    type: 'error'
                });
            } else if (
                this.verification == this.userInformation.verification &&
                this.userInformation.verification != null &&
                this.userInformation.verification != ''
            ) {
                this.userInformation.verification = null;
                this.pd = false;
            } else {
                this.$message({
                    message: '验证码错误',
                    type: 'error'
                });
                this.userInformation.verification = '';
            }
        },
        confirmChange() {
            let that = this;
            if (this.userInformation.password == '') {
                that.$message({
                    message: '请填写新密码',
                    type: 'error'
                });
            } else if (this.userInformation.newPassword == '') {
                that.$message({
                    message: '请再一次输入新密码',
                    type: 'error'
                });
            } else if (this.userInformation.newPassword != this.userInformation.password) {
                that.$message({
                    message: '两次密码不匹配，请重新输入',
                    type: 'error'
                });
                this.userInformation.password = '';
                this.userInformation.newPassword = '';
            } else {
                this.$axios
                    .post('/api/change_password', {
                        account: that.userInformation.account,
                        newPassword: that.userInformation.newPassword
                    })
                    .then(() => {
                        that.$message({
                            message: '修改密码成功',
                            type: 'success'
                        });
                        that.$router.push('/login');
                    });
            }
        }
    }
};
</script>
<style scoped>
body,
html,
div {
    margin: 0px;
    padding: 0px;
    border: 0px;
}
.formas {
    width: 520px;
    display: flex;
    flex-direction: row;
    justify-content: center;
    margin: 200px auto;
    border-radius: 5px;
    background: rgba(255, 255, 255, 0.198);
    overflow: hidden;
    padding-bottom: 30px;
}
.container {
    position: fixed;
    width: 100%;
    height: 100%;
    background-image: url(../../assets/img/login-bg.jpg);
    background-size: 100%;
}
.formClass {
    font-size: 20px;
    font-weight: 600;
    color: rgb(58, 58, 58);
}
.formClass el-input {
    width: 200px;
}
.title {
    text-align: center;
    font-size: 24px;
    margin-top: 10px;
    font-weight: 700;
}
</style>
