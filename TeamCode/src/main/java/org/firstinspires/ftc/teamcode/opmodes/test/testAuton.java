package org.firstinspires.ftc.teamcode.opmodes.test;

import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Alliance;
import org.firstinspires.ftc.teamcode.opmodes.auto.BasicAuton;

/**
 * TEST AUTON (issue #22): the Basic Auton routes, but the start is ALWAYS (0, 0, 0) to take the setup out of the picture.
 *
 * The real Basic Auton tells the Pinpoint "you are at tile A6 against the red wall", so any error in where students
 * place the robot shows up as an error at the park spot. This test tells the Pinpoint "you are at 0, 0, facing 0"
 * wherever the robot sits, and drives the SAME moves written relative to that start. Run it several times from a
 * tape mark on any clear floor and measure where the robot ends: that is the repeatability of the driving + Pinpoint.
 *
 * It shares ALL the driving code and gains with BasicAuton (DRIVE_GAIN, tolerances, timeouts, lights, start delay,
 * Pinpoint READY wait). The only differences are startPose(), path() and the SET UP line.
 *
 * Coordinates here are NOT the field. The robot ALWAYS starts at (0, 0, heading 0), so +X = inches forward of the
 * start, +Y = inches LEFT of the start (-Y = right), heading 0 = the way it was facing. Each waypoint is where the
 * robot's CENTER should be, not how far to move from the last one. The start heading is 0 ON PURPOSE: with the real
 * NEAR start heading (-90) the Pinpoint's +X would be the robot's LEFT and forward would be -Y, and the numbers
 * would stop reading as forward / left.
 *
 * The moves below are the real routes in BasicAuton, read as "waypoint minus start" in robot directions:
 *   NEAR (real start (60, 63, -90), facing -Y: field -Y = forward, field -X = right)
 *     (60, 40) -> forward 23          = (23,   0)
 *     (48, 40) -> and 12 to the right = (23, -12)
 *     (48, 58) -> then 18 back        = ( 5, -12)
 *   FAR  (real start (-63, 36, 0), facing +X: field +X = forward, field +Y = left)
 *     (37, 36) -> forward 100         = (100,  0)
 *     (37, 56) -> and 20 to the left  = (100, 20)
 * If a waypoint in BasicAuton changes, change the matching number here BY HAND (and the other way round).
 *
 * Two OpModes use this class: Test Auton NEAR (needs ~2.5 ft ahead, 1.5 ft right) and Test Auton FAR (needs ~9 ft
 * ahead, 2.5 ft left). Lights: GREEN = reached every waypoint, YELLOW = stopped early.
 */
public abstract class testAuton extends BasicAuton {

    /** Every test starts here: 0, 0, facing 0. +X = forward, +Y = left. */
    static final Pose2D TEST_START = pose(0, 0, 0);

    /** NEAR moves, relative to the start (BasicAuton.RED_NEAR_PATH in robot directions). */
    static final Pose2D[] TEST_NEAR_PATH = {
            pose(23, 0, 0),    // LEAVE: 23 in forward
            pose(23, -12, 0),  // 12 in right
            pose(5, -12, 0),   // PARK: 18 in back
    };

    /** FAR moves, relative to the start (BasicAuton.RED_FAR_PATH in robot directions). */
    static final Pose2D[] TEST_FAR_PATH = {
            pose(100, 0, 0),   // LEAVE + the long drive: 100 in forward
            pose(100, 20, 0),  // PARK: 20 in left
    };

    /** The paths above are written as moves; the test never rotates them for BLUE. Only the INIT lights use this. */
    @Override
    protected Alliance alliance() {
        return Alliance.RED;
    }

    @Override
    protected Pose2D startPose() {
        return TEST_START;   // wherever the robot is right now
    }

    @Override
    protected Pose2D[] path() {
        return route() == Route.NEAR ? TEST_NEAR_PATH : TEST_FAR_PATH;
    }

    @Override
    protected String setupInstructions() {
        return route() == Route.NEAR
                ? "TEST: any clear floor, ~2.5 ft ahead and 1.5 ft to the RIGHT. Start = (0, 0, 0)"
                : "TEST: any clear floor, ~9 ft ahead and 2.5 ft to the LEFT. Start = (0, 0, 0)";
    }
}
