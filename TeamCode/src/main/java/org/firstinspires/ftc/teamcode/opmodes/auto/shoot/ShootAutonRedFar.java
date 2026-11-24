package org.firstinspires.ftc.teamcode.opmodes.auto.shoot;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Shoot Auton for RED, FAR start, EDGE spot (tile B1, BACK touching the AUDIENCE wall, FRONT facing the FAR wall).
 * Moves 22.5 in right, shoots, comes back, then drives Main Auton FAR EDGE's route. All the code is in ShootAuton.
 * After AUTO, "Main TeleOp RED" is queued up automatically.
 */
@Autonomous(name = "Shoot Auton Red Far", group = "Shoot Auton", preselectTeleOp = "Main TeleOp RED")
public class ShootAutonRedFar extends ShootAuton {

    @Override
    protected Alliance alliance() {
        return Alliance.RED;
    }

    @Override
    protected Route route() {
        return Route.FAR;
    }
}
