package org.firstinspires.ftc.teamcode.opmodes.auto.timed;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.BasicAuton;

/**
 * Timed Auton for BLUE, FAR start, CORNER spot (tile E6, BACK touching the FAR wall, FRONT facing the AUDIENCE).
 * All the code (and the times) are in TimedAuton; this file only picks the route and the spot.
 * After AUTO, "Main TeleOp BLUE" is queued up automatically.
 */
@Autonomous(name = "Timed Auton Blue Far Corner", group = "Timed Auton", preselectTeleOp = "Main TeleOp BLUE")
public class TimedAutonBlueFarCorner extends TimedAuton {

    @Override
    protected BasicAuton.Route route() {
        return BasicAuton.Route.FAR;
    }

    @Override
    protected Spot spot() {
        return Spot.CORNER;
    }
}
