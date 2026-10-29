package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

/**
 * Launcher subsystem: scores game elements using a flywheel launcher (issue #5).
 *
 * Used by BOTH TeleOp and Autonomous. This class doesn't know where its
 * commands come from: TeleOp gets them from the gamepad, Autonomous gets them from code.
 *
 * Hardware:
 *   launcherMotor  Flywheel motor: goBILDA 5203 Series Yellow Jacket Motor (DcMotorEx)
 *                  on Expansion Hub (decision 009).
 *                  Config name: "launcherMotor" in Driver Station config.
 *   launcherTriggerServo  POSITIONAL servo that pushes a game element into the flywheel (decision 024).
 *                  Two positions: REST and FIRE. Config type: "Servo". Missing = OFFLINE, no crash.
 */
public class Launcher {

    // Config name (decision 009, hardware-config.md). Must match Driver Station config.
    public static final String FLYWHEEL_NAME = "launcherMotor";
    public static final String TRIGGER_SERVO_NAME = "launcherTriggerServo";

    // Trigger servo positions (0.0 to 1.0). TODO(#5): find these with "Launcher Test" (left bumper).
    public static final double TRIGGER_REST_POSITION = 0.0;
    public static final double TRIGGER_FIRE_POSITION = 0.5;

    // Speed / power constants
    public static final double DEFAULT_SPIN_POWER = 1.0;
    // Motor direction is locked to REVERSED as requested.
    public static final boolean MOTOR_REVERSED = true;

    // Minimum encoder velocity (ticks/sec) to consider flywheel ready (if encoders present)
    public static final double READY_VELOCITY_THRESHOLD = 1000.0;

    private final DcMotorEx launcherMotor;
    private final Servo launcherTriggerServo;  // null if not in the Driver Station config
    private boolean triggerFired = false;
    private double targetPower = 0.0;

    public Launcher(HardwareMap hardwareMap) {
        DcMotorEx motor;
        try {
            motor = hardwareMap.get(DcMotorEx.class, FLYWHEEL_NAME);
            motor.setDirection(MOTOR_REVERSED ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            motor.setPower(0.0);
        } catch (Exception e) {
            motor = null;
        }
        launcherMotor = motor;

        launcherTriggerServo = hardwareMap.tryGet(Servo.class, TRIGGER_SERVO_NAME);
        // We do NOT move the trigger here. INIT must not move anything (game rule G304.H).
    }

    /** Start getting the launcher up to full speed. */
    public void spinUp() {
        spinUp(DEFAULT_SPIN_POWER);
    }

    /** Spin the launcher at a specific power (-1.0 to 1.0). */
    public void spinUp(double power) {
        targetPower = power;
        if (launcherMotor != null) {
            launcherMotor.setPower(power);
        }
    }

    /** Set launcher motor power directly. */
    public void setPower(double power) {
        spinUp(power);
    }

    /** Target power set on the launcher motor. */
    public double getPower() {
        return launcherMotor != null ? launcherMotor.getPower() : targetPower;
    }

    /** Current flywheel velocity in encoder ticks per second (0.0 if not equipped or offline). */
    public double getVelocity() {
        if (launcherMotor != null) {
            try {
                return launcherMotor.getVelocity();
            } catch (Exception e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    /** Current drawn by the launcher motor in Amperes (0.0 if offline/error). */
    public double getCurrentAmps() {
        if (launcherMotor != null) {
            try {
                return launcherMotor.getCurrent(CurrentUnit.AMPS);
            } catch (Exception e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * Launch: move the trigger to FIRE, pushing a game element into the flywheel.
     * Only call this when isReady() is true. Call resetTrigger() afterwards to load the next one.
     */
    public void fire() {
        setTrigger(true);
    }

    /** Move the trigger back to REST, out of the way of the next game element. */
    public void resetTrigger() {
        setTrigger(false);
    }

    /** True if the trigger is at FIRE. */
    public boolean isTriggerFired() {
        return triggerFired;
    }

    private void setTrigger(boolean fire) {
        triggerFired = fire;
        if (launcherTriggerServo != null) {
            launcherTriggerServo.setPosition(fire ? TRIGGER_FIRE_POSITION : TRIGGER_REST_POSITION);
        }
    }

    /** Stop the launcher flywheel. */
    public void stop() {
        spinUp(0.0);
    }

    /** True when the launcher is spinning and ready to fire. */
    public boolean isReady() {
        if (launcherMotor == null) {
            return false;
        }
        return Math.abs(getPower()) > 0.05 || Math.abs(getVelocity()) >= READY_VELOCITY_THRESHOLD;
    }

    /** True if the launcher motor was found in the Driver Station configuration. */
    public boolean isConnected() {
        return launcherMotor != null;
    }

    /** Show this subsystem's status on the Driver Station. */
    public void addTelemetry(Telemetry telemetry) {
        if (launcherMotor != null) {
            telemetry.addData("Launcher Status", isReady() ? "READY (Spinning)" : (getPower() != 0 ? "SPINNING" : "STOPPED"));
            telemetry.addData("Launcher Power", "%.2f", getPower());
            telemetry.addData("Launcher Velocity", "%.0f ticks/s", getVelocity());
            telemetry.addData("Launcher Current", "%.2f A", getCurrentAmps());
        } else {
            telemetry.addData("Launcher", "OFFLINE (device '%s' not in DS config)", FLYWHEEL_NAME);
        }
        if (launcherTriggerServo != null) {
            telemetry.addData("Launcher trigger", triggerFired ? "FIRE" : "rest");
        } else {
            telemetry.addData("Launcher trigger", "OFFLINE ('%s' not in DS config)", TRIGGER_SERVO_NAME);
        }
    }
}
