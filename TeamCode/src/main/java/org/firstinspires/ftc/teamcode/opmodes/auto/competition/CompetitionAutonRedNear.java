package org.firstinspires.ftc.teamcode.opmodes.auto.competition;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.opmodes.auto.BasicAuton;
import org.firstinspires.ftc.teamcode.opmodes.auto.timed.TimedAuton;

/**
 * COMPETITION BACKUP (decision 069): NEAR for RED exactly as we ran it at the competition. Timed Auton with the
 * frozen COMP_NEAR_* times (forward 1.0 s, right 0.8 s, back 0.8 s at 0.3 power), which worked on the robot.
 * SET UP: tile A6, BACK touching the RED wall, FRONT facing into the field.
 * Don't change its times: tune Timed Auton NEAR EDGE / CORNER instead, so this one always stays a known-good backup.
 * After AUTO, "Main TeleOp RED" is queued up automatically.
 */
@Autonomous(name = "Auton Red Near", group = "Competition", preselectTeleOp = "Main TeleOp RED")
public class CompetitionAutonRedNear extends TimedAuton {

    @Override
    protected BasicAuton.Route route() {
        return BasicAuton.Route.NEAR;
    }

    @Override
    protected Spot spot() {
        return Spot.COMPETITION;
    }
}
