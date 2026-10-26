package com.example.springboot.service;

import okhttp3.HttpUrl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class UrlService {

  private static final String HOST_URL = "https://iat-api.xfyun.cn/v2/iat";

  private final String apiSecret;
  private final String apiKey;

  public UrlService(
      @Value("${XFYUN_API_SECRET:}") String apiSecret,
      @Value("${XFYUN_API_KEY:}") String apiKey) {
    this.apiSecret = apiSecret;
    this.apiKey = apiKey;
  }

  public String getUrl() {
    if (isBlank(apiKey) || isBlank(apiSecret)) {
      throw new IllegalStateException(
          "讯飞语音未配置：请设置环境变量 XFYUN_API_KEY 和 XFYUN_API_SECRET。"
              + "未配置时不会调用任何第三方账号。");
    }
    try {
      String authUrl = getAuthUrl(HOST_URL, apiKey, apiSecret);
      return authUrl.replace("http://", "ws://").replace("https://", "wss://");
    } catch (Exception e) {
      throw new IllegalStateException("讯飞语音鉴权 URL 生成失败，请检查密钥配置。", e);
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  // 这个 getAuthUrl() 方法是官网上给出来的，可以在官方文档中找到。
  private String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
    URL url = new URL(hostUrl);
    SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
    String date = format.format(new Date());
    StringBuilder builder = new StringBuilder("host: ").append(url.getHost()).append("\n").//
        append("date: ").append(date).append("\n").//
        append("GET ").append(url.getPath()).append(" HTTP/1.1");
    Charset charset = Charset.forName("UTF-8");
    Mac mac = Mac.getInstance("hmacsha256");
    SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
    mac.init(spec);
    byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
    String sha = Base64.getEncoder().encodeToString(hexDigits);

    String authorization = String.format(
        "api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256",
        "host date request-line", sha);
    HttpUrl httpUrl = HttpUrl.parse("https://" + url.getHost() + url.getPath()).newBuilder().//
        addQueryParameter("authorization",
            Base64.getEncoder().encodeToString(authorization.getBytes(charset))).//
        addQueryParameter("date", date).//
        addQueryParameter("host", url.getHost()).//
        build();
    return httpUrl.toString();
  }

}
