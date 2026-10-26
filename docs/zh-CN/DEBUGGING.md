<!-- Source: docs/DEBUGGING.md; Based on: b89c7e1ea68fd0183c6877fdbe32f36b5d89cb2b -->

# 运行与调试 12pit

## 环境准备

请先按 [README 环境要求](README.md#环境要求)准备好对应版本的 JDK 和 Node.js。

## 运行开发客户端

Windows：

```powershell
.\gradlew.bat runClient
```

Linux 或 macOS：

```sh
./gradlew runClient
```

开发客户端的运行目录是 `run/`，可以在 `run/logs/latest.log` 中查看客户端报错和配置界面的本地地址。

开发环境内置了 [DevAuth](https://github.com/DJtheRedstoner/DevAuth)。如果需要使用正版账号登录测试，可以启用 `devauth.enabled` JVM 属性，并参考 [DevAuth 配置说明](https://github.com/DJtheRedstoner/DevAuth#configuration)。

## 开发配置界面

启动游戏后，按右 Shift（默认按键）会打开 HUD 编辑器。在未选中任何 HUD 的状态下（点击空白处可取消选中），再按一次右 Shift 就会在浏览器中打开配置界面。如果浏览器没有自动弹出，可以直接打开 `run/logs/latest.log` 里打印的地址。

本地服务默认监听 `http://127.0.0.1:60916/`；如果该端口已被占用，会自动换用其他空闲端口，实际地址以日志为准。

前端开发时，可以在单独的终端里启动 Vite 来实时预览修改：

```powershell
npm ci --prefix web-ui
npm run dev --prefix web-ui
```

启动后打开 Vite 输出的地址即可。`/api` 代理默认转发到 `60916` 端口；如果游戏实际使用的是其他端口，启动 Vite 前先通过 `WEB_UI_TARGET` 指定端口：

```powershell
$env:WEB_UI_TARGET = 'http://127.0.0.1:<game-port>'
npm run dev --prefix web-ui
```

前端修改完成后，重新构建并重启游戏即可更新打包进模组的页面。

## 运行检查

在仓库根目录下运行以下命令即可执行测试和格式检查（Linux 或 macOS 请将 `.\gradlew.bat` 换成 `./gradlew`）：

```powershell
.\gradlew.bat check
npm ci --prefix web-ui
npm run format:check --prefix web-ui
```
