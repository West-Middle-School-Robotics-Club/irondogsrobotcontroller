# Hardware Configuration

The names in the **Driver Station robot configuration** must match the names in our code **exactly**, including capital letters.
If a name doesn't match, the OpMode crashes on INIT with an error like `Unable to find a hardware device with name "frontLeftDrive"`.

**Changing a name?** Update all three places: the Driver Station config, the constant in the subsystem class, and this table.

## Naming rules (decision 009)

| Kind of hardware | Rule | Examples |
|---|---|---|
| **One-of-a-kind devices** | Use the standard name that the SDK samples, Road Runner and Pedro Pathing all use | `imu`, `pinpoint`, `limelight` |
| **Drive motors** | `<position>Drive`: named by their job, since other mechanisms use motors too | `frontLeftDrive`, `frontRightDrive`, `backLeftDrive`, `backRightDrive` |
| **Mechanisms** | camelCase, mechanism first, then the part | `intakeMotor`, `launcherMotor`, `launcherFeedServo`, `intakeColorSensor` |

- Name hardware by its **job**, not its port (`intakeMotor`, not `motor2`).
- The **variable in code uses the same name** as the config name: `DcMotor frontLeftDrive = hardwareMap.get(DcMotor.class, "frontLeftDrive");`
- No spaces. Names are **case-sensitive**.
- Label the physical wires with the same name.

## Drivetrain (`subsystems/Drivetrain.java`)

| Config name | Device type | Hub / Port | Direction | Notes |
|---|---|---|---|---|
| `frontLeftDrive`  | Motor | Control Hub / motor __ | REVERSE | Left side reversed (tested 9/23) |
| `backLeftDrive`   | Motor | Control Hub / motor __ | REVERSE | |
| `frontRightDrive` | Motor | Control Hub / motor __ | FORWARD | |
| `backRightDrive`  | Motor | Control Hub / motor __ | FORWARD | |

⚠️ **Set the correct motor type** for each drive motor in the Driver Station config (the exact goBILDA model / gear ratio, not a generic type).
In encoder speed control mode (`RUN_USING_ENCODER`, decision 008), the SDK uses the configured motor type to know the motor's top speed.
A wrong type can make the robot much slower. Drive motor model / ratio: ______

## Hubs

| Hub | Notes |
|---|---|
| Control Hub | Drive motors (all 4 motor ports), Limelight (USB 3.0), Pinpoint (I2C) |
| Expansion Hub | Flywheel and intake motors (#5, #4), `turretEncoder` (motor-encoder port, decision 019). Connected by: ___ (RS485 cable) |

Each motor port has a matching **encoder port** with the same number. Ports on the Expansion Hub count from 0 again.

## IMU

| Config name | Device type | Hub / Port | Notes |
|---|---|---|---|
| `imu` | Control Hub built-in IMU | Control Hub / I2C bus 0 | Default name. Heading for driving comes from the Pinpoint (decision 005) |

## Odometry (issue #6)

| Config name | Device type | Hub / Port | Notes |
|---|---|---|---|
| `pinpoint` | goBILDA Pinpoint Odometry Computer | Control Hub / I2C bus __ | `subsystems/Odometry.java`. Pod type, offsets and directions are tuned with the Pinpoint Test OpMode (#6) |

## Intake (`subsystems/Intake.java`, issue #4)

| Config name | Device type | Hub / Port | Notes |
|---|---|---|---|
| ___ | ___ | ___ | Waiting on build team |

## Launcher (`subsystems/Launcher.java`, issue #5)

| Config name | Device type | Hub / Port | Notes |
|---|---|---|---|
| ___ | ___ | ___ | Waiting on build team |

## Camera (`subsystems/Camera.java`, issue #7, decisions 015, 016)

| Config name | Device type | Hub / Port | Notes |
|---|---|---|---|
| `limelight` | Limelight 3A | Control Hub / **USB 3.0 port** | ⚠️ **Must use the port labeled USB 3.0.** Mounted on the turret, fixed tilt angle: ___° up |

## Turret (`subsystems/Turret.java`, issue #31, decisions 016, 017, 019)

| Config name | Device type | Hub / Port | Notes |
|---|---|---|---|
| `turretServo` | Servo | ___ / servo port ___ | **Temporary:** goBILDA 2000 Series Dual Mode Servo, 25-2 Torque (**2000-0025-0002**), standard (positional) mode, **300°**. Code sets its signal range to 500–2500 µs (`Turret.SERVO_PWM_MIN_US/MAX_US`). Will be upgraded later: recheck those constants then |
| `turretEncoder` | Motor (encoder only, never powered) | **Expansion Hub** / motor-encoder port ___ | ELC Encoder V2, **quadrature** output (decision 019): use the encoder's **"D" connector** (not "A" = analog). Configure the motor port with this name; the code only reads its encoder. Data sheet: 3.3–3.6 V (the hub's encoder port gives 3.3 V ✅), JST-PH 4-pin (same as the hub ✅), 4000 counts per revolution. ⚠️ Pin order isn't on the data sheet: use the cable that came with it and check "Encoder counts" changes when you turn it |
| `turretForwardSwitch` | Digital Device | ___ / digital port ___ | REV Magnetic Limit Switch: triggers when the turret faces **forward** (indicator, not a stop, decision 017). Digital ports have 2 channels (n, n+1): if it never changes, try the other channel number |

## Light (`subsystems/Light.java`, issue #31, decision 018)

| Config name | Device type | Hub / Port | Notes |
|---|---|---|---|
| `indicatorLight` | Servo | ___ / servo port ___ | goBILDA RGB Indicator Light. The "servo position" picks the color (see `Light.Color`) |
