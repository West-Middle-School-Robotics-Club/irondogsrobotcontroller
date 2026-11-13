package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

/**
 * Timed Auton for RED, FAR start: tile B1, BACK touching the AUDIENCE wall, FRONT facing the FAR wall.
 * All the code is in TimedAuton.
 * This file only gives it a name, picks FAR and queues up Main TeleOp RED afterwards.
 */
// Hidden on the Driver Station (decision 064): we run the other auton for this spot.
// Delete @Disabled to bring it back.
@Disabled
@Autonomous(name = "Timed Auton RED FAR", group = "Timed Auton", preselectTeleOp = "Main TeleOp RED")
public class TimedAutonRedFar extends TimedAuton {

    @Override
    protected BasicAuton.Route route() {
        return BasicAuton.Route.FAR;
    }
}
