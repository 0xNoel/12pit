# Contributing translations

## General rules

We recommend translating only into your native language.

## Contribute interface translations

The configuration interface, configuration text, command messages, and HUD labels share the same translation files. The OneConfig settings view uses the original English text. Check translated settings in the configuration interface.

Translate the interface on [Crowdin](https://crowdin.com/project/12pit). Each use of a string has its own entry, so the same English text can have different translations. The entry's context shows the source file and location.

### Request a language

To request an interface language, open an issue with its name and region, if needed. Interface languages must be supported by [Crowdin](https://support.crowdin.com/project-settings/languages/).

### Write translations

Translate on [Crowdin](https://crowdin.com/project/12pit) and check where each string is used. Keep numbered parameters like `{0}` and the number of times they appear. You may change their order. Preserve Minecraft color codes like `§a`. Missing or empty translations fall back to the English source. Partial translations are welcome.

## Contribute documentation translations

Languages with an [ISO 639-1 two-letter code](https://www.loc.gov/standards/iso639-2/php/code_list.php) can be added directly. For other languages, get a maintainer's approval in an issue or on [Discord](https://discord.gg/e9PRKMUenc). Documentation language tags must follow [RFC 5646](https://www.rfc-editor.org/rfc/rfc5646.html).

### Paths and links

Put translations in `docs/<language-tag>/`. Keep the source filename and its subdirectory structure within `docs/`.

| English source | Translation example |
| --- | --- |
| `README.md` | `docs/nl-NL/README.md` |
| `docs/CONTRIBUTING.md` | `docs/nl-NL/CONTRIBUTING.md` |

For a new language, translate the README first. Add a link alongside the language links in the root README, using the language's own name. Separate language links with `|` in translated documents as well.

Link to a translated page when it exists; otherwise, link to the English page. Check relative paths and heading anchors after translating. Keep commands, code, identifiers, file paths, and external URLs unchanged.

### Source header

Start each translated file with an HTML comment:

```markdown
<!-- Source: docs/CONTRIBUTING.md; Based on: <source-commit> -->
```

`Source` is the English file's path relative to the repository root. `Based on` is the full commit hash for the source version you reviewed. Keep these field names in English. For a translated README, the source path is `README.md`.

### Check for outdated translations

From the repository root, compare the source with the commit in the header. Replace `<source-commit>` with that hash and use the file path from `Source`:

```sh
git diff <source-commit> -- docs/CONTRIBUTING.md
```

Review all source changes since the recorded commit and update the translation as needed. When the translation matches the source, record the latest commit that changed the source:

```sh
git log -1 --format=%H -- docs/CONTRIBUTING.md
```

### Submit a translation

Keep each pull request to one language and list the changed files. Review the translation, links, headings, examples, and source header.

If a pull request changes both the source and its translation, use two commits: source first, then translation.
