/**
 * 小程序统一请求封装。
 *
 * 与旧实现的区别：
 * 1. 自动在请求头带上 `Authorization: Bearer <token>`（令牌由后端 /wechat_login 签发）；
 * 2. 401（未登录 / 令牌过期 / 已注销）统一清理本地登录态并回到登录页，
 *    避免页面拿着空数据继续渲染；
 * 3. 403（角色不足或访问他人数据）弹出明确提示，不再静默失败；
 * 4. 其余行为与 wx.request 完全一致（url / data / method / header / success / fail / complete），
 *    因此各页面只需把 `wx.request(` 换成 `request(`。
 *
 * 安全说明：令牌存放在 wx.setStorageSync 中，仅用于向本项目后端表明身份；
 * 后端对所有私有接口都会用令牌里的手机号校验数据归属，改请求体里的 phone 无法越权。
 */
const TOKEN_KEY = 'smartqa_token';
const USER_KEY = 'smartqa_user';

function getToken() {
    return wx.getStorageSync(TOKEN_KEY) || '';
}

function setToken(token) {
    if (token) {
        wx.setStorageSync(TOKEN_KEY, token);
    } else {
        wx.removeStorageSync(TOKEN_KEY);
    }
}

function getUser() {
    return wx.getStorageSync(USER_KEY) || null;
}

function setUser(user) {
    if (user) {
        wx.setStorageSync(USER_KEY, user);
    }
}

function clearAuth() {
    wx.removeStorageSync(TOKEN_KEY);
    wx.removeStorageSync(USER_KEY);
}

function isPublicEntry(url) {
    return url.indexOf('/wechat_login') !== -1 || url.indexOf('/wechat_register') !== -1;
}

function request(options) {
    const opt = Object.assign({}, options);
    const header = Object.assign({ 'Content-Type': 'application/json' }, options.header || {});
    const token = getToken();
    if (token) {
        header.Authorization = 'Bearer ' + token;
    }
    opt.header = header;

    const success = options.success;
    const fail = options.fail;
    const url = options.url || '';

    opt.success = function (res) {
        if (res.statusCode === 401 && !isPublicEntry(url)) {
            clearAuth();
            const app = getApp();
            if (app && app.globalData) {
                app.globalData.phone = null;
                app.globalData.userInfo = null;
            }
            wx.showToast({ title: '登录已失效，请重新登录', icon: 'none' });
            setTimeout(function () {
                wx.reLaunch({ url: '/pages/login/login' });
            }, 800);
            if (fail) {
                fail(res);
            }
            return;
        }
        if (res.statusCode === 403) {
            const message = (res.data && res.data.message) || '当前账号无权访问该功能';
            wx.showModal({ title: '无权访问', content: message, showCancel: false });
        }
        if (success) {
            success(res);
        }
    };

    opt.fail = function (res) {
        if (fail) {
            fail(res);
        }
    };

    wx.request(opt);
}

/** 退出登录：先请求后端吊销令牌，再清理本地存储。 */
function logout(callback) {
    request({
        url: require('./config.js').baseUrl + '/wechat_logout',
        method: 'POST',
        data: {},
        complete: function () {
            clearAuth();
            if (callback) {
                callback();
            }
        }
    });
}

module.exports = {
    request,
    logout,
    getToken,
    setToken,
    getUser,
    setUser,
    clearAuth
};
