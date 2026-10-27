package com.example.springboot.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link ImportPathGuard} 的路径穿越 / 任意文件访问防护测试。
 *
 * <p>这些用例直接在文件系统上构造“越界”场景，不依赖 Spring 上下文与数据库，
 * 因此可以在任何环境（包括 CI）稳定运行。
 */
class ImportPathGuardTest {

  @TempDir
  Path tempDir;

  private Path base;
  private Path outside;
  private ImportPathGuard guard;

  @BeforeEach
  void setUp() throws IOException {
    base = Files.createDirectories(tempDir.resolve("import-base"));
    outside = Files.createDirectories(tempDir.resolve("import-base-evil"));
    guard = new ImportPathGuard(base.toString(), 64 * 1024);
  }

  private Path write(Path dir, String name, String content) throws IOException {
    return Files.write(dir.resolve(name), content.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  @DisplayName("允许：基准目录内的相对路径 csv")
  void allowsRelativeCsvInsideBase() throws IOException {
    write(base, "entity.csv", "a,b,c\n");
    Path resolved = guard.resolve("entity.csv");
    assertTrue(Files.isRegularFile(resolved));
    assertEquals("entity.csv", resolved.getFileName().toString());
  }

  @Test
  @DisplayName("允许：指向基准目录内的绝对路径 csv")
  void allowsAbsoluteCsvInsideBase() throws IOException {
    Path file = write(base, "abs.csv", "x\n");
    assertEquals(file.toRealPath(), guard.resolve(file.toString()));
  }

  @Test
  @DisplayName("允许：带 . 与 .. 但仍在基准目录内")
  void allowsDotSegmentsStayingInside() throws IOException {
    Files.createDirectories(base.resolve("sub"));
    write(base, "sub/inner.csv", "x\n");
    Path resolved = guard.resolve("sub/../sub/inner.csv");
    assertEquals("inner.csv", resolved.getFileName().toString());
  }

  @Test
  @DisplayName("拒绝：..\\..\\ 路径穿越")
  void rejectsParentTraversal() throws IOException {
    write(outside, "secret.csv", "secret\n");
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> guard.resolve("../import-base-evil/secret.csv"));
    assertTrue(e.getMessage().contains("超出允许目录"), e.getMessage());
  }

  @Test
  @DisplayName("拒绝：多级 ../ 穿越到系统目录")
  void rejectsDeepTraversal() {
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
        () -> guard.resolve("../../../../../../../../etc/passwd"));
    assertTrue(e.getMessage().contains("超出允许目录"), e.getMessage());
  }

  @Test
  @DisplayName("拒绝：基准目录外的绝对路径（模拟读取 C:\\Windows\\win.ini）")
  void rejectsAbsoluteOutsideBase() {
    String systemFile = System.getenv("SystemRoot") == null
        ? "/etc/passwd"
        : System.getenv("SystemRoot") + "\\win.ini";
    Assumptions.assumeTrue(Files.exists(Path.of(systemFile)), "系统文件不存在，跳过");
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> guard.resolve(systemFile));
    assertTrue(e.getMessage().contains("超出允许目录"), e.getMessage());
  }

  @Test
  @DisplayName("拒绝：同名前缀的兄弟目录（import-base-evil 不能被当作 import-base）")
  void rejectsSiblingDirectoryWithSamePrefix() throws IOException {
    write(outside, "x.csv", "x\n");
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> guard.resolve(outside.resolve("x.csv").toString()));
    assertTrue(e.getMessage().contains("超出允许目录"), e.getMessage());
  }

  @Test
  @DisplayName("拒绝：非 .csv 后缀（例如直接读 application.yml）")
  void rejectsNonCsvSuffix() throws IOException {
    write(base, "application.yml", "password: x\n");
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> guard.resolve("application.yml"));
    assertTrue(e.getMessage().contains(".csv"), e.getMessage());
  }

  @Test
  @DisplayName("拒绝：不存在的文件")
  void rejectsMissingFile() {
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> guard.resolve("nope.csv"));
    assertTrue(e.getMessage().contains("不存在"), e.getMessage());
  }

  @Test
  @DisplayName("拒绝：目录本身")
  void rejectsDirectory() throws IOException {
    Files.createDirectories(base.resolve("dir.csv"));
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> guard.resolve("dir.csv"));
    assertTrue(e.getMessage().contains("普通文件"), e.getMessage());
  }

  @Test
  @DisplayName("拒绝：空路径与 NUL 字符")
  void rejectsBlankAndNul() {
    assertThrows(IllegalArgumentException.class, () -> guard.resolve(null));
    assertThrows(IllegalArgumentException.class, () -> guard.resolve("   "));
    assertThrows(IllegalArgumentException.class, () -> guard.resolve("a\0b.csv"));
  }

  @Test
  @DisplayName("拒绝：超过大小上限的文件")
  void rejectsOversizedFile() throws IOException {
    ImportPathGuard small = new ImportPathGuard(base.toString(), 16);
    write(base, "big.csv", "0123456789ABCDEFGHIJ\n");
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> small.resolve("big.csv"));
    assertTrue(e.getMessage().contains("上限"), e.getMessage());
  }

  @Test
  @DisplayName("拒绝：基准目录内的软链指向目录外")
  void rejectsSymlinkEscapingBase() throws IOException {
    Path target = write(outside, "outside.csv", "secret\n");
    Path link = base.resolve("link.csv");
    try {
      Files.createSymbolicLink(link, target);
    } catch (IOException | UnsupportedOperationException | SecurityException ex) {
      Assumptions.abort("当前环境不允许创建符号链接（Windows 需要开发者模式/管理员），跳过软链用例");
    }
    // 部分受限环境（含本项目的沙箱）createSymbolicLink 不报错但链接并不真正落地，
    // 此时无法构造软链场景，如实跳过而不是把用例改成永远通过。
    Assumptions.assumeTrue(Files.isSymbolicLink(link), "环境未能真正创建符号链接，跳过软链用例");

    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> guard.resolve("link.csv"));
    assertTrue(e.getMessage().contains("超出允许目录"), e.getMessage());
  }

  @Test
  @DisplayName("大小写：.CSV 后缀同样允许")
  void acceptsUpperCaseSuffix() throws IOException {
    write(base, "UPPER.CSV", "x\n");
    Path resolved = guard.resolve("UPPER.CSV");
    assertTrue(resolved.getFileName().toString().toUpperCase(Locale.ROOT).endsWith(".CSV"));
  }
}
