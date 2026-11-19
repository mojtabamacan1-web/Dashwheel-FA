#!/usr/bin/env python3
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FILE = ROOT / "locales" / "fa-IR.json"

def main():
    data = json.loads(FILE.read_text(encoding="utf-8"))
    assert data.get("locale") == "fa-IR", "locale must be fa-IR"
    assert data.get("direction") == "rtl", "direction must be rtl"
    translations = data.get("translations")
    assert isinstance(translations, dict) and translations, "translations must be a non-empty object"
    empty = [key for key, value in translations.items()
             if not isinstance(value, str) or not value.strip()]
    if empty:
        raise SystemExit("Empty or invalid translations: " + ", ".join(empty))
    print(f"OK: {len(translations)} Persian glossary entries")

if __name__ == "__main__":
    main()
