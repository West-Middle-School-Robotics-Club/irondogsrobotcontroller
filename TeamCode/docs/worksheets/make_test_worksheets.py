"""
Makes the printable ROBOT TEST WORKSHEETS: one PDF per test OpMode, with the controls, the steps in order,
an Expected / Actual / Fix table, and a box for the numbers that go into the code.

Run:   python3 make_test_worksheets.py
Needs: pip install reportlab
Makes (in this folder):
    intake-test.pdf          Intake Test            (issue #4,  Intake.java)
    launcher-test.pdf        Launcher Test          (issue #5,  Launcher.java)
    turret-test.pdf          Turret Test            (issue #31, Turret.java)
    pinpoint-test.pdf        Pinpoint Test          (issues #6 #21, Odometry.java)
    flower-flipper-test.pdf  Flower Flipper Test    (FlowerFlipper.java)
    driver-tuning.pdf        Main TeleOp, driver speeds and straight-line assist (#3, #67, DriverControls.java)

Keep each sheet in sync with its test OpMode (opmodes/test/) and the TODO constants in its subsystem.
Basic Auton has its own guide (make_basic_auton_guide.py) and route sheets (game/diagrams/).
"""
import os

from reportlab.lib import colors
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.platypus import KeepTogether, PageBreak, Paragraph, SimpleDocTemplate, Spacer, Table, TableStyle

HERE = os.path.dirname(os.path.abspath(__file__))

styles = getSampleStyleSheet()
H1 = ParagraphStyle("H1", parent=styles["Title"], fontSize=19, spaceAfter=2)
H2 = ParagraphStyle("H2", parent=styles["Heading2"], fontSize=13.5, spaceBefore=8, spaceAfter=3,
                    textColor=colors.HexColor("#1F3A68"))
BODY = ParagraphStyle("Body", parent=styles["BodyText"], fontSize=10.3, leading=13.5)
SMALL = ParagraphStyle("Small", parent=BODY, fontSize=8.8, leading=11, textColor=colors.HexColor("#444444"))
CELL = ParagraphStyle("Cell", parent=BODY, fontSize=9.3, leading=11.5)
CELLB = ParagraphStyle("CellB", parent=CELL, fontName="Helvetica-Bold")
CODE = "<font face='Courier'>%s</font>"

GRID = colors.HexColor("#888888")
HEAD_BG = colors.HexColor("#DCE6F2")
WARN_BG = colors.HexColor("#FFF4D6")
WRITE_H = 0.4 * inch


def p(text, style=BODY):
    return Paragraph(text, style)


def c(text):
    """Code font inside a paragraph."""
    return CODE % text


def table(rows, widths, row_height=WRITE_H, header=True, write_rows=True, pad=3):
    """A grid table. Strings become Paragraphs so long text wraps. widths in inches.
    write_rows=True gives every body row a fixed height to write in. write_rows=False lets rows grow
    with their text; use a bigger pad to leave writing room."""
    data = [[cell if not isinstance(cell, str) else p(cell, CELLB if (header and r == 0) else CELL)
             for cell in row] for r, row in enumerate(rows)]
    if write_rows:
        heights = ([None] if header else []) + [row_height] * (len(rows) - (1 if header else 0))
    else:
        heights = None
    t = Table(data, colWidths=[w * inch for w in widths], rowHeights=heights, repeatRows=1 if header else 0)
    style = [
        ("GRID", (0, 0), (-1, -1), 0.6, GRID),
        ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
        ("LEFTPADDING", (0, 0), (-1, -1), 5),
        ("RIGHTPADDING", (0, 0), (-1, -1), 5),
        ("TOPPADDING", (0, 0), (-1, -1), pad),
        ("BOTTOMPADDING", (0, 0), (-1, -1), pad),
    ]
    if header:
        style.append(("BACKGROUND", (0, 0), (-1, 0), HEAD_BG))
    t.setStyle(TableStyle(style))
    return t


