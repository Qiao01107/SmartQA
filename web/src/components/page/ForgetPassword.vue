<template>
    <div class="container">
        <div class="formas">
            <div v-if="step === 'request'">
                <el-form :model="form" class="formClass" label-position="left">
                    <el-form-item>
                        <div class="title">验证用户</div>
                    </el-form-item>
                    <el-form-item label="账号:" label-width="100px" style="margin-top: 30px">
                        <el-input v-model="form.account" style="width: 300px" placeholder="请输入账号"></el-input>
                    </el-form-item>
                    <el-form-item label="手机号:" label-width="100px" style="margin-top: 30px">
                        <el-input
                            v-model="form.phone"
                            style="width: 300px"
                            placeholder="请输入手机号"
                            maxlength="11"
                        ></el-input>
                    </el-form-item>
                    <div style="display: flex; justify-content: space-between; width: 100%">
                        <el-button style="margin-left: 30px; width: 100px; margin: 5px auto" @click="returnLogin()">返回</el-button>
                        <el-button
                            style="margin-left: 30px; width: 140px; margin: 5px auto"
                            type="info"
                            :loading="sending"
                            @click="sendCode()"
                            >发送验证码</el-button
                        >
                    </div>
                    <div class="tip">
                        验证码由服务端校验，不会在页面上显示；未配置短信服务时此功能不可用。
                    </div>
                </el-form>
            </div>
            <div v-else>
                <el-form :model="form" class="formClass" label-position="left">
                    <el-form-item>
                        <div class="title">重置密码</div>
                    </el-form-item>
                    <el-form-item label="验证码:" label-width="100px" style="margin-top: 30px">
                        <el-input v-model="form.code" style="width: 200px" placeholder="请输入短信验证码" maxlength="6"></el-input>
                    </el-form-item>
                    <el-form-item label="新密码:" label-width="100px" style="margin-top: 20px">
                        <el-input
                            v-model="form.newPassword"
                            type="password"
                            style="width: 300px"
                            show-password
                            placeholder="至少 8 位，含字母和数字"
                        ></el-input>
                    </el-form-item>
                    <el-form-item label="确认密码:" label-width="100px" style="margin-top: 20px">
                        <el-input
                            v-model="form.confirmPassword"
                            type="password"
                            style="width: 300px"
                            show-password
                            placeholder="请再次输入新密码"
                        ></el-input>
                    </el-form-item>
                    <div style="display: flex; justify-content: space-between; width: 100%">
                        <el-button style="margin-left: 30px; width: 100px; margin: 5px auto" @click="returnLogin()">返回</el-button>
                        <el-button
                            style="margin-left: 30px; width: 100px; margin: 5px auto"
                            type="info"
                            :loading="submitting"
                            @click="submitReset()"
                            >确认修改</el-button
                        >
                    </div>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script>
/**
 * 忘记密码 / 重置密码。
 *
 * 旧实现在前端比对验证码，且调用的是无需登录的 /change_password——
 * 任何人都能重置任意账号的密码。现在改为：
 *   1. /forget_password 让服务端生成并保存验证码（响应里绝不返回验证码）；
 *   2. /reset_password 由服务端校验验证码 + 手机号归属后重置密码。
 */
export default {
    name: 'ForgetPassword',
    data() {
        return {
            step: 'request',
            sending: false,
            submitting: false,
            form: {
                account: '',
                phone: '',
                code: '',
                newPassword: '',
                confirmPassword: ''
            }
        };
    },
    methods: {
        returnLogin() {
            this.$router.push('/login');
        },
        sendCode() {
            if (!this.form.account) {
                this.$message({ message: '请输入账号', type: 'error' });
                return;
            }
            if (!/^\d{11}$/.test(this.form.phone)) {
                this.$message({ message: '请输入正确的手机号', type: 'error' });
                return;
            }
            this.sending = true;
            this.$axios
                .post('/api/forget_password', {
                    account: this.form.account,
                    phoneNumber: this.form.phone
                })
                .then(response => {
                    const body = response.data || {};
                    if (body.success) {
                        this.$message({ message: body.message || '验证码已发送', type: 'success' });
                        this.step = 'reset';
                    } else {
                        this.$message({ message: body.message || '验证码发送失败', type: 'error' });
                    }
                })
                .catch(error => {
                    const body = error && error.response && error.response.data;
                    this.$message({ message: (body && body.message) || '请求失败，请稍后重试', type: 'error' });
                })
                .finally(() => {
                    this.sending = false;
                });
        },
        submitReset() {
            if (!this.form.code) {
                this.$message({ message: '请输入短信验证码', type: 'error' });
                return;
            }
            if (!this.form.newPassword || this.form.newPassword.length < 8) {
                this.$message({ message: '新密码至少 8 位', type: 'error' });
                return;
            }
            if (!/[A-Za-z]/.test(this.form.newPassword) || !/\d/.test(this.form.newPassword)) {
                this.$message({ message: '新密码需同时包含字母和数字', type: 'error' });
                return;
            }
            if (this.form.newPassword !== this.form.confirmPassword) {
                this.$message({ message: '两次输入的密码不一致', type: 'error' });
                return;
            }
            this.submitting = true;
            this.$axios
                .post('/api/reset_password', {
                    account: this.form.account,
                    phoneNumber: this.form.phone,
                    code: this.form.code,
                    newPassword: this.form.newPassword
                })
                .then(response => {
                    const body = response.data || {};
                    if (body.success) {
                        this.$message({ message: body.message || '密码重置成功', type: 'success' });
                        this.$router.push('/login');
                    } else {
                        this.$message({ message: body.message || '密码重置失败', type: 'error' });
                    }
                })
                .catch(error => {
                    const body = error && error.response && error.response.data;
                    this.$message({ message: (body && body.message) || '请求失败，请稍后重试', type: 'error' });
                })
                .finally(() => {
                    this.submitting = false;
                });
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
.tip {
    margin: 16px 30px 0;
    font-size: 12px;
    font-weight: 400;
    line-height: 18px;
    color: #666;
}
</style>
