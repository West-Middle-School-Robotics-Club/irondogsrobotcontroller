package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Timed Auton for RED, FAR start: tile B1, BACK touching the AUDIENCE wall, FRONT facing the FAR wall.
 * All the code is in TimedAuton.
 * This file only gives it a name, picks FAR and queues up Main TeleOp RED afterwards.
 */
@Autonomous(name = "Timed Auton RED FAR", group = "Timed Auton", preselectTeleOp = "Main TeleOp RED")
public class TimedAutonRedFar extends TimedAuton {

    @Override
    protected BasicAuton.Route route() {
        return BasicAuton.Route.FAR;
    }
}
