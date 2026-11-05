package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Turret subsystem: turns the launcher (flywheel) and the camera left and right, like a tank turret
 * (issue #31, decisions 016, 017, 019, 021, 033).
 *
 * Hardware:
 *   turretRotateServo          CONTINUOUS ROTATION servo (decision 021). We tell it a SPEED (-1 to +1), not a position.
 *                        goBILDA 2000 Series Dual Mode Servo, 25-3 Speed (2000-0025-0003), set to CONTINUOUS
 *                        mode with the goBILDA Servo Programmer.
 *                        Driver Station config type: "Continuous Rotation Servo".
 *   turretEncoder        ELC Encoder V2, "D" (quadrature) connector, on an Expansion Hub MOTOR ENCODER port
 *                        (decision 019). This is how the turret knows where it is.
 *   turretHomeSwitch     REV Magnetic Limit Switch: our HOME sensor (decisions 017, 033). It is NOT a stop.
 *                        The turret is mounted reversed, so the switch triggers when the turret faces the REAR
 *                        (HOME_ANGLE_DEGREES = 180). Was called turretForwardSwitch.
 *   turretHoodServo      POSITIONAL servo: tilts the hood to aim the launch UP or DOWN (decision 024).
 *                        Optional: if it's not in the config, the turret still works and the hood shows OFFLINE.
 * Turning needs ALL THREE of turretRotateServo, turretEncoder and turretHomeSwitch. If any is missing from
 * the config, the turret shows OFFLINE and does nothing (no crash), so Main TeleOp still runs on any robot.
 *
 * How it works:
 *   - A continuous servo doesn't know its own angle, so OUR CODE holds the angle using the encoder.
 *     Call update() EVERY loop. It compares where the turret IS (encoder) with where we WANT it (target),
 *     and spins the servo faster when far away and slower when close ("proportional control").
 *   - HOMING: the encoder counts from 0 at power-on. The first time the home switch triggers, that spot
 *     becomes HOME_ANGLE_DEGREES (180 = facing the REAR). Before a match, point the turret at the rear
 *     (the light turns GREEN in Turret Test) and it homes during INIT.
 *   - Until it's homed, turnTo() does nothing and manual moves are SLOW. After homing, nothing moves past
 *     MAX_LEFT / MAX_RIGHT. Nothing physical stops the turret, so these limits matter!
 *
 * Angles use our direction convention (decision 007), measured from the ROBOT's front:
 *   0 degrees = turret pointing straight FORWARD.   + = LEFT (counter-clockwise),  - = RIGHT.   180 = REAR.
 *   Angles keep counting past 180 (they don't wrap around), so turning left from the rear goes 180, 200, 270...
 *   turnTo() picks whichever way of writing the angle is inside the limits: turnTo(-90) can mean 270.
 *
 * Calibrate the TODO constants with the "Turret Test" OpMode, IN ORDER (issue #31):
 *   1. SERVO_REVERSED   2. ENCODER_REVERSED   3. ENCODER_REVS_PER_TURRET_REV   4. limits   5. AIM_GAIN
 */
public class Turret {

    // Config names (decision 009). Must match the Driver Station robot configuration.
    public static final String ROTATE_SERVO_NAME = "turretRotateServo";
    public static final String HOOD_SERVO_NAME = "turretHoodServo";
    public static final String ENCODER_NAME = "turretEncoder";
    public static final String HOME_SWITCH_NAME = "turretHomeSwitch";

    // The servo's signal range in microseconds, in CONTINUOUS mode. goBILDA 2000 series: 1000-2000.
    // 1500 = stop, 1000 = full speed one way, 2000 = full speed the other way. With this range,
    // a speed of 1.0 really is full speed. CHECK THIS IF WE CHANGE SERVOS (Axon MAX used 1050-1950).
    public static final double SERVO_PWM_MIN_US = 1000;
    public static final double SERVO_PWM_MAX_US = 2000;

    // TODO(#31) step 1: set to true if a POSITIVE speed turns the turret RIGHT (it should turn LEFT).
    public static final boolean SERVO_REVERSED = false;

    // ELC Encoder V2 data sheet: 4000 counts per revolution.
    public static final double ENCODER_COUNTS_PER_REV = 4000.0;
    // TODO(#31) step 2: set to true if "Turret angle" goes DOWN when the turret turns LEFT.
    public static final boolean ENCODER_REVERSED = false;
    // TODO(#31) step 3: encoder turns per ONE turret turn (the gear ratio between the servo and the turret).
    // The encoder is on the servo. Example: servo gear 20 teeth, turret gear 100 teeth = 100 / 20 = 5.0
    public static final double ENCODER_REVS_PER_TURRET_REV = 1.0;

    // Where the turret points when the home switch triggers (decision 033). The turret is mounted reversed,
    // so the magnet is at the REAR: 180. If the turret is remounted, change only this number.
    public static final double HOME_ANGLE_DEGREES = 180.0;

    // TODO(#31) step 4: how far the turret may turn each way. Nothing physical stops the turret,
    // so these keep it from turning too far (into wires or the frame).
    // PLACEHOLDERS (11/5, not measured yet): the rear +/- 90 degrees. From pointing LEFT (90), around the
    // back (180), to pointing RIGHT (270, the same direction as -90). Same amount of turning as before
    // (180 degrees in all), centered on the switch. With these, the turret can't point forward yet.
    // MAX_LEFT must be bigger than HOME_ANGLE_DEGREES and MAX_RIGHT smaller, or the turret can't home.
    public static final double MAX_LEFT_DEGREES = 270.0;
    public static final double MAX_RIGHT_DEGREES = 90.0;

    // TODO(#31) step 5: tune the aiming. Speed = AIM_GAIN x (degrees away from the target).
    // Example: 0.02 means 10 degrees away = speed 0.2. Bigger = faster but may wobble past the target.
    public static final double AIM_GAIN = 0.02;
    public static final double MAX_SPEED = 0.6;            // fastest the turret is allowed to turn (0 to 1)
    public static final double TOLERANCE_DEGREES = 1.0;    // "close enough" to the target: stop here
    public static final double NOT_HOMED_MAX_SPEED = 0.35; // slower, for moving before the turret is homed
                                                           // (0.15 was too slow to move the Axon turret, 10/15)

    // Safety: if the turret ends up this much FARTHER from the target than when it started,
    // something is backwards (step 1 or 2), so we stop instead of spinning away.
    public static final double RUNAWAY_DEGREES = 20.0;

    // Hood (positional servo, 0.0 to 1.0). TODO(#31): find these with the hood controls in "Turret Test".
    // Keep the hood between these so it can't press into its hard stops.
    public static final double HOOD_LOWEST_POSITION = 0.0;   // flattest launch
    public static final double HOOD_HIGHEST_POSITION = 1.0;  // steepest launch
    public static final double HOOD_START_POSITION = 0.5;    // a safe middle spot (Turret Test starts here after START)

    // TODO(#31): check with "Turret Test". REV's switch reads false when a magnet is near (active low).
    public static final boolean HOME_SWITCH_TRIGGERED_STATE = false;

    // "At home" zone: once homed, the turret counts as HOME (light GREEN) when the encoder is within
    // this many counts of the home switch spot. The magnet spot alone was too narrow to return to exactly.
    // 200 counts = 18 degrees of ENCODER turn (4000 counts = 360). With gears, less at the turret.
    // TODO(#31): tune. Smaller = stricter "home".
    public static final int HOME_TOLERANCE_COUNTS = 200;

    private final CRServoImplEx turretRotateServo;  // null if not in the Driver Station config
    // Only used to READ the encoder port. We never give it power.
    private final DcMotorEx turretEncoder;           // null if not in the Driver Station config
    private final DigitalChannel turretHomeSwitch;    // null if not in the Driver Station config
    private final boolean connected;                 // true only if all three are in the config
    private final Servo turretHoodServo;   // null if not in the Driver Station config
    private double hoodPosition = Double.NaN; // NaN until the first hood command

    // Homing: the encoder count when the turret was at the home switch (HOME_ANGLE_DEGREES).
    private boolean homed = false;
    private int homeCounts = 0;

    // What we've been asked to do. A TARGET angle (turnTo) or a MANUAL speed (setManualSpeed).
    private double targetDegrees = Double.NaN;  // NaN = no target
    private double manualSpeed = 0.0;
    private double startErrorDegrees = 0.0;
    private boolean runawayStopped = false;
    private boolean commanded = false;          // false until the first command: never move during INIT
    private double speedSent = 0.0;

    public Turret(HardwareMap hardwareMap) {
        turretRotateServo = hardwareMap.tryGet(CRServoImplEx.class, ROTATE_SERVO_NAME);
        turretEncoder = hardwareMap.tryGet(DcMotorEx.class, ENCODER_NAME);
        turretHomeSwitch = hardwareMap.tryGet(DigitalChannel.class, HOME_SWITCH_NAME);
        turretHoodServo = hardwareMap.tryGet(Servo.class, HOOD_SERVO_NAME);
        connected = turretRotateServo != null && turretEncoder != null && turretHomeSwitch != null;

        if (turretHomeSwitch != null) {
            turretHomeSwitch.setMode(DigitalChannel.Mode.INPUT);
        }
        if (turretRotateServo != null) {
            turretRotateServo.setDirection(SERVO_REVERSED ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
            // Only sets the signal range. Nothing moves until the first command (game rule G304.H: INIT must not move).
            turretRotateServo.setPwmRange(new PwmControl.PwmRange(SERVO_PWM_MIN_US, SERVO_PWM_MAX_US));
        }
    }

    /** True if the rotate servo, encoder and home switch are all in the Driver Station config. */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Call this ONCE EVERY LOOP, including during INIT.
     * It homes the encoder at the home switch, and spins the servo toward the target.
     */
    public void update() {
        if (!connected) {
            return; // OFFLINE: nothing to do
        }
        if (!homed && isAtHomeSwitch()) {
            homeCounts = turretEncoder.getCurrentPosition();
            homed = true;
        }

        if (!commanded) {
            return; // nothing asked yet: send nothing, so the turret can't move during INIT
        }

        double speed;
        if (!Double.isNaN(targetDegrees)) {
            speed = speedToTarget();
        } else {
            speed = manualSpeed;
            if (!homed) {
                speed = clamp(speed, NOT_HOMED_MAX_SPEED);
            }
        }

        // Never go past the limits (once homed, we know where they are).
        if (homed && speed > 0 && getAngle() >= MAX_LEFT_DEGREES) {
            speed = 0.0;
        }
        if (homed && speed < 0 && getAngle() <= MAX_RIGHT_DEGREES) {
            speed = 0.0;
        }

        speedSent = speed;
        turretRotateServo.setPower(speed);
    }

    /** Proportional control: faster when far from the target, slower when close, stop when close enough. */
    private double speedToTarget() {
        double error = targetDegrees - getAngle(); // + = target is to the LEFT
        if (Math.abs(error) > Math.abs(startErrorDegrees) + RUNAWAY_DEGREES) {
            runawayStopped = true;
            targetDegrees = Double.NaN;
            manualSpeed = 0.0;
            return 0.0;
        }
        if (Math.abs(error) < TOLERANCE_DEGREES) {
            return 0.0;
        }
        return clamp(AIM_GAIN * error, MAX_SPEED);
    }

    /**
     * Turn the turret to an angle (0 = forward, + = left, 180 = rear) and hold it there. Only works once homed.
     * The same direction can be written two ways (-90 = 270), so we use the one inside the limits.
     * If it's outside the limits both ways, the turret goes to whichever limit points closest to it.
     */
    public void turnTo(double degrees) {
        commanded = true;
        if (!homed) {
            return;
        }
        while (degrees < MAX_RIGHT_DEGREES && degrees + 360.0 <= MAX_LEFT_DEGREES) {
            degrees += 360.0;
        }
        while (degrees > MAX_LEFT_DEGREES && degrees - 360.0 >= MAX_RIGHT_DEGREES) {
            degrees -= 360.0;
        }
        if (degrees < MAX_RIGHT_DEGREES || degrees > MAX_LEFT_DEGREES) {
            // Still outside: can't get there. Stop at the limit that points closest to it.
            boolean leftIsCloser = degreesApart(degrees, MAX_LEFT_DEGREES) < degreesApart(degrees, MAX_RIGHT_DEGREES);
            degrees = leftIsCloser ? MAX_LEFT_DEGREES : MAX_RIGHT_DEGREES;
        }
        targetDegrees = degrees;
        startErrorDegrees = targetDegrees - getAngle();
        runawayStopped = false;
    }

    /** Point the turret straight forward (as far as the limits allow). */
    public void turnForward() {
        turnTo(0.0);
    }

    /**
     * Turn at a speed from -1 (right) to +1 (left), for driver control. 0 = stop.
     * This cancels any turnTo() target. Before homing, it is limited to a slow speed.
     */
    public void setManualSpeed(double speed) {
        commanded = true;
        targetDegrees = Double.NaN;
        manualSpeed = clamp(speed, 1.0);
    }

    /**
     * Move the hood to a position from HOOD_LOWEST_POSITION (flat) to HOOD_HIGHEST_POSITION (steep).
     * Call after START, never during INIT.
     */
    public void setHoodPosition(double position) {
        hoodPosition = Math.max(HOOD_LOWEST_POSITION, Math.min(HOOD_HIGHEST_POSITION, position));
        if (turretHoodServo != null) {
            turretHoodServo.setPosition(hoodPosition);
        }
    }

    /** The hood position we last sent (NaN if we haven't moved it yet). */
    public double getHoodPosition() {
        return hoodPosition;
    }

    /** True if the hood servo was found in the Driver Station config. */
    public boolean isHoodConnected() {
        return turretHoodServo != null;
    }

    /** Stop turning and forget the target. The hood stays where it is. */
    public void stop() {
        targetDegrees = Double.NaN;
        manualSpeed = 0.0;
        speedSent = 0.0;
        if (turretRotateServo != null) {
            turretRotateServo.setPower(0.0);
        }
    }

    /**
     * True when the turret is at HOME (the switch, now at the REAR): the switch is triggered, or (once homed)
     * the encoder is within HOME_TOLERANCE_COUNTS of the switch spot.
     * The OpMode can show this on the light: light.setColor(GREEN) (decision 018).
     */
    public boolean isAtHome() {
        return isAtHomeSwitch() || (homed && Math.abs(getCountsFromHome()) <= HOME_TOLERANCE_COUNTS);
    }

    /** Encoder counts away from the home switch spot (+ or -). 0 until homed. */
    public int getCountsFromHome() {
        return homed ? getEncoderCounts() - homeCounts : 0;
    }

    /** True once the encoder has been homed (the home switch has triggered at least once). */
    public boolean isHomed() {
        return homed;
    }

    /** True when there is a target and the turret is within TOLERANCE_DEGREES of it. */
    public boolean isAtTarget() {
        return !Double.isNaN(targetDegrees) && Math.abs(targetDegrees - getAngle()) < TOLERANCE_DEGREES;
    }

    /** True if the runaway safety stopped the turret. Check SERVO_REVERSED and ENCODER_REVERSED. */
    public boolean wasRunawayStopped() {
        return runawayStopped;
    }

    /** The real turret angle from the encoder, in degrees (0 = forward, + = left, 180 = rear). NaN until homed. */
    public double getAngle() {
        if (!homed) {
            return Double.NaN;
        }
        double encoderRevs = (getEncoderCounts() - homeCounts) / ENCODER_COUNTS_PER_REV;
        double degrees = encoderRevs / ENCODER_REVS_PER_TURRET_REV * 360.0;
        return HOME_ANGLE_DEGREES + (ENCODER_REVERSED ? -degrees : degrees);
    }

    /** The angle we're turning to (NaN if there's no target). */
    public double getTargetAngle() {
        return targetDegrees;
    }

    /** The speed we last sent to the servo (-1 to +1). */
    public double getSpeed() {
        return speedSent;
    }

    /** Raw encoder count. Used for calibrating. */
    public int getEncoderCounts() {
        return turretEncoder != null ? turretEncoder.getCurrentPosition() : 0;
    }

    /**
     * True when the magnet is at the home switch (the turret faces HOME_ANGLE_DEGREES: the rear).
     * This is the exact magnet spot. For the light, use isAtHome() (a wider zone).
     */
    public boolean isAtHomeSwitch() {
        return turretHomeSwitch != null && turretHomeSwitch.getState() == HOME_SWITCH_TRIGGERED_STATE;
    }

    /** How far apart two directions are, the short way around (0 to 180). E.g. -45 and 270 are 45 apart. */
    private static double degreesApart(double a, double b) {
        double difference = Math.abs(a - b) % 360.0;
        return difference > 180.0 ? 360.0 - difference : difference;
    }

    private static double clamp(double value, double limit) {
        return Math.max(-limit, Math.min(limit, value));
    }

    /** Show this subsystem's status on the Driver Station. */
    public void addTelemetry(Telemetry telemetry) {
        if (!connected) {
            telemetry.addData("Turret", "OFFLINE (needs '%s', '%s' and '%s' in DS config)",
                    ROTATE_SERVO_NAME, ENCODER_NAME, HOME_SWITCH_NAME);
            return;
        }
        telemetry.addData("Turret homed", homed ? "yes" : "NO: turn the turret past the home switch (REAR) once (slow until then)");
        telemetry.addData("Turret at home", "%s  (%d counts from home, zone ±%d)",
                isAtHome() ? "YES" : "no", getCountsFromHome(), HOME_TOLERANCE_COUNTS);
        telemetry.addData("Turret angle (°)  +left", homed ? String.format("%.1f", getAngle()) : "unknown");
        telemetry.addData("Turret target (°)", Double.isNaN(targetDegrees) ? "none (manual)" : String.format("%.1f", targetDegrees));
        telemetry.addData("Turret speed sent", "%.2f  (+ = left)", speedSent);
        if (turretHoodServo == null) {
            telemetry.addData("Turret hood", "OFFLINE ('%s' not in DS config)", HOOD_SERVO_NAME);
        } else {
            telemetry.addData("Turret hood", Double.isNaN(hoodPosition) ? "not moved yet" : String.format("%.2f", hoodPosition));
        }
        if (runawayStopped) {
            telemetry.addLine("⚠️ RUNAWAY STOP: turret moved AWAY from the target. Check SERVO_REVERSED / ENCODER_REVERSED");
        }
    }
}