def lines(n, width=7.0):
    t = Table([[""]] * n, colWidths=[width * inch], rowHeights=[0.32 * inch] * n)
    t.setStyle(TableStyle([("LINEBELOW", (0, 0), (-1, -1), 0.5, GRID)]))
    return t


def warn(text):
    t = Table([[p(text, CELL)]], colWidths=[7.0 * inch])
    t.setStyle(TableStyle([("BACKGROUND", (0, 0), (-1, -1), WARN_BG),
                           ("BOX", (0, 0), (-1, -1), 0.8, colors.HexColor("#D9A400")),
                           ("LEFTPADDING", (0, 0), (-1, -1), 8), ("TOPPADDING", (0, 0), (-1, -1), 5),
                           ("BOTTOMPADDING", (0, 0), (-1, -1), 5)]))
    return t


def header_block(title, subtitle, opmode, needs, group="Tests group on the Driver Station"):
    return [
        p(title, H1),
        p(subtitle, BODY),
        Spacer(1, 6),
        table([["Names", "", "Date", ""],
               ["Battery voltage at start", "", "Battery at end", ""]],
              [1.7, 2.3, 1.3, 1.7], row_height=0.34 * inch, header=False),
        Spacer(1, 4),
        p("<b>OpMode:</b> %s (%s).  <b>Needs in the config:</b> %s" % (opmode, group, needs),
          SMALL),
        p("<b>Safety:</b> robot on blocks or on the floor with space around it, hands clear of moving parts, "
          "one person ready to press STOP. Nothing moves during INIT; everything starts after START.", SMALL),
    ]


def controls(rows):
    return [p("Controls (gamepad 1)", H2), table([["Button", "Does"]] + rows, [2.0, 5.0], write_rows=False)]


def steps(rows, title="Steps, in order"):
    """Rows grow with their text (so nothing is cut off) and get extra padding to write in."""
    t = table([["#", "Do this", "Expected", "Actual (what happened)", "If wrong"]] + rows,
              [0.3, 1.9, 1.5, 1.3, 2.0], write_rows=False, pad=9)
    # A short table stays with its heading; a long one may split (its header row repeats).
    return [KeepTogether([p(title, H2), t])] if len(rows) <= 5 else [p(title, H2), t]


def constants(rows, file_name):
    return [KeepTogether([p("Numbers to put into the code: " + c(file_name), H2),
                          table([["Constant", "Was", "New value", "Checked by"]] + rows, [2.6, 1.1, 1.6, 1.7],
                                row_height=0.34 * inch)])]


def section(title, *flowables):
    """A heading plus its table(s), kept on one page."""
    return [KeepTogether([p(title, H2)] + list(flowables))]


def after(issue, portfolio=None):
    extra = (" The filled-in sheet goes with the portfolio entry " + c(portfolio) + ".") if portfolio else ""
    return [Spacer(1, 6),
            p("<b>After the test:</b> a coder puts the new numbers into the constants and removes the matching "
              "TODO comments; the results go on GitHub issue %s.%s Photos of anything surprising!" % (issue, extra),
              SMALL)]


def build(filename, title, issue, story):
    out = os.path.join(HERE, filename)

    def footer(canvas, doc):
        canvas.saveState()
        canvas.setFont("Helvetica", 8)
        canvas.setFillColor(colors.HexColor("#666666"))
        canvas.drawString(0.75 * inch, 0.5 * inch, "WMS Irondogs  |  %s  |  GitHub issue %s" % (title, issue))
        canvas.drawRightString(7.75 * inch, 0.5 * inch, "Page %d" % doc.page)
        canvas.restoreState()

    doc = SimpleDocTemplate(out, pagesize=letter, leftMargin=0.75 * inch, rightMargin=0.75 * inch,
                            topMargin=0.65 * inch, bottomMargin=0.8 * inch, title=title, author="WMS Irondogs")
    doc.build(story, onFirstPage=footer, onLaterPages=footer)
    print("Wrote", out)


