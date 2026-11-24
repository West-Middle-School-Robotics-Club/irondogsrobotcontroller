package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;  // for hiding it again (see below)

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Main Auton for RED, NEAR start, CORNER spot: tile A6, BACK touching the RED wall, FRONT facing into the field.
 * Drives MAIN_NEAR_CORNER_PATH: forward 24, right 5, back to 2 in from the start (stays near the corner).
 * All the code is in MainAuton (and BasicAuton); this file only picks the alliance, the route and the spot.
 * After AUTO, "Main TeleOp RED" is queued up automatically.
 */
// SHOWN on the Driver Station for testing (11/23, decision 068). At matches we run "Auton Red Near" (Timed Auton):
// add a line "@Disabled" right above @Autonomous to hide it before a competition.
@Autonomous(name = "Main Auton Red Near Corner", group = "Main Auton", preselectTeleOp = "Main TeleOp RED")
public class MainAutonRedNearCorner extends MainAuton {

    @Override
    protected Alliance alliance() {
        return Alliance.RED;
    }

    @Override
    protected Route route() {
        return Route.NEAR;
    }

    @Override
    protected NearSpot nearSpot() {
        return NearSpot.CORNER;
    }
}
