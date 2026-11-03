package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Robot;

/**
 * EXAMPLE: the pattern every OpMode should follow.
 *
 *   1. Create the Robot (this sets up all the hardware).
 *   2. Wait for START.
 *   3. Loop: read the gamepads, call subsystem methods, show telemetry.
 *
 * Notice there is no motor code here. All hardware details live in the subsystems,
 * and turning sticks into drive commands lives in DriverControls.
 * Our real TeleOp (MainTeleOp) is issue #3.
 */
@TeleOp(name = "Example TeleOp", group = "Examples")
public class ExampleTeleOp extends LinearOpMode {

    @Override
    public void runOpMode() {
        Robot robot = new Robot(hardwareMap);
        DriverControls driver = new DriverControls(); // A = straight-line assist on/off

        telemetry.addData("Status", "Ready! Press START");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            // Driving always goes through DriverControls, so every TeleOp drives the same way.
            driver.drive(robot.drivetrain, gamepad1);

            robot.addTelemetry(telemetry);
            telemetry.update();
        }

        robot.stop();
    }
}
