package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Basic Auton for RED, FAR start: tile B1, back against the AUDIENCE wall.
 * All the code is in BasicAuton; this file only picks the alliance and the route.
 * After AUTO, "Main TeleOp RED" is queued up automatically (G305 tip).
 */
// Hidden on the Driver Station: we use the Timed Auton backups for now (decision 058). Delete @Disabled to bring it back.
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
