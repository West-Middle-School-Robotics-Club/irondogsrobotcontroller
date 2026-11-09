"""
Makes the printable CONTROLLER LAYOUT: one page per gamepad.
  Page 1  Gamepad 1 = DRIVER    (filled in: what DriverControls.java does)
  Page 2  Gamepad 2 = OPERATOR  (filled in: what OperatorControls.java does. Empty OPERATOR = blanks to decide)

Run:   python3 make_controller_layout.py
Needs: pip install reportlab
Makes: controller-layout.pdf (in this folder)

Keep DRIVER in sync with opmodes/teleop/DriverControls.java and OPERATOR with OperatorControls.java.
Set OPERATOR = {} to print a blank operator page again (for planning a new layout).
"""
import os
from datetime import date

from reportlab.lib import colors
from reportlab.lib.pagesizes import landscape, letter
from reportlab.lib.units import inch
from reportlab.pdfgen import canvas

from code_values import now, pct   # speeds and the assist angle, read from DriverControls.java (never out of date)

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "controller-layout.pdf")

# What each control does. Missing or "" = a blank to fill in. None = shown as "(free)".
DRIVER = {
    "LT": ("TURBO (hold)", "full speed: " + pct("TURBO_SCALE")),
    "RT": ("TURBO (hold)", "full speed: " + pct("TURBO_SCALE")),
    "LB": ("SLOW (hold)", "lining up: " + pct("SLOW_MODE_SCALE")),
    "RB": ("SLOW (hold)", "lining up: " + pct("SLOW_MODE_SCALE")),
    "LS": ("DRIVE", "almost straight = exactly straight (±%g°)" % float(now("SNAP_ANGLE_DEGREES"))),
    "RS": ("TURN", "push left / right"),
    "A": ("STRAIGHT ASSIST on/off", "rumble: 1 blip = ON, 2 = OFF. Starts ON"),
}
OPERATOR = {  # decisions 029, 035
    "Y": ("LAUNCH (hold)", "trigger moves out of the way; let go = blocked"),
    "X": ("TURRET REST SPOT (OFF for now)", "TURRET_ENABLED = false: does nothing"),
    "B": ("DIRECTION (toggle)", "IN (intake in + wheel UP) or OUT (balls leave)"),
    "A": ("INTAKE SYSTEM (toggle)", "intake + water wheel together, on / off"),
    "LT": ("EJECT (hold)", "water wheel DOWN + intake OUT; then all off"),
    "LS": ("TURRET (OFF for now)", "TURRET_ENABLED = false: does nothing"),
}

# (key, button name, anchor x, anchor y). Positions are in inches on a landscape page.
LEFT = [
    ("LT", "Left trigger  (LT / L2)", 3.88, 5.83),
    ("LB", "Left bumper  (LB / L1)", 3.68, 5.53),
    ("DU", "Dpad UP", 4.30, 5.06),
    ("DL", "Dpad LEFT", 3.99, 4.75),
    ("DR", "Dpad RIGHT", 4.61, 4.75),
    ("DD", "Dpad DOWN", 4.30, 4.44),
    ("LS", "Left stick", 4.90, 4.00),
    ("LSB", "Left stick: press it down", 4.90, 3.80),
]
RIGHT = [
    ("RT", "Right trigger  (RT / R2)", 7.12, 5.83),
    ("RB", "Right bumper  (RB / R1)", 7.32, 5.53),
    ("Y", "Y  (triangle)", 6.70, 5.05),
    ("X", "X  (square)", 6.40, 4.75),
    ("B", "B  (circle)", 7.00, 4.75),
    ("A", "A  (cross)", 6.70, 4.45),
    ("RS", "Right stick", 6.10, 4.00),
    ("RSB", "Right stick: press it down", 6.10, 3.80),
]
LABEL_TOPS = [7.25, 6.67, 6.09, 5.51, 4.93, 4.35, 3.77, 3.19]  # top of each callout, both sides

BODY = colors.HexColor("#D9DDE3")
DARK = colors.HexColor("#4B5563")
NAVY = colors.HexColor("#1F3A68")
GREY = colors.HexColor("#6B7280")
FACE = {"Y": "#EAB308", "X": "#2563EB", "B": "#DC2626", "A": "#16A34A"}


