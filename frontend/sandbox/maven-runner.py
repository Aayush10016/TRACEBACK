import json
import os
from pathlib import Path, PurePosixPath
import stat
import subprocess
import sys
import xml.etree.ElementTree as ET
import zipfile

MAX_EXPANDED = 40 * 1024 * 1024
MAX_LOG = 24 * 1024


def extract(zip_path, root):
    with zipfile.ZipFile(zip_path) as archive:
        entries = archive.infolist()
        if not entries or len(entries) > 1500 or sum(e.file_size for e in entries) > MAX_EXPANDED:
            raise ValueError("ZIP exceeds the file count or 40 MB expanded limit.")
        for entry in entries:
            path = PurePosixPath(entry.filename)
            if (not path.parts or path.is_absolute() or ".." in path.parts or "\\" in entry.filename
                    or ":" in entry.filename or stat.S_ISLNK(entry.external_attr >> 16)):
                raise ValueError("ZIP contains an unsafe path or a symbolic link.")
            if any(part.lower() in {".env", ".ssh", "id_rsa", "id_ed25519"}
                   or part.lower().startswith(".env.") and part.lower() != ".env.example"
                   for part in path.parts):
                raise ValueError("Remove credentials and .env files before uploading.")
            if any(part in {"target", ".git"} for part in path.parts):
                continue
            dest = root.joinpath(*path.parts)
            if entry.is_dir():
                dest.mkdir(parents=True, exist_ok=True)
                continue
            dest.parent.mkdir(parents=True, exist_ok=True)
            with archive.open(entry) as source, dest.open("xb") as target:
                while chunk := source.read(65536):
                    target.write(chunk)
    poms = sorted(root.rglob("pom.xml"), key=lambda p: len(p.parts))
    if not poms or any(not pom.is_relative_to(poms[0].parent) for pom in poms):
        raise ValueError("Include one Maven project with a root pom.xml.")
    return poms[0].parent


def count_reports(root):
    counts = dict(tests=0, failures=0, errors=0, skipped=0)
    for report in root.rglob("TEST-*.xml"):
        if (report.parent.name not in {"surefire-reports", "failsafe-reports"}
                or report.parent.parent.name != "target" or report.is_symlink()
                or not report.resolve().is_relative_to(root.resolve())
                or not report.is_file() or report.stat().st_size > 1024 * 1024):
            continue
        data = report.read_bytes()
        if b"<!DOCTYPE" in data.upper() or b"<!ENTITY" in data.upper():
            continue
        try:
            suite = ET.fromstring(data)
            if suite.tag != "testsuite":
                continue
            for key in counts:
                counts[key] += max(0, int(suite.get(key, 0)))
        except (ET.ParseError, ValueError):
            continue
    return counts


def run(maven, project, root):
    with subprocess.Popen(
        ["timeout", "--kill-after=5s", "105s", maven, "-B", "-ntp", "clean", "verify"],
        cwd=project, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        env={**os.environ, "JAVA_TOOL_OPTIONS": "-Xmx1024m"},
    ) as process:
        tail = bytearray()
        while chunk := process.stdout.read(8192):
            tail.extend(chunk)
            if len(tail) > MAX_LOG:
                del tail[:-MAX_LOG]
        code = process.wait()
    counts = count_reports(root)
    if code in (124, 137):
        status = "TIMED_OUT"
    elif counts["failures"] or counts["errors"]:
        status = "TESTS_FAILED"
    elif code:
        status = "BUILD_FAILED"
    elif counts["tests"] <= counts["skipped"]:
        status = "NO_TESTS"
    else:
        status = "PASSED"
    return dict(status=status, counts=counts, output=tail.decode("utf-8", "replace"))


def main():
    archive, destination, maven = sys.argv[1:]
    root = Path(destination).resolve()
    root.mkdir()
    try:
        project = extract(archive, root)
    except (OSError, ValueError, RuntimeError, zipfile.BadZipFile) as error:
        print(json.dumps(dict(status="INVALID_ZIP", message=str(error))))
        return
    print(json.dumps(run(maven, project, root)))


if __name__ == "__main__":
    main()
