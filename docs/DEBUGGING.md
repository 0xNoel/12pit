# Running and debugging 12pit

## Setup

The [README requirements](../README.md#requirements) list the required JDK and Node.js versions.

## Run the development client

On Windows, run:

```powershell
.\gradlew.bat runClient
```

On Linux/macOS, run:

```sh
./gradlew runClient
```

The development game directory is `run/`. Check `run/logs/latest.log` for client errors and the configuration interface address.

[DevAuth](https://github.com/DJtheRedstoner/DevAuth) is included in the development environment. To test with an authenticated account, enable the `devauth.enabled` JVM property and follow the [DevAuth configuration guide](https://github.com/DJtheRedstoner/DevAuth#configuration).

## Develop the configuration interface

Start the game, then press Right Shift (the default key) to open the HUD editor. Clear the HUD selection by clicking empty space, then press the key again to open the configuration interface. If the browser stays closed, open the URL from the log.

The server listens at `http://127.0.0.1:60916/` by default. If that port is in use, it picks another available port. Check the log for the current address.

To see frontend changes as you work, start Vite in a separate terminal:

```powershell
npm ci --prefix web-ui
npm run dev --prefix web-ui
```

Open the URL printed by Vite. The `/api` proxy forwards requests to port `60916` by default. If the game uses another port, set the proxy's `target` before starting Vite.

```powershell
$env:WEB_UI_TARGET = 'http://127.0.0.1:<game-port>'
npm run dev --prefix web-ui
```

Replace `<game-port>` with the port from the log. Rebuild and restart the mod to update the bundled page.

## Run checks

On Windows, run the following commands from the repository root. On Linux or macOS, use `./gradlew` instead of `./gradlew.bat`.

```powershell
.\gradlew.bat check
.\gradlew.bat assemble
npm run format:check --prefix web-ui
```

`check` runs the Java tests, including `ArchitectureTest`. `assemble` builds the configuration interface and creates the jar file in `build/libs/`.

Install dependencies with `npm ci --prefix web-ui` before running the npm check.

## Troubleshooting

If Gradle sync fails or reports a toolchain error, check that Gradle uses JDK 21 and can find or download the Java 8 toolchain.

If the Vite page opens but requests fail, check that the game is running and `WEB_UI_TARGET` matches its current address. If the configuration interface fails to open, look for "Configuration interface is unavailable" in the game log.

Saved data is in the `12pit/` folder inside the Minecraft game directory. Profiles are in `12pit/config/`, relations in `12pit/relations.json`, and swap bindings in `12pit/swap-bindings.json`.

Back up these files before editing them to diagnose a load error.