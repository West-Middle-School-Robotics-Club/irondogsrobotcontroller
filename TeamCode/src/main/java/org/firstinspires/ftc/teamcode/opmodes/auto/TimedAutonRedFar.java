package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;  // for hiding it again (see below)

/**
 * Timed Auton for RED, FAR start: tile B1, BACK touching the AUDIENCE wall, FRONT facing the FAR wall.
 * All the code is in TimedAuton.
 * This file only gives it a name, picks FAR and queues up Main TeleOp RED afterwards.
 */
// SHOWN on the Driver Station for testing (11/23). We run "Auton ... Far" (Main Auton) at matches (decision 064):
// add a line "@Disabled" right above @Autonomous before a competition.
@Autonomous(name = "Timed Auton RED FAR", group = "Timed Auton", preselectTeleOp = "Main TeleOp RED")
public class TimedAutonRedFar extends TimedAuton {

    @Override
    protected BasicAuton.Route route() {
        return BasicAuton.Route.FAR;
    }
}
