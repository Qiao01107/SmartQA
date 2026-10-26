<template>
    <div>
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item> <i class="el-icon-lx-cascades"></i> 个人信息 </el-breadcrumb-item>
            </el-breadcrumb>
            <div class="container">
                <div class="imgclass">
                    <img src="../../assets/userInforImage.gif" class="kli" />
                </div>
                <div class="formas">
                    <el-form :model="userInformation" class="formClass">
                        <el-form-item label="姓名:" label-width="80px">
                            <el-input v-model="userInformation.name" disabled></el-input>
                        </el-form-item>
                        <el-form-item label="账号:" label-width="80px" style="margin-top: 30px">
                            <el-input v-model="userInformation.account" disabled></el-input>
                        </el-form-item>
                        <el-form-item label="密码:" label-width="80px" style="margin-top: 30px">
                            <el-input v-model="userInformation.password"></el-input>
                        </el-form-item>
                        <el-form-item label="手机号:" label-width="80px" style="margin-top: 30px">
                            <el-input v-model="userInformation.phone" oninput="value=value.replace(/[^\d]/g,'')" maxlength="11"></el-input>
                        </el-form-item>
                        <el-form-item label="邮箱:" label-width="80px" style="margin-top: 30px">
                            <el-input v-model="userInformation.email" placeholder="请输入邮箱"></el-input>
                        </el-form-item>
                        <el-form-item label="类型:" label-width="80px" style="margin-top: 30px">
                            <el-radio-group disabled v-model="userInformation.adminType">
                                <el-radio value="0">普通管理员 </el-radio>
                                <el-radio value="1"> 超级管理员</el-radio>
                            </el-radio-group>
                        </el-form-item>
                        <div style="display: flex; justify-content: space-between; width: 100%">
                            <el-button style="margin-left: 30px" @click="cancel">恢复</el-button>
                            <el-button type="primary" @click="onSubmit">保存修改</el-button>
                        </div>
                    </el-form>
                </div>
            </div>
        </div>
    </div>
</template>
<script>
import store from '../../store';
export default {
    name: 'UserInformation',
    data() {
        return {
            userInformation: {}
        };
    },
    methods: {
        onSubmit() {
            if (this.userInformation.phone.length != 11) {
                this.$message({
                    message: '请输入正确的手机号',
                    type: 'error'
                });
            } else {
                let that = this;
                this.$axios
                    .post('/api/change_self_info', {
                        phone: this.userInformation.phone,
                        email: this.userInformation.email,
                        password: this.userInformation.password,
                        account: this.userInformation.account
                    })
                    .then(() => {
                        that.$message({
                            message: '修改信息成功',
                            type: 'success'
                        });
                        store.commit('setUserinfomation', that.userInformation);
                    });
            }
        },
        cancel() {
            this.userInformation = JSON.parse(JSON.stringify(store.state.userInfo));
        }
    },
    mounted() {
        this.userInformation = JSON.parse(JSON.stringify(store.state.userInfo));
    }
};
</script>
<style scoped>
.container {
    margin: 40px auto;
    width: 810px;
    display: flex;
}
.formas {
    width: 50%;
    display: flex;
    justify-items: column;
}
.formClass {
    font-size: 20px;
    font-weight: 600;
}
.kli {
    width: 80%;
    margin-left: 10%;
}
.imgclass {
    width: 50%;
}
</style>