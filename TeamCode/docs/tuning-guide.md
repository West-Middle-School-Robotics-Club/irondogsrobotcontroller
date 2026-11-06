# Tuning Guide: every number you can change

Every number the team tunes lives at the **top of the file that uses it**, as a named constant (code-structure rule 3).
This page lists them all in one place: **what it does, where it is, what it is now, and how to find the right value.**

**All files are under** `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`.

## How to change a value
1. Find the setting below and open its file. In Android Studio: press **Shift twice** and type the file name, then
   **Ctrl+F** (Mac: Cmd+F) and type the setting's name.
2. Change **only the number** (or `true`/`false`). Keep the name, the `;` and the comment.
3. Build and run to the robot. Test it with the OpMode in the "How to find it" column.
4. Write what you changed and why in `TeamCode/docs/changes-made.md`, and post the result on the issue.
5. Update the **Now** column on this page (or run the checker below: it tells you what's out of date).

**Status:** ✅ measured or tested on the robot · 🔧 placeholder, needs tuning · 🔒 a fact (game manual, data sheet): don't change

Most of these have a **printable worksheet** in `TeamCode/docs/worksheets/` (see its README) to fill in at the robot.

---

## ⭐ The ones you'll change most

| Setting | File | Now | What it does |
|---|---|---|---|
| `NORMAL_SCALE` | `opmodes/teleop/DriverControls.java` | `0.7` | Everyday driving speed (70% power) |
| `SLOW_MODE_SCALE` | `opmodes/teleop/DriverControls.java` | `0.4` | Hold a bumper: slow, for lining up |
| `TURBO_SCALE` | `opmodes/teleop/DriverControls.java` | `1.0` | Hold a trigger: full speed |
| `SNAP_ANGLE_DEGREES` | `opmodes/teleop/DriverControls.java` | `15.0` | Straight-line assist: how forgiving it is |
| `DEFAULT_SPIN_POWER` | `subsystems/Launcher.java` | `1.0` | Flywheel power (it runs the whole match) |
| `X_POD_OFFSET_MM` | `subsystems/Odometry.java` | `0.0` | Where the X odometry pod is, sideways from the turning center |
| `Y_POD_OFFSET_MM` | `subsystems/Odometry.java` | `0.0` | Where the Y odometry pod is, forward from the turning center |

The full lists are below.

---

## Driving (TeleOp): `opmodes/teleop/DriverControls.java`
Worksheet: `driver-tuning.pdf`. Test with **Main TeleOp**. Issues #3, #67.

| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `SLOW_MODE_SCALE` | `0.4` | Power while a bumper is held (0 to 1) | Drivers try lining up on a goal; pick what feels controllable | 🔧 |
| `NORMAL_SCALE` | `0.7` | Power with nothing held | Drivers drive laps; fast but still controllable | 🔧 |
| `TURBO_SCALE` | `1.0` | Power while a trigger is held | Usually leave at 1.0 (full power) | ✅ |
| `ASSIST_ON_AT_START` | `true` | Is straight-line assist ON when a TeleOp starts? (A toggles it) | Drivers' preference | 🔧 |
| `SNAP_ANGLE_DEGREES` | `15.0` | Stick within this many degrees of straight = drive exactly straight. Bigger = more forgiving, but diagonals need a bigger push | Try 10, 15, 20 | 🔧 |
| `STRAFE_CORRECTION` | `1.1` | Boosts sideways driving, because mecanum strafes slower than it drives forward | Strafe 48 in and drive 48 in at the same stick: raise it if strafing is shorter | 🔧 |
| `DEADZONE` | `0.05` | Ignore tiny stick movement so the robot doesn't creep | Raise it if the robot creeps with sticks released | ✅ |
| `TRIGGER_HELD` | `0.5` | How far a trigger must be pressed to count as "held" (turbo) | Rarely changed | ✅ |

## Operator (gamepad 2): `opmodes/teleop/OperatorControls.java`
Test with **Main TeleOp**. Decisions 029, 030.

| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `TURRET_MAX_STICK_SPEED` | `1.0` | Fastest the left stick turns the turret (0 to 1) | Lower it if aiming by hand is too twitchy | 🔧 |
| `TURRET_STICK_DEADZONE` | `0.05` | Ignore tiny stick movement | Raise it if the turret creeps with the stick released | ✅ |
| `TRIGGER_HELD` | `0.5` | How far the left trigger must be pressed to EJECT | Rarely changed | ✅ |

## Drive motors: `subsystems/Drivetrain.java`
| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `USE_ENCODER_SPEED_CONTROL` | `true` | `true` = motors hold a speed using their encoders; `false` = plain power | "Encoder Mode Toggle" test, issue #11 (decision 008 still open) | 🔧 |

The left motors are set to REVERSE in the constructor. That matches the robot's wiring; change it only if a side drives backwards.

## Odometry (Pinpoint): `subsystems/Odometry.java`
Worksheet: `pinpoint-test.pdf`. Test with **Pinpoint Test**. Issues #6, #21.

| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `POD_TYPE` | `goBILDA_4_BAR_POD` | Which pods we have (sets ticks per inch) | Push the robot 48 in: Pinpoint Test must read ~48 | ✅ |
| `X_POD_OFFSET_MM` | `0.0` | X pod (wheel rolls front-back): how far **sideways** from the turning center. Left = +, right = − | Measure from the **wheelbase center**, then the spin test | ✅ measured, 🔧 spin test to confirm |
| `Y_POD_OFFSET_MM` | `0.0` | Y pod (wheel rolls side-side): how far **forward** from the turning center. Front = +, back = − | Same as above | ✅ measured, 🔧 spin test to confirm |
| `X_POD_DIRECTION` | `FORWARD` | Push the robot forward: X must go UP. If not, `REVERSED` | Pinpoint Test | ✅ |
| `Y_POD_DIRECTION` | `REVERSED` | Push the robot left: Y must go UP. If not, `REVERSED` | Pinpoint Test | ✅ |

## Basic Auton: `opmodes/auto/BasicAuton.java`
Guide: `basic-auton-guide.pdf`, routes: `docs/game/diagrams/basic-auton-near.pdf` / `far.pdf`. Issue #22, decision 026.

| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `ROBOT_LENGTH_IN` | `17.98` | Robot length, front to back (inches) | Measured 11/3 | ✅ |
| `ROBOT_WIDTH_IN` | `15.99` | Robot width, left to right (inches) | Measured 11/3 | ✅ |
| `RED_NEAR_START`, `RED_NEAR_PATH` | (positions) | Where NEAR starts and drives to, for RED. BLUE is calculated | Taped practice field; re-run `make_diagrams.py` after changing | 🔧 |
| `RED_FAR_START`, `RED_FAR_PATH` | (positions) | Same for FAR | Same | 🔧 |
| `DRIVE_GAIN` | `0.05` | Power per inch away from the waypoint. Bigger = faster approach, but may overshoot | Taped field: overshoots? lower it. Crawls? raise it | 🔧 |
| `MAX_DRIVE_POWER` | `0.6` | Fastest Basic Auton drives | Same | 🔧 |
| `MIN_DRIVE_POWER` | `0.15` | Slowest it drives (lower and the robot may not move) | Lower until the robot just still moves, then add a little | 🔧 |
| `TURN_GAIN` | `0.02` | Turn power per degree away from the heading | Wobbles? lower it | 🔧 |
| `MAX_TURN_POWER` | `0.4` | Fastest it turns | | 🔧 |
| `POSITION_TOLERANCE_IN` | `1.5` | "Close enough" to a waypoint (inches) | Stops and jitters at a waypoint? raise it | 🔧 |
| `HEADING_TOLERANCE_DEG` | `3.0` | "Close enough" heading (degrees) | | 🔧 |
| `WAYPOINT_TIMEOUT_S` | `8.0` | Give up on one waypoint after this many seconds | | ✅ |
| `STOP_BY_S` | `28.0` | Stop everything by this time (AUTO is 30 s) | Keep below 30 | 🔒 |
| `MAX_DELAY_S` | `10` | Longest start delay the drive team can pick in INIT | | ✅ |
| `PARKED_COLOR` | `Light.Color.GREEN` | Lights at the end when parked | Team's choice | ✅ |
| `STOPPED_EARLY_COLOR` | `Light.Color.YELLOW` | Lights at the end when it stopped early | Team's choice | ✅ |

## Intake and water wheel: `subsystems/Intake.java`
Worksheet: `intake-test.pdf`. Test with **Intake Test**. Issue #4.

| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `INTAKE_SPEED` | `1.0` | Intake servo speed (0 to 1) | Lower it if balls bounce off | 🔧 |
| `WATER_WHEEL_SPEED` | `1.0` | Water wheel speed (0 to 1) | Lower it if balls jam at the top | 🔧 |
| `INTAKE_REVERSED` | `false` | Set `true` if "intake IN" pushes balls OUT | Intake Test, A button | 🔧 |
| `WATER_WHEEL_REVERSED` | `false` | Set `true` if "water wheel UP" moves balls DOWN | Intake Test, X button | 🔧 |
| `NECTAR_SWITCH_WIRING` | `NORMALLY_OPEN` | How the NECTAR switches are wired. If "PRESSED" shows when NOT pressed, use `NORMALLY_CLOSED` | Intake Test: press each lever | 🔧 |

## Launcher (flywheel + trigger gate): `subsystems/Launcher.java`
Worksheet: `launcher-test.pdf`. Test with **Launcher Test**. Issue #5, decision 032.

| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `DEFAULT_SPIN_POWER` | `1.0` | Flywheel power (0 to 1). Runs the whole match | Hit-rate test at a few powers; pick the most accurate | 🔧 |
| `MOTOR_REVERSED` | `true` | Flywheel direction | Launcher Test: balls must go OUT | 🔧 |
| `READY_VELOCITY_THRESHOLD` | `1000.0` | The flywheel counts as READY at this speed (ticks/second) | Launcher Test: read "Launcher Velocity" once settled; use ~90% of it | 🔧 placeholder |
| `TRIGGER_REST_POSITION` | `0.0` | Trigger gate position that **blocks** the balls (0 to 1) | Launcher Test, left bumper | 🔧 |
| `TRIGGER_FIRE_POSITION` | `0.5` | Trigger gate position that **lets balls through** | Launcher Test, left bumper | 🔧 |

## Turret and hood: `subsystems/Turret.java`
Worksheet: `turret-test.pdf`. Test with **Turret Test**, **in this order** (steps 1 to 5). Issue #31.

| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `SERVO_REVERSED` | `false` | Step 1: set `true` if a + (left) speed turns the turret RIGHT | Turret Test, dpad left | 🔧 |
| `ENCODER_REVERSED` | `false` | Step 2: set `true` if "Turret angle" goes DOWN when turning left | Turret Test | 🔧 |
| `ENCODER_REVS_PER_TURRET_REV` | `1.0` | Step 3: encoder turns per one turret turn (gear ratio) | Turn exactly 90°; angle must read ~90. Or count gear teeth | 🔧 (changes if the encoder moves to the big gear) |
| `HOME_ANGLE_DEGREES` | `180.0` | Where the turret points when the home switch triggers. Mounted reversed, so the switch is at the REAR (decision 033). Change only if the turret is remounted | Where the magnet is | ✅ |
| `MAX_LEFT_DEGREES` | `270.0` | Step 4: farthest the turret may turn left. Angles don't wrap: from the rear (180), left counts up | Home it, turn slowly left until wires/frame get close; back off. Must be over 180 | 🔧 placeholder (rear ±90) |
| `MAX_RIGHT_DEGREES` | `90.0` | Step 4: farthest right. From the rear, right counts down | Same, turning right. Must be under 180 | 🔧 placeholder (rear ±90) |
| `AIM_GAIN` | `0.02` | Step 5: speed per degree away from a target. Bigger = faster, may wobble | Turret Test X / B (±45°) | 🔧 |
| `MAX_SPEED` | `0.6` | Fastest the turret turns to a target | | 🔧 |
| `TOLERANCE_DEGREES` | `1.0` | "Close enough" to a target | | 🔧 |
| `NOT_HOMED_MAX_SPEED` | `0.35` | Speed cap until the turret has passed forward once | 0.15 was too slow (10/15) | ✅ |
| `RUNAWAY_DEGREES` | `20.0` | Safety: stop if it moves this much the WRONG way | Leave it | ✅ |
| `HOME_TOLERANCE_COUNTS` | `200` | How close to forward counts as "home" (light green) | Smaller = stricter | ✅ |
| `HOME_SWITCH_TRIGGERED_STATE` | `false` | What the home switch reads at the magnet | Turret Test: "Home switch (magnet)" says YES at the magnet | ✅ |
| `HOOD_LOWEST_POSITION` | `0.0` | Hood's flattest position (0 to 1). Keeps it off its hard stop | Turret Test dpad down | 🔧 |
| `HOOD_HIGHEST_POSITION` | `1.0` | Hood's steepest position | Turret Test dpad up | 🔧 |
| `HOOD_START_POSITION` | `0.5` | Hood position after START in Turret Test | | 🔧 |
| `SERVO_PWM_MIN_US` | `1000` | Servo signal range: **only change if the servo model changes** (goBILDA 1000-2000; Axon MAX 1050-1950) | Servo data sheet | 🔒 |
| `SERVO_PWM_MAX_US` | `2000` | Same | Same | 🔒 |
| `ENCODER_COUNTS_PER_REV` | `4000.0` | ELC Encoder V2 counts per turn | Data sheet | 🔒 |

## Camera (Limelight): `subsystems/Camera.java`
Test with **Camera Test** / **Red/Blue Camera Test**. Issue #7 (the other mentor's area).

| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `CAMERA_HEIGHT_INCHES` | `13.0` | Lens height above the floor | Tape measure on the robot | 🔧 placeholder |
| `CAMERA_MOUNT_TILT_DEGREES` | `0.0` | Camera tilt up from level | Angle finder; change when the mount changes | ✅ (flat mount) |
| `TAG_HEIGHT_INCHES` | `44.0` | AprilTag height above the floor | Measure on a real HIVE | 🔧 |
| `APRILTAG_PIPELINE` | `0` | Which Limelight pipeline slot has our AprilTag setup | Limelight web page | ✅ |
| `TAG_SIZE_INCHES` | `3.25` | AprilTag size; the Limelight "marker size" must be 82.55 mm | Game manual | 🔒 |
| `TAG_RED_FAR_MIN` … `TAG_BLUE_FAR_MAX` | `30` … `45` | Which AprilTag IDs belong to each alliance | Game manual | 🔒 |

## Lights
| Setting | File | Now | What it does | Status |
|---|---|---|---|---|
| `NECTAR_COLOR` | `opmodes/teleop/MainTeleOp.java` | `Light.Color.VIOLET` | Both lights while the next ball is a NECTAR ("purple") | ✅ |
| `RED_ALLIANCE_COLOR` | `opmodes/teleop/MainTeleOp.java` | `Light.Color.RED` | Lights for RED (INIT, and when no NECTAR) | ✅ |
| `BLUE_ALLIANCE_COLOR` | `opmodes/teleop/MainTeleOp.java` | `Light.Color.BLUE` | Lights for BLUE | ✅ |
| Color list (`RED(0.282)`, `VIOLET(0.722)`, …) | `subsystems/Light.java` | | The servo position that makes each color. Fix any color that looks wrong | 🔧 (check with **Light Test**) |

## Other
| Setting | File | Now | What it does | Status |
|---|---|---|---|---|
| `LOW_BATTERY_VOLTS` | `subsystems/Power.java` | `11.5` | Below this, telemetry says the battery is LOW | ✅ |
| `REST_POSITION`, `POSITION_A`, `POSITION_B`, `SWITCH_SECONDS` | `subsystems/FlowerFlipper.java` | `0.5`, `0.4`, `0.6`, `0.15` | Flower flipper shake (on hold: may not be on the build) | 🔧 |

**Not settings:** the hardware **config names** (`..._NAME = "..."`) must match the Driver Station config. Change them only
together with the config and `hardware-config.md`. **Test-only** speeds (e.g. `DPAD_SPEED` in `opmodes/test/TurretTest.java`)
only change that test.

---

## Keeping this page right
The code is the truth. If this page and the code disagree, the code wins: fix this page. A checker compares every
**Now** value here with the code:

```
python3 TeamCode/docs/check_tuning_guide.py
```
It prints each setting that is out of date, or "All ... values match the code." Run it after tuning, before you open a PR.
