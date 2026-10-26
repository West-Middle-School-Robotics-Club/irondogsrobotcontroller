package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Alliance;

/** Main TeleOp when we are the BLUE alliance. All the driving code is in MainTeleOp. */
@TeleOp(name = "Main TeleOp BLUE", group = "TeleOp")
public class MainTeleOpBlue extends MainTeleOp {

    @Override
    protected Alliance alliance() {
        return Alliance.BLUE;
    }
}
