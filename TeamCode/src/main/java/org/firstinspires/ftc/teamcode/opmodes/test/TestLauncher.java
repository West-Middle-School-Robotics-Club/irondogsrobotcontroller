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
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Launcher;

/**
 * TEST: spin the launcher flywheel (goBILDA 5203 Series Yellow Jacket) at different powers, and dial in
 * VELOCITY control with PIDF (issue #5, decision 032).
 *
 * Two modes (RIGHT BUMPER on gamepad 1 switches):
 *   POWER     what Main TeleOp does today: a fixed power (% of the battery). The speed drops as the battery drains
 *             and right after each ball.
 *   VELOCITY  the hub holds a TARGET SPEED (ticks per second) with its built-in PIDF controller, using the encoder.
 *             Same speed every shot, whatever the battery. The gains are changed LIVE (gamepad 1 or 2), no redeploy.
 *
 * PIDF in one line each (for a flywheel, F and P do almost all the work):
 *   F  feedforward: the power needed for the target speed, so the motor starts out close
 *   P  proportional: pushes harder the further the speed is below the target (brings it back after a shot)
 *   I  integral: fixes a small error that never goes away. Usually tiny or 0 for a flywheel
 *   D  derivative: damps wobble. Usually 0 for a flywheel
 *
 * Gamepad 1 (everything can be done with gamepad 1 alone):
 *   A                   Spin on / off (toggle)
 *   B                   STOP
 *   RIGHT BUMPER        Switch POWER / VELOCITY mode
 *   X                   Intake + water wheel ON / OFF, the shooting way (intake IN, water wheel UP), to feed balls
 *   HOLD Left Bumper    Trigger servo to FIRE (let go = REST). Also measures the shot (see "Last shot")
 *   POWER mode:
 *     Dpad UP / DOWN or R2 / L2   test power +/- POWER_STEP (5%)
 *     Y                           100% power
 *   VELOCITY mode:
 *     R2 / L2 (triggers)          target speed +/- TARGET_STEP ticks/s, one step per press
 *     Dpad LEFT / RIGHT           pick P, I, D or F
 *     Dpad UP / DOWN              raise / lower the picked gain by the step (applied right away)
 *     Y                           next step size: 1 -> 10 -> 100 -> 0.001 -> 0.01 -> 0.1 -> 1
 * The flywheel direction is set in Launcher.MOTOR_REVERSED (checked: true).
 * Gamepad 2 (optional, so a second person can tune the gains; same gains as gamepad 1):
 *   Dpad LEFT / RIGHT   Pick P, I, D or F
 *   Dpad UP / DOWN      Raise / lower the picked gain by the step
 *   RIGHT / LEFT BUMPER Step x10 / step /10
 *
 * How to tune (also in the tuning guide, Launcher section):
 *   1. POWER mode, Y (100%), A. Wait 3 s, read "F estimate" (= 32767 x power / speed). VELOCITY mode, set F to it,
 *      P, I, D to 0. Set the target to the speed DEFAULT_SPIN_POWER gives today. "Speed error" should be small
 *   2. X to feed balls. Raise P until the speed comes back quickly after a shot ("Last shot: back to speed in ...") without
 *      wobbling around the target. Too much P = the speed shakes. Then maybe a tiny I if it settles a bit low
 *   3. Write the P, I, D, F and target speed on the worksheet. Hit-rate test at that target
 * The gains are NOT saved: they reset to the hub's own values every time this test starts. Write them down!
 *
 * Hardware config name in Driver Station: "launcherMotor" (goBILDA 5203 Series, exact type, see decision 008)
 *
 * Printable worksheet (steps, Expected / Actual / Fix, numbers for the code): TeamCode/docs/worksheets/launcher-test.pdf
 */
// Hidden on the Driver Station (decision 052). To use it: delete the @Disabled line below, then build and deploy.
@TeleOp(name = "Launcher Test", group = "Tests")
public class TestLauncher extends LinearOpMode {

    // ---- Test settings (not robot tunables) ----
    static final double START_TARGET_VELOCITY = 1500.0; // VELOCITY mode starts at this target (ticks/s)
    static final double POWER_STEP = 0.05;              // POWER mode: dpad / R2 / L2 change of the test power (5%)
    static final double TARGET_STEP = 50.0;             // R2 / L2 change of the target (ticks/s)
    static final double[] GAIN_STEPS = {0.001, 0.01, 0.1, 1.0, 10.0, 100.0};  // gain step sizes to pick from
    static final int START_STEP_INDEX = 3;              // start changing gains by GAIN_STEPS[3] = 1.0
    static final double RECOVERED_FRACTION = 0.97;      // "back to speed" = at least 97% of the speed before the shot
    static final double SHOT_WATCH_MAX_S = 5.0;         // stop measuring a shot after this long
    static final double TRIGGER_PRESSED = 0.5;          // R2 / L2 count as "pressed" past halfway (they are analog)

    private static final String[] GAIN_NAMES = {"P", "I", "D", "F"};

    private DcMotorEx directMotor;  // test-only direct motor access (code-structure rule 8)
    private final double[] gains = new double[4];  // P, I, D, F
    private boolean velocityMode = false;

