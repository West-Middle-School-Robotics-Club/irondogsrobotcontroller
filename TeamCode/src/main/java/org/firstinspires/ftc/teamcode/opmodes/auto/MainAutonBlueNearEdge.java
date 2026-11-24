package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;  // for hiding it again (see below)

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Main Auton for BLUE, NEAR start, EDGE spot: tile F1, BACK touching the BLUE wall, FRONT facing into the field.
 * Drives MAIN_NEAR_EDGE_PATH: forward 24, right 15, back to 2 in from the start.
 * All the code is in MainAuton (and BasicAuton); this file only picks the alliance, the route and the spot.
 * After AUTO, "Main TeleOp BLUE" is queued up automatically.
 */
// SHOWN on the Driver Station for testing (11/23, decision 068). At matches we run "Auton Blue Near" (Timed Auton):
// add a line "@Disabled" right above @Autonomous to hide it before a competition.
@Autonomous(name = "Main Auton Blue Near Edge", group = "Main Auton", preselectTeleOp = "Main TeleOp BLUE")
public class MainAutonBlueNearEdge extends MainAuton {

    @Override
    protected Alliance alliance() {
        return Alliance.BLUE;
    }

    @Override
    protected Route route() {
        return Route.NEAR;
    }

    @Override
    protected NearSpot nearSpot() {
        return NearSpot.EDGE;
    }
}
