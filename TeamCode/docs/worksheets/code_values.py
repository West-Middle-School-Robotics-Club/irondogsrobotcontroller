"""
Reads the CURRENT values of our tunable constants straight from the Java code, so the printed worksheets
always show what the robot code says (no numbers typed in by hand that go out of date).

Used by make_test_worksheets.py and make_controller_layout.py:
    now("NORMAL_SCALE")                          -> "0.8"
    now("MAX_LEFT_DEGREES", "MAX_RIGHT_DEGREES") -> "120.0 / -120.0"
    pct("NORMAL_SCALE")                          -> "80%"
It looks for lines like:  public static final double NORMAL_SCALE = 0.8;
"""
import os
import re

CODE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "src", "main", "java",
                    "org", "firstinspires", "ftc", "teamcode")
_values = None


def _load():
    global _values
    if _values is None:
        _values = {}
        for root, _dirs, files in os.walk(CODE):
            for name in files:
                if name.endswith(".java"):
                    with open(os.path.join(root, name), encoding="utf-8") as f:
                        src = f.read()
                    for m in re.finditer(r"static\s+final\s+[\w.\[\]<>]+\s+([A-Z][A-Z0-9_]+)\s*=\s*([^;]+);", src):
                        value = " ".join(m.group(2).split())
                        is_number = re.fullmatch(r"-?[\d.]+", value)
                        if not is_number and re.fullmatch(r"[\w.]+", value):
                            value = value.split(".")[-1]   # SwitchWiring.NORMALLY_CLOSED -> NORMALLY_CLOSED
                        _values.setdefault(m.group(1), value)
    return _values


def now(*names):
    """The current value(s) in the code, joined with " / "."""
    values = _load()
    missing = [n for n in names if n not in values]
    if missing:
        raise KeyError("not found in the Java code: " + ", ".join(missing))
    return " / ".join(values[n] for n in names)


def pct(name):
    """A 0-to-1 value as a percentage, e.g. 0.8 -> "80%"."""
    return "%d%%" % round(float(now(name)) * 100)