# =============================================================== 1. Intake Test (#4)
story = header_block(
    "Intake Test",
    "Does the intake pull balls in, does the water wheel lift them, and do the two switches spot a NECTAR?  "
    "(issue #4, decisions 024, 025, 028)",
    "Intake Test",
    c("intakeServo") + ", " + c("waterWheelServo") + " (Continuous Rotation Servo), " +
    c("nectarDetectorLeftSwitch") + ", " + c("nectarDetectorRightSwitch") + " (Digital Device)")
story += controls([
    ["HOLD A / B", "intake IN / OUT"],
    ["HOLD X / Y", "water wheel UP (to the launcher) / DOWN (clear a jam)"],
    ["(no button)", "the switches are read all the time, even in INIT. Watch the telemetry lines "
                    "\"NECTAR detected\", \"Nectar left switch\", \"Nectar right switch\""],
])
story += steps([
    ["1", "Hold A", "balls go IN", "", c("INTAKE_REVERSED = true")],
    ["2", "Hold X", "balls go UP toward the launcher", "", c("WATER_WHEEL_REVERSED = true")],
    ["3", "Hold A + X with 3 balls", "no jam, no ball flying out", "", "lower " + c("INTAKE_SPEED") + " / " +
     c("WATER_WHEEL_SPEED") + " (0 to 1)"],
    ["4", "Nothing touching the levers", "both \"not pressed\", \"NECTAR detected: no\"", "",
     "says PRESSED: switch " + c("NECTAR_SWITCH_WIRING") + " to the other value"],
    ["5", "Press the LEFT lever by hand", "\"Nectar left switch: PRESSED\", \"NECTAR detected: YES\"", "",
     "never changes: try the other channel of that digital port (n / n+1)"],
    ["6", "Press the RIGHT lever by hand", "same, for right", "", "same"],
    ["7", "Run a NECTAR (big ball, ~3.6 in) through", "YES while it passes", "",
     "levers too far apart: move them in"],
    ["8", "Run a POLLEN (small ball, ~2.8 in) through", "stays \"no\"", "", "levers too close: move them out"],
])
story.append(Spacer(1, 4))
story.append(p("Step 7 and 8, repeat 5 times each. NECTAR detected: ___ / 5.   POLLEN wrongly detected: ___ / 5.", BODY))
story += constants([
    [c("INTAKE_REVERSED"), "false", "", ""],
    [c("WATER_WHEEL_REVERSED"), "false", "", ""],
    [c("INTAKE_SPEED"), "1.0", "", ""],
    [c("WATER_WHEEL_SPEED"), "1.0", "", ""],
    [c("NECTAR_SWITCH_WIRING"), "NORMALLY_OPEN", "", ""],
], "subsystems/Intake.java")
story += section("Wiring, for " + c("hardware-config.md") + " (Intake table)",
          table([["Device", "Hub (CH / EH)", "Port"],
                 [c("intakeServo"), "", "servo"], [c("waterWheelServo"), "", "servo"],
                 [c("nectarDetectorLeftSwitch"), "", "digital"], [c("nectarDetectorRightSwitch"), "", "digital"]],
                [2.6, 2.0, 2.4], row_height=0.32 * inch))
story += after("#4", "docs/portfolio/2026-11-04-intake-and-nectar-detection.md")
build("intake-test.pdf", "Intake Test", "#4", story)

# =============================================================== 2. Launcher Test (#5)
story = header_block(
    "Launcher Test",
    "Does the flywheel spin the right way, where should the trigger gate sit, when is the wheel READY, "
    "and do shots land?  (issue #5, decisions 029, 032)",
    "Launcher Test",
    c("launcherMotor") + " (Motor, exact goBILDA type), " + c("launcherTriggerServo") + " (Servo)")
story += [warn("<b>The flywheel is fast and the ball comes out hard.</b> Nobody in front of the launcher. "
               "Aim at a wall or a net for steps 1 to 5. Eye protection on.")]
