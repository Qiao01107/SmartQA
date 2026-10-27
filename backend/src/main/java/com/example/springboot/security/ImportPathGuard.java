package com.example.springboot.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * 数据导入（{@code /import_data}）的路径校验器。
 *
 * <p>历史实现把请求体里的 {@code path} 直接交给 {@code new File(path)} 读取，等于给任何
 * 拿到超管令牌的人（以及令牌泄露后的攻击者）开了一个“读取服务器任意文件”的口子：
 * 可以读 {@code .env}、{@code application.yml}、{@code C:\Windows\win.ini} 等。
 *
 * <p>本类把可读取范围收敛到<b>一个固定目录</b>，只允许该目录内的 {@code .csv} 文件：
 * <ol>
 *   <li>拒绝空路径、含 NUL 的路径、无法解析的路径；</li>
 *   <li>相对路径一律相对于基准目录解析，{@code ..} 会被规范化，逃出基准目录即拒绝；</li>
 *   <li>绝对路径同样必须落在基准目录内（防止直接传 {@code C:\Windows\win.ini}）；</li>
 *   <li>用 {@link Path#toRealPath()} 解析符号链接后<b>再次</b>校验前缀，
 *       防止“目录内的软链指向目录外”这种绕过；</li>
 *   <li>必须是普通文件、后缀为 {@code .csv}、大小不超过上限。</li>
 * </ol>
 *
 * <p>校验失败一律抛 {@link IllegalArgumentException}，消息可直接回给调用方（不含服务器绝对路径细节以外的敏感信息）。
 */
public class ImportPathGuard {

  /** 默认单文件上限 5 MB，避免一次导入把内存/IO 打满。 */
  public static final long DEFAULT_MAX_BYTES = 5L * 1024 * 1024;

  private static final String ALLOWED_SUFFIX = ".csv";

  private final Path baseDir;
  private final long maxBytes;

  public ImportPathGuard(String baseDir, long maxBytes) {
    if (baseDir == null || baseDir.trim().isEmpty()) {
      throw new IllegalArgumentException("导入基准目录未配置");
    }
    this.baseDir = Paths.get(baseDir.trim()).toAbsolutePath().normalize();
    this.maxBytes = maxBytes > 0 ? maxBytes : DEFAULT_MAX_BYTES;
  }

  /** 基准目录（绝对、已规范化）。 */
  public Path baseDir() {
    return baseDir;
  }

  /**
   * 把请求里的路径解析成一个确定可安全读取的文件。
   *
   * @param rawPath 请求体里的 path（相对或绝对均可，但必须落在基准目录内）
   * @return 已解析符号链接的真实文件路径
   * @throws IllegalArgumentException 路径非法、越界、非 csv、不存在、超限
   */
  public Path resolve(String rawPath) {
    if (rawPath == null || rawPath.trim().isEmpty()) {
      throw new IllegalArgumentException("导入路径不能为空");
    }
    String cleaned = rawPath.trim();
    if (cleaned.indexOf('\0') >= 0) {
      throw new IllegalArgumentException("导入路径包含非法字符");
    }

    Path realBase = ensureBaseDir();

    Path candidate;
    try {
      Path raw = Paths.get(cleaned);
      // 绝对路径直接用；相对路径基于基准目录解析。normalize 会折叠掉 ".."。
      candidate = raw.isAbsolute() ? raw.normalize() : realBase.resolve(raw).normalize();
    } catch (InvalidPathException e) {
      throw new IllegalArgumentException("导入路径格式非法");
    }

    if (!candidate.startsWith(realBase)) {
      throw new IllegalArgumentException("导入路径超出允许目录：" + realBase);
    }
    // 跟随链接判断存在性：软链的真实目标随后由 toRealPath() + startsWith 校验，
    // 断链/目标在允许目录外都会被拒。这里若用 NOFOLLOW 只能判断“链接本身在不在”，
    // 无法判定目标是否越界，反而容易漏掉真正的逃逸检查。
    if (!Files.exists(candidate)) {
      throw new IllegalArgumentException("导入文件不存在");
    }

    Path real;
    try {
      // 解析软链，防止 base 内软链指向 base 外。
      real = candidate.toRealPath();
    } catch (IOException e) {
      throw new IllegalArgumentException("导入文件无法访问");
    }
    if (!real.startsWith(realBase)) {
      throw new IllegalArgumentException("导入路径超出允许目录：" + realBase);
    }
    if (!Files.isRegularFile(real)) {
      throw new IllegalArgumentException("导入路径必须是普通文件");
    }

    String fileName = real.getFileName().toString().toLowerCase(Locale.ROOT);
    if (!fileName.endsWith(ALLOWED_SUFFIX)) {
      throw new IllegalArgumentException("只允许导入 .csv 文件");
    }

    try {
      if (Files.size(real) > maxBytes) {
        throw new IllegalArgumentException("导入文件超过大小上限（" + maxBytes + " 字节）");
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("导入文件无法读取");
    }
    return real;
  }

  /** 确保基准目录存在，并返回其真实路径（用于前缀比较，避免软链/大小写差异导致的误判）。 */
  private Path ensureBaseDir() {
    try {
      if (!Files.isDirectory(baseDir)) {
        Files.createDirectories(baseDir);
      }
      return baseDir.toRealPath();
    } catch (IOException e) {
      // 目录建不出来时，退化为规范化路径继续校验（后续 exists/isRegularFile 仍会拦住非法访问）。
      return baseDir;
    }
  }
}
