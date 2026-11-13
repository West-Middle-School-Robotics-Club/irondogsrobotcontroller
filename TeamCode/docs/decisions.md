# Decision Log — BIOBUZZ 2026–27

This file records **why** we made important choices, so new members (and judges) can understand our robot and code.

**How to use it**
1. Discuss the decision in a GitHub Issue first.
2. When the team agrees, add an entry at the bottom using the template, and link the Issue.
3. Never delete an old decision. If we change our minds, add a new entry and mark the old one `Superseded by #NNN`.

**Template**
```
## NNN – Short title  (YYYY-MM-DD)
Status: Proposed | Accepted | Superseded by NNN
Issue: #__
Context: What problem are we solving? What constraints matter?
Options: What did we consider?
Decision: What did we pick, and why?
Who: Who was part of the decision?
```

---

## 001 – Use FTC SDK v12.0 and keep team code in `TeamCode`  (2026-09-24)
Status: Accepted
Context: Our repo is a fork of FIRST's official FtcRobotController. FIRST releases SDK updates during the season that we'll need to merge in.
Options: Edit anywhere in the project, or only in `TeamCode/`.
Decision: All team code lives in `TeamCode/`. We don't edit the `FtcRobotController/` module, so SDK updates merge cleanly. When we want a sample, we copy it into `TeamCode/`.
Who: Team

## 002 – Don't use AprilTags for field localization  (2026-09-24)
Status: Accepted
Context: In BIOBUZZ the AprilTags are mounted on moving game elements. The SDK v12.0 release notes say they aren't suitable for absolute field localization. Tags come in "clusters" whose origin is the center of a Cell opening.
Options: Use AprilTags for position, or use AprilTags only for aiming.
Decision: We use AprilTags only for **aiming at Cells**. Robot position comes from odometry (see 005). Any AprilTag code must handle both `AprilTagSingleDetection` and `AprilTagClusterDetection` (breaking change in v12.0).
Who: Team

## 003 – Program in Android Studio  (2026-09-24)
Status: Accepted
Context: The options are Blocks, OnBot Java (in the browser), or Android Studio.
Options: Blocks, OnBot Java, Android Studio.
Decision: **Android Studio.** It works with git and GitHub (Issues, pull requests, code review) and lets us use third-party libraries (path following, FTC Dashboard). It requires Android Studio **Narwhal 3 Feature Drop or later**. Never accept Android Studio's offer to downgrade the Android Gradle Plugin.
Who: Team

## 004 – Four-motor mecanum drivetrain  (2026-09-24)
Status: Accepted
Context: The drivetrain decides how the robot moves and how the drive code is written.
Options: Mecanum, tank/differential.
Decision: **Four-motor mecanum.** Strafing (driving sideways) makes lining up with scoring locations easier in both TeleOp and autonomous. It's also the most common FTC drivetrain, with lots of examples (see the SDK samples `BasicOmniOpMode_Linear` and `RobotTeleopMecanumFieldRelativeDrive`).
Who: Team

## 005 – goBILDA Pinpoint for odometry  (2026-09-24)
Status: Accepted
Context: Because of decision 002, we need something other than AprilTags to know where the robot is on the field during autonomous.
Options: goBILDA Pinpoint, SparkFun OTOS, drive encoders + IMU only.
Decision: **goBILDA Pinpoint** with two odometry pods. It's accurate, widely used, and has a built-in SDK driver (see the SDK sample `SensorGoBildaPinpoint`). Path-following libraries like Road Runner and Pedro Pathing support it.
Who: Team

## 006 – Structured subsystems code style  (2026-09-24)
Status: Accepted
Context: Several students will work on the code at the same time, and it needs to stay readable as the robot grows.
Options: Simple OpModes with one hardware class, structured subsystems, or a command-based framework (FTCLib/NextFTC).
Decision: **Structured subsystems.** Each mechanism gets its own class (e.g. `Drive`, `Intake`, `Arm`) that owns its hardware and exposes simple methods. OpModes stay short and call those methods. Students can each own a subsystem with fewer merge conflicts. This is easier to learn than a command-based framework, and we can move to one later if needed.
Who: Team

## 007 – One direction convention: +X forward, +Y left, +heading counter-clockwise  (2026-09-24)
Status: Accepted
Issue: #6
Context: The Pinpoint reports +Y = left and +heading = counter-clockwise. Our first `Drivetrain.drive()` used +strafe = right and +turn = clockwise, to match the gamepad sticks. Mixing the two in autonomous would drive the robot the wrong way without any error.
Options: Match the gamepad sticks everywhere, or match the Pinpoint everywhere.
Decision: **Match the Pinpoint**: +X forward, +Y left, +heading counter-clockwise. This is also what Road Runner, Pedro Pathing and FTC field coordinates use, so outside code and examples agree with ours. `Drivetrain.drive(forward, left, turn)` uses it, and `opmodes/teleop/DriverControls.java` is the only place that flips the gamepad stick values (every TeleOp calls it). See the "Directions" section of `TeamCode/docs/code-structure.md`.
Who: Team

## 008 – Drive motor encoder mode  (2026-09-24, updated 2026-10-07)
Status: **Decided in 055** (plain power, `RUN_WITHOUT_ENCODER`, everywhere)
Issue: #11, #25
Context: Our drive motors have encoders, and the hub can run them in two modes:
- `RUN_WITHOUT_ENCODER`: power = % of battery voltage. Full top speed. Speed drops as the battery drains, and wheels can run at slightly different speeds
- `RUN_USING_ENCODER`: power = % of max speed; the hub uses the encoders to hold that speed. More consistent (battery level, wheel to wheel). Needs all four encoder cables working (a loose one can make a wheel spin at full speed)

**Test (2026-10-07, "Encoder Mode Toggle", #11):** the team drove in both modes and **didn't notice a big difference**.

**Findings (2026-10-07):**
- **Top speed:** in `RUN_USING_ENCODER`, full stick = **85%** of the motor's rated top speed (the FTC SDK's default "achievable max" for a motor type). Easy to miss in a casual test
- **Motor type matters:** `RUN_USING_ENCODER` uses the **motor type set in the Driver Station config** to know what "full speed" is. A wrong type (e.g. the generic "GoBILDA 5202/3/4 series", which the SDK treats as a 60 RPM, 99.5:1 motor) can make the robot much slower. See `hardware-config.md`
- **Pedro Pathing never sets the encoder mode** (`com.pedropathing:revhub:3.0.1`, `Mecanum.java`): it uses whatever mode the motors are already in. The SDK starts motors in `RUN_WITHOUT_ENCODER` at power-up, and the mode carries over between OpModes. So **Advanced Auton must set the chosen mode itself** before starting Pedro, and Pedro must be **tuned in that same mode** (#25)

**What the guides say (2026-10-07):**
- **FTC Docs** ("Motor Modes and Encoders" tech tip): `RUN_WITHOUT_ENCODER` "more or less blindly" sets power as a % of battery. `RUN_USING_ENCODER` uses the hub's built-in speed control, so "it's VERY important to set the correct motor type" in the config. Matches our motor-type finding
- **gm0** (Control Loops page): `RUN_USING_ENCODER` turns on the hub's built-in speed control. Encoders can be read in **any** mode. If you use **your own controller** (like Pedro's path follower), gm0 generally recommends `RUN_WITHOUT_ENCODER`. gm0 also says the built-in controller only updates about **20 times per second**
- **FIRST's own mecanum sample** (`RobotTeleopMecanumFieldRelativeDrive.java`, in our repo) uses `RUN_USING_ENCODER` "to be more accurate"
- So: encoder mode is a common choice for **TeleOp**. For **Pedro**, the guides lean toward `RUN_WITHOUT_ENCODER`, because Pedro already corrects speed itself and two controllers can fight

Options:
- **`RUN_WITHOUT_ENCODER`**: full top speed and direct feel, nothing depends on encoder cables, and what path libraries usually expect
- **`RUN_USING_ENCODER`**: team is leaning this way. Lower top speed is fine on a **crowded game floor**, and **consistency** (same speed at any battery level, wheels matched) is appealing for both drivers and autonomous

Decision: **not made yet.** Current code: `USE_ENCODER_SPEED_CONTROL = false` in `Drivetrain.java` (`RUN_WITHOUT_ENCODER`). Question left to discuss: **one mode everywhere** (TeleOp, Basic Auton, Advanced Auton/Pedro, so the robot always behaves the same), or **encoders for TeleOp and Basic Auton, without encoders for Pedro** (what the guides suggest for path followers)? Either way, every OpMode must set its mode on purpose, because the mode carries over between OpModes.
Who: Team (open)

