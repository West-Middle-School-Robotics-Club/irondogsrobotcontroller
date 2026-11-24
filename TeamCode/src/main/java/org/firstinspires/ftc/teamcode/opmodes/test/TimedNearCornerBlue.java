package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;  // for hiding it again (see below)

/**
 * TEST: Timed Near Auton for BLUE, NEAR CORNER spot: tile F1, BACK touching the BLUE wall, FRONT facing into the field.
 * Forward 4, right 10, back 2: ends near the corner, still off the wall. All the code is in TimedNearAuton. After AUTO, "Main TeleOp BLUE" is queued up automatically.
 */
// SHOWN on the Driver Station for testing (11/23). Test OpModes are hidden at competitions (decision 052):
// add a line "@Disabled" right above @Autonomous before a competition.
@Autonomous(name = "Timed Near Corner Blue", group = "Tests", preselectTeleOp = "Main TeleOp BLUE")
public class TimedNearCornerBlue extends TimedNearAuton {

    @Override
    protected NearSpot nearSpot() {
        return NearSpot.CORNER;
    }
}
