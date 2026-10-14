<!-- Source: docs/TRANSLATING.md; Based on: 1ba2c692c4ee6c2312b39063b1ebfff6b3732539 -->

# 贡献翻译

## 通用规则

建议只翻译成自己的母语。

## 贡献界面翻译

配置界面、配置文字、命令回显和 HUD 标签使用同一套语言文件。OneConfig 设置界面使用英文原文。核对设置的译文时，使用配置界面。

界面翻译使用 [Crowdin](https://crowdin.com/project/12pit)。每个使用位置都有独立条目，所以同一句英文可以有不同译文。条目的上下文会显示源文件和使用位置。

### 申请新语言

需要新增界面语言时，先开 issue，说明语言名称，需要时注明地区。界面语言须由 [Crowdin](https://support.crowdin.com/project-settings/languages/) 支持。

### 填写译文

在 [Crowdin](https://crowdin.com/project/12pit) 填写译文，并核对使用位置。保留 `{0}` 等编号参数及其出现次数，可以调整顺序。保留 `§a` 等 Minecraft 格式代码。缺失或空译文使用英文原文。可以贡献部分译文。

## 贡献文档翻译

有 [ISO 639-1 两字母代码](https://www.loc.gov/standards/iso639-2/php/code_list.php) 的语言可以直接添加，无需先开 issue。其他语言须先在 issue 或 [Discord](https://discord.gg/e9PRKMUenc) 取得维护者同意。文档的语言标签须符合 [RFC 5646](https://www.rfc-editor.org/rfc/rfc5646.html)。

### 路径和链接

译文放在 `docs/<语言标签>/` 下。保留原文的文件名和它在 `docs/` 内的子目录。

| 英文原文 | 译文示例 |
| --- | --- |
| `README.md` | `docs/nl-NL/README.md` |
| `docs/CONTRIBUTING.md` | `docs/nl-NL/CONTRIBUTING.md` |

添加新语言时，先翻译 README。在根目录 README 的语言链接旁添加入口，使用该语言自己的名称。译文中的语言链接也用 `|` 分隔，与根目录 README 保持一致。

链接到的页面已有译文时，指向译文；否则指向英文原文。翻译后检查相对路径和标题锚点。命令、代码、标识符、文件路径和外部 URL 保持不变。

### 源文件页头

每篇译文都以 HTML 注释开头：

```markdown
<!-- Source: docs/CONTRIBUTING.md; Based on: <原文提交号> -->
```

`Source` 填写英文原文相对于仓库根目录的路径。`Based on` 填写已核对的原文版本对应的完整提交号。两个字段名都使用英文。README 译文的源文件路径是 `README.md`。

### 检查译文是否过时

在仓库根目录，将原文与页头记录的提交比较。把 `<原文提交号>` 替换为页头中的提交号，文件路径使用 `Source` 的值：

```sh
git diff <原文提交号> -- docs/CONTRIBUTING.md
```

核对原文在该提交之后的所有改动，并按需更新译文。确认译文与原文一致后，记录原文最近一次提交的完整提交号：

```sh
git log -1 --format=%H -- docs/CONTRIBUTING.md
```

### 提交译文

每个拉取请求尽量只包含一种语言的翻译，并列出新增或更新的文件。通读译文，检查链接、标题、示例和源文件页头。

如果同一拉取请求也需要新增或更新原文，应分成两个提交：先提交原文，再提交译文。译文的 `Based on` 填写原文提交的完整提交号。
