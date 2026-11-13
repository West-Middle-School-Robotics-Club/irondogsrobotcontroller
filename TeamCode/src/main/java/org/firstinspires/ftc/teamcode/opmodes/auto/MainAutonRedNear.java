package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * Main Auton for RED, NEAR start: tile A6, BACK touching the RED wall, FRONT facing into the field.
 * All the code is in MainAuton (and BasicAuton); this file only picks the alliance and the route.
 * After AUTO, "Main TeleOp RED" is queued up automatically.
 */
// Hidden on the Driver Station (decision 064): we run the other auton for this spot.
// Delete @Disabled to bring it back.
@Disabled
@Autonomous(name = "Main Auton RED NEAR", group = "Main Auton", preselectTeleOp = "Main TeleOp RED")
public class MainAutonRedNear extends MainAuton {

    @Override
    protected Alliance alliance() {
        return Alliance.RED;
    }

    @Override
    protected Route route() {
        return Route.NEAR;
    }
}
