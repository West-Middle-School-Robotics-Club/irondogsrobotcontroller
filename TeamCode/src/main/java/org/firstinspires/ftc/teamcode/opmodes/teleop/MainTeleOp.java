package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Alliance;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.subsystems.Camera;
import org.firstinspires.ftc.teamcode.subsystems.Light;
import org.firstinspires.ftc.teamcode.subsystems.Power;

/**
 * Main TeleOp for competition driving: the shared code behind "Main TeleOp RED" and "Main TeleOp BLUE".
 *
 * This class has no @TeleOp annotation, so it doesn't show on the Driver Station by itself.
 * MainTeleOpRed and MainTeleOpBlue extend it and only say which alliance we are (decision 022).
 * The drive team picks the OpMode that matches our alliance for the match; no buttons to press.
 *
 * During INIT both lights show the alliance color (red or blue) so the drive team can see from the
 * field that the right OpMode is selected. Nothing moves during INIT (game rule G304.H).
 * During the match both lights turn PURPLE (VIOLET) while the next ball to launch is a NECTAR
 * (either nectar detector switch pressed), then go back to the alliance color (decision 028).
 * The nectar detectors are OFF for now (Intake.NECTAR_DETECTOR_ENABLED = false, decision 065), so no purple.
 *
 * Gamepad 1 = DRIVER: driving goes through DriverControls (sticks, A = assist; SLOW / TURBO are off, decision 063), so every
 * TeleOp drives consistently. Gamepad 2 = OPERATOR: mechanism buttons, in OperatorControls (decision 029).
 * The flywheel (launcherMotor) starts when START is pressed and runs the whole match (not in INIT: G304.H).
 * When START is pressed (decision 041): the launcher trigger goes to REST (blocks balls), the flywheel runs at
 * Launcher.DEFAULT_SPIN_POWER, the INTAKE starts and runs the whole match (OperatorControls.INTAKE_ALWAYS_ON,
 * decision 067), the water wheel stays OFF until the operator presses A (WATER_WHEEL_ON_AT_START = false), and the turret
 * turns to its rest spot, the home magnet (if it is homed). The lights turn GREEN while the turret is homed and turning there.
 * Camera (only if CAMERA_ENABLED), Light and Power are created here, not in Robot.java, until they're configured on every robot
 * (see code-structure.md).
 */
public abstract class MainTeleOp extends LinearOpMode {

    // What the lights mean in Main TeleOp (decisions 018, 022, 028). Colors are in Light.Color.
    public static final Light.Color RED_ALLIANCE_COLOR = Light.Color.RED;
    public static final Light.Color BLUE_ALLIANCE_COLOR = Light.Color.BLUE;
    public static final Light.Color NECTAR_COLOR = Light.Color.VIOLET; // "purple": the next ball to launch is a NECTAR
    public static final Light.Color TURRET_MOVING_COLOR = Light.Color.GREEN; // the turret is homed and turning to its rest spot (the magnet)

    // At START the turret turns to its rest spot (the home magnet) by itself. That is only safe if it is already HOMED (we pointed it at the
    // magnet in INIT). Set this to true to let it also FIND the magnet by turning LEFT. Keep it false until the
    // turret's hard stops are moved: today the left stop is before the magnet, so searching drives into the stop.
    public static final boolean TURRET_FIND_HOME_AT_START = false;

    // Is the Limelight camera used? false = Main TeleOp doesn't start it or show it, so it runs without a camera
    // (11/26: no camera on the robot; we haven't found a good use for AprilTags yet. Maybe later, with Pedro).
    // true = start it and show its lines; then 'limelight' must be in the Driver Station config, or Main TeleOp crashes.
    public static final boolean CAMERA_ENABLED = false;

    /** Which alliance this OpMode is for. */
    protected abstract Alliance alliance();

