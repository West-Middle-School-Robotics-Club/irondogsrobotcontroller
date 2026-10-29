package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * FlowerFlipper subsystem: a servo that shakes back and forth (decision 024).
 *
 * Hardware:
 *   flowerFlipperServo  POSITIONAL servo (Driver Station config type: "Servo").
 *                       A missing servo shows OFFLINE instead of crashing every OpMode.
 *
 * How the shaking works: while shaking, the servo switches between POSITION_A and POSITION_B
 * every SWITCH_SECONDS. That only happens if the OpMode calls update() EVERY loop.
 */
public class FlowerFlipper {

    // Config name (decisions 009, 024). Must match the Driver Station robot configuration.
    public static final String SERVO_NAME = "flowerFlipperServo";

    // TODO: tune with "Flower Flipper Test". Positions go from 0.0 to 1.0.
    public static final double REST_POSITION = 0.5;   // where it sits when not shaking
    public static final double POSITION_A = 0.4;      // one end of the shake
    public static final double POSITION_B = 0.6;      // the other end of the shake
    public static final double SWITCH_SECONDS = 0.15; // time at each end. Smaller = faster shaking

    private final Servo flowerFlipperServo;  // null if not in the Driver Station config
    private final ElapsedTime switchTimer = new ElapsedTime();
    private boolean shaking = false;
    private boolean atPositionA = false;
    private boolean everStarted = false;  // stop() only moves the servo if we've used it (never during INIT)

    public FlowerFlipper(HardwareMap hardwareMap) {
        flowerFlipperServo = hardwareMap.tryGet(Servo.class, SERVO_NAME);
        // We do NOT move the servo here. INIT must not move anything (game rule G304.H).
    }

    /** Start shaking back and forth. Call after START, never during INIT. */
    public void startShaking() {
        shaking = true;
        everStarted = true;
        switchTimer.reset();
        setPosition(POSITION_A);
        atPositionA = true;
    }

    /** Stop shaking and go back to REST_POSITION (only if it was started, so INIT never moves it). */
    public void stop() {
        shaking = false;
        if (everStarted) {
            setPosition(REST_POSITION);
        }
    }

    /** Call this ONCE EVERY LOOP. While shaking, it switches between POSITION_A and POSITION_B. */
    public void update() {
        if (shaking && switchTimer.seconds() >= SWITCH_SECONDS) {
            atPositionA = !atPositionA;
            setPosition(atPositionA ? POSITION_A : POSITION_B);
            switchTimer.reset();
        }
    }

    /** True while shaking. */
    public boolean isShaking() {
        return shaking;
    }

    /** True if the servo was found in the Driver Station config. */
    public boolean isConnected() {
        return flowerFlipperServo != null;
    }

    private void setPosition(double position) {
        if (flowerFlipperServo != null) {
            flowerFlipperServo.setPosition(position);
        }
    }

    /** Show this subsystem's status on the Driver Station. */
    public void addTelemetry(Telemetry telemetry) {
        if (flowerFlipperServo == null) {
            telemetry.addData("Flower flipper", "OFFLINE ('%s' not in DS config)", SERVO_NAME);
        } else {
            telemetry.addData("Flower flipper", shaking ? "SHAKING" : "resting");
        }
    }
}
