"""
Make printable BIOBUZZ field diagrams for planning, alliance meetings and the portfolio.

Run from this folder:
    python3 make_diagrams.py

It writes, for each diagram:
    <name>.svg  sharp at any size (open in a browser to print)
    <name>.pdf  letter size, ready to print      (needs: pip install pymupdf)
    <name>.png  for the portfolio or a phone     (needs: pip install pymupdf)

Field positions use OUR field coordinate system (decision 011), in inches:
    origin = center of the field, +X = toward the far wall, +Y = toward the red wall.
These are the same numbers the autonomous code uses, so diagrams match what the robot does.
Positions of field elements are approximate (about +/- 2 in). See ../field.md for sources.

To change what the alliance sheet says about our robot, edit OUR_AUTO and OUR_TELEOP below.
"""

import os
from html import escape  # notes from the code may contain < or &, which SVG doesn't allow

# ---------------------------------------------------------------------------
# What our robot can do (shown on the alliance sheet). Update as the robot improves!
# Each item: (text, checked). checked=True draws a checked box.
# ---------------------------------------------------------------------------
TEAM_NAME = "WMS Irondogs"

OUR_AUTO = [
    ("LEAVE + PARK in LOADING ZONE (8 pts)", False),
    ("Start spots: Near / Far (see route sheet)", False),
    ("Start delay: 0-10 s (set at INIT)", False),
    ("Launch 4 preloads into CELL", False),
]

OUR_TELEOP = [
    ("Collect POLLEN from floor", False),
    ("Launch into upward CELL (HIVE TIP)", False),
    ("Place NECTAR in FLOWER", False),
    ("Push to GARDEN", False),
    ("PARK in LOADING ZONE at end", False),
]

# ---------------------------------------------------------------------------
# Field geometry, in OUR field coordinates (inches). From Competition Manual Section 9.
# ---------------------------------------------------------------------------
HALF = 72.0  # field is 144 x 144 in

# Rectangles as (x_min, x_max, y_min, y_max)
RED_LOADING_ZONE = (24.0, 47.0, 61.0, 72.0)     # red wall, tile A5, ~23 x 11 in
BLUE_LOADING_ZONE = (-47.0, -24.0, -72.0, -61.0)  # blue wall, tile F2 (Red rotated 180 deg)
RED_GARDEN = (-72.0, -70.0, 49.0, 72.0)          # along audience wall from corner A1, ~23 x 2 in
BLUE_GARDEN = (70.0, 72.0, -72.0, -49.0)         # along far wall from corner F6

HIVE_FRAME = (-24.7, 24.7, -19.5, 19.5)          # frame base ~49.5 x 39 in, field center
CELL_W_Y = 20.0                                  # CELL opening ~20 in wide
CELL_D_X = 13.0                                  # drawn depth (top-down view, approximate)
# (x, y, alliance, UP at match start?, AprilTag IDs). At match start the CELL that points at a FLOWER is DOWN
# (Competition Manual p. 83, field.md): Red's AUDIENCE-side CELL and Blue's FAR-side CELL start UP.
# Every HIVE TIP flips which CELL is up, so this is only the STARTING position.
CELL_CENTERS = [
    (12.0, 11.0, "red", False, "30-33"), (-12.0, 11.0, "red", True, "34-37"),
    (12.0, -11.0, "blue", True, "42-45"), (-12.0, -11.0, "blue", False, "38-41"),
]

FLOWERS = [(-24.0, 69.0), (69.0, 24.0), (24.0, -69.0), (-69.0, -24.0)]  # on the walls
FLOWER_R = 3.0

COLUMNS = "ABCDEF"  # left to right from the audience (red to blue)

# Colors (still readable in black and white because everything is also labeled)
RED = "#d62828"
BLUE = "#1d4ed8"
RED_TINT = "#fde8e8"
BLUE_TINT = "#e3ebfd"
GREY = "#6b7280"
INK = "#111827"
FONT = "Helvetica, Arial, sans-serif"

PAGE_W, PAGE_H = 850, 1100  # letter page, 1 unit = 0.01 in


