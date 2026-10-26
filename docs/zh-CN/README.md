<!-- Source: README.md; Based on: b89c7e1ea68fd0183c6877fdbe32f36b5d89cb2b -->

# 12pit

[![Release](https://img.shields.io/github/v/release/12src/12pit)](https://github.com/12src/12pit/releases/latest) [![License](https://img.shields.io/badge/license-GPL--3.0--or--later-blue)](../../LICENSE) [![Build](https://github.com/12src/12pit/actions/workflows/build.yml/badge.svg?branch=main&event=push)](https://github.com/12src/12pit/actions/workflows/build.yml?query=branch%3Amain+event%3Apush) [![Crowdin](https://badges.crowdin.net/12pit/localized.svg)](https://crowdin.com/project/12pit)

[English](../../README.md) | 简体中文

12pit 是适用于 Minecraft 1.8.9 The Pit 的轻量级开源 Forge 模组，自带网页配置界面，同时支持 [OneConfig](https://github.com/Polyfrost/OneConfig)。

建议配合 [PolyPatcher](https://modrinth.com/mod/patcher) 和 [OptiFine](https://optifine.net/home) 一起使用，以获得更好的性能。

如果遇到问题或想交流开发，欢迎加入我们的 [Discord](https://discord.gg/e9PRKMUenc)，在 `#developer` 频道讨论。

## 环境要求

运行 Gradle 需要 JDK 21。模组本身与 Minecraft 1.8.9 一样运行在 Java 8 上，编译时 Gradle 会自动查找或下载 Java 8 工具链。

此外，`assemble` 和 `runClient` 任务会构建网页配置界面，因此还需要安装 Node.js 和 npm（CI 使用 Node.js 24）。

## 构建

Windows：

```powershell
.\gradlew.bat assemble
```

Linux 或 macOS：

```sh
./gradlew assemble
```

构建好的 jar 文件会输出到 `build/libs/`。

## 开发

开发时建议同时安装 PolyPatcher 和 OptiFine，至少保证在两者的默认设置下兼容。

### IntelliJ IDEA

通过 Gradle 导入项目，并将 Gradle JVM 设为 JDK 21。之后可以直接在 Gradle 工具窗口中运行 `assemble`、`check` 和 `runClient`。

### VS Code

按提示安装推荐扩展后，按 `Ctrl+Shift+B`（macOS 为 `Cmd+Shift+B`）即可运行 `assemble`，也可以在命令面板中运行 `check` 和 `runClient` 任务。

## 文档

- [架构](ARCHITECTURE.md)
- [贡献指南](CONTRIBUTING.md)
- [翻译指南](TRANSLATING.md)
- [议题](ISSUES.md)
- [运行与调试](DEBUGGING.md)

## 第三方声明

12pit 包含以下第三方开源组件：

- [SpongePowered Mixin](https://github.com/SpongePowered/Mixin)，MIT。许可证文本：[MIXIN-MIT.txt](../../src/main/resources/META-INF/third-party-notices/MIXIN-MIT.txt)。
- [ASM](https://asm.ow2.io/)，随 Mixin 打包，BSD 3-Clause。许可证文本：[ASM-BSD-3-CLAUSE.txt](../../src/main/resources/META-INF/third-party-notices/ASM-BSD-3-CLAUSE.txt)。
- [Vue](https://vuejs.org/)，MIT。许可证文本：[VUE-MIT.txt](../../src/main/resources/META-INF/third-party-notices/VUE-MIT.txt)。
- [Vite](https://vite.dev/)，用于配置界面的模块预加载，MIT。许可证文本：[VITE-MIT.txt](../../src/main/resources/META-INF/third-party-notices/VITE-MIT.txt)。
- [Rolldown](https://rolldown.rs/)，用于配置界面的模块预加载，MIT。许可证文本：[ROLLDOWN-MIT.txt](../../src/main/resources/META-INF/third-party-notices/ROLLDOWN-MIT.txt)。
- [Lucide](https://lucide.dev/)，ISC，包含 MIT 协议的 Feather 图标。许可证文本：[LUCIDE-ISC.txt](../../src/main/resources/META-INF/third-party-notices/LUCIDE-ISC.txt)。

12pit 基于 [GPL-3.0-or-later](../../LICENSE) 协议开源。
