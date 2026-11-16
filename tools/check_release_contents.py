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
    reports = [name for name in package.namelist() if name.endswith("/aboutlibraries.json")]
    assert len(reports) == 1, reports
    report = json.loads(package.read(reports[0]))
    assert report["libraries"], "Empty dependency report"
    for library in report["libraries"]:
        assert library.get("licenses"), "Missing license: " + library["uniqueId"]
        for key in library["licenses"]:
            assert report["licenses"][key].get("content", "").strip(), "Missing license text: " + key
manifest = subprocess.check_output([aapt, "dump", "xmltree", apk, "AndroidManifest.xml"], text=True)
assert re.search(r"android:allowBackup[^\n]*\(type 0x12\)0x0", manifest), "Cloud backup is not disabled"
assert "dataExtractionRules" in manifest
rules = subprocess.check_output([aapt, "dump", "xmltree", apk, "res/xml/data_extraction_rules.xml"], text=True)
assert "E: cloud-backup" in rules
expected_domains = {"root", "file", "database", "sharedpref", "external", "device_root", "device_file", "device_database", "device_sharedpref"}
assert set(re.findall(r'A: domain="([^"]+)"', rules)) == expected_domains
assert rules.count("E: exclude") == len(expected_domains)
print(f"APK verified: cloud backup disabled; offline licenses for {len(report['libraries'])} dependencies.")