class Field:
    """Draws the field at any position and scale on the page."""

    def __init__(self, left, top, scale):
        self.left, self.top, self.s = left, top, scale  # scale = page units per inch

    # Our coordinates -> page coordinates. Audience at the bottom, red wall on the left.
    def px(self, y):
        return self.left + (HALF - y) * self.s

    def py(self, x):
        return self.top + (HALF - x) * self.s

    def size(self):
        return 2 * HALF * self.s

    def rect(self, r, fill, stroke="none", sw=1, extra=""):
        x0, x1, y0, y1 = r
        return (f'<rect x="{self.px(y1):.1f}" y="{self.py(x1):.1f}" '
                f'width="{(y1 - y0) * self.s:.1f}" height="{(x1 - x0) * self.s:.1f}" '
                f'fill="{fill}" stroke="{stroke}" stroke-width="{sw}" {extra}/>')

    def draw(self, show_axes=True, start_hints=True):
        s, out = self.s, []
        L, T, W = self.left, self.top, self.size()
        fs = max(9, 3.0 * s)  # label font size

        # Red and blue halves (AUTO: red = columns A-C, blue = D-F)
        out.append(self.rect((-HALF, HALF, 0, HALF), RED_TINT))
        out.append(self.rect((-HALF, HALF, -HALF, 0), BLUE_TINT))

        # Tiles
        for i in range(1, 6):
            out.append(f'<line x1="{L + i * 24 * s:.1f}" y1="{T}" x2="{L + i * 24 * s:.1f}" '
                       f'y2="{T + W}" stroke="#c4c8d0" stroke-width="1"/>')
            out.append(f'<line x1="{L}" y1="{T + i * 24 * s:.1f}" x2="{L + W}" '
                       f'y2="{T + i * 24 * s:.1f}" stroke="#c4c8d0" stroke-width="1"/>')
        # Center line between the halves
        out.append(f'<line x1="{self.px(0):.1f}" y1="{T}" x2="{self.px(0):.1f}" y2="{T + W}" '
                   f'stroke="{GREY}" stroke-width="1.5" stroke-dasharray="6,4"/>')

        # Tile labels: columns top and bottom, rows left and right
        for c, name in enumerate(COLUMNS):
            cx = L + (c + 0.5) * 24 * s
            out.append(self.text(cx, T - 0.25 * fs, name, fs, GREY, "middle", bold=True))
            out.append(self.text(cx, T + W + 1.1 * fs, name, fs, GREY, "middle", bold=True))
        for r in range(6):
            cy = T + W - (r + 0.5) * 24 * s + 0.35 * fs
            out.append(self.text(L - 0.6 * fs, cy, str(r + 1), fs, GREY, "middle", bold=True))
            out.append(self.text(L + W + 0.6 * fs, cy, str(r + 1), fs, GREY, "middle", bold=True))

        # LOADING ZONES and GARDENS
        out.append(self.rect(RED_LOADING_ZONE, "#f6b3b3", RED, 2))
        out.append(self.rect(BLUE_LOADING_ZONE, "#b3c6f6", BLUE, 2))
        out.append(self.rect(RED_GARDEN, RED))
        out.append(self.rect(BLUE_GARDEN, BLUE))
        small = 0.75 * fs
        out.append(self.text(self.px(61) + 0.4 * small, self.py(35.5) - 0.15 * small,
                             "LOADING", small, RED, "start", bold=True))
        out.append(self.text(self.px(61) + 0.4 * small, self.py(35.5) + 0.85 * small,
                             "ZONE", small, RED, "start", bold=True))
        out.append(self.text(self.px(-61) - 0.4 * small, self.py(-35.5) - 0.15 * small,
                             "LOADING", small, BLUE, "end", bold=True))
        out.append(self.text(self.px(-61) - 0.4 * small, self.py(-35.5) + 0.85 * small,
                             "ZONE", small, BLUE, "end", bold=True))
        out.append(self.text(self.px(60.5), self.py(-70) - 0.4 * small, "GARDEN", small, RED, "middle", bold=True))
        out.append(self.text(self.px(-60.5), self.py(70) + 1.2 * small, "GARDEN", small, BLUE, "middle", bold=True))

        # HIVE structure and CELLS (approximate, top-down)
        out.append(self.rect(HIVE_FRAME, "#f3f4f6", GREY, 1.5))
        for (cx, cy, color, up, tags) in CELL_CENTERS:
            r = (cx - CELL_D_X / 2, cx + CELL_D_X / 2, cy - CELL_W_Y / 2, cy + CELL_W_Y / 2)
            c = RED if color == "red" else BLUE
            if up:   # UP at match start: filled with the alliance color
                out.append(self.rect(r, c, c, 2.5, 'fill-opacity="0.35"'))
                out.append(self.text(self.px(cy), self.py(cx) + 0.05 * small, "UP", 1.15 * small, c, "middle", bold=True))
            else:
                out.append(self.rect(r, "white", c, 2.5))
                out.append(self.text(self.px(cy), self.py(cx) + 0.05 * small, "down", 0.85 * small, GREY, "middle"))
            out.append(self.text(self.px(cy), self.py(cx) + 1.05 * small, "tags " + tags, 0.6 * small, GREY, "middle"))
        out.append(self.text(self.px(0), self.py(HIVE_FRAME[1]) + 1.05 * small, "HIVE", small, INK, "middle", bold=True))

        # FLOWERS
        for (fx, fy) in FLOWERS:
            out.append(f'<circle cx="{self.px(fy):.1f}" cy="{self.py(fx):.1f}" r="{FLOWER_R * s:.1f}" '
                       f'fill="#fde68a" stroke="#a16207" stroke-width="1.5"/>')

        # Perimeter
        out.append(f'<rect x="{L}" y="{T}" width="{W}" height="{W}" fill="none" stroke="{INK}" stroke-width="3"/>')

        # Axes of our coordinate system (decision 011)
        if show_axes:
            ox, oy, a = self.px(0), self.py(0), 9 * s
            out.append(f'<circle cx="{ox}" cy="{oy}" r="2.5" fill="{INK}"/>')
            out.append(self.arrow(ox, oy, ox, oy - a, INK, 1.5))
            out.append(self.arrow(ox, oy, ox - a, oy, INK, 1.5))
            out.append(self.text(ox + 3, oy - a + 0.2 * small, "+X", 0.7 * small, INK, "start"))
            out.append(self.text(ox - a, oy - 4, "+Y", 0.7 * small, INK, "middle"))

        # Wall and side labels
        out.append(self.text(L + W / 2, T - 1.6 * fs, "FAR WALL", fs, GREY, "middle"))
        out.append(self.text(L + W / 2, T + W + 2.5 * fs, "AUDIENCE", fs, GREY, "middle", bold=True))
        out.append(self.vtext(L - 1.9 * fs, T + W / 2, "RED ALLIANCE AREA", fs, RED))
        out.append(self.vtext(L + W + 1.9 * fs, T + W / 2, "BLUE ALLIANCE AREA", fs, BLUE, rotate=90))
        out.append(self.text(self.px(36), self.py(-36), "RED SIDE (A-C)", 0.8 * fs, RED, "middle", bold=True))
        out.append(self.text(self.px(-36), self.py(36), "BLUE SIDE (D-F)", 0.8 * fs, BLUE, "middle", bold=True))
        return "\n".join(out)

    @staticmethod
    def text(x, y, txt, size, color, anchor="start", bold=False):
        w = ' font-weight="bold"' if bold else ""
        return (f'<text x="{x:.1f}" y="{y:.1f}" font-family="{FONT}" font-size="{size:.1f}" '
                f'fill="{color}" text-anchor="{anchor}"{w}>{txt}</text>')

    @staticmethod
    def vtext(x, y, txt, size, color, rotate=-90):
        return (f'<text x="{x:.1f}" y="{y:.1f}" font-family="{FONT}" font-size="{size:.1f}" fill="{color}" '
                f'font-weight="bold" text-anchor="middle" transform="rotate({rotate} {x:.1f} {y:.1f})">{txt}</text>')

    @staticmethod
    def arrow(x0, y0, x1, y1, color, sw):
        import math
        ang = math.atan2(y1 - y0, x1 - x0)
        h = 6
        p1 = (x1 - h * math.cos(ang - 0.45), y1 - h * math.sin(ang - 0.45))
        p2 = (x1 - h * math.cos(ang + 0.45), y1 - h * math.sin(ang + 0.45))
        return (f'<line x1="{x0:.1f}" y1="{y0:.1f}" x2="{x1:.1f}" y2="{y1:.1f}" stroke="{color}" stroke-width="{sw}"/>'
                f'<polygon points="{x1:.1f},{y1:.1f} {p1[0]:.1f},{p1[1]:.1f} {p2[0]:.1f},{p2[1]:.1f}" fill="{color}"/>')


