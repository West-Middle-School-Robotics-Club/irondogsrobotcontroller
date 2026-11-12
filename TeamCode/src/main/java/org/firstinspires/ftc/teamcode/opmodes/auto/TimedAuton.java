package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Robot;

/**
 * TIMED AUTON: the BACKUP for Basic Auton NEAR (decision 054). LEAVE + PARK with no sensors at all.
 *
 * It does not know where the robot is. It just runs the wheels at a set power for a set TIME, one move after
 * another, in a box:
 *   1. FORWARD off the wall        (FORWARD_S seconds)
 *   2. RIGHT, toward the audience  (RIGHT_S seconds)
 *   3. BACK into the LOADING ZONE  (BACK_S seconds)
 * Same moves as Basic Auton NEAR: forward 23 in, right 12 in, back 18 in.
 *
 * No Pinpoint, no encoders (plain power, RUN_WITHOUT_ENCODER), no telemetry. If the Pinpoint or the encoders
 * misbehave at a match, this still drives. The price: battery level, bumps and wheel slip all change how far it goes.
 *
 * SET UP: same as Basic Auton NEAR. RED: tile A6, BACK touching the RED wall, FRONT facing into the field.
 *         BLUE: tile F1, BACK touching the BLUE wall, FRONT facing into the field.
 * RED and BLUE drive the SAME moves: BLUE is RED rotated 180 degrees, so "forward, right, back" works for both.
 *
 * TUNE on the robot (tuning guide): change the SECONDS to change how far each move goes. Test with a charged battery.
 */
public abstract class TimedAuton extends LinearOpMode {

    // How hard the wheels push (0 to 1) on every move. Plain power = % of the battery.
    static final double DRIVE_POWER = 0.3;

    // How long each move runs, in seconds. Longer = farther. STARTING GUESSES: measure and change on the robot.
    static final double FORWARD_S = 1.0;   // 1. forward off the wall (goal: 23 in)
    static final double RIGHT_S = 0.8;     // 2. strafe right, toward the audience (goal: 12 in)
    static final double BACK_S = 0.8;      // 3. back into the LOADING ZONE (goal: 18 in; stop before the wall)

    // Stop and sit still this long between moves, so one move's slide doesn't carry into the next.
    static final double PAUSE_S = 0.5;

    // Wait this long after START before moving (for when our partner's route crosses ours). Set before the match.
    static final double START_DELAY_S = 0.0;

    private Robot robot;

    @Override
    public void runOpMode() {
        // ---------- INIT: nothing moves (G304.H) ----------
        robot = new Robot(hardwareMap);
        robot.drivetrain.setEncoderSpeedControl(false);  // plain power, no encoders
        robot.drivetrain.setBrakeOnStop(true);           // stop when the power goes to 0 (decision 023)

        waitForStart();

        // ---------- AUTO: the box, one move at a time ----------
        hold(START_DELAY_S);
        move(DRIVE_POWER, 0, FORWARD_S);    // 1. forward
        hold(PAUSE_S);
        move(0, -DRIVE_POWER, RIGHT_S);     // 2. right (+left = LEFT, decision 007, so right is minus)
        hold(PAUSE_S);
        move(-DRIVE_POWER, 0, BACK_S);      // 3. back

        robot.stop();
    }

    /** Drive with this forward / left power for this many seconds, then stop. */
    private void move(double forward, double left, double seconds) {
        ElapsedTime timer = new ElapsedTime();
        robot.drivetrain.drive(forward, left, 0);
        while (opModeIsActive() && timer.seconds() < seconds) {
            idle();  // keep driving; STOP on the Driver Station ends the loop right away
        }
        robot.drivetrain.stop();
    }

    /** Sit still for this many seconds. */
    private void hold(double seconds) {
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && timer.seconds() < seconds) {
            idle();
        }
    }
}