story += controls([
    ["A", "start / stop the flywheel at the test power"],
    ["B", "STOP the flywheel"],
    ["Dpad UP / DOWN", "test power +10% / -10%"],
    ["X / Y", "test power 50% / 100%"],
    ["HOLD right trigger", "power follows the trigger (0 to 100%)"],
    ["Dpad LEFT / RIGHT", "flip the motor direction (for step 1)"],
    ["HOLD left bumper", "trigger gate to FIRE (open). Let go = REST (closed)"],
])
story += steps([
    ["1", "X, then A", "wheel spins so a ball would go OUT the launcher", "",
     "flip with dpad; if FORWARD is right, " + c("MOTOR_REVERSED = false")],
    ["2", "Wheel stopped. Put a ball in the path, let go of the left bumper", "gate at REST BLOCKS the ball", "",
     "change " + c("TRIGGER_REST_POSITION")],
    ["3", "Hold the left bumper", "gate at FIRE lets the ball pass freely", "", "change " +
     c("TRIGGER_FIRE_POSITION")],
    ["4", "Y, then A. Wait 3 s. Read \"Launcher Velocity\"", "a steady number: ______ ticks/s", "",
     c("READY_VELOCITY_THRESHOLD") + " = about 90% of it: ______"],
    ["5", "B, then A again. Count seconds until \"READY\"", "under 2 s", "", "note it; the operator must wait this long"],
    ["6", "Launch one ball (A, wait READY, hold left bumper)", "ball comes out", "",
     "jam: check the gate positions and the water wheel"],
])
story += section("Power vs distance",
          p("Put the robot a set distance from the goal. For each power, launch 5 balls. The hit-rate test at the end "
            "uses the best power.", BODY),
          table([["Power", "Distance to goal (in)", "Made / 5", "Too short / too long?", "Notes"],
                 ["60%", "", "", "", ""], ["80%", "", "", "", ""], ["100%", "", "", "", ""],
                 ["___%", "", "", "", ""]], [0.7, 1.6, 0.9, 1.7, 2.1], row_height=0.36 * inch))
story += section("Hit-rate test (the \"done when\" of issue #5)",
          table([["Power", "Distance (in)", "Battery (V)", "Made / 10", "Who launched"],
                 ["", "", "", "", ""], ["", "", "", "", ""]], [1.0, 1.3, 1.2, 1.2, 2.3], row_height=0.36 * inch))
story += constants([
    [c("MOTOR_REVERSED"), "true", "", ""],
    [c("TRIGGER_REST_POSITION"), "0.0", "", ""],
    [c("TRIGGER_FIRE_POSITION"), "0.5", "", ""],
    [c("READY_VELOCITY_THRESHOLD"), "1000", "", ""],
    [c("DEFAULT_SPIN_POWER"), "1.0", "", ""],
], "subsystems/Launcher.java")
story += [p("Also write in " + c("hardware-config.md") + ": motor gear ratio ______, Expansion Hub motor port ____, "
            "trigger servo hub ____ port ____.", SMALL)]
story += after("#5")
build("launcher-test.pdf", "Launcher Test", "#5", story)

# =============================================================== 3. Turret Test (#31)
story = header_block(
    "Turret Test",
    "Calibrate the turret IN ORDER: servo direction, encoder direction, gear ratio, limits, aiming. Then the hood "
    "and the home switch.  (issue #31, decisions 019, 021, 033)",
    "Turret Servo Raw Test, then Turret Test",
    c("turretRotateServo") + " (Continuous Rotation Servo), " + c("turretEncoder") + " (Motor port, encoder only), " +
    c("turretHomeSwitch") + " (Digital Device), " + c("turretHoodServo") + " (Servo)")
story += [warn("<b>Nothing physical stops the turret.</b> Until step 4 is done it can turn into wires or the frame. "
               "Keep a hand near STOP and turn in small moves. Angles: 0 = forward, + = LEFT, home switch = 90 (LEFT). Limits: -60 to +90 (the hard stops are at -33 and +56 until moved, decision 037).")]