# ---------------------------------------------------------------------------
# Page helpers
# ---------------------------------------------------------------------------
def svg_page(body):
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="8.5in" height="11in" '
            f'viewBox="0 0 {PAGE_W} {PAGE_H}">\n<rect width="{PAGE_W}" height="{PAGE_H}" fill="white"/>\n'
            f'{body}\n</svg>\n')


def blank(x, y, w, label, size=13):
    """A label followed by a line to write on."""
    return (Field.text(x, y, label, size, INK) +
            f'<line x1="{x + len(label) * size * 0.55:.1f}" y1="{y + 3}" x2="{x + w}" y2="{y + 3}" '
            f'stroke="{GREY}" stroke-width="1"/>')


def checkbox(x, y, label, checked=False, size=12):
    box = size * 0.95
    out = f'<rect x="{x}" y="{y - box + 2}" width="{box}" height="{box}" fill="white" stroke="{INK}" stroke-width="1.2"/>'
    if checked:
        out += (f'<polyline points="{x + 2},{y - box / 2 + 2} {x + box / 2},{y} {x + box - 1},{y - box + 3}" '
                f'fill="none" stroke="{INK}" stroke-width="2"/>')
    return out + Field.text(x + box + 6, y, label, size, INK)


def box(x, y, w, h, title, color=INK):
    return (f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="none" stroke="{color}" '
            f'stroke-width="1.5" rx="6"/>' + Field.text(x + 10, y + 20, title, 15, color, bold=True))


