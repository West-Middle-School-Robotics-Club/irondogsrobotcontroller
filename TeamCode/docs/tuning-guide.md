# Tuning Guide: every number you can change

Every number the team tunes lives at the **top of the file that uses it**, as a named constant (code-structure rule 3).
This page lists them all in one place: **what it does, where it is, what it is now, and how to find the right value.**

**All files are under** `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`.

**The code is the source of truth** (decision 051). The team tunes values on the robot, and the code holds the
latest one. If this page, a worksheet or a decision gives a different number, the code wins: fix the doc.

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
| `NORMAL_SCALE` | `opmodes/teleop/DriverControls.java` | `0.8` | Everyday driving speed (80% power) |
| `SLOW_MODE_SCALE` | `opmodes/teleop/DriverControls.java` | `0.5` | Hold a bumper: slow, for lining up |
| `TURBO_SCALE` | `opmodes/teleop/DriverControls.java` | `1.0` | Hold a trigger: full speed |
| `SNAP_ANGLE_DEGREES` | `opmodes/teleop/DriverControls.java` | `20.0` | Straight-line assist: how forgiving it is |
| `DEFAULT_SPIN_POWER` | `subsystems/Launcher.java` | `0.75` | Flywheel power (it runs the whole match) |
| `X_POD_OFFSET_MM` | `subsystems/Odometry.java` | `0.0` | Where the X odometry pod is, sideways from the turning center |
| `Y_POD_OFFSET_MM` | `subsystems/Odometry.java` | `0.0` | Where the Y odometry pod is, forward from the turning center |

The full lists are below.

---

## Driving (TeleOp): `opmodes/teleop/DriverControls.java`
Worksheet: `driver-tuning.pdf`. Test with **Main TeleOp**. Issues #3, #67.

| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `SLOW_MODE_SCALE` | `0.5` | Power while a bumper is held (0 to 1) | Drivers try lining up on a goal; pick what feels controllable | 🔧 |
| `NORMAL_SCALE` | `0.8` | Power with nothing held | Drivers drive laps; fast but still controllable | 🔧 |
| `TURBO_SCALE` | `1.0` | Power while a trigger is held | Usually leave at 1.0 (full power) | ✅ |
| `ASSIST_ON_AT_START` | `true` | Is straight-line assist ON when a TeleOp starts? (A toggles it) | Drivers' preference | 🔧 |
| `SNAP_ANGLE_DEGREES` | `20.0` | Stick within this many degrees of straight = drive exactly straight. Bigger = more forgiving, but diagonals need a bigger push | Try 10, 15, 20 | 🔧 |
| `STRAFE_CORRECTION` | `1.1` | Boosts sideways driving, because mecanum strafes slower than it drives forward | Strafe 48 in and drive 48 in at the same stick: raise it if strafing is shorter | 🔧 |
| `DEADZONE` | `0.05` | Ignore tiny stick movement so the robot doesn't creep | Raise it if the robot creeps with sticks released | ✅ |
| `TRIGGER_HELD` | `0.5` | How far a trigger must be pressed to count as "held" (turbo) | Rarely changed | ✅ |

## Operator (gamepad 2): `opmodes/teleop/OperatorControls.java`
Test with **Main TeleOp**. Decisions 029, 030.

| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `TURRET_MAX_STICK_SPEED` | `1.0` | Fastest the left stick turns the turret (0 to 1) | Lower it if aiming by hand is too twitchy | 🔧 |
| `TURRET_STICK_DEADZONE` | `0.05` | Ignore tiny stick movement | Raise it if the turret creeps with the stick released | ✅ |
| `INTAKE_ON_AT_START` | `false` | Do the intake + water wheel start ON when the match starts? `false` = they start OFF, the operator presses A to start them (decision 062) | Operator's preference | 🔧 |
| `DIRECTION_TOGGLE_ENABLED` | `false` | Can B flip the intake + water wheel to OUT? `false` = B does nothing, the direction is always IN (decision 061). The left trigger (EJECT) still pushes balls out | Set `true` to turn the B toggle back on | 🔧 |
| `TURRET_ENABLED` | `false` | `OperatorControls.java`: can the turret move in Main TeleOp? `false` = left stick and X do nothing, and no move at START (decision 050) | Set `true` to turn the turret controls back on | 🔧 |
| `TRIGGER_HELD` | `0.5` | How far the left trigger must be pressed to EJECT | Rarely changed | ✅ |
| `AUTO_FIRE_NECTAR` | `true` | A NECTAR on a nectar detector switch opens the launcher trigger by itself (decision 059). `false` = only Y opens it | Operator's preference. Turn it off if it fires when it shouldn't | 🔧 |
| `AUTO_FIRE_HOLD_S` | `0.5` | Seconds the trigger stays open after the switch lets go, so it doesn't close on the ball | Intake + Launcher Test: NECTAR gets clipped by the gate? longer. A POLLEN right behind it also gets launched? shorter | 🔧 |

