"""
Makes the printable HARDWARE MAP: every device, which subsystem owns it, and where it plugs in.

Run:   python3 make_hardware_map.py
Needs: pip install reportlab
Makes: hardware-map.pdf (in this folder)

When hardware changes, edit the DEVICES list below (same names as TeamCode/docs/hardware-config.md,
decision 024) and re-run. Fill the blanks in pencil, then copy the ports into hardware-config.md.
"""
import os
from datetime import date

from reportlab.lib import colors
from reportlab.lib.pagesizes import landscape, letter
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.platypus import Paragraph, SimpleDocTemplate, Spacer, Table, TableStyle

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "hardware-map.pdf")

# ------------------------------------------------------------------------------------------------
# The devices. (subsystem, config name, device, Driver Station config type, port type, hub, port, note)
# Leave hub/port "" to print a blank to fill in. Only fill them for ports that can't change.
# ------------------------------------------------------------------------------------------------
DEVICES = [
    ("Drivetrain", "frontLeftDrive", "goBILDA 5203 Yellow Jacket", "goBILDA 5203 Series", "Motor", "", "", "Direction REVERSE"),
    ("Drivetrain", "backLeftDrive", "goBILDA 5203 Yellow Jacket", "goBILDA 5203 Series", "Motor", "", "", "Direction REVERSE"),
    ("Drivetrain", "frontRightDrive", "goBILDA 5203 Yellow Jacket", "goBILDA 5203 Series", "Motor", "", "", ""),
    ("Drivetrain", "backRightDrive", "goBILDA 5203 Yellow Jacket", "goBILDA 5203 Series", "Motor", "", "", ""),
    ("Odometry", "pinpoint", "goBILDA Pinpoint + 2 pods", "goBILDA Pinpoint", "I2C bus", "", "", "Not bus 0 (built-in IMU)"),
    ("Odometry", "imu", "Control Hub built-in IMU", "(built in)", "I2C bus", "CH", "0", "Built in, can't move"),
    ("Intake", "intakeServo", "Servo (continuous)", "Continuous Rotation Servo", "Servo", "", "", "Pulls game elements in"),
    ("Intake", "intakeLoaderServo", "Servo (continuous)", "Continuous Rotation Servo", "Servo", "", "", "Moves them up to the launcher"),
    ("Intake", "intake______Switch", "goBILDA micro limit switch", "Digital Device", "Digital", "", "", "Job + name TBD (#4)"),
    ("Intake", "intake______Switch", "goBILDA micro limit switch", "Digital Device", "Digital", "", "", "Job + name TBD (#4)"),
    ("Launcher", "launcherMotor", "goBILDA 5203 Yellow Jacket", "goBILDA 5203 Series", "Motor", "", "", "The flywheel"),
    ("Launcher", "launcherTriggerServo", "Servo", "Servo", "Servo", "", "", "REST / FIRE"),
    ("Turret", "turretRotateServo", "goBILDA 2000 25-3 Speed (continuous)", "Continuous Rotation Servo", "Servo", "", "", "Signal 1000-2000 us"),
    ("Turret", "turretEncoder", "ELC Encoder V2, \"D\" cable", "Motor (never powered)", "Encoder", "EH", "", "Motor port # must match; keep that motor port empty"),
    ("Turret", "turretForwardSwitch", "REV Magnetic Limit Switch", "Digital Device", "Digital", "", "", "Usually the ODD channel (n+1)"),
    ("Turret", "turretHoodServo", "Servo", "Servo", "Servo", "", "", "Tilts the launch up/down"),
    ("Camera", "limelight", "Limelight 3A", "Limelight 3A", "USB", "CH", "USB 3.0", "Must be the USB 3.0 port"),
    ("FlowerFlipper", "flowerFlipperServo", "Servo", "Servo", "Servo", "", "", "Shakes back and forth"),
    ("Lights", "leftIndicatorLight", "goBILDA RGB Indicator Light", "Servo", "Servo", "", "", "Black wire to ground mark"),
    ("Lights", "rightIndicatorLight", "goBILDA RGB Indicator Light", "Servo", "Servo", "", "", "Black wire to ground mark"),
]

# Colors per subsystem, so rows and the hub diagrams are easy to match up
SUB_COLORS = {
    "Drivetrain": "#DCE6F2", "Odometry": "#E4DFEC", "Intake": "#E2EFDA", "Launcher": "#FCE4D6",
    "Turret": "#FFF2CC", "Camera": "#DDEBF7", "FlowerFlipper": "#F8CBAD", "Lights": "#EDEDED",
}

styles = getSampleStyleSheet()
TITLE = ParagraphStyle("T", parent=styles["Title"], fontSize=18, spaceAfter=2)
H2 = ParagraphStyle("H2", parent=styles["Heading2"], fontSize=12.5, spaceBefore=6, spaceAfter=3,
                    textColor=colors.HexColor("#1F3A68"))
