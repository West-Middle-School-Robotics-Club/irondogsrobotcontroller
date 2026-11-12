package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Basic Auton for BLUE, NEAR start: tile F1, BACK against the BLUE wall, facing into the field, next to our LOADING ZONE (F2).
 * All the code is in BasicAuton; this file only picks the alliance and the route.
 * After AUTO, "Main TeleOp BLUE" is queued up automatically (G305 tip).
 */
// Hidden on the Driver Station: we use the Timed Auton backups for now (decision 058). Delete @Disabled to bring it back.
@Disabled
@Autonomous(name = "Basic Auton BLUE NEAR", group = "Basic Auton", preselectTeleOp = "Main TeleOp BLUE")
public class BasicAutonBlueNear extends BasicAuton {

    @Override
    protected Alliance alliance() {
        return Alliance.BLUE;
    }

    @Override
    protected Route route() {
        return Route.NEAR;
    }
}
