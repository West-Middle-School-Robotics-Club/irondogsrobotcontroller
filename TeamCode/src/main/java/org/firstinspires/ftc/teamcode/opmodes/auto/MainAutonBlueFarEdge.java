package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Main Auton for BLUE, FAR start, EDGE spot (the spot we've been using): tile E6, BACK touching the FAR wall,
 * FRONT facing the AUDIENCE. Drives MAIN_FAR_EDGE_PATH: forward 83, left 22 (the same moves as RED).
 * All the code is in MainAuton (and BasicAuton); this file only picks the alliance, the route and the spot.
 * After AUTO, "Main TeleOp BLUE" is queued up automatically.
 */
// Driver Station name (decision 066): "Auton Blue Far Edge", next to "Auton Blue Far Corner"
@Autonomous(name = "Auton Blue Far Edge", group = "Auton", preselectTeleOp = "Main TeleOp BLUE")
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
