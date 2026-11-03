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
 * Gamepad 1 = DRIVER: driving goes through DriverControls (sticks, bumpers = SLOW, triggers = TURBO), so every
 * TeleOp drives consistently. Gamepad 2 = OPERATOR: mechanism buttons, in OperatorControls (decision 029).
 * The flywheel (launcherMotor) starts when START is pressed and runs the whole match (not in INIT: G304.H).
 * Camera, Light and Power are created here, not in Robot.java, until they're configured on every robot
 * (see code-structure.md).
 */
public abstract class MainTeleOp extends LinearOpMode {

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
        OperatorControls operator = new OperatorControls();

        // TeleOp drives in FLOAT (wheels coast when the sticks are released) for smoother driving.
        // Autonomous keeps the Drivetrain default, BRAKE, for precise stops (decision 023).
        robot.drivetrain.setBrakeOnStop(false);

        // ---------- INIT ----------
        leftLight.setColor(allianceColor(alliance));
        rightLight.setColor(allianceColor(alliance));

        while (opModeInInit()) {
            robot.turret.update(); // homes the turret if it passes forward; never moves it during INIT
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

        while (opModeIsActive()) {
            // Driving always goes through DriverControls, so every TeleOp drives the same way.
            DriverControls.DriveSpeed speed = DriverControls.drive(robot.drivetrain, gamepad1);
            // Mechanisms always go through OperatorControls (gamepad 2).
            operator.update(robot, gamepad2);
            robot.turret.update(); // every loop: turns the turret and keeps it inside its limits

            // Lights: PURPLE = the next ball to launch is a NECTAR, otherwise our alliance color.
            Light.Color lightColor = robot.intake.isNectarDetected() ? Light.Color.VIOLET : allianceColor(alliance);
            leftLight.setColor(lightColor);
            rightLight.setColor(lightColor);

            telemetry.addData("Alliance", alliance);
            telemetry.addData("Drive speed", speed);
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
        return alliance == Alliance.RED ? Light.Color.RED : Light.Color.BLUE;
    }
}
