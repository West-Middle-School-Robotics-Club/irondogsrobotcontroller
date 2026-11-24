package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;  // for hiding it again (see below)

/**
 * TEST: Timed Far Auton for RED, FAR CORNER spot: tile B1, BACK touching the AUDIENCE wall, FRONT facing the FAR wall.
 * Forward up the field, then left into the LOADING ZONE. All the code is in TimedFarAuton.
 * After AUTO, "Main TeleOp RED" is queued up automatically.
 */
// SHOWN on the Driver Station for testing (11/23). Test OpModes are hidden at competitions (decision 052):
// add a line "@Disabled" right above @Autonomous before a competition.
@Autonomous(name = "Timed Far Corner Red", group = "Tests", preselectTeleOp = "Main TeleOp RED")
public class TimedFarCornerRed extends TimedFarAuton {

    @Override
    protected FarSpot farSpot() {
        return FarSpot.CORNER;
    }
}