BODY = ParagraphStyle("B", parent=styles["BodyText"], fontSize=9, leading=11.5)
SMALL = ParagraphStyle("S", parent=BODY, fontSize=7.8, leading=9.5, textColor=colors.HexColor("#444444"))
CELL = ParagraphStyle("C", parent=BODY, fontSize=8.3, leading=10)
CELLB = ParagraphStyle("CB", parent=CELL, fontName="Helvetica-Bold")
MONO = ParagraphStyle("M", parent=CELL, fontName="Courier-Bold", fontSize=8.5)
GRID = colors.HexColor("#888888")


def p(text, style=BODY):
    return Paragraph(text, style)


def footer(canvas, doc):
    canvas.saveState()
    canvas.setFont("Helvetica", 7.5)
    canvas.setFillColor(colors.HexColor("#666666"))
    canvas.drawString(0.5 * inch, 0.35 * inch,
                      "WMS Irondogs  |  Hardware Map  |  names: TeamCode/docs/hardware-config.md (decision 024)  |  "
                      "printed " + date.today().isoformat())
    canvas.drawRightString(10.5 * inch, 0.35 * inch, "Page %d" % doc.page)
    canvas.restoreState()


def device_table():
    head = ["Subsystem", "Config name\n(Driver Station)", "Device", "Config type", "Hub\n(circle)", "Port type",
            "Port #", "Notes"]
    rows = [[p(h, CELLB) for h in head]]
    style = [
        ("GRID", (0, 0), (-1, -1), 0.5, GRID),
        ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#1F3A68")),
        ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
        ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
        ("LEFTPADDING", (0, 0), (-1, -1), 4),
        ("RIGHTPADDING", (0, 0), (-1, -1), 4),
    ]
    header_style = ParagraphStyle("HW", parent=CELLB, textColor=colors.white)
    rows[0] = [p(h.replace("\n", "<br/>"), header_style) for h in head]
    for i, (sub, name, device, cfg, ptype, hub, port, note) in enumerate(DEVICES, start=1):
        hub_cell = {"CH": "<b>CH</b>", "EH": "<b>EH</b>"}.get(hub, "CH  /  EH")
        rows.append([p(sub, CELLB), p(name, MONO), p(device, CELL), p(cfg, CELL), p(hub_cell, CELL),
                     p(ptype, CELL), p("<b>%s</b>" % port if port else "", CELL), p(note, CELL)])
        style.append(("BACKGROUND", (0, i), (0, i), colors.HexColor(SUB_COLORS[sub])))
    widths = [0.95, 1.75, 1.75, 1.55, 0.7, 0.7, 0.6, 2.0]
    t = Table(rows, colWidths=[w * inch for w in widths], rowHeights=[0.34 * inch] + [0.27 * inch] * len(DEVICES),
              repeatRows=1)
    t.setStyle(TableStyle(style))
    return t


story = [
    p("Robot Hardware Map", TITLE),
    p("Every device, the subsystem that owns it, and where it plugs in. <b>Fill the blanks in pencil</b> as you wire: "
      "circle <b>CH</b> (Control Hub) or <b>EH</b> (Expansion Hub) and write the <b>Port #</b>. The <b>config name</b> must "
      "match the Driver Station config <b>exactly</b> (capital letters too). Then copy the ports into "
      "<font face='Courier'>hardware-config.md</font>.", BODY),
    Spacer(1, 5),
    device_table(),
    Spacer(1, 5),
    p("<b>Port types:</b> <b>Motor</b> = 4 per hub (0-3). <b>Encoder</b> = the small 4-pin socket next to each motor port, "
      "same number. <b>Servo</b> = 6 per hub (0-5). <b>Digital</b> = 4 sockets per hub, each with 2 channels (0-1, 2-3, 4-5, 6-7): "
      "write the <b>channel</b> the config uses. <b>I2C bus</b> = 4 per hub (0-3). <b>Config type</b> = what to pick in the "
      "Driver Station when adding the device.", SMALL),
    Spacer(1, 3),
    p("<b>Reminders:</b> the Expansion Hub connects with the RS485 <b>3-pin</b> cable plus XT30 power. "
      "The turret encoder goes on an Expansion Hub encoder port whose motor port (same number) is <b>empty</b>. "
      "Drive motor encoders and the flywheel encoder use their own motor's encoder port. "
      "Nothing may move during INIT (G304.H).", SMALL),
]

doc = SimpleDocTemplate(OUT, pagesize=landscape(letter), leftMargin=0.5 * inch, rightMargin=0.5 * inch,
                        topMargin=0.45 * inch, bottomMargin=0.55 * inch,
                        title="Robot Hardware Map", author="WMS Irondogs")
doc.build(story, onFirstPage=footer, onLaterPages=footer)
print("Wrote", OUT)
