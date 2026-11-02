"""
Makes the printable DRIVE TEAM GUIDE for Basic Auton (issue #22, decision 026):
which OpMode to pick, what the INIT screen means, how and why to set the START DELAY, and what the lights mean.

Run:   python3 make_basic_auton_guide.py
Needs: pip install reportlab
Makes: basic-auton-guide.pdf (in this folder)

Keep it in sync with opmodes/auto/BasicAuton.java (the INIT screen text and the lights).
"""
import os

from reportlab.lib import colors
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.platypus import KeepTogether, PageBreak, Paragraph, SimpleDocTemplate, Spacer, Table, TableStyle

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "basic-auton-guide.pdf")

styles = getSampleStyleSheet()
TITLE = ParagraphStyle("T", parent=styles["Title"], fontSize=19, spaceAfter=2)
H2 = ParagraphStyle("H2", parent=styles["Heading2"], fontSize=13.5, spaceBefore=7, spaceAfter=3,
                    textColor=colors.HexColor("#1F3A68"))
BODY = ParagraphStyle("B", parent=styles["BodyText"], fontSize=10.3, leading=13.5)
SMALL = ParagraphStyle("S", parent=BODY, fontSize=8.8, leading=11, textColor=colors.HexColor("#444444"))
CELL = ParagraphStyle("C", parent=BODY, fontSize=9.5, leading=12)
CELLB = ParagraphStyle("CB", parent=CELL, fontName="Helvetica-Bold")
SCREEN = ParagraphStyle("SCR", parent=BODY, fontName="Courier", fontSize=9, leading=12, textColor=colors.HexColor("#E5E7EB"))
GRID = colors.HexColor("#888888")
HEAD = colors.HexColor("#DCE6F2")
RED = colors.HexColor("#d62828")
BLUE = colors.HexColor("#1d4ed8")


def p(text, style=BODY):
    return Paragraph(text, style)


def grid_table(rows, widths, head=True, row_h=None):
    data = [[c if not isinstance(c, str) else p(c, CELLB if head and r == 0 else CELL) for c in row]
            for r, row in enumerate(rows)]
    t = Table(data, colWidths=[w * inch for w in widths], rowHeights=row_h)
    st = [("GRID", (0, 0), (-1, -1), 0.6, GRID), ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
          ("LEFTPADDING", (0, 0), (-1, -1), 5), ("RIGHTPADDING", (0, 0), (-1, -1), 5)]
    if head:
        st.append(("BACKGROUND", (0, 0), (-1, 0), HEAD))
    t.setStyle(TableStyle(st))
    return t


def screen(lines):
    """A picture of the Driver Station screen."""
    t = Table([[p(line.replace(" ", "&nbsp;"), SCREEN)] for line in lines], colWidths=[6.6 * inch])
    t.setStyle(TableStyle([("BACKGROUND", (0, 0), (-1, -1), colors.HexColor("#111827")),
                           ("BOX", (0, 0), (-1, -1), 2, colors.HexColor("#374151")),
                           ("TOPPADDING", (0, 0), (-1, -1), 1), ("BOTTOMPADDING", (0, 0), (-1, -1), 1),
                           ("LEFTPADDING", (0, 0), (-1, -1), 8)]))
    return t


def timeline(delay, drive=7, label=""):
    """One 30-second AUTO bar: grey = waiting (delay), orange = driving, green = parked."""
    cells, st = [], [("GRID", (0, 0), (-1, -1), 0.3, colors.white),
                     ("FONTSIZE", (0, 0), (-1, -1), 6.5), ("ALIGN", (0, 0), (-1, -1), "CENTER"),
                     ("TOPPADDING", (0, 0), (-1, -1), 0), ("BOTTOMPADDING", (0, 0), (-1, -1), 0)]
    for s in range(30):
        cells.append(str(s) if s % 5 == 0 else "")
        if s < delay:
            c = "#9CA3AF"
        elif s < delay + drive:
            c = "#F59E0B"
        elif s < 28:
            c = "#22C55E"
        else:
            c = "#111827"
        st.append(("BACKGROUND", (s, 0), (s, 0), colors.HexColor(c)))
    t = Table([cells], colWidths=[0.155 * inch] * 30, rowHeights=0.22 * inch)
    t.setStyle(TableStyle(st))
    outer = Table([[p(label, CELLB), t]], colWidths=[1.6 * inch, 4.8 * inch])
    outer.setStyle(TableStyle([("VALIGN", (0, 0), (-1, -1), "MIDDLE"), ("LEFTPADDING", (0, 0), (-1, -1), 0)]))
    return outer


