package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;  // for hiding it again (see below)

/**
 * Timed Auton for BLUE, FAR start: tile E6, BACK touching the FAR wall, FRONT facing the AUDIENCE.
 * All the code is in TimedAuton. BLUE drives the same moves as RED (the field is rotated 180 degrees).
 * This file only gives it a name, picks FAR and queues up Main TeleOp BLUE afterwards.
 */
// SHOWN on the Driver Station for testing (11/23). We run "Auton ... Far" (Main Auton) at matches (decision 064):
// add a line "@Disabled" right above @Autonomous before a competition.
@Autonomous(name = "Timed Auton BLUE FAR", group = "Timed Auton", preselectTeleOp = "Main TeleOp BLUE")
public class TimedAutonBlueFar extends TimedAuton {

    @Override
    protected BasicAuton.Route route() {
        return BasicAuton.Route.FAR;
    }
}
