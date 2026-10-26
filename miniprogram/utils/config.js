/**
 * 小程序 API 基地址配置。
 *
 * 本地调试：
 * - 微信开发者工具：DEVTOOLS_BASE_URL
 * - 真机调试：DEVICE_BASE_URL（改成电脑局域网 IP，手机与电脑同一网络）
 *
 * 发布：
 * - 在微信小程序后台配置 HTTPS 合法域名后，把 PRODUCTION_BASE_URL 填成该域名。
 * - 未配置生产域名时，release 版本不会回退到原作者域名。
 */
const DEVTOOLS_BASE_URL = 'http://127.0.0.1:8081';
const DEVICE_BASE_URL = 'http://192.168.1.100:8081';
const PRODUCTION_BASE_URL = '';

function resolveBaseUrl() {
    let platform = '';
    let envVersion = 'develop';
    try {
        platform = wx.getSystemInfoSync().platform;
    } catch (e) {
        platform = '';
    }
    try {
        const account = wx.getAccountInfoSync ? wx.getAccountInfoSync() : null;
        if (account && account.miniProgram && account.miniProgram.envVersion) {
            envVersion = account.miniProgram.envVersion;
        }
    } catch (e) {
        envVersion = 'develop';
    }

    if (envVersion === 'release') {
        return PRODUCTION_BASE_URL;
    }
    if (envVersion === 'trial') {
        return DEVICE_BASE_URL;
    }
    return platform === 'devtools' ? DEVTOOLS_BASE_URL : DEVICE_BASE_URL;
}

const baseUrl = resolveBaseUrl();

module.exports = {
    baseUrl,
    DEVTOOLS_BASE_URL,
    DEVICE_BASE_URL,
    PRODUCTION_BASE_URL
};
