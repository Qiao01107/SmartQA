const { baseUrl } = require('../../utils/config.js');

Page({
    data: {
      question : [],
      currentData : 0,
      tabs: [
        {
          id: 0,
          value: "反馈中",
          isActive: true,
        },
        {
          id: 1,
          value: "已接受反馈",
          isActive: false,
        },
      ],
      //文本的值
      textValue: "",
      textValue2: "",
    },
    onLoad: function (options) {
      var that = this;
      wx.request({
        url: baseUrl + '/wechat_feedbackView',
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
    //单击自定义组件事件
    handleTabsItemChange(e) {
      //console.log(e)
      //获取索引值
      const { index } = e.detail;
      let { tabs } = this.data;
      tabs.forEach((v, i) =>
        i === index ? (v.isActive = true) : (v.isActive = false)
      );
      this.setData({
        tabs,
      });
    },
    //文本输入
    handleTextInput(e) {
      this.setData({
        textValue: e.detail.value,
      });
    },
    handleTextInput2(e) {
      this.setData({
        textValue2: e.detail.value,
      });
    },
    //获取当前滑块的index
  bindchange:function(e){
    const that  = this;
    that.setData({
      currentData: e.detail.current
    })
  },
  //点击切换，滑块index赋值
  checkCurrent:function(e){
    const that = this;
 
    if (that.data.currentData === e.target.dataset.current){
        return false;
    }else{
 
      that.setData({
        currentData: e.target.dataset.current
      })
    }
  },
    //提交按钮单击事件
    handlefrom(e) {
      var that =this
      //获取文本的值
      const { textValue, chooseimg } = this.data;
      //验证合法性
      if (!textValue.trim()) {
        //不合法
        wx.showToast({
          title: "输入不可以为空",
          icon: "none",
          mask: true,
        });
        return;
      }//获取文本的值
      const { textValue2, chooseimg2 } = this.data;
      //验证合法性
      if (!textValue2.trim()) {
        //不合法
        wx.showToast({
          title: "输入不可以为空",
          icon: "none",
          mask: true,
        });
        return;
      }
      wx.request({
        url: baseUrl + '/wechat_feedback',
      data:{
        phone: getApp().globalData.phone,
        question: that.data.textValue,
        opinion: that.data.textValue2,
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
            content: '反馈失败，请稍后再试！',
            success: function (res) { }
            })
            else if(res.data == true){
              getApp().globalData.userInfo_detail = res.data.data
              wx.showModal({
              title: '恭喜！',
              showCancel: false,
              content: '反馈成功',
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
      //显示正在上传中
      wx.showLoading({
        title: "正在上传中",
        mask: true,
      });
      {
        wx.hideLoading();
        console.log("提交了文本");
        wx.navigateBack({
          delta: 1,
        });
      }
    },
  });
