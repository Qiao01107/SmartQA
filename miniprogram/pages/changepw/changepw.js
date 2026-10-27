// pages/changepw/changepw.js
const { baseUrl } = require('../../utils/config.js');
const { request } = require('../../utils/request.js');

Page({

    /**
     * 页面的初始数据
     */
    data: {
        oldPassword:"",
        newPassword:"",
        confirmpassword:"",
    },

    change: function () {
        var that = this
        if (that.data.oldPassword == '') {
          wx.showModal({
            title: '提示！',
            showCancel: false,
            content: '请输入旧密码！',
            success: function (res) { }
          })
        }
        else if (that.data.newPassword == '') {
          wx.showModal({
            title: '提示！',
            showCancel: false,
            content: '请输入新密码！',
            success: function (res) { }
          })
        }
        else if (that.data.newPassword.length < 6) {
          wx.showModal({
            title: '提示！',
            showCancel: false,
            content: '新密码长度需要大于6！',
            success: function (res) { }
          })
        }
        else if (that.data.confirmpassword != that.data.newPassword) {
            wx.showModal({
              title: '提示！',
              showCancel: false,
              content: '两次密码输入不一致！',
              success: function (res) { }
            })
          }
          else {
            request({
                    url: baseUrl + '/wechat_change_password',
                  data:{
                    phone: getApp().globalData.phone,
                    oldPassword: that.data.oldPassword,
                    newPassword: that.data.newPassword,
                  },
                  method:'POST',
                  header: {
                        "Content-Type": "application/json"
                      },
                      success: function (res) {
                        var body = res.data || {};
                        if (body.success === false) {
                          wx.showModal({
                            title: '提示！',
                            showCancel: false,
                            content: body.message || '修改失败'
                          })
                        } else if (body.success === true) {
                          // 改密后后端已吊销此前所有令牌，必须重新登录。
                          wx.showModal({
                            title: '恭喜！',
                            showCancel: false,
                            content: body.message || '修改密码成功，请重新登录',
                            complete: function () {
                              require('../../utils/request.js').clearAuth();
                              getApp().globalData.phone = null;
                              getApp().globalData.userInfo = null;
                              wx.reLaunch({
                                url: "/pages/login/login"
                              })
                            }
                          })
                        }
                      },
                    fail: function (res) {
                      wx.showModal({
                        title: '哎呀～',
                        content: '网络不在状态呢！',
                        success: function (res) {
                          if (res.confirm) {
                            console.log('用户点击确定')
                          } else if (res.cancel) {
                            console.log('用户点击取消')
                          }
                        }
                      })
                    }
                  })
                }
    },



    oldpasswordInput: function (e) {
        this.data.oldPassword = e.detail.value
    },
    newpasswordInput: function (e) {
        this.data.newPassword = e.detail.value
    },
    confirmpasswordInput: function (e) {
        this.data.confirmpassword = e.detail.value
    },
    /**
     * 生命周期函数--监听页面加载
     */
    onLoad: function (options) {

    },

    /**
     * 生命周期函数--监听页面初次渲染完成
     */
    onReady: function () {

    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow: function () {

    },

    /**
     * 生命周期函数--监听页面隐藏
     */
    onHide: function () {

    },

    /**
     * 生命周期函数--监听页面卸载
     */
    onUnload: function () {

    },

    /**
     * 页面相关事件处理函数--监听用户下拉动作
     */
    onPullDownRefresh: function () {

    },

    /**
     * 页面上拉触底事件的处理函数
     */
    onReachBottom: function () {

    },

    /**
     * 用户点击右上角分享
     */
    onShareAppMessage: function () {

    }
})