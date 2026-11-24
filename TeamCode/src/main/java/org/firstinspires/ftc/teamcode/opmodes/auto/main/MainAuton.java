package org.firstinspires.ftc.teamcode.opmodes.auto.main;

import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Alliance;
import org.firstinspires.ftc.teamcode.opmodes.auto.BasicAuton;

/**
 * MAIN AUTON (issue #22, decisions 060, 069): LEAVE + PARK with the Pinpoint, for NEAR and FAR, from an EDGE or a CORNER
 * spot. Was "Test Auton" (opmodes/test/testAuton.java, decision 053).
 *
 * It drives with the Pinpoint like Basic Auton, but the start is ALWAYS (0, 0, 0): the Pinpoint is told "you are
 * at 0, 0, facing 0" wherever the robot sits, and the moves are written relative to that start. So the numbers
 * read as plain robot moves ("forward 85, then left 25"), and the same moves work for RED and BLUE (the field is
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
 * NEAR and FAR each have TWO starting spots, each with its own OpModes: EDGE (the spot we've been using) and CORNER
 * (closer to the corner of the field).
 *   NEAR (decision 068): forward, right, then back to just off the wall (MAIN_NEAR_EDGE_PATH, MAIN_NEAR_CORNER_PATH)
 *   FAR  (decision 066): forward, then left into the LOADING ZONE (MAIN_FAR_EDGE_PATH, MAIN_FAR_CORNER_PATH)
 * The numbers are tuned on the robot: read them in the paths below, not in comments.
 *
 * Eight small OpMode files in this folder use this class (decision 069), all in the "Main Auton" group on the Driver
 * Station: Main Auton Red / Blue  Near / Far  Edge / Corner. They only pick the alliance (for the INIT lights, the SET UP
 * line and the TeleOp that comes next), the route and the spot. Shoot Auton (auto/shoot) builds on this class too, and
 * so does the FAR competition BACKUP in auto/competition ("Auton Red Far" / "Auton Blue Far", FarSpot.COMPETITION).
 * Lights: GREEN = reached every waypoint, YELLOW = stopped early.
 */
public abstract class MainAuton extends BasicAuton {

    /** Every run starts here: 0, 0, facing 0. +X = forward, +Y = left. */
    protected static final Pose2D MAIN_START = pose(0, 0, 0);

    /** Which NEAR starting spot (decision 068). */
    public enum NearSpot { EDGE, CORNER }

    /** NEAR from the CORNER spot (closer to the corner of the field), relative to the start. */
    static final Pose2D[] MAIN_NEAR_CORNER_PATH = {
            pose(20, 0, 0),    // LEAVE: 20 in forward
            pose(20, -17, 0),  // 17 in right
            pose(1, -17, 0),   // PARK: back, to 1 in from the start (still off the wall)
    };

    /** NEAR from the EDGE spot (the one we've been using), relative to the start. Was MAIN_NEAR_PATH. */
    static final Pose2D[] MAIN_NEAR_EDGE_PATH = {
            pose(20, 0, 0),    // LEAVE: 20 in forward
            pose(20, -8, 0),   // 8 in right
            pose(1, -8, 0),    // PARK: back, to 1 in from the start (still off the wall)
    };

    /** The NEAR spot this OpMode starts from. EDGE unless an OpMode file says CORNER. Not used for FAR. */
    protected NearSpot nearSpot() {
        return NearSpot.EDGE;
    }

    /**
     * Which FAR starting spot (decision 066). COMPETITION = FAR exactly as we ran it at the competition (the backup,
     * decision 069): MAIN_FAR_COMPETITION_PATH and a COMPETITION_WAYPOINT_PAUSE_S pause.
     */
    public enum FarSpot { EDGE, CORNER, COMPETITION }

    /** FAR COMPETITION: the BACKUP, exactly as we ran it at the competition. DON'T CHANGE: tune EDGE / CORNER instead. */
    protected static final Pose2D[] MAIN_FAR_COMPETITION_PATH = {
            pose(85, 0, 0),   // LEAVE + the long drive: 85 in forward
            pose(85, 22, 0),  // PARK: 22 in left
    };

    /** The pause after each waypoint at the competition (WAYPOINT_PAUSE_S was 2.0 then). DON'T CHANGE. */
    static final double COMPETITION_WAYPOINT_PAUSE_S = 2.0;

    /** FAR from the EDGE spot (the one we've been using), relative to the start. Shoot Auton drives these too. */
    protected static final Pose2D[] MAIN_FAR_EDGE_PATH = {
            pose(85, 0, 0),   // LEAVE + the long drive: 85 in forward
            pose(85, 25, 0),  // PARK: 25 in left
    };

    /** FAR from the CORNER spot (closer to the corner of the field), relative to the start. */
    protected static final Pose2D[] MAIN_FAR_CORNER_PATH = {
            pose(95, 0, 0),   // LEAVE + the long drive: 95in forward
            pose(95, 25, 0),  // PARK: 25 in left
    };

    /** The FAR spot this OpMode starts from. EDGE unless an OpMode file says CORNER. Not used for NEAR. */
    protected FarSpot farSpot() {
        return FarSpot.EDGE;
    }

    @Override
    protected Pose2D startPose() {
        return MAIN_START;   // wherever the robot is right now
    }

    /** The paths are robot moves, so they are NOT rotated for BLUE: both alliances drive the same moves. */
    @Override
    protected Pose2D[] path() {
        if (route() == Route.NEAR) {
            return nearSpot() == NearSpot.CORNER ? MAIN_NEAR_CORNER_PATH : MAIN_NEAR_EDGE_PATH;
        }
        if (farSpot() == FarSpot.COMPETITION) {
            return MAIN_FAR_COMPETITION_PATH;
        }
        return farSpot() == FarSpot.CORNER ? MAIN_FAR_CORNER_PATH : MAIN_FAR_EDGE_PATH;
    }

    /** The competition backup keeps the competition's pause; everything else uses BasicAuton's WAYPOINT_PAUSE_S. */
    @Override
    protected double waypointPauseSeconds() {
        if (route() == Route.FAR && farSpot() == FarSpot.COMPETITION) {
            return COMPETITION_WAYPOINT_PAUSE_S;
        }
        return super.waypointPauseSeconds();
    }

    /** The SET UP line on the INIT screen: for FAR it also says which spot, so the drive team puts the robot there. */
    @Override
    protected String setupInstructions() {
        boolean red = alliance() == Alliance.RED;
        if (route() == Route.NEAR) {
            if (nearSpot() == NearSpot.CORNER) {
                return red ? "NEAR CORNER spot (closer to the field corner), BACK touching the RED wall, FRONT facing into the field"
                           : "NEAR CORNER spot (closer to the field corner), BACK touching the BLUE wall, FRONT facing into the field";
            }
            return "NEAR EDGE spot: " + super.setupInstructions();
        }
        if (farSpot() == FarSpot.CORNER) {
            return red ? "FAR CORNER spot (closer to the field corner), BACK touching the AUDIENCE wall, FRONT facing the FAR wall"
                       : "FAR CORNER spot (closer to the field corner), BACK touching the FAR wall, FRONT facing the AUDIENCE";
        }
        if (farSpot() == FarSpot.COMPETITION) {
            return "COMPETITION backup (FAR as we ran it): " + super.setupInstructions();
        }
        return "FAR EDGE spot: " + super.setupInstructions();
    }
}