## Main TeleOp start-up: `opmodes/teleop/MainTeleOp.java`
Test with **Main TeleOp**. Decision 041.

| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `TURRET_FIND_HOME_AT_START` | `false` | At START the turret turns to forward by itself if it is already homed. `true` = it also turns LEFT to find the magnet first | Keep `false` until the turret's hard stops are moved (the left stop is before the magnet, so searching drives into it) | 🔧 |

## Drive motors: `subsystems/Drivetrain.java`
| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `USE_ENCODER_SPEED_CONTROL` | `false` | `true` = motors hold a speed using their encoders; `false` = plain power (% of battery voltage). Every OpMode starts in this mode | Decision 055 (11/11): plain power everywhere, after encoder mode lurched at Basic Auton waypoints. "Encoder Mode Toggle" test compares the two | ✅ |

The left motors are set to REVERSE in the constructor. That matches the robot's wiring; change it only if a side drives backwards.

## Odometry (Pinpoint): `subsystems/Odometry.java`
Worksheet: `pinpoint-test.pdf`. Test with **Pinpoint Test**. Issues #6, #21.

| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `POD_TYPE` | `goBILDA_4_BAR_POD` | Which pods we have (sets ticks per inch) | Push the robot 48 in: Pinpoint Test must read ~48 | ✅ |
| `X_POD_OFFSET_MM` | `0.0` | X pod (wheel rolls front-back): how far **sideways** from the turning center. Left = +, right = − | Measure from the **wheelbase center**, then the spin test | ✅ measured; spin test 11/7: 360.4° turned, heading 0.4° after, X drift 1.0 in, Y drift 2.11 in (close enough) |
| `Y_POD_OFFSET_MM` | `0.0` | Y pod (wheel rolls side-side): how far **forward** from the turning center. Front = +, back = − | Same as above | ✅ measured; spin test 11/7 (see above) |
| `X_POD_DIRECTION` | `FORWARD` | Push the robot forward: X must go UP. If not, `REVERSED` | Pinpoint Test | ✅ |
| `Y_POD_DIRECTION` | `REVERSED` | Push the robot left: Y must go UP. If not, flip it | Pinpoint Test (11/7: Y went up pushing RIGHT, so changed to `REVERSED`; confirmed after: pushing left makes Y go up) | ✅ |

The Pinpoint Test OpMode drives in BRAKE with straight-line assist ON and a dpad **speed dial**:

| Setting | File | Now | What it does | Status |
|---|---|---|---|---|
| `SPEED_DIAL_START` | `opmodes/test/PinpointTest.java` | `0.5` | Where the speed dial is at START (times the normal drive power) | ✅ |
| `SPEED_DIAL_STEP` | `opmodes/test/PinpointTest.java` | `0.1` | How much one dpad up / down press changes it | ✅ |
| `SPEED_DIAL_MIN` | `opmodes/test/PinpointTest.java` | `0.1` | The dial never goes below this | ✅ |

## Basic Auton: `opmodes/auto/BasicAuton.java`
Guide: `basic-auton-guide.pdf`, routes: `docs/game/diagrams/basic-auton-near.pdf` / `far.pdf`. Issue #22, decisions 026, 053.
Positions are **field coordinates** (011). To tune a route on any clear floor, use **Main Auton** (`opmodes/auto/MainAuton.java`, was Test Auton): it starts at (0, 0, 0) and holds the moves as forward / left distances (`MAIN_NEAR_PATH`, `MAIN_FAR_PATH`). Since decision 060 Main Auton is what we run for FAR, and its moves were tuned on the floor, so they no longer match Basic Auton exactly.

| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `ROBOT_LENGTH_IN` | `17.98` | Robot length, front to back (inches) | Measured 11/3 | ✅ |
| `ROBOT_WIDTH_IN` | `15.99` | Robot width, left to right (inches) | Measured 11/3 | ✅ |
| `RED_NEAR_START`, `RED_NEAR_PATH` | (positions) | Where NEAR starts and drives to, for RED, in field coordinates. BLUE is calculated. The start HEADING decides which field direction is forward (NEAR faces -90: forward = -Y, right = -X) | Measure the moves with Main Auton on any floor, convert to field positions (each waypoint comment shows the move); re-run `make_diagrams.py` after changing | 🔧 |
| `RED_FAR_START`, `RED_FAR_PATH` | (positions) | Same for FAR (faces 0: forward = +X, left = +Y) | Same | 🔧 |
| `USE_ENCODER_SPEED_CONTROL` | `false` | Basic Auton's own copy of the drive mode: `false` = plain power, % of battery voltage, same as the whole robot (decision 055). Main Auton inherits it | Keep `false`. If a test ever sets it `true`, `MIN_DRIVE_POWER` / `MIN_TURN_POWER` can drop to 0.05 | ✅ |
| `DRIVE_GAIN` | `0.02` | Power per inch away from the waypoint. Bigger = faster approach, but may overshoot | Taped field: overshoots? lower it. Crawls? raise it | 🔧 |
| `MAX_DRIVE_POWER` | `0.5` | Fastest Basic Auton drives (plain power since 11/11; the team set 0.5 on the floor) | Same | 🔧 |
| `MAX_SIDEWAYS_POWER` | `0.25` | Fastest it drives sideways (strafing). Blended with `MAX_DRIVE_POWER` by how sideways the move is | Slides past sideways waypoints? lower it | 🔧 |
| `MIN_DRIVE_POWER` | `0.2` | Slowest it drives (lower and the robot may not move). Plain power since 11/11; the team set 0.2 on the floor | Lower until the robot just still moves, then add a little | 🔧 |
| `BRAKE_LOOKAHEAD_S` | `0.15` | Brake this many seconds early. The code aims the spot the robot will reach in this time (speed from the Pinpoint) at the waypoint, so it doesn't slide past | Still overshoots? raise it. Stops short and crawls the last bit? lower it | 🔧 |
| `BRAKE_LOOKAHEAD_SIDEWAYS_S` | `0.3` | Brake lookahead for sideways moves. Braked mecanum wheels still roll sideways on their rollers, so strafes need to brake earlier | Still slides past sideways? raise it. Stops short? lower it | 🔧 |
| `TURN_GAIN` | `0.02` | Turn power per degree away from the heading | Wobbles? lower it | 🔧 |
| `MIN_TURN_POWER` | `0.12` | Slowest turn that still moves the robot. Without it a 3° error gives a power too small to budge, and the waypoint never counts | Sits a few degrees off and never finishes? raise it. Twitches? lower it | 🔧 |
| `MAX_TURN_POWER` | `0.2` | Fastest it turns | | 🔧 |
| `POSITION_TOLERANCE_IN` | `1` | "Close enough" to a waypoint (inches) | Stops and jitters at a waypoint? raise it | 🔧 |
| `HEADING_TOLERANCE_DEG` | `2.0` | "Close enough" heading (degrees) | | 🔧 |
| `SETTLED_SPEED_IN_PER_S` | `2.0` | "Stopped" = slower than this. A waypoint only counts once the robot is close enough AND stopped, so a slide through the spot doesn't count | Takes long to "finish" each waypoint? raise it a little | 🔧 |
| `WAYPOINT_TIMEOUT_S` | `4.0` | Give up on one waypoint after this many seconds, plus the per-inch time below | | ✅ |
| `WAYPOINT_TIMEOUT_PER_INCH_S` | `0.15` | Extra timeout per inch of the move (100 in = 4 + 15 = 19 s). A fixed 8 s gave up on the long FAR drive | Times out on a drive that was going fine? raise it | 🔧 |
| `WAYPOINT_PAUSE_S` | `2.0` | After each waypoint, stop and sit still this long before the next move (lets the robot settle; telemetry shows the Pinpoint position) | Longer = easier to watch and measure; shorter = faster run. Everything must still finish by `STOP_BY_S` | 🔧 |
| `STOP_BY_S` | `28.0` | Stop everything by this time (AUTO is 30 s) | Keep below 30 | 🔒 |
| `MAX_DELAY_S` | `10` | Longest start delay the drive team can pick in INIT | | ✅ |
| `RECALIBRATE_AT_START` | `true` | Re-calibrate the Pinpoint gyro at START, when the robot is surely still. If someone bumps the robot during INIT's calibration the heading drifts all run | Keep on. Costs about half a second of AUTO | ✅ |
| `RECALIBRATE_MIN_S` | `0.5` | How long the START re-calibration is given before READY is trusted | | ✅ |
| `PINPOINT_READY_TIMEOUT_S` | `3.0` | After START, how long to wait for the Pinpoint to be READY before setting the start position. If it never is, the robot does not drive (YELLOW lights) | Normally READY already, since INIT shows its status | ✅ |
| `PARKED_COLOR` | `Light.Color.GREEN` | Lights at the end when parked | Team's choice | ✅ |
| `STOPPED_EARLY_COLOR` | `Light.Color.YELLOW` | Lights at the end when it stopped early | Team's choice | ✅ |

