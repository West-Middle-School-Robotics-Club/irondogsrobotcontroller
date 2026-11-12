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
    public static final String FRONT_LEFT_DRIVE_NAME  = "frontLeftDrive";  //1
    public static final String FRONT_RIGHT_DRIVE_NAME = "frontRightDrive"; //3
    public static final String BACK_LEFT_DRIVE_NAME   = "backLeftDrive"; //2
    public static final String BACK_RIGHT_DRIVE_NAME  = "backRightDrive"; //4

    // How the drive motors take "power" (decision 054: plain power everywhere):
    // false = RUN_WITHOUT_ENCODER (power = % of battery voltage, plain and simple)
    // true  = RUN_USING_ENCODER (power = % of top speed, the hub uses the encoders to hold that speed)
    // Note 11/11: false. With encoders the hub's speed loop surged at low powers in Basic Auton (lurch, stop, lurch).
    public static final boolean USE_ENCODER_SPEED_CONTROL = false;

    private final DcMotor frontLeftDrive;
    private final DcMotor frontRightDrive;
    private final DcMotor backLeftDrive;
    private final DcMotor backRightDrive;

    private boolean brakeOnStop = true;
    private boolean encoderSpeedControl = USE_ENCODER_SPEED_CONTROL;

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

        setEncoderSpeedControl(USE_ENCODER_SPEED_CONTROL);

        // Default is BRAKE (precise stops, what autonomous needs). TeleOp switches to FLOAT
        // for smoother driving by calling setBrakeOnStop(false). See decision 023.
        setBrakeOnStop(true);
    }

    /**
     * Switch encoder speed control on or off (decision 008 is still open, so an OpMode can pick its own mode).
     *   true  = RUN_USING_ENCODER: power means "% of top speed", and the hub uses the wheel encoders to hold that speed
     *   false = RUN_WITHOUT_ENCODER: power means "% of battery voltage", plain and simple
     * The constructor sets USE_ENCODER_SPEED_CONTROL. Call this with the robot stopped. Nothing moves.
     */
    public void setEncoderSpeedControl(boolean useEncoders) {
        encoderSpeedControl = useEncoders;
        DcMotor.RunMode runMode = useEncoders
                ? DcMotor.RunMode.RUN_USING_ENCODER
                : DcMotor.RunMode.RUN_WITHOUT_ENCODER;
        for (DcMotor motor : new DcMotor[] {frontLeftDrive, frontRightDrive, backLeftDrive, backRightDrive}) {
            motor.setMode(runMode);
        }
    }

    /** True if the hub holds wheel speeds with the encoders (RUN_USING_ENCODER), false for plain power. */
    public boolean isEncoderSpeedControl() {
        return encoderSpeedControl;
    }

    /**
     * What the wheels do when power is 0 (decision 023).
     *   true  = BRAKE: the motors resist turning, so the robot stops quickly. Autonomous uses this for precise moves.
     *   false = FLOAT: the wheels coast to a stop. TeleOp uses this so driving feels smoother.
     * This doesn't move anything, so it's OK to call during INIT.
     */
    public void setBrakeOnStop(boolean brake) {
        brakeOnStop = brake;
        DcMotor.ZeroPowerBehavior behavior = brake
                ? DcMotor.ZeroPowerBehavior.BRAKE
                : DcMotor.ZeroPowerBehavior.FLOAT;
        for (DcMotor motor : new DcMotor[] {frontLeftDrive, frontRightDrive, backLeftDrive, backRightDrive}) {
            motor.setZeroPowerBehavior(behavior);
        }
    }

    /** True if the wheels BRAKE at zero power, false if they FLOAT. */
    public boolean isBrakeOnStop() {
        return brakeOnStop;
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
        telemetry.addData("Drive mode", encoderSpeedControl ? "RUN_USING_ENCODER" : "RUN_WITHOUT_ENCODER");
        telemetry.addData("Stop mode", brakeOnStop ? "BRAKE" : "FLOAT");
        telemetry.addData("Drivetrain front L/R", "%.2f  %.2f", frontLeftDrive.getPower(), frontRightDrive.getPower());
        telemetry.addData("Drivetrain back  L/R", "%.2f  %.2f", backLeftDrive.getPower(), backRightDrive.getPower());
    }
}
