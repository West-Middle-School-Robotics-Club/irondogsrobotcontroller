package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

/**
 * Test Auton, FAR route from a (0, 0, 0) start on any clear floor (needs about 8 ft). All the code is in testAuton (and BasicAuton).
 */
// Hidden on the Driver Station (decision 052). To use it: delete the @Disabled line below, then build and deploy.
@Autonomous(name = "Test Auton FAR", group = "Tests")
public class testAutonFar extends testAuton {

    @Override
    protected Route route() {
        return Route.FAR;
    }
}
