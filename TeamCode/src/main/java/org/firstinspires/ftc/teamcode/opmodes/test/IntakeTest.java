package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Power;

/**
 * TEST: run the intake and the water wheel, and tune their speed (issue #4). Controls work like Launcher Test.
 *
 * Needs "intakeServo" and "waterWheelServo" as Continuous Rotation Servos in the Driver Station config.
 * A missing one shows OFFLINE.
 *
 * Controls (gamepad 1), after START. It starts STOPPED, at the speed saved in Intake.java:
 *   A                  spin ON / OFF (toggle)
 *   B                  STOP
 *   Dpad UP / DOWN     speed of BOTH, +0.05 / -0.05
 *   Dpad LEFT / RIGHT  direction (toggle): IN + UP, or OUT + DOWN
 *   X                  which one runs (toggle through): BOTH, INTAKE only, WATER WHEEL only.
 *                      Use "only" to check one at a time (the REVERSED settings)
 *
 * Check: with the intake IN, balls are pulled IN. With the water wheel UP, balls move UP to the launcher.
 * If one runs backwards, set INTAKE_REVERSED or WATER_WHEEL_REVERSED to true in Intake.java.
 *
 * Tuning: change the speed while it runs. When it feels right, put the number from the screen into
 * INTAKE_SPEED and WATER_WHEEL_SPEED in Intake.java (they are what Main TeleOp and Basic Auton use).
 * The speed here is NOT saved: it goes back to the Intake.java numbers the next time you run this.
 *
 * Nectar switches ("nectarDetectorLeftSwitch", "nectarDetectorRightSwitch", Digital Device):
 *   - Press each lever by hand: its line must say PRESSED, and "NECTAR detected" must say YES.
 *     Backwards (PRESSED when not pressed)? Change NECTAR_SWITCH_WIRING in Intake.java.
 *   - Never changes? Try the other channel of that digital port (each port has 2: n and n+1).
 *   - Run a NECTAR up to the launcher: YES. Run a POLLEN through: it should stay "no".
 * The switches work during INIT too (nothing moves).
 *
 * Printable worksheet (steps, Expected / Actual / Fix, numbers for the code): TeamCode/docs/worksheets/intake-test.pdf
 */
@TeleOp(name = "Intake Test", group = "Tests")
public class IntakeTest extends LinearOpMode {

    private static final double SPEED_STEP = 0.05;
    private static final double MIN_SPEED = 0.1;

    private enum Run { BOTH, INTAKE_ONLY, WATER_WHEEL_ONLY }

    @Override
    public void runOpMode() {
        Power power = new Power(hardwareMap); // battery voltage + current on every test
        Intake intake = new Intake(hardwareMap);

        double testSpeed = Intake.INTAKE_SPEED;   // one speed for both; the screen shows it
        boolean spinning = false;
        boolean reversed = false;                 // false = intake IN + water wheel UP
        Run run = Run.BOTH;

        // INIT: nothing moves until START (game rule G304.H).
        while (opModeInInit()) {
            telemetry.addLine("Press START. A = spin on/off, B = STOP, dpad up/down = speed,");
            telemetry.addLine("dpad left/right = direction, X = both / intake only / water wheel only.");
            intake.addTelemetry(telemetry);
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        while (opModeIsActive()) {
            // Speed of both (0.1 to 1). Rounded to 2 decimals so 0.05 steps don't pick up tiny errors
            if (gamepad1.dpadUpWasPressed()) {
                testSpeed = Math.min(1.0, Math.round((testSpeed + SPEED_STEP) * 100) / 100.0);
            } else if (gamepad1.dpadDownWasPressed()) {
                testSpeed = Math.max(MIN_SPEED, Math.round((testSpeed - SPEED_STEP) * 100) / 100.0);
            }

            // Direction
            if (gamepad1.dpadLeftWasPressed() || gamepad1.dpadRightWasPressed()) {
                reversed = !reversed;
            }

            // Which one runs
            if (gamepad1.xWasPressed()) {
                run = Run.values()[(run.ordinal() + 1) % Run.values().length];
            }

            // On / off
            if (gamepad1.aWasPressed()) {
                spinning = !spinning;
            }
            if (gamepad1.bWasPressed()) {
                spinning = false;
            }

            // Run. The intake and the water wheel always go the same way: both IN / UP, or both OUT / DOWN
            double signedSpeed = reversed ? -testSpeed : testSpeed;
            if (spinning && run != Run.WATER_WHEEL_ONLY) {
                intake.runIntake(signedSpeed);
            } else {
                intake.stopIntake();
            }
            if (spinning && run != Run.INTAKE_ONLY) {
                intake.runWaterWheel(signedSpeed);
            } else {
                intake.stopWaterWheel();
            }

            telemetry.addLine("A = spin on/off | B = STOP | Dpad UP/DOWN = speed | Dpad LEFT/RIGHT = direction | X = which one");
            telemetry.addLine();
            telemetry.addData("Spin", spinning ? "ON" : "OFF");
            telemetry.addData("Speed (both)", "%.2f   <- put this in Intake.java when it feels right", testSpeed);
            telemetry.addData("Direction", reversed ? "OUT + water wheel DOWN" : "IN + water wheel UP");
            telemetry.addData("Running", run == Run.BOTH ? "BOTH" : run == Run.INTAKE_ONLY ? "INTAKE only" : "WATER WHEEL only");
            telemetry.addLine();
            intake.addTelemetry(telemetry);
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        intake.stop();
    }
}
