package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;  // for hiding it again (see below)

/**
 * TEST: Timed Near Auton for RED, NEAR EDGE spot: tile A6, BACK touching the RED wall, FRONT facing into the field.
 * Forward 24, right 15, back (like MAIN_NEAR_EDGE_PATH). All the code is in TimedNearAuton. After AUTO, "Main TeleOp RED" is queued up automatically.
 */
// SHOWN on the Driver Station for testing (11/23). Test OpModes are hidden at competitions (decision 052):
// add a line "@Disabled" right above @Autonomous before a competition.
@Autonomous(name = "Timed Near Edge Red", group = "Tests", preselectTeleOp = "Main TeleOp RED")
public class TimedNearEdgeRed extends TimedNearAuton {

    @Override
    protected NearSpot nearSpot() {
        return NearSpot.EDGE;
    }
}
