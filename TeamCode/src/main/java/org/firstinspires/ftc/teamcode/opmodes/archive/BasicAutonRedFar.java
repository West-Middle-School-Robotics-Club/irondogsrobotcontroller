package org.firstinspires.ftc.teamcode.opmodes.archive;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.teamcode.Alliance;
import org.firstinspires.ftc.teamcode.opmodes.auto.BasicAuton;

/**
 * Basic Auton for RED, FAR start: tile B1, back against the AUDIENCE wall.
 * All the code is in BasicAuton; this file only picks the alliance and the route.
 * After AUTO, "Main TeleOp RED" is queued up automatically (G305 tip).
 */
// ARCHIVED (decision 060): we run Main Auton (FAR) and Timed Auton (NEAR) instead. Hidden on the Driver Station
// (decisions 052, 058). Delete @Disabled to bring it back.
@Disabled
@Autonomous(name = "Basic Auton RED FAR", group = "Basic Auton", preselectTeleOp = "Main TeleOp RED")
public class BasicAutonRedFar extends BasicAuton {

    @Override
    protected Alliance alliance() {
        return Alliance.RED;
    }

    @Override
    protected Route route() {
        return Route.FAR;
    }
}