## 009 – Hardware naming rules  (2026-09-25)
Status: Accepted
Context: Config names in the Driver Station must match the code exactly, and we're about to add intake, launcher, odometry and camera hardware. We compared the names used by the FTC SDK samples, Road Runner, Pedro Pathing, goBILDA and gm0.
Options: Every source agrees on one-of-a-kind devices (`imu`, `pinpoint`). Drive motor names are all different (`front_left_drive`, `leftFront`, `lf`, `frontLeftMotor`), and libraries let you change them.
Decision:
- **One-of-a-kind devices use the standard names:** `imu`, `pinpoint`, `webcam` (not the default `Webcam 1`).
- **Drive motors are named by their job: `frontLeftDrive`, `frontRightDrive`, `backLeftDrive`, `backRightDrive`.** No outside standard exists. "Drive" says more than "Motor", because the intake and launcher use motors too. (Renamed from `frontLeftMotor` etc. on 9/25.)
- **Mechanisms:** camelCase, mechanism first, then the part (`intakeMotor`, `launcherFeedServo`).
- **Variables in code use the same name as the config name** (`DcMotor frontLeftDrive`), so one name means one device everywhere.
- Name hardware by its job, not its port. The full rules are in `TeamCode/docs/hardware-config.md`.
Who: Team

## 010 – Team docs live in `TeamCode/docs/`  (2026-09-25)
Status: Accepted
Issue: #12
Context: Our docs started in a root `docs/` folder, right next to FIRST's `doc/` folder (legal text and images). The two names were easy to mix up. Decision 001 already keeps our code in `TeamCode/` so SDK updates merge cleanly.
Options: Keep root `docs/`, rename it (e.g. `WMSTeamDocs/`), or move it to `TeamCode/docs/`.
Decision: **`TeamCode/docs/`.** One simple rule: everything our team makes is in `TeamCode/`. No confusion with FIRST's `doc/` folder, FIRST never adds files there, and the build ignores it (Gradle only compiles `TeamCode/src/`). Restoring FIRST's `README.md` for the same reason is tracked in #12.
Who: Team

## 011 – Field coordinate system for autonomous  (2026-10-05)
Status: Accepted
Issue: #22
Context: Autonomous needs field positions (start spots, LOADING ZONE, paths) for both alliances. The BIOBUZZ field is a **180° rotation** between Red and Blue, not a mirror image (Competition Manual Section 9).
Options: A corner origin, or a center origin. Mirroring Blue from Red, or rotating it.
Decision: **Origin at the center of the field. +X toward the far wall (away from the audience), +Y toward the red wall, heading counter-clockwise (0° = facing +X).** This matches the manual's tile names (A–F, 1–6, seen from the audience) and our robot direction convention (decision 007). **Blue positions are Red's rotated 180°: (x, y, heading) → (−x, −y, heading + 180°)**, so we tune Red's numbers once. Details and tile-center coordinates are in `TeamCode/docs/game/field.md`.
Who: Team

## 012 – Pedro Pathing for Advanced Auton  (2026-10-06)
Status: Accepted (**trial**). Confirm or change after tuning in #25
Issue: #8, #25
Context: Autonomous that scores more than LEAVE + PARK needs smooth, accurate paths. Both popular path-following libraries work with our mecanum drivetrain (decision 004) and goBILDA Pinpoint (decision 005).
Options: Road Runner, Pedro Pathing.
Decision: **Try Pedro Pathing first.** The team chose it after the #8 research. Because it's a trial, we revisit this if installing or tuning it goes badly. Its libraries are added in `TeamCode/build.gradle`, not FIRST's root Gradle files (decision 001).
Who: Team

## 013 – Who controls the drive wheels  (2026-10-06)
Status: Accepted
Issue: #8, #25
Context: Pedro Pathing's `Follower` sets the drive motor powers itself while following a path. We already have `Drivetrain` + `DriverControls` for TeleOp. Two pieces of code controlling the same motors at once would be confusing.
Options: Our `Drivetrain` wraps Pedro, or Pedro controls the wheels in its own OpModes.
Decision: **Split by mode.** TeleOp and Basic Auton drive with our `Drivetrain`. Advanced Auton lets Pedro's `Follower` drive the wheels. **All other subsystems (Intake, Launcher, Odometry) are shared by every mode.** Pedro's setup **reads our constants** (motor names from `Drivetrain`, pod type/offsets/directions from `Odometry`), so every setting lives in one place.
Who: Team

