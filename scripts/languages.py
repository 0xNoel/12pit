import argparse
import ast
from collections import Counter
import hashlib
import json
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
DIRECTORY = ROOT / "src/main/resources/assets/pit12/languages"
PARAMETER = re.compile(r"\{([0-9]+)\}")
TOKEN = re.compile(r'//[^\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|[A-Za-z_$][\w$]*|[^\s]')
WEB_STRING = r'''(?:"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*')'''
PO_STRING = re.compile(r'"(?:[^"\\\x00-\x1f]|\\(?:[abfnrtv"\\]|[0-7]{1,3}))*"')
LOCATION = re.compile(r"(?:src/main/java/.+\.java:[1-9][0-9]*"
                      r"|web-ui/src/.+\.(?:vue|ts):[1-9][0-9]*(?::[1-9][0-9]*)?)")


def write_changed(path, content):
    if path.exists() and path.read_text(encoding="utf-8") == content:
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8", newline="\n")


def read_catalog(path):
    catalog = {}
    usages = set()
    fields, locations, comments, flags = {}, [], [], []
    current = None

    def finish():
        if not fields:
            return
        if "msgid" not in fields or "msgstr" not in fields:
            raise ValueError(f"{path}: incomplete PO entry")
        source, target = fields["msgid"], fields["msgstr"]
        if not source and "msgctxt" not in fields:
            return  # The empty msgid holds the PO header.
        key = fields.get("msgctxt", "")
        if not key or not source or not locations:
            raise ValueError(f"{path}: PO entries need a context, source, and usage location")
        if key in catalog:
            raise ValueError(f"{path}: duplicate context: {key}")
        for location in locations:
            if not LOCATION.fullmatch(location) or (source, location) in usages:
                raise ValueError(f"{path}: {key}: invalid or duplicate usage location: {location}")
            usages.add((source, location))
        if target and "fuzzy" not in flags and Counter(PARAMETER.findall(source)) != Counter(PARAMETER.findall(target)):
            raise ValueError(f"{path}: {key}: translation parameters do not match the source")
        catalog[key] = {"source": source, "translation": target, "locations": locations,
                        "comments": comments, "flags": flags}

    for number, line in enumerate(path.read_text(encoding="utf-8-sig").splitlines() + [""], 1):
        line = line.strip()
        if not line or ("msgstr" in fields and line.startswith(("#", "msgctxt", "msgid"))):
            finish()
            fields, locations, comments, flags = {}, [], [], []
            current = None
        if not line:
            continue
        if line.startswith("#"):
            current = None
            if line.startswith("#:"):
                locations.extend(line[2:].split())
            elif line.startswith("#,"):
                flags.extend(flag.strip() for flag in line[2:].split(",") if flag.strip())
            elif not line.startswith(("#~", "#|")):
                comments.append(line)
            continue
        if line.startswith('"'):
            value = line
            if current is None:
                raise ValueError(f"{path}:{number}: unexpected PO continuation")
        else:
            match = re.fullmatch(r'(msgctxt|msgid|msgstr)\s+(".*")', line)
            if not match:
                raise ValueError(f"{path}:{number}: unsupported PO line")
            current, value = match.groups()
            if current in fields:
                raise ValueError(f"{path}:{number}: duplicate PO field: {current}")
            fields[current] = ""
        if not PO_STRING.fullmatch(value):
            raise ValueError(f"{path}:{number}: invalid PO string")
        fields[current] += ast.literal_eval(value)
    return catalog


def write_catalog(path, catalog, locale=None):
    def quote(text):
        escapes = {"\\": r"\\", '"': r'\"', "\n": r"\n", "\r": r"\r", "\t": r"\t"}
        return '"' + "".join(escapes.get(character, f"\\{ord(character):03o}"
                                       if ord(character) < 32 else character) for character in text) + '"'

    headers = ["Project-Id-Version: 12pit\n", "MIME-Version: 1.0\n",
               "Content-Type: text/plain; charset=UTF-8\n", "Content-Transfer-Encoding: 8bit\n"]
    if locale:
        headers.insert(1, f"Language: {locale}\n")
    lines = ['msgid ""', 'msgstr ""', *(quote(header) for header in headers), ""]
    for key, entry in catalog.items():
        lines.extend(entry.get("comments", []))
        lines.append("#: " + " ".join(entry["locations"]))
        if entry.get("flags"):
            lines.append("#, " + ", ".join(entry["flags"]))
        lines.extend(["msgctxt " + quote(key), "msgid " + quote(entry["source"]),
                      "msgstr " + quote(entry.get("translation", "")), ""])
    write_changed(path, "\n".join(lines))


