package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.opmodes.auto.MainAuton;
import org.firstinspires.ftc.teamcode.subsystems.Odometry;

/**
 * TEST: SHOOT AUTON, FAR start. Main Auton FAR, but it shoots its pre-loaded balls first.
 *
 * Same setup as Main Auton FAR: RED in tile B1, BLUE in tile E6, BACK touching the wall (decision 046).
 * Like Main Auton, the robot starts at (0, 0, 0) and the moves are robot moves: +X = forward, +Y = LEFT (-Y = right).
 * RED and BLUE drive the SAME moves (Blue = Red turned 180 degrees): to the robot, B1 -> C1 and E6 -> D6 are both
 * a move to its RIGHT.
 *
 *   1. SHOOT_SPOT: 22.5 in RIGHT (RED B1 -> C1, BLUE E6 -> D6). The front still faces the far wall / audience
 *   2. Shoot: flywheel + intake + water wheel ON, wait FLYWHEEL_SPIN_UP_S, then HOLD the gate open (trigger at FIRE)
 *      for SHOOT_TIME_S so the water wheel feeds the balls through. Then everything OFF and the gate closed
 *   3. Back to the start (0, 0), 22.5 in left
 *   4. Main Auton FAR's route (MAIN_FAR_PATH): forward 85, left 22 = PARK
 *
 * The driving is Main Auton's / Basic Auton's (same gains, start delay, lights, STOP_BY_S). The new parts are the path,
 * the shooting (in atWaypoint() at the first waypoint) and a shorter pause after each waypoint (SHOOT_WAYPOINT_PAUSE_S,
 * 1 s; Main Auton keeps WAYPOINT_PAUSE_S = 2 s).
 * The turret is NOT moved: it shoots wherever the turret already points (forward, if it was left at home).
 *
 * TIME: this route is longer than Main Auton FAR (two extra 22.5 in strafes, the shooting, two extra pauses).
 * Everything stops at STOP_BY_S (28 s). If the lights end YELLOW, it ran out of time before PARK: shorten
 * SHOOT_TIME_S, FLYWHEEL_SPIN_UP_S or SHOOT_WAYPOINT_PAUSE_S.
 */
public abstract class ShootAuton extends MainAuton {

    // Tune on the robot (tuning-guide.md has a row for each).
    static final Pose2D SHOOT_SPOT = pose(0, -22.5, 0); // 22.5 in RIGHT of the start: RED B1 -> C1, BLUE E6 -> D6
    static final double FLYWHEEL_SPIN_UP_S = 2.0;       // flywheel ON this long before the gate opens (intake runs too)
    static final double SHOOT_TIME_S = 6.0;             // gate HELD open this long: enough for all 4 balls to go through
    static final double SHOOT_WAYPOINT_PAUSE_S = 1.0;   // sit still this long after each waypoint (Main Auton: 2.0)

    /** The shooting happens at the first waypoint (SHOOT_SPOT). */
    private static final int SHOOT_WAYPOINT = 0;

    /** SHOOT_SPOT, back to the start, then Main Auton FAR's moves. */
    @Override
    protected Pose2D[] path() {
        Pose2D[] path = new Pose2D[2 + MAIN_FAR_PATH.length];
        path[0] = SHOOT_SPOT;
        path[1] = MAIN_START;   // back where we started, so the normal FAR route works unchanged
        System.arraycopy(MAIN_FAR_PATH, 0, path, 2, MAIN_FAR_PATH.length);
        return path;
    }

    /** Shoot Auton's own, shorter pause after each waypoint (Main Auton keeps WAYPOINT_PAUSE_S). */
    @Override
    protected double waypointPauseSeconds() {
        return SHOOT_WAYPOINT_PAUSE_S;
    }

    /** At SHOOT_SPOT: spin up, then HOLD the gate open for SHOOT_TIME_S so the water wheel feeds every ball through. */
    @Override
    protected void atWaypoint(int index, Robot robot, Odometry odometry, ElapsedTime autoTimer) {
        if (index != SHOOT_WAYPOINT) {
            return;
        }
        robot.launcher.spinUp();        // DEFAULT_SPIN_POWER, same as TeleOp
        robot.intake.in();
        robot.intake.waterWheelUp();    // balls wait at the closed gate until it opens

        // Step 1: gate CLOSED while the flywheel gets up to speed
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && autoTimer.seconds() < STOP_BY_S && timer.seconds() < FLYWHEEL_SPIN_UP_S) {
            robot.launcher.resetTrigger();
            odometry.update();
            showStatus("Flywheel spinning up (gate closed)", autoTimer, odometry, null);
        }

        // Step 2: gate HELD OPEN for SHOOT_TIME_S (fire() every loop, so it stays at FIRE the whole time)
        timer.reset();
        while (opModeIsActive() && autoTimer.seconds() < STOP_BY_S && timer.seconds() < SHOOT_TIME_S) {
            robot.launcher.fire();
            odometry.update();
            showStatus(String.format("SHOOTING: gate held OPEN, %.1f s left", SHOOT_TIME_S - timer.seconds()),
                    autoTimer, odometry, null);
        }

        // Done shooting (or out of time): gate closed, everything OFF, then drive back to the start
        robot.launcher.resetTrigger();
        robot.intake.stop();
        robot.launcher.stop();
    }
}
