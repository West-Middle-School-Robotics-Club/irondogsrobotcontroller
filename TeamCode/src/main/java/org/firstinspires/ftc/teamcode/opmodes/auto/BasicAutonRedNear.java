package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Basic Auton for RED, NEAR start: tile A6, left side against the RED wall, next to our LOADING ZONE (A5).
 * All the code is in BasicAuton; this file only picks the alliance and the route.
 * After AUTO, "Main TeleOp RED" is queued up automatically (G305 tip).
 */
@Autonomous(name = "Basic Auton RED NEAR", group = "Basic Auton", preselectTeleOp = "Main TeleOp RED")
public class BasicAutonRedNear extends BasicAuton {

    @Override
    protected Alliance alliance() {
        return Alliance.RED;
    }

    @Override
    protected Route route() {
        return Route.NEAR;
    }
}