def read_languages():
    entries = json.loads((DIRECTORY / "languages.json").read_text(encoding="utf-8"))
    values, locales = set(), set()
    for entry in entries:
        value, locale, name = entry["value"], entry["locale"], entry["name"]
        if type(value) is not int or value < 0 or value in values:
            raise ValueError("languages.json: language values must be unique nonnegative integers")
        if not re.fullmatch(r"[a-zA-Z]{2,3}(?:-[a-zA-Z0-9]{2,8})*", locale) or locale.lower() in locales:
            raise ValueError(f"languages.json: invalid or duplicate language tag: {locale}")
        if not isinstance(name, str) or not name.strip():
            raise ValueError(f"languages.json: missing language name: {locale}")
        if (value == 0) != (locale.lower() == "en-us"):
            raise ValueError("languages.json: English must keep value 0")
        values.add(value)
        locales.add(locale.lower())
    if 0 not in values:
        raise ValueError("languages.json: English is missing")
    return entries


def select_languages(entries, locale):
    if locale is None:
        return entries
    for entry in entries:
        if entry["locale"].lower() == locale.lower():
            return [entry]
    raise ValueError(f"unknown language: {locale}")


def sources():
    found = {}
    for path in sorted((ROOT / "src/main/java").rglob("*.java")):
        content = path.read_text(encoding="utf-8")
        tokens = [match for match in TOKEN.finditer(content)
                  if not match.group().startswith(("//", "/*"))]
        for index, match in enumerate(tokens[:-2]):
            if match.group() not in {"source", "translate", "format"} or tokens[index + 1].group() != "(":
                continue
            cursor = index + 2
            if not tokens[cursor].group().startswith('"'):
                continue
            source = json.loads(tokens[cursor].group())
            while cursor + 2 < len(tokens) and tokens[cursor + 1].group() == "+" and tokens[cursor + 2].group().startswith('"'):
                cursor += 2
                source += json.loads(tokens[cursor].group())
            if tokens[cursor + 1].group() not in {",", ")"}:
                raise ValueError(f"{path}: language methods need a complete source string")
            if match.group() == "format" and not PARAMETER.search(source):
                continue
            line = content.count("\n", 0, match.start()) + 1
            if source:
                found.setdefault(source, []).append(f"{path.relative_to(ROOT).as_posix()}:{line}")
    for path in sorted((ROOT / "web-ui/src").rglob("*")):
        if path.suffix not in {".vue", ".ts"} or "generated" in path.parts:
            continue
        content = path.read_text(encoding="utf-8")
        for match in re.finditer(r"\bt\s*\(\s*(" + WEB_STRING + r"(?:\s*\+\s*" + WEB_STRING + r")*)\s*(?=[,)])", content):
            source = "".join(ast.literal_eval(part.group())
                             for part in re.finditer(WEB_STRING, match.group(1)))
            if source:
                line = content.count("\n", 0, match.start()) + 1
                # Match the compiler's UTF-16 columns.
                prefix = content[content.rfind("\n", 0, match.start()) + 1:match.start()]
                column = len(prefix.encode("utf-16-le")) // 2 + 1
                found.setdefault(source, []).append(f"{path.relative_to(ROOT).as_posix()}:{line}:{column}")
    return found


def source_catalog(found):
    catalog = {}
    for source, locations in found.items():
        counts = Counter(location.split(":", 1)[0] for location in locations)
        positions = Counter()
        if len(set(locations)) != len(locations):
            raise ValueError(f"{source!r}: put separate Java uses on separate lines")
        for location in locations:
            file = location.split(":", 1)[0]
            positions[file] += 1
            # Line changes keep the key; changed usage counts need new translations.
            identity = f"{source}\0{file}\0{counts[file]}\0{positions[file]}"
            key = hashlib.sha256(identity.encode("utf-8")).hexdigest()
            catalog[key] = {"source": source, "locations": [location]}
    return catalog


