package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;  // for hiding it again (see below)

/**
 * TEST: Timed Far Auton for BLUE, FAR EDGE spot: tile E6, BACK touching the FAR wall, FRONT facing the AUDIENCE.
 * Forward up the field, then left into the LOADING ZONE. All the code is in TimedFarAuton.
 * After AUTO, "Main TeleOp BLUE" is queued up automatically.
 */
// SHOWN on the Driver Station for testing (11/23). Test OpModes are hidden at competitions (decision 052):
// add a line "@Disabled" right above @Autonomous before a competition.
@Autonomous(name = "Timed Far Edge Blue", group = "Tests", preselectTeleOp = "Main TeleOp BLUE")
public class TimedFarEdgeBlue extends TimedFarAuton {

    @Override
    protected FarSpot farSpot() {
        return FarSpot.EDGE;
    }
}
