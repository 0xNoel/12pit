# 12pit

[![Release](https://img.shields.io/github/v/release/12src/12pit)](https://github.com/12src/12pit/releases/latest) [![License](https://img.shields.io/badge/license-GPL--3.0--or--later-blue)](LICENSE) [![Build](https://github.com/12src/12pit/actions/workflows/build.yml/badge.svg?branch=main&event=push)](https://github.com/12src/12pit/actions/workflows/build.yml?query=branch%3Amain+event%3Apush) [![Crowdin](https://badges.crowdin.net/12pit/localized.svg)](https://crowdin.com/project/12pit)

English | [简体中文](docs/zh-CN/README.md)

12pit is a lightweight, open-source Forge mod for The Pit on Minecraft 1.8.9. It includes a configuration interface and supports [OneConfig](https://github.com/Polyfrost/OneConfig).

We recommend using 12pit with [PolyPatcher](https://modrinth.com/mod/patcher) and [OptiFine](https://optifine.net/home) for better performance and smoother gameplay.

Join our [Discord](https://discord.gg/e9PRKMUenc) and visit `#developer` for help or development discussions.

## Requirements

Use JDK 21 to run Gradle. Compiling the mod requires a Java 8 toolchain. Gradle will find or download it automatically. The Minecraft client also runs on Java 8.

Node.js and npm are required for `assemble` and `runClient` because those tasks build the configuration interface. Java tests also require them. CI uses Node.js 24.

## Building

On Windows, run:

```powershell
.\gradlew.bat assemble
```

On Linux/macOS, run:

```sh
./gradlew assemble
```

The jar file is in `build/libs/`.

## Development

Use PolyPatcher and OptiFine during development. At a minimum, ensure compatibility with their default settings.

### IntelliJ IDEA

Import the project through Gradle and set the Gradle JVM to JDK 21. Use the Gradle tool window to run `assemble`, `check`, or `runClient`.

### VS Code

Install the recommended extensions when prompted. Press `Ctrl+Shift+B` to run `assemble`. Use the Command Palette to run `check` or `runClient` as Gradle tasks. On macOS, press `Cmd+Shift+B` to build.

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Contributing](docs/CONTRIBUTING.md)
- [Translating](docs/TRANSLATING.md)
- [Issues](docs/ISSUES.md)
- [Running, testing, and debugging](docs/DEBUGGING.md)

## Third-Party Notices

12pit includes the following third-party components:

- [SpongePowered Mixin](https://github.com/SpongePowered/Mixin), MIT. License text: [MIXIN-MIT.txt](src/main/resources/META-INF/third-party-notices/MIXIN-MIT.txt).
- [ASM](https://asm.ow2.io/), bundled with Mixin, BSD 3-Clause. License text: [ASM-BSD-3-CLAUSE.txt](src/main/resources/META-INF/third-party-notices/ASM-BSD-3-CLAUSE.txt).
- [Vue](https://vuejs.org/), MIT. License text: [VUE-MIT.txt](src/main/resources/META-INF/third-party-notices/VUE-MIT.txt).
- [Vite](https://vite.dev/), used for module preloading in the configuration interface, MIT. License text: [VITE-MIT.txt](src/main/resources/META-INF/third-party-notices/VITE-MIT.txt).
- [Rolldown](https://rolldown.rs/), used for module preloading in the configuration interface, MIT. License text: [ROLLDOWN-MIT.txt](src/main/resources/META-INF/third-party-notices/ROLLDOWN-MIT.txt).
- [Lucide](https://lucide.dev/), ISC, with Feather icons under MIT. License texts: [LUCIDE-ISC.txt](src/main/resources/META-INF/third-party-notices/LUCIDE-ISC.txt).

12pit is licensed under [GPL-3.0-or-later](LICENSE).