    @Override
    public void runOpMode() {
        Alliance alliance = alliance();

        Robot robot = new Robot(hardwareMap);
        Light leftLight = new Light(hardwareMap, Light.LEFT_LIGHT_NAME);
        Light rightLight = new Light(hardwareMap, Light.RIGHT_LIGHT_NAME);
        Camera camera = CAMERA_ENABLED ? new Camera(hardwareMap, alliance) : null;   // null = no camera
        Power power = new Power(hardwareMap);
        DriverControls driver = new DriverControls();
        OperatorControls operator = new OperatorControls();

        // TeleOp drives in BRAKE (the robot stops quickly when the sticks are released), like Autonomous (decision 049).
        // It was FLOAT (decision 023). To go back to coasting, change true to false.
        robot.drivetrain.setBrakeOnStop(true);

        // ---------- INIT ----------
        leftLight.setColor(allianceColor(alliance));
        rightLight.setColor(allianceColor(alliance));

        while (opModeInInit()) {
            robot.turret.update(); // homes the turret if it passes the magnet; never moves it during INIT
            telemetry.addData("ALLIANCE", "%s  (lights are %s)", alliance, allianceColor(alliance));
            telemetry.addLine("Wrong color? Stop and pick the other Main TeleOp.");
            telemetry.addLine();
            power.addTelemetry(telemetry);
            if (camera != null) {
                camera.addTelemetry(telemetry);
            }
            telemetry.update();
        }

        if (isStopRequested()) {
            if (camera != null) {
                camera.stop();
            }
            leftLight.off();
            rightLight.off();
            return;
        }

        // ---------- MATCH ----------
        // START pressed: the flywheel runs the whole match, and the trigger goes to rest (blocks launching).
        robot.launcher.spinUp();
        robot.launcher.resetTrigger();
        // The turret turns to its rest spot (the magnet) on its own. Not homed yet? Then it waits for the operator (X button).
        // Skipped when OperatorControls.TURRET_ENABLED is false: then the turret never moves in Main TeleOp.
        if (OperatorControls.TURRET_ENABLED && (robot.turret.isHomed() || TURRET_FIND_HOME_AT_START)) {
            robot.turret.goToRestAngle();
        }

        while (opModeIsActive()) {
            // Driving always goes through DriverControls, so every TeleOp drives the same way.
            DriverControls.DriveSpeed speed = driver.drive(robot.drivetrain, gamepad1);
            // Mechanisms always go through OperatorControls (gamepad 2).
            operator.update(robot, gamepad2);
            robot.turret.update(); // every loop: turns the turret and keeps it inside its limits

            // Lights: PURPLE = the next ball to launch is a NECTAR (it matters most, so it wins). Never while the
            // nectar detectors are off (Intake.NECTAR_DETECTOR_ENABLED, decision 065).
            // GREEN = the turret is homed and turning to its rest spot. Otherwise our alliance color.
            Light.Color lightColor = allianceColor(alliance);
            if (robot.turret.isHomed() && robot.turret.isAutoMoving() && !robot.turret.isAtTarget()) {
                lightColor = TURRET_MOVING_COLOR;
            }
            if (robot.intake.isNectarDetected()) {
                lightColor = NECTAR_COLOR;
            }
            leftLight.setColor(lightColor);
            rightLight.setColor(lightColor);

            telemetry.addData("Alliance", alliance);
            driver.addTelemetry(telemetry, speed);
            operator.addTelemetry(telemetry);
            power.addTelemetry(telemetry);
            // The short match screen (11/26): no drive / stop mode, NECTAR lines only while the detectors are on,
            // no launcher cable warning. The tests still show everything (each subsystem's addTelemetry()).
            robot.drivetrain.addMatchTelemetry(telemetry);
            robot.intake.addMatchTelemetry(telemetry);
            robot.launcher.addMatchTelemetry(telemetry);
            if (OperatorControls.TURRET_ENABLED) {
                robot.turret.addTelemetry(telemetry);   // only while the turret is used (decision 050)
            }
            // Flower flipper: left off the match screen (on hold, may not be on the build).
            if (camera != null) {
                camera.addTelemetry(telemetry);
            }
            leftLight.addTelemetry(telemetry);
            rightLight.addTelemetry(telemetry);
            telemetry.update();
        }

        robot.stop();
        if (camera != null) {
            camera.stop();
        }
        leftLight.off();
        rightLight.off();
    }

    /** The light color for an alliance: the OpMode decides what colors mean (decision 018). */
    private static Light.Color allianceColor(Alliance alliance) {
        return alliance == Alliance.RED ? RED_ALLIANCE_COLOR : BLUE_ALLIANCE_COLOR;
    }
}