def legend(x, y, size=11, vertical=False):
    """Explains the field symbols. vertical=True stacks the items."""
    items = [
        (f'<circle cx="{{cx}}" cy="{{cy}}" r="{size * 0.45}" fill="#fde68a" stroke="#a16207" stroke-width="1.2"/>', "FLOWER"),
        (f'<rect x="{{x0}}" y="{{y0}}" width="{size * 1.1}" height="{size * 0.8}" fill="#f6b3b3" stroke="{RED}" stroke-width="1.5"/>', "LOADING ZONE (PARK)"),
        (f'<rect x="{{x0}}" y="{{y1}}" width="{size * 1.1}" height="{size * 0.3}" fill="{RED}"/>', "GARDEN"),
        (f'<rect x="{{x0}}" y="{{y0}}" width="{size * 1.1}" height="{size * 0.8}" fill="{BLUE}" fill-opacity="0.35" stroke="{BLUE}" stroke-width="2"/>', "UP CELL at start"),
        (f'<rect x="{{x0}}" y="{{y0}}" width="{size * 1.1}" height="{size * 0.8}" fill="white" stroke="{BLUE}" stroke-width="2"/>', "CELL down"),
        (f'<line x1="{{x0}}" y1="{{cy}}" x2="{{x2}}" y2="{{cy}}" stroke="{GREY}" stroke-width="1.5" stroke-dasharray="4,3"/>', "Center line"),
    ]
    out, cx_, cy_ = [], x, y
    for sym, label in items:
        x0, y0 = cx_, cy_ - size * 0.75
        out.append(sym.format(cx=cx_ + size * 0.55, cy=cy_ - size * 0.35, x0=x0, y0=y0,
                              y1=cy_ - size * 0.5, x2=x0 + size * 1.1))
        out.append(Field.text(cx_ + size * 1.6, cy_, label, size, INK))
        if vertical:
            cy_ += size * 1.7
        else:
            cx_ += size * 1.6 + len(label) * size * 0.55 + 18
    return "\n".join(out)


def footer():
    return Field.text(PAGE_W / 2, PAGE_H - 12,
                      "Field positions approximate. Source: BIOBUZZ Competition Manual Section 9. "
                      "Generated by TeamCode/docs/game/diagrams/make_diagrams.py",
                      9, GREY, "middle")


# ---------------------------------------------------------------------------
# Diagrams
# ---------------------------------------------------------------------------
def field_template():
    """One big blank field for sketching routes, with room for notes."""
    out = [Field.text(60, 55, f"BIOBUZZ Field Planner: {TEAM_NAME}", 22, INK, bold=True),
           blank(60, 85, 210, "Match #"), blank(300, 85, 250, "Team #"),
           checkbox(600, 87, "Red", size=13), checkbox(680, 87, "Blue", size=13)]
    f = Field(left=95, top=150, scale=4.4)  # 144 in * 4.4 = 634 units (6.3 in)
    out.append(f.draw())
    out.append(legend(60, 150 + f.size() + 62, 10.5))
    out.append(Field.text(60, 150 + f.size() + 82,
                          "Coordinates (decision 011): origin = field center, +X toward far wall, +Y toward red wall, in inches. "
                          "Blue = Red rotated 180\u00b0. Stay on your own side in AUTO (G402).", 10, GREY))
    out.append(Field.text(60, 150 + f.size() + 97,
                          "HIVE: the UP CELLS shown are at MATCH START (Red: audience side, Blue: far side). "
                          "Every TIP flips which CELL of that HIVE is up.", 10, GREY))
    y = 150 + f.size() + 125
    out.append(Field.text(60, y, "Notes", 15, INK, bold=True))
    for i in range(3):
        out.append(f'<line x1="60" y1="{y + 30 + i * 28}" x2="{PAGE_W - 60}" y2="{y + 30 + i * 28}" '
                   f'stroke="#c4c8d0" stroke-width="1"/>')
    out.append(footer())
    return svg_page("\n".join(out))