## 014 – Two autonomous tiers: Basic Auton and Advanced Auton  (2026-10-06)
Status: Accepted
Issue: #22, #25
Context: LEAVE + PARK in AUTO is worth 8 points per robot, and with our partner it earns the SWARM ranking point (Competition Manual Table 10-2). Setting up and tuning Pedro Pathing will take time.
Options: Build all autonomous on Pedro, or keep a simple version without it.
Decision: **Basic Auton** (#22) is LEAVE + PARK with our own simple driving code and **no Pedro**: a hedge so we always score the easy points. **Advanced Auton** (#25) uses Pedro to do more. If Advanced Auton becomes reliable enough, it may replace Basic Auton.
Who: Team

## 015 – Camera: Limelight 3A on a servo, one `Camera` subsystem  (2026-10-07)
Status: **Replaced in part by 016** (the camera doesn't tilt; the servo turns the turret). The `limelight` name still stands
Issue: #7
Context: The team is using a **Limelight 3A** camera, aimed by a **servo**. BIOBUZZ AprilTags are on the bottoms of the CELLS, facing down, so the camera must tilt up to see them. Decision 009 had planned a webcam named `webcam`.
Options: Separate subsystems for the camera and the servo, or one subsystem for both.
Decision: **One `Camera` subsystem** (`subsystems/Camera.java`) holds the Limelight and its servo, because aiming the camera only matters for what the camera sees. Config names: **`limelight`** (the name FIRST's Limelight sample uses; one-of-a-kind device rule from 009, replacing `webcam`) and **`cameraServo`** (mechanism, then part). The servo never moves during INIT (G304.H); code calls `lookForward()` / `lookUp()` after START. Like `Odometry`, it is **not in `Robot.java` yet**: once the Limelight and servo are in the Driver Station config and tested with "Camera Test", we add it.
Who: Team

## 016 – Turret: positional servo, ELC encoder, magnetic limit switch  (2026-10-07)
Status: Accepted. **The limit switch part is replaced by 017** (it's a forward indicator, not a stop). **The encoder and camera-aiming parts are replaced by 019**
Issue: #7, #5
Context: The launcher (flywheel) sits on a **turret** that turns left and right, like a tank turret. The **Limelight is mounted on the turret** at a **fixed** up/down angle, so it turns with the turret. AprilTag feedback from the camera aims the turret. Hardware: a **positional servo** turns it, an **ELC Encoder V2** on the servo measures it, and a **REV Magnetic Limit Switch** marks the edge of its travel.
Options for the encoder (it has both outputs): **analog absolute** (0–3.3 V, one turn) into an analog port, or **quadrature** (4000 counts per turn) into a motor encoder port.
Decision:
- **New `Turret` subsystem** (`subsystems/Turret.java`): `turretServo`, `turretEncoder`, `turretLimitSwitch`. `Camera` is now the Limelight only (no servo)
- **Angles follow 007:** 0° = forward, + = left. The camera's `getTargetDegreesLeft()` feeds `turret.turnBy()`; because the camera rides on the turret, no extra math is needed
- **Encoder on the analog port** for now: it knows the turret angle right at power-on (no homing), and the Control Hub's four encoder ports are likely taken by the drive motors. Quadrature is still an option if we need more precision
- **Limit switch = hard stop:** when tripped, the turret only moves back toward forward. Soft limits (`MAX_LEFT/RIGHT_DEGREES`) sit just inside the magnets
- **A light when the switch trips:** decide later (options: the hub's own LED, the gamepad LED/rumble, or an add-on light). `Turret.update()` has the spot for it
- Not in `Robot.java` yet (same reason as `Camera`/`Odometry`)
Who: Team

## 017 – The turret's magnetic switch is a "facing forward" indicator, not a stop  (2026-10-07)
Status: Accepted
Issue: #7
Context: Decision 016 treated the REV Magnetic Limit Switch as a hard stop at the edge of the turret's travel. That was a misunderstanding: the magnet is placed so the switch triggers when the turret is **facing forward**.
Decision:
- Config name **`turretForwardSwitch`** (named by its job, 009), replacing `turretLimitSwitch`. `Turret.isFacingForward()` reads it; it never stops or moves the turret
- Uses: checking and calibrating "forward" (`SERVO_FORWARD_POSITION`, `ENCODER_FORWARD_DEGREES`), and showing drivers when the turret is forward
- Nothing physical stops the turret, so the software limits `MAX_LEFT_DEGREES` / `MAX_RIGHT_DEGREES` are the only protection against turning too far (into wires or the frame). Set them carefully
- The light idea now means "turret is forward". Still to decide which light
Who: Team

## 018 – goBILDA RGB Indicator Light, one `Light` subsystem  (2026-10-07)
Status: Accepted
Issue: #7
Context: We want a light that shows when the turret faces forward (017). We have a **goBILDA RGB Indicator Light**. It plugs into a **servo port**: the "servo position" picks the color.
Options: Put the light inside `Turret`, or make it its own subsystem.
Decision:
- **Own subsystem: `subsystems/Light.java`**, config name **`indicatorLight`** (configured as a Servo). It only knows colors (`Light.Color.GREEN`, `RED`, `OFF`, ...). **The OpMode decides what each color means**, so the same light can show other things later (target locked, pollen loaded)
- First use: **GREEN = turret facing forward** (in "Turret Test"). 10/25: **RED / BLUE = our alliance** during INIT and the match (in "Main TeleOp RED / BLUE", decision 022)
- The light doesn't move anything, so it's **OK during INIT**: drivers can see the turret is forward while setting up for the match
- Color positions come from goBILDA's color chart; check them with "Light Test"
- Not in `Robot.java` yet (same reason as `Camera`/`Turret`/`Odometry`)
Who: Team

## 019 – Turret encoder on an Expansion Hub encoder port, homed by the forward switch; camera is a skeleton  (2026-10-08)
Status: Accepted
Issue: #31, #7
Context: The team wants the ELC Encoder V2's **digital (quadrature)** output, not analog (016). Quadrature signals change thousands of times per turn. The hub's **digital ports** are only read when our code asks, so they would miss counts. FTC reads quadrature only on **motor encoder ports**, and we have an **Expansion Hub** with 4 more. Meanwhile, the other mentor is designing how the camera aims the turret.
Decision:
- **`turretEncoder` plugs into an Expansion Hub motor-encoder port.** In the config, that motor port is set up as a motor named `turretEncoder`, which we only read and never power
- **Homing:** quadrature starts at 0 at power-on. The **forward switch (017) is the home sensor**: `Turret.update()` sets 0° the first time it triggers. Before a match, point the turret forward (the light turns green, 018) and it homes during INIT
- **Camera is a skeleton** (start the Limelight, which tags it sees). **How to aim is not decided yet** (#7). We removed `getTargetDegreesLeft()`, `Turret.turnBy()` and "Turret Aim Test" so we don't guess. **Only the turret moves based on camera data**, never the whole robot
- **The Limelight must use the Control Hub's USB 3.0 port**
Who: Team

## 020 – Basic Auton is drive-only  (2026-10-08)
Status: Accepted
Issue: #22, #25
Context: Decision 014 made Basic Auton LEAVE + PARK. Issue #22 also planned a "Level 2" with launching the preloads.
Decision: **Basic Auton only drives**, to earn the movement points (LEAVE + PARK, and the SWARM ranking point with our partner). **No camera, turret or launcher.** Anything using them goes in **Advanced Auton** (#25) or a later refactor.
Who: Team

## 021 – Turret servo in continuous mode; our code holds the angle  (2026-10-11)
Status: Accepted
Issue: #31
Context: In Turret Test, the positional servo (300° of travel) only turned the turret a little each way. A turret usually needs more travel than a positional servo can give through its gears.
Options: Change the gearing, use a servo with more travel, or switch the servo to **continuous rotation**.
Decision:
- **`turretServo` runs in continuous mode.** The servo is switched with its maker's programmer (code can't do it). Driver Station config type: **Continuous Rotation Servo**. (10/14: an **Axon MAX**, 1050–1950 µs. 10/20: now a **goBILDA 2000 25-3 Speed**, 1000–2000 µs)
- The code sends a **speed**, not a position. **Our code holds the angle** using the ELC encoder (019): `Turret.update()` runs every loop and uses **proportional control** (speed = `AIM_GAIN` × degrees from the target, up to `MAX_SPEED`)
- **Homing is required:** `turnTo()` only works after the forward switch has triggered once. Before that, manual moves are slow (`NOT_HOMED_MAX_SPEED`)
- **Safety:** after homing, nothing moves past `MAX_LEFT/RIGHT_DEGREES`. A **runaway stop** halts the turret if it moves away from its target (a sign that `SERVO_REVERSED` or `ENCODER_REVERSED` is wrong). Nothing moves until the first command (INIT rule)
Who: Team

## 022 – The OpMode picks our alliance; the camera only targets our alliance's tags  (2026-10-24)
Status: Accepted
Issue: #7
Context: BIOBUZZ CELL AprilTags come in two colors: Red 30–37 and Blue 38–45. The camera saw all of them and the Limelight's "primary target" could be the other alliance's tag. In a match we only ever aim at our own color.
Options: (a) Separate Limelight pipelines per alliance with an ID filter (needs Limelight web config, and someone must switch pipelines). (b) Filter in our code using the tag ID ranges we already have.
Decision:
- **(b) Filter in code.** New `Alliance` enum (`RED`, `BLUE`) in `teamcode/`. The **OpMode** tells the camera which alliance we are (`new Camera(hardwareMap, Alliance.RED)` or `camera.setAlliance(...)`). Subsystems never guess the alliance
- `Camera` keeps **seeing** every tag (`getVisibleTagIds()`, telemetry "Other tags (ignored)") but **targets only our color**: `getTargetDegreesLeft()`, `getTargetDegreesUp()`, `getTargetArea()`, `getTargetTagId()`, `seesOurTag()`. If several of our tags are visible, the target is the one with the **biggest area** (closest)
- **No alliance set = every BIOBUZZ tag counts** (plain "Camera Test" on the bench). Match OpModes must always set one
- 10/26: **two match OpModes, "Main TeleOp RED" and "Main TeleOp BLUE"** (shared code in `MainTeleOp`), instead of pressing a button during INIT. Less for the students to remember under pressure; the light shows the color during INIT as a check
- Test with **"Red Camera Test" / "Blue Camera Test"** (`opmodes/test/`, shared code in `AllianceCameraTest`)
- Later: the same `Alliance` picks the autonomous start side (011)
Who: Team

## 023 – TeleOp drives in FLOAT, autonomous in BRAKE  (2026-10-25)
Status: Accepted
Issue: #17
Context: The drive motors' zero-power behavior decides what happens when the sticks are released: **BRAKE** stops the robot quickly, **FLOAT** lets it coast. Students drive in TeleOp and smoother stops are easier to control; autonomous needs to stop exactly where it planned.
Decision:
- **`Drivetrain` defaults to BRAKE** (set in the constructor) and has **`setBrakeOnStop(true/false)`**. Every OpMode gets BRAKE unless it asks for FLOAT
- **"Main TeleOp" calls `setBrakeOnStop(false)` → FLOAT.** Autonomous leaves the default → BRAKE
- Because the constructor sets it every time an OpMode starts, the #17 question (does brake mode carry over between OpModes?) no longer matters for real code
- Telemetry shows "Stop mode" so the driver can see which one is active
Who: Team

## 024 – One naming pattern for hardware: `<subsystem><Job><Type>`  (2026-10-29)
Status: Accepted (adds to 009)
Issue: #4, #5, #31
Context: The robot grew to 8 servos, a flywheel motor, an encoder, a switch and 2 lights. Our names mixed styles: some ended with the device type (`turretServo`), some only said the job (`launchFlywheel`), and prefixes varied (`launch` / `launcher`).
Decision:
- **Pattern: `<subsystem><Job><Type>`.** Start with the subsystem's name, add the job when it helps, and **always end with the device type** (`Motor`, `Servo`, `Encoder`, `Switch`, `Light`)
- **Exceptions:** drive motors keep `<position>Drive` (Pedro uses them), and one-of-a-kind devices keep their standard names (`imu`, `pinpoint`, `limelight`)
- **The full list:**

| Subsystem | Config names |
|---|---|
| Drivetrain | `frontLeftDrive`, `frontRightDrive`, `backLeftDrive`, `backRightDrive` |
| Intake | `intakeServo`, `intakeLoaderServo` (+ 2 micro switches, names TBD) |
| Launcher | `launcherMotor` (the flywheel), `launcherTriggerServo` |
| Turret | `turretRotateServo` (was `turretServo`), `turretEncoder`, `turretForwardSwitch`, `turretHoodServo` |
| FlowerFlipper | `flowerFlipperServo` (new subsystem: shakes back and forth) |
| Light | `leftIndicatorLight`, `rightIndicatorLight` (was one `indicatorLight`) |
| Single devices | `imu`, `pinpoint`, `limelight` |

- The **hood belongs to Turret** (all aiming: left/right and up/down)
- New devices in subsystems that `Robot` creates (Intake, Launcher, FlowerFlipper) and the lights show **OFFLINE** if they're missing from the config, instead of crashing every OpMode (the pattern the team started in `Launcher`)
Who: Team

## 025 – NECTAR detection: two intake switches, read as a plain yes/no  (2026-11-02)
Status: Accepted
Issue: #4
Context: BIOBUZZ has two balls: **POLLEN** (~2.8 in) and the larger **NECTAR** (~3.6 in). Two goBILDA micro limit switches sit on each side of the intake path; only a NECTAR is wide enough to press them. A switch's raw reading is backwards from what students expect (normally open: pressed = **false**).
Decision:
- Config names `intakeNectarLeftSwitch` and `intakeNectarRightSwitch` (Digital Device, decision 024)
- **`intake.isNectarDetected()`** gives a plain true/false: true when **either** switch is pressed. OpModes never read raw switch values
- The wiring is set **once** with an enum, `Intake.NECTAR_SWITCH_WIRING = SwitchWiring.NORMALLY_OPEN` (or `NORMALLY_CLOSED`), and `Intake` does the translation. Checked with Intake Test
- A missing switch counts as "not pressed" and shows OFFLINE (no crash)
Who: Team

## 026 – How Basic Auton is built  (2026-11-02)
Status: Accepted (positions and numbers are a first version, tuned in #22)
Issue: #22
Context: We need the minimum movement for competition: LEAVE (3) + PARK (5) in AUTO, which with our partner earns the SWARM ranking point. Decisions 014 and 020 make Basic Auton drive-only.
Decision:
- **4 OpModes:** Basic Auton RED/BLUE x NEAR/FAR (the proposed plan in #22). NEAR = tile A6 against our alliance wall, next to the LOADING ZONE. FAR = tile B1 against the audience wall, up column B
- **One shared class, `BasicAuton`**; each OpMode only picks the alliance and route (same pattern as Main TeleOp RED/BLUE, decision 022)
- **Positions written once, for RED**, in field coordinates (011). BLUE is calculated: (x, y, h) -> (-x, -y, h + 180)
- **Drives with the Pinpoint**: proportional control to each waypoint (faster when far, slower when close), not timers. Each waypoint has a timeout, and everything stops by 28 s (G403)
- **Start delay** (0-10 s) chosen with the dpad during INIT. **Nothing moves during INIT** (G304.H)
- **Lights:** alliance color in INIT; GREEN = parked, YELLOW = stopped early
- `Odometry.setPose()` added so the Pinpoint reports field positions from the start position
- Uses BRAKE (023). After AUTO, the matching Main TeleOp is preselected (G305)
Who: Team (positions to confirm on the field)

## 027 – Two gamepads: DRIVER and OPERATOR; three driving speeds  (2026-11-03)
Status: Accepted (driver controls). Operator buttons: the students are deciding
Issue: #3
Context: The robot now has many mechanisms (intake, loader, flywheel, trigger, turret, hood, flower flipper). One person driving AND running all of them is a lot to learn.
Decision:
- **Gamepad 1 = DRIVER** (START + A on the Driver Station): only driving, in `DriverControls.java`
- **Gamepad 2 = OPERATOR** (START + B): the mechanisms. The students choose the buttons with the printable sheet `docs/worksheets/controller-layout.pdf`, then we wire them into Main TeleOp
- **Driver:** left stick = drive and strafe, right stick = turn. **Three speeds**: hold either **bumper** = SLOW (40%), nothing = NORMAL (70%), hold either **trigger** = TURBO (100%). If both are held, SLOW wins (safer). The percentages are a first try, tuned with the drivers
- `DriverControls.drive()` returns the speed (`DriveSpeed.SLOW / NORMAL / TURBO`) so TeleOps can show it on telemetry
Who: Team / mentors

## 028 – Team names for the water wheel and the NECTAR detector switches  (2026-11-03)
Status: Accepted (renames devices from 024 and 025)
Issue: #4
Context: The team calls the intake's lifting wheel the **"water wheel"** because of how it looks. The two NECTAR switches sit where they tell us about the **next ball to be launched**, so "intake" in their names was misleading.
Decision:
- `intakeLoaderServo` → **`waterWheelServo`**. Code: `intake.waterWheelUp()`, `waterWheelDown()` (clear a jam), `stopWaterWheel()`
- `intakeNectarLeftSwitch` / `intakeNectarRightSwitch` → **`nectarDetectorLeftSwitch`** / **`nectarDetectorRightSwitch`**
- Still owned by `Intake`, and still end with the device type. They are an **exception** to 024's "start with the subsystem" rule: the team's names for the parts are clearer to us
- The switches work as an **OR**: if either is pressed, **the next ball to launch is a NECTAR** (`intake.isNectarDetected()`, unchanged from 025)
- **Main TeleOp:** both lights turn **purple** (`Light.Color.VIOLET`) while a NECTAR is detected, then back to the alliance color. A first choice; the team can change it
Who: Team

## 029 – The operator's buttons (gamepad 2)  (2026-11-03)
Status: Accepted (a first version: expect changes after driving practice)
Issue: #3
Context: Decision 027 split the work between a DRIVER (gamepad 1) and an OPERATOR (gamepad 2). The team chose the operator's buttons.
Decision:
- **Y (hold) = LAUNCH.** The `launcherTriggerServo` rests in the way, blocking launching; while Y is held it moves out of the way
- **X = water wheel UP, B = water wheel DOWN.** Both are **toggles** (press = on, press again = off), and they cancel each other (B while going up = going down)
- **A = intake** on/off (toggle)
- **Left trigger (hold) = EJECT:** water wheel down + intake out, to push stored balls out. Eject beats the toggles; when it's let go, the intake and water wheel are off
- **The flywheel (`launcherMotor`) runs the whole match**, starting when START is pressed (not during INIT, G304.H). No button
- All in one class, `opmodes/teleop/OperatorControls.java` (like `DriverControls`). It's an object because toggles must remember on/off between loops. It uses the SDK's `xWasPressed()` so a held button toggles only once
- Not on a button yet: flower flipper, turret, hood, auto-aim
Who: Team

## 030 – Operator's left stick turns the turret; Turret joins Robot  (2026-11-03)
Status: Accepted (adds to 029)
Issue: #3, #31
Context: The operator needs to aim by hand until camera auto-aim (#7) is designed. Main TeleOp needs the turret, but `Turret` crashed any OpMode on a robot without its devices configured.
Decision:
- **Gamepad 2 left stick, left / right = turret by hand.** Further = faster, let go = stop. Slow until homed (turn past forward once); stops at its ±90° limits. Stick left = turret left (flipped in `OperatorControls`, decision 007)
- `Turret` now shows **OFFLINE** (no crash) unless all three of `turretRotateServo`, `turretEncoder` and `turretForwardSwitch` are in the config, like the other subsystems
- So **`Turret` is now in `Robot`** (`robot.turret`). Main TeleOp calls `robot.turret.update()` every loop, in INIT too (homing only; nothing moves in INIT)
- **Flower flipper stays off the controller** until we know it's on the build
Who: Team

## 031 – Driver straight-line assist: the left stick snaps to straight lines  (2026-11-03)
Status: Accepted (angle to tune in full tests)
Issue: #67
Context: The team asked for cleaner straight driving. A thumb rarely pushes the stick perfectly straight, so "forward" usually drifts a little sideways.
Decision:
- If the left stick points within **±`SNAP_ANGLE_DEGREES` (15°)** of straight forward, back, left or right, the robot drives **exactly** that way, at the same speed (how far the stick is pushed)
- **Diagonals still work** outside those zones. **Turning (right stick) is not changed**
- **The driver toggles it with A** (gamepad 1). The gamepad rumbles: 1 blip = ON, 2 blips = OFF, so the driver can feel it without looking. Telemetry shows "Straight-line assist (A): ON/OFF". Every TeleOp starts with `ASSIST_ON_AT_START` (true)
- In `DriverControls.java`, so every TeleOp gets it (rule 7). `DriverControls` is now an object (`new DriverControls()`), like `OperatorControls`, so the toggle remembers on/off between loops
- Strafe correction is applied after snapping, so a snapped strafe is still exactly sideways
Who: Team (angle to tune with the drivers)

## 032 – Flywheel runs at a fixed power, not a set velocity  (2026-11-04)
Status: Accepted (revisit if shots fall short late in a match)
Issue: #5
Context: Issue #5 first asked for `DcMotorEx.setVelocity()`, which uses the encoder to hold a target speed as the battery drains. The code was written with `RUN_WITHOUT_ENCODER` and `setPower()` instead, and `isReady()` said "ready" as soon as any power was set, so it never really waited for speed.
Decision:
- **Keep `RUN_WITHOUT_ENCODER` with a fixed power** (`Launcher.DEFAULT_SPIN_POWER`). Simpler to understand and tune, no dependence on the motor type in the Driver Station config, and the flywheel runs the whole match (029) so it has time to settle
- **Trade-off we accept:** at the same power the flywheel spins a bit slower as the battery drains. If shots fall short late in a match, switch to velocity control (and set the exact goBILDA motor type in the config, see 008)
- **`isReady()` means up to speed:** true only when the encoder velocity is at least `READY_VELOCITY_THRESHOLD`. Power alone doesn't count. The threshold is set from "Launcher Test": read the settled velocity at `DEFAULT_SPIN_POWER`, use about 90% of it
- The trigger servo is a **gate**, as in 029: REST blocks the ball path, FIRE opens it. `Launcher.java` and `hardware-config.md` now say so (they used to say it "pushes" a ball)
Who: Mentor (2026-11-04)

## 033 – Turret mounted reversed: the home switch is at the REAR  (2026-11-05)
Status: Accepted (turn limits are placeholders until measured, #31)
Issue: #31
Context: The build had to mount the turret reversed. The magnetic switch that marked "forward" (decision 017) now triggers when the turret faces the **rear** of the robot.
Decision:
- Config name `turretForwardSwitch` → **`turretHomeSwitch`**: it says what the switch does (homing) and stays right if the turret is remounted again
- New **`Turret.HOME_ANGLE_DEGREES = 180`**: when the switch triggers, the turret is at 180 (the rear). Angles still mean the same thing: 0 = robot forward, + = left. Remounting later = change only this number
- Angles **don't wrap**: from the rear, turning left counts up past 180 (to 270), turning right counts down. `turnTo()` picks the way of writing an angle that's inside the limits (`turnTo(-90)` = 270), or the closest limit if it can't get there
- **Turn limits are placeholders: rear ±90** (`MAX_RIGHT_DEGREES = 90`, `MAX_LEFT_DEGREES = 270`): the same 180° of travel as before, centered on the switch, which we know the turret can reach. With these, the turret can't point forward yet. Measure the real range with Turret Test step 4
- Servo and encoder directions should not change (the turret turned around, it didn't flip over), but re-check Turret Test steps 1 and 2
Who: Team / mentor

## 034 – Turret home is 90° LEFT; the turret turns 45° each way from forward  (2026-11-07)
Status: Accepted (limits not tested on the robot yet). Replaces the home angle and the limits in 033 (the rest of 033 still stands)
Issue: #31
Context: With the switch working and the magnet checked, the team found the home switch triggers when the turret faces **90° to the LEFT** of robot forward, not the rear. Forward is 90° to the right of home. The team wants the turret to turn only 45° each way from forward.
Decision:
- **`Turret.HOME_ANGLE_DEGREES = 90`**: the switch spot is 90° (left). Home only tells the code what angle it is at
- **`MAX_LEFT_DEGREES = 45`, `MAX_RIGHT_DEGREES = -45`**: forward ±45°. Home (90) is outside this range on purpose. Limits only apply once homed, so the turret can still reach the switch. After homing it can only turn RIGHT from home (toward forward)
- **`ENCODER_REVS_PER_TURRET_REV = 4.69`**: servo gear 18 teeth, turret gear about 84.5 teeth (counted by hand)
- Earlier on 11/7 the limits were set to 205 / 155 from a test where the turret stopped about 30° from home. That stop was most likely the old placeholder limit plus the turret coasting, not a real hard stop, so those numbers were dropped
- The turret coasts a few degrees past a limit, so check on the robot that ±45° stays clear of the real stops
Who: Team / mentor

## 035 – Intake and water wheel run together; operator can reverse them and send the turret home  (2026-11-07)
Status: Accepted (turret home direction not tested on the robot yet). Changes the X / B / A buttons from 029; the rest of 029 stands
Issue: #3, #4, #31
Context: In testing, the intake and the water wheel are always needed together, so separate buttons for each were awkward. The operator also needs a way to send the turret back to its home spot from wherever it is, and to push balls back out.
Decision:
- **A = intake system on / off (toggle):** the intake and the water wheel run **together**
- **B = direction (toggle), IN or OUT:** IN = intake in + water wheel UP. OUT = intake out + water wheel DOWN, to take balls out. Starts as IN. It works whether the system is on or off
- **Left trigger (hold) = EJECT stays** as a quick push-out (beats the toggles; everything off when let go)
- **X = TURRET HOME (press):** `Turret.goHome()` turns the turret to the home switch (90° left) from wherever it is. If the turret isn't homed yet it doesn't know where home is, so it turns LEFT (home is the left end, 034). **Moving the left stick cancels it**, so the operator can always take over. It gives up after `HOME_SEARCH_TIMEOUT_S` (6 s) or when homed and `HOME_OVERSHOOT_DEGREES` (10°) past home, and telemetry says "HOME NOT FOUND". While going home the ±45° limits are skipped (home is outside them on purpose); those two stops are the safety instead. At the switch it re-sets home from the encoder, which fixes any drift
- **Water wheel up/down on their own are gone** from the buttons. `Intake` still has `waterWheelUp()` / `waterWheelDown()` / `stopWaterWheel()`, and Intake Test still uses them
Who: Team (buttons chosen by the programmer; the students can change them, 029 is "a first version")

## 036 – The X button turns the turret FORWARD, not to the home switch  (2026-11-07)
Status: Accepted (not tested on the robot yet). Replaces the X-button part of 035; the rest of 035 stands
Issue: #3, #31
Context: 035 made X turn the turret to the home switch (90° left). But the switch is only a sensor for the code: the spot the operator wants is **forward**, which is 90° to the RIGHT of the switch.
Decision:
- **X = TURRET FORWARD (press):** `Turret.goForward()` turns the turret to 0° (forward) from wherever it is. Moving the left stick cancels it
- **Already homed:** it turns straight to forward (`turnForward()`), inside the ±45° limits, so the normal limits stay on
- **Not homed yet:** the code doesn't know where forward is, so it first turns **LEFT** slowly (`NOT_HOMED_MAX_SPEED`) to find the home switch (home is the left end, 034), then turns to forward. It gives up after `HOME_SEARCH_TIMEOUT_S` (6 s) and telemetry says "HOME NOT FOUND". Test the search direction on the robot
- `Turret.goHome()` and `HOME_OVERSHOOT_DEGREES` from 035 are removed. After it arrives, the turret holds forward until the stick moves
Who: Team

## 037 – Turret limits: left 90 (the home switch), right -60  (2026-11-07)
Status: Accepted, but **the physical stops must be moved first** (not tested). Replaces the ±45° limits in 034; the rest of 034 stands
Issue: #31
Context: After homing, the team measured the hard stops: **-33.1°** on the right and **+55.9°** on the left, from forward. With the turret mounted sideways, the home switch (90° left) was past the left limit, so the turret could not return to the magnet to home. The team wants to reach the magnet and will move the stops a little.
Decision:
- **`MAX_LEFT_DEGREES = 90`** (the home switch, `HOME_ANGLE_DEGREES`) and **`MAX_RIGHT_DEGREES = -60`**
- **Warning:** both limits are PAST the measured stops (left by 34°, right by 27°). The software limits only protect the turret if they are inside the real stops, so **move the stops before using the full range**. Until then, the turret can drive into a stop and stall the servo or strip the gears: turn it slowly by hand, and use the left trigger, A or the stick only a little at first
- After the stops are moved, measure them again and set the limits a few degrees inside (the turret coasts a few degrees after we stop the servo)
Who: Team

## 038 – Home angle fine-tuned to 88.2°  (2026-11-07)
Status: Accepted (re-check on the robot). Changes the number in 034; the rest of 034 stands
Issue: #31
Context: Turret Test A (`goForward()`) stopped at a reading of 0° but the turret faced about 1.8° too far RIGHT of forward.
Decision: **`HOME_ANGLE_DEGREES = 88.2`** (was 90): the magnet is about 88.2° left of forward. Check it by sending the turret to 45° left and right. If those are also off by about 1.8°, the home angle is right. If they are off by about half of that, the gear ratio (`ENCODER_REVS_PER_TURRET_REV = 4.69`, teeth counted by hand) is slightly off, because an error in the ratio grows with the distance from home
Who: Team

## 039 – The turret's rest spot is 10.5° left of forward  (2026-11-07)
Status: Accepted (re-check on the robot). Replaces the "turns to forward (0°)" part of 036; the rest of 036 stands
Issue: #31
Context: The home switch isn't repeatable to better than about 1°, so chasing 0° exactly wasn't worth it. The team chose a fixed rest spot instead.
Decision: **`Turret.REST_ANGLE_DEGREES = 10.5`** (+ = left of forward). The operator's X button and Turret Test's A button now call **`Turret.goToRestAngle()`** (was `goForward()`), which turns the turret to +10.5°. Homed: straight there. Not homed: turn left to find the switch first, then go there. `turnForward()` (0°) still exists for other uses. `HOME_ANGLE_DEGREES = 88.2` (038) is unchanged
Who: Team

## 040 – The rest spot is dead forward; the code's angle scale is about 10.5° off  (2026-11-07)
Status: Accepted (checked on the robot). Corrects the wording of 039; the number in 039 stands
Issue: #31
Context: With `HOME_ANGLE_DEGREES = 86.2` and `REST_ANGLE_DEGREES = 10.5`, the turret faces **dead forward** (checked 11/7). So 039's "10.5° left of forward" was only the number that works, not the real direction
Decision: **X (operator) and A (Turret Test) turn the turret to dead forward.** In the code's own angle scale that spot is **+10.5**, so the code's **0 is really about 10.5° to the RIGHT of forward**. Home angle (86.2) and rest angle (10.5) were set by testing, not measured, so they also absorb any small gear-ratio error. Anything that needs the TRUE angle (the ±limits, `turnTo(0)`, `turnForward()`, camera aiming later) must remember this: use `REST_ANGLE_DEGREES` for "forward", or fix the angle scale first (home angle and gear ratio) and then set the rest angle back to 0
Who: Team

## 041 – What Main TeleOp does at START  (2026-11-07)
Status: Accepted (not tested on the robot yet)
Issue: #3, #4, #5, #31
Context: The team described how the robot should start a match: trigger blocking balls, a slow flywheel, intake running, turret facing forward, and lights that show what is happening.
Decision: When START is pressed in Main TeleOp:
- **Launcher trigger** goes to REST, so it **blocks balls** (already true). **Flywheel** runs at `Launcher.DEFAULT_SPIN_POWER = 0.45` (40-50%, was 0.8)
- **Intake and water wheel run together** at 65% (`INTAKE_SPEED`, `WATER_WHEEL_SPEED`, were 80%): `OperatorControls.INTAKE_ON_AT_START = true`, direction IN. The operator's A button still turns them off, B switches direction
- **Turret** turns to forward (`goToRestAngle()`, 040) by itself **if it is already homed**. If not, it waits for the operator's X button. `MainTeleOp.TURRET_FIND_HOME_AT_START` (false) can make it find the magnet first, but until the turret's hard stops are moved the left stop is before the magnet, so searching would drive the turret into the stop. Turn it on after the stops move
- **Lights:** the alliance color, as before. **GREEN while the turret is homed and turning to forward**, then back to the alliance color when it arrives. **PURPLE (NECTAR) still wins** over green
- Nothing moves during INIT (G304.H). The turret still homes in INIT if it is pointed at the magnet
Who: Team

## 042 – The turret's rest spot is the home magnet  (2026-11-08)
Status: Accepted (not tested on the robot yet)
Issue: #31
Context: Decisions 039 and 040 made the rest spot dead forward (code angle 10.5). The team wants the rest spot back at the home magnet instead.
Decision: **`Turret.REST_ANGLE_DEGREES = HOME_ANGLE_DEGREES`**. `goToRestAngle()` (operator X, Turret Test A, Main TeleOp start-up) now turns the turret onto the **home magnet**, 90° left of forward. The home search is unchanged: not homed = turn left until the switch triggers, then stop on it. The team also set `HOME_ANGLE_DEGREES = 0` and the limits to `MAX_LEFT_DEGREES = 120` / `MAX_RIGHT_DEGREES = -120` by hand (11/8), so **the magnet is angle 0** and dead forward is about -86 on this scale. Anything written as "0 = forward" (like `turnForward()`, or Turret Test X / B at 45°) now means 0 = the magnet. 039 and 040 stay as history: forward is still reachable with `turnForward()` or the stick.
Who: Team

## 043 – Home on the middle of the magnet  (2026-11-08)
Status: Accepted (not tested on the robot yet)
Issue: #31
Context: Homing at the first switch trigger (the magnet's edge) landed about 2° different from run to run, which moves every turn from the magnet, like ±90°. The edge moves with speed and direction. The middle of the magnet's range is steadier.
Decision: `Turret.CENTER_HOME_ON_MAGNET = true`. The turret still homes at the first trigger so nothing waits. When the switch turns off again (the turret has passed all the way through), it re-homes **once** on `(enter count + leave count) / 2`. `goToRestAngle()` keeps turning left until it is through the magnet, then goes back to the middle. A pass narrower than `MIN_MAGNET_ZONE_COUNTS` is ignored. Turret Test shows the magnet's width. If it is still off, the next ideas are approaching the magnet from the same direction every time (gear backlash) and a slower pass.
Who: Team

## 044 – Home search goes both ways; rest spot is approached the same way every time  (2026-11-08)
Status: Accepted (not tested on the robot yet)
Issue: #31
Context: The home search always turned LEFT. If the turret was already left of the magnet, it drove into the hard stop. The turret also landed a few degrees different from run to run, which fits gear backlash (it arrives a little differently depending on which way it was moving).
Decision: (1) The search starts LEFT. If the encoder stops changing for `SEARCH_STALL_S` (hard stop), it turns around and goes RIGHT; a second stop gives up (HOME NOT FOUND). `HOME_SEARCH_TIMEOUT_S` is 10 s. (2) A move to the rest spot arrives moving the same way the turret was moving when it homed. On the wrong side it first goes `APPROACH_OVERSHOOT_DEGREES` past, then comes back.
Who: Team

## 045 – The turret never aims slower than MIN_AIM_SPEED  (2026-11-08)
Status: Accepted (not tested on the robot yet)
Issue: #31
Context: The turret always stopped 4-5° away from its target (the magnet, or the ±90° moves). Aiming speed was `AIM_GAIN x error`, so near the target it asked for only 0.06-0.1, which is inside the servo's dead zone: the servo stopped moving before it got there.
Decision: `Turret.MIN_AIM_SPEED = 0.15`. While the turret is outside `TOLERANCE_DEGREES`, it never asks for less than this. If it goes PAST the target (it coasts), it stops there instead of chasing back and forth. `isAtTarget()` is true once it has reached or passed the target.
Who: Team

## 046 – Every Basic Auton starts with the robot's BACK against the wall  (2026-11-08)
Status: Accepted (NEAR not tested on the floor yet). Changes the NEAR start in 026; the rest of 026 stands
Issue: #22
Context: NEAR started with the robot's LEFT side on the alliance wall, FAR with its BACK on the audience wall. Students could easily put the back against the wall for NEAR too. Then the robot is turned 90° from what the code tells the Pinpoint, and every move goes the wrong way.
Decision:
- **One rule for every start: BACK against the wall.** NEAR (RED) starts in A6 with its back on the red wall, facing into the field (heading -90): `redPose(60, 72 - ROBOT_LENGTH_IN / 2, -90)`
- NEAR route: LEAVE = drive straight forward off the wall to (60, 50); PARK = slide toward the audience to (40, 60), still facing into the field (no turning). That is partly in the LOADING ZONE (PARK) and about 3 in off the wall (LEAVE). BLUE is still RED rotated 180°. FAR is unchanged (it was already back to the wall)
- The **INIT screen shows a plain "SET UP:" line** for each program (tile, which wall the back touches, which way the front faces), so the drive team doesn't need the route sheet to place the robot
Who: Mentor (11/8)

## 047 – Turret angles: 0 = the magnet, robot forward = -90, limits ±135 from forward  (2026-11-08)
Status: Accepted (limits not tested on the robot yet). Settles the angle scale from 042 and replaces its limits
Issue: #31
Context: The turret code was first written with 0 = robot forward and the magnet in front. Then the magnet moved 90° left, and 042 made the magnet angle 0, but some comments and `turnForward()` still assumed 0 = forward. Future features (camera aiming, field aiming in Advanced Auton) think in "degrees from robot forward", so a hidden 90° offset would be an easy bug.
Decision:
- **Turret angle 0 = the home magnet** (team choice). + = left (decision 007)
- **Robot forward = `Turret.FORWARD_ANGLE_DEGREES = -90`**: the magnet is 90° left of forward. This constant is the **only** place the offset lives; a remount changes only it
- **Helpers:** `Turret.robotToTurret(degreesFromForward)` and `Turret.turretToRobot(turretDegrees)`. Anything measured from the robot's front (the camera, field aiming, autonomous) uses them, never a hand-added 90
- **Limits: ±135° from robot forward** (team, for the wiring): `MAX_LEFT_DEGREES = 45`, `MAX_RIGHT_DEGREES = -225`. Replaces 042's ±120 from the magnet
- `turnForward()` now really points forward (-90). Turret Test X / B turn to 45° left / right of forward (90° left of the magnet would now be past the left limit)
- The rest spot (the magnet), homing (043, 044) and minimum aim speed (045) are unchanged
Who: Team / mentor (11/8)

## 048 – Correction: at the magnet the turret faces FORWARD (no 90° offset)  (2026-11-08)
Status: Accepted. Corrects the forward angle and limits in 047; the rest of 047 stands
Issue: #31
Context: 047 put robot forward at turret angle -90, because the magnet sits 90° to the left of the robot's front. But at the home switch the turret (launcher) itself faces forward. Turret Test showed it: X / B aimed 90° off, and the turret could turn past the intended limits.
Decision:
- **`FORWARD_ANGLE_DEGREES = 0`**: magnet = turret angle 0 = robot forward. No offset
- **Limits ±135 from forward = `MAX_LEFT_DEGREES = 135`, `MAX_RIGHT_DEGREES = -135`**
- Keep `robotToTurret()` / `turretToRobot()` and the constant: today they change nothing, but a future remount only changes `FORWARD_ANGLE_DEGREES`
Who: Team / mentor (11/8)

## 049 – Main TeleOp drives in BRAKE  (2026-11-08)
Status: Accepted (not tested on the robot yet)
Issue: #17
Context: Decision 023 had Main TeleOp drive in FLOAT so the robot coasts when the sticks are released. The team wants the robot to stop quickly instead.
Decision: **Main TeleOp calls `setBrakeOnStop(true)` → BRAKE**, the same as Autonomous. 023 stays as history. To go back to coasting, change that one line in `MainTeleOp.java` to `false`. Telemetry still shows "Stop mode".
Who: Team

## 050 – The turret does not move in Main TeleOp (for now)  (2026-11-08)
Status: Accepted (not tested on the robot yet)
Issue: #31
Context: The turret isn't ready to use in matches yet (homing and aiming are still being tuned), so the team wants it switched off in Main TeleOp.
Decision: **`OperatorControls.TURRET_ENABLED = false`.** The operator's left stick and X do nothing, and Main TeleOp does not send the turret to its rest spot at START. The turret is never given a command, so it does not move. The turret code, Turret Test and the other mechanisms are unchanged. Set it to `true` to turn the controls back on.
Who: Team

## 051 – The code is the source of truth for tuned values  (2026-11-09)
Status: Accepted
Issue: —
Context: As the team tests and calibrates on the robot, values in the code change quickly (speeds, positions, limits). Docs and earlier decisions fall behind, e.g. decision 041 said intake 0.65 and flywheel 40-50%, while testing moved both to 1.0.
Decision:
- **The value in the code wins.** A number in a doc, worksheet or decision is history, not a target. Don't change code to match an older doc
- **Docs follow the code:** `check_tuning_guide.py` checks the tuning guide; the worksheet scripts read current values from the code (`docs/worksheets/code_values.py`). Re-run them before printing
- A note in the code next to a changed value says when and why (e.g. "Note 11/8: 1.0 after testing")
- Bigger design choices (how something works, not a number) still get a decision
Who: Mentor (11/9)

## 052 – Test and archive OpModes are hidden on the Driver Station  (2026-11-10)
Status: Accepted
Issue: —
Context: The Driver Station list had the competition programs mixed in with 11 test programs and old archive programs. At a competition the drive team should only see what they need.
Decision:
- **Every OpMode in `opmodes/test/` and `opmodes/archive/` has `@Disabled`** (the FTC SDK's way to hide an OpMode). The list shows only **Main TeleOp RED / BLUE** and the **4 Basic Autons**
- **To use a test:** delete its `@Disabled` line, build and deploy. Put it back before a competition. Each file has a one-line comment saying so, and so do the printed worksheets
- `ExampleTeleOp` and `HelloWorld` are **deleted**. The example OpMode pattern now lives in `code-structure.md` ("Adding a new OpMode")
Who: Mentor (11/10)

## 053 – Basic Auton keeps field coordinates; Test Auton runs the same moves from (0, 0, 0)  (2026-11-11)
Status: Accepted
Issue: #22
Context: While tuning on the floor (11/10-11/11) the NEAR route was hand-edited with its start at heading -90 and waypoints written as if +X were forward. The Pinpoint's frame is set by the start pose, so with heading -90 its +X is the robot's LEFT: the robot strafed 23 in instead of driving forward. The origin never mattered; the start heading did. The question was whether to keep field waypoints in Basic Auton at all.
Options: (a) Keep field coordinates (011) in Basic Auton and convert each measured move into a field position. (b) Drive every route relative to a (0, 0, 0) start, same moves for both alliances.
Decision: **(a).** Basic Auton keeps the real field start (`RED_NEAR_START`, `RED_FAR_START`) and field waypoints (`RED_NEAR_PATH`, `RED_FAR_PATH`), BLUE rotated 180° as in 011, so the robot's position on the field means something (telemetry, route sheets, later Advanced Auton). Each waypoint comment says the move in robot words ("23 in forward", "12 in right") and the field numbers do the math; the header warns that the start heading decides which field direction is forward. **Test Auton** (`opmodes/test/testAuton.java`) overrides the start to (0, 0, 0) and holds the SAME moves as forward / left distances, so the routes can be repeated on any clear floor with no setup error. When a waypoint changes in one file, change it in the other by hand.
Who: Mentor (11/11)

## 054 – Timed Auton: a no-sensor backup for Basic Auton NEAR  (2026-11-12)
Status: Accepted
Issue: #22
Context: Two days before the 11/14 competition, Basic Auton NEAR was still having movement problems on the floor (overshoot, a strafe instead of the first forward move). It depends on the Pinpoint, the gyro calibration and (until now) the motor encoders. If any of those misbehaves at a match, we have no AUTO.
Decision:
- **Add a backup, `Timed Auton RED NEAR (backup)` / `BLUE NEAR (backup)`** (`opmodes/auto/TimedAuton.java`): the same box as Basic Auton NEAR (forward, right, back), but **time-based**: each move runs the wheels at `DRIVE_POWER` for a set number of seconds. **No Pinpoint, no encoders (plain power), no telemetry.**
- RED and BLUE drive the same robot moves (BLUE is RED rotated 180°), so the two OpModes share all the code and only differ in name and the TeleOp they queue up.
- Same setup as Basic Auton NEAR: BACK touching our wall, FRONT facing into the field (046).
- **Basic Auton stays the plan.** Use the backup only if Basic Auton isn't reliable at the competition. The times are tuned on the robot with a charged battery.
- The start delay is a constant (`START_DELAY_S`), not a dpad setting: there is no screen to show it.
Who: Mentor (11/12)

## 055 – Drive motors run on plain power (RUN_WITHOUT_ENCODER) everywhere  (2026-11-11)
Status: Accepted (closes 008)
Issue: #11, #22
Context: Decision 008 left the drive motor mode open. Floor tests of Basic Auton on 11/10-11/11 showed the robot lurching back and forth at waypoints: with `RUN_USING_ENCODER` the hub's own speed loop sits between our code and the wheels, and at the tiny speed targets used near a waypoint it winds up against friction, breaks free in a surge, and our Pinpoint-based controller sees a robot that jumps. Encoder mode also depends on the exact motor type set in the Driver Station config, and a loose encoder cable makes one wheel run at full speed.
Options: Encoders everywhere; plain power everywhere; a mix per OpMode.
Decision: **Plain power everywhere.** `Drivetrain.USE_ENCODER_SPEED_CONTROL = false`, so every OpMode starts in `RUN_WITHOUT_ENCODER`; Basic Auton and Pinpoint Test also set it explicitly. Power means "% of battery voltage". One mode for TeleOp, Basic Auton and (later) Pedro, so the robot always behaves the same and Pedro is tuned in the mode it runs in (008's finding). The Pinpoint closes the position loop in autonomous, so a second speed loop in the hub isn't needed. Trade-off: speed drops a little as the battery drains (swap batteries between matches, `Power` shows the voltage). `Drivetrain.setEncoderSpeedControl()` stays, so an OpMode can switch if a test needs it. The Basic Auton powers were reset for plain power (`MIN_DRIVE_POWER` 0.12, `MAX_DRIVE_POWER` 0.3, `MAX_SIDEWAYS_POWER` 0.25, `MIN_TURN_POWER` 0.12) and must be re-tuned on the floor.
Who: Mentor (11/11)

## 056 – The water wheel is a motor: `waterWheelMotor`  (2026-11-11)
Status: Accepted
Issue: #4
Context: The water wheel (the intake's lifting wheel, decision 028) was driven by a continuous rotation servo. The team swapped it for a **goBILDA 5203-2402-0051 Yellow Jacket motor** (117 RPM, 50.9:1) for more torque and speed.
Decision: The config name follows the naming rule's type suffix (024): **`waterWheelMotor`** (⚠️ rename it in the Driver Station config and pick the exact goBILDA 5203 type). `Intake` drives it as a `DcMotor` on plain power (`RUN_WITHOUT_ENCODER`, like every motor, 055) with **BRAKE** when stopped, so the weight of the balls can't turn the wheel back. The methods (`waterWheelUp()`, `waterWheelDown()`, `runWaterWheel()`, `stopWaterWheel()`) and `WATER_WHEEL_SPEED` / `WATER_WHEEL_REVERSED` keep their names, so no OpMode changed. Both values must be re-tuned with Intake Test: the direction has not been checked on the motor, and 1.0 on a 117 RPM motor is about twice the old servo's speed. Expansion Hub servo 1 is free; the motor's port is written in `hardware-config.md` once wired.
Who: Mentor (11/11)

## 057 – Timed Auton FAR: the backup for Basic Auton FAR too  (2026-11-11)
Status: Accepted
Issue: #22
Context: Decision 054 added a time-based backup for Basic Auton NEAR only. If the Pinpoint or the encoders misbehave when we start FAR, we would still have no AUTO.
Decision: Add **Timed Auton RED FAR (backup)** / **BLUE FAR (backup)**, in the same `TimedAuton.java`. They drive the same moves as Basic Auton FAR and Test Auton FAR: **forward 100 in** up column B (LEAVE), then **left 20 in** into the LOADING ZONE (PARK), each at `DRIVE_POWER` for a set time (`FAR_FORWARD_S`, `FAR_LEFT_S`). Same setup as Basic Auton FAR. Each OpMode file picks NEAR or FAR with `route()`, like Basic Auton (it reuses `BasicAuton.Route`). Everything else from 054 stays: no sensors, no telemetry, Basic Auton stays the plan, times tuned on the robot with a charged battery.
Who: Mentor (11/11)

## 058 – Hide Basic Auton for now; run the Timed Auton backups  (2026-11-11)
Status: Accepted
Issue: #22
Context: Now that Timed Auton covers NEAR and FAR for both alliances (054, 057), there is a no-sensor AUTO for every start. Basic Auton was still having movement problems on the floor.
Decision: Put `@Disabled` on the four Basic Auton OpModes (`BasicAutonRedNear`, `BasicAutonBlueNear`, `BasicAutonRedFar`, `BasicAutonBlueFar`), so only the **Timed Auton** OpModes show in AUTO on the Driver Station. This changes 054's "Basic Auton stays the plan" for now. The code stays as it is (`BasicAuton.java`, Test Auton still use it); to bring one back, delete its `@Disabled` line.
Who: Mentor (11/11)

## 059 – NECTARs launch by themselves in Main TeleOp  (2026-11-11)
Status: Accepted
Issue: #4, #5
Context: The nectar detector switches (028) already tell us when the next ball to launch is a NECTAR (lights turn purple). The operator then has to hold Y to open the launcher trigger. The mentor asked for NECTARs to shoot automatically.
Decision: In `OperatorControls` (Main TeleOp), when either nectar detector switch is pressed, the launcher trigger opens (`fire()`) with no button. It stays open `AUTO_FIRE_HOLD_S` (0.5 s) after the switch lets go, because the ball moves off the switch as soon as the gate opens, and closing right then could clip it. Y still opens the trigger by hand (for POLLEN). Not while ejecting. No wait for "flywheel READY": the flywheel runs the whole match, and `READY_VELOCITY_THRESHOLD` is still a placeholder, so waiting on it could stop NECTARs from launching at all. `AUTO_FIRE_NECTAR = false` turns it off. Intake + Launcher Test does the same, to try it on the bench.
Who: Mentor (11/11)

## 060 – AUTO game plan: Timed Auton for NEAR, Main Auton for FAR; a short Driver Station list  (2026-11-12)
Status: Accepted
Issue: #22
Context: Test Auton (053) drives the moves from a (0, 0, 0) start, and the team tuned those moves on the floor until they worked. Timed Auton NEAR already works on the robot. Basic Auton (field coordinates) is hidden (058). Before a competition the Driver Station should only list the OpModes the drive team needs.
Decision: **NEAR = Timed Auton RED / BLUE NEAR. FAR = Main Auton RED / BLUE FAR.** Test Auton moves from `opmodes/test/` to `opmodes/auto/` and is renamed **Main Auton** (`MainAuton.java` + `MainAutonRedNear` / `RedFar` / `BlueNear` / `BlueFar`). It was RED only; now there is a BLUE of each. RED and BLUE drive the same robot moves (the field is rotated 180°, like Timed Auton); the alliance picks the lights, the SET UP line (the real field setup, back touching a wall, 046) and the Main TeleOp that comes next. The four Basic Auton OpModes move to `opmodes/archive/` (still `@Disabled`); `BasicAuton.java` stays in `auto/` because Main Auton and Timed Auton use it. The Driver Station shows only: **AUTO** = Main Auton RED / BLUE NEAR / FAR and Timed Auton RED / BLUE NEAR / FAR; **TeleOp** = Main TeleOp RED / BLUE and Intake Test. Brake Mode Check, Pinpoint Test and Intake + Launcher Test get `@Disabled` (052).
Who: Mentor (11/12)

## 061 – The operator's B button does nothing in Main TeleOp for now  (2026-11-12)
Status: Accepted
Issue: #4
Context: B flipped the intake + water wheel to OUT (decision 041). Pressed by accident, the robot pushes balls out until B is pressed again. The left trigger (EJECT) already pushes balls out while held.
Decision: New `OperatorControls.DIRECTION_TOGGLE_ENABLED = false`: B does nothing and the direction is always IN. Hold the left trigger to push balls out. Set it `true` to bring the B toggle back. Controller layout updated.
Who: Mentor (11/12)

## 062 – Main TeleOp: the intake + water wheel start OFF; A starts and stops them  (2026-11-12)
Status: Accepted
Issue: #4
Context: Decision 041 started the intake + water wheel ON at START. The team wants the operator to choose when they start.
Decision: `OperatorControls.INTAKE_ON_AT_START = false`. At START the intake + water wheel are OFF. **A** starts them, A again stops them. They always run together the shooting way (intake IN + water wheel UP), since B is off (061). **L2 (left trigger)** held = EJECT: both run the opposite way (intake OUT + water wheel DOWN); let go = both OFF. This changes 041's "intake ON at START".
Who: Mentor (11/12)
