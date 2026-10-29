package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.subsystems.Light;
import org.firstinspires.ftc.teamcode.subsystems.Turret;
import org.firstinspires.ftc.teamcode.subsystems.Power;

/**
 * TEST: turn the turret with the dpad, learn how the ELC encoder works, and calibrate (issue #31).
 * The turret servo is in CONTINUOUS mode (decision 021): the dpad sets a SPEED, and the encoder tells us the angle.
 *
 * Controls (gamepad 1), all after START:
 *   HOLD dpad left / right   turn the turret left / right
 *     + hold RIGHT TRIGGER   FULL speed (only once homed; before that, everything is capped at NOT_HOMED_MAX_SPEED)
 *     + hold a bumper        SLOW, fine moves
 *   Y                        set a MARK: "Since mark" counts from here. Use it to measure a move
 *   A                        turn to forward (0°)      } these use the encoder to aim,
 *   X / B                    turn to 45° left / right  } so they only work once HOMED
 *   any dpad left/right      cancels A / X / B
 *   dpad up / down           tilt the HOOD up / down a little (find HOOD_LOWEST / HIGHEST_POSITION)
 *
 * The first time the forward switch triggers, the encoder is HOMED (that spot = 0).
 * The LEFT light turns GREEN whenever the turret is at HOME: at the switch, or within ±HOME_TOLERANCE_COUNTS of 0.
 * Until then, the turret only moves slowly.
 *
 * Calibrate IN ORDER (steps in issue #31). Don't try A / X / B until steps 1 and 2 are right!
 *   1. Dpad LEFT must turn the turret LEFT. If not: SERVO_REVERSED = true in Turret.java
 *   2. Turning LEFT must make "Turret angle" go UP. If not: ENCODER_REVERSED = true
 *   3. Turn exactly 90° left: "Turret angle" should read about 90. If not: fix ENCODER_REVS_PER_TURRET_REV
 *   4. Set MAX_LEFT_DEGREES / MAX_RIGHT_DEGREES
 *   5. Tune AIM_GAIN with A / X / B
 *
 * How the ELC encoder works:
 *   It counts tiny steps as its shaft turns: 4000 counts = 1 full turn, so about 11 counts = 1 degree.
 *   Turning one way counts UP, the other way counts DOWN. It starts at 0 when the robot powers on,
 *   which is why we home it at the forward switch.
 */
@TeleOp(name = "Turret Test", group = "Tests")
public class TurretTest extends LinearOpMode {

    // Dpad speeds (0 to 1). Before homing, Turret limits everything to NOT_HOMED_MAX_SPEED anyway.
    static final double DPAD_FULL_SPEED = 1.0;   // + right trigger
    static final double DPAD_SPEED = 0.5;
    static final double DPAD_SLOW_SPEED = 0.2;   // + bumper. Much lower and the servo may not move the turret at all

