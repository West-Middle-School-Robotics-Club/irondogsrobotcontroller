package org.firstinspires.ftc.teamcode.opmodes.auto;

import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

/**
 * MAIN AUTON (issue #22, decision 060): the AUTO we run for the FAR start (Timed Auton is the plan for NEAR, but
 * Main Auton NEAR is on the Driver Station too). Was "Test Auton" (opmodes/test/testAuton.java, decision 053).
 *
 * It drives with the Pinpoint like Basic Auton, but the start is ALWAYS (0, 0, 0): the Pinpoint is told "you are
 * at 0, 0, facing 0" wherever the robot sits, and the moves are written relative to that start. So the numbers
 * read as plain robot moves ("forward 85, then left 22"), and the same moves work for RED and BLUE (the field is
 * rotated 180 degrees, so the robot drives the same moves on either side, like Timed Auton).
 *
 * It shares ALL the driving code and gains with BasicAuton (DRIVE_GAIN, tolerances, timeouts, lights, start delay,
 * Pinpoint READY wait). The only differences are startPose() and path(). The SET UP line on the INIT screen is
 * the real field setup (same as Basic Auton and Timed Auton: BACK touching a wall, decision 046).
 *
 * Coordinates here are NOT the field. The robot ALWAYS starts at (0, 0, heading 0), so +X = inches forward of the
 * start, +Y = inches LEFT of the start (-Y = right), heading 0 = the way it was facing. Each waypoint is where the
 * robot's CENTER should be, not how far to move from the last one. The start heading is 0 ON PURPOSE: with the real
 * NEAR start heading (-90) the Pinpoint's +X would be the robot's LEFT and forward would be -Y, and the numbers
 * would stop reading as forward / left.
 *
 * The moves started as the routes in BasicAuton, read as "waypoint minus start" in robot directions:
 *   NEAR (real start (60, 63, -90), facing -Y: field -Y = forward, field -X = right)
 *     (60, 40) -> forward 23          = (23,   0)
 *     (48, 40) -> and 12 to the right = (23, -12)
 *     (48, 58) -> then 18 back        = ( 5, -12)
 *   FAR  (real start (-63, 36, 0), facing +X: field +X = forward, field +Y = left)
 *     (37, 36) -> forward 100         = (100,  0)
 *     (37, 56) -> and 20 to the left  = (100, 20)
 * The team then tuned them on the floor (the numbers below), so they no longer match BasicAuton exactly.
 *
 * Four OpModes use this class: Main Auton RED / BLUE NEAR and RED / BLUE FAR. They only pick the alliance (for the
 * INIT lights, the SET UP line and the TeleOp that comes next) and the route.
 * Lights: GREEN = reached every waypoint, YELLOW = stopped early.
 */
public abstract class MainAuton extends BasicAuton {

    /** Every run starts here: 0, 0, facing 0. +X = forward, +Y = left. */
    static final Pose2D MAIN_START = pose(0, 0, 0);

    /** NEAR moves, relative to the start. */
    static final Pose2D[] MAIN_NEAR_PATH = {
            pose(24, 0, 0),    // LEAVE: 24 in forward
            pose(23, -15, 0),  // 15 in right
            pose(2, -15, 0),   // PARK: back
    };

    /** FAR moves, relative to the start. */
    static final Pose2D[] MAIN_FAR_PATH = {
            pose(85, 0, 0),   // LEAVE + the long drive: 85 in forward
            pose(85, 22, 0),  // PARK: 22 in left
    };

    @Override
    protected Pose2D startPose() {
        return MAIN_START;   // wherever the robot is right now
    }

    /** The paths are robot moves, so they are NOT rotated for BLUE: both alliances drive the same moves. */
    @Override
    protected Pose2D[] path() {
        return route() == Route.NEAR ? MAIN_NEAR_PATH : MAIN_FAR_PATH;
    }
}
