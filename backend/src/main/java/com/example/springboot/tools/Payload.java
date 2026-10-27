package com.example.springboot.tools;

import java.util.Map;

/**
 * 请求体取值小工具。
 *
 * <p>统一处理 JSON 请求体里的可空字段，避免历史写法 {@code map.get("x").toString()}
 * 在字段缺失时抛 NPE 变成 500。所有方法都不会返回 null。
 */
public final class Payload {

  private Payload() {
  }

  /** 取字符串；缺失或 null 时返回空串。 */
  public static String text(Map<String, Object> map, String key) {
    if (map == null) {
      return "";
    }
    Object value = map.get(key);
    return value == null ? "" : String.valueOf(value).trim();
  }

  /** 取原始对象；缺失时返回 null。 */
  public static Object raw(Map<String, Object> map, String key) {
    return map == null ? null : map.get(key);
  }

  /** 取 int；缺失、非数字时返回 defaultValue。 */
  public static int intValue(Map<String, Object> map, String key, int defaultValue) {
    String text = text(map, key);
    if (text.isEmpty()) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(text);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
}
