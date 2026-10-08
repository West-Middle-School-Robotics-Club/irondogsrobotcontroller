# WMS Irondogs – FTC BIOBUZZ (2026–27)

Our team's fork of FIRST's FtcRobotController (SDK v12.0). Middle school team; code is written for students to read and learn from.

## Read first
- `TeamCode/docs/decisions.md`: every team decision and why (numbered; add new ones, never rewrite accepted ones)
- `TeamCode/docs/code-structure.md`: folder layout, the code rules, direction convention
- `TeamCode/docs/hardware-config.md`: Driver Station config names and naming rules
- `TeamCode/docs/game/`: BIOBUZZ field, scoring and rules summaries (from the Competition Manual, with page numbers)
- `TeamCode/docs/changes-made.md`: running change log (add an entry for each change)
- `TeamCode/docs/portfolio/`: engineering portfolio notes, written as we go
- GitHub issues track all work; decisions in progress are discussed there

## Key rules (details in the docs above)
- **All team work lives in `TeamCode/`** (decisions 001, 010). Don't edit `FtcRobotController/` or FIRST's root `README.md`, so SDK updates merge cleanly
- **Structured subsystems** (006): only `subsystems/` touch hardware; OpModes start with `new Robot(hardwareMap)`
- **Directions** (007): +X forward, +Y left, +heading counter-clockwise. Only `opmodes/teleop/DriverControls.java` reads/flips gamepad sticks
- **Field coordinates** (011): origin at field center, +X → far wall, +Y → red wall. Blue = Red rotated 180°
- **Autonomous** (012–014, 020): **Basic Auton** = LEAVE + PARK, **drive-only** (no Pedro, camera, turret or launcher; a hedge). **Advanced Auton** = Pedro Pathing (trial), where Pedro's `Follower` drives the wheels. TeleOp uses our `Drivetrain`. All other subsystems are shared; Pedro reads our constants
- **Naming** (009): config name = variable name (`frontLeftDrive`); single devices `imu`, `pinpoint`, `limelight`. Limelight (USB 3.0 port) on a turret: `subsystems/Camera.java` (skeleton, #7; aiming not designed yet), `subsystems/Turret.java` (#31). Turret angles: 0 = forward, + = left. Only the turret moves on camera data. `turretForwardSwitch` is the turret's home sensor, not a stop; `turretEncoder` is quadrature on an Expansion Hub encoder port (019). `Light.java` (018) only knows colors; OpModes decide what they mean
- **Test code stays in `opmodes/test/`** (code-structure rule 8): real code never uses it, no test-only methods in subsystems
- **INIT must not move anything** (game rule G304.H / G403)

## Workflow
- Work on a branch and open a PR; teammates merge after testing on the robot
- Gradle/build file versions: under discussion with the mentors, don't change them
- Explain things simply; students and mentors read the PRs, issues and docs