## Timed Auton: `opmodes/auto/TimedAuton.java`
The no-sensor backup for Basic Auton NEAR and FAR (decisions 054, 057): plain power for a set time, no Pinpoint, no encoders. Tune with a **charged battery**; a weak battery drives shorter.

| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `DRIVE_POWER` | `0.3` | Wheel power for every move (plain power = % of the battery) | Pick a power that doesn't slide or spin the wheels, then tune the times. Change the power = re-tune all the times | 🔧 |
| `FORWARD_S` | `1.0` | Seconds driving forward off the wall (goal: 23 in) | Run it, measure how far it went, scale: new time = time × 23 / inches measured. **Worked on the robot 11/11 (Timed Auton NEAR, RED and BLUE)** | ✅ |
| `RIGHT_S` | `0.8` | Seconds strafing right, toward the audience (goal: 12 in) | Same, × 12 / measured. Strafing is slower than driving, so it needs more time per inch. **Worked on the robot 11/11 (Timed Auton NEAR, RED and BLUE)** | ✅ |
| `BACK_S` | `0.8` | Seconds driving back into the LOADING ZONE (goal: 18 in) | Same, × 18 / measured. Must stop before hitting the wall. **Worked on the robot 11/11 (Timed Auton NEAR, RED and BLUE)** | ✅ |
| `FAR_FORWARD_S` | `3.7` | FAR: seconds driving forward up column B, the long drive (goal: 85 in, was 100 until 11/11). Starting guess scaled from `FORWARD_S` | Same, × 85 / measured. Too short = no LEAVE; too long = into the far end of the field | 🔧 |
| `FAR_LEFT_S` | `1.5` | FAR: seconds strafing left into the LOADING ZONE (goal: 23 in, was 20 until 11/11). Starting guess scaled from `RIGHT_S` | Same, × 23 / measured. Must stop before hitting the wall | 🔧 |
| `PAUSE_S` | `0.5` | Sit still between moves so one slide doesn't carry into the next | Moves blend together? longer | 🔧 |
| `START_DELAY_S` | `0.0` | Wait after START before moving (no dpad: change it in the code before the match) | Agree with the partner | 🔧 |

## Intake and water wheel: `subsystems/Intake.java`
Worksheet: `intake-test.pdf`. Test with **Intake Test**. Issue #4.

| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `INTAKE_SPEED` | `1.0` | Intake servo speed (0 to 1). 1.0 since 11/8 testing (was 0.65, decision 041) | Intake Test: dpad up / down changes the speed of both live (0.05 steps), then copy the number here. Lower it if balls bounce off | 🔧 |
| `WATER_WHEEL_SPEED` | `0.2` | Water wheel MOTOR power (0 to 1). Since 11/11 the water wheel is a goBILDA 117 RPM motor (decision 056); 0.2 since 11/12 (was 0.4). About twice as fast as the old servo at the same number, so 1.0 may be too fast: re-tune. (Servo history: 1.0 since 11/8, was 0.65) | Intake Test: same dpad speed as the intake; use X = water wheel only to tune it alone, then copy the number here. Lower it if balls jam at the top | 🔧 |
| `INTAKE_REVERSED` | `true` | Set `true` if "intake IN" pushes balls OUT. 11/7: Main TeleOp ran OUT at start, so `true`. Confirmed 11/7: IN at start, eject pushes balls out | Intake Test, A button | ✅ |
| `WATER_WHEEL_REVERSED` | `true` | Set `true` if "water wheel UP" moves balls DOWN. Was confirmed 11/7 for the old servo; the new motor (11/11) has NOT been checked | Intake Test step 2: X = water wheel only, A, balls must go UP | 🔧 |
| `NECTAR_SWITCH_WIRING` | `NORMALLY_CLOSED` | How the NECTAR switches are wired. If "PRESSED" shows when NOT pressed, use `NORMALLY_CLOSED` | Intake Test: press each lever | ✅ |

## Launcher (flywheel + trigger gate): `subsystems/Launcher.java`
Worksheet: `launcher-test.pdf`. Test with **Launcher Test**. Issue #5, decision 032.

| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `DEFAULT_SPIN_POWER` | `0.75` | Flywheel power (0 to 1). Runs the whole match. 0.75 since 11/12 (was 1.0 from 11/8 testing, 0.45 before, decision 041) | Hit-rate test at a few powers; pick the most accurate | 🔧 |
| `MOTOR_REVERSED` | `true` | Flywheel direction | Launcher Test: balls must go OUT | 🔧 |
| `READY_VELOCITY_THRESHOLD` | `1000.0` | The flywheel counts as READY at this speed (ticks/second) | Launcher Test: read "Launcher Velocity" once settled; use ~90% of it | 🔧 placeholder |
| `TRIGGER_REST_POSITION` | `0.0` | Trigger gate position that **blocks** the balls (0 to 1) | Launcher Test, left bumper | 🔧 |
| `TRIGGER_FIRE_POSITION` | `1.0` | Trigger gate position that **lets balls through** | Launcher Test, left bumper | 🔧 |

## Turret and hood: `subsystems/Turret.java`
Worksheet: `turret-test.pdf`. Test with **Turret Test**, **in this order** (steps 1 to 5). Issue #31.

