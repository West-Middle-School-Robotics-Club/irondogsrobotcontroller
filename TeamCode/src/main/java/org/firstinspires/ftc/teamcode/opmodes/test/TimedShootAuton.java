package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.opmodes.auto.TimedAuton;

/**
 * TEST: TIMED SHOOT AUTON, FAR start. Timed Auton FAR, but it shoots its pre-loaded balls first.
 * Like Shoot Auton, but with NO sensors: every move is a set power for a set TIME (like Timed Auton).
 *
 * Same setup as Timed Auton FAR: RED in tile B1, BLUE in tile E6, BACK touching the wall.
 * RED and BLUE drive the SAME moves (Blue = Red turned 180 degrees).
 *
 *   1. Flywheel + intake + water wheel ON (gate CLOSED), and strafe RIGHT for SHOOT_RIGHT_S (goal: 22.5 in,
 *      RED B1 -> C1, BLUE E6 -> D6). The flywheel spins up while the robot moves
 *   2. Wait until the flywheel has had ShootAuton.FLYWHEEL_SPIN_UP_S, then HOLD the gate open for
 *      ShootAuton.SHOOT_TIME_S. Then everything OFF and the gate closed
 *   3. Strafe LEFT for SHOOT_RIGHT_S, back to the start
 *   4. Timed Far Auton's CORNER times: forward TimedFarAuton.FAR_CORNER_FORWARD_S, left FAR_CORNER_LEFT_S = PARK
 *      (the same numbers as Timed Far Corner, so they are tuned once for both)
 *
 * The shooting uses the SAME two numbers as Shoot Auton (FLYWHEEL_SPIN_UP_S, SHOOT_TIME_S in ShootAuton.java), so
 * they are tuned once for both. The turret is NOT moved.
 *
 * TUNE: SHOOT_RIGHT_S like the Timed Auton times: measure the strafe, new time = old time x 22.5 / measured.
 * Going right and coming back left use the same time, so it should end up back at the start; if it doesn't, the
 * FAR route that follows will be off by the same amount.
 */
public abstract class TimedShootAuton extends TimedAuton {

    // Tune on the robot (tuning-guide.md has a row). STARTING GUESS from Timed Auton NEAR: about 15 in per second
    // sideways at DRIVE_POWER, so 22.5 in = 1.5 s.
    static final double SHOOT_RIGHT_S = 1.5;   // strafe right to the shooting spot (goal: 22.5 in), and back left

    /** Shoot first (beforeFarRoute), then drive Timed Far Auton's CORNER times to PARK. */
    @Override
    protected void driveRoute() {
        beforeFarRoute();
        move(DRIVE_POWER, 0, TimedFarAuton.FAR_CORNER_FORWARD_S);   // forward up the field (LEAVE)
        hold(PAUSE_S);
        show("Left into the LOADING ZONE (PARK)");
        move(0, DRIVE_POWER, TimedFarAuton.FAR_CORNER_LEFT_S);      // left (plus = LEFT, decision 007)
        show("Done");
    }

    @Override
    protected void beforeFarRoute() {
        // 1. Flywheel, intake and water wheel ON; the gate stays CLOSED, so the balls wait at it
        ElapsedTime sinceFlywheelOn = new ElapsedTime();
        robot.launcher.resetTrigger();
        robot.launcher.spinUp();        // DEFAULT_SPIN_POWER, same as TeleOp
        robot.intake.in();
        robot.intake.waterWheelUp();
        show("Strafing RIGHT to the shooting spot (flywheel spinning up)");
        move(0, -DRIVE_POWER, SHOOT_RIGHT_S);   // right (+left = LEFT, decision 007, so right is minus)
        hold(PAUSE_S);

        // 2a. Give the flywheel the rest of its spin-up time (the strafe already counted)
        while (opModeIsActive() && sinceFlywheelOn.seconds() < ShootAuton.FLYWHEEL_SPIN_UP_S) {
            show("Flywheel spinning up (gate closed)");
        }

        // 2b. Gate HELD OPEN (fire() every loop, so it stays at FIRE the whole time)
        ElapsedTime shootTimer = new ElapsedTime();
        while (opModeIsActive() && shootTimer.seconds() < ShootAuton.SHOOT_TIME_S) {
            robot.launcher.fire();
            show(String.format("SHOOTING: gate held OPEN, %.1f s left", ShootAuton.SHOOT_TIME_S - shootTimer.seconds()));
        }

        // Done shooting: gate closed, everything OFF before we drive on
        robot.launcher.resetTrigger();
        robot.intake.stop();
        robot.launcher.stop();

        // 3. Back to the start, so Timed Auton FAR's moves work unchanged
        show("Strafing LEFT back to the start");
        move(0, DRIVE_POWER, SHOOT_RIGHT_S);
        hold(PAUSE_S);
        show("Forward up the field (Timed Far CORNER times)");
    }

    /** One line on the Driver Station, so the drive team can see which step it is on. */
    private void show(String step) {
        telemetry.addData("TIMED SHOOT AUTON", "FAR");
        telemetry.addData("Step", step);
        telemetry.update();
    }
}
