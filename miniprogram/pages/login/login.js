// pages/login/login.js
const { baseUrl } = require('../../utils/config.js');
const { request, setToken, setUser, clearAuth } = require('../../utils/request.js');

const app = getApp();
Page({

  /**
   * 页面的初始数据
   */
  data: {
    is_disabled: false,
    username: "",
    password: "",
    defaultType: true,
    passwordType: true,
    submitting: false
  },

  eyeStatus: function () {
    if (this.data.defaultType) {
      this.setData({
        passwordType: false,
        defaultType: false,
      })
    } else {
      this.setData({
        passwordType: true,
        defaultType: true,
      })
    }
  },

  signup: function () {
    wx.navigateTo({
      url: '/pages/enroll/enroll'
    })
  },

  login: function () {
    var that = this;
    app.globalData.msgListd = [];
    if (that.data.username == '') {
      wx.showModal({
        title: '提示！',
        showCancel: false,
        content: '请输入手机号！',
        success: function (res) { }
      })
    }
    else if (that.data.password == '') {
      wx.showModal({
        title: '提示！',
        showCancel: false,
        content: '请输入密码！',
        success: function (res) { }
      })
    }
    else {
      that.setData({ submitting: true });
      request({
        url: baseUrl + '/wechat_login',
        data: {
          phone: that.data.username,
          password: that.data.password,
        },
        method: 'POST',
        success: function (res) {
          var body = res.data || {};
          if (res.statusCode === 401) {
            wx.showModal({
              title: '提示！',
              showCancel: false,
              content: body.message || '手机号或密码错误！'
            })
          } else if (res.statusCode === 403) {
            wx.showModal({
              title: '提示！',
              showCancel: false,
              content: body.message || '用户已被禁用！'
            })
          } else if (res.statusCode === 200 && body.success && body.token) {
            // 保存后端签发的真实令牌与用户信息
            setToken(body.token);
            setUser(body.user);
            app.globalData.phone = body.user.phone;
            app.globalData.userInfo = body.user;

            wx.showModal({
              title: '用户指南',
              showCancel: false,
              content: '恭喜，登录成功！您可以在对话区域以文本或语音形式向机器人提出问题，根据回答您还可以选择收藏，个人中心可以查看历史记录和收藏，也可以选择反馈问题~祝您使用愉快',
              complete: function () {
                wx.reLaunch({
                  url: "/pages/home/home"
                })
              }
            })
          } else {
            wx.showModal({
              title: '提示！',
              showCancel: false,
              content: body.message || '登录失败，请稍后重试'
            })
          }
        },
        fail: function () {
          wx.showModal({
            title: '哎呀～',
            showCancel: false,
            content: '网络不在状态呢！'
          })
        },
        complete: function () {
          that.setData({ submitting: false });
        }
      })
    }
  },
  usernameInput: function (e) {
    this.data.username = e.detail.value
    getApp().globalData.phone = e.detail.value
  },

  passwordInput: function (e) {
    this.data.password = e.detail.value
  },

  onLoad: function (options) {
    // 进入登录页即清空本地登录态与页面缓存，避免残留旧令牌。
    clearAuth();
    app.globalData.msgListd = [];
    wx.removeStorageSync('logs');
  },

  onReady: function () {
  },

  onShow: function () {
  },

  onHide: function () {
  },

  onUnload: function () {
  },

  onPullDownRefresh: function () {
  },

  onReachBottom: function () {
  },

  onShareAppMessage: function () {
  }
})
