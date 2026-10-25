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

| Config name | Device type | Hub / Port | Direction | Notes                     |
|---|---|---|---|---------------------------|
| `frontLeftDrive`  | Motor | Control Hub / motor 0 | REVERSE | Left side reversed (tested 9/23) |
| `backLeftDrive`   | Motor | Control Hub / motor 1 | REVERSE |                           |
| `frontRightDrive` | Motor | Expansion Hub / motor 0 | FORWARD | Bench bot (10/24)     |
| `backRightDrive`  | Motor | Expansion Hub / motor 1 | FORWARD | Bench bot (10/24) |

Randy suggests keeping 2 motors per hub (left on control hub, and right on expansion hub) since the hubs have current limits, so this is just to be safe. 
Motor cabling (goBILDA): the motor has **3.5 mm bullet** power plugs → goBILDA adaptor **3801-0613-0100** (2-Pos JST VH to 3.5 mm bullet) → hub motor port.
Bullets aren't keyed: red to red, black to black, or the motor spins backwards. Encoder: 4-pin JST PH on the hub, black 4-pin 0.1" (TJC8) housing on the motor.

⚠️ **Set the correct motor type** for each drive motor in the Driver Station config (the exact goBILDA model / gear ratio, not a generic type).
In encoder speed control mode (`RUN_USING_ENCODER`, decision 008), the SDK uses the configured motor type to know the motor's top speed.
A wrong type can make the robot much slower. Drive motor model / ratio: TBA

## Hubs

| Hub | Notes |
|---|---|
| Control Hub | Drive motors (all 4 motor ports, planned; bench bot has the left side only), Limelight (USB 3.0), Pinpoint (I2C), `indicatorLight` (servo 0) |
| Expansion Hub | Flywheel and intake motors (#5, #4), `turretEncoder` (motor-encoder port, decision 019). Bench bot (10/24): right drive motors on motors 0 and 1. Connected by: REV JST PH **3-pin** Communication Cable (RS485 → RS485, REV-31-1417) + XT30 Extension Cable for power (REV-31-1392). Without the 3-pin cable the hub has power (LED blinks blue) but the Driver Station can't find its devices |

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
| ___ | ___ | ___ | Intake motor(s): waiting on build team |
| `intake___Switch` | Digital Device | ___ / digital port ___ | goBILDA Micro Limit Switch (Bump Lever), **3103-0001-0002**: switch 1. Exact job TBD. Name it by that job, e.g. `intakeLoadedSwitch`. Check which reading (true/false) means "pressed" |
| `intake___Switch` | Digital Device | ___ / digital port ___ | goBILDA Micro Limit Switch (Bump Lever), **3103-0001-0002**: switch 2. Same notes as switch 1 |

## Launcher (`subsystems/Launcher.java`, issue #5)

| Config name | Device type | Hub / Port | Notes |
|---|---|---|---|
| `launcherMotor` | Motor (DcMotorEx) | Expansion Hub / motor port ___ | **goBILDA 5203 Series Yellow Jacket Motor**. Configure as `goBILDA 5203 Series` in Driver Station |

## Camera (`subsystems/Camera.java`, issue #7, decisions 015, 016)

| Config name | Device type | Hub / Port | Notes |
|---|---|---|---|
| `limelight` | Limelight 3A | Control Hub / **USB 3.0 port** | ⚠️ **Must use the port labeled USB 3.0.** Mounted on turret. For distance math, measure and set in `Camera.java`: `CAMERA_MOUNT_TILT_DEGREES` (0 = faces forward; bench bot = 0), `CAMERA_HEIGHT_INCHES` (lens above floor, **not measured yet**), `TAG_HEIGHT_INCHES`. Limelight pipeline marker size = **3.25 in (82.55 mm)** |

## Turret (`subsystems/Turret.java`, issue #31, decisions 016, 017, 019, 021)

| Config name | Device type | Hub / Port | Notes |
|---|---|---|---|
| `turretServo` | **Continuous Rotation Servo** | ___ / servo port ___ | **goBILDA 2000 Series Dual Mode Servo, 25-3 Speed (2000-0025-0003)**, set to **continuous mode** with the goBILDA Servo Programmer (decision 021). Code sets its signal range to **1000–2000 µs** (goBILDA's continuous range). History: goBILDA 25-2 Torque → Axon MAX (10/14) → this (10/20) |
| `turretEncoder` | Motor (encoder only, never powered) | **Expansion Hub** / motor-encoder port ___ | ELC Encoder V2, **quadrature** output (decision 019): use the encoder's **"D" connector** (not "A" = analog). Configure the motor port with this name; the code only reads its encoder. Data sheet: 3.3–3.6 V (the hub's encoder port gives 3.3 V ✅), JST-PH 4-pin (same as the hub ✅), 4000 counts per revolution. ⚠️ Pin order isn't on the data sheet: use the cable that came with it and check "Encoder counts" changes when you turn it |
| `turretForwardSwitch` | Digital Device | ___ / digital port ___ | REV Magnetic Limit Switch: triggers when the turret faces **forward** (indicator, not a stop, decision 017). Digital ports have 2 channels (n, n+1): if it never changes, try the other channel number |

## Light (`subsystems/Light.java`, issue #31, decision 018)

| Config name | Device type | Hub / Port | Notes |
|---|---|---|---|
| `indicatorLight` | Servo | Control Hub / servo port 0 | goBILDA RGB Indicator Light (3-pin servo plug; black wire toward the hub's ground mark). The "servo position" picks the color (see `Light.Color`). Tested 10/24 |
