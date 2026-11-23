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
 * read so isReady() and telemetry can show the real speed, in RPM (turns per minute) of the motor shaft.
 */
public class Launcher {

    // Config name (decision 009, hardware-config.md). Must match Driver Station config.
    public static final String FLYWHEEL_NAME = "launcherMotor";
    public static final String TRIGGER_SERVO_NAME = "launcherTriggerServo";

    // Trigger (gate) servo positions (0.0 to 1.0). REST blocks the ball path, FIRE opens it.
    // TODO(#5): find these with "Launcher Test" (hold left bumper = FIRE).
    // Note 11/6: changed firing position to move farther back
    public static final double TRIGGER_REST_POSITION = 0.0;
    public static final double TRIGGER_FIRE_POSITION = 1.0;

    // Flywheel power (0 to 1), decision 032. TODO(#5): tune with "Launcher Test" and a hit-rate test.
    public static final double DEFAULT_SPIN_POWER = 0.83;
    // Note 11/8: 1.0 after testing. Was 0.45 on 11/7 (decision 041) and 0.8 on 11/6.
    // Note 11/6: tuned to 0.8, needs to be checked again once we are actually able to launch a ball
    // Note 11/14: tuned to 0.7 after noticing consistent overshooting in our competition matches
    // Note 11/15: tuned to 0.83 after further testing and finding the sweet spot arc
    // TODO(#5): confirm on the robot. true = the flywheel spins the right way with REVERSE set.
    public static final boolean MOTOR_REVERSED = true;

    // Encoder ticks per turn of the motor's output shaft, to turn the encoder reading into RPM.
    // It depends on the motor's gearbox (goBILDA 5203 Yellow Jacket, the RPM is on its label):
    //   6000 RPM (1:1) = 28.0    1620 RPM (3.7:1) = 103.8    1150 RPM (5.2:1) = 145.1    435 RPM (13.7:1) = 384.5
    // TODO(#5): 28.0 (6000 RPM) is a PLACEHOLDER, the motor isn't known yet. Read the label and fix it.
    public static final double ENCODER_TICKS_PER_REV = 28.0;

    // The flywheel counts as READY once it spins at least this fast (RPM).
    // TODO(#5): run "Launcher Test" at DEFAULT_SPIN_POWER, read "Launcher Speed" once it settles,
    // and set this to about 90% of that number. 2000 is a placeholder (was 1000 ticks/s, about 2140 RPM).
    public static final double READY_RPM = 2000.0;

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

    /** Current flywheel speed in RPM (turns per minute), 0.0 if offline. Negative = spinning backwards. */
    public double getRpm() {
        if (launcherMotor != null) {
            try {
                // The hub measures ticks per second; 60 seconds per minute, ENCODER_TICKS_PER_REV ticks per turn
                return launcherMotor.getVelocity() * 60.0 / ENCODER_TICKS_PER_REV;
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
     * True when the flywheel is really up to speed: at least READY_RPM.
     * Power alone doesn't count, because a motor at full power is still slow for the first moment,
     * and slows down again right after each ball.
     */
    public boolean isReady() {
        if (launcherMotor == null) {
            return false;
        }
        return Math.abs(getRpm()) >= READY_RPM;
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
            telemetry.addData("Launcher Speed", "%.0f RPM", getRpm());
            if (Math.abs(getPower()) > 0.2 && getRpm() == 0.0) {
                // Powered but EXACTLY 0: the hub isn't getting encoder pulses. The speed can't be measured.
                telemetry.addLine("  ⚠️ Speed 0 while powered: check the flywheel ENCODER cable (same port number as the motor)");
            }
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
