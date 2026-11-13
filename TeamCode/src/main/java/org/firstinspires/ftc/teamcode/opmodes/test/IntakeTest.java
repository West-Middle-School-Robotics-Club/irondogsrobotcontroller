package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Power;

/**
 * TEST: run the intake and the water wheel, and tune their speed (issue #4). Controls work like Launcher Test.
 *
 * Needs "intakeServo" (Continuous Rotation Servo) and "waterWheelMotor" (goBILDA 5203 motor) in the Driver Station config.
 * A missing one shows OFFLINE.
 *
 * Controls (gamepad 1), after START. It starts STOPPED, at the speed saved in Intake.java:
 *   A                  spin ON / OFF (toggle)
 *   B                  STOP
 *   Dpad UP / DOWN     speed +0.05 / -0.05 of whichever is running (BOTH = both). Each has its own speed, because
 *                      the water wheel is a motor since 11/11 (decision 056) and wants a different number
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
 * Power check: the "POWER SENT" lines read straight from each device what power it is getting right now (-1 to 1),
 * so you can see the code really sends what the screen says. The water wheel motor also shows its CURRENT (amps):
 * it jumps when the wheel is jammed or stalled, and is near 0 if the motor isn't turning anything (loose gear/belt).
 *
 * Printable worksheet (steps, Expected / Actual / Fix, numbers for the code): TeamCode/docs/worksheets/intake-test.pdf
 */
// Hidden on the Driver Station (decision 052). To use it: delete the @Disabled line below, then build and deploy.
@Disabled
@TeleOp(name = "Intake Test", group = "Tests")
public class IntakeTest extends LinearOpMode {

    private static final double SPEED_STEP = 0.05;
    private static final double MIN_SPEED = 0.1;

    private enum Run { BOTH, INTAKE_ONLY, WATER_WHEEL_ONLY }

    @Override
    public void runOpMode() {
        Power power = new Power(hardwareMap); // battery voltage + current on every test
        Intake intake = new Intake(hardwareMap);
        // Test-only: the same two devices, read directly to show the power they really get (code-structure rule 8).
        // Missing from the Driver Station config = null, shown as OFFLINE.
        CRServo intakeServo = hardwareMap.tryGet(CRServo.class, Intake.INTAKE_SERVO_NAME);
        DcMotorEx waterWheelMotor = hardwareMap.tryGet(DcMotorEx.class, Intake.WATER_WHEEL_MOTOR_NAME);

        double intakeSpeed = Intake.INTAKE_SPEED;          // each starts at its Intake.java number
        double waterWheelSpeed = Intake.WATER_WHEEL_SPEED;
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
            // Speed (0.1 to 1) of whichever is running: INTAKE only, WATER WHEEL only, or both together
            double step = gamepad1.dpadUpWasPressed() ? SPEED_STEP : gamepad1.dpadDownWasPressed() ? -SPEED_STEP : 0.0;
            if (step != 0.0) {
                if (run != Run.WATER_WHEEL_ONLY) {
                    intakeSpeed = stepSpeed(intakeSpeed, step);
                }
                if (run != Run.INTAKE_ONLY) {
                    waterWheelSpeed = stepSpeed(waterWheelSpeed, step);
                }
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
            double sign = reversed ? -1.0 : 1.0;
            if (spinning && run != Run.WATER_WHEEL_ONLY) {
                intake.runIntake(sign * intakeSpeed);
            } else {
                intake.stopIntake();
            }
            if (spinning && run != Run.INTAKE_ONLY) {
                intake.runWaterWheel(sign * waterWheelSpeed);
            } else {
                intake.stopWaterWheel();
            }

            telemetry.addLine("A = spin on/off | B = STOP | Dpad UP/DOWN = speed | Dpad LEFT/RIGHT = direction | X = which one");
            telemetry.addLine();
            telemetry.addData("Spin", spinning ? "ON" : "OFF");
            telemetry.addData("Intake speed", "%.2f   <- INTAKE_SPEED in Intake.java when it feels right", intakeSpeed);
            telemetry.addData("Water wheel speed", "%.2f   <- WATER_WHEEL_SPEED (motor) when it feels right", waterWheelSpeed);
            telemetry.addData("Direction", reversed ? "OUT + water wheel DOWN" : "IN + water wheel UP");
            telemetry.addData("Running", run == Run.BOTH ? "BOTH" : run == Run.INTAKE_ONLY ? "INTAKE only" : "WATER WHEEL only");
            telemetry.addLine();
            telemetry.addLine("POWER SENT (read back from each device, -1 to 1)");
            telemetry.addData("  Intake servo", intakeServo != null
                    ? String.format("%.2f", intakeServo.getPower())
                    : "OFFLINE ('" + Intake.INTAKE_SERVO_NAME + "' not in DS config)");
            telemetry.addData("  Water wheel motor", waterWheelMotor != null
                    ? String.format("%.2f   current %.2f A", waterWheelMotor.getPower(), waterWheelMotor.getCurrent(CurrentUnit.AMPS))
                    : "OFFLINE ('" + Intake.WATER_WHEEL_MOTOR_NAME + "' not in DS config)");
            telemetry.addLine();
            intake.addTelemetry(telemetry);
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        intake.stop();
    }

    /** Add one dpad step to a speed and keep it between MIN_SPEED and 1. Rounded so 0.05 steps stay exact. */
    private static double stepSpeed(double speed, double step) {
        double next = Math.round((speed + step) * 100) / 100.0;
        return Math.max(MIN_SPEED, Math.min(1.0, next));
    }
}