def alliance_sheet():
    """One page to bring to alliance planning: us, our partner, and the agreed plan."""
    out = [Field.text(50, 50, f"Alliance Planning Sheet: {TEAM_NAME}", 22, INK, bold=True),
           blank(50, 82, 170, "Match #"), blank(245, 82, 230, "Partner team #"),
           checkbox(520, 84, "Red", size=13), checkbox(595, 84, "Blue", size=13),
           Field.text(680, 84, "AUTO = 30 s", 12, GREY)]

    # Our robot
    bx, by, bw, bh = 50, 105, 365, 268
    out.append(box(bx, by, bw, bh, "Our robot", RED))
    out.append(Field.text(bx + 12, by + 45, "AUTO", 12, GREY, bold=True))
    for i, (t, c) in enumerate(OUR_AUTO):
        out.append(checkbox(bx + 12, by + 65 + i * 20, t, c, 11.5))
    ty = by + 65 + len(OUR_AUTO) * 20 + 8
    out.append(Field.text(bx + 12, ty, "TELEOP", 12, GREY, bold=True))
    for i, (t, c) in enumerate(OUR_TELEOP):
        out.append(checkbox(bx + 12, ty + 20 + i * 20, t, c, 11.5))

    # Partner robot
    px_ = 435
    out.append(box(px_, by, bw, bh, "Partner robot", BLUE))
    out.append(Field.text(px_ + 12, by + 45, "AUTO", 12, GREY, bold=True))
    partner_auto = ["No AUTO", "LEAVE only", "LEAVE + PARK", "Launches preloads: ___ of 4"]
    for i, t in enumerate(partner_auto):
        out.append(checkbox(px_ + 12, by + 65 + i * 20, t, False, 11.5))
    out.append(blank(px_ + 12, by + 65 + len(partner_auto) * 20 + 2, bw - 24, "Start tile / wall:", 11.5))
    ty = by + 65 + len(partner_auto) * 20 + 30
    out.append(Field.text(px_ + 12, ty, "TELEOP", 12, GREY, bold=True))
    partner_tele = ["Collects POLLEN", "Launches into CELL", "FLOWERS", "GARDEN / defense"]
    for i, t in enumerate(partner_tele):
        col, row = i % 2, i // 2
        out.append(checkbox(px_ + 12 + col * 170, ty + 20 + row * 20, t, False, 11.5))

    # Field for drawing both routes
    f = Field(left=220, top=425, scale=3.0)  # 432 units (4.3 in)
    out.append(Field.text(50, 412, "Draw the AUTO routes", 15, INK, bold=True))
    out.append(Field.text(50, 434, "Us: solid line", 11.5, INK))
    out.append(Field.text(50, 452, "Partner: dashed line", 11.5, INK))
    out.append(Field.text(50, 470, "Start spots: X", 11.5, INK))
    out.append(legend(50, 520, 10, vertical=True))
    out.append(f.draw(show_axes=False))

    # Agreed plan
    ay = 425 + f.size() + 50
    out.append(box(50, ay, PAGE_W - 100, PAGE_H - ay - 30, "Agreed plan"))
    out.append(blank(62, ay + 48, 340, "We start at:", 12.5))
    out.append(blank(430, ay + 48, 345, "Partner starts at:", 12.5))
    out.append(blank(62, ay + 78, 340, "Our start delay (s):", 12.5))
    out.append(blank(430, ay + 78, 345, "Partner delay (s):", 12.5))
    out.append(Field.text(62, ay + 108, "LOADING ZONE park:", 12.5, INK))
    out.append(checkbox(200, ay + 108, "We park far-wall third", False, 12.5))
    out.append(checkbox(380, ay + 108, "We park audience third", False, 12.5))
    out.append(Field.text(62, ay + 136, "SWARM RP: both robots LEAVE + PARK in AUTO = 16 pts", 12.5, RED, bold=True))
    out.append(footer())
    return svg_page("\n".join(out))


# ---------------------------------------------------------------------------
# AUTO route sheets (issue #22), drawn from the SAME positions as the robot code.
# They are read straight out of BasicAuton.java, so the diagram always matches what the robot does.
# Change a position in the code, re-run this script, and the picture updates.
# ---------------------------------------------------------------------------
BASIC_AUTON_JAVA = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "..", "src", "main", "java",
                                "org", "firstinspires", "ftc", "teamcode", "opmodes", "auto", "BasicAuton.java")


