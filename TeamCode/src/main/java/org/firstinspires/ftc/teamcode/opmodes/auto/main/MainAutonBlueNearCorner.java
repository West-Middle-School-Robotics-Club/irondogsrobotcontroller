package org.firstinspires.ftc.teamcode.opmodes.auto.main;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Main Auton for BLUE, NEAR start, CORNER spot (tile F1, BACK touching the BLUE wall, FRONT facing into the field).
 * Drives MAIN_NEAR_CORNER_PATH (the numbers are in MainAuton.java). RED and BLUE drive the same moves.
 * All the code is in MainAuton (and BasicAuton); this file only picks the alliance, the route and the spot.
 * After AUTO, "Main TeleOp BLUE" is queued up automatically.
 */
@Autonomous(name = "Main Auton Blue Near Corner", group = "Main Auton", preselectTeleOp = "Main TeleOp BLUE")
public class MainAutonBlueNearCorner extends MainAuton {

    @Override
    protected Alliance alliance() {
        return Alliance.BLUE;
    }

    @Override
    protected Route route() {
        return Route.NEAR;
    }

    @Override
    protected NearSpot nearSpot() {
        return NearSpot.CORNER;
    }
}
