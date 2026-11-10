package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Alliance;

/**
 * TEST: camera as the RED alliance. Only red tags (30-37) should become the target (issue #7, decision 022).
 * See AllianceCameraTest for what to check.
 */
// Hidden on the Driver Station (decision 052). To use it: delete the @Disabled line below, then build and deploy.
@Disabled
@TeleOp(name = "Red Camera Test", group = "Tests")
public class RedCameraTest extends AllianceCameraTest {

    @Override
    protected Alliance alliance() {
        return Alliance.RED;
    }
}
