# How Our Code Is Organized

Everything our team makes lives in `TeamCode/` (decisions 001 and 010):
- **Code:** `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`
- **Docs:** `TeamCode/docs/` (this file, `decisions.md`, `hardware-config.md`, `changes-made.md`, `portfolio/`, and `game/`: the BIOBUZZ rules summary)
We use **structured subsystems** (decision 006).

```
teamcode/
├── Robot.java              ← holds every subsystem
├── subsystems/             ← one class per mechanism; the ONLY code that touches hardware
│   ├── Camera.java         ← the Limelight. Not in Robot.java yet; see below
│   ├── Drivetrain.java
│   ├── Intake.java
│   ├── Launcher.java
│   ├── Light.java          ← the goBILDA RGB light. Not in Robot.java yet; see below
│   ├── Odometry.java       ← the Pinpoint (X, Y, heading). Not in Robot.java yet; see below
│   └── Turret.java         ← turns the launcher + camera left/right. Not in Robot.java yet; see below
└── opmodes/                ← what shows up on the Driver Station
    ├── teleop/
    │   ├── DriverControls.java ← the ONLY code that turns gamepad sticks into drive commands
    │   └── ExampleTeleOp.java
    ├── auto/
    └── test/               ← test and tuning OpModes (the "Tests" group on the Driver Station)
        ├── BrakeModeCheck.java
        ├── CameraTest.java ← check the Limelight sees AprilTags (#7)
        ├── EncoderModeToggle.java ← drive and switch encoder mode with Y (#11)
        ├── LightTest.java ← check the light's colors
        ├── PinpointTest.java
        ├── TestDriveMotors.java ← test-only helper; never used by real robot code
        └── TurretTest.java ← turn the turret with the dpad, learn the ELC encoder, calibrate (#31)
```

`Odometry`, `Camera`, `Turret` and `Light` are created directly by the OpModes that need them, not by `Robot.java` yet. If a device
is missing from the Driver Station config, `new Robot(hardwareMap)` would crash **every** OpMode. Once the Pinpoint
(issue #6) the Limelight (issue #7), and the turret and light (issue #31) are configured and tested on the robot, we'll add them
to `Robot.java` like the other subsystems.

## The rules

1. **Only subsystems touch hardware.** Motors, servos and sensors are `private` inside their subsystem.
   OpModes call methods like `robot.drivetrain.drive(...)` or `robot.intake.in()`, never `motor.setPower(...)`.
2. **Every OpMode starts with `Robot robot = new Robot(hardwareMap);`.** See `ExampleTeleOp.java`.
3. **No magic numbers.** Config names, speeds and positions are named constants at the top of the subsystem,
   e.g. `FRONT_LEFT_DRIVE_NAME = "frontLeftDrive"`.
   The variable holding the device uses the same name as the config: `frontLeftDrive`.
4. **Every subsystem has `stop()` and `addTelemetry(telemetry)`**, so `Robot` can stop everything
   and show everything with one call.
5. **Config names match `TeamCode/docs/hardware-config.md`.**
6. **Subsystems never read the gamepad.** The same subsystem is used by TeleOp *and* Autonomous;
   only the OpMode knows where commands come from (sticks in TeleOp, code in Autonomous).
7. **Every TeleOp drives with `DriverControls.drive(robot.drivetrain, gamepad1);`.** Don't read the drive
   sticks anywhere else. Autonomous skips `DriverControls` and calls `robot.drivetrain.drive(...)` directly.
8. **Test code stays in `opmodes/test/`.** Test and tuning OpModes (and their helpers) may break rules 1–7
   when a test needs to, for example by reading or changing motor settings directly. Real robot code
   (subsystems, `opmodes/teleop/`, `opmodes/auto/`) **never** uses anything from `opmodes/test/`, and we never
   add test-only methods to subsystems. That way test code can be deleted without touching real code.

## Directions (decision 007)

All of our code uses the same directions as the Pinpoint, Road Runner, Pedro Pathing and the FTC field:

| | Positive (+) means | Pinpoint value |
|---|---|---|
| **X / forward** | forward | `getPosX()` |
| **Y / left** | **left** | `getPosY()` |
| **Heading / turn** | **counter-clockwise** (turning left) | `getHeading()` |

The gamepad sticks are different (stick right = +X, stick forward = −Y).
**Only `DriverControls` flips the stick values.** Everything else, including subsystems and autonomous, uses the table above.

```
TeleOp:  gamepad → DriverControls → drivetrain.drive(forward, left, turn) → motors
Auto:    Pinpoint / path code ───→ drivetrain.drive(forward, left, turn) → motors
```

## Adding a new mechanism

1. Create `subsystems/YourThing.java`. Copy the shape of `Intake.java`.
2. Add its config names to `TeamCode/docs/hardware-config.md`.
3. Add it to `Robot.java` in the field, the constructor, `stop()` and `addTelemetry()`.
4. Call its methods from an OpMode.

## Adding a new OpMode

1. Copy `opmodes/teleop/ExampleTeleOp.java` into `opmodes/teleop/` or `opmodes/auto/`.
2. Change the class name, and the `name` in `@TeleOp(...)` (or use `@Autonomous(...)`).
3. Build and deploy, then pick it on the Driver Station.