    @Override
    public void runOpMode() {
        Launcher launcher = new Launcher(hardwareMap);
        Intake intake = new Intake(hardwareMap);  // X feeds balls to the launcher

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
        int stepIndex = START_STEP_INDEX;  // which GAIN_STEPS entry the gains change by
        boolean isSpinning = false;
        boolean intakeOn = false;  // intake + water wheel, the shooting way
        boolean r2WasDown = false; // last loop's trigger state, so one press = one step (like the dpad)
        boolean l2WasDown = false;

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

            // R2 / L2 are analog (0 to 1), so we check "down now, but not last loop" to get one step per press
            boolean r2Down = gamepad1.right_trigger > TRIGGER_PRESSED;
            boolean l2Down = gamepad1.left_trigger > TRIGGER_PRESSED;
            boolean r2Pressed = r2Down && !r2WasDown;
            boolean l2Pressed = l2Down && !l2WasDown;
            r2WasDown = r2Down;
            l2WasDown = l2Down;

            // Read each "was pressed" only ONCE per loop: reading it clears it
            boolean upPressed = gamepad1.dpadUpWasPressed();
            boolean downPressed = gamepad1.dpadDownWasPressed();
            boolean leftPressed = gamepad1.dpadLeftWasPressed();
            boolean rightPressed = gamepad1.dpadRightWasPressed();
            boolean yPressed = gamepad1.yWasPressed();

            if (velocityMode) {
                // R2 / L2 = target speed.  Dpad + Y = PIDF gains (same as gamepad 2, so one person can tune alone)
                if (r2Pressed) {
                    targetVelocity += TARGET_STEP;
                } else if (l2Pressed) {
                    targetVelocity = Math.max(0.0, targetVelocity - TARGET_STEP);
                }
                if (rightPressed) {
                    pickedGain = (pickedGain + 1) % 4;
                } else if (leftPressed) {
                    pickedGain = (pickedGain + 3) % 4;
                }
                if (upPressed) {
                    gains[pickedGain] += GAIN_STEPS[stepIndex];
                } else if (downPressed) {
                    gains[pickedGain] = Math.max(0.0, gains[pickedGain] - GAIN_STEPS[stepIndex]);
                }
                if (yPressed) {
                    stepIndex = (stepIndex + 1) % GAIN_STEPS.length;  // 1 -> 10 -> 100 -> 0.001 -> ... -> 1
                }
            } else {
                if (upPressed || r2Pressed) {
                    testPower = roundPower(Math.min(1.0, testPower + POWER_STEP));
                } else if (downPressed || l2Pressed) {
                    testPower = roundPower(Math.max(0.1, testPower - POWER_STEP));
                }
                if (gamepad1.y) {
                    testPower = 1.0;
                }
            }

            // X = intake + water wheel on / off, the same way Main TeleOp shoots: intake IN, water wheel UP
            if (gamepad1.xWasPressed()) {
                intakeOn = !intakeOn;
            }
            if (intakeOn) {
                intake.in();
                intake.waterWheelUp();
            } else {
                intake.stop();
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
                gains[pickedGain] += GAIN_STEPS[stepIndex];
            } else if (gamepad2.dpadDownWasPressed()) {
                gains[pickedGain] = Math.max(0.0, gains[pickedGain] - GAIN_STEPS[stepIndex]);
            }
            if (gamepad2.rightBumperWasPressed()) {
                stepIndex = Math.min(GAIN_STEPS.length - 1, stepIndex + 1);  // step x10
            } else if (gamepad2.leftBumperWasPressed()) {
                stepIndex = Math.max(0, stepIndex - 1);                      // step /10
            }

            // ---------- Run the flywheel ----------
            if (velocityMode && directMotor != null) {
                if (isSpinning) {
                    useVelocityMode(targetVelocity);  // sends the gains and the target every loop: changes apply at once
                } else {
                    usePowerMode();                   // off = coast down, like POWER mode (no hard braking)
                }
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
            telemetry.addLine("GP1: A spin | B stop | RB switch mode | X intake | hold LB fire");
            if (velocityMode) {
                telemetry.addLine("GP1: R2/L2 target | dpad L/R pick gain | dpad U/D change | Y step size");
                telemetry.addLine("GP2 (optional): dpad = same | RB/LB step x10 / 10");
            } else {
                telemetry.addLine("GP1: dpad U/D or R2/L2 power | Y = 100%");
            }
            telemetry.addLine();
            telemetry.addData("Spinning", isSpinning ? "ON" : "OFF");
            telemetry.addData("Intake + water wheel", intakeOn ? "ON (feeding)" : "off   (X to start)");
            telemetry.addData("Speed", "%.0f ticks/s", speed);
            if (velocityMode) {
                telemetry.addData("Target", "%.0f ticks/s", targetVelocity);
                telemetry.addData("Speed error", "%.0f ticks/s", targetVelocity - speed);
                telemetry.addData("PIDF", gainText());
                telemetry.addData("Changing", "%s, step %s", GAIN_NAMES[pickedGain], trim(GAIN_STEPS[stepIndex]));
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
            telemetry.addLine();

            telemetry.addLine("--- Subsystem Telemetry ---");
            launcher.addTelemetry(telemetry);
            intake.addTelemetry(telemetry);
            if (directMotor != null) {
                telemetry.addData("Motor Current", "%.2f Amps", directMotor.getCurrent(CurrentUnit.AMPS));
            }
            telemetry.update();
        }

        usePowerMode();
        launcher.stop();
        intake.stop();
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

    /** Round to 2 decimals, so 5% steps land exactly on 0.70, 0.75... (computers add 0.05 slightly off). */
    private static double roundPower(double power) {
        return Math.round(power * 100.0) / 100.0;
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
