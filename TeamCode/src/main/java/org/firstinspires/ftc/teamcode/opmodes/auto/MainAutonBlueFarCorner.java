package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Main Auton for BLUE, FAR start, CORNER spot: closer to the corner of the field, BACK touching the FAR wall,
 * FRONT facing the AUDIENCE. Drives MAIN_FAR_CORNER_PATH: forward 87, left 22 (the same moves as RED).
 * All the code is in MainAuton (and BasicAuton); this file only picks the alliance, the route and the spot.
 * After AUTO, "Main TeleOp BLUE" is queued up automatically.
 */
// Driver Station name (decision 066): "Auton Blue Far Corner", next to "Auton Blue Far Edge"
@Autonomous(name = "Auton Blue Far Corner", group = "Auton", preselectTeleOp = "Main TeleOp BLUE")
public class MainAutonBlueFarCorner extends MainAuton {

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
        return FarSpot.CORNER;
    }
}
