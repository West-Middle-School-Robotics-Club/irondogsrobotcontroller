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
 *
 * Gamepad 1 = DRIVER: driving goes through DriverControls (sticks, bumpers = SLOW, triggers = TURBO, A = assist), so every
 * TeleOp drives consistently. Gamepad 2 = OPERATOR: mechanism buttons, in OperatorControls (decision 029).
 * The flywheel (launcherMotor) starts when START is pressed and runs the whole match (not in INIT: G304.H).
 * When START is pressed (decision 041): the launcher trigger goes to REST (blocks balls), the flywheel runs at
 * Launcher.DEFAULT_SPIN_POWER, the intake + water wheel run (OperatorControls.INTAKE_ON_AT_START), and the turret
 * turns to forward (if it is homed). The lights turn GREEN while the turret is homed and turning to forward.
 * Camera, Light and Power are created here, not in Robot.java, until they're configured on every robot
 * (see code-structure.md).
 */
public abstract class MainTeleOp extends LinearOpMode {

    // What the lights mean in Main TeleOp (decisions 018, 022, 028). Colors are in Light.Color.
    public static final Light.Color RED_ALLIANCE_COLOR = Light.Color.RED;
    public static final Light.Color BLUE_ALLIANCE_COLOR = Light.Color.BLUE;
    public static final Light.Color NECTAR_COLOR = Light.Color.VIOLET; // "purple": the next ball to launch is a NECTAR
    public static final Light.Color TURRET_MOVING_COLOR = Light.Color.GREEN; // the turret is homed and turning to forward

    // At START the turret turns to forward by itself. That is only safe if it is already HOMED (we pointed it at the
    // magnet in INIT). Set this to true to let it also FIND the magnet by turning LEFT. Keep it false until the
    // turret's hard stops are moved: today the left stop is before the magnet, so searching drives into the stop.
    public static final boolean TURRET_FIND_HOME_AT_START = false;

    /** Which alliance this OpMode is for. */
    protected abstract Alliance alliance();

    @Override
    public void runOpMode() {
        Alliance alliance = alliance();

        Robot robot = new Robot(hardwareMap);
        Light leftLight = new Light(hardwareMap, Light.LEFT_LIGHT_NAME);
        Light rightLight = new Light(hardwareMap, Light.RIGHT_LIGHT_NAME);
        Camera camera = new Camera(hardwareMap, alliance);
        Power power = new Power(hardwareMap);
        DriverControls driver = new DriverControls();
        OperatorControls operator = new OperatorControls();

        // TeleOp drives in FLOAT (wheels coast when the sticks are released) for smoother driving.
        // Autonomous keeps the Drivetrain default, BRAKE, for precise stops (decision 023).
        robot.drivetrain.setBrakeOnStop(false);

        // ---------- INIT ----------
        leftLight.setColor(allianceColor(alliance));
        rightLight.setColor(allianceColor(alliance));

        while (opModeInInit()) {
            robot.turret.update(); // watches the home switch; never moves the turret during INIT
            telemetry.addData("ALLIANCE", "%s  (lights are %s)", alliance, allianceColor(alliance));
            telemetry.addLine("Wrong color? Stop and pick the other Main TeleOp.");
            telemetry.addLine();
            power.addTelemetry(telemetry);
            camera.addTelemetry(telemetry);
            telemetry.update();
        }

        if (isStopRequested()) {
            camera.stop();
            leftLight.off();
            rightLight.off();
            return;
        }

        // ---------- MATCH ----------
        // START pressed: the flywheel runs the whole match, and the trigger goes to rest (blocks launching).
        robot.launcher.spinUp();
        robot.launcher.resetTrigger();
        // The turret turns to forward on its own. Not homed yet? Then it waits for the operator (X button).
        if (robot.turret.isHomed() || TURRET_FIND_HOME_AT_START) {
            robot.turret.goToRestAngle();
        }

        while (opModeIsActive()) {
            // Driving always goes through DriverControls, so every TeleOp drives the same way.
            DriverControls.DriveSpeed speed = driver.drive(robot.drivetrain, gamepad1);
            // Mechanisms always go through OperatorControls (gamepad 2).
            operator.update(robot, gamepad2);
            robot.turret.update(); // every loop: turns the turret and keeps it inside its limits

            // Lights: PURPLE = the next ball to launch is a NECTAR (it matters most, so it wins).
            // GREEN = the turret is homed and turning to forward. Otherwise our alliance color.
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
            robot.addTelemetry(telemetry);
            camera.addTelemetry(telemetry);
            leftLight.addTelemetry(telemetry);
            rightLight.addTelemetry(telemetry);
            telemetry.update();
        }

        robot.stop();
        camera.stop();
        leftLight.off();
        rightLight.off();
    }

    /** The light color for an alliance: the OpMode decides what colors mean (decision 018). */
    private static Light.Color allianceColor(Alliance alliance) {
        return alliance == Alliance.RED ? RED_ALLIANCE_COLOR : BLUE_ALLIANCE_COLOR;
    }
}
