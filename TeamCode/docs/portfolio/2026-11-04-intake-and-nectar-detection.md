# Intake and the NECTAR detector: how the code knows which ball is next

**Date(s):** 2026-10-15 – ___ (robot test still to do)
**Who worked on it:** build team (design), ___ (code owner, issue #4), with help from Claude (AI coding assistant) writing `Intake.java` and the test OpMode
**Links:** issue #4, PRs #45 #53 #56 #63, decisions 024, 025, 028
**Status:** Waiting on results (robot test)

## The problem
The robot has to pick up balls and move them up to the launcher. BIOBUZZ has two kinds of ball:
**POLLEN** (~2.8 in) and **NECTAR** (~3.6 in). They score differently, so the robot should know
**which ball is about to be launched**.

We also wanted the code written so that one student can own the intake, and so OpModes never have to
know how the hardware is wired (decision 006).

## What we thought (hypothesis)
- Two continuous servos are enough: one to pull balls in (`intakeServo`), one "water wheel" to lift them
  to the launcher (`waterWheelServo`). The team named the water wheel after how it looks (decision 028).
- A NECTAR is about 0.8 in wider than a POLLEN. If we put a **micro limit switch on each side** of the
  ball path, a NECTAR should be wide enough to press at least one lever and a POLLEN should pass without
  touching either. That gives us a NECTAR detector with no camera and no color sensor.
- A switch's raw reading is confusing (normally open wiring: **pressed reads `false`**). If every OpMode
  had to remember that, someone would get it backwards. So `Intake` should translate once and give the rest
  of the code a plain yes/no.

## What we tried
The design went through three rounds, all on issue #4:
1. **10/15:** 2 goBILDA Micro Limit Switches (bump lever, 3103-0001-0002) added to the hardware list. Job
   not decided yet.
2. **10/29 (decision 024):** intake becomes real code: `in()`, `out()`, water wheel up / down, `stop()`.
   Speeds and "reversed" flags are named constants at the top of `Intake.java`.
3. **11/2 (decision 025):** the switches become the **NECTAR detector**. `intake.isNectarDetected()` is true
   when **either** switch is pressed. The wiring is set once with an enum,
   `NECTAR_SWITCH_WIRING = SwitchWiring.NORMALLY_OPEN` (or `NORMALLY_CLOSED`).
4. **11/3 (decision 028):** renamed to the team's words: `waterWheelServo`, `nectarDetectorLeftSwitch`,
   `nectarDetectorRightSwitch`. Main TeleOp turns both lights **purple** while a NECTAR is detected, so the
   drive team can see it without reading the screen.

Operator controls (decision 029): A = intake on/off, X = water wheel up, B = water wheel down,
left trigger (hold) = eject everything.

Every device is loaded with `tryGet`, so a device missing from the Driver Station config shows **OFFLINE**
instead of crashing the whole OpMode. Nothing moves in INIT (rule G304.H).

## Results
_Fill in on the robot with the **Intake Test** OpMode (Tests group). Hold A/B = intake in/out, X/Y = water wheel up/down._

**Step 1: directions and speeds**

| Test | Expected | Actual | Fix if wrong |
|---|---|---|---|
| Hold A | balls pulled IN | | set `INTAKE_REVERSED = true` |
| Hold X | balls move UP to the launcher | | set `WATER_WHEEL_REVERSED = true` |
| Speed at 1.0 | pulls in and lifts without jamming | | lower `INTAKE_SPEED` / `WATER_WHEEL_SPEED` to ___ |

**Step 2: the switches**

| Test | Expected | Actual | Fix if wrong |
|---|---|---|---|
| Press left lever by hand | "Nectar left switch: PRESSED", "NECTAR detected: YES" | | reading never changes: try the other channel (n / n+1) of that digital port |
| Press right lever by hand | same, for right | | |
| Nothing pressed | both "not pressed", "NECTAR detected: no" | | says PRESSED when it isn't: switch `NECTAR_SWITCH_WIRING` |
| Run a NECTAR through | YES while it passes | | levers too far apart: move them in |
| Run a POLLEN through | stays "no" | | levers too close: move them out |

**Step 3: write down the wiring** (then copy it into `hardware-config.md`, Intake table)

| Device | Hub (CH / EH) | Port |
|---|---|---|
| `intakeServo` | | servo ___ |
| `waterWheelServo` | | servo ___ |
| `nectarDetectorLeftSwitch` | | digital ___ |
| `nectarDetectorRightSwitch` | | digital ___ |

Photos to add to `images/`: the levers from above with a POLLEN and with a NECTAR in the path.

## What we learned and decided
_Did a NECTAR press a lever every time? Did a POLLEN ever press one by mistake (false positive)?
What speeds did we end up with? Fill in after the test._

## What's next
- Run the test above and record the results here and on issue #4. Then the `TODO(#4)` comments in
  `Intake.java` can go.
- Decide what else should happen when a NECTAR is detected (only the lights so far). Does the launcher
  need a different speed or hood angle for the bigger ball? (issues #5, #31)
- Could the same switches count how many balls are stored? Not designed yet.
