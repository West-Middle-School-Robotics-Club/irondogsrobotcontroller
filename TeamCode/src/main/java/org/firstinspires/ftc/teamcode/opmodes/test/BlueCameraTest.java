package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * TEST: camera as the BLUE alliance. Only blue tags (38-45) should become the target (issue #7, decision 022).
 * See AllianceCameraTest for what to check.
 */
// Hidden on the Driver Station (decision 052). To use it: delete the @Disabled line below, then build and deploy.
@Disabled
@TeleOp(name = "Blue Camera Test", group = "Tests")
public class BlueCameraTest extends AllianceCameraTest {

    @Override
    protected Alliance alliance() {
        return Alliance.BLUE;
    }
}
