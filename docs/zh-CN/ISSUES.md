<!-- Source: docs/ISSUES.md; Based on: b89c7e1ea68fd0183c6877fdbe32f36b5d89cb2b -->

# 议题

创建新议题前，请先搜索一下是否已经有相同的议题。如果已经有人提过，直接在原有议题下补充信息即可。

## 创建议题

反馈 bug 时，请写清楚预期表现、实际结果和复现步骤，并附上 12pit 版本、Minecraft 与模组环境，以及相关的日志片段。上传日志前请先删掉账号令牌等个人信息。

提新功能或改进建议时，请说明你想解决什么问题以及期望的效果。能附上简短的例子会很有帮助，有初步想法或疑问也欢迎直接提出来。

## 分类与处理

维护者在排查新议题时，可能会视情况加上 https://github.com/12src/12pit/labels/needs%20triage、https://github.com/12src/12pit/labels/needs%20info 或 https://github.com/12src/12pit/labels/needs%20reproduction 标签。

需要先敲定产品或技术方案的议题会标上 https://github.com/12src/12pit/labels/needs%20decision，开放性讨论使用 https://github.com/12src/12pit/labels/discussion，使用问题使用 https://github.com/12src/12pit/labels/question，与项目无关或基于误解的议题则会标上 https://github.com/12src/12pit/labels/invalid。

当问题成功复现或功能建议被采纳后，维护者会加上 https://github.com/12src/12pit/labels/confirmed 标签，并按类型、领域、优先级和环境进行分类。

大多数议题会在对应的拉取请求合并后自动关闭，或者在问题解答完毕、讨论结束后手动关闭。不涉及代码改动的关闭原因会使用 https://github.com/12src/12pit/labels/duplicate、https://github.com/12src/12pit/labels/cannot%20reproduce、https://github.com/12src/12pit/labels/upstream 或 https://github.com/12src/12pit/labels/wontfix。

## 标签说明

新需求使用 https://github.com/12src/12pit/labels/feature 或 https://github.com/12src/12pit/labels/enhancement；各类问题与故障使用 https://github.com/12src/12pit/labels/bug、https://github.com/12src/12pit/labels/crash、https://github.com/12src/12pit/labels/regression、https://github.com/12src/12pit/labels/compatibility 或 https://github.com/12src/12pit/labels/performance。

其他维护工作使用 https://github.com/12src/12pit/labels/documentation、https://github.com/12src/12pit/labels/refactor、https://github.com/12src/12pit/labels/chore 或 https://github.com/12src/12pit/labels/clean，不兼容改动会标上 https://github.com/12src/12pit/labels/breaking%20change。

领域标签用来标明议题涉及的模块：https://github.com/12src/12pit/labels/domain%3A%20CI、https://github.com/12src/12pit/labels/domain%3A%20build、https://github.com/12src/12pit/labels/domain%3A%20dependencies、https://github.com/12src/12pit/labels/domain%3A%20release、https://github.com/12src/12pit/labels/domain%3A%20testing、https://github.com/12src/12pit/labels/domain%3A%20architecture、https://github.com/12src/12pit/labels/domain%3A%20tooling、https://github.com/12src/12pit/labels/domain%3A%20localization、https://github.com/12src/12pit/labels/domain%3A%20licensing、https://github.com/12src/12pit/labels/domain%3A%20governance 和 https://github.com/12src/12pit/labels/domain%3A%20UI%2FUX。

优先级用 https://github.com/12src/12pit/labels/priority%3A%20P0、https://github.com/12src/12pit/labels/priority%3A%20P1、https://github.com/12src/12pit/labels/priority%3A%20P2 和 https://github.com/12src/12pit/labels/priority%3A%20P3 区分；特定环境下的问题会加上 https://github.com/12src/12pit/labels/env%3A%20Windows、https://github.com/12src/12pit/labels/env%3A%20Linux、https://github.com/12src/12pit/labels/env%3A%20macOS、https://github.com/12src/12pit/labels/env%3A%20mod%20conflict 或 https://github.com/12src/12pit/labels/env%3A%20server-specific。此外还有表示受阻或阻塞发布的 https://github.com/12src/12pit/labels/blocked 和 https://github.com/12src/12pit/labels/release%20blocker。

## 认领议题

同时带有 https://github.com/12src/12pit/labels/confirmed 与 https://github.com/12src/12pit/labels/help%20wanted（或 https://github.com/12src/12pit/labels/good%20first%20issue）的未关闭议题欢迎社区认领。动手前请确认还没有人接手，并参考[贡献指南](CONTRIBUTING.md)。