| Setting | Now | What it does | How to find it | Status |
|---|---|---|---|---|
| `SERVO_REVERSED` | `false` | Step 1: set `true` if a + (left) speed turns the turret RIGHT | Turret Test, dpad left | 🔧 |
| `ENCODER_REVERSED` | `false` | Step 2: set `true` if "Turret angle" goes DOWN when turning left | Turret Test | 🔧 |
| `ENCODER_REVS_PER_TURRET_REV` | `4.11` | Step 3: encoder turns per one turret turn (gear ratio) | Turn exactly 90°; angle must read ~90. Or count gear teeth | ✅ 18-tooth servo gear, about 84.5-tooth turret gear (11/7). Changes if the encoder moves to the big gear. 11/8: the team changed it from 4.69 to 4.11 after the 90° test moves |
| `HOME_ANGLE_DEGREES` | `0` | The angle the code calls the home magnet. Keep it 0: every turret angle is measured from the magnet (decisions 042, 047). To move "forward", change `FORWARD_ANGLE_DEGREES` | | ✅ |
| `FORWARD_ANGLE_DEGREES` | `0.0` | Where ROBOT FORWARD is in turret angles. At the magnet the turret faces forward, so 0: no offset (decision 048). The ONE place an offset would live if the turret or magnet is remounted | Turret Test X / B must land 45° left / right of the robot's front | ✅ |
| `MAX_LEFT_DEGREES` | `120.0` | Step 4: farthest the turret may turn LEFT, in turret angles (0 = magnet = forward). 135° left of forward (decisions 047, 048) | Turn slowly to it once and check the wiring. The turret coasts a few degrees past a limit | 🔧 check the wiring once |
| `MAX_RIGHT_DEGREES` | `-120.0` | Step 4: farthest RIGHT: 135° right of forward (decisions 047, 048) | Same | 🔧 check the wiring once |
| `AIM_GAIN` | `0.02` | Step 5: speed per degree away from a target. Bigger = faster, may wobble | Turret Test X / B (±90° from the magnet) | 🔧 |
| `MIN_AIM_SPEED` | `0.15` | Slowest aiming speed. The servo doesn't move below some power, so without this the turret stopped 3-5° short of a target (decision 045) | Turret Test: A, X, B must land within about 1° of the target. Stops short: raise it. Jumps past: lower it | 🔧 not tested yet |
| `MAX_SPEED` | `0.7` | Fastest the turret turns to a target | | 🔧 |
| `TOLERANCE_DEGREES` | `0.5` | "Close enough" to a target | | 🔧 |
| `NOT_HOMED_MAX_SPEED` | `0.45` | Speed cap until the turret has passed forward once | 0.15 was too slow (10/15) | ✅ |
| `RUNAWAY_DEGREES` | `10.0` | Safety: stop if it moves this much the WRONG way | Leave it | ✅ |
| `HOME_TOLERANCE_COUNTS` | `100` | How close to forward counts as "home" (light green) | Smaller = stricter | ✅ |
| `CENTER_HOME_ON_MAGNET` | `true` | Home on the MIDDLE of the magnet's range instead of the first trigger (decision 043). The turret must pass all the way through the magnet once. Set `false` for the old edge homing | Turret Test: turn left through the magnet, "Magnet range" shows its width and "homed on the MIDDLE". Press A / X / B several times: the turret should land within about 1° each time | 🔧 not tested yet |
| `MIN_MAGNET_ZONE_COUNTS` | `20` | A pass through the magnet narrower than this is ignored (the turret turned around inside it) | Compare with the "Magnet range" counts in Turret Test; keep it well under that | 🔧 |
| `REST_ANGLE_DEGREES` | `HOME_ANGLE_DEGREES` | Where X (operator) and A (Turret Test) turn the turret: onto the home magnet, which is angle 0 now (decision 042). It follows HOME_ANGLE_DEGREES. Was 10.5 = dead forward (039, 040) | Turret Test A: the turret stops on the magnet and both lights go GREEN | 🔧 |
| `HOME_SEARCH_TIMEOUT_S` | `10.0` | Home search (X / A / start-up) when the turret is NOT homed: how long it may look, turning left first and then right if it hits a stop (decision 044). Gives up after this many seconds | Time a full sweep both ways, then add a margin | 🔧 not tested yet |
| `SEARCH_STALL_S` | `0.4` | During the home search, no encoder movement for this long = the turret hit a hard stop, so turn around. A second stop = give up | Too short: stops it before it starts. Too long: pushes into the stop | 🔧 |
| `SEARCH_STALL_COUNTS` | `15` | The least the encoder must change to count as "still moving" (about 52 counts = 1°) | Leave it | 🔧 |
| `APPROACH_OVERSHOOT_DEGREES` | `2.0` | A move to the rest spot always ends moving the same way the turret moved when it homed. On the wrong side, it first goes this far past, then comes back (gear backlash) | Press A from both sides of the magnet: it should land in the same spot | 🔧 |
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
| `CAMERA_MOUNT_TILT_DEGREES` | `60.0` | Camera tilt up from level | Angle finder; change when the mount changes | 🔧 (about 60, estimated 11/6) |
| `TAG_HEIGHT_INCHES` | `44.0` | AprilTag height above the floor | Measure on a real HIVE | 🔧 |
| `APRILTAG_PIPELINE` | `0` | Which Limelight pipeline slot has our AprilTag setup | Limelight web page | ✅ |
| `TAG_SIZE_INCHES` | `3.25` | AprilTag size; the Limelight "marker size" must be 82.55 mm | Game manual | 🔒 |
| `TAG_RED_FAR_MIN` … `TAG_BLUE_FAR_MAX` | `30` … `45` | Which AprilTag IDs belong to each alliance | Game manual | 🔒 |

## Lights
| Setting | File | Now | What it does | Status |
|---|---|---|---|---|
| `NECTAR_COLOR` | `opmodes/teleop/MainTeleOp.java` | `Light.Color.VIOLET` | Both lights while the next ball is a NECTAR ("purple") | ✅ |
| `TURRET_MOVING_COLOR` | `opmodes/teleop/MainTeleOp.java` | `Light.Color.GREEN` | Both lights while the turret is homed and turning to forward (NECTAR purple wins) | 🔧 |
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
