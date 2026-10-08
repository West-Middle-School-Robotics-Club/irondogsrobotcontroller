package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Turret subsystem: turns the launcher (flywheel) and the camera left and right, like a tank turret
 * (issue #31, decisions 016, 017, 019).
 *
 * Hardware:
 *   turretServo          positional servo that turns the turret
 *   turretEncoder        ELC Encoder V2 on the servo. Use the connector marked "D" (digital = quadrature),
 *                        NOT "A" (analog). Plugged into an Expansion Hub MOTOR ENCODER port (decision 019).
 *                        It counts from 0 at power-on, so it must be HOMED.
 *   turretForwardSwitch  REV Magnetic Limit Switch: tells us when the turret is facing FORWARD (decision 017).
 *                        It is only an indicator, NOT a stop. It is also our HOME sensor.
 *
 * Homing: the encoder doesn't know where the turret is until the forward switch triggers once.
 * Call update() every loop. The first time the switch triggers, that spot becomes 0 degrees.
 * Before a match, set the turret facing forward (the light turns GREEN), and it homes during INIT.
 *
 * Angles use our direction convention (decision 007):
 *   0 degrees = turret pointing straight FORWARD.   + = LEFT (counter-clockwise),  - = RIGHT.
 *
 * Calibrate the TODO constants with the "Turret Test" OpMode.
 */
public class Turret {

    // Config names (decision 009). Must match the Driver Station robot configuration.
    public static final String SERVO_NAME = "turretServo";
    public static final String ENCODER_NAME = "turretEncoder";
    public static final String FORWARD_SWITCH_NAME = "turretForwardSwitch";

    // TODO(#31): the servo position (0.0 to 1.0) that points the turret straight forward.
    public static final double SERVO_FORWARD_POSITION = 0.5;
    // TODO(#31): how many turret degrees one full servo range (0.0 -> 1.0) turns.
    // Example: a 270-degree servo turning the turret directly = 270. Make it NEGATIVE if a bigger servo position turns RIGHT.
    public static final double TURRET_DEGREES_PER_SERVO_RANGE = 270.0;

    // ELC Encoder V2 data sheet: 4000 CPR (counts per revolution).
    // TODO(#31): check it: turn the encoder's hex exactly ONE full turn by hand. Raw counts should change by about 4000.
    public static final double ENCODER_COUNTS_PER_REV = 4000.0;
    // TODO(#31): encoder turns per turret turn (the gear ratio). 1.0 if they turn together.
    public static final double ENCODER_REVS_PER_TURRET_REV = 1.0;
    // TODO(#31): set to true if "Turret angle" goes DOWN when the turret turns LEFT.
    public static final boolean ENCODER_REVERSED = false;

    // TODO(#31): how far the turret may turn each way. Nothing physical stops the turret,
    // so these keep it from turning too far (into wires or the frame).
    public static final double MAX_LEFT_DEGREES = 90.0;
    public static final double MAX_RIGHT_DEGREES = -90.0;

    // TODO(#31): check with "Turret Test". REV's switch reads false when a magnet is near (active low).
    public static final boolean FORWARD_SWITCH_TRIGGERED_STATE = false;

    private final Servo turretServo;
    // Only used to READ the encoder port. We never give it power.
    private final DcMotorEx turretEncoder;
    private final DigitalChannel turretForwardSwitch;

    // The angle we last told the turret to go to. NaN until the first command.
    private double targetDegrees = Double.NaN;

    // Homing: the encoder count when the turret was facing forward.
    private boolean homed = false;
    private int homeCounts = 0;

    public Turret(HardwareMap hardwareMap) {
        turretServo = hardwareMap.get(Servo.class, SERVO_NAME);
        turretEncoder = hardwareMap.get(DcMotorEx.class, ENCODER_NAME);
        turretForwardSwitch = hardwareMap.get(DigitalChannel.class, FORWARD_SWITCH_NAME);
        turretForwardSwitch.setMode(DigitalChannel.Mode.INPUT);

        // We do NOT move the servo here. INIT must not move anything (game rule G304.H).
    }

    /** Call this once every loop, including during INIT. It homes the encoder when the turret faces forward. */
    public void update() {
        if (!homed && isFacingForward()) {
            homeCounts = turretEncoder.getCurrentPosition();
            homed = true;
        }
    }

    /** Turn the turret to an angle (0 = forward, + = left). Call after START, never during INIT. */
    public void turnTo(double degrees) {
        degrees = Math.max(MAX_RIGHT_DEGREES, Math.min(MAX_LEFT_DEGREES, degrees));
        targetDegrees = degrees;
        turretServo.setPosition(SERVO_FORWARD_POSITION + degrees / TURRET_DEGREES_PER_SERVO_RANGE);
    }

    /** Point the turret straight forward. */
    public void turnForward() {
        turnTo(0.0);
    }

    /** True once the encoder has been homed (the forward switch has triggered at least once). */
    public boolean isHomed() {
        return homed;
    }

    /** The real turret angle from the encoder, in degrees (0 = forward, + = left). NaN until homed. */
    public double getAngle() {
        if (!homed) {
            return Double.NaN;
        }
        double encoderRevs = (getEncoderCounts() - homeCounts) / ENCODER_COUNTS_PER_REV;
        double degrees = encoderRevs / ENCODER_REVS_PER_TURRET_REV * 360.0;
        return ENCODER_REVERSED ? -degrees : degrees;
    }

    /** The angle we last told the turret to go to (NaN if we haven't moved it yet). */
    public double getTargetAngle() {
        return targetDegrees;
    }

    /** Raw encoder count. Used for calibrating. */
    public int getEncoderCounts() {
        return turretEncoder.getCurrentPosition();
    }

    /**
     * True when the turret is facing forward (the magnet is at the switch).
     * The OpMode can show this on the light: light.setColor(GREEN) (decision 018).
     */
    public boolean isFacingForward() {
        return turretForwardSwitch.getState() == FORWARD_SWITCH_TRIGGERED_STATE;
    }

    /** Show this subsystem's status on the Driver Station. */
    public void addTelemetry(Telemetry telemetry) {
        telemetry.addData("Turret homed", homed ? "yes" : "NO: turn the turret forward once");
        telemetry.addData("Turret angle (°)  +left", homed ? String.format("%.1f", getAngle()) : "unknown");
        telemetry.addData("Turret target (°)", Double.isNaN(targetDegrees) ? "not moved yet" : String.format("%.1f", targetDegrees));
        telemetry.addData("Turret facing forward (switch)", isFacingForward() ? "YES" : "no");
    }
}
