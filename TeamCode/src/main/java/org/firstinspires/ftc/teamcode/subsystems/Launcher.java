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
 *   launcherTriggerServo  POSITIONAL servo that works as a GATE in front of the flywheel (decisions 024, 029).
 *                  REST = in the way, so balls can't reach the flywheel. FIRE = moved clear, so the
 *                  water wheel (Intake) can feed balls through. The operator holds Y to launch.
 *                  Config type: "Servo". Missing = OFFLINE, no crash.
 *
 * Speed control (decision 032): the flywheel runs in RUN_WITHOUT_ENCODER at a fixed POWER (DEFAULT_SPIN_POWER),
 * not at a set velocity. Simpler, and it runs the whole match so it has time to settle. The encoder is still
 * read so isReady() and telemetry can show the real speed.
 */
public class Launcher {

    // Config name (decision 009, hardware-config.md). Must match Driver Station config.
    public static final String FLYWHEEL_NAME = "launcherMotor";
    public static final String TRIGGER_SERVO_NAME = "launcherTriggerServo";

    // Trigger (gate) servo positions (0.0 to 1.0). REST blocks the ball path, FIRE opens it.
    // TODO(#5): find these with "Launcher Test" (hold left bumper = FIRE).
    public static final double TRIGGER_REST_POSITION = 0.0;
    public static final double TRIGGER_FIRE_POSITION = 0.5;

    // Flywheel power (0 to 1), decision 032. TODO(#5): tune with "Launcher Test" and a hit-rate test.
    public static final double DEFAULT_SPIN_POWER = 1.0;
    // TODO(#5): confirm on the robot. true = the flywheel spins the right way with REVERSE set.
    public static final boolean MOTOR_REVERSED = true;

    // The flywheel counts as READY once the encoder reads at least this many ticks per second.
    // TODO(#5): run "Launcher Test" at DEFAULT_SPIN_POWER, read "Launcher Velocity" once it settles,
    // and set this to about 90% of that number. 1000 is a placeholder.
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
     * Open the gate: move the trigger to FIRE, out of the ball path, so the water wheel can feed
     * balls into the flywheel. Best when isReady() is true. Call resetTrigger() to close it again.
     */
    public void fire() {
        setTrigger(true);
    }

    /** Close the gate: move the trigger back to REST, blocking the ball path. */
    public void resetTrigger() {
        setTrigger(false);
    }

    /** True if the trigger is at FIRE (gate open). */
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

    /**
     * True when the flywheel is really up to speed: the encoder reads at least READY_VELOCITY_THRESHOLD.
     * Power alone doesn't count, because a motor at full power is still slow for the first moment,
     * and slows down again right after each ball.
     */
    public boolean isReady() {
        if (launcherMotor == null) {
            return false;
        }
        return Math.abs(getVelocity()) >= READY_VELOCITY_THRESHOLD;
    }

    /** True if the launcher motor was found in the Driver Station configuration. */
    public boolean isConnected() {
        return launcherMotor != null;
    }

    /** Show this subsystem's status on the Driver Station. */
    public void addTelemetry(Telemetry telemetry) {
        if (launcherMotor != null) {
            telemetry.addData("Launcher Status", isReady() ? "READY" : (getPower() != 0 ? "SPINNING UP (not ready)" : "STOPPED"));
            telemetry.addData("Launcher Power", "%.2f", getPower());
            telemetry.addData("Launcher Velocity", "%.0f ticks/s", getVelocity());
            telemetry.addData("Launcher Current", "%.2f A", getCurrentAmps());
        } else {
            telemetry.addData("Launcher", "OFFLINE (device '%s' not in DS config)", FLYWHEEL_NAME);
        }
        if (launcherTriggerServo != null) {
            telemetry.addData("Launcher trigger", triggerFired ? "FIRE (gate open)" : "rest (gate closed)");
        } else {
            telemetry.addData("Launcher trigger", "OFFLINE ('%s' not in DS config)", TRIGGER_SERVO_NAME);
        }
    }
}
