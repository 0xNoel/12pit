# Contributing translations

## General rules

We recommend translating only into your native language.

## Contribute interface translations

The same files include translations for the configuration interface, configuration text, command messages, and HUD labels. The OneConfig settings view uses the original English text. Check translated settings in the configuration interface.

Interface translations use [Crowdin](https://crowdin.com/project/12pit). Each use of a string has its own entry, so the same English text can have different translations. The entry's context shows its source file and location.

### Request a language

Open an issue to request an interface language. Include the language name and region, if needed. Interface languages must be supported by [Crowdin](https://support.crowdin.com/project-settings/languages/).

### Write translations

Translate in [Crowdin](https://crowdin.com/project/12pit) and review the usage context. Keep numbered parameters like `{0}` and their number of occurrences; their order may change. Preserve Minecraft color codes like `§a`. Missing or empty translations use the English source. Partial translations are welcome.

## Contribute documentation translations

Languages with an [ISO 639-1 two-letter code](https://www.loc.gov/standards/iso639-2/php/code_list.php) can be added without opening an issue first. For other languages, get a maintainer's approval in an issue or on [Discord](https://discord.gg/e9PRKMUenc). Documentation language tags must follow [RFC 5646](https://www.rfc-editor.org/rfc/rfc5646.html).

### Paths and links

Put translations in `docs/<language-tag>/`. Keep the source filename and any subdirectories within `docs/`.

| English source | Translation example |
| --- | --- |
| `README.md` | `docs/nl-NL/README.md` |
| `docs/CONTRIBUTING.md` | `docs/nl-NL/CONTRIBUTING.md` |

For a new language, translate the README first. Add a link beside the language links in the root README, using the language's own name. Separate language links with `|` in translated documents as well.

Link to a translated page when it exists; otherwise, link to the English page. Check relative paths and heading anchors after translating. Keep commands, code, identifiers, file paths, and external URLs unchanged.

### Source header

Start each translated file with an HTML comment:

```markdown
<!-- Source: docs/CONTRIBUTING.md; Based on: <source-commit> -->
```

`Source` is the English file's path from the repository root. `Based on` is the full hash of a commit containing the source version you reviewed. Keep these field names in English. For a translated README, the source path is `README.md`.

### Check for outdated translations

From the repository root, compare the source with the commit in the header. Replace `<source-commit>` with that hash and use the file path from `Source`:

```sh
git diff <source-commit> -- docs/CONTRIBUTING.md
```

Review every source change since the recorded commit, then update the translation as needed. Once they match, record the latest committed source version:

```sh
git log -1 --format=%H -- docs/CONTRIBUTING.md
```

### Submit a translation

Keep each pull request to one language and list the changed files. Review the translation, links, headings, examples, and source header.

If a pull request changes both the source and its translation, use two commits: source first, then translation.
