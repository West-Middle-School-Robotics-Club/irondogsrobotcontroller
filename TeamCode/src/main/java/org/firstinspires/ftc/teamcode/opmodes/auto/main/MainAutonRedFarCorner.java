package org.firstinspires.ftc.teamcode.opmodes.auto.main;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Main Auton for RED, FAR start, CORNER spot (tile B1, BACK touching the AUDIENCE wall, FRONT facing the FAR wall).
 * Drives MAIN_FAR_CORNER_PATH (the numbers are in MainAuton.java). RED and BLUE drive the same moves.
 * All the code is in MainAuton (and BasicAuton); this file only picks the alliance, the route and the spot.
 * After AUTO, "Main TeleOp RED" is queued up automatically.
 */
@Autonomous(name = "Main Auton Red Far Corner", group = "Main Auton", preselectTeleOp = "Main TeleOp RED")
public class MainAutonRedFarCorner extends MainAuton {

    @Override
    protected Alliance alliance() {
        return Alliance.RED;
    }

    @Override
    protected Route route() {
        return Route.FAR;
    }

    @Override
    protected FarSpot farSpot() {
        return FarSpot.CORNER;
    }
}
