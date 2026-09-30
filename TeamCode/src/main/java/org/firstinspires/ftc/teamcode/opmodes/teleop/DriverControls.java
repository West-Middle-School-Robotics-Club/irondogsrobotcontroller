package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;

/**
 * The ONE place that turns gamepad sticks into drive commands.
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

    // Slow mode reduces drive power for precise lining up.
    public static final double SLOW_MODE_SCALE = 0.4;

    /**
     * Read the driver's sticks and drive the robot. Call this once per loop.
     *
     * @return true if slow mode is active, false otherwise.
     */
    public static boolean drive(Drivetrain drivetrain, Gamepad gamepad) {
        // The gamepad sticks don't match our direction convention (decision 007),
        // so we flip them HERE, and only here:
        //   stick pushed forward = negative Y   -> flip to get +forward
        //   stick pushed right   = positive X   -> flip to get +left
        //   right stick right    = positive X   -> flip to get +turn (counter-clockwise)
        double forward = applyDeadzone(-gamepad.left_stick_y);
        double left    = applyDeadzone(-gamepad.left_stick_x) * STRAFE_CORRECTION;
        double turn    = applyDeadzone(-gamepad.right_stick_x);

        // Slow mode for precise alignment (holding left or right bumper)
        boolean slowMode = gamepad.left_bumper || gamepad.right_bumper;
        if (slowMode) {
            forward *= SLOW_MODE_SCALE;
            left    *= SLOW_MODE_SCALE;
            turn    *= SLOW_MODE_SCALE;
        }

        drivetrain.drive(forward, left, turn);
        return slowMode;
    }

    /** Apply deadzone threshold to stick input. */
    private static double applyDeadzone(double input) {
        return Math.abs(input) < DEADZONE ? 0.0 : input;
    }
}
