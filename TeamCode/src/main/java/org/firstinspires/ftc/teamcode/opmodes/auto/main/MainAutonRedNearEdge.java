package org.firstinspires.ftc.teamcode.opmodes.auto.main;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Main Auton for RED, NEAR start, EDGE spot (tile A6, BACK touching the RED wall, FRONT facing into the field).
 * Drives MAIN_NEAR_EDGE_PATH (the numbers are in MainAuton.java). RED and BLUE drive the same moves.
 * All the code is in MainAuton (and BasicAuton); this file only picks the alliance, the route and the spot.
 * After AUTO, "Main TeleOp RED" is queued up automatically.
 */
@Autonomous(name = "Main Auton Red Near Edge", group = "Main Auton", preselectTeleOp = "Main TeleOp RED")
public class MainAutonRedNearEdge extends MainAuton {

    @Override
    protected Alliance alliance() {
        return Alliance.RED;
    }

    @Override
    protected Route route() {
        return Route.NEAR;
    }

    @Override
    protected NearSpot nearSpot() {
        return NearSpot.EDGE;
    }
}
