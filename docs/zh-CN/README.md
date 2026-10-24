<!-- Source: README.md; Based on: 5c8bce221aa767a7dad58c3d23b817d9c07395d9 -->

# 12pit

[![Release](https://img.shields.io/github/v/release/12src/12pit)](https://github.com/12src/12pit/releases/latest) [![License](https://img.shields.io/badge/license-GPL--3.0--or--later-blue)](../../LICENSE) [![Build](https://github.com/12src/12pit/actions/workflows/build.yml/badge.svg?branch=main&event=push)](https://github.com/12src/12pit/actions/workflows/build.yml?query=branch%3Amain+event%3Apush) [![Crowdin](https://badges.crowdin.net/12pit/localized.svg)](https://crowdin.com/project/12pit)

[English](../../README.md) | 简体中文

12pit 是一款用于 Minecraft 1.8.9 The Pit 的轻量开源 Forge 模组，提供配置界面，并支持 [OneConfig](https://github.com/Polyfrost/OneConfig)。

建议搭配 [PolyPatcher](https://modrinth.com/mod/patcher) 和 [OptiFine](https://optifine.net/home) 使用，改善性能和游戏体验。

欢迎加入我们的 [Discord](https://discord.gg/e9PRKMUenc)，在 `#developer` 频道求助或讨论开发问题。

## 环境要求

请使用 JDK 21 运行 Gradle。编译模组需要 Java 8 工具链，Gradle 会自动查找或下载。Minecraft 客户端也使用 Java 8。

运行 `assemble`、`runClient` 和相关 Java 测试都需要 Node.js 和 npm，用于构建配置界面。CI 使用 Node.js 24。

## 构建

在 Windows 上运行：

```powershell
.\gradlew.bat assemble
```

在 Linux/macOS 上运行：

```sh
./gradlew assemble
```

输出的 jar 文件位于 `build/libs/`。

## 开发

开发时也应使用 PolyPatcher 和 OptiFine，至少确保 12pit 兼容两者的默认配置。

### IntelliJ IDEA

通过 Gradle 导入项目，将 Gradle JVM 设为 JDK 21。在 Gradle 工具窗口中运行 `assemble`、`check` 或 `runClient`。

### VS Code

打开项目时，按提示安装推荐扩展。按 `Ctrl+Shift+B`（macOS 为 `Cmd+Shift+B`）运行 `assemble`。通过命令面板运行 `check` 或 `runClient` 对应的 Gradle 任务。

## 文档

- [架构](ARCHITECTURE.md)
- [贡献指南](CONTRIBUTING.md)
- [翻译指南](TRANSLATING.md)
- [议题](ISSUES.md)
- [运行、测试和调试](DEBUGGING.md)

## 第三方声明

12pit 使用了以下第三方开源组件：

- [SpongePowered Mixin](https://github.com/SpongePowered/Mixin)，MIT。许可证文本：[MIXIN-MIT.txt](../../src/main/resources/META-INF/third-party-notices/MIXIN-MIT.txt)。
- [ASM](https://asm.ow2.io/)，随 Mixin 打包，BSD 3-Clause。许可证文本：[ASM-BSD-3-CLAUSE.txt](../../src/main/resources/META-INF/third-party-notices/ASM-BSD-3-CLAUSE.txt)。
- [Vue](https://vuejs.org/)，MIT。许可证文本：[VUE-MIT.txt](../../src/main/resources/META-INF/third-party-notices/VUE-MIT.txt)。
- [Vite](https://vite.dev/)，用于配置界面的模块预加载，MIT。许可证文本：[VITE-MIT.txt](../../src/main/resources/META-INF/third-party-notices/VITE-MIT.txt)。
- [Rolldown](https://rolldown.rs/)，用于配置界面的模块预加载，MIT。许可证文本：[ROLLDOWN-MIT.txt](../../src/main/resources/META-INF/third-party-notices/ROLLDOWN-MIT.txt)。
- [Lucide](https://lucide.dev/)，ISC，包含采用 MIT 许可证的 Feather 图标。许可证文本：[LUCIDE-ISC.txt](../../src/main/resources/META-INF/third-party-notices/LUCIDE-ISC.txt)。

12pit 基于 [GPL-3.0-or-later](../../LICENSE) 协议开源。
