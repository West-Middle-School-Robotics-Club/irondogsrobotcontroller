package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;

/**
 * TEST-ONLY helper: reads and changes the drive motors' settings directly
 * (brake mode and encoder mode).
 *
 * Only test OpModes in this folder use it. Real robot code (subsystems and the
 * teleop/auto OpModes) never does, so when testing is done this file can be
 * deleted without touching Drivetrain.
 *
 * Brake mode ("ZeroPowerBehavior") decides what a motor does when its power is 0:
 *   BRAKE = the motor resists turning, so the robot stops quickly and is hard to push
 *   FLOAT = the motor spins freely, so the robot coasts and is easy to push
 *
 * Encoder mode ("RunMode") decides what setPower() means (issue #11, decision 008):
 *   RUN_WITHOUT_ENCODER = power is a % of battery voltage
 *   RUN_USING_ENCODER   = power is a % of max speed; the hub uses the encoders to hold that speed
 */
public class TestDriveMotors {

    private static final String[] DRIVE_MOTOR_NAMES = {
            Drivetrain.FRONT_LEFT_DRIVE_NAME,
            Drivetrain.FRONT_RIGHT_DRIVE_NAME,
            Drivetrain.BACK_LEFT_DRIVE_NAME,
            Drivetrain.BACK_RIGHT_DRIVE_NAME,
    };

    /** The brake mode the drive motors have right now (reads the front-left motor). */
    public static DcMotor.ZeroPowerBehavior getBrakeMode(HardwareMap hardwareMap) {
        return hardwareMap.get(DcMotor.class, Drivetrain.FRONT_LEFT_DRIVE_NAME).getZeroPowerBehavior();
    }

    /** true = BRAKE, false = FLOAT, for all four drive motors. */
    public static void setBrake(HardwareMap hardwareMap, boolean brake) {
        DcMotor.ZeroPowerBehavior behavior =
                brake ? DcMotor.ZeroPowerBehavior.BRAKE : DcMotor.ZeroPowerBehavior.FLOAT;
        for (String name : DRIVE_MOTOR_NAMES) {
            hardwareMap.get(DcMotor.class, name).setZeroPowerBehavior(behavior);
        }
    }

    /** true = RUN_USING_ENCODER, false = RUN_WITHOUT_ENCODER, for all four drive motors. */
    public static void setUseEncoders(HardwareMap hardwareMap, boolean useEncoders) {
        DcMotor.RunMode mode =
                useEncoders ? DcMotor.RunMode.RUN_USING_ENCODER : DcMotor.RunMode.RUN_WITHOUT_ENCODER;
        for (String name : DRIVE_MOTOR_NAMES) {
            hardwareMap.get(DcMotor.class, name).setMode(mode);
        }
    }

    /** The encoder mode the drive motors have right now (reads the front-left motor). */
    public static DcMotor.RunMode getRunMode(HardwareMap hardwareMap) {
        return hardwareMap.get(DcMotor.class, Drivetrain.FRONT_LEFT_DRIVE_NAME).getMode();
    }

    /** Average speed of the four drive wheels, in encoder ticks per second (always positive). */
    public static double getAverageWheelSpeed(HardwareMap hardwareMap) {
        double total = 0;
        for (String name : DRIVE_MOTOR_NAMES) {
            total += Math.abs(hardwareMap.get(DcMotorEx.class, name).getVelocity());
        }
        return total / DRIVE_MOTOR_NAMES.length;
    }
}
