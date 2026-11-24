package org.firstinspires.ftc.teamcode.opmodes.auto.competition;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Alliance;
import org.firstinspires.ftc.teamcode.opmodes.auto.main.MainAuton;

/**
 * COMPETITION BACKUP (decision 069): FAR for RED exactly as we ran it at the competition. Main Auton (Pinpoint)
 * with the frozen MAIN_FAR_COMPETITION_PATH, (85, 0) -> (85, 22), and the competition's 2 s pause after each waypoint.
 * SET UP: tile B1, BACK touching the AUDIENCE wall, FRONT facing the FAR wall.
 * Don't change it: tune Main Auton FAR EDGE / CORNER instead, so this one always stays a known-good backup.
 * After AUTO, "Main TeleOp RED" is queued up automatically.
 */
@Autonomous(name = "Auton Red Far", group = "Competition", preselectTeleOp = "Main TeleOp RED")
public class CompetitionAutonRedFar extends MainAuton {

    @Override
    protected Alliance alliance() {
        return Alliance.RED;
    }

    @Override
    protected Route route() {
        return Route.FAR;
    }

    @Override
    protected FarSpot farSpot() {
        return FarSpot.COMPETITION;
    }
}
