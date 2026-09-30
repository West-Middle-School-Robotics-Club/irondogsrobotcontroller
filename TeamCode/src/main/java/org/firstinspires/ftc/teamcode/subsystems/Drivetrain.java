package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Drivetrain subsystem: our four-motor mecanum drivetrain (decision 004).
 *
 * Used by BOTH TeleOp and Autonomous. This class doesn't know where its
 * commands come from: TeleOp gets them from the gamepad sticks, Autonomous
 * gets them from code. Gamepad code belongs in OpModes, NOT here.
 *
 * This class is the ONLY place that talks to the drive motors.
 * OpModes just call drive(...) and stop().
 */
public class Drivetrain {

    // Config names. These must match the Driver Station robot configuration EXACTLY.
    // If you change one here, update TeamCode/docs/hardware-config.md too.
    public static final String FRONT_LEFT_DRIVE_NAME  = "frontLeftDrive";
    public static final String FRONT_RIGHT_DRIVE_NAME = "frontRightDrive";
    public static final String BACK_LEFT_DRIVE_NAME   = "backLeftDrive";
    public static final String BACK_RIGHT_DRIVE_NAME  = "backRightDrive";

    // Our toggle for issue #11 comparison test for Wednesday:
    // false = RUN_WITHOUT_ENCODER (power = % of battery voltage, proposed decision 008)
    // true  = RUN_USING_ENCODER (speed control using motor encoders)
    public static final boolean USE_ENCODER_SPEED_CONTROL = false;

    private final DcMotor frontLeftDrive;
    private final DcMotor frontRightDrive;
    private final DcMotor backLeftDrive;
    private final DcMotor backRightDrive;

    public Drivetrain(HardwareMap hardwareMap) {
        frontLeftDrive  = hardwareMap.get(DcMotor.class, FRONT_LEFT_DRIVE_NAME);
        frontRightDrive = hardwareMap.get(DcMotor.class, FRONT_RIGHT_DRIVE_NAME);
        backLeftDrive   = hardwareMap.get(DcMotor.class, BACK_LEFT_DRIVE_NAME);
        backRightDrive  = hardwareMap.get(DcMotor.class, BACK_RIGHT_DRIVE_NAME);

        // The LEFT side is reversed on our robot (tested 9/23 with manualDriver).
        // If the robot drives backwards when you push the stick forward, swap these.
        frontLeftDrive.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeftDrive.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRightDrive.setDirection(DcMotorSimple.Direction.FORWARD);
        backRightDrive.setDirection(DcMotorSimple.Direction.FORWARD);

        DcMotor.RunMode runMode = USE_ENCODER_SPEED_CONTROL
                ? DcMotor.RunMode.RUN_USING_ENCODER
                : DcMotor.RunMode.RUN_WITHOUT_ENCODER;

        for (DcMotor motor : new DcMotor[] {frontLeftDrive, frontRightDrive, backLeftDrive, backRightDrive}) {
            // BRAKE makes the robot stop quickly when the sticks are released.
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            motor.setMode(runMode);
        }
    }

    /**
     * Drive the robot, relative to the way the robot is facing.
     *
     * Directions follow our team convention (decision 007), the same as the Pinpoint:
     * +forward = forward, +left = LEFT, +turn = COUNTER-CLOCKWISE (turning left).
     *
     * @param forward positive = drive forward                  (-1 to 1)
     * @param left    positive = strafe LEFT                     (-1 to 1)
     * @param turn    positive = turn COUNTER-CLOCKWISE (left)   (-1 to 1)
     */
    public void drive(double forward, double left, double turn) {
        // Mecanum math: each wheel gets a mix of forward, left and turn.
        // To strafe left, the front-left and back-right wheels spin backwards.
        // To turn left, the left side spins backwards and the right side forwards.
        double frontLeftPower  = forward - left - turn;
        double frontRightPower = forward + left + turn;
        double backLeftPower   = forward + left - turn;
        double backRightPower  = forward - left + turn;

        // If any power is above 1, scale them all down together so the
        // robot still moves in the direction the driver asked for.
        double max = Math.max(1.0, Math.max(
                Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower)),
                Math.max(Math.abs(backLeftPower), Math.abs(backRightPower))));

        frontLeftDrive.setPower(frontLeftPower / max);
        frontRightDrive.setPower(frontRightPower / max);
        backLeftDrive.setPower(backLeftPower / max);
        backRightDrive.setPower(backRightPower / max);
    }

    /** Stop all drive motors. */
    public void stop() {
        drive(0, 0, 0);
    }

    /** Show this subsystem's status on the Driver Station. */
    public void addTelemetry(Telemetry telemetry) {
        telemetry.addData("Drive mode", USE_ENCODER_SPEED_CONTROL ? "RUN_USING_ENCODER" : "RUN_WITHOUT_ENCODER");
        telemetry.addData("Drivetrain front L/R", "%.2f  %.2f", frontLeftDrive.getPower(), frontRightDrive.getPower());
        telemetry.addData("Drivetrain back  L/R", "%.2f  %.2f", backLeftDrive.getPower(), backRightDrive.getPower());
    }
}
