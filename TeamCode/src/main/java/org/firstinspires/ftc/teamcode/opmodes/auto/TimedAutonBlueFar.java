package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

/**
 * Timed Auton for BLUE, FAR start: tile E6, BACK touching the FAR wall, FRONT facing the AUDIENCE.
 * All the code is in TimedAuton. BLUE drives the same moves as RED (the field is rotated 180 degrees).
 * This file only gives it a name, picks FAR and queues up Main TeleOp BLUE afterwards.
 */
// Hidden on the Driver Station (decision 064): we run the other auton for this spot.
// Delete @Disabled to bring it back.
@Disabled
@Autonomous(name = "Timed Auton BLUE FAR", group = "Timed Auton", preselectTeleOp = "Main TeleOp BLUE")
public class TimedAutonBlueFar extends TimedAuton {

    @Override
    protected BasicAuton.Route route() {
        return BasicAuton.Route.FAR;
    }
}