    @Override
    public void runOpMode() {
        Power power = new Power(hardwareMap); // battery voltage + current on every test
        Turret turret = new Turret(hardwareMap);
        Light light = new Light(hardwareMap, Light.LEFT_LIGHT_NAME);
        // Test-only: the same encoder, used directly to show its speed (code-structure rule 8).
        DcMotorEx turretEncoder = hardwareMap.get(DcMotorEx.class, Turret.ENCODER_NAME);
        // Test-only: the same switch, to show its RAW reading (code-structure rule 8).
        DigitalChannel forwardSwitch = hardwareMap.get(DigitalChannel.class, Turret.FORWARD_SWITCH_NAME);
        ElapsedTime sinceMarkSet = new ElapsedTime(1000); // starts "long ago", so no message at first

        int markCounts = turret.getEncoderCounts();
        boolean dpadWasHeld = false;

        // INIT: nothing moves until START (game rule G304.H). Readings, homing and the light work already.
        while (opModeInInit()) {
            turret.update();
            showForward(turret, light);
            telemetry.addLine("Press START. Nothing moves until you press a button.");
            telemetry.addLine("Don't force the turret by hand: the servo's gears can strip.");
            telemetry.addLine();
            addReadings(turret, turretEncoder, forwardSwitch, markCounts, sinceMarkSet);
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        while (opModeIsActive()) {
            // Dpad = manual speed. Letting go stops the turret.
            double speed = DPAD_SPEED;
            if (gamepad1.right_trigger > 0.5) {
                speed = DPAD_FULL_SPEED;
            } else if (gamepad1.left_bumper || gamepad1.right_bumper) {
                speed = DPAD_SLOW_SPEED;
            }
            if (gamepad1.dpad_left) {
                turret.setManualSpeed(speed);
                dpadWasHeld = true;
            } else if (gamepad1.dpad_right) {
                turret.setManualSpeed(-speed);
                dpadWasHeld = true;
            } else if (dpadWasHeld) {
                turret.setManualSpeed(0.0);
                dpadWasHeld = false;
            }

            // Hood: dpad up/down nudges it. The first press starts from HOOD_START_POSITION.
            double hood = Double.isNaN(turret.getHoodPosition()) ? Turret.HOOD_START_POSITION : turret.getHoodPosition();
            if (gamepad1.dpadUpWasPressed()) {
                turret.setHoodPosition(hood + 0.02);
            }
            if (gamepad1.dpadDownWasPressed()) {
                turret.setHoodPosition(hood - 0.02);
            }

            if (gamepad1.yWasPressed()) {
                markCounts = turret.getEncoderCounts();
                sinceMarkSet.reset();
            }
            if (gamepad1.aWasPressed()) {
                turret.turnForward();
            }
            if (gamepad1.xWasPressed()) {
                turret.turnTo(45);
            }
            if (gamepad1.bWasPressed()) {
                turret.turnTo(-45);
            }

            turret.update();
            showForward(turret, light);

            telemetry.addLine("HOLD dpad L/R = turn (+R trigger = FULL, +bumper = slow)   dpad U/D = hood");
            telemetry.addLine("Y = mark   A = forward   X/B = 45° L/R");
            if (!turret.isHomed()) {
                telemetry.addData("Speed capped", "%.2f until homed: turn past forward once", Turret.NOT_HOMED_MAX_SPEED);
            }
            telemetry.addLine();
            addReadings(turret, turretEncoder, forwardSwitch, markCounts, sinceMarkSet);
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        turret.stop();
        light.off();
    }

    /** GREEN when the turret is at home, OFF otherwise. The light is OK during INIT: it doesn't move anything. */
    private void showForward(Turret turret, Light light) {
        light.setColor(turret.isAtHome() ? Light.Color.GREEN : Light.Color.OFF);
    }

    private void addReadings(Turret turret, DcMotorEx turretEncoder, DigitalChannel forwardSwitch,
                             int markCounts, ElapsedTime sinceMarkSet) {
        int counts = turret.getEncoderCounts();
        int sinceMark = counts - markCounts;

        telemetry.addLine("--- Forward switch ---");
        telemetry.addData("Forward switch (magnet)", turret.isFacingForward() ? "YES" : "no");
        telemetry.addData("Switch raw reading", "%b  (should FLIP when the magnet passes)", forwardSwitch.getState());
        telemetry.addLine("  Never flips? Try the other channel of that digital port (REV switch: usually the odd one, n+1).");

        telemetry.addLine("--- Turret ---");
        turret.addTelemetry(telemetry);
        if (!turret.isHomed()) {
            telemetry.addLine("A / X / B do nothing until homed. Turn to forward with the dpad first.");
        }

        telemetry.addLine("--- ELC encoder (on the servo) ---");
        telemetry.addData("Raw counts", "%d  (0 at power-on)", counts);
        telemetry.addLine("  Doesn't change while turning? Check the ELC \"D\" cable is in the Expansion Hub ENCODER port");
        telemetry.addLine("  that matches turretEncoder's motor port number in the config.");
        if (sinceMarkSet.seconds() < 2) {
            telemetry.addLine("  ✅ Mark set! \"Since mark\" now counts from here.");
        }
        telemetry.addData("Since mark (Y)", "%d counts = %.1f° of encoder turn",
                sinceMark, sinceMark / Turret.ENCODER_COUNTS_PER_REV * 360.0);
        telemetry.addData("Encoder turns", "%.3f  (%.0f counts = 1 turn)",
                counts / Turret.ENCODER_COUNTS_PER_REV, Turret.ENCODER_COUNTS_PER_REV);
        telemetry.addData("Speed", "%.0f counts/s = %.0f°/s",
                turretEncoder.getVelocity(), turretEncoder.getVelocity() / Turret.ENCODER_COUNTS_PER_REV * 360.0);
    }
}
