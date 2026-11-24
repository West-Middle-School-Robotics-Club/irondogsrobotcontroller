package org.firstinspires.ftc.teamcode.opmodes.auto.shoot;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Shoot Auton for BLUE, FAR start, EDGE spot (tile E6, BACK touching the FAR wall, FRONT facing the AUDIENCE).
 * Moves 22.5 in right, shoots, comes back, then drives Main Auton FAR EDGE's route. All the code is in ShootAuton.
 * After AUTO, "Main TeleOp BLUE" is queued up automatically.
 */
@Autonomous(name = "Shoot Auton Blue Far", group = "Shoot Auton", preselectTeleOp = "Main TeleOp BLUE")
public class ShootAutonBlueFar extends ShootAuton {

    @Override
    protected Alliance alliance() {
        return Alliance.BLUE;
    }

    @Override
    protected Route route() {
        return Route.FAR;
    }
}
