"""
Makes the printable LAUNCHER TEST controller layout (issue #5): which button does what in Launcher Test.
  Page 1  Gamepad 1, POWER mode     (fixed power, like Main TeleOp today; finds the F estimate)
  Page 2  Gamepad 1, VELOCITY mode  (PIDF: the hub holds a target speed; tune P, I, D, F live)
  Page 3  Gamepad 2, optional       (a second person can change the same P, I, D, F)

Run:   python3 make_launcher_test_layout.py
Needs: pip install reportlab
Makes: launcher-test-layout.pdf (in this folder)

Keep it in sync with opmodes/test/TestLauncher.java. The step sizes and start values are read from the code
(code_values.py), so they are never out of date. The gamepad drawing comes from make_controller_layout.py.
"""
import os
from datetime import date

from reportlab.lib import colors
from reportlab.lib.pagesizes import landscape, letter
from reportlab.lib.units import inch
from reportlab.pdfgen import canvas

from code_values import now
from make_controller_layout import (EDGE, GAMEPAD_LIFT, GREY, LABEL_TOPS, LEFT, NAVY, RIGHT, box, callout,
                                    draw_gamepad)

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "launcher-test-layout.pdf")

POWER_STEP = "%g%%" % (float(now("POWER_STEP")) * 100)       # "2.5%"
TARGET_STEP = "%g" % float(now("TARGET_STEP_RPM"))           # "100"
START_TARGET = "%g" % float(now("START_TARGET_RPM"))         # "3000"

# The buttons both modes share (gamepad 1)
SHARED = {
    "A": ("FLYWHEEL on / off", "spins at the power or target on screen"),
    "B": ("STOP the flywheel", ""),
    "X": ("INTAKE + WATER WHEEL on / off", "feeds balls to the launcher"),
    "LB": ("FIRE (hold)", "gate open; let go = closed. Times the shot"),
}

POWER = dict(SHARED, **{
    "RB": ("Switch to VELOCITY mode", "if spinning, jumps straight to the target"),
    "RT": ("POWER + " + POWER_STEP, "one step per press"),
    "LT": ("POWER - " + POWER_STEP, "one step per press"),
    "DU": ("POWER + " + POWER_STEP, "same as R2"),
    "DD": ("POWER - " + POWER_STEP, "same as L2"),
    "Y": ("POWER 100%", "use it to read the F estimate"),
    "DR": ("COPY SPEED to VELOCITY target", "wait for a steady speed first"),
})

VELOCITY = dict(SHARED, **{
    "RB": ("Switch to POWER mode", "if spinning, keeps spinning at the test power"),
    "RT": ("TARGET + " + TARGET_STEP + " RPM", "one step per press. Starts at " + START_TARGET + " RPM"),
    "LT": ("TARGET - " + TARGET_STEP + " RPM", "one step per press"),
    "DL": ("PICK the gain: back", "F <- D <- I <- P. Starts on F"),
    "DR": ("PICK the gain: next", "P -> I -> D -> F"),
    "DU": ("RAISE the picked gain", "by the step size on screen"),
    "DD": ("LOWER the picked gain", "never goes below 0"),
    "Y": ("NEXT STEP SIZE", "1 > 10 > 0.1 > back to 1"),
})

GAMEPAD2 = {
    "DL": ("PICK the gain: back", "same as gamepad 1"),
    "DR": ("PICK the gain: next", "same as gamepad 1"),
    "DU": ("RAISE the picked gain", "by the step size on screen"),
    "DD": ("LOWER the picked gain", "never goes below 0"),
    "RB": ("STEP x 10", "up to 10"),
    "LB": ("STEP / 10", "down to 0.1"),
}


def header(c, title, subtitle):
    c.setFillColor(NAVY)
    c.setFont("Helvetica-Bold", 22)
    c.drawString(0.45 * inch, 7.95 * inch, title)
    c.setFont("Helvetica", 10.5)
    c.setFillColor(colors.black)
    c.drawString(0.45 * inch, 7.68 * inch, subtitle)
    c.setFont("Helvetica", 7.5)
    c.setFillColor(GREY)
    c.drawString(0.45 * inch, 0.32 * inch, "WMS Irondogs  |  Launcher Test controller layout (#5)  |  code: "
                 "TeamCode/opmodes/test/TestLauncher.java  |  worksheet: launcher-test.pdf  |  printed "
                 + date.today().isoformat())


