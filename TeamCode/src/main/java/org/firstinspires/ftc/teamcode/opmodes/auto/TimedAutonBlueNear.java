package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Timed Auton (BACKUP) for BLUE, NEAR start: tile F1, BACK touching the BLUE wall, FRONT facing into the field.
 * All the code is in TimedAuton. BLUE drives the same moves as RED (the field is rotated 180 degrees).
 * This file only gives it a name and queues up Main TeleOp BLUE afterwards.
 */
@Autonomous(name = "Timed Auton BLUE NEAR (backup)", group = "Timed Auton", preselectTeleOp = "Main TeleOp BLUE")
public class TimedAutonBlueNear extends TimedAuton {
}
