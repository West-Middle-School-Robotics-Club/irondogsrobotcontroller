package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.Camera;

/**
 * TEST: check that the Limelight camera sees AprilTags (issue #7).
 *
 * Nothing moves in this test. Hold an AprilTag (IDs 30-45) in front of the camera, or point the robot
 * at a CELL, and check that "AprilTags seen" shows the tag's ID.
 *
 * "Camera connected" = false? Check it's in the Control Hub USB port labeled "USB 3.0".
 */
@TeleOp(name = "Camera Test", group = "Tests")
public class CameraTest extends LinearOpMode {

    @Override
    public void runOpMode() {
        Camera camera = new Camera(hardwareMap);

        while (opModeInInit() || opModeIsActive()) {
            camera.addTelemetry(telemetry);
            telemetry.update();
        }

        camera.stop();
    }
}
