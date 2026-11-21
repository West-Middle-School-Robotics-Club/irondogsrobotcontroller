package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * TEST: Shoot Auton for BLUE, FAR start: tile E6, BACK touching the FAR wall, FRONT facing the AUDIENCE.
 * Moves right to D6, shoots, comes back to E6, then drives Main Auton FAR's route. All the code is in ShootAuton.
 * After AUTO, "Main TeleOp BLUE" is queued up automatically.
 */
// Hidden on the Driver Station (decision 052). To use it: delete the @Disabled line below, then build and deploy.
@Disabled
@Autonomous(name = "Shoot Auton Blue Far", group = "Tests", preselectTeleOp = "Main TeleOp BLUE")
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
