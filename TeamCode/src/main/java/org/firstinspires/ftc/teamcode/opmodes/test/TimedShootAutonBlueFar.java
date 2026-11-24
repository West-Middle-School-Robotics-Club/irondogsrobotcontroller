package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;  // for hiding it again (see below)

import org.firstinspires.ftc.teamcode.opmodes.auto.BasicAuton;

/**
 * TEST: Timed Shoot Auton for BLUE, FAR start: tile E6, BACK touching the FAR wall, FRONT facing the AUDIENCE.
 * Strafes right to D6, shoots, strafes back to E6, then drives Timed Auton FAR's route. All the code is in
 * TimedShootAuton. BLUE drives the same moves as RED (the field is rotated 180 degrees).
 * After AUTO, "Main TeleOp BLUE" is queued up automatically.
 */
// SHOWN on the Driver Station for testing (11/23). Test OpModes are hidden at competitions (decision 052):
// add a line "@Disabled" right above @Autonomous before a competition.
@Autonomous(name = "Timed Shoot Auton Blue Far", group = "Tests", preselectTeleOp = "Main TeleOp BLUE")
public class TimedShootAutonBlueFar extends TimedShootAuton {

    @Override
    protected BasicAuton.Route route() {
        return BasicAuton.Route.FAR;
    }
}
