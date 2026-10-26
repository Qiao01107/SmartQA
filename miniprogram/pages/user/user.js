// pages/user/my/my.js
const app = getApp()
Page({

  /**
   * 页面的初始数据
   */
  data: {

    userId: app.globalData.userId,
    userAccount: app.globalData.userAccount,
    userName: '',
    userIdentity: 1,
    userGender: 'm',
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {

  },

  openPage: function () {
    wx.navigateTo({
      url: '../history/history',
    })
  },
  openPage2: function () {
    wx.navigateTo({
      url: '../collection/collection',
    })
  },
  openPage3: function () {
    wx.navigateTo({
      url: '../changepw/changepw',
    })
  },
  openPage4: function () {
    wx.navigateTo({
      url: '../feedback/feedback',
    })
  },
  out: function () {
    wx.clearStorage(),
    wx.reLaunch({
      url: '../login/login',
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