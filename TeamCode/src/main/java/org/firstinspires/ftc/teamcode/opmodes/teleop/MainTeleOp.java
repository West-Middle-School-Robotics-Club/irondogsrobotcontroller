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
 * During INIT the light shows the alliance color (red or blue) so the drive team can see from the
 * field that the right OpMode is selected. Nothing moves during INIT (game rule G304.H).
 *
 * Driving goes through DriverControls, so every TeleOp drives consistently.
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
        Light light = new Light(hardwareMap);
        Camera camera = new Camera(hardwareMap, alliance);
        Power power = new Power(hardwareMap);

        // TeleOp drives in FLOAT (wheels coast when the sticks are released) for smoother driving.
        // Autonomous keeps the Drivetrain default, BRAKE, for precise stops (decision 023).
        robot.drivetrain.setBrakeOnStop(false);

        // ---------- INIT ----------
        light.setColor(allianceColor(alliance));

        while (opModeInInit()) {
            telemetry.addData("ALLIANCE", "%s  (light is %s)", alliance, allianceColor(alliance));
            telemetry.addLine("Wrong color? Stop and pick the other Main TeleOp.");
            telemetry.addLine();
            power.addTelemetry(telemetry);
            camera.addTelemetry(telemetry);
            telemetry.update();
        }

        if (isStopRequested()) {
            camera.stop();
            light.off();
            return;
        }

        // ---------- MATCH ----------
        while (opModeIsActive()) {
            // Driving always goes through DriverControls, so every TeleOp drives the same way.
            boolean slowMode = DriverControls.drive(robot.drivetrain, gamepad1);

            telemetry.addData("Alliance", alliance);
            telemetry.addData("Slow mode", slowMode ? "ON" : "OFF");
            power.addTelemetry(telemetry);
            robot.addTelemetry(telemetry);
            camera.addTelemetry(telemetry);
            light.addTelemetry(telemetry);
            telemetry.update();
        }

        robot.stop();
        camera.stop();
        light.off();
    }

    /** The light color for an alliance: the OpMode decides what colors mean (decision 018). */
    private static Light.Color allianceColor(Alliance alliance) {
        return alliance == Alliance.RED ? Light.Color.RED : Light.Color.BLUE;
    }
}
