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
 * Speed control, two ways (USE_VELOCITY_CONTROL picks):
 *   false = fixed POWER (decision 032): RUN_WITHOUT_ENCODER at DEFAULT_SPIN_POWER. Simple, but the flywheel
 *           spins slower on a tired battery and right after each ball.
 *   true  = steady SPEED (decision 071): the hub holds TARGET_RPM with its PIDF controller, using the encoder.
 *           Same speed every shot, whatever the battery. Needs the encoder working and the gains tuned.
 * Either way the encoder is read so isReady() and telemetry can show the real speed, in RPM (turns per minute)
 * of the motor shaft.
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
    public static final double DEFAULT_SPIN_POWER = 0.70;
    // Note 11/8: 1.0 after testing. Was 0.45 on 11/7 (decision 041) and 0.8 on 11/6.
    // Note 11/6: tuned to 0.8, needs to be checked again once we are actually able to launch a ball
    // Note 11/14: tuned to 0.7 after noticing consistent overshooting in our competition matches
    // Note 11/15: tuned to 0.83 after further testing and finding the sweet spot arc
    // TODO(#5): confirm on the robot. true = the flywheel spins the right way with REVERSE set.
    public static final boolean MOTOR_REVERSED = true;

    // Encoder ticks per turn of the motor's output shaft, to turn the encoder reading into RPM.
    // It depends on the motor's gearbox (goBILDA 5203 Yellow Jacket, the RPM is on its label):
    //   6000 RPM (1:1) = 28.0    1620 RPM (3.7:1) = 103.8    1150 RPM (5.2:1) = 145.1    435 RPM (13.7:1) = 384.5
    // Checked 11/23: the flywheel motor is a 5203-2402-0001 = 6000 RPM, no gearbox (1:1), so 28.0.
    public static final double ENCODER_TICKS_PER_REV = 28.0;

    // The flywheel counts as READY once it spins at least this fast (RPM). About 95% of TARGET_RPM.
    // Note 11/26: 3040 = 95% of 3200. Was a 2000 placeholder. Change it together with TARGET_RPM.
    public static final double READY_RPM = 3040.0;

    // Steady speed (decision 071). Tuned in "Launcher Test" VELOCITY mode (tuning guide, Launcher section).
    // false = back to fixed power (DEFAULT_SPIN_POWER), e.g. if the flywheel encoder stops working.
    // Note 11/26: switched ON after tuning. The flywheel encoder cable needed its A and B wires swapped first:
    // a backwards encoder makes P push the speed ABOVE the target.
    public static final boolean USE_VELOCITY_CONTROL = true;
    // The speed the hub holds (RPM). Note 11/26: 3200 = about what 70% power gave (3171-3214).
    // Landings looked consistent by eye; TODO(#5): hit-rate test on the field (Sunday 11/29).
    // Shots short with the speed steady? Raise this (e.g. 3300, with F about 16) and READY_RPM with it.
    public static final double TARGET_RPM = 3200.0;
    // The hub's PIDF gains for speed. F does most of the work, P brings the speed back after a shot,
    // I and D stay 0 for our flywheel.
    // Note 11/26: F 15.5 holds 3200 alone (Launcher Test's "F estimate" said 15.3; about 16 holds 3300).
    // P: 0 -> lowest 3000 during 4 balls, 10 -> 3100, 20 -> 3130, 30 -> about the same, so 20.
    // (Fixed power 70% dropped to 2914.) The speed reading jumps about 40 RPM at rest: encoder noise, not P.
    public static final double VELOCITY_P = 20.0;
    public static final double VELOCITY_I = 0.0;
    public static final double VELOCITY_D = 0.0;
    public static final double VELOCITY_F = 15.5;

    private final DcMotorEx launcherMotor;
    private final Servo launcherTriggerServo;  // null if not in the Driver Station config
    private boolean triggerFired = false;
    private double targetPower = 0.0;
    private boolean velocityMode = false;   // true while holding a speed (spinAtRpm)
    private double targetRpm = 0.0;

    public Launcher(HardwareMap hardwareMap) {
        DcMotorEx motor;
        try {
            motor = hardwareMap.get(DcMotorEx.class, FLYWHEEL_NAME);
            motor.setDirection(MOTOR_REVERSED ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            motor.setPower(0.0);
            if (USE_VELOCITY_CONTROL) {
                // Give the hub our gains now (nothing moves). Launcher Test starts from these too.
                motor.setVelocityPIDFCoefficients(VELOCITY_P, VELOCITY_I, VELOCITY_D, VELOCITY_F);
            }
        } catch (Exception e) {
            motor = null;
        }
        launcherMotor = motor;

        launcherTriggerServo = hardwareMap.tryGet(Servo.class, TRIGGER_SERVO_NAME);
        // We do NOT move the trigger here. INIT must not move anything (game rule G304.H).
    }

    /**
     * Start the flywheel for shooting: steady TARGET_RPM if USE_VELOCITY_CONTROL is on, otherwise DEFAULT_SPIN_POWER.
     * Main TeleOp and Shoot Auton use this one, so the switch changes both.
     */
    public void spinUp() {
        if (USE_VELOCITY_CONTROL) {
            spinAtRpm(TARGET_RPM);
        } else {
            spinUp(DEFAULT_SPIN_POWER);
        }
    }

    /** Spin the launcher at a specific POWER (-1.0 to 1.0), no speed control. */
    public void spinUp(double power) {
        velocityMode = false;
        targetPower = power;
        if (launcherMotor != null) {
            if (launcherMotor.getMode() != DcMotor.RunMode.RUN_WITHOUT_ENCODER) {
                launcherMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            }
            launcherMotor.setPower(power);
        }
    }

    /** Hold a steady speed (RPM) with the hub's PIDF controller and the encoder (decision 071). */
    public void spinAtRpm(double rpm) {
        velocityMode = true;
        targetRpm = rpm;
        if (launcherMotor != null) {
            if (launcherMotor.getMode() != DcMotor.RunMode.RUN_USING_ENCODER) {
                launcherMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            }
            // The hub works in ticks per second: RPM / 60 seconds x ticks per turn
            launcherMotor.setVelocity(rpm / 60.0 * ENCODER_TICKS_PER_REV);
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

    /**
     * Stop the launcher flywheel. Back to plain power 0, so it COASTS to a stop (FLOAT). Holding a speed of 0
     * instead would make the hub brake the heavy flywheel hard.
     */
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

    /** Show this subsystem's status on the Driver Station (tests): the match lines + the encoder cable warning. */
    public void addTelemetry(Telemetry telemetry) {
        addMatchTelemetry(telemetry);
        if (launcherMotor != null
                && (velocityMode ? targetRpm > 0 : Math.abs(getPower()) > 0.2) && getRpm() == 0.0) {
            // Powered but EXACTLY 0: the hub isn't getting encoder pulses. The speed can't be measured.
            telemetry.addLine("  ⚠️ Speed 0 while powered: check the flywheel ENCODER cable (same port number as the motor)");
        }
    }

    /** The short version for Main TeleOp's match screen: status, speed, current, trigger. */
    public void addMatchTelemetry(Telemetry telemetry) {
        if (launcherMotor != null) {
            boolean spinning = velocityMode ? targetRpm != 0 : getPower() != 0;
            telemetry.addData("Launcher Status", isReady() ? "READY" : (spinning ? "SPINNING UP (not ready)" : "STOPPED"));
            if (velocityMode) {
                telemetry.addData("Launcher Speed", "%.0f RPM (holding %.0f)", getRpm(), targetRpm);
            } else {
                telemetry.addData("Launcher Power", "%.2f", getPower());
                telemetry.addData("Launcher Speed", "%.0f RPM", getRpm());
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
