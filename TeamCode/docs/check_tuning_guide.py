"""
Checks that every "Now" value in tuning-guide.md matches the code.

Run (from anywhere):   python3 TeamCode/docs/check_tuning_guide.py
Prints each setting that is out of date, or "All ... values match the code."

How it works: for each table row that starts with a setting name in backticks (like `NORMAL_SCALE`) and has a
"Now" value in backticks (like `0.7`), it finds "static final ... NORMAL_SCALE = ...;" in that row's Java file and compares.
The Java file is the row's File column, or else the file named in the section heading above the table.
"""
import os
import re
import sys

DOCS = os.path.dirname(os.path.abspath(__file__))
GUIDE = os.path.join(DOCS, "tuning-guide.md")
CODE = os.path.join(DOCS, "..", "src", "main", "java", "org", "firstinspires", "ftc", "teamcode")

JAVA_PATH = re.compile(r"`((?:subsystems|opmodes)/[\w/]+\.java)`")
NAME = re.compile(r"^`([A-Z][A-Z0-9_]+)`$")
VALUE = re.compile(r"^`([^`]+)`")


def code_value(java_file, name):
    """The value after 'NAME =' in the Java file (spaces squeezed), or None if not found."""
    with open(os.path.join(CODE, java_file), encoding="utf-8") as f:
        src = f.read()
    # Only real declarations ("static final double NAME = ...;"), not comments that mention the name.
    m = re.search(r"static\s+final\s+[\w.\[\]<>]+\s+%s\s*=\s*([^;]+);" % name, src)
    return " ".join(m.group(1).split()) if m else None


def main():
    heading_file = None
    checked, problems = 0, []
    with open(GUIDE, encoding="utf-8") as f:
        for line_no, line in enumerate(f, start=1):
            if line.startswith("#"):
                m = JAVA_PATH.search(line)
                heading_file = m.group(1) if m else None
                continue
            if not line.startswith("|"):
                continue
            cells = [c.strip() for c in line.strip().strip("|").split("|")]
            name = NAME.match(cells[0])
            if not name:
                continue
            java_file = heading_file
            rest = cells[1:]
            if rest and JAVA_PATH.fullmatch(rest[0]):
                java_file = JAVA_PATH.fullmatch(rest[0]).group(1)
                rest = rest[1:]
            now = VALUE.match(rest[0]) if rest else None
            if not now or not java_file:
                continue
            want = now.group(1)
            have = code_value(java_file, name.group(1))
            checked += 1
            if have is None:
                problems.append("line %d: %s not found in %s" % (line_no, name.group(1), java_file))
            elif have != want and not have.endswith("." + want):
                problems.append("line %d: %s is %s in %s, but the guide says %s"
                                % (line_no, name.group(1), have, java_file, want))

    if problems:
        print("tuning-guide.md is out of date (%d of %d settings):" % (len(problems), checked))
        for p in problems:
            print("  " + p)
        sys.exit(1)
    print("All %d values in tuning-guide.md match the code." % checked)


if __name__ == "__main__":
    main()
