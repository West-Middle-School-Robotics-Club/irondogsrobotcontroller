package org.firstinspires.ftc.teamcode.opmodes.auto.timed;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.BasicAuton;

/**
 * Timed Auton for BLUE, NEAR start, CORNER spot (tile F1, BACK touching the BLUE wall, FRONT facing into the field).
 * All the code (and the times) are in TimedAuton; this file only picks the route and the spot.
 * After AUTO, "Main TeleOp BLUE" is queued up automatically.
 */
@Autonomous(name = "Timed Auton Blue Near Corner", group = "Timed Auton", preselectTeleOp = "Main TeleOp BLUE")
public class TimedAutonBlueNearCorner extends TimedAuton {

    @Override
    protected BasicAuton.Route route() {
        return BasicAuton.Route.NEAR;
    }

    @Override
    protected Spot spot() {
        return Spot.CORNER;
    }
}
