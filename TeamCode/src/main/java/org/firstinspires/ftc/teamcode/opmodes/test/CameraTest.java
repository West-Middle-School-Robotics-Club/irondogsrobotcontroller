package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.Camera;

/**
 * TEST: check that the Limelight camera sees AprilTags (issue #7).
 *
 * Nothing moves in this test. Hold an AprilTag (IDs 30-45) in front of the camera, or point the robot
 * at a CELL, and check:
 *   - "AprilTags seen" shows the tag's ID
 *   - "Target" goes UP when the tag moves LEFT in the camera's view, and DOWN when it moves right
 */
@TeleOp(name = "Camera Test", group = "Tests")
public class CameraTest extends LinearOpMode {

    @Override
    public void runOpMode() {
        Camera camera = new Camera(hardwareMap);

        while (opModeInInit() || opModeIsActive()) {
            telemetry.addLine("Move an AprilTag LEFT: Target goes UP");
            telemetry.addLine();
            camera.addTelemetry(telemetry);
            telemetry.update();
        }

        camera.stop();
    }
}
