package org.firstinspires.ftc.teamcode.opmodes.test;

import org.firstinspires.ftc.teamcode.opmodes.auto.BasicAuton;
import org.firstinspires.ftc.teamcode.opmodes.auto.TimedAuton;

/**
 * TEST: TIMED FAR AUTON with two starting spots, EDGE and CORNER (like Main Auton FAR, decision 066, and like
 * Timed Near Auton). No sensors: every move is a set power for a set TIME, like Timed Auton (same DRIVE_POWER and
 * PAUSE_S). Same setup as Timed Auton FAR: RED tile B1 / BLUE tile E6, BACK touching the wall (audience wall for RED,
 * far wall for BLUE), FRONT facing the other end. RED and BLUE drive the SAME moves.
 *
 *   EDGE:   1. FORWARD up the field (FAR_EDGE_FORWARD_S)   2. LEFT into the LOADING ZONE (FAR_EDGE_LEFT_S) = PARK
 *           Main Auton FAR EDGE drives (83, 0) -> (83, 22)
 *   CORNER: 1. FORWARD up the field (FAR_CORNER_FORWARD_S) 2. LEFT into the LOADING ZONE (FAR_CORNER_LEFT_S) = PARK
 *           Main Auton FAR CORNER drives (87, 0) -> (87, 22)
 *
 * TUNE: measure each move with a charged battery; new time = old time x goal / measured (tuning guide).
 */
public abstract class TimedFarAuton extends TimedAuton {

    /** Which FAR starting spot. Each OpMode file picks one. */
    public enum FarSpot { EDGE, CORNER }

    // Set by the team 11/23 (tuning-guide.md has a row for each).
    static final double FAR_EDGE_FORWARD_S = 4.2;    // EDGE 1: forward up column B
    static final double FAR_EDGE_LEFT_S = 1.7;       // EDGE 2: strafe left into the LOADING ZONE (stop before the wall)
    static final double FAR_CORNER_FORWARD_S = 5.0;  // CORNER 1: forward up the field
    static final double FAR_CORNER_LEFT_S = 1.7;     // CORNER 2: strafe left into the LOADING ZONE (stop before the wall)

    protected abstract FarSpot farSpot();

    @Override
    protected BasicAuton.Route route() {
        return BasicAuton.Route.FAR;
    }

    @Override
    protected void driveRoute() {
        boolean corner = farSpot() == FarSpot.CORNER;
        show("1 of 2: FORWARD up the field (LEAVE)");
        move(DRIVE_POWER, 0, corner ? FAR_CORNER_FORWARD_S : FAR_EDGE_FORWARD_S);
        hold(PAUSE_S);
        show("2 of 2: LEFT into the LOADING ZONE (PARK)");
        move(0, DRIVE_POWER, corner ? FAR_CORNER_LEFT_S : FAR_EDGE_LEFT_S);   // plus = LEFT (decision 007)
        show("Done");
    }

    /** One line on the Driver Station, so the drive team can see which move it is on. */
    private void show(String step) {
        telemetry.addData("TIMED FAR AUTON", farSpot());
        telemetry.addData("Step", step);
        telemetry.update();
    }
}
