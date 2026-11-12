package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Basic Auton for RED, NEAR start: tile A6, BACK against the RED wall, facing into the field, next to our LOADING ZONE (A5).
 * All the code is in BasicAuton; this file only picks the alliance and the route.
 * After AUTO, "Main TeleOp RED" is queued up automatically (G305 tip).
 */
// Hidden on the Driver Station: we use the Timed Auton backups for now (decision 058). Delete @Disabled to bring it back.
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
