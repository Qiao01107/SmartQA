// pages/login/login.js
const { baseUrl } = require('../../utils/config.js');

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
    passwordType: true
  },

  eyeStatus: function() {
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

  test: function(){
    wx.reLaunch({
      url: '/pages/home/home'
    })
  },

  signup: function () {
    wx.navigateTo({
      url: '/pages/enroll/enroll'
    })
  },

  login: function () {
    var that = this;
    app.globalData.msgListd=[];
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
wx.request({
        url: baseUrl + '/wechat_login',
      data:{
        phone: that.data.username,
        password: that.data.password,
      },
      method:'POST',
      header: {
            "Content-Type": "application/json"
          },
          success: function (res) {
            console.log(res.data)
            if(res.data == 2)
            wx.showModal({
            title: '提示！',
            showCancel: false,
            content: '手机号或密码错误！',
            success: function (res) { }
            })
            else if(res.data == 1){
              wx.showModal({
                title: '提示！',
                showCancel: false,
                content: '用户已被禁用！',
                })
            }
            else if(res.data == 0){
              getApp().globalData.userInfo_detail = res.data.data
              wx.showModal({
              title: '用户指南',
              showCancel: false,
              content: '恭喜，登录成功！您可以在对话区域以文本或语音形式向机器人提出问题，根据回答您还可以选择收藏，个人中心可以查看历史记录和收藏，也可以选择反馈问题~祝您使用愉快',
              success: function (res){
                if (res.confirm) {
                  console.log('用户点击确定')
                } else if (res.cancel) {
                  console.log('用户点击取消')
                }
              },
              complete: function (res) {
                wx.reLaunch({
                  url: "/pages/home/home"
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
            



      // wx.request({
      //   url: getApp().globalData.server + '/API/Login/phone_login',
      //   data: {
      //     phone: that.data.username,
      //     password: that.data.password,
      //     user_id: getApp().globalData.userInfo_detail.user_id,
      //     shop_id: getApp().globalData.settings.shop_id,
      //     company_id: getApp().globalData.settings.company_id,
      //   },
      //   method: "POST",
      //   header: {
      //     "Content-Type": "application/x-www-form-urlencoded"
      //   },
      //   success: function (res) {
      //     console.log(res.data)
      //     if (res.data.error_no == 10012) {
      //       wx.showModal({
      //         title: '提示！',
      //         showCancel: false,
      //         content: '密码错误！',
      //         success: function (res) { }
      //       })
      //     }
      //     else if (res.data.error_no == 10008) {
      //       wx.showModal({
      //         title: '提示！',
      //         showCancel: false,
      //         content: '手机号不存在！',
      //         success: function (res) { }
      //       })
      //     }
      //     else if (res.data.error_no != 0) {
      //       wx.showModal({
      //         title: '哎呀～',
      //         content: '出错了呢！' + res.data.data.error_msg,
      //         success: function (res) {
      //           if (res.confirm) {
      //             console.log('用户点击确定')
      //           } else if (res.cancel) {
      //             console.log('用户点击取消')
      //           }
      //         }
      //       })
      //     }
      //     else if (res.data.error_no == 0) {
      //       getApp().globalData.userInfo_detail = res.data.data
      //       //console.log(getApp().globalData.userInfo_detail)
      //       wx.showModal({
      //         title: '恭喜！',
      //         showCancel: false,
      //         content: '登录成功',
      //         success: function (res) {
      //           if (res.confirm) {
      //             console.log('用户点击确定')
      //           } else if (res.cancel) {
      //             console.log('用户点击取消')
      //           }
      //         },
      //         complete: function (res) {
      //           wx.reLaunch({
      //             url: '../load/load'
      //           })
      //         }
      //       })
      //     }
      //   },
      //   fail: function (res) {
      //     wx.showModal({
      //       title: '哎呀～',
      //       content: '网络不在状态呢！',
      //       success: function (res) {
      //         if (res.confirm) {
      //           console.log('用户点击确定')
      //         } else if (res.cancel) {
      //           console.log('用户点击取消')
      //         }
      //       }
      //     })
      //   }
      // })
    }
  },
  usernameInput: function (e) {
    this.data.username = e.detail.value
    getApp().globalData.phone = e.detail.value
  },

  passwordInput: function (e) {
    // let value = e.detail.value.replace(/\D/g, '')
    // this.setData({
    //   password: value
    // })
    this.data.password = e.detail.value
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    wx.clearStorage()
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