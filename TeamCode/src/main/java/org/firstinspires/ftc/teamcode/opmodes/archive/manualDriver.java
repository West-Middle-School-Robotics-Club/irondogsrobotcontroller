package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp
public class manualDriver extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        // Declare our motors
        // Make sure your ID's match your configuration
        DcMotor frontLeftDrive = hardwareMap.dcMotor.get("frontLeftDrive");
        DcMotor backLeftDrive = hardwareMap.dcMotor.get("backLeftDrive");
        DcMotor frontRightDrive = hardwareMap.dcMotor.get("frontRightDrive");
        DcMotor backRightDrive = hardwareMap.dcMotor.get("backRightDrive");

        // Reverse the right side motors. This may be wrong for your setup.
        // If your robot moves backwards when commanded to go forwards,
        // reverse the left side instead.
        // See the note about this earlier on this page.
        //NOTE IRONDOGS 09/23/26:
        //Changed to left side as it is backwards if we have it on right side
        frontLeftDrive.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeftDrive.setDirection(DcMotorSimple.Direction.REVERSE);

        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {
            double y = -gamepad1.left_stick_y; // Remember, Y stick value is reversed
            double x = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
            double rx = gamepad1.right_stick_x;

            // Denominator is the largest motor power (absolute value) or 1
            // This ensures all the powers maintain the same ratio,
            // but only if at least one is out of the range [-1, 1]
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
            double frontLeftPower = (y + x + rx) / denominator;
            double backLeftPower = (y - x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backRightPower = (y + x - rx) / denominator;

            frontLeftDrive.setPower(frontLeftPower);
            backLeftDrive.setPower(backLeftPower);
            frontRightDrive.setPower(frontRightPower);
            backRightDrive.setPower(backRightPower);
        }
    }
}