def layout_page(c, jobs, title, subtitle):
    """Our header, the gamepad, and a callout for each button in jobs (unused buttons are not shown)."""
    header(c, title, subtitle)
    draw_gamepad(c)
    for side, rows in (("L", LEFT), ("R", RIGHT)):
        for (key, name, ax, ay), top in zip(rows, LABEL_TOPS):
            if key in jobs:
                dx, dy = EDGE.get(key, (0.0, 0.0))
                callout(c, side, top, name, (ax + dx, ay + dy + GAMEPAD_LIFT), jobs[key])


def main():
    c = canvas.Canvas(OUT, pagesize=landscape(letter))
    c.setTitle("Launcher Test Controller Layout")
    c.setAuthor("WMS Irondogs")

    # ---------- Page 1: gamepad 1, POWER mode ----------
    layout_page(c, POWER, "Launcher Test: Gamepad 1, POWER mode",
                "The test starts here. Fixed power, like Main TeleOp today. R1 switches to VELOCITY mode (page 2).")
    box(c, 0.45, 0.55, 10.1, 1.25, "Good to know", [
        [("Starts at Main TeleOp's power (" + now("DEFAULT_SPIN_POWER") + "). ", True), ("Each press of R2 / L2 or dpad up / down changes it by " + POWER_STEP
                                          + " (up or down from there).", False)],
        [("F estimate: ", True), ("Y (100%), A, wait 3 s, then read \"F estimate\" on the screen. "
                                  "That is the F to start with in VELOCITY mode.", False)],
        [("Compare: ", True), ("dpad RIGHT copies the speed to the VELOCITY target. Fire here, then in VELOCITY: "
                               "\"Last shot POWER\" and \"Last shot VELOCITY\" show side by side.", False)],
        "Nobody in front of the launcher. Nothing moves during INIT.",
    ], size=9.5)
    c.showPage()

    # ---------- Page 2: gamepad 1, VELOCITY mode ----------
    layout_page(c, VELOCITY, "Launcher Test: Gamepad 1, VELOCITY (PIDF) mode",
                "The hub holds a TARGET speed. One person can tune it all on gamepad 1. R1 goes back to POWER mode.")
    box(c, 0.45, 0.55, 10.1, 1.25, "How to tune  (write the numbers down: the test forgets them when it stops!)", [
        [("1. F first: ", True), ("pick F, set it to the F estimate (page 1). Set P, I and D to 0. "
                                  "R2 / L2 to the target, then A.", False)],
        [("2. Then P: ", True), ("hold L1 for a few balls in a row. Raise P (step 10, then 1) until the speed comes "
                                 "back fast with no shaking.", False)],
        [("3. Target: ", True), ("about 85-90% of the 100% speed, so the motor has power left to recover after "
                                 "each ball.", False)],
        [("Better than POWER? ", True), ("\"Last shot VELOCITY\" drops less and is back faster than \"Last shot "
                                         "POWER\". \"Changing: F, step 1\" = which gain moves.", False)],
    ], size=9.5)
    c.showPage()

    # ---------- Page 3: gamepad 2, optional ----------
    layout_page(c, GAMEPAD2, "Launcher Test: Gamepad 2 (optional)",
                "Only if a second person helps: changes the SAME P, I, D, F as gamepad 1. Pick it with START + B.")
    box(c, 0.45, 0.55, 10.1, 1.25, "Good to know", [
        "Gamepad 2 only changes the gains. The flywheel, firing and the target speed stay on gamepad 1.",
        "The gains only do something in VELOCITY mode (gamepad 1, R1).",
        "Gamepad 1's Y and gamepad 2's bumpers change the same step size.",
        "The gains start from the hub's own values every time the test starts.",
    ], size=9.5)
    c.showPage()
    c.save()
    print("Wrote", OUT)


if __name__ == "__main__":
    main()