story += [p("Part A: Turret Servo Raw Test (servo alone)", H2),
          table([["#", "Do this", "Expected", "Actual", "If wrong"],
                 ["A1", "Push the left stick left and hold", "turret KEEPS turning while held, stops when let go", "",
                  "moves a bit, stops, comes back = servo still in positional mode: reprogram it as continuous"],
                 ["A2", "Stick centered, \"Speed 0.00\"", "turret does not creep", "", "recenter with the servo programmer"]],
                [0.35, 1.9, 1.6, 1.2, 1.95], write_rows=False, pad=9)]
story += controls([
    ["HOLD dpad LEFT / RIGHT", "turn left / right (half speed). + right trigger = full speed (once homed). + bumper = slow"],
    ["Y", "set a MARK: \"Since mark\" counts degrees from here"],
    ["A", "turn to the REST spot (the home magnet, 90° LEFT of forward). Not homed yet? It turns LEFT to find the home switch first. Do steps 1 to 3 first!"],
    ["X / B", "turn to 90° left / right of the MAGNET (same warning). Right is about dead forward"],
    ["Dpad UP / DOWN", "hood up / down a little"],
    ["BOTH lights GREEN", "turret is at HOME: the switch at 90° LEFT, or within HOME_TOLERANCE_COUNTS of it"],
])
story += steps([
    ["1", "Hold dpad LEFT", "turret turns LEFT (counter-clockwise from above)", "", c("SERVO_REVERSED = true")],
    ["2", "Turn left, watch \"Turret angle\"", "angle goes UP", "", c("ENCODER_REVERSED = true")],
    ["3", "Y (mark), then turn exactly 90° left (use a square or tape marks)", "\"Since mark\" reads about 90", "",
     "new " + c("ENCODER_REVS_PER_TURRET_REV") + " = old value x (what it read) / 90 = ______"],
    ["4", "Home it. Turn slowly left until just before anything hits. Read the angle. Same to the right",
     "left: ____° right: ____° (from forward)",
     "", "set " + c("MAX_LEFT_DEGREES") + " / " + c("MAX_RIGHT_DEGREES") + " a few degrees inside those"],
    ["5", "Turn past the home switch (90° LEFT) once (homes). Press A, then X, then B",
     "turns to the magnet, 90 L, 90 R (within limits), no wobble", "",
     "wobbles: lower " + c("AIM_GAIN") + ". Too slow: raise it. Runs away: steps 1 or 2 are wrong"],
    ["6", "Turn through the home switch slowly, watch \"Switch raw reading\"", "reading flips at the magnet", "",
     "flips the other way: " + c("HOME_SWITCH_TRIGGERED_STATE") + " to the other value"],
    ["7", "Dpad DOWN until the hood is flattest, read the position. Then UP to steepest",
     "lowest: ____, highest: ____", "", "set " + c("HOOD_LOWEST_POSITION") + " / " + c("HOOD_HIGHEST_POSITION") +
     " just inside the hard stops"],
], title="Part B: Turret Test, steps IN ORDER")
story += constants([
    [c("SERVO_REVERSED"), "false", "", ""],
    [c("ENCODER_REVERSED"), "false", "", ""],
    [c("ENCODER_REVS_PER_TURRET_REV"), "1.0", "", ""],
    [c("HOME_ANGLE_DEGREES"), "90", "", ""],
    [c("MAX_LEFT_DEGREES") + " / " + c("MAX_RIGHT_DEGREES"), "90 / -60", "", ""],
    [c("AIM_GAIN"), "0.02", "", ""],
    [c("HOME_SWITCH_TRIGGERED_STATE"), "false", "", ""],
    [c("HOOD_LOWEST_POSITION") + " / " + c("HOOD_HIGHEST_POSITION"), "0.0 / 1.0", "", ""],
    [c("HOME_TOLERANCE_COUNTS"), "200", "", ""],
], "subsystems/Turret.java")
story += after("#31")
build("turret-test.pdf", "Turret Test", "#31", story)

# =============================================================== 4. Pinpoint Test (#6, #21)
story = header_block(
    "Pinpoint Test",
    "Does the odometry computer measure where the robot really went?  (issues #6 and #21, decision 005)",
    "Pinpoint Test",
    c("pinpoint") + " (goBILDA Pinpoint, I2C) with both pods plugged in")
