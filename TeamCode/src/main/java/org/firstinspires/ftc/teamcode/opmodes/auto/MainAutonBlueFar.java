package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Main Auton for BLUE, FAR start: tile E6, BACK touching the FAR wall, FRONT facing the AUDIENCE.
 * All the code is in MainAuton (and BasicAuton); this file only picks the alliance and the route.
 * After AUTO, "Main TeleOp BLUE" is queued up automatically.
 */
// Driver Station name is just "Auton Blue Far" so the drive team isn't confused (decision 064)
@Autonomous(name = "Auton Blue Far", group = "Auton", preselectTeleOp = "Main TeleOp BLUE")
public class MainAutonBlueFar extends MainAuton {

    @Override
    protected Alliance alliance() {
        return Alliance.BLUE;
    }

    @Override
    protected Route route() {
        return Route.FAR;
    }
}
