package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;

/**
 * The ONE place that turns gamepad sticks into drive commands.
 *
 * Gamepad 1 is the DRIVER (gamepad 2 is the OPERATOR, for the mechanisms):
 *   left stick   drive forward/back and strafe left/right
 *   right stick  turn (left/right)
 *   hold either bumper   SLOW   (precise lining up)
 *   hold either trigger  TURBO  (full speed)
 * Printable diagram: TeamCode/docs/worksheets/controller-layout.pdf
 *
 * Every TeleOp drives the robot with:
 *     DriverControls.drive(robot.drivetrain, gamepad1);
 * so all TeleOps drive the same way, and a wrong direction only needs fixing here.
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

    /**
     * The three driving speeds. The driver picks one by holding a button:
     *   SLOW    hold EITHER bumper   precise lining up
     *   NORMAL  nothing held         everyday driving
     *   TURBO   hold EITHER trigger  full speed across the field
     * If a bumper AND a trigger are held, SLOW wins (the safer choice).
     */
    public enum DriveSpeed { SLOW, NORMAL, TURBO }

    // How much of full power each speed uses (1.0 = full power). TODO: tune with the drivers.
    public static final double SLOW_MODE_SCALE = 0.4;
    public static final double NORMAL_SCALE = 0.7;
    public static final double TURBO_SCALE = 1.0;

    // The triggers are analog (0 = released, 1 = all the way down). Pressed past this = "held".
    public static final double TRIGGER_HELD = 0.5;

    /**
     * Read the driver's sticks and drive the robot. Call this once per loop.
     *
     * @return which speed is active (SLOW, NORMAL or TURBO), to show on telemetry.
     */
    public static DriveSpeed drive(Drivetrain drivetrain, Gamepad gamepad) {
        // The gamepad sticks don't match our direction convention (decision 007),
        // so we flip them HERE, and only here:
        //   stick pushed forward = negative Y   -> flip to get +forward
        //   stick pushed right   = positive X   -> flip to get +left
        //   right stick right    = positive X   -> flip to get +turn (counter-clockwise)
        double forward = applyDeadzone(-gamepad.left_stick_y);
        double left    = applyDeadzone(-gamepad.left_stick_x) * STRAFE_CORRECTION;
        double turn    = applyDeadzone(-gamepad.right_stick_x);

        DriveSpeed speed = pickSpeed(gamepad);
        double scale = scaleFor(speed);

        drivetrain.drive(forward * scale, left * scale, turn * scale);
        return speed;
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
