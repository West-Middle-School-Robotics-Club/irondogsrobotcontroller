package org.firstinspires.ftc.teamcode.opmodes.auto.competition;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Alliance;
import org.firstinspires.ftc.teamcode.opmodes.auto.main.MainAuton;

/**
 * COMPETITION BACKUP (decision 069): FAR for BLUE exactly as we ran it at the competition. Main Auton (Pinpoint)
 * with the frozen MAIN_FAR_COMPETITION_PATH, (85, 0) -> (85, 22), and the competition's 2 s pause after each waypoint.
 * SET UP: tile E6, BACK touching the FAR wall, FRONT facing the AUDIENCE.
 * Don't change it: tune Main Auton FAR EDGE / CORNER instead, so this one always stays a known-good backup.
 * After AUTO, "Main TeleOp BLUE" is queued up automatically.
 */
@Autonomous(name = "Auton Blue Far", group = "Competition", preselectTeleOp = "Main TeleOp BLUE")
public class CompetitionAutonBlueFar extends MainAuton {

    @Override
    protected Alliance alliance() {
        return Alliance.BLUE;
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
