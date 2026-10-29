package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Intake subsystem: picks up game elements and loads them toward the launcher (issue #4).
 *
 * Used by BOTH TeleOp and Autonomous. This class doesn't know where its commands come from.
 *
 * Hardware (decision 024 names):
 *   intakeServo        CONTINUOUS servo: pulls game elements into the robot
 *   intakeLoaderServo  CONTINUOUS servo: moves game elements up to the launcher
 *   (2 goBILDA micro limit switches will be added once their job is decided, see #4)
 *
 * Driver Station config type for both: "Continuous Rotation Servo".
 * A device that isn't in the config shows OFFLINE instead of crashing every OpMode.
 */
public class Intake {

    // Config names (decisions 009, 024). Must match the Driver Station robot configuration.
    public static final String INTAKE_SERVO_NAME = "intakeServo";
    public static final String LOADER_SERVO_NAME = "intakeLoaderServo";

    // Speeds, from 0 to 1. TODO(#4): tune on the robot with "Intake Test".
    public static final double INTAKE_SPEED = 1.0;
    public static final double LOADER_SPEED = 1.0;

    // TODO(#4): set to true if "in" / "load" spins the wrong way.
    public static final boolean INTAKE_REVERSED = false;
    public static final boolean LOADER_REVERSED = false;

    private final CRServo intakeServo;        // null if not in the Driver Station config
    private final CRServo intakeLoaderServo;  // null if not in the Driver Station config
    private double intakeSpeed = 0.0;
    private double loaderSpeed = 0.0;

    public Intake(HardwareMap hardwareMap) {
        intakeServo = hardwareMap.tryGet(CRServo.class, INTAKE_SERVO_NAME);
        intakeLoaderServo = hardwareMap.tryGet(CRServo.class, LOADER_SERVO_NAME);

        if (intakeServo != null) {
            intakeServo.setDirection(INTAKE_REVERSED ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
        }
        if (intakeLoaderServo != null) {
            intakeLoaderServo.setDirection(LOADER_REVERSED ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
        }
        // Nothing spins until the first command (game rule G304.H: INIT must not move).
    }

    /** Run the intake to pull game elements in. */
    public void in() {
        setIntakeSpeed(INTAKE_SPEED);
    }

    /** Run the intake backwards to push game elements out. */
    public void out() {
        setIntakeSpeed(-INTAKE_SPEED);
    }

    /** Run the loader to move game elements UP to the launcher. */
    public void load() {
        setLoaderSpeed(LOADER_SPEED);
    }

    /** Run the loader backwards, to clear a jam. */
    public void unload() {
        setLoaderSpeed(-LOADER_SPEED);
    }

    /** Stop only the intake (the loader keeps doing what it was doing). */
    public void stopIntake() {
        setIntakeSpeed(0.0);
    }

    /** Stop only the loader. */
    public void stopLoader() {
        setLoaderSpeed(0.0);
    }

    /** Stop the intake AND the loader. */
    public void stop() {
        setIntakeSpeed(0.0);
        setLoaderSpeed(0.0);
    }

    private void setIntakeSpeed(double speed) {
        intakeSpeed = speed;
        if (intakeServo != null) {
            intakeServo.setPower(speed);
        }
    }

    private void setLoaderSpeed(double speed) {
        loaderSpeed = speed;
        if (intakeLoaderServo != null) {
            intakeLoaderServo.setPower(speed);
        }
    }

    /** Show this subsystem's status on the Driver Station. */
    public void addTelemetry(Telemetry telemetry) {
        telemetry.addData("Intake", intakeServo != null ? String.format("%.2f", intakeSpeed)
                : "OFFLINE ('" + INTAKE_SERVO_NAME + "' not in DS config)");
        telemetry.addData("Intake loader", intakeLoaderServo != null ? String.format("%.2f", loaderSpeed)
                : "OFFLINE ('" + LOADER_SERVO_NAME + "' not in DS config)");
    }
}
