// pages/enroll/enroll.js
const { baseUrl } = require('../../utils/config.js');

Page({

  /**
   * 页面的初始数据
   */
  data: {
    username: "",
    phonenumber: "",
    password: "",
    passwordack: "",
    profile_picture: "",
  },

  signin: function(e) {
    wx.navigateBack({
      delta: 1
    })
  },

  regist: function(e) {
    var that = this
    var myreg = /^(((13[0-9]{1})|(14[0-9]{1})|(15[0-9]{1})|(16[0-9]{1})|(19[0-9]{1})|(18[0-9]{1})|(17[0-9]{1}))+\d{8})$/;
    if (that.data.username == '') {
      wx.showModal({
        title: '提示！',
        content: '请输入用户名！',
        showCancel: false,
        success(res) {}
      })
    } else if (that.data.phonenumber == '') {
      wx.showModal({
        title: '提示！',
        content: '请输入手机号！',
        showCancel: false,
        success(res) {}
      })
    } else if (that.data.phonenumber.length != 11) {
      wx.showModal({
        title: '提示！',
        content: '手机号长度有误，请重新输入！',
        showCancel: false,
        success(res) {}
      })
    } else if (!myreg.test(that.data.phonenumber)) {
      wx.showModal({
        title: '提示！',
        content: '请输入正确的手机号码！',
        showCancel: false,
        success(res) {}
      })
    } else if (that.data.password == '') {
      wx.showModal({
        title: '提示！',
        content: '请输入密码！',
        showCancel: false,
        success(res) {}
      })
    } else if (that.data.password.length < 6) {
      wx.showModal({
        title: '提示！',
        content: '密码长度需要大于6位！',
        showCancel: false,
        success(res) {}
      })
    } else if (that.data.passwordack == '') {
      wx.showModal({
        title: '提示！',
        content: '请输入确认密码！',
        showCancel: false,
        success(res) {}
      })
    } else if (that.data.password!=that.data.passwordack ) {
      wx.showModal({
        title: '提示！',
        content: '两次输入密码不一致！',
        showCancel: false,
        success(res) {}
      })
    } else {
      console.log("userInfo", getApp().globalData.userInfo.avatarUrl);
      that.data.avatarUrl=getApp().globalData.userInfo.avatarUrl;
      wx.request({
        url: baseUrl + '/wechat_register',
      data:{
        wechatName: that.data.username,
        phone: that.data.phonenumber,
        profile_picture:getApp().globalData.userInfo.avatarUrl,
        password: that.data.password,
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
            content: '手机号已存在！',
            success: function (res) { }
            })
            else if(res.data == true){
              getApp().globalData.userInfo_detail = res.data.data
              wx.showModal({
              title: '恭喜！',
              showCancel: false,
              content: '注册成功',
              success: function (res){
                if (res.confirm) {
                  console.log('用户点击确定')
                } else if (res.cancel) {
                  console.log('用户点击取消')
                }
              },
              complete: function (res) {
                wx.navigateBack({
                  delta: 1
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

  usernameInput: function(e) {
    this.data.username = e.detail.value
  },

  phonenumberInput: function(e) {
    this.data.phonenumber = e.detail.value
  },

  passwordInput: function(e) {
    this.data.password = e.detail.value
  },

  passwordInputAck: function(e) {
    this.data.passwordack = e.detail.value
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function(options) {

  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function() {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function() {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function() {

  }
})