def footer(canvas, doc):
    canvas.saveState()
    canvas.setFont("Helvetica", 8)
    canvas.setFillColor(colors.HexColor("#666666"))
    canvas.drawString(0.75 * inch, 0.5 * inch, "WMS Irondogs  |  Basic Auton Drive Team Guide  |  issue #22, decision 026")
    canvas.drawRightString(7.75 * inch, 0.5 * inch, "Page %d" % doc.page)
    canvas.restoreState()


story = [
    p("Basic Auton: Drive Team Guide", TITLE),
    p("Basic Auton scores <b>LEAVE (3)</b> + <b>PARK (5)</b> in the 30-second AUTO period. "
      "If <b>both</b> robots on our alliance do it, that's 16 points and the <b>SWARM ranking point</b>. "
      "Route pictures: <font face='Courier'>docs/game/diagrams/basic-auton-near.pdf</font> and "
      "<font face='Courier'>basic-auton-far.pdf</font>."),

    p("1. Pick the right OpMode", H2),
    p("On the Driver Station, open the <b>Basic Auton</b> group. Pick our alliance color and starting spot:"),
    Spacer(1, 4),
    grid_table([["OpMode", "Set the robot up...", "Pick it when..."],
                ["Basic Auton RED NEAR", "Tile A6, LEFT side against the RED wall, facing the far wall",
                 "We're red, and our partner starts somewhere else"],
                ["Basic Auton RED FAR", "Tile B1, BACK against the AUDIENCE wall, facing the far wall",
                 "We're red, and our partner wants the NEAR spot"],
                ["Basic Auton BLUE NEAR", "Tile F1, LEFT side against the BLUE wall, facing the audience",
                 "We're blue, and our partner starts somewhere else"],
                ["Basic Auton BLUE FAR", "Tile E6, BACK against the FAR wall, facing the audience",
                 "We're blue, and our partner wants the NEAR spot"]],
               [1.75, 2.75, 2.5]),
    Spacer(1, 3),
    p("Decide this with our partner before the match, using the <b>alliance planning sheet</b>. "
      "After AUTO, the matching <b>Main TeleOp</b> is already picked for you.", SMALL),

    p("2. Press INIT: what the screen means", H2),
    p("INIT gets the robot ready. <b>Nothing moves during INIT</b> (game rule G304.H): the robot must stay still "
      "until the match starts. You'll see something like this:"),
    Spacer(1, 4),
    screen(["BASIC AUTON : RED  NEAR  (lights are RED)",
            "Wrong one? Stop and pick the right Basic Auton.",
            "Start delay : 3 s   (dpad up/down to change)",
            "Start position : x 60.0  y 63.0  heading 0°",
            "Pinpoint : READY  (keep the robot STILL until READY)",
            "Battery : 12.9 V"]),
    Spacer(1, 4),
    grid_table([["Line", "What to check"],
                ["BASIC AUTON + the <b>lights</b>", "Both lights show our alliance color (red or blue). Wrong color or "
                 "wrong NEAR/FAR? Press STOP and pick the right OpMode."],
                ["<b>Start delay</b>", "How long the robot waits after START before it moves. See section 3."],
                ["<b>Pinpoint</b>", "Must say <b>READY</b> before START. It's measuring where \"still\" is, so "
                 "<b>don't touch or bump the robot</b> during INIT."],
                ["Battery", "Low battery = slower, less accurate driving. Swap it if it's low."]],
               [1.9, 5.1]),
    PageBreak(),

    p("3. The START DELAY: what it is and why we use it", H2),
    p("The <b>start delay</b> makes our robot <b>wait</b> a few seconds after the match starts, and <b>then</b> "
      "drive its route. AUTO is 30 seconds, and our route only takes a few seconds, so we have time to spare."),
    p("<b>Why wait?</b> Both robots on our alliance drive at the same time, and they both park in the <b>same "
      "small LOADING ZONE</b> (about 23 in wide). If our partner's route crosses ours, the robots can <b>crash</b> "
      "and nobody parks. Waiting lets our partner go first, then we follow."),
    Spacer(1, 6),
    timeline(0, label="Delay 0 s"),
    Spacer(1, 3),
    timeline(5, label="Delay 5 s"),
    Spacer(1, 3),
    p("Grey = waiting (delay).  Orange = driving the route (a few seconds; we'll time it in testing).  "
      "Green = parked.  Black = the code stops everything by 28 s, before AUTO ends at 30 s.", SMALL),
    Spacer(1, 6),
    p("<b>How to set it</b> (during INIT, before START):"),
    grid_table([["Do this", "Result"],
                ["Press <b>dpad UP</b> on gamepad 1", "Delay goes up 1 second (max 10 s)"],
                ["Press <b>dpad DOWN</b>", "Delay goes down 1 second (min 0 s)"],
                ["Look at the screen", "\"Start delay\" shows the number. Check it before START!"]],
               [3.0, 4.0]),
    Spacer(1, 3),
    p("The delay goes back to <b>0</b> every time you press INIT. Set it again for each match.", SMALL),
    Spacer(1, 6),
    Spacer(1, 0),
    p("<b>How to choose the number:</b>"),
    p("&bull; <b>0 s</b>: our partner's route doesn't cross ours (most matches).<br/>"
      "&bull; <b>A few seconds</b>: our partner drives through our path, or parks in the LOADING ZONE first. "
      "Ask them how long their AUTO takes, and wait until they're out of the way.<br/>"
      "&bull; Write the agreed delay on the alliance sheet (\"Our start delay\")."),

    p("4. After START", H2),
    p("The robot waits for the delay, then drives each waypoint. The <b>Pinpoint</b> tells it where it is, so it "
      "slows down as it gets close and stops on the spot (no guessing with timers)."),
    Spacer(1, 4),
    grid_table([["Lights at the end", "Means", "Do this"],
                ["<font color='#16a34a'><b>GREEN</b></font>", "Parked where planned", "Nothing. Nice!"],
                ["<font color='#ca8a04'><b>YELLOW</b></font>", "Stopped early: a waypoint took too long "
                 "(bumped, blocked, or slipping)", "Tell the coders. Write it in the test log."]],
               [1.5, 2.9, 2.6]),

    p("5. Quick checklist", H2),
    grid_table([["", "Before every AUTO"],
                ["[  ]", "Right OpMode: alliance color + NEAR/FAR agreed with our partner"],
                ["[  ]", "Robot on the right tile, touching the right wall, facing the right way (see the route picture)"],
                ["[  ]", "Press INIT. Both lights show our alliance color"],
                ["[  ]", "Start delay set (dpad up/down) and checked on the screen"],
                ["[  ]", "Pinpoint says READY. Hands off the robot"],
                ["[  ]", "Battery charged"]],
               [0.5, 6.5], row_h=0.23 * inch),
]

doc = SimpleDocTemplate(OUT, pagesize=letter, leftMargin=0.75 * inch, rightMargin=0.75 * inch,
                        topMargin=0.65 * inch, bottomMargin=0.8 * inch,
                        title="Basic Auton: Drive Team Guide", author="WMS Irondogs")
doc.build(story, onFirstPage=footer, onLaterPages=footer)
print("Wrote", OUT)
