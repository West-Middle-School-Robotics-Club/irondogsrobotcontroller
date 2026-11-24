package org.firstinspires.ftc.teamcode.opmodes.auto.timed;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.opmodes.auto.BasicAuton;

/**
 * TIMED AUTON (decisions 054, 057, 060, 069): LEAVE + PARK with NO sensors at all, for NEAR and FAR, from an EDGE or a
 * CORNER spot. Eight small OpMode files in this folder use it, all in the "Timed Auton" group on the Driver Station:
 * Timed Auton Red / Blue  Near / Far  Edge / Corner. The competition BACKUP for NEAR (Spot.COMPETITION, frozen times)
 * is in auto/competition as "Auton Red Near" / "Auton Blue Near".
 *
 * It does not know where the robot is. It just runs the wheels at DRIVE_POWER for a set TIME, one move after
 * another, with a PAUSE_S stop between moves. Longer time = farther. The moves (and the Main Auton coordinates they
 * aim for, from a 0, 0 start: +X forward, +Y left):
 *
 *   NEAR EDGE    forward, right, back   NEAR_EDGE_FORWARD_S, NEAR_EDGE_RIGHT_S, NEAR_EDGE_BACK_S
 *   NEAR CORNER  forward, right, back   NEAR_CORNER_FORWARD_S, NEAR_CORNER_RIGHT_S, NEAR_CORNER_BACK_S
 *   FAR EDGE     forward, left          FAR_EDGE_FORWARD_S, FAR_EDGE_LEFT_S
 *   FAR CORNER   forward, left          FAR_CORNER_FORWARD_S, FAR_CORNER_LEFT_S
 * The distances to aim for are the matching Main Auton paths (MAIN_NEAR_EDGE_PATH, ... in main/MainAuton.java).
 *
 * No Pinpoint, no encoders (plain power, RUN_WITHOUT_ENCODER). If the Pinpoint or the encoders misbehave at a match,
 * this still drives. The price: battery level, bumps and wheel slip all change how far it goes.
 *
 * SET UP: BACK touching a wall (decision 046).
 *   NEAR  RED: tile A6, BACK touching the RED wall, FRONT facing into the field.
 *         BLUE: tile F1, BACK touching the BLUE wall, FRONT facing into the field.
 *   FAR   RED: tile B1, BACK touching the AUDIENCE wall, FRONT facing the FAR wall.
 *         BLUE: tile E6, BACK touching the FAR wall, FRONT facing the AUDIENCE.
 * RED and BLUE drive the SAME moves: BLUE is RED rotated 180 degrees.
 *
 * TUNE on the robot (tuning guide): measure each move with a charged battery; new time = old time x goal / measured.
 * NEAR moves must end with the robot OFF the wall, or there's no LEAVE.
 */
public abstract class TimedAuton extends LinearOpMode {

    /**
     * Which starting spot: EDGE (the one we've used) or CORNER (closer to the corner of the field).
     * COMPETITION = the NEAR auton exactly as we ran it at the competition (the backup, decision 069). NEAR only.
     */
    public enum Spot { EDGE, CORNER, COMPETITION }

    // How hard the wheels push (0 to 1) on every move. Plain power = % of the battery.
    static final double DRIVE_POWER = 0.3;

    // ---- NEAR COMPETITION: the BACKUP, exactly as we ran it at the competition (worked 11/11). DON'T CHANGE these:
    //      tune NEAR EDGE / CORNER instead, so we always have a NEAR auton that is known to work ----
    static final double COMP_NEAR_FORWARD_S = 1.0;  // 1. forward off the wall (23 in)
    static final double COMP_NEAR_RIGHT_S = 0.8;    // 2. strafe right, toward the audience (12 in)
    static final double COMP_NEAR_BACK_S = 0.8;     // 3. back into the LOADING ZONE (18 in)

    // ---- NEAR EDGE (forward, right, back into the LOADING ZONE) ----
    static final double NEAR_EDGE_FORWARD_S = 1.0;  // 1. forward off the wall (goal: 23-24 in)
    static final double NEAR_EDGE_RIGHT_S = 1.0;    // 2. strafe right, toward the audience
    static final double NEAR_EDGE_BACK_S = 0.9;     // 3. back into the LOADING ZONE (stop before the wall)