GAMEPAD_LIFT = 0.40  # moves the whole gamepad drawing up (inches)

# Where each leader line ends: the edge of the button, so the dot doesn't hide the button's letter
EDGE = {"Y": (0.13, 0.0), "X": (0.0, 0.13), "B": (0.13, 0.0), "A": (0.13, 0.0)}


def draw_gamepad(c):
    """A simple gamepad outline (Logitech / PS style, seen from the front)."""
    i = inch
    c.saveState()
    c.translate(0, GAMEPAD_LIFT * i)
    # triggers and bumpers (on the top edge)
    c.setFillColor(DARK)
    c.roundRect(3.85 * i, 5.72 * i, 0.70 * i, 0.22 * i, 0.08 * i, stroke=0, fill=1)
    c.roundRect(6.45 * i, 5.72 * i, 0.70 * i, 0.22 * i, 0.08 * i, stroke=0, fill=1)
    c.setFillColor(GREY)
    c.roundRect(3.65 * i, 5.44 * i, 1.10 * i, 0.18 * i, 0.08 * i, stroke=0, fill=1)
    c.roundRect(6.25 * i, 5.44 * i, 1.10 * i, 0.18 * i, 0.08 * i, stroke=0, fill=1)
    c.setFont("Helvetica-Bold", 7)
    c.setFillColor(colors.white)
    for x, t in ((4.20, "LT"), (6.80, "RT")):
        c.drawCentredString(x * i, 5.80 * i, t)
    for x, t in ((4.20, "LB"), (6.80, "RB")):
        c.drawCentredString(x * i, 5.50 * i, t)
    # body: two grips and a middle
    c.setFillColor(BODY)
    c.ellipse(3.15 * i, 2.75 * i, 4.75 * i, 4.55 * i, stroke=0, fill=1)
    c.ellipse(6.25 * i, 2.75 * i, 7.85 * i, 4.55 * i, stroke=0, fill=1)
    c.roundRect(3.45 * i, 3.55 * i, 4.10 * i, 1.90 * i, 0.55 * i, stroke=0, fill=1)
    # dpad
    c.setFillColor(DARK)
    c.rect(4.18 * i, 4.40 * i, 0.24 * i, 0.70 * i, stroke=0, fill=1)
    c.rect(3.95 * i, 4.63 * i, 0.70 * i, 0.24 * i, stroke=0, fill=1)
    # sticks
    for x in (4.90, 6.10):
        c.setFillColor(GREY)
        c.circle(x * i, 4.00 * i, 0.34 * i, stroke=0, fill=1)
        c.setFillColor(DARK)
        c.circle(x * i, 4.00 * i, 0.24 * i, stroke=0, fill=1)
    # face buttons
    c.setFont("Helvetica-Bold", 9)
    for key, _, x, y in RIGHT:
        if key not in FACE:
            continue
        c.setFillColor(colors.HexColor(FACE[key]))
        c.circle(x * i, y * i, 0.13 * i, stroke=0, fill=1)
        c.setFillColor(colors.white)
        c.drawCentredString(x * i, (y - 0.045) * i, key)
    # back / guide / start (not used: the Driver Station uses START to pick the gamepad)
    c.setFillColor(GREY)
    for x in (5.20, 5.80):
        c.roundRect((x - 0.12) * i, 4.95 * i, 0.24 * i, 0.10 * i, 0.05 * i, stroke=0, fill=1)
    c.circle(5.50 * i, 4.62 * i, 0.10 * i, stroke=0, fill=1)
    c.setFont("Helvetica", 5.5)
    c.setFillColor(DARK)
    c.drawCentredString(5.20 * i, 5.10 * i, "BACK")
    c.drawCentredString(5.80 * i, 5.10 * i, "START")
    c.drawCentredString(5.50 * i, 4.42 * i, "don't use")
    c.restoreState()


