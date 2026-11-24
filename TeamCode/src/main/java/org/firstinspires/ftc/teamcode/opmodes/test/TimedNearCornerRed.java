package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;  // for hiding it again (see below)

/**
 * TEST: Timed Near Auton for RED, NEAR CORNER spot: tile A6, BACK touching the RED wall, FRONT facing into the field.
 * Forward 4, right 10, back 2: ends near the corner, still off the wall. All the code is in TimedNearAuton. After AUTO, "Main TeleOp RED" is queued up automatically.
 */
// SHOWN on the Driver Station for testing (11/23). Test OpModes are hidden at competitions (decision 052):
// add a line "@Disabled" right above @Autonomous before a competition.
@Autonomous(name = "Timed Near Corner Red", group = "Tests", preselectTeleOp = "Main TeleOp RED")
public class TimedNearCornerRed extends TimedNearAuton {

    @Override
    protected NearSpot nearSpot() {
        return NearSpot.CORNER;
    }
}