    // ---- NEAR CORNER (stays near the corner) ----
    // 1. forward ~4 in. A short move spends most of its time speeding up, so a bit more than 4 / 23 = 0.17 s
    static final double NEAR_CORNER_FORWARD_S = 0.35;
    static final double NEAR_CORNER_RIGHT_S = 0.65; // 2. strafe right ~10 in (0.8 x 10 / 12)
    // 3. back ~2 in. MUST stay shorter than the forward move, or the back touches the wall again (no LEAVE)
    static final double NEAR_CORNER_BACK_S = 0.15;

    // ---- FAR EDGE (forward up column B, then left into the LOADING ZONE) ----
    static final double FAR_EDGE_FORWARD_S = 5.0;   // 1. forward, the long drive (LEAVE)
    static final double FAR_EDGE_LEFT_S = 1.8;      // 2. strafe left into the LOADING ZONE (PARK; stop before the wall)

    // ---- FAR CORNER ----
    static final double FAR_CORNER_FORWARD_S = 5.0; // 1. forward, the long drive (LEAVE)
    static final double FAR_CORNER_LEFT_S = 1.7;    // 2. strafe left into the LOADING ZONE (PARK; stop before the wall)

    // Stop and sit still this long between moves, so one move's slide doesn't carry into the next.
    static final double PAUSE_S = 0.5;

    // Wait this long after START before moving (for when our partner's route crosses ours). Set before the match.
    static final double START_DELAY_S = 0.0;

    private Robot robot;

    /** NEAR or FAR. Each OpMode file picks one. */
    protected abstract BasicAuton.Route route();

    /** EDGE or CORNER. Each OpMode file picks one. */
    protected abstract Spot spot();

    @Override
    public void runOpMode() {
        // ---------- INIT: nothing moves (G304.H) ----------
        robot = new Robot(hardwareMap);
        robot.drivetrain.setEncoderSpeedControl(false);  // plain power, no encoders
        robot.drivetrain.setBrakeOnStop(true);           // stop when the power goes to 0 (decision 023)
        show("Ready: press START");

        waitForStart();
        robot.launcher.resetTrigger();   // START: trigger CLOSED (blocks the balls)

        // ---------- AUTO: one move at a time ----------
        hold(START_DELAY_S);
        boolean corner = spot() == Spot.CORNER;
        if (route() == BasicAuton.Route.NEAR) {
            // The three NEAR times for this spot: forward, right, back
            double forwardS = spot() == Spot.COMPETITION ? COMP_NEAR_FORWARD_S : corner ? NEAR_CORNER_FORWARD_S : NEAR_EDGE_FORWARD_S;
            double rightS = spot() == Spot.COMPETITION ? COMP_NEAR_RIGHT_S : corner ? NEAR_CORNER_RIGHT_S : NEAR_EDGE_RIGHT_S;
            double backS = spot() == Spot.COMPETITION ? COMP_NEAR_BACK_S : corner ? NEAR_CORNER_BACK_S : NEAR_EDGE_BACK_S;
            show("1 of 3: FORWARD off the wall (LEAVE)");
            move(DRIVE_POWER, 0, forwardS);
            hold(PAUSE_S);
            show("2 of 3: RIGHT");
            move(0, -DRIVE_POWER, rightS);   // +left = LEFT (decision 007), so right is minus
            hold(PAUSE_S);
            show("3 of 3: BACK (PARK, still off the wall)");
            move(-DRIVE_POWER, 0, backS);
        } else {
            // FAR has no COMPETITION spot (FAR at the competition was Main Auton); COMPETITION here drives FAR EDGE
            show("1 of 2: FORWARD up the field (LEAVE)");
            move(DRIVE_POWER, 0, corner ? FAR_CORNER_FORWARD_S : FAR_EDGE_FORWARD_S);
            hold(PAUSE_S);
            show("2 of 2: LEFT into the LOADING ZONE (PARK)");
            move(0, DRIVE_POWER, corner ? FAR_CORNER_LEFT_S : FAR_EDGE_LEFT_S);     // plus = LEFT (decision 007)
        }
        show("Done");

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

    /** One line on the Driver Station, so the drive team can see which move it is on. */
    private void show(String step) {
        telemetry.addData("TIMED AUTON", "%s %s", route(), spot());
        telemetry.addData("Step", step);
        telemetry.update();
    }
}
