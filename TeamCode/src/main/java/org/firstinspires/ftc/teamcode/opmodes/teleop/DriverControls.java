package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;

/**
 * The ONE place that turns gamepad sticks into drive commands.
 *
 * Gamepad 1 is the DRIVER (gamepad 2 is the OPERATOR, for the mechanisms):
 *   left stick   drive forward/back and strafe left/right
 *   right stick  turn (left/right)
 *   hold either bumper   SLOW   (precise lining up)
 *   hold either trigger  TURBO  (full speed)
 *   A            STRAIGHT-LINE ASSIST on / off (issue #67). ON = a stick pointed almost straight drives
 *                exactly straight. The gamepad rumbles: 1 blip = ON, 2 blips = OFF
 * Printable diagram: TeamCode/docs/worksheets/controller-layout.pdf
 * Tuning worksheet (speeds, assist angle, with the drivers): TeamCode/docs/worksheets/driver-tuning.pdf
 *
 * Every TeleOp drives the robot with:
 *     DriverControls driver = new DriverControls();   // once, before the loop
 *     driver.drive(robot.drivetrain, gamepad1);       // every loop
 * so all TeleOps drive the same way, and a wrong direction only needs fixing here.
 * It's an object (like OperatorControls) because the assist toggle must REMEMBER on/off between loops.
 *
 * Autonomous does NOT use this. It calls robot.drivetrain.drive(...) directly,
 * because its numbers (e.g. from the Pinpoint) already follow our convention.
 */
public class DriverControls {

    // Multiply sideways stick input a little, because mecanum wheels strafe
    // slower than they drive forward.
    public static final double STRAFE_CORRECTION = 1.1;

    // Small deadzone so the robot doesn't creep when sticks are released.
    public static final double DEADZONE = 0.05;

    // Straight-line assist (issue #67): if the left stick points ALMOST straight (forward, back, left or right),
    // drive EXACTLY straight. Forgives a thumb that leans a little. Diagonals still work outside the zone.
    // The driver turns it on/off with A. ASSIST_ON_AT_START = how every TeleOp starts.
    // SNAP_ANGLE_DEGREES = how far off straight still counts as straight. Bigger = more forgiving.
    public static final boolean ASSIST_ON_AT_START = true;
    public static final double SNAP_ANGLE_DEGREES = 20.0;

    private boolean straightLineAssist = ASSIST_ON_AT_START;

    /**
     * The three driving speeds. The driver picks one by holding a button:
     *   SLOW    hold EITHER bumper   precise lining up
     *   NORMAL  nothing held         everyday driving
     *   TURBO   hold EITHER trigger  full speed across the field
     * If a bumper AND a trigger are held, SLOW wins (the safer choice).
     */
    public enum DriveSpeed { SLOW, NORMAL, TURBO }

    // How much of full power each speed uses (1.0 = full power). TODO: tune with the drivers.
    public static final double SLOW_MODE_SCALE = 0.5;
    public static final double NORMAL_SCALE = 0.8;
    public static final double TURBO_SCALE = 1.0;

    // The triggers are analog (0 = released, 1 = all the way down). Pressed past this = "held".
    public static final double TRIGGER_HELD = 0.5;

    /**
     * Read the driver's sticks and drive the robot. Call this once per loop.
     *
     * @return which speed is active (SLOW, NORMAL or TURBO), to show on telemetry.
     */
    public DriveSpeed drive(Drivetrain drivetrain, Gamepad gamepad) {
        // A = straight-line assist on / off. aWasPressed() is true only ONCE per press.
        if (gamepad.aWasPressed()) {
            straightLineAssist = !straightLineAssist;
            gamepad.rumbleBlips(straightLineAssist ? 1 : 2); // feel it without looking: 1 = ON, 2 = OFF
        }

        // The gamepad sticks don't match our direction convention (decision 007),
        // so we flip them HERE, and only here:
        //   stick pushed forward = negative Y   -> flip to get +forward
        //   stick pushed right   = positive X   -> flip to get +left
        //   right stick right    = positive X   -> flip to get +turn (counter-clockwise)
        double forward = applyDeadzone(-gamepad.left_stick_y);
        double left    = applyDeadzone(-gamepad.left_stick_x);
        double turn    = applyDeadzone(-gamepad.right_stick_x);

        // Almost straight? Make it exactly straight (issue #67).
        if (straightLineAssist) {
            double strength = Math.min(1.0, Math.hypot(forward, left)); // how far the stick is pushed
            double snapRatio = Math.tan(Math.toRadians(SNAP_ANGLE_DEGREES));
            if (Math.abs(left) <= Math.abs(forward) * snapRatio) {
                // Close to forward/back: no sideways part, same speed.
                forward = Math.copySign(strength, forward);
                left = 0.0;
            } else if (Math.abs(forward) <= Math.abs(left) * snapRatio) {
                // Close to strafe left/right: no forward part, same speed.
                left = Math.copySign(strength, left);
                forward = 0.0;
            }
        }
        left *= STRAFE_CORRECTION;

        DriveSpeed speed = pickSpeed(gamepad);
        double scale = scaleFor(speed);

        drivetrain.drive(forward * scale, left * scale, turn * scale);
        return speed;
    }

    /** True when straight-line assist is ON. */
    public boolean isStraightLineAssistOn() {
        return straightLineAssist;
    }

    /** Show the driver's settings. */
    public void addTelemetry(Telemetry telemetry, DriveSpeed speed) {
        telemetry.addData("Drive speed", speed);
        telemetry.addData("Straight-line assist (A)", straightLineAssist ? "ON" : "OFF");
    }

    /** Which speed the driver wants: either bumper = SLOW, either trigger = TURBO, otherwise NORMAL. */
    private static DriveSpeed pickSpeed(Gamepad gamepad) {
        boolean bumperHeld  = gamepad.left_bumper || gamepad.right_bumper;
        boolean triggerHeld = gamepad.left_trigger > TRIGGER_HELD || gamepad.right_trigger > TRIGGER_HELD;
        if (bumperHeld) {
            return DriveSpeed.SLOW;   // checked first, so SLOW wins if both are held
        } else if (triggerHeld) {
            return DriveSpeed.TURBO;
        } else {
            return DriveSpeed.NORMAL;
        }
    }

    /** How much of full power a speed uses. */
    private static double scaleFor(DriveSpeed speed) {
        switch (speed) {
            case SLOW:  return SLOW_MODE_SCALE;
            case TURBO: return TURBO_SCALE;
            default:    return NORMAL_SCALE;
        }
    }

    /** Apply deadzone threshold to stick input. */
    private static double applyDeadzone(double input) {
        return Math.abs(input) < DEADZONE ? 0.0 : input;
    }
}
