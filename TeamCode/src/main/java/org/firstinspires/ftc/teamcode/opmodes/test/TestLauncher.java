package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.subsystems.Launcher;

/**
 * TEST: spin the launcher flywheel (goBILDA 5203 Series Yellow Jacket) at different powers, and dial in
 * VELOCITY control with PIDF (issue #5, decision 032).
 *
 * Two modes (RIGHT BUMPER on gamepad 1 switches):
 *   POWER     what Main TeleOp does today: a fixed power (% of the battery). The speed drops as the battery drains
 *             and right after each ball.
 *   VELOCITY  the hub holds a TARGET SPEED (ticks per second) with its built-in PIDF controller, using the encoder.
 *             Same speed every shot, whatever the battery. The gains are changed LIVE on gamepad 2, no redeploy.
 *
 * PIDF in one line each (for a flywheel, F and P do almost all the work):
 *   F  feedforward: the power needed for the target speed, so the motor starts out close
 *   P  proportional: pushes harder the further the speed is below the target (brings it back after a shot)
 *   I  integral: fixes a small error that never goes away. Usually tiny or 0 for a flywheel
 *   D  derivative: damps wobble. Usually 0 for a flywheel
 *
 * Gamepad 1:
 *   A                   Spin on / off (toggle)
 *   B                   STOP
 *   RIGHT BUMPER        Switch POWER / VELOCITY mode
 *   Dpad UP / DOWN      POWER: test power +/- 10%.  VELOCITY: target speed +/- TARGET_STEP ticks/s
 *   X / Y               POWER only: presets 50% (X) / 100% (Y)
 *   HOLD Right Trigger  POWER only: variable power (0 to 100%)
 *   Dpad LEFT / RIGHT   Toggle motor direction (FORWARD / REVERSE)
 *   HOLD Left Bumper    Trigger servo to FIRE (let go = REST). Also measures the shot (see "Last shot")
 * Gamepad 2 (VELOCITY gains, applied right away):
 *   Dpad LEFT / RIGHT   Pick P, I, D or F
 *   Dpad UP / DOWN      Raise / lower the picked gain by the step
 *   RIGHT / LEFT BUMPER Step x10 / step /10
 *
 * How to tune (also in the tuning guide, Launcher section):
 *   1. POWER mode, Y (100%), A. Wait 3 s, read "F estimate" (= 32767 x power / speed). VELOCITY mode, set F to it,
 *      P, I, D to 0. Set the target to the speed DEFAULT_SPIN_POWER gives today. "Speed error" should be small
 *   2. Raise P until the speed comes back quickly after a shot ("Last shot: back to speed in ...") without
 *      wobbling around the target. Too much P = the speed shakes. Then maybe a tiny I if it settles a bit low
 *   3. Write the P, I, D, F and target speed on the worksheet. Hit-rate test at that target
 * The gains are NOT saved: they reset to the hub's own values every time this test starts. Write them down!
 *
 * Hardware config name in Driver Station: "launcherMotor" (goBILDA 5203 Series, exact type, see decision 008)
 *
 * Printable worksheet (steps, Expected / Actual / Fix, numbers for the code): TeamCode/docs/worksheets/launcher-test.pdf
 */
// Hidden on the Driver Station (decision 052). To use it: delete the @Disabled line below, then build and deploy.
@Disabled
@TeleOp(name = "Launcher Test", group = "Tests")
public class TestLauncher extends LinearOpMode {

    // ---- Test settings (not robot tunables) ----
    static final double START_TARGET_VELOCITY = 1500.0; // VELOCITY mode starts at this target (ticks/s)
    static final double TARGET_STEP = 50.0;             // dpad up/down change of the target (ticks/s)
    static final double START_GAIN_STEP = 1.0;          // gamepad 2 starts changing gains by this much
    static final double RECOVERED_FRACTION = 0.97;      // "back to speed" = at least 97% of the speed before the shot
    static final double SHOT_WATCH_MAX_S = 5.0;         // stop measuring a shot after this long

    private static final String[] GAIN_NAMES = {"P", "I", "D", "F"};

    private DcMotorEx directMotor;  // test-only direct motor access (code-structure rule 8)
    private final double[] gains = new double[4];  // P, I, D, F
    private boolean velocityMode = false;

