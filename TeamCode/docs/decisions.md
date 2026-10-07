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

## 008 – Drive motors run without encoder speed control  (2026-09-24)
Status: **Proposed**. Not final until the team decides in #11
Issue: #11
Context: Our drive motors have encoders. In `RUN_USING_ENCODER` mode the hub holds each wheel at a set speed, which means the same speed on a low battery and straighter driving. But it needs all four encoder cables working (a loose one can make a wheel spin at full speed), and it lowers top speed slightly.
Options: `RUN_WITHOUT_ENCODER` (power = % of battery voltage) or `RUN_USING_ENCODER` (power = % of max speed).
Decision (proposed): **`RUN_WITHOUT_ENCODER`**, for now:
- **Reliable:** nothing depends on encoder cables, so a loose one can't make a wheel go wild mid-match.
- **Full speed and direct feel:** all motor power goes to the wheels, and drivers already practice with this mode (`manualDriver`).
- **Precision comes from the Pinpoint** (decision 005), which corrects for battery level and wheel differences in autonomous.
- **Road Runner and Pedro Pathing normally expect this mode** (#8).

The trade-off is that speed drops as the battery drains and the robot may pull slightly to one side; in TeleOp the driver corrects for that. Revisit after the on-robot comparison in #11 and after #8 is decided.
Who: Team (proposed; to be confirmed)

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

