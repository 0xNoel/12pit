<!-- Source: docs/DEBUGGING.md; Based on: 5c8bce221aa767a7dad58c3d23b817d9c07395d9 -->

# 运行和调试 12pit

## 环境设置

[README 环境要求](README.md#环境要求)列出了 JDK 和 Node.js 的版本。

## 运行开发客户端

在 Windows 上运行：

```powershell
.\gradlew.bat runClient
```

在 Linux 或 macOS 上运行：

```sh
./gradlew runClient
```

开发客户端的游戏目录是 `run/`。在 `run/logs/latest.log` 中查看客户端错误和配置界面地址。

开发环境集成了 [DevAuth](https://github.com/DJtheRedstoner/DevAuth)。使用正版账号测试时，启用 `devauth.enabled` JVM 属性，并按 [DevAuth 配置指南](https://github.com/DJtheRedstoner/DevAuth#configuration)设置。

## 开发配置界面

启动游戏后，按右 Shift 键（默认按键）打开 HUD 编辑器。点击空白处取消 HUD 选择，再按一次即可打开配置界面。浏览器没有打开时，可以使用日志中的 URL。

服务器默认监听 `http://127.0.0.1:60916/`。端口被占用时，会选用其他可用端口。当前地址会记在日志中。

在单独的终端中启动 Vite，即可实时查看前端改动：

```powershell
npm ci --prefix web-ui
npm run dev --prefix web-ui
```

打开 Vite 输出的 URL。`/api` 代理默认将请求转发到端口 `60916`。游戏使用其他端口时，先设置代理的 `target`，再启动 Vite。

```powershell
$env:WEB_UI_TARGET = 'http://127.0.0.1:<game-port>'
npm run dev --prefix web-ui
```

将 `<game-port>` 替换为日志中的端口。更新内置页面需要重新构建并重启模组。

## 运行检查

在 Windows 上，从仓库根目录运行以下命令。在 Linux 或 macOS 上，使用 `./gradlew` 替代 `./gradlew.bat`。

```powershell
.\gradlew.bat check
.\gradlew.bat assemble
npm run format:check --prefix web-ui
```

`check` 运行 Java 测试，包括 `ArchitectureTest`。`assemble` 构建配置界面，并在 `build/libs/` 中生成 jar 文件。

运行 npm 检查前，先用 `npm ci --prefix web-ui` 安装依赖。

## 故障排查

Gradle 同步失败或报告工具链错误时，检查 Gradle 是否使用 JDK 21，并能找到或下载 Java 8 工具链。

Vite 页面打开后请求失败时，检查游戏是否已启动，以及 `WEB_UI_TARGET` 是否与当前游戏地址一致。配置界面打开失败时，在游戏日志中查找 "Configuration interface is unavailable" 消息。

数据保存在 Minecraft 游戏目录下的 `12pit/` 中。配置方案位于 `12pit/config/`，关系列表位于 `12pit/relations.json`，切换绑定位于 `12pit/swap-bindings.json`。

排查加载错误时，先备份这些文件，再修改。
