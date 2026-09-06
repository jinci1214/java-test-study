# Java Test Study

一个用于学习 Java 自动化测试的示例项目。

## 技术栈

- Java 21
- Maven
- TestNG
- REST Assured
- WireMock
- Allure Report
- GitHub Actions

## 运行测试

```bash
mvn test
```

也可以运行指定的 Maven Profile，例如：

```bash
mvn -Pregression-test test
mvn -Pperformance-test test
mvn -Pstability-test test
```

## CI

推送到 `main`、创建 Pull Request，或在 GitHub Actions 页面手动运行工作流时，GitHub Actions 会执行测试并生成 Allure 报告产物。
