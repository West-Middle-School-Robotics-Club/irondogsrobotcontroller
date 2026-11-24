package org.firstinspires.ftc.teamcode.opmodes.auto.timed;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.BasicAuton;

/**
 * Timed Auton for RED, NEAR start, EDGE spot (tile A6, BACK touching the RED wall, FRONT facing into the field).
 * All the code (and the times) are in TimedAuton; this file only picks the route and the spot.
 * After AUTO, "Main TeleOp RED" is queued up automatically.
 */
@Autonomous(name = "Timed Auton Red Near Edge", group = "Timed Auton", preselectTeleOp = "Main TeleOp RED")
public class TimedAutonRedNearEdge extends TimedAuton {

    @Override
    protected BasicAuton.Route route() {
        return BasicAuton.Route.NEAR;
    }

    @Override
    protected Spot spot() {
        return Spot.EDGE;
    }
}
