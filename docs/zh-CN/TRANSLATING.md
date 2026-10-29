<!-- Source: docs/TRANSLATING.md; Based on: fd3499ea65660abdafb7116049c5fd734e73efe8 -->

# 贡献翻译

## 基本原则

建议只翻译成你自己的母语。

## 贡献界面翻译

网页配置界面、设置项说明、命令提示和 HUD 标签共用同一套翻译文件。由于 OneConfig 菜单始终显示英文原文，核对翻译效果时请在网页配置界面中查看。

界面翻译托管在 [Crowdin](https://crowdin.com/project/12pit) 上。每处文本调用都有独立的词条，并在上下文中标明了源文件和位置，因此相同的英文词组在不同位置可以根据语境翻译成不同的表达。

### 申请新语言

如果还没有你想翻译的语言，可以提一个议题，写明语言名称（有需要的话注明地区）。只要是 [Crowdin](https://support.crowdin.com/project-settings/languages/) 支持的语言都可以添加。

### 翻译注意事项

在 [Crowdin](https://crowdin.com/project/12pit) 上翻译时，请结合词条的上下文确认它在游戏或界面里的实际用途。像 `{0}` 这样的编号占位符需要保留，并确保每个占位符出现的次数与原文一致，但语序可以根据目标语言的习惯调整。像 `§a` 这样的 Minecraft 格式代码也需要原样保留。未翻译的词条会自动回退显示英文原文，因此只翻译一部分也完全没问题。

## 贡献文档翻译

拥有两字母 [ISO 639-1 代码](https://www.loc.gov/standards/iso639-2/php/code_list.php)的语言可以直接提交拉取请求；其他语言请先在议题或 [Discord](https://discord.gg/e9PRKMUenc) 上与维护者讨论。文档语言目录名需符合 [RFC 5646](https://www.rfc-editor.org/rfc/rfc5646.html) 规范。

### 路径与链接

翻译后的文档放在 `docs/<语言标签>/` 下，文件名和子目录结构与英文文档保持一致：

| 英文原文 | 译文路径示例 |
| --- | --- |
| `README.md` | `docs/nl-NL/README.md` |
| `docs/CONTRIBUTING.md` | `docs/nl-NL/CONTRIBUTING.md` |

新增一门语言时，请先翻译 `README.md`，并在根目录 `README.md`（以及其他已翻译的 README）顶部的语言栏里加上链接，链接文字使用该语言自己的名称，用 `|` 分隔。

文档内的链接优先指向已有的同语言译文，还没有译文的则指向英文原版。翻译完后请检查相对路径和标题锚点是否有效，命令、代码、标识符、文件路径和外部链接保持原样不要改动。

### 源文件页头

每篇译文文件的最顶部都需要加上一行 HTML 注释，用来记录所依据的英文原版提交版本：

```markdown
<!-- Source: docs/CONTRIBUTING.md; Based on: <原文提交号> -->
```

其中 `Source` 是英文原文相对于仓库根目录的路径（README 译文填 `README.md`），`Based on` 是你参照的英文原文对应的完整提交哈希。注意 `Source` 和 `Based on` 这两个字段名需要保持英文。

### 检查译文是否过时

想查看英文原文自上次翻译以来改了哪些内容，可以在仓库根目录下用页头里的提交哈希运行 `git diff`：

```sh
git diff <原文提交号> -- docs/CONTRIBUTING.md
```

把译文同步更新到最新内容后，获取该英文文件最近一次修改的提交哈希并更新 `Based on`：

```sh
git log -1 --format=%H -- docs/CONTRIBUTING.md
```

### 提交译文

每个翻译拉取请求请只包含一种语言，并在描述中列出改动的文件。如果同一个拉取请求同时修改了英文文档和对应的译文，请拆成两个提交：先提交英文改动，再提交译文更新。
