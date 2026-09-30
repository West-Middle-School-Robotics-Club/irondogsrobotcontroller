package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Robot;

/**
 * Main TeleOp OpMode for competition driving.
 *
 * Driving goes through DriverControls, so every TeleOp drives consistently.
 * Hardware operations go through the Robot subsystem classes.
 */
@TeleOp(name = "Main TeleOp", group = "TeleOp")
public class MainTeleOp extends LinearOpMode {

    @Override
    public void runOpMode() {
        Robot robot = new Robot(hardwareMap);

        telemetry.addData("Status", "Ready! Press START");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            // Driving always goes through DriverControls, so every TeleOp drives the same way.
            boolean slowMode = DriverControls.drive(robot.drivetrain, gamepad1);

            robot.addTelemetry(telemetry);
            telemetry.addData("Slow mode", slowMode ? "ON" : "OFF");
            telemetry.update();
        }

        robot.stop();
    }
}
