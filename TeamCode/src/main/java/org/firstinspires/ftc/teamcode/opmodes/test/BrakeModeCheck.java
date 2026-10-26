package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.opmodes.teleop.DriverControls;
import org.firstinspires.ftc.teamcode.subsystems.Power;

/**
 * TEST: what brake mode do our drive motors really use?
 *
 * Brake mode ("ZeroPowerBehavior") decides what a motor does when its power is 0:
 *   BRAKE = the motor resists turning, so the robot stops quickly and is hard to push
 *   FLOAT = the motor spins freely, so the robot coasts and is easy to push
 *
 * The SDK does NOT reset brake mode between OpModes. This test shows what the
 * previous OpMode (or powering up the robot) left behind, and lets you feel the difference.
 * Follow the experiment steps in issue #17 and record your results there.
 *
 * Controls (gamepad 1):
 *   X          switch to BRAKE
 *   Y          switch to FLOAT
 *   sticks     drive (let go of the sticks at full speed to compare stopping)
 */
@TeleOp(name = "Brake Mode Check", group = "Tests")
public class BrakeModeCheck extends LinearOpMode {

    @Override
    public void runOpMode() {
        Power power = new Power(hardwareMap); // battery voltage + current on every test
        // Read the leftover brake mode FIRST, because creating the Robot sets BRAKE.
        DcMotor.ZeroPowerBehavior brakeModeAtStart = TestDriveMotors.getBrakeMode(hardwareMap);
        Robot robot = new Robot(hardwareMap);

        while (opModeInInit()) {
            telemetry.addData("Brake mode when this OpMode started", brakeModeAtStart);
            telemetry.addLine("(left over from the last OpMode, or from powering up)");
            telemetry.addLine("Write this down, then press START.");
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        while (opModeIsActive()) {
            if (gamepad1.xWasPressed()) {
                TestDriveMotors.setBrake(hardwareMap, true);
            }
            if (gamepad1.yWasPressed()) {
                TestDriveMotors.setBrake(hardwareMap, false);
            }

            DriverControls.drive(robot.drivetrain, gamepad1);

            telemetry.addData("Brake mode when this OpMode started", brakeModeAtStart);
            telemetry.addData("Brake mode NOW", TestDriveMotors.getBrakeMode(hardwareMap));
            telemetry.addLine();
            telemetry.addLine("X = BRAKE     Y = FLOAT");
            telemetry.addLine("Drive full speed, let go, and see how far the robot rolls.");
            telemetry.addLine("With the robot stopped, try pushing it in each mode.");
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        // Leave the motors in BRAKE for whatever OpMode runs next.
        TestDriveMotors.setBrake(hardwareMap, true);
        robot.stop();
    }
}
