// pages/user/history/history.js
const { baseUrl } = require('../../utils/config.js');

var app = getApp()
Page({

  /**
   * 页面的初始数据
   */
  data: {
    question:[],
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    var that = this;
    wx.request({
      url: baseUrl + '/wechat_chatting',
      data: {
        phone: getApp().globalData.phone
      },
      method:'POST',
      header: {
            "Content-Type": "application/json"
          },
      success: function (res) {
        that.setData({
          question: res.data
        })
      }
    })

  },

  objectTap: function (e) {
    console.log(e.currentTarget.dataset.question);
    var question =e.currentTarget.dataset.question;
    var answer;
    var that = this 
      wx.request({
      url: baseUrl + '/apart',
    data:{
      text: question,
    },
    method:'POST',
    header: {
          "Content-Type": "application/json"
        },
        success: function (res) {
          console.log(res.data);
          answer = res.data
          wx.navigateTo({
            url: '/pages/simplequestion/simplequestion?question='+ question + '&&answer=' + answer
          })
        }
      })
  },

  clean: function () {
    wx.showModal({
      title: '提示！',
      showCancel: true,
      content: '确认要清除历史记录吗？',
      success: function (res){
        if (res.confirm) {
          console.log('用户点击确定')
          wx.request({
            url: baseUrl + '/clearHistory',
            data: {
              phone: getApp().globalData.phone
            },
            method:'POST',
            header: {
                  "Content-Type": "application/json"
                },
            
            success: function (res) {
              wx.showModal({
              title: '完成！',
              showCancel: false,
              content: '已清除历史记录',
              complete: function (res) {
          }
          })
        }
      })
        } else if (res.cancel) {
          console.log('用户点击取消')
        }
      },
      complete: function (res) {
        wx.redirectTo({
          url: '../history/history',   
        })
      }
    })
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