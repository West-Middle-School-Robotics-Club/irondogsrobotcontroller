package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Main Auton for RED, FAR start, CORNER spot: closer to the corner of the field, BACK touching the AUDIENCE wall,
 * FRONT facing the FAR wall. Drives MAIN_FAR_CORNER_PATH: forward 87, left 22.
 * All the code is in MainAuton (and BasicAuton); this file only picks the alliance, the route and the spot.
 * After AUTO, "Main TeleOp RED" is queued up automatically.
 */
// Driver Station name (decision 066): "Auton Red Far Corner", next to "Auton Red Far Edge"
@Autonomous(name = "Auton Red Far Corner", group = "Auton", preselectTeleOp = "Main TeleOp RED")
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
