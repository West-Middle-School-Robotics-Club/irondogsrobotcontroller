package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Power;

/**
 * TEST: run the intake and the water wheel (issue #4).
 *
 * Needs "intakeServo" and "waterWheelServo" as Continuous Rotation Servos in the Driver Station config.
 * A missing one shows OFFLINE.
 *
 * Controls (gamepad 1), after START. HOLD a button to run, let go to stop:
 *   A   intake IN          B   intake OUT
 *   X   water wheel UP     Y   water wheel DOWN (clear a jam)
 *
 * Check: A pulls game elements IN, X moves them UP. If one runs backwards,
 * set INTAKE_REVERSED or WATER_WHEEL_REVERSED to true in Intake.java.
 *
 * Nectar switches ("nectarDetectorLeftSwitch", "nectarDetectorRightSwitch", Digital Device):
 *   - Press each lever by hand: its line must say PRESSED, and "NECTAR detected" must say YES.
 *     Backwards (PRESSED when not pressed)? Change NECTAR_SWITCH_WIRING in Intake.java.
 *   - Never changes? Try the other channel of that digital port (each port has 2: n and n+1).
 *   - Run a NECTAR up to the launcher: YES. Run a POLLEN through: it should stay "no".
 * The switches work during INIT too (nothing moves).
 */
@TeleOp(name = "Intake Test", group = "Tests")
public class IntakeTest extends LinearOpMode {

    @Override
    public void runOpMode() {
        Power power = new Power(hardwareMap); // battery voltage + current on every test
        Intake intake = new Intake(hardwareMap);

        // INIT: nothing moves until START (game rule G304.H).
        while (opModeInInit()) {
            telemetry.addLine("Press START, then HOLD A/B (intake) or X/Y (water wheel).");
            intake.addTelemetry(telemetry);
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        while (opModeIsActive()) {
            if (gamepad1.a) {
                intake.in();
            } else if (gamepad1.b) {
                intake.out();
            } else {
                intake.stopIntake();
            }

            if (gamepad1.x) {
                intake.waterWheelUp();
            } else if (gamepad1.y) {
                intake.waterWheelDown();
            } else {
                intake.stopWaterWheel();
            }

            telemetry.addLine("HOLD: A = intake IN, B = OUT   X = water wheel UP, Y = DOWN");
            telemetry.addLine();
            intake.addTelemetry(telemetry);
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        intake.stop();
    }
}
