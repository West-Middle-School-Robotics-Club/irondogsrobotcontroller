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
Status: **Open**. The team is still discussing this in #11
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
- First use: **GREEN = turret facing forward** (in "Turret Test")
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
