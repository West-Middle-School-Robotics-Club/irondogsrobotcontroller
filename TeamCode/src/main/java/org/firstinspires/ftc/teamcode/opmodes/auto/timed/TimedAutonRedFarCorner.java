package org.firstinspires.ftc.teamcode.opmodes.auto.timed;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.BasicAuton;

/**
 * Timed Auton for RED, FAR start, CORNER spot (tile B1, BACK touching the AUDIENCE wall, FRONT facing the FAR wall).
 * All the code (and the times) are in TimedAuton; this file only picks the route and the spot.
 * After AUTO, "Main TeleOp RED" is queued up automatically.
 */
@Autonomous(name = "Timed Auton Red Far Corner", group = "Timed Auton", preselectTeleOp = "Main TeleOp RED")
public class TimedAutonRedFarCorner extends TimedAuton {

    @Override
    protected BasicAuton.Route route() {
        return BasicAuton.Route.FAR;
    }

    @Override
    protected Spot spot() {
        return Spot.CORNER;
    }
}
