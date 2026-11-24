package org.firstinspires.ftc.teamcode.opmodes.test;

import org.firstinspires.ftc.teamcode.opmodes.auto.BasicAuton;
import org.firstinspires.ftc.teamcode.opmodes.auto.TimedAuton;

/**
 * TEST: TIMED NEAR AUTON with two starting spots, CORNER and EDGE. No sensors: every move is a set power for a set
 * TIME, like Timed Auton (same DRIVE_POWER and PAUSE_S). Same setup as Timed Auton NEAR: RED tile A6 / BLUE tile F1,
 * BACK touching our alliance wall, FRONT facing into the field. RED and BLUE drive the SAME moves.
 *
 * The moves are the Main Auton style robot moves the team picked (start = 0, 0; +X forward, +Y left), turned into
 * times with the speeds Timed Auton NEAR showed on the robot at DRIVE_POWER (0.3):
 *   forward about 23 in/s (23 in in 1.0 s), right about 15 in/s (12 in in 0.8 s), back about 22.5 in/s (18 in in 0.8 s)
 *
 *   CORNER: (0, -10)                      = 10 in RIGHT, staying near the corner
 *           NOTE: the back stays on the wall, and LEAVE needs the robot OFF the wall (scoring.md), so this probably
 *           scores PARK only, no LEAVE
 *   EDGE:   (24, 0) -> (23, -16) -> (2, -16) = 24 in FORWARD, 16 in RIGHT, 21 in BACK
 *
 * TUNE: measure each move with a charged battery; new time = old time x goal / measured (tuning guide).
 */
public abstract class TimedNearAuton extends TimedAuton {

    /** Which NEAR starting spot. Each OpMode file picks one. */
    public enum NearSpot { CORNER, EDGE }

    // STARTING GUESSES from Timed Auton NEAR's speeds (above). Tune on the robot (tuning-guide.md has a row for each).
    static final double NEAR_CORNER_RIGHT_S = 0.65;  // CORNER: right 10 in        (0.8 x 10 / 12)
    static final double NEAR_EDGE_FORWARD_S = 1.05;  // EDGE 1: forward 24 in      (1.0 x 24 / 23)
    static final double NEAR_EDGE_RIGHT_S = 1.05;    // EDGE 2: right 16 in        (0.8 x 16 / 12)
    static final double NEAR_EDGE_BACK_S = 0.95;     // EDGE 3: back 21 in to x = 2 (0.8 x 21 / 18). Stop before the wall

    protected abstract NearSpot nearSpot();

    @Override
    protected BasicAuton.Route route() {
        return BasicAuton.Route.NEAR;
    }

    @Override
    protected void driveRoute() {
        if (nearSpot() == NearSpot.CORNER) {
            show("CORNER: 10 in RIGHT");
            move(0, -DRIVE_POWER, NEAR_CORNER_RIGHT_S);   // right (+left = LEFT, decision 007, so right is minus)
        } else {
            show("EDGE 1 of 3: 24 in FORWARD (LEAVE)");
            move(DRIVE_POWER, 0, NEAR_EDGE_FORWARD_S);
            hold(PAUSE_S);
            show("EDGE 2 of 3: 16 in RIGHT");
            move(0, -DRIVE_POWER, NEAR_EDGE_RIGHT_S);
            hold(PAUSE_S);
            show("EDGE 3 of 3: 21 in BACK (PARK)");
            move(-DRIVE_POWER, 0, NEAR_EDGE_BACK_S);
        }
        show("Done");
    }

    /** One line on the Driver Station, so the drive team can see which move it is on. */
    private void show(String step) {
        telemetry.addData("TIMED NEAR AUTON", nearSpot());
        telemetry.addData("Step", step);
        telemetry.update();
    }
}
