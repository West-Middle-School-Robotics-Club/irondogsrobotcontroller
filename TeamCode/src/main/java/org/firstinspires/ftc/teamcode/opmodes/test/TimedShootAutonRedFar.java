package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;  // for hiding it again (see below)

import org.firstinspires.ftc.teamcode.opmodes.auto.BasicAuton;

/**
 * TEST: Timed Shoot Auton for RED, FAR start: tile B1, BACK touching the AUDIENCE wall, FRONT facing the FAR wall.
 * Strafes right to C1, shoots, strafes back to B1, then drives Timed Auton FAR's route. All the code is in
 * TimedShootAuton. After AUTO, "Main TeleOp RED" is queued up automatically.
 */
// SHOWN on the Driver Station for testing (11/23). Test OpModes are hidden at competitions (decision 052):
// add a line "@Disabled" right above @Autonomous before a competition.
@Autonomous(name = "Timed Shoot Auton Red Far", group = "Tests", preselectTeleOp = "Main TeleOp RED")
public class TimedShootAutonRedFar extends TimedShootAuton {

    @Override
    protected BasicAuton.Route route() {
        return BasicAuton.Route.FAR;
    }
}
