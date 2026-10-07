package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.Camera;

/**
 * TEST: check the Limelight camera and find the camera servo positions (issue #7).
 *
 * Controls (gamepad 1), all after START:
 *   dpad up / down   move the servo a little (+/- 0.01). Hold a bumper for big steps (0.05)
 *   A                go to LOOK_FORWARD_POSITION
 *   Y                go to LOOK_UP_POSITION
 *
 * How to use it:
 *   1. Use the dpad to aim the camera straight ahead. Write down the "Camera servo" number.
 *   2. Aim it up until it sees an AprilTag under a CELL. Write down that number too.
 *   3. Put both numbers in Camera.java (LOOK_FORWARD_POSITION, LOOK_UP_POSITION).
 *
 * Only the camera is used here; the drive motors are not touched.
 */
@TeleOp(name = "Camera Test", group = "Tests")
public class CameraTest extends LinearOpMode {

    @Override
    public void runOpMode() {
        Camera camera = new Camera(hardwareMap);

        // INIT: the servo does NOT move until START (game rule G304.H).
        while (opModeInInit()) {
            telemetry.addLine("Press START. The camera servo will move to LOOK_FORWARD_POSITION.");
            camera.addTelemetry(telemetry);
            telemetry.update();
        }

        camera.lookForward();

        while (opModeIsActive()) {
            double step = (gamepad1.left_bumper || gamepad1.right_bumper) ? 0.05 : 0.01;

            if (gamepad1.dpadUpWasPressed()) {
                camera.setServoPosition(camera.getServoPosition() + step);
            }
            if (gamepad1.dpadDownWasPressed()) {
                camera.setServoPosition(camera.getServoPosition() - step);
            }
            if (gamepad1.aWasPressed()) {
                camera.lookForward();
            }
            if (gamepad1.yWasPressed()) {
                camera.lookUp();
            }

            telemetry.addLine("dpad = move servo (bumper = big steps)   A = forward   Y = up");
            telemetry.addLine();
            camera.addTelemetry(telemetry);
            telemetry.update();
        }

        camera.stop();
    }
}
