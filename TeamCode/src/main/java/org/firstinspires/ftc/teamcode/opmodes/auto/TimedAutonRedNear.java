package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Timed Auton for RED, NEAR start: tile A6, BACK touching the RED wall, FRONT facing into the field.
 * All the code is in TimedAuton. This file only gives it a name and queues up Main TeleOp RED afterwards.
 */
@Autonomous(name = "Timed Auton RED NEAR", group = "Timed Auton", preselectTeleOp = "Main TeleOp RED")
public class TimedAutonRedNear extends TimedAuton {

    @Override
    protected BasicAuton.Route route() {
        return BasicAuton.Route.NEAR;
    }
}