def read_basic_auton():
    """Reads robot size, start positions and paths from BasicAuton.java. Returns (consts, routes)."""
    import re
    src = open(BASIC_AUTON_JAVA).read()
    consts = {}
    for name in ("ROBOT_LENGTH_IN", "ROBOT_WIDTH_IN"):
        consts[name] = float(re.search(name + r"\s*=\s*([0-9.]+)", src).group(1))

    def ev(expr):  # evaluates things like "72 - ROBOT_WIDTH_IN / 2" (only numbers and our constants)
        return float(eval(expr, {"__builtins__": {}}, dict(consts)))

    pose_re = r"redPose\(\s*([^,]+?)\s*,\s*([^,]+?)\s*,\s*([^)]+?)\s*\)"
    routes = {}
    for route in ("NEAR", "FAR"):
        s = re.search(r"RED_%s_START\s*=\s*%s" % (route, pose_re), src)
        start = tuple(ev(g) for g in s.groups())
        block = re.search(r"RED_%s_PATH\s*=\s*\{(.*?)\};" % route, src, re.S).group(1)
        path = []
        for line in block.splitlines():
            m = re.search(pose_re, line)
            if m:
                note = line.split("//", 1)[1].strip() if "//" in line else ""
                path.append((tuple(ev(g) for g in m.groups()), note))
        routes[route] = (start, path)
    # The plain-words setup from BasicAuton.setupText(): NEAR first, then FAR, each as (red, blue)
    setups = re.findall(r'red \?\s*"([^"]+)"\s*:\s*"([^"]+)"', src)
    consts["SETUP"] = {"NEAR": setups[0], "FAR": setups[1]}
    return consts, routes


def to_blue(pose):
    """Blue = Red rotated 180 degrees (decision 011)."""
    x, y, h = pose
    return (-x, -y, (h + 180) % 360)


def robot_outline(f, pose, length, width, color, dashed=False, fill="none", label="", mark_back=False):
    """The robot's footprint, with an arrow showing which way it faces. mark_back = thick black bar on its BACK edge."""
    import math
    x, y, h = pose
    hr = math.radians(h)
    fx, fy = math.cos(hr), math.sin(hr)       # forward, field coordinates
    lx, ly = -math.sin(hr), math.cos(hr)      # left
    corners = []
    for a, b in ((1, 1), (1, -1), (-1, -1), (-1, 1)):
        cx = x + a * length / 2 * fx + b * width / 2 * lx
        cy = y + a * length / 2 * fy + b * width / 2 * ly
        corners.append(f"{f.px(cy):.1f},{f.py(cx):.1f}")
    dash = ' stroke-dasharray="6,4"' if dashed else ""
    out = [f'<polygon points="{" ".join(corners)}" fill="{fill}" fill-opacity="0.35" stroke="{color}" '
           f'stroke-width="2.5"{dash}/>']
    tip = (x + length * 0.45 * fx, y + length * 0.45 * fy)
    out.append(f.arrow(f.px(y), f.py(x), f.px(tip[1]), f.py(tip[0]), color, 2))
    if mark_back:  # corners 2 and 3 are the back corners (a = -1)
        (bx0, by0), (bx1, by1) = [tuple(float(v) for v in c.split(",")) for c in corners[2:4]]
        out.append(f'<line x1="{bx0:.1f}" y1="{by0:.1f}" x2="{bx1:.1f}" y2="{by1:.1f}" stroke="{INK}" '
                   f'stroke-width="5" stroke-linecap="round"/>')
    if label:
        out.append(Field.text(f.px(y), f.py(x) + 0.5 * f.s * 3, label, 2.6 * f.s, color, "middle", bold=True))
    return "\n".join(out)


def draw_route(f, start, path, length, width, color):
    """Start footprint, numbered waypoints joined by arrows, and the park footprint (dashed)."""
    out = [robot_outline(f, start, length, width, color, fill=color, label="START", mark_back=True)]
    prev = start
    for i, (wp, _note) in enumerate(path, start=1):
        out.append(f.arrow(f.px(prev[1]), f.py(prev[0]), f.px(wp[1]), f.py(wp[0]), color, 3))
        out.append(f'<circle cx="{f.px(wp[1]):.1f}" cy="{f.py(wp[0]):.1f}" r="{2.6 * f.s:.1f}" fill="white" '
                   f'stroke="{color}" stroke-width="2"/>')
        out.append(Field.text(f.px(wp[1]), f.py(wp[0]) + 0.9 * f.s, str(i), 2.8 * f.s, color, "middle", bold=True))
        prev = wp
    out.append(robot_outline(f, path[-1][0], length, width, color, dashed=True))
    return "\n".join(out)


