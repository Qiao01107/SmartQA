// pages/community/community.js
//ll
const { baseUrl } = require('../../utils/config.js');

const app = getApp();
var inputVal = '';
var msgList = getApp().globalData.msgListd;
var windowWidth = wx.getSystemInfoSync().windowWidth;
var windowHeight = wx.getSystemInfoSync().windowHeight;
// var keyHeight = 0;
var now_input = 'text';
//zj
const recorderManager = wx.getRecorderManager();
var wxst; // 语音websocket
var status = 0;  //  音频的状态
var iatResult = []; //  识别结果
const searchoptions = {
  duration: 60000,// 指定录音的时长，单位 ms
  sampleRate: 8000,// 采样率
  numberOfChannels: 1,// 录音通道数
  encodeBitRate: 48000,// 编码码率
  format: 'PCM',// 音频格式
  frameSize: 5,// 指定帧大小，单位 KB
}
// 初始化数据
function initData(that) {
  inputVal = '';
  msgList = [
    {
      speaker: 'customer',
      contentType: 'text',
      content: '你好',
      Collected: -1,
    },
    {
      speaker: 'server',
      contentType: 'text',
      content: '你好啊',
      Collected: -1,
    }
  ]
  that.setData({
    msgList,
    inputVal
  })
}

Page({

  // 页面的初始数据
  data: {
    question: '问题',
    answer: '回答',
    scrollHeight: windowHeight,
    // 小程序变量

    nowInput: 1, //当前输入法，1为键盘输入，2为语音输入
    isCollected: 0, //当前这一条有没有被收藏
    voiceResult: "语言识别的结果",
    hotnum: -1,
    sendornot: 0,
    hotyes: 0,
    hotques0:'热搜问题0',
    hotques1:'热搜问题1',
    hotques2:'热搜问题2',
    hotques3:'热搜问题3',
    hotansw0:'热搜回答0',
    hotansw1:'热搜回答1',
    hotansw2:'热搜回答2',
    hotansw3:'热搜回答3',
    collect:[],
  },

  question: function (e) {
    this.data.question = e.detail.value
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    initData(this);
    this.setData({
      cusHeadIcon: app.globalData.userInfo.avatarUrl,
    });
  },
  onHide:function(res){
    msgList = getApp().globalData.msgListd;
  },
  /**
   * 获取聚焦
   */
  focus: function (e) {
    // keyHeight = e.detail.height - 60;
    // this.setData({
    //   scrollHeight: (windowHeight - keyHeight) + 'px'
    // });
    this.setData({
      toView: 'msg-' + (msgList.length - 1),
      // inputBottom: keyHeight + 'px'
    })

    //计算msg高度
    // calScrollHeight(this, keyHeight);
  },
  //失去聚焦(软键盘消失)
  blur: function (e) {
    this.setData({
      // scrollHeight: '100vh',
      // inputBottom: 0
    })
    this.setData({
      toView: 'msg-' + (msgList.length - 1)
    })

  },
  /**
   * 发送点击监听
   */
  sendClick: function (e) {
    var that = this
    wx.request({ //发送问题接收回答

      url: baseUrl + '/apart',
      data: {
        text: that.data.question,
      },
      method: 'POST',
      header: {
        "Content-Type": "application/json"
      },

      success: function (res) {
          that.setData({
            answer: res.data
          })

          wx.request({ //如果回答返回成功，则开始判断该问答是否被收藏
            url: baseUrl + '/get_collectible',
            data: {
              phone: getApp().globalData.phone,
              question: that.data.question,
              reply: that.data.answer,
            },
            method: 'POST',
            header: {
              "Content-Type": "application/json"
            },

            success: function (res) { //如果是否收藏返回成功，则开始赋值
              if (res.data == true) {
                that.setData({
                  isCollected: 1,
                  collect: that.data.collect.concat(1, 1),
                })
              }
              else if (res.data == false) {
                that.setData({
                  isCollected: 0,
                  collect: that.data.collect.concat(0, 0),
                })
              }
              msgList.push({
                speaker: 'customer',
                contentType: 'text',
                content: e.detail.value,
                Collected: that.data.isCollected,
              },
                {
                  speaker: 'server',
                  contentType: 'text',
                  content: that.data.answer,
                  Collected: that.data.isCollected,
                }),
                inputVal = '';
              that.setData({
                msgList,
                inputVal,
              })

          }
          })
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

    wx.request({
      url: baseUrl + '/add_chatting',
      data: {
        phone: getApp().globalData.phone,
        speaker: 'customer',
        content: that.data.question,
      },
      method: 'POST',
      header: {
        "Content-Type": "application/json"
      },
      success: function (res) {
        console.log('加入聊天历史 ' + res.data)
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
  },

  //用于热搜模块
  renewhotnum() {
    this.setData({
      hotnum: -1,
      sendornot: 0
    })
  },

  //热搜实现

  sethotnum0() {
    console.log('NO1');
    this.setData({
      hotnum: 0
    })
    var that = this;
    
    wx.request({
      url: baseUrl + '/get_collectible',
      data: {
        phone: getApp().globalData.phone,
        question: that.data.hotques0,
        reply: that.data.hotansw0,
      },
      method: 'POST',
      header: {
        "Content-Type": "application/json"
      },
      success: function (res) {
        console.log("发送时判断是否已经被收藏：");
        console.log(res.data);
        if (res.data == true) {
          that.setData({
            isCollected: 1,
            collect: that.data.collect.concat(1,1),
          })
        }
        else if (res.data == false) {
          that.setData({
            isCollected: 0,
            collect: that.data.collect.concat(0,0),
          })
        }
        msgList.push({
          speaker: 'customer',
          contentType: 'text',
          content: that.data.hotques0,
          Collected:that.data.isCollected,
        },
        {
          speaker: 'server',
          contentType: 'text',
          content: that.data.hotansw0,
          Collected:that.data.isCollected,
        }
      )
      
      inputVal = '';
      that.setData({
        msgList,
        inputVal,
      });
        console.log('发送时iscollected的值为'+that.data.isCollected)
      }
    })
    
   
    wx.request({
      url: baseUrl + '/add_chatting',
      data: {
        phone: getApp().globalData.phone,
        speaker: 'customer',
        content: that.data.hotques0,
      },
      method: 'POST',
      header: {
        "Content-Type": "application/json"
      },
      success: function (res) {
        console.log('加入聊天历史 '+res.data)
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
  },


sethotnum1() {
    console.log('NO1');
    this.setData({
      hotnum: 1
    })
    var that = this;
    wx.request({
      url: baseUrl + '/get_collectible',
      data: {
        phone: getApp().globalData.phone,
        question: that.data.hotques1,
        reply: that.data.hotansw1,
      },
      method: 'POST',
      header: {
        "Content-Type": "application/json"
      },
      success: function (res) {
        console.log("发送时判断是否已经被收藏：");
        console.log(res.data);
        if (res.data == true) {
          that.setData({
            isCollected: 1,
            collect: that.data.collect.concat(1,1),
          })
        }
        else if (res.data == false) {
          that.setData({
            isCollected: 0,
            collect: that.data.collect.concat(0,0),
          })
        }
        msgList.push({
          speaker: 'customer',
          contentType: 'text',
          content: that.data.hotques1,
          Collected:that.data.isCollected,
        },
        {
          speaker: 'server',
          contentType: 'text',
          content: that.data.hotansw1,
          Collected:that.data.isCollected,
        }
      )
      inputVal = '';
      that.setData({
        msgList,
        inputVal,
      })
        console.log('发送时iscollected的值为'+that.data.isCollected)
      }
    })

  wx.request({
    url: baseUrl + '/add_chatting',
    data: {
      phone: getApp().globalData.phone,
      speaker: 'customer',
      content: that.data.hotques1,
    },
    method: 'POST',
    header: {
      "Content-Type": "application/json"
    },
    success: function (res) {
      console.log('加入聊天历史 '+res.data)
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
},


  sethotnum2() {
    console.log('NO1');
    this.setData({
      hotnum: 2
    })
    var that = this;
    wx.request({
      url: baseUrl + '/get_collectible',
      data: {
        phone: getApp().globalData.phone,
        question: that.data.hotques2,
        reply: that.data.hotansw2,
      },
      method: 'POST',
      header: {
        "Content-Type": "application/json"
      },
      success: function (res) {
        console.log("发送时判断是否已经被收藏：");
        console.log(res.data);
        if (res.data == true) {
          that.setData({
            isCollected: 1,
            collect: that.data.collect.concat(1,1),
          })
        }
        else if (res.data == false) {
          that.setData({
            isCollected: 0,
            collect: that.data.collect.concat(0,0),
          })
        }
        msgList.push({
          speaker: 'customer',
          contentType: 'text',
          content: that.data.hotques2,
          Collected:that.data.isCollected,
        },
        {
          speaker: 'server',
          contentType: 'text',
          content: that.data.hotansw2,
          Collected:that.data.isCollected,
        }
      )
      inputVal = '';
      that.setData({
        msgList,
        inputVal,
      });
        console.log('发送时iscollected的值为'+that.data.isCollected)
      }
    })
  wx.request({
    url: baseUrl + '/add_chatting',
    data: {
      phone: getApp().globalData.phone,
      speaker: 'customer',
      content: that.data.hotques2,
    },
    method: 'POST',
    header: {
      "Content-Type": "application/json"
    },
    success: function (res) {
      console.log('加入聊天历史 '+res.data)
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
},

sethotnum3() {
  console.log('NO1');
  this.setData({
    hotnum: 3
  })
  var that = this;
  wx.request({
    url: baseUrl + '/get_collectible',
    data: {
      phone: getApp().globalData.phone,
      question: that.data.hotques3,
      reply: that.data.hotansw3,
    },
    method: 'POST',
    header: {
      "Content-Type": "application/json"
    },
    success: function (res) {
      console.log("发送时判断是否已经被收藏：");
      console.log(res.data);
      if (res.data == true) {
        that.setData({
          isCollected: 1,
          collect: that.data.collect.concat(1,1),
        })
      }
      else if (res.data == false) {
        that.setData({
          isCollected: 0,
          collect: that.data.collect.concat(0,0),
        })
      }
      msgList.push({
        speaker: 'customer',
        contentType: 'text',
        content: that.data.hotques3,
        Collected:that.data.isCollected,
      },
      {
        speaker: 'server',
        contentType: 'text',
        content: that.data.hotansw3,
        Collected:that.data.isCollected,
      }
    )
    inputVal = '';
    that.setData({
      msgList,
      inputVal,
    });
      console.log('发送时iscollected的值为'+that.data.isCollected)
    }
  })
wx.request({
  url: baseUrl + '/add_chatting',
  data: {
    phone: getApp().globalData.phone,
    speaker: 'customer',
    content: that.data.hotques3,
  },
  method: 'POST',
  header: {
    "Content-Type": "application/json"
  },
  success: function (res) {
    console.log('加入聊天历史 '+res.data)
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
},

  // 下面是收藏部分
  doCollect(e) {
    let nownum = e.currentTarget.dataset.nownumber; //获取当前赞标签的序号（它和回答的序号相同）
    console.log('序号'+nownum);
    let quenum = nownum-1; //问题的序号
    let ansnum = nownum;   //回答的序号
    //获取此条问答是否被收藏成功
    var that=this
    wx.request({
      url: baseUrl + '/get_collectible',
      data: {
        phone: getApp().globalData.phone,
        question: msgList[quenum].content,
        reply: msgList[ansnum].content,
      },
      method: 'POST',
      header: {
        "Content-Type": "application/json"
      },
      success: function (res) {
        console.log("这里是docollect的判断函数1"),
        console.log("问题是否已经被收藏："+res.data);
        if (res.data == true) {
          msgList[quenum].Collected = 1 ;
          msgList[ansnum].Collected = 1 ;
          console.log('msg.collected现在的值为：'+msgList[ansnum].Collected);
        }
        else if (res.data == false) {
          msgList[quenum].Collected = 0 ;
          msgList[ansnum].Collected = 0 ;
          console.log('msg.collected现在的值为：'+msgList[ansnum].Collected);
        }
      }
    })
    if(msgList[nownum].Collected == 1){
      var that=this
      console.log("if ==1:接下来取消收藏")
          wx.request({
            url: baseUrl + '/remove_collectible',
            method: "POST",
            data: {
              phone: getApp().globalData.phone,
              question: msgList[quenum].content,
              reply: msgList[ansnum].content
            },
            header: {
              "Content-Type": "application/json"
            },
            success: function (res) {
              console.log("判断是否取消收藏：");
              console.log(res.data);
              if (res.data == true) {
                console.log("取消收藏成功！");
                msgList[quenum].Collected = 0
                msgList[ansnum].Collected = 0
                that.setData({
                  [`collect[${quenum}]`]:0,
                  [`collect[${ansnum}]`]:0
                })
                console.log('此时'+msgList[ansnum].Collected);
              }
              else if (res.data == false) {
                console.log("取消收藏失败！");
              }
            }
          })
    }
    else if(msgList[nownum].Collected == 0){ 
      console.log("if ==0:接下来进行收藏") 
      var that=this
      wx.request({
        url: baseUrl + '/add_collectible',
        data: {
          phone: getApp().globalData.phone,
          question: msgList[quenum].content,
          reply: msgList[ansnum].content
        },
        method: "POST",
        header: {
          "Content-Type": "application/json"
        },
        success: function (res) {
          console.log("判断是否成功收藏：");
          console.log(res.data);
          if (res.data == true) {
            console.log("收藏成功！");
            msgList[quenum].Collected = 1;
            msgList[ansnum].Collected = 1;
            that.setData({
              [`collect[${quenum}]`]:1,
              [`collect[${ansnum}]`]:1
            })
          }
          else if (res.data == false) {
            console.log('收藏失败。。。。');
          }
        }
      }) 
  }
},

  // 语音文字输入法切换
  inputchange() {
    console.log(this)
    if (this.data.nowInput == 1) {
      this.setData({
        nowInput: 2
      })
    }
    else if (this.data.nowInput == 2) {
      this.setData({
        nowInput: 1
      })
    }
  },
  resay(){
    this.setData({
      nowInput: 2
    })
  },
  // 后面全是语音识别功能  
  getUrl() {
    wx.request({
      url: baseUrl + '/url',//  服务器地址
      method: 'GET',
      header: {
        'content-type': 'application/json' //  默认值
      },
      success(result) {
        apiUrl = result.data;
        console.log(apiUrl);
      }
    })
  },

  start_say() {
    var that = this;

    wx.getSetting({//   查看用户有没有开启语音权限
      success(res) {
        if (res.authSetting['scope.record']) {
          wx.authorize({
            scope: 'scope.record',
            success() {
              var xfurl = "";
              console.log("开始获取录音权鉴"),

                wx.request({//  请求接口 获取讯飞语音鉴权
                  url: baseUrl + '/url',
                  // url:baseUrl + '/url',
                  method: "GET",
                  header: {
                    //'Content-Type': 'application/x-www-form-urlencoded'
                    'content-type': 'application/json' //   默认值
                  },
                  success: function (res) {
                    console.log("下面是res");
                    console.log(res);
                    if (
                      res.statusCode == "200" && res.data
                    ) {
                      xfurl = res.data;
                      console.log(xfurl);
                      console.log("开始访问讯飞");

                      wxst = wx.connectSocket({ //   开启websocket连接
                        url: xfurl,
                        method: 'GET',
                        header: {
                          'content-type': 'application/json'
                        },
                        success: function (res) {
                          that.setData({//  我这里是个遮罩层 开启他
                            shows: true,
                          })
                          console.log("获取录音权鉴成功");
                          recorderManager.start(searchoptions);//  开始录音
                          console.log("录音成功");
                        }
                      });
                    } else {

                      wx.showToast({
                        title: '获取语音鉴权失败1',
                        icon: 'none',
                        mask: true,
                        duration: 3000
                      })
                    }
                  },
                  fail: function () {
                    wx.showToast({
                      title: '获取语音鉴权失败2',
                      icon: 'none',
                      mask: true,
                      duration: 3000
                    })
                  }
                })
            },
            fail() {
              wx.showModal({  //====> 授权
                title: '提示',
                content: '您未授权录音，功能将无法使用',
                showCancel: true,
                confirmText: "授权",
                confirmColor: "#52a2d8",
                success: function (res) {
                  if (res.confirm) {
                    //确认则打开设置页面（重点）
                    wx.openSetting({
                      success: (res) => {
                        console.log(res.authSetting);
                        if (!res.authSetting['scope.record']) {
                          //未设置录音授权
                          console.log("未设置录音授权");
                          self.setData({
                            show: 0
                          })
                          wx.showModal({
                            title: '提示',
                            content: '您未授权录音，功能将无法使用',
                            showCancel: false,
                            success: function (res) {

                            },
                          })
                        } else {
                          //第二次才成功授权
                          console.log("设置录音授权成功");
                          // 配置一些东西打开,关闭
                          self.setData({
                            status: 2,
                            show: true,
                            hideText: false
                          })
                          // recorderManager.start(options);
                        }
                      },
                      fail: function () {
                        console.log("授权设置录音失败");
                        self.setData({
                          show: false
                        })
                      }
                    })
                  } else if (res.cancel) {
                    console.log("cancel");
                  }
                },
                fail: function () {
                  console.log("openfail");
                  self.setData({
                    show: 0
                  })
                }
              })

            }
          })
        } else {
          wx.showModal({
            title: '微信授权',
            content: '您当前未开启语音权限，请在右上角设置(···)中开启“录音功能”',
            showCancel: false,
            success(res) {
              if (res.confirm) {
                console.log('用户点击确定')
              }
            }
          })
        }
      }
    })
  },

  //  结束录音
  end_say() {
    //  结束录音
    console.log("结束录音");
    var that = this;
    recorderManager.stop();
    that.setData({//  关闭遮罩层
      shows: false,
    })
    this.setData({
      nowInput: 3
    })
  },

  onShow: function () {
   
    var that = this;
    wx.request({
      url: baseUrl + '/hotSearch',
      data: {
      },
      method: 'POST',
      header: {
        "Content-Type": "application/json"
      },
      success:function(res){
        console.log(res.data)
        that.setData({
        hotques0: res.data[0].question,
        hotansw0: res.data[0].reply,
        hotques1: res.data[1].question,
        hotansw1: res.data[1].reply,
        hotques2: res.data[2].question,
        hotansw2: res.data[2].reply,
        hotques3: res.data[3].question,
        hotansw3: res.data[3].reply,
        hotyes:1,
        })
      },
    })


    recorderManager.onStart(() => {//  开始录音时触发
      status = 0;
      iatResult = []
      console.log('recorder start');
      wxst.send({ status: status })
    });
    recorderManager.onError((res) => {//  错误回调
      console.log(res);
    });
    recorderManager.onStop((res) => {//  结束录音时触发
      console.log('recorder stop', res)
      status = 2;
      var sendsty = '{"data":{"status":2,"audio":"","format":"audio/L16;rate=8000","encoding":"raw"}}'
      wxst.send({
        data: sendsty
      })
    });
    recorderManager.onFrameRecorded((res) => {//  每帧触发
      const { frameBuffer } = res
      var int16Arr = new Int8Array(res.frameBuffer);
      const base64 = wx.arrayBufferToBase64(int16Arr)
      switch (status) {
        case 0:
          status = 1;
          var sendsty = '{"common":{"app_id":"f78aa034"},"business":{"language":"zh_cn","domain":"iat","accent":"mandarin","dwa":"wpgs","vad_eos":1000},"data":{"status":0,"format":"audio/L16;rate=8000","encoding":"raw","audio":"' + base64 + '"}}'
          wxst.send({
            data: sendsty
          })
          break;
        case 1:
          var sendsty = '{"data":{"status":1,"format":"audio/L16;rate=8000","encoding":"raw","audio":"' + base64 + '"}}'
          wxst.send({
            data: sendsty
          })
          break;
        default:
          console.log("default");
      }
    })
  },


  onLoad: function (options) {

    this.setData({
      nowInput: 1
    })
    var that = this;
    wx.onSocketOpen((res) => {//   websocket打开
      console.log('监听到 WebSocket 连接已打开' + res);
    })
    wx.onSocketError((err) => {//  连接失败
      console.log('websocket连接失败', err);
      wx.showToast({
        title: 'websocket连接失败',
        icon: 'none',
        duration: 2000,
        mask: false
      })
    })
    wx.onSocketMessage((res) => {// 接收返回值
      console.log("接受返回值")
      var data = JSON.parse(res.data)
      console.log(data)
      if (data.code != 0) {
        console.log("error code " + data.code + ", reason " + data.message)
        return
      }
      let str = ""
      if (data.data.status == 2) {// 最终识别结果
        data.data.status == 2 //说明数据全部返回完毕，可以关闭连接，释放资源
        wxst.close();
      } else {// 中间识别结果
      }
      iatResult[data.data.result.sn] = data.data.result
      if (data.data.result.pgs == 'rpl') {
        data.data.result.rg.forEach(i => {
          iatResult[i] = null
        })
      }
      iatResult.forEach(i => {
        if (i != null) {
          i.ws.forEach(j => {
            j.cw.forEach(k => {
              str += k.w
            })
          })
        }
      })
      that.setData({
        searchKey: str // 这个是中间的语音识别结果
      })
    })
    wx.onSocketClose((res) => {// WebSocket连接已关闭！
      var that = this;
      recorderManager.stop();
      that.setData({// 把之前开开的遮罩层关上
        shows: false,
      })
      var str = that.data.searchKey;
      console.log(str);
      str = str.replace(/\s*/g, "");// 去除空格
      console.log(str)
      this.setData({
        voiceResult: str// 这个是最后确定的语音识别结果
      })
      console.log('WebSocket连接已关闭！')
    })
  },

  send() {
    var that = this
    wx.request({
      url: baseUrl + '/apart',
      data: {
        text: that.data.voiceResult,
      },
      method: 'POST',
      header: {
        "Content-Type": "application/json"
      },
      success: function (res) {
        that.setData({
          answer: res.data
        })
        wx.request({
          url: baseUrl + '/get_collectible',
          data: {
            phone: getApp().globalData.phone,
            question: that.data.voiceResult,
            reply:  that.data.answer,
          },
          method: 'POST',
          header: {
            "Content-Type": "application/json"
          },
          success: function (res) {
            console.log("发送时判断是否已经被收藏：");
            console.log(res.data);
            if (res.data == true) {
              that.setData({
                isCollected: 1,
                collect: that.data.collect.concat(1,1),
              })
            }
            else if (res.data == false) {
              that.setData({
                isCollected: 0,
                collect: that.data.collect.concat(0,0),
              })
            }
            msgList.push({
              speaker: 'customer',
              contentType: 'text',
              content: that.data.voiceResult,
              Collected:that.data.isCollected,
            },
              {
                speaker: 'server',
                contentType: 'text',
                content: that.data.answer,
                Collected:that.data.isCollected,
              })
    
            inputVal = '';
            that.setData({
              msgList,
              inputVal,
            });
            that.setData({
              nowInput: 2
            });
          }
        })
        
      }
    })
   
    wx.request({
      url: baseUrl + '/add_chatting',
      data: {
        phone: getApp().globalData.phone,
        speaker: 'customer',
        content: that.data.question,
      },
      method: 'POST',
      header: {
        "Content-Type": "application/json"
      },
      success: function (res) {
        console.log('加入聊天历史 '+res.data)
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
  },

  /**
   * 退回上一页
   */
  toBackClick: function () {
    wx.navigateBack({})
  }

})