def sync(catalog, entry):
    path = (ROOT / "build/generated/languages/12pit.pot" if entry["value"] == 0
            else DIRECTORY / (entry["locale"] + ".po"))
    previous = read_catalog(path) if path.exists() else {}
    if entry["value"] == 0:
        updated = catalog
    else:
        updated = {}
        for key, source in catalog.items():
            old = previous.get(key, {})
            if old and old["source"] != source["source"]:
                raise ValueError(f"{path}: {key}: source does not match the code")
            updated[key] = {**source, "translation": old.get("translation", ""),
                            "comments": old.get("comments", []), "flags": old.get("flags", [])}
    write_catalog(path, updated, entry["locale"] if entry["value"] != 0 else None)
    return len(catalog.keys() - previous.keys()), len(previous.keys() - catalog.keys())


def status(found, catalog, entries):
    rows, notes = [], []
    failed = False
    for entry in entries:
        locale, name = entry["locale"], entry["name"]
        if entry["value"] == 0:
            rows.append((locale, "Source", "-", name))
            continue
        path = DIRECTORY / (locale + ".po")
        try:
            previous = read_catalog(path) if path.exists() else {}
            for key in catalog.keys() & previous.keys():
                if previous[key]["source"] != catalog[key]["source"]:
                    raise ValueError(f"{path}: {key}: source does not match the code")
        except (OSError, ValueError) as error:
            rows.append((locale, "Invalid", "-", name))
            notes.append(f"{locale}: {error}")
            failed = True
            continue
        translated = sum(bool(previous.get(key, {}).get("translation"))
                         and "fuzzy" not in previous[key].get("flags", []) for key in catalog)
        slots = len(catalog)
        progress = translated / slots if slots else 1
        rows.append((locale, f"{translated}/{slots}", f"{progress:.1%}", name))
        missing = len(catalog.keys() - previous.keys())
        stale = len(previous.keys() - catalog.keys())
        moved = sum(previous[key]["locations"] != catalog[key]["locations"]
                    for key in catalog.keys() & previous.keys())
        if not path.exists():
            notes.append(f"{locale}: file missing")
        elif missing or stale:
            notes.append(f"{locale}: {missing} missing, {stale} obsolete")
        if moved:
            notes.append(f"{locale}: {moved} usage locations changed; run sync")

    width = max(len("Language"), *(len(row[0]) for row in rows))
    count_width = max(len("Translated"), *(len(row[1]) for row in rows))
    print(f"{len(found)} source texts, {len(catalog)} usages\n")
    print(f"{'Language':<{width}}  {'Translated':>{count_width}}  {'Progress':>8}  Name")
    for locale, count, progress, name in rows:
        print(f"{locale:<{width}}  {count:>{count_width}}  {progress:>8}  {name}")
    if notes:
        print()
        print("\n".join(notes))
    return 1 if failed else 0


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    add = commands.add_parser("add", help="Register a Crowdin target language (maintainers)")
    add.add_argument("language", help="Language tag, for example nl-NL")
    add.add_argument("--name", required=True, help="Native name, for example Nederlands")
    for name, help_text in (("sync", "Update source text"), ("status", "Show translation progress")):
        command = commands.add_parser(name, help=help_text)
        command.add_argument("--language", help="Limit to one language")
    args = parser.parse_args()
    try:
        entries = read_languages()
        if args.command == "add":
            if not re.fullmatch(r"[a-zA-Z]{2,3}(?:-[a-zA-Z0-9]{2,8})*", args.language):
                raise ValueError(f"invalid language tag: {args.language}")
            if any(entry["locale"].lower() == args.language.lower() for entry in entries):
                raise ValueError(f"language already exists: {args.language}")
            if not args.name.strip():
                raise ValueError("language name cannot be empty")
            entry = {"value": max(entry["value"] for entry in entries) + 1,
                     "locale": args.language, "name": args.name.strip()}
            found = sources()
            catalog = source_catalog(found)
            sync(catalog, entry)
            entries.append(entry)
            write_changed(DIRECTORY / "languages.json", json.dumps(entries, ensure_ascii=False, indent=2) + "\n")
            print(f"Added {entry['locale']} ({entry['name']}): {len(found)} texts")
            return 0
        selected = select_languages(entries, args.language)
        found = sources()
        catalog = source_catalog(found)
        if args.command == "status":
            return status(found, catalog, selected)
        if args.language is None:
            selected = [entry for entry in selected if entry["value"] != 0]
        for entry in selected:
            added, removed = sync(catalog, entry)
            print(f"Synced {entry['locale']}: {len(catalog)} usages ({added} added, {removed} removed)")
    except (OSError, ValueError, KeyError, TypeError) as error:
        print(error, file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
