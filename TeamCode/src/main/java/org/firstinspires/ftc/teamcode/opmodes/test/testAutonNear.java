package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

/**
 * Test Auton, NEAR route from a (0, 0, 0) start on any clear floor. All the code is in testAuton (and BasicAuton).
 */
// Hidden on the Driver Station (decision 052). To use it: delete the @Disabled line below, then build and deploy.
@Autonomous(name = "Test Auton NEAR", group = "Tests")
public class testAutonNear extends testAuton {

    @Override
    protected Route route() {
        return Route.NEAR;
    }
}
