package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.Camera;
import org.firstinspires.ftc.teamcode.subsystems.Power;

/**
 * TEST: check that the Limelight camera sees AprilTags (issue #7).
 *
 * Nothing moves in this test. Hold an AprilTag (IDs 30-45) in front of the camera, or point the robot
 * at a CELL, and check that "AprilTags seen" shows the tag's ID.
 *
 * No alliance is chosen here, so EVERY BIOBUZZ tag counts as a target. To check that the camera
 * ignores the other alliance's color, use "Red Camera Test" or "Blue Camera Test" (decision 022).
 *
 * "Camera connected" = false? Check it's in the Control Hub USB port labeled "USB 3.0".
 */
@TeleOp(name = "Camera Test", group = "Tests")
public class CameraTest extends LinearOpMode {

    @Override
    public void runOpMode() {
        Power power = new Power(hardwareMap); // battery voltage + current on every test
        Camera camera = new Camera(hardwareMap);

        while (opModeInInit() || opModeIsActive()) {
            camera.addTelemetry(telemetry);
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        camera.stop();
    }
}
