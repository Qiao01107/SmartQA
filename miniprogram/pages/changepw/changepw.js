// pages/changepw/changepw.js
const { baseUrl } = require('../../utils/config.js');

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
            wx.request({
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
                        console.log(res.data)
                        if(res.data == false)
                        wx.showModal({
                        title: '提示！',
                        showCancel: false,
                        content: '旧密码错误！',
                        success: function (res) { }
                        })
                        else if(res.data == true){
                          getApp().globalData.userInfo_detail = res.data.data
                          wx.showModal({
                          title: '恭喜！',
                          showCancel: false,
                          content: '修改密码成功',
                          success: function (res){
                            if (res.confirm) {
                              console.log('用户点击确定')
                            } else if (res.cancel) {
                              console.log('用户点击取消')
                            }
                          },
                          complete: function (res) {
                            wx.reLaunch({
                              url: "/pages/user/user"
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