story += controls([
    ["A", "reset position to 0, 0, 0° (do this before EVERY run)"],
    ["B", "reset AND recalibrate the IMU. The robot must be STILL for a second"],
    ["Sticks", "drive, to bring the robot back. Motors are set to FLOAT so it is easy to push by hand"],
])
story += [p("<b>Setup:</b> tape a start line and a 48 in mark on the floor. Our pods are 4-bar pods (32 mm wheels), "
            "already set in the code. Directions: +X = forward, +Y = left, +heading = counter-clockwise (decision 007).",
            BODY)]
story += steps([
    ["1", "INIT, robot still", "\"Pinpoint READY\" before START", "", "stays not ready: check the I2C cable and the pods"],
    ["2", "A. Push the robot straight FORWARD 48 in", "X about +48, Y about 0", "",
     "X goes DOWN: " + c("X_POD_DIRECTION = REVERSED") + ". X far from 48: wrong " + c("POD_TYPE")],
    ["3", "A. Push the robot straight LEFT 48 in", "Y about +48, X about 0", "",
     "Y goes DOWN: " + c("Y_POD_DIRECTION = REVERSED")],
    ["4", "A. Turn the robot in place exactly 1 turn counter-clockwise (tape mark)", "heading back to about 0 (or 360), "
     "X and Y stay within 1 in", "", "X or Y drift a lot: the pod OFFSETS are off, see step 5"],
    ["5", "A. Spin 10 turns in place, slowly", "X and Y still within 2 in of 0", "",
     "measure from the WHEELBASE center to each pod wheel (mm) and set " + c("X_POD_OFFSET_MM") + " / " +
     c("Y_POD_OFFSET_MM")],
    ["6", "A. Drive a 48 in square and come back to the start", "X, Y within 2 in, heading within 3°", "",
     "repeat 3 times and write the worst error"],
])
story += section("Measurements",
          table([["Run", "What", "Pushed / turned", "Pinpoint read X", "Y", "Heading", "Error"],
                 ["2", "forward 48 in", "48 in", "", "", "", ""], ["3", "left 48 in", "48 in", "", "", "", ""],
                 ["4", "1 turn", "360°", "", "", "", ""], ["5", "10 turns", "3600°", "", "", "", ""],
                 ["6a", "square", "", "", "", "", ""], ["6b", "square", "", "", "", "", ""], ["6c", "square", "", "", "", "", ""]],
                [0.45, 1.2, 1.1, 1.15, 0.9, 1.0, 1.2], row_height=0.33 * inch))
story += constants([
    [c("X_POD_DIRECTION"), "FORWARD", "", ""],
    [c("Y_POD_DIRECTION"), "FORWARD", "", ""],
    [c("X_POD_OFFSET_MM"), "0.0", "", ""],
    [c("Y_POD_OFFSET_MM"), "0.0", "", ""],
], "subsystems/Odometry.java")
story += after("#6 (and #21 for the offsets)")
build("pinpoint-test.pdf", "Pinpoint Test", "#6 / #21", story)

# =============================================================== 5. Flower Flipper Test
story = header_block(
    "Flower Flipper Test",
    "Find the three servo positions and how fast it should shake.  (decision 024, " + c("FlowerFlipper.java") + ")",
    "Flower Flipper Test",
    c("flowerFlipperServo") + " (Servo)")
story += controls([
    ["A", "start shaking (between POSITION_A and POSITION_B)"],
    ["B", "stop: goes to REST_POSITION"],
    ["Dpad UP / DOWN", "move the servo by hand, 0.01 at a time (only when not shaking). Read \"Hand position\""],
])
story += steps([
    ["1", "Dpad to where the flipper should sit when not in use", "clear of the ball path and the frame", "",
     "that number = " + c("REST_POSITION")],
    ["2", "Dpad to one end of the shake, just before it hits anything", "position: ______", "", c("POSITION_A")],
    ["3", "Dpad to the other end", "position: ______", "", c("POSITION_B")],
    ["4", "A, watch it shake for 10 s", "moves the flower, servo not buzzing or hot", "",
     "too slow: smaller " + c("SWITCH_SECONDS") + ". Not reaching the ends: bigger"],
    ["5", "B", "back to REST, stays there", "", ""],
])
story += constants([
    [c("REST_POSITION"), "0.5", "", ""],
    [c("POSITION_A"), "0.4", "", ""],
    [c("POSITION_B"), "0.6", "", ""],
    [c("SWITCH_SECONDS"), "0.15", "", ""],
], "subsystems/FlowerFlipper.java")
story += [p("Not on a gamepad button yet (decision 029). Write here what the team wants: which button, driver or "
            "operator, hold or toggle?", BODY), lines(3)]
story += after("the flower flipper issue (or #3 for the button)")
build("flower-flipper-test.pdf", "Flower Flipper Test", "(none yet), decision 024", story)

# =============================================================== 6. Driver tuning (Main TeleOp)
story = header_block(
    "Driver Tuning",
    "Pick the three driving speeds and the straight-line assist angle WITH the drivers.  (issues #3 and #67, "
    "decisions 027, 031, " + c("DriverControls.java") + ")",
    "Main TeleOp RED or BLUE (gamepad 1 = driver)",
    "the 4 drive motors and the " + c("pinpoint"), group="TeleOp list on the Driver Station")
story += controls([
    ["Left stick", "drive and strafe.   Right stick: turn"],
    ["Nothing held", "NORMAL speed (70%)"],
    ["HOLD a bumper", "SLOW (40%), for lining up"],
    ["HOLD a trigger", "TURBO (100%). Bumper beats trigger"],
    ["A", "straight-line assist on / off. Rumble: 1 blip = ON, 2 = OFF"],
])
story += [p("<b>Course:</b> tape a start box, a 10 ft straight run, and a 2 ft target box to park in. Each driver runs "
            "it three ways. Time it and count how many tries it takes to park inside the box.", BODY)]
story += section("Runs",
          table([["Driver", "Speed used", "Assist", "Time (s)", "Tries to park", "Felt (too fast / ok / too slow)"],
                 ["", "SLOW", "ON", "", "", ""], ["", "NORMAL", "ON", "", "", ""], ["", "TURBO", "ON", "", "", ""],
                 ["", "NORMAL", "OFF", "", "", ""],
                 ["", "SLOW", "ON", "", "", ""], ["", "NORMAL", "ON", "", "", ""], ["", "TURBO", "ON", "", "", ""],
                 ["", "NORMAL", "OFF", "", "", ""]],
                [1.3, 0.9, 0.7, 0.8, 1.1, 2.2], row_height=0.34 * inch))
story += steps([
    ["1", "Drive forward with the stick a little off straight, assist ON", "robot goes exactly straight", "",
     "still drifts: raise " + c("SNAP_ANGLE_DEGREES") + ". Can't drive a shallow diagonal: lower it"],
    ["2", "Strafe left with assist ON", "exactly sideways, no forward creep", "", "creeps: check " + c("STRAFE_CORRECTION")],
    ["3", "Let go of the sticks at NORMAL", "robot stops, no creep", "", "creeps: raise " + c("DEADZONE") + " a little"],
    ["4", "Press A twice", "1 blip then 2 blips, telemetry ON then OFF", "", ""],
], title="Checks")
story += constants([
    [c("SLOW_MODE_SCALE"), "0.4", "", ""],
    [c("NORMAL_SCALE"), "0.7", "", ""],
    [c("TURBO_SCALE"), "1.0", "", ""],
    [c("SNAP_ANGLE_DEGREES"), "15", "", ""],
    [c("ASSIST_ON_AT_START"), "true", "", ""],
], "opmodes/teleop/DriverControls.java")
story += [p("Drivers' decision (everyone agrees): SLOW ____%, NORMAL ____%, TURBO ____%, assist starts ON / OFF. Why?", BODY),
          lines(2)]
story += after("#3 (speeds) and #67 (assist)")
build("driver-tuning.pdf", "Driver Tuning", "#3 / #67", story)
