<!-- Source: docs/TRANSLATING.md; Based on: 5c8bce221aa767a7dad58c3d23b817d9c07395d9 -->

# 贡献翻译

## 通用规则

建议只翻译成自己的母语。

## 贡献界面翻译

配置界面、配置文字、命令消息和 HUD 标签共用一套语言文件。OneConfig 设置界面使用英文原文，请在配置界面中核对设置的译文。

界面翻译在 [Crowdin](https://crowdin.com/project/12pit) 上进行。每个使用位置都有独立条目，同一句英文可以根据上下文采用不同译文。条目会显示源文件和使用位置。

### 申请新语言

需要新增界面语言时，先创建议题，说明语言名称，并按需注明地区。界面语言须由 [Crowdin](https://support.crowdin.com/project-settings/languages/) 支持。

### 填写译文

在 [Crowdin](https://crowdin.com/project/12pit) 上填写译文，并查看使用位置。保留 `{0}` 等编号参数及其出现次数，顺序可以调整。保留 `§a` 等 Minecraft 格式代码。译文缺失或为空时，使用英文原文。也欢迎贡献部分译文。

## 贡献文档翻译

有 [ISO 639-1 两字母代码](https://www.loc.gov/standards/iso639-2/php/code_list.php) 的语言可以直接添加。其他语言须先在议题或 [Discord](https://discord.gg/e9PRKMUenc) 取得维护者同意。文档的语言标签须符合 [RFC 5646](https://www.rfc-editor.org/rfc/rfc5646.html)。

### 路径和链接

译文放在 `docs/<语言标签>/` 下。保留原文的文件名和它在 `docs/` 内的子目录。

| 英文原文 | 译文示例 |
| --- | --- |
| `README.md` | `docs/nl-NL/README.md` |
| `docs/CONTRIBUTING.md` | `docs/nl-NL/CONTRIBUTING.md` |

添加新语言时，先翻译 README。在根目录 README 的语言链接旁添加入口，使用该语言自己的名称。译文中的语言链接也用 `|` 分隔，与根目录 README 保持一致。

目标页面已有译文时，链接到译文，否则链接到英文原文。翻译后检查相对路径和标题锚点。命令、代码、标识符、文件路径和外部 URL 保持不变。

### 源文件页头

每篇译文都以 HTML 注释开头：

```markdown
<!-- Source: docs/CONTRIBUTING.md; Based on: <原文提交号> -->
```

`Source` 填写英文原文相对于仓库根目录的路径。`Based on` 填写已核对的原文版本对应的完整提交号。两个字段名都使用英文。README 译文的源文件路径是 `README.md`。

### 检查译文是否过时

在仓库根目录中，比较当前原文与页头记录的版本。将 `<原文提交号>` 替换为页头中的提交号，文件路径使用 `Source` 的值：

```sh
git diff <原文提交号> -- docs/CONTRIBUTING.md
```

核对原文自该提交以来的所有改动，并按需更新译文。确认译文与原文一致后，记录最近一次修改原文的完整提交号：

```sh
git log -1 --format=%H -- docs/CONTRIBUTING.md
```

### 提交译文

每个拉取请求尽量只包含一种语言的翻译，并列出新增或更新的文件。通读译文，检查链接、标题、示例和源文件页头。

同一拉取请求同时更新原文和译文时，分成两个提交：先提交原文，再提交译文。译文的 `Based on` 填写原文提交的完整提交号。
