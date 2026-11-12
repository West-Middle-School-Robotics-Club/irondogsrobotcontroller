package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Main Auton for RED, FAR start: tile B1, BACK touching the AUDIENCE wall, FRONT facing the FAR wall.
 * All the code is in MainAuton (and BasicAuton); this file only picks the alliance and the route.
 * After AUTO, "Main TeleOp RED" is queued up automatically.
 */
@Autonomous(name = "Main Auton RED FAR", group = "Main Auton", preselectTeleOp = "Main TeleOp RED")
public class MainAutonRedFar extends MainAuton {

    @Override
    protected Alliance alliance() {
        return Alliance.RED;
    }

    @Override
    protected Route route() {
        return Route.FAR;
    }
}
