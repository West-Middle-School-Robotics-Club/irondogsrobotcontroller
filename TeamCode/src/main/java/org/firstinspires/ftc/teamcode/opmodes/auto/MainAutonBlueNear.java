package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Main Auton for BLUE, NEAR start: tile F1, BACK touching the BLUE wall, FRONT facing into the field.
 * All the code is in MainAuton (and BasicAuton); this file only picks the alliance and the route.
 * After AUTO, "Main TeleOp BLUE" is queued up automatically.
 */
@Autonomous(name = "Main Auton BLUE NEAR", group = "Main Auton", preselectTeleOp = "Main TeleOp BLUE")
public class MainAutonBlueNear extends MainAuton {

    @Override
    protected Alliance alliance() {
        return Alliance.BLUE;
    }

    @Override
    protected Route route() {
        return Route.NEAR;
    }
}