def callout(c, side, top, name, anchor, job):
    """A button name, what it does (or blank lines), and a line to the button."""
    i = inch
    left_x, width = (0.45, 2.55) if side == "L" else (8.05, 2.5)  # clear of the gamepad (x 3.15 to 7.85)
    c.setFillColor(NAVY)
    c.setFont("Helvetica-Bold", 9.5)
    c.drawString(left_x * i, (top - 0.13) * i, name)
    if job is None or job == ():
        c.setFont("Helvetica-Oblique", 9)
        c.setFillColor(GREY)
        c.drawString(left_x * i, (top - 0.33) * i, "(free)")
    elif job:
        what, note = job
        c.setFillColor(colors.black)
        c.setFont("Helvetica-Bold", 11)
        c.drawString(left_x * i, (top - 0.33) * i, what)
        c.setFont("Helvetica", 8.5)
        c.setFillColor(GREY)
        c.drawString(left_x * i, (top - 0.49) * i, note)
    else:
        c.setStrokeColor(colors.HexColor("#9CA3AF"))
        c.setLineWidth(0.6)
        for dy in (0.33, 0.51):
            c.line(left_x * i, (top - dy) * i, (left_x + width) * i, (top - dy) * i)
    # leader line from the label edge to the button
    c.setStrokeColor(colors.HexColor("#9CA3AF"))
    c.setLineWidth(0.5)
    start_x = (left_x + width + 0.05) if side == "L" else (left_x - 0.08)
    c.line(start_x * i, (top - 0.10) * i, anchor[0] * i, anchor[1] * i)
    c.setFillColor(NAVY)
    c.circle(anchor[0] * i, anchor[1] * i, 0.025 * i, stroke=0, fill=1)


def header(c, title, subtitle):
    c.setFillColor(NAVY)
    c.setFont("Helvetica-Bold", 22)
    c.drawString(0.45 * inch, 7.95 * inch, title)
    c.setFont("Helvetica", 10.5)
    c.setFillColor(colors.black)
    c.drawString(0.45 * inch, 7.68 * inch, subtitle)
    c.setFont("Helvetica", 7.5)
    c.setFillColor(GREY)
    c.drawString(0.45 * inch, 0.32 * inch, "WMS Irondogs  |  Controller layout  |  code: TeamCode/opmodes/teleop "
                 "(DriverControls.java, OperatorControls.java, MainTeleOp.java)  |  printed " + date.today().isoformat())


def box(c, x, y, w, h, title, lines, size=9):
    """A notes box: title, then lines of text (a line can be a list of (text, bold) pieces)."""
    i = inch
    c.setStrokeColor(colors.HexColor("#9CA3AF"))
    c.setFillColor(colors.HexColor("#F3F4F6"))
    c.roundRect(x * i, y * i, w * i, h * i, 0.08 * i, stroke=1, fill=1)
    c.setFillColor(NAVY)
    c.setFont("Helvetica-Bold", 10)
    c.drawString((x + 0.12) * i, (y + h - 0.22) * i, title)
    ty = y + h - 0.42
    for line in lines:
        tx = x + 0.12
        for text, bold in (line if isinstance(line, list) else [(line, False)]):
            font = "Helvetica-Bold" if bold else "Helvetica"
            c.setFont(font, size)
            c.setFillColor(colors.black)
            c.drawString(tx * i, ty * i, text)
            tx += c.stringWidth(text, font, size) / inch
        ty -= size * 1.35 / 72


def page(c, jobs, title, subtitle, blank_default):
    header(c, title, subtitle)
    draw_gamepad(c)
    for side, rows in (("L", LEFT), ("R", RIGHT)):
        for (key, name, ax, ay), top in zip(rows, LABEL_TOPS):
            job = jobs.get(key, "" if blank_default else None)
            dx, dy = EDGE.get(key, (0.0, 0.0))
            callout(c, side, top, name, (ax + dx, ay + dy + GAMEPAD_LIFT), job)


c = canvas.Canvas(OUT, pagesize=landscape(letter))
c.setTitle("Controller Layout")
c.setAuthor("WMS Irondogs")

# ---------- Page 1: DRIVER ----------
page(c, DRIVER, "Gamepad 1: DRIVER",
     "Drives the robot. Pick it on the Driver Station with START + A. Same in every TeleOp (DriverControls.java).",
     blank_default=False)
