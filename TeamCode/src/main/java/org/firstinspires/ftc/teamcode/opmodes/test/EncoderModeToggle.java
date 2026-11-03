package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.opmodes.teleop.DriverControls;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.subsystems.Power;

/**
 * TEST: compare driving WITH and WITHOUT encoder speed control (issue #11, decision 008).
 *
 * Drives exactly like Main TeleOp, but press Y to switch the drive motors between:
 *   WITHOUT encoders (RUN_WITHOUT_ENCODER): power = % of battery voltage
 *   WITH encoders    (RUN_USING_ENCODER):   power = % of max speed, held steady by the hub
 *
 * Controls:
 *   Gamepad 1 sticks   drive (bumpers = slow, triggers = turbo, same as Main TeleOp)
 *   Y on gamepad 1     switch mode
 *   Y on gamepad 2     switch mode, for the "blind" test: the driver doesn't know which mode is on
 *
 * The real setting for all other OpModes is USE_ENCODER_SPEED_CONTROL in Drivetrain.java.
 * This test puts that setting back when it stops.
 */
@TeleOp(name = "Encoder Mode Toggle", group = "Tests")
public class EncoderModeToggle extends LinearOpMode {

    @Override
    public void runOpMode() {
        Power power = new Power(hardwareMap); // battery voltage + current on every test
        Robot robot = new Robot(hardwareMap);
        DriverControls driver = new DriverControls(); // A = straight-line assist on/off
        boolean useEncoders = Drivetrain.USE_ENCODER_SPEED_CONTROL;  // start with the real setting

        telemetry.addLine("Press START, then Y to switch modes.");
        power.addTelemetry(telemetry);
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            if (gamepad1.yWasPressed() || gamepad2.yWasPressed()) {
                useEncoders = !useEncoders;
                robot.drivetrain.stop();  // switch modes while the wheels are stopped
                TestDriveMotors.setUseEncoders(hardwareMap, useEncoders);
            }

            DriverControls.DriveSpeed speed = driver.drive(robot.drivetrain, gamepad1);

            telemetry.addData("MODE", useEncoders ? "WITH encoders" : "WITHOUT encoders");
            telemetry.addData("Motors report", TestDriveMotors.getRunMode(hardwareMap));
            telemetry.addData("Avg wheel speed (ticks/s)", "%.0f", TestDriveMotors.getAverageWheelSpeed(hardwareMap));
            driver.addTelemetry(telemetry, speed);
            telemetry.addLine();
            telemetry.addLine("Y (gamepad 1 or 2) = switch mode");
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        robot.stop();
        // Put the real setting back for whatever OpMode runs next.
        TestDriveMotors.setUseEncoders(hardwareMap, Drivetrain.USE_ENCODER_SPEED_CONTROL);
    }
}
