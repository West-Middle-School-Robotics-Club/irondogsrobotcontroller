package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * TEST: Shoot Auton for RED, FAR start: tile B1, BACK touching the AUDIENCE wall, FRONT facing the FAR wall.
 * Moves right to C1, shoots, comes back to B1, then drives Main Auton FAR's route. All the code is in ShootAuton.
 * After AUTO, "Main TeleOp RED" is queued up automatically.
 */
// Hidden on the Driver Station (decision 052). To use it: delete the @Disabled line below, then build and deploy.
@Autonomous(name = "Shoot Auton Red Far", group = "Tests", preselectTeleOp = "Main TeleOp RED")
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
