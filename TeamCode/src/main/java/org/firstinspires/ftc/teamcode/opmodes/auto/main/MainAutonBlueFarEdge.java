package org.firstinspires.ftc.teamcode.opmodes.auto.main;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Main Auton for BLUE, FAR start, EDGE spot (tile E6, BACK touching the FAR wall, FRONT facing the AUDIENCE).
 * Drives MAIN_FAR_EDGE_PATH (the numbers are in MainAuton.java). RED and BLUE drive the same moves.
 * All the code is in MainAuton (and BasicAuton); this file only picks the alliance, the route and the spot.
 * After AUTO, "Main TeleOp BLUE" is queued up automatically.
 */
@Autonomous(name = "Main Auton Blue Far Edge", group = "Main Auton", preselectTeleOp = "Main TeleOp BLUE")
public class MainAutonBlueFarEdge extends MainAuton {

    @Override
    protected Alliance alliance() {
        return Alliance.BLUE;
    }

    @Override
    protected Route route() {
        return Route.FAR;
    }

    @Override
    protected FarSpot farSpot() {
        return FarSpot.EDGE;
    }
}
