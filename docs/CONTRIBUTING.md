# Contributing to 12pit

For translations, see the [translation guide](https://github.com/12src/12pit/blob/main/docs/TRANSLATING.md).

## Getting Started

Bug fixes, documentation, tests, maintenance, and new features are all welcome.

Look for an open issue labeled https://github.com/12src/12pit/labels/confirmed and https://github.com/12src/12pit/labels/help%20wanted. New contributors can start with issues labeled https://github.com/12src/12pit/labels/confirmed and https://github.com/12src/12pit/labels/good%20first%20issue. Check the assignees, comments, and linked pull requests before starting.

Open an issue before starting a large feature or a change that needs design decisions. The [issue guide](https://github.com/12src/12pit/blob/main/docs/ISSUES.md) explains what to include. If the scope is unclear, ask in the issue or in `#developer` on [Discord](https://discord.gg/e9PRKMUenc).

## Claiming an Issue

Leave a comment when you start working on an issue. If your work may overlap with someone else's, open a draft pull request to let them know what you're working on.

## Working on Your Change

Keep each commit focused on one change. We prefer the [conventional commit format](https://www.conventionalcommits.org/), such as `fix: correct profile loading` or `docs: clarify build steps`. A pull request should usually have one goal. A few related changes can go together if they are easy to review.

The [architecture guide](https://github.com/12src/12pit/blob/main/docs/ARCHITECTURE.md) explains package roles and dependencies. The [debugging guide](https://github.com/12src/12pit/blob/main/docs/DEBUGGING.md) covers the development client and checks.

Read the relevant code and reuse existing helpers where they fit. Keep new behavior in the feature that owns it. Extract shared code when there is a clear need, and keep code cleanup focused on the change.

[BootstrapLayoutTest](../src/test/java/pit12/architecture/BootstrapLayoutTest.java) creates configs to check their categories. Configs without injected dependencies use a public no-argument constructor. When adding a config with injected dependencies, update the test to pass them.

Use short, natural sentences throughout the project. Prefer common words and use project terms when they make the meaning clearer.

## Coding Standards

Write the code the feature needs now. Add interfaces or wrappers for a concrete need, such as a second implementation or testing.

Extract a method, constant, or variable when it improves readability or is used in several places. Keep one-off expressions inline when they read clearly. Combine methods that do the same job, and call a method directly when a wrapper only forwards the call.

Validate user input, file and network data, and potentially missing Minecraft values where they enter the project. Rely on internal callers to pass valid data. Treat broken internal invariants as bugs and handle them where they occur. Let these failures surface. `stop()` must handle resources that were never created because initialization may have failed.

Copy a collection only when another consumer may modify it or when passing it to another thread.

Access live state on the client thread. Run file I/O, HTTP requests, and expensive computations on workers owned by the component. Before applying worker results, check that the owner is still running and the request or generation is current. Check connection and world identities when the work depends on them.

Complete state changes before notifying listeners. Callbacks should see the new state.

Preserve existing files when loading fails, and keep unsaved changes when writing fails. Check the save revision before marking data saved. When changing a file format, handle older versions and keep saved IDs stable.

Catch an exception when you can handle it or give the caller more information. Remove a `catch` block that only rethrows or ignores the exception.

Avoid obvious waste, such as parsing or scanning the whole world every frame. Measure the benefit before adding caches or special cases. Choose the simpler approach when the gain is small and the extra complexity is high.

Remove unused code from your changes, including methods, fields, parameters, imports, and classes. Before removing a public member, search the entire repository for uses, including tests, documentation, and Mixin configuration.

Use standard language features. Let autoboxing handle boxing, and use imports for type names.

## Comments

Read the code as someone seeing it for the first time. Add a short comment where the purpose, behavior, or contract would be unclear or easy to misread. Let clear code speak for itself. Keep comments up to date when the code changes.

## Submitting a Pull Request

Format code with `./gradlew spotlessApply` and `npm run format --prefix web-ui`. Run the checks relevant to your change and build once before submitting the pull request. State what you ran and any known limitations. Include focused tests when behavior changes.

Describe what changed and why, and report any failed checks. Mark the draft pull request ready for review when the work is complete.