def setup_panel(x0, y0, w, route, red_text, blue_text):
    """Big, simple SET UP instructions with a close-up: the robot's BACK on the wall (decision 046)."""
    import textwrap
    out = [f'<rect x="{x0}" y="{y0}" width="{w}" height="490" fill="#fef9c3" stroke="#a16207" stroke-width="2" rx="8"/>',
           Field.text(x0 + w / 2, y0 + 30, "SET UP", 24, INK, "middle", bold=True),
           Field.text(x0 + w / 2, y0 + 52, "BACK against the wall", 15, INK, "middle", bold=True)]

    def closeup(cx, top, good):
        """A wall (left) and a robot seen from above. good = back on the wall; else its side (wrong)."""
        parts = [f'<rect x="{cx - 70}" y="{top}" width="12" height="80" fill="#9ca3af"/>',
                 Field.text(cx - 64, top + 94, "WALL", 9, GREY, "middle", bold=True)]
        if good:
            rx, ry, rw, rh = cx - 58, top + 16, 64, 48           # robot: back (left edge) on the wall
            parts.append(f'<rect x="{rx}" y="{ry}" width="{rw}" height="{rh}" fill="#fecaca" stroke="{INK}" stroke-width="2"/>')
            parts.append(f'<line x1="{rx}" y1="{ry}" x2="{rx}" y2="{ry + rh}" stroke="{INK}" stroke-width="6"/>')
            parts.append(Field.text(rx + 6, ry + rh / 2 + 4, "BACK", 10, INK, "start", bold=True))
            parts.append(Field.arrow(rx + rw - 8, ry + rh / 2, rx + rw + 40, ry + rh / 2, INK, 3))
            parts.append(Field.text(rx + rw + 22, ry + rh / 2 - 8, "FRONT", 10, INK, "middle", bold=True))
        else:
            rx, ry, rw, rh = cx - 58, top + 8, 48, 64            # robot sideways: its SIDE on the wall
            parts.append(f'<rect x="{rx}" y="{ry}" width="{rw}" height="{rh}" fill="#e5e7eb" stroke="{GREY}" stroke-width="2"/>')
            parts.append(Field.arrow(rx + rw / 2, ry + rh - 6, rx + rw / 2, ry + 4, GREY, 3))
            parts.append(f'<line x1="{cx - 75}" y1="{top}" x2="{cx + 20}" y2="{top + 80}" stroke="{RED}" stroke-width="6"/>')
            parts.append(f'<line x1="{cx + 20}" y1="{top}" x2="{cx - 75}" y2="{top + 80}" stroke="{RED}" stroke-width="6"/>')
        return parts

    out += closeup(x0 + w / 2, y0 + 70, good=True)
    out.append(Field.text(x0 + w / 2, y0 + 190, "YES: back on the wall", 12, "#15803d", "middle", bold=True))
    out += closeup(x0 + w / 2 + 25, y0 + 205, good=False)
    out.append(Field.text(x0 + w / 2, y0 + 318, "NO: side on the wall", 12, RED, "middle", bold=True))

    y = y0 + 348
    for color, label, text in ((RED, "RED", red_text), (BLUE, "BLUE", blue_text)):
        out.append(Field.text(x0 + 12, y, label + ":", 12, color, bold=True))
        for line in textwrap.wrap(text, 27):
            y += 15
            out.append(Field.text(x0 + 12, y, escape(line), 11.5, INK))
        y += 22
    out.append(Field.text(x0 + w / 2, y0 + 480, "Same as the SET UP line on the INIT screen", 9, GREY, "middle"))
    return "\n".join(out)


