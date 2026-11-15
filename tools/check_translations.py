"""Require complete German resources and preserve Android formatting placeholders."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1] / "app/src/main/res"

def read_strings(path):
    elements = ET.parse(path).getroot().findall("string")
    result = {item.attrib["name"]: "".join(item.itertext()) for item in elements}
    if len(elements) != len(result):
        raise SystemExit(f"Duplicate string names in {path}")
    return result

source = read_strings(root / "values/strings.xml")
german = read_strings(root / "values-de/strings.xml")
if source.keys() != german.keys():
    raise SystemExit(f"German resource mismatch: missing={source.keys() - german.keys()}, extra={german.keys() - source.keys()}")
pattern = re.compile(r"%(?:\d+\$)?[-#+ 0,(]*\d*(?:\.\d+)?[a-zA-Z]")
for key, value in source.items():
    if not german[key].strip():
        raise SystemExit(f"Empty German translation: {key}")
    if sorted(pattern.findall(value)) != sorted(pattern.findall(german[key])):
        raise SystemExit(f"Formatting placeholder mismatch: {key}")
print(f"German translations complete: {len(source)} strings; placeholders match.")
