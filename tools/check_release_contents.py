"""Validate the privacy and offline-license contents of the built APK."""
import json
import re
import subprocess
import sys
import zipfile
from pathlib import Path

apk, aapt = sys.argv[1:]
with zipfile.ZipFile(apk) as package:
    assert package.read("assets/APACHE-2.0.txt") == Path("LICENSE").read_bytes()
    assert package.read("assets/THIRD_PARTY_NOTICES.txt").strip(), "Missing artifact notices"
    # Release optimization renames resource files. Identify their contents, not paths.
    reports = []
    rule_files = []
    for name in package.namelist():
        if name.endswith(".json"):
            candidate = json.loads(package.read(name))
            if isinstance(candidate, dict) and "libraries" in candidate and "licenses" in candidate:
                reports.append(candidate)
        elif name.endswith(".xml"):
            data = package.read(name)
            if any("data-extraction-rules".encode(encoding) in data for encoding in ("utf-8", "utf-16le")):
                rule_files.append(name)
    assert len(reports) == 1, "Missing or ambiguous dependency report"
    assert len(rule_files) == 1, "Missing or ambiguous backup rules"
    report = reports[0]
    assert report["libraries"], "Empty dependency report"
    assert "Copyright 2008 Google Inc." in report["licenses"]["BSD-3-Clause"]["content"]
    assert all("<<var;" not in license.get("content", "") for license in report["licenses"].values()), "Unexpanded SPDX template"
    for library in report["libraries"]:
        assert library.get("licenses"), "Missing license: " + library["uniqueId"]
        for key in library["licenses"]:
            assert report["licenses"][key].get("content", "").strip(), "Missing license text: " + key
manifest = subprocess.check_output([aapt, "dump", "xmltree", apk, "AndroidManifest.xml"], text=True)
assert re.search(r"android:allowBackup[^\n]*\(type 0x12\)0x0", manifest), "Cloud backup is not disabled"
assert "dataExtractionRules" in manifest
rules = subprocess.check_output([aapt, "dump", "xmltree", apk, rule_files[0]], text=True)
assert "E: cloud-backup" in rules
expected_domains = {"root", "file", "database", "sharedpref", "external", "device_root", "device_file", "device_database", "device_sharedpref"}
assert set(re.findall(r'A: domain="([^"]+)"', rules)) == expected_domains
assert rules.count("E: exclude") == len(expected_domains)
print(f"APK verified: cloud backup disabled; offline licenses for {len(report['libraries'])} dependencies.")
