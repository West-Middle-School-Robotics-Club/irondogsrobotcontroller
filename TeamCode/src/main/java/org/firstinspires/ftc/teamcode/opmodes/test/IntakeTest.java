package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Power;

/**
 * TEST: run the intake and the loader (issue #4).
 *
 * Needs "intakeServo" and "intakeLoaderServo" as Continuous Rotation Servos in the Driver Station config.
 * A missing one shows OFFLINE.
 *
 * Controls (gamepad 1), after START. HOLD a button to run, let go to stop:
 *   A   intake IN          B   intake OUT
 *   X   loader UP (load)   Y   loader DOWN (unload / clear a jam)
 *
 * Check: A pulls game elements IN, X moves them UP. If one runs backwards,
 * set INTAKE_REVERSED or LOADER_REVERSED to true in Intake.java.
 */
@TeleOp(name = "Intake Test", group = "Tests")
public class IntakeTest extends LinearOpMode {

    @Override
    public void runOpMode() {
        Power power = new Power(hardwareMap); // battery voltage + current on every test
        Intake intake = new Intake(hardwareMap);

        // INIT: nothing moves until START (game rule G304.H).
        while (opModeInInit()) {
            telemetry.addLine("Press START, then HOLD A/B (intake) or X/Y (loader).");
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
                intake.load();
            } else if (gamepad1.y) {
                intake.unload();
            } else {
                intake.stopLoader();
            }

            telemetry.addLine("HOLD: A = intake IN, B = OUT   X = loader UP, Y = DOWN");
            telemetry.addLine();
            intake.addTelemetry(telemetry);
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        intake.stop();
    }
}
