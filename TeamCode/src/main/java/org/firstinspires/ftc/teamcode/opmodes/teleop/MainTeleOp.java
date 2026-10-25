package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Alliance;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.subsystems.Camera;
import org.firstinspires.ftc.teamcode.subsystems.Light;

/**
 * Main TeleOp OpMode for competition driving.
 *
 * Driving goes through DriverControls, so every TeleOp drives consistently.
 * Hardware operations go through the Robot subsystem classes.
 *
 * PICKING OUR ALLIANCE (decision 022): during INIT, the driver presses
 *     B (the red button)  = RED alliance
 *     X (the blue button) = BLUE alliance
 * The Driver Station shows the choice and the robot's light turns that color, so the
 * drive team can check it from the field before START. The choice is remembered until
 * the robot is power-cycled, so it only needs picking once per match day unless it changes.
 * Nothing moves during INIT (game rule G304.H): the light is the only thing that changes.
 *
 * Camera and Light are created here, not in Robot.java, until they're configured on every robot
 * (see code-structure.md).
 */
@TeleOp(name = "Main TeleOp", group = "TeleOp")
public class MainTeleOp extends LinearOpMode {

    // Remembered between runs of this OpMode (until the Robot Controller restarts).
    private static Alliance alliance = Alliance.RED;

    @Override
    public void runOpMode() {
        Robot robot = new Robot(hardwareMap);
        Light light = new Light(hardwareMap);
        Camera camera = new Camera(hardwareMap, alliance);

        // TeleOp drives in FLOAT (wheels coast when the sticks are released) for smoother driving.
        // Autonomous keeps the Drivetrain default, BRAKE, for precise stops (decision 023).
        robot.drivetrain.setBrakeOnStop(false);

        // ---------- INIT: pick the alliance ----------
        while (opModeInInit()) {
            if (gamepad1.b) {
                alliance = Alliance.RED;
            } else if (gamepad1.x) {
                alliance = Alliance.BLUE;
            }
            camera.setAlliance(alliance);
            light.setColor(allianceColor(alliance));

            telemetry.addLine("=== PICK ALLIANCE: press B = RED, X = BLUE ===");
            telemetry.addData("ALLIANCE", alliance);
            telemetry.addLine("Light shows the alliance color. Then press START.");
            telemetry.addLine();
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