box(c, 0.45, 0.55, 4.9, 1.85, "Three driving speeds", [
    [("SLOW  ", True), ("hold either bumper: " + pct("SLOW_MODE_SCALE") + " power, for lining up", False)],
    [("NORMAL  ", True), ("nothing held: " + pct("NORMAL_SCALE") + " power, everyday driving", False)],
    [("TURBO  ", True), ("hold either trigger: " + pct("TURBO_SCALE") + " power, crossing the field", False)],
    [("Both held? ", True), ("SLOW wins (the safer choice).", False)],
    "The Driver Station shows \"Drive speed\" during the match.",
    "These numbers come from SLOW_MODE_SCALE, NORMAL_SCALE and",
    "TURBO_SCALE in DriverControls.java (reprint after changing them).",
])
box(c, 5.65, 0.55, 4.9, 1.85, "Good to know", [
    "Directions match the robot: stick forward = drive forward,",
    "stick left = strafe left, right stick left = turn left.",
    [("Straight-line assist (A): ", True), ("left stick within %g° of" % float(now("SNAP_ANGLE_DEGREES")), False)],
    "straight forward / back / left / right drives exactly straight (#67).",
    "Let go of the sticks and the robot stops quickly (BRAKE, decision 049).",
    "Nothing moves during INIT (game rule G304.H).",
    [("Free buttons ", True), ("(B X Y, dpad, stick presses) can get", False)],
    "driver jobs later, e.g. \"turret to forward\".",
])
c.showPage()

# ---------- Page 2: OPERATOR ----------
decided = bool(OPERATOR)
page(c, OPERATOR, "Gamepad 2: OPERATOR",
     "Runs the mechanisms. Pick it on the Driver Station with START + B. " +
     ("" if decided else "Write each job next to a button (pencil!), then tell the coders."),
     blank_default=not decided)
if not decided:
    box(c, 0.45, 0.55, 6.6, 1.85, "Jobs that need a button  (for each: HOLD it, TOGGLE on/off, or PRESS once?)", [
        "", "", "", "",
        "Tick each job once it has a button. A stick or trigger is good for",
        "\"how much\" (turret speed); a button is good for on/off.",
    ], size=8.6)
    jobs = ["Intake IN", "Intake OUT", "Water wheel UP", "Water wheel DOWN (clear a jam)",
            "Flywheel ON / OFF", "FIRE (the trigger)", "Flower flipper shake", "Turret LEFT / RIGHT",
            "Turret to FORWARD", "Hood UP / DOWN", "Turret AUTO-AIM (later, #7)"]
    c.setFont("Helvetica", 8.6)
    c.setFillColor(colors.black)
    for n, job in enumerate(jobs):
        col, row = n // 4, n % 4
        c.drawString((0.57 + col * 2.2) * inch, (1.98 - row * 0.155) * inch, "[   ]  " + job)
    box(c, 7.25, 0.55, 3.3, 1.85, "Questions to decide", [
        "1. Intake: hold, or toggle on/off?",
        "2. Fire: one button for the whole shot",
        "   (load, fire, reset) or a button each?",
        "3. Anything automatic? e.g. stop the",
        "   intake when a NECTAR is detected",
        "4. Lights: what should they show?",
    ], size=8.6)
else:
    box(c, 0.45, 0.55, 5.6, 1.85, "How it works", [
        [("Flywheel: ", True), ("starts when START is pressed and runs the whole match.", False)],
        "It is NOT on a button. Watch \"Launcher Status\" for READY.",
        [("A ", True), ("= intake + water wheel on / off.  ", False), ("B ", True), ("= switch IN / OUT.", False)],
        [("X ", True), ("= turret rest spot. TURRET OFF for now (TURRET_ENABLED = false): X and the left stick do nothing.", False)],
        [("EJECT ", True), ("beats everything; let go and the intake + water wheel are OFF.", False)],
        [("Lights: ", True), ("PURPLE = the next ball to launch is a NECTAR.", False)],
    ], size=8.6)
    box(c, 6.25, 0.55, 4.3, 1.85, "Not on a button yet", [
        "Turret: OFF in Main TeleOp for now (decision 050). Hood up / down,",
        "turret auto-aim (later, #7). Flower flipper: waiting to see if built.",
        "Free: dpad, bumpers, right trigger, right stick.",
        "Code: opmodes/teleop/OperatorControls.java",
    ], size=8.6)
c.showPage()
c.save()
print("Wrote", OUT)