    @Override
    public void runOpMode() {
        Launcher launcher = new Launcher(hardwareMap);

        try {
            directMotor = hardwareMap.get(DcMotorEx.class, Launcher.FLYWHEEL_NAME);
            directMotor.setDirection(Launcher.MOTOR_REVERSED ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
            // Start the gains from what the hub uses now, so nothing changes until someone presses a button
            PIDFCoefficients hub = directMotor.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER);
            gains[0] = hub.p;
            gains[1] = hub.i;
            gains[2] = hub.d;
            gains[3] = hub.f;
        } catch (Exception e) {
            directMotor = null;
        }

        double testPower = 0.5; // Default test power level
        double targetVelocity = START_TARGET_VELOCITY;
        int pickedGain = 3;      // start on F: the first one to set
        double gainStep = START_GAIN_STEP;
        boolean isSpinning = false;
        boolean directionReversed = Launcher.MOTOR_REVERSED; // Locked to REVERSED by default

        // Shot measuring: from the moment the gate opens, the lowest speed and how long until it's back up
        boolean watchingShot = false;
        double speedBeforeShot = 0.0;
        double lowestSpeed = 0.0;
        ElapsedTime shotTimer = new ElapsedTime();
        double lowestAtSeconds = 0.0;
        String lastShot = "none yet (hold left bumper with a ball)";

        // INIT loop: nothing moves (G304.H)
        while (opModeInInit()) {
            telemetry.addLine("=== LAUNCHER TEST (power + PIDF velocity) ===");
            if (launcher.isConnected()) {
                telemetry.addData("Hardware Status", "CONNECTED ('%s')", Launcher.FLYWHEEL_NAME);
                telemetry.addData("Hub PIDF now", gainText());
            } else {
                telemetry.addData("Hardware Status", "⚠️ NOT FOUND ('%s' missing in DS config)", Launcher.FLYWHEEL_NAME);
                telemetry.addLine("Check Driver Station configuration: name must be 'launcherMotor'");
            }
            telemetry.addLine();
            telemetry.addLine("Press START. Nobody in front of the launcher!");
            telemetry.update();
        }

        while (opModeIsActive()) {
            // ---------- Gamepad 1: mode, speed, spin ----------
            if (gamepad1.rightBumperWasPressed()) {
                velocityMode = !velocityMode;
                if (!velocityMode) {
                    usePowerMode();   // hand the motor back to plain power
                }
            }

            if (velocityMode) {
                if (gamepad1.dpadUpWasPressed()) {
                    targetVelocity += TARGET_STEP;
                } else if (gamepad1.dpadDownWasPressed()) {
                    targetVelocity = Math.max(0.0, targetVelocity - TARGET_STEP);
                }
            } else {
                if (gamepad1.dpadUpWasPressed()) {
                    testPower = Math.min(1.0, testPower + 0.1);
                } else if (gamepad1.dpadDownWasPressed()) {
                    testPower = Math.max(0.1, testPower - 0.1);
                }
                if (gamepad1.x) {
                    testPower = 0.5;
                }
                if (gamepad1.y) {
                    testPower = 1.0;
                }
            }

            if (gamepad1.dpadLeftWasPressed() || gamepad1.dpadRightWasPressed()) {
                directionReversed = !directionReversed;
                if (directMotor != null) {
                    directMotor.setDirection(directionReversed ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
                }
            }

            if (gamepad1.aWasPressed()) {
                isSpinning = !isSpinning;
            }
            if (gamepad1.bWasPressed()) {
                isSpinning = false;
            }

            // ---------- Gamepad 2: PIDF gains ----------
            if (gamepad2.dpadRightWasPressed()) {
                pickedGain = (pickedGain + 1) % 4;
            } else if (gamepad2.dpadLeftWasPressed()) {
                pickedGain = (pickedGain + 3) % 4;
            }
            if (gamepad2.dpadUpWasPressed()) {
                gains[pickedGain] += gainStep;
            } else if (gamepad2.dpadDownWasPressed()) {
                gains[pickedGain] = Math.max(0.0, gains[pickedGain] - gainStep);
            }
            if (gamepad2.rightBumperWasPressed()) {
                gainStep = Math.min(100.0, gainStep * 10);
            } else if (gamepad2.leftBumperWasPressed()) {
                gainStep = Math.max(0.001, gainStep / 10);
            }

            // ---------- Run the flywheel ----------
            if (velocityMode && directMotor != null) {
                if (isSpinning) {
                    useVelocityMode(targetVelocity);  // sends the gains and the target every loop: changes apply at once
                } else {
                    usePowerMode();                   // off = coast down, like POWER mode (no hard braking)
                }
            } else if (gamepad1.right_trigger > 0.05) {
                launcher.spinUp(gamepad1.right_trigger);  // the trigger has priority
                isSpinning = true;
            } else if (isSpinning) {
                launcher.spinUp(testPower);
            } else {
                launcher.stop();
            }

            // ---------- Trigger servo + shot measuring ----------
            double speed = Math.abs(launcher.getVelocity());  // size only, so a flipped direction still measures
            if (gamepad1.leftBumperWasPressed()) {
                launcher.fire();
                watchingShot = true;
                speedBeforeShot = speed;
                lowestSpeed = speed;
                lowestAtSeconds = 0.0;
                shotTimer.reset();
            } else if (gamepad1.leftBumperWasReleased()) {
                launcher.resetTrigger();
            }
            if (watchingShot) {
                if (speed < lowestSpeed) {
                    lowestSpeed = speed;
                    lowestAtSeconds = shotTimer.seconds();
                }
                boolean dipped = lowestSpeed < RECOVERED_FRACTION * speedBeforeShot;
                boolean backUp = speed >= RECOVERED_FRACTION * speedBeforeShot;
                if (dipped && backUp) {
                    lastShot = String.format("%.0f -> lowest %.0f ticks/s (-%.0f%%), back to speed in %.2f s",
                            speedBeforeShot, lowestSpeed, dropPercent(speedBeforeShot, lowestSpeed),
                            shotTimer.seconds() - lowestAtSeconds);
                    watchingShot = false;
                } else if (shotTimer.seconds() > SHOT_WATCH_MAX_S) {
                    lastShot = dipped
                            ? String.format("lowest %.0f ticks/s (-%.0f%%), NOT back to speed in %.0f s",
                                    lowestSpeed, dropPercent(speedBeforeShot, lowestSpeed), SHOT_WATCH_MAX_S)
                            : "no ball went through (the speed didn't drop)";
                    watchingShot = false;
                }
            }

            // ---------- Telemetry ----------
            telemetry.addLine("=== LAUNCHER TEST: " + (velocityMode ? "VELOCITY (PIDF)" : "POWER") + " mode ===");
            telemetry.addLine("GP1: A spin | B stop | RB switch mode | dpad U/D " + (velocityMode ? "target" : "power")
                    + " | hold LB fire");
            if (velocityMode) {
                telemetry.addLine("GP2: dpad L/R pick gain | dpad U/D change | RB/LB step x10 / 10");
            } else {
                telemetry.addLine("X = 50% | Y = 100% | hold RT = variable power | dpad L/R direction");
            }
            telemetry.addLine();
            telemetry.addData("Spinning", isSpinning ? "ON" : "OFF");
            telemetry.addData("Speed", "%.0f ticks/s", speed);
            if (velocityMode) {
                telemetry.addData("Target", "%.0f ticks/s", targetVelocity);
                telemetry.addData("Speed error", "%.0f ticks/s", targetVelocity - speed);
                telemetry.addData("PIDF", gainText());
                telemetry.addData("Changing", "%s, step %s", GAIN_NAMES[pickedGain], trim(gainStep));
            } else {
                telemetry.addData("Test power", "%.0f%%", testPower * 100);
                double power = launcher.getPower();
                if (Math.abs(power) > 0.3 && Math.abs(speed) > 100) {
                    // REV's rule for the hub's F: F = 32767 / top speed. At less than full power, scale by the power.
                    telemetry.addData("F estimate", "%.1f  (= 32767 x %.2f / %.0f, read it once the speed is steady)",
                            32767 * Math.abs(power) / Math.abs(speed), Math.abs(power), Math.abs(speed));
                }
            }
            telemetry.addData("Last shot", lastShot);
            telemetry.addData("Motor Direction", directionReversed ? "REVERSED" : "FORWARD");
            telemetry.addLine();

            telemetry.addLine("--- Subsystem Telemetry ---");
            launcher.addTelemetry(telemetry);
            if (directMotor != null) {
                telemetry.addData("Motor Current", "%.2f Amps", directMotor.getCurrent(CurrentUnit.AMPS));
            }
            telemetry.update();
        }

        usePowerMode();
        launcher.stop();
    }

    /** VELOCITY mode: the hub holds this speed with our gains. */
    private void useVelocityMode(double ticksPerSecond) {
        if (directMotor.getMode() != DcMotor.RunMode.RUN_USING_ENCODER) {
            directMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        directMotor.setVelocityPIDFCoefficients(gains[0], gains[1], gains[2], gains[3]);
        directMotor.setVelocity(ticksPerSecond);
    }

    /** Back to plain power, the way the Launcher subsystem runs it (decision 032), and stop. */
    private void usePowerMode() {
        if (directMotor == null) {
            return;
        }
        directMotor.setPower(0.0);
        if (directMotor.getMode() != DcMotor.RunMode.RUN_WITHOUT_ENCODER) {
            directMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
    }

    private String gainText() {
        return String.format("P %s  I %s  D %s  F %s", trim(gains[0]), trim(gains[1]), trim(gains[2]), trim(gains[3]));
    }

    private static double dropPercent(double before, double lowest) {
        return before > 0 ? 100.0 * (before - lowest) / before : 0.0;
    }

    /** A number without extra zeros: 10 -> "10", 0.5 -> "0.5", 0.001 -> "0.001". */
    private static String trim(double value) {
        String text = String.format("%.3f", value);
        return text.replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
