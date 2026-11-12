package org.firstinspires.ftc.teamcode.opmodes.archive;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.teamcode.Alliance;
import org.firstinspires.ftc.teamcode.opmodes.auto.BasicAuton;

/**
 * Basic Auton for RED, NEAR start: tile A6, BACK against the RED wall, facing into the field, next to our LOADING ZONE (A5).
 * All the code is in BasicAuton; this file only picks the alliance and the route.
 * After AUTO, "Main TeleOp RED" is queued up automatically (G305 tip).
 */
// ARCHIVED (decision 060): we run Main Auton (FAR) and Timed Auton (NEAR) instead. Hidden on the Driver Station
// (decisions 052, 058). Delete @Disabled to bring it back.
@Disabled
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
