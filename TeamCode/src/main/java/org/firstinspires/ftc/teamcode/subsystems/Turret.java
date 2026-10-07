package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Turret subsystem: turns the launcher (flywheel) and the camera left and right, like a tank turret
 * (issue #7, decision 016).
 *
 * Hardware:
 *   turretServo        positional servo that turns the turret
 *   turretEncoder      ELC Encoder V2 on the servo, ANALOG output: tells us the real angle, even right after power-on
 *   turretForwardSwitch REV Magnetic Limit Switch: tells us when the turret is facing FORWARD (decision 017).
 *                       It is only an indicator, NOT a stop.
 *
 * Angles use our direction convention (decision 007):
 *   0 degrees = turret pointing straight FORWARD.   + = LEFT (counter-clockwise),  - = RIGHT.
 *
 * Calibrate the TODO(#7) constants with the "Turret Test" OpMode.
 */
public class Turret {

    // Config names (decision 009). Must match the Driver Station robot configuration.
    public static final String SERVO_NAME = "turretServo";
    public static final String ENCODER_NAME = "turretEncoder";
    public static final String FORWARD_SWITCH_NAME = "turretForwardSwitch";

    // TODO(#7): the servo position (0.0 to 1.0) that points the turret straight forward.
    public static final double SERVO_FORWARD_POSITION = 0.5;
    // TODO(#7): how many turret degrees one full servo range (0.0 -> 1.0) turns.
    // Example: a 270-degree servo turning the turret directly = 270. Make it NEGATIVE if a bigger servo position turns RIGHT.
    public static final double TURRET_DEGREES_PER_SERVO_RANGE = 270.0;

    // TODO(#7): the encoder reading (degrees, 0-360) when the turret points straight forward.
    public static final double ENCODER_FORWARD_DEGREES = 180.0;
    // TODO(#7): encoder degrees per turret degree (the gear ratio). 1.0 if they turn together.
    // Make it NEGATIVE if the encoder number goes DOWN when the turret turns left.
    public static final double ENCODER_DEGREES_PER_TURRET_DEGREE = 1.0;

    // TODO(#7): how far the turret may turn each way. Nothing physical stops the turret,
    // so these keep it from turning too far (into wires or the frame).
    public static final double MAX_LEFT_DEGREES = 90.0;
    public static final double MAX_RIGHT_DEGREES = -90.0;

    // TODO(#7): check with "Turret Test". REV's switch reads false when a magnet is near (active low).
    public static final boolean FORWARD_SWITCH_TRIGGERED_STATE = false;

    private final Servo turretServo;
    private final AnalogInput turretEncoder;
    private final DigitalChannel turretForwardSwitch;

    // The angle we last told the turret to go to. NaN until the first command.
    private double targetDegrees = Double.NaN;

    public Turret(HardwareMap hardwareMap) {
        turretServo = hardwareMap.get(Servo.class, SERVO_NAME);
        turretEncoder = hardwareMap.get(AnalogInput.class, ENCODER_NAME);
        turretForwardSwitch = hardwareMap.get(DigitalChannel.class, FORWARD_SWITCH_NAME);
        turretForwardSwitch.setMode(DigitalChannel.Mode.INPUT);

        // We do NOT move the servo here. INIT must not move anything (game rule G304.H).
    }

    /** Turn the turret to an angle (0 = forward, + = left). Call after START, never during INIT. */
    public void turnTo(double degrees) {
        degrees = Math.max(MAX_RIGHT_DEGREES, Math.min(MAX_LEFT_DEGREES, degrees));
        targetDegrees = degrees;
        turretServo.setPosition(SERVO_FORWARD_POSITION + degrees / TURRET_DEGREES_PER_SERVO_RANGE);
    }

    /** Turn the turret by some degrees from where it is now (+ = left). Used for aiming with the camera. */
    public void turnBy(double degrees) {
        turnTo(getAngle() + degrees);
    }

    /** Point the turret straight forward. */
    public void turnForward() {
        turnTo(0.0);
    }

    /** The real turret angle from the encoder, in degrees (0 = forward, + = left). */
    public double getAngle() {
        double encoderDegrees = getEncoderVoltage() / turretEncoder.getMaxVoltage() * 360.0;
        return (encoderDegrees - ENCODER_FORWARD_DEGREES) / ENCODER_DEGREES_PER_TURRET_DEGREE;
    }

    /** The angle we last told the turret to go to (NaN if we haven't moved it yet). */
    public double getTargetAngle() {
        return targetDegrees;
    }

    /** Raw encoder voltage (0 to 3.3 V). Used for calibrating. */
    public double getEncoderVoltage() {
        return turretEncoder.getVoltage();
    }

    /**
     * True when the turret is facing forward (the magnet is at the switch).
     * TODO(#7): decide on a light to show this (hub LED, gamepad LED/rumble, or an add-on light).
     */
    public boolean isFacingForward() {
        return turretForwardSwitch.getState() == FORWARD_SWITCH_TRIGGERED_STATE;
    }

    /** Show this subsystem's status on the Driver Station. */
    public void addTelemetry(Telemetry telemetry) {
        telemetry.addData("Turret angle (°)  +left", "%.1f", getAngle());
        telemetry.addData("Turret target (°)", Double.isNaN(targetDegrees) ? "not moved yet" : String.format("%.1f", targetDegrees));
        telemetry.addData("Turret facing forward (switch)", isFacingForward() ? "YES" : "no");
    }
}
