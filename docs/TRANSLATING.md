# Contributing translations

## General rules

We recommend translating only into your native language.

## Contribute interface translations

The configuration interface, setting descriptions, command messages, and HUD labels all share the same translation files. As the OneConfig menu always uses the original English text, test your translations in the configuration interface.

Interface translations are handled on [Crowdin](https://crowdin.com/project/12pit). Each occurrence of a string has a separate entry and context showing which file and location it is taken from, so an English word may have multiple translations depending on its context.

### Request a language

If your language hasn't been added yet, open an issue mentioning the language name and region (if applicable). Any language supported by [Crowdin](https://support.crowdin.com/project-settings/languages/) can be added.

### Write translations

When contributing translations on [Crowdin](https://crowdin.com/project/12pit), refer to the entry's context to know how the string is used. Keep numbered placeholders such as `{0}` and ensure that each one appears the same number of times as in the original text, even though you can reorder them for your language. Leave Minecraft formatting codes such as `§a` untouched. Untouched strings will use the English texts automatically, so incomplete translations are perfectly acceptable.

## Contribute documentation translations

Languages whose ISO 639-1 code is two letters can be added via pull requests. Other languages should first be discussed in an issue or on [Discord](https://discord.gg/e9PRKMUenc) with a maintainer. Directory names for languages should follow [RFC 5646](https://www.rfc-editor.org/rfc/rfc5646.html).

### Paths and links

Translate files should be put in `docs/<language-tag>/` while keeping the same filenames and structure as the English documentation:

| English source | Translation example |
| -------------- | ------------------- |
| `README.md`                            | `docs/nl-NL/README.md`                           |
| `docs/CONTRIBUTING.md`                | `docs/nl-NL/CONTRIBUTING.md`                    |

When introducing a new language, first translate `README.md` and then link to it from the language bar at the top of the root `README.md` (as well as other translated READMEs), using the language's own name and `|` as a separator.

When linking to a page that is already translated, link to the translated version. Otherwise, fall back to English. Verify that relative links and heading anchors still work after translation, and leave commands, code snippets, identifiers, paths and external links untouched.

### Source header

Each translated file should have the following HTML comment at the top:

```markdown
<!-- Source: docs/CONTRIBUTING.md; Based on: <source-commit> -->
```

`Source` is the path to the English file starting from the repository root (`README.md` for the translated README), and `Based on` is the full commit hash of the English version you translated from. Both field names should be in English.

### Check for outdated translations

In order to compare the changes in the English file after the last translation update, use `git diff` in the repository root with the commit hash from `Based on`:

```sh
git diff <source-commit> -- docs/CONTRIBUTING.md
```

After updating the translation accordingly, obtain the most recent commit hash for the English file and update `Based on`:

```sh
git log -1 --format=%H -- docs/CONTRIBUTING.md
```

### Submit a translation

Contribute one language per pull request and mention the files you translated. If a pull request modifies both an English doc and its translation, split them into two commits: English modification first, translation modification next.