def route_sheet(route, consts, routes):
    """One printable page per route: the field with RED and BLUE drawn, the steps, and a test log."""
    start, path = routes[route]
    length, width = consts["ROBOT_LENGTH_IN"], consts["ROBOT_WIDTH_IN"]
    where = {"NEAR": "our alliance wall, next to the LOADING ZONE",
             "FAR": "the audience wall (Red) / far wall (Blue)"}[route]
    out = [Field.text(50, 48, f"Basic Auton {route}: LEAVE + PARK  ({TEAM_NAME})", 21, INK, bold=True),
           Field.text(50, 72, f"Robot's BACK against {where}. OpModes: \"Basic Auton RED {route}\" and "
                      f"\"Basic Auton BLUE {route}\".", 12, GREY)]

    f = Field(left=95, top=120, scale=3.3)  # 475 units, leaves room for the SET UP panel on the right
    red_setup, blue_setup = consts["SETUP"][route]
    out.append(setup_panel(620, 105, 200, route, red_setup, blue_setup))
    out.append(f.draw(show_axes=False))
    out.append(draw_route(f, start, path, length, width, RED))
    out.append(draw_route(f, to_blue(start), [(to_blue(p), n) for p, n in path], length, width, BLUE))

    # Key
    ky = 120 + f.size() + 50
    out.append(Field.text(50, ky, "Filled box = START (arrow = FRONT, black bar = BACK on the wall).  Numbered circles = waypoints.  "
                          "Dashed box = where it PARKS.", 10.5, INK))
    out.append(Field.text(50, ky + 16, f"Robot size used: {length:g} x {width:g} in (BasicAuton.ROBOT_LENGTH_IN / ROBOT_WIDTH_IN). "
                          "Blue = Red rotated 180° (decision 011).", 10.5, GREY))
    out.append(Field.text(50, ky + 32, "HIVE: the UP CELLS shown are at MATCH START (Red: audience side, Blue: far side). "
                          "Every TIP flips which CELL is up.", 10.5, GREY))

    # Steps table, straight from the code
    ty = ky + 60
    out.append(Field.text(50, ty, "The steps (from BasicAuton.java, in inches)", 14, INK, bold=True))
    cols = [50, 105, 250, 395]
    heads = ["Step", "RED x, y, heading", "BLUE x, y, heading", "What happens"]
    for c, h in zip(cols, heads):
        out.append(Field.text(c, ty + 22, h, 11, GREY, bold=True))
    rows = [("Start", start, "Robot set up here. Nothing moves during INIT")] + \
           [(str(i), p, n) for i, (p, n) in enumerate(path, start=1)]
    for r, (label, pose, note) in enumerate(rows):
        y = ty + 42 + r * 19
        b = to_blue(pose)
        out.append(Field.text(cols[0], y, label, 11, INK, bold=True))
        out.append(Field.text(cols[1], y, f"{pose[0]:g}, {pose[1]:g}, {pose[2]:g}°", 11, RED))
        out.append(Field.text(cols[2], y, f"{b[0]:g}, {b[1]:g}, {b[2]:g}°", 11, BLUE))
        out.append(Field.text(cols[3], y, escape(note)[:80], 11, INK))

    # Test log for students
    ly = ty + 42 + len(rows) * 19 + 28
    out.append(Field.text(50, ly, "Test log: 5 good runs in a row = ready for competition", 14, INK, bold=True))
    heads = ["Run", "Red / Blue", "Delay (s)", "Parked? (Y/N)", "Lights", "Time (s)", "Notes (overshoot, short, bumped...)"]
    xs = [50, 95, 170, 240, 340, 405, 470]
    for x, h in zip(xs, heads):
        out.append(Field.text(x, ly + 22, h, 10.5, GREY, bold=True))
    for r in range(5):
        y = ly + 30 + r * 24
        out.append(f'<line x1="50" y1="{y + 18}" x2="{PAGE_W - 50}" y2="{y + 18}" stroke="#c4c8d0" stroke-width="1"/>')
        out.append(Field.text(58, y + 14, str(r + 1), 11, INK))
    out.append(footer())
    return svg_page("\n".join(out))


def save(name, svg):
    here = os.path.dirname(os.path.abspath(__file__))
    path = os.path.join(here, name + ".svg")
    with open(path, "w") as f:
        f.write(svg)
    print("wrote", path)
    try:
        import pymupdf
    except ImportError:
        print("  (pymupdf not installed: skipping PDF/PNG. Install with: pip install pymupdf)")
        return
    doc = pymupdf.open(path)
    pdf = pymupdf.open("pdf", doc.convert_to_pdf())
    pdf.save(os.path.join(here, name + ".pdf"))
    pdf[0].get_pixmap(dpi=110).save(os.path.join(here, name + ".png"))
    print("wrote", name + ".pdf", "and", name + ".png")


if __name__ == "__main__":
    save("field-template", field_template())
    save("alliance-sheet", alliance_sheet())
    consts, routes = read_basic_auton()
    for route in ("NEAR", "FAR"):
        save("basic-auton-" + route.lower(), route_sheet(route, consts, routes))
