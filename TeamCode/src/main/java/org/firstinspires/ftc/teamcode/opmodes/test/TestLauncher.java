package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.subsystems.Launcher;

/**
 * TEST: Verify whether the launcher flywheel motor (goBILDA 5203 Series Yellow Jacket) can spin
 * and test various powers/speeds (issue #5).
 *
 * Direction is locked to REVERSED by default as per launcher subsystem configuration.
 *
 * Controls (gamepad 1):
 *   A                   Spin up at test speed / toggle spin
 *   B                   STOP launcher motor
 *   HOLD Right Trigger  Variable throttle (0 to 100% power)
 *   Dpad UP / DOWN      Increase / decrease test power (10% steps)
 *   X / Y               Preset speeds: 50% power (X) / 100% power (Y)
 *   Dpad LEFT / RIGHT   Toggle motor direction (FORWARD / REVERSE)
 *   HOLD Left Bumper    Trigger servo to FIRE (let go = REST). Use it to find the TRIGGER_ positions
 *
 * Hardware config name in Driver Station: "launcherMotor" (goBILDA 5203 Series)
 */
@TeleOp(name = "Launcher Test", group = "Tests")
public class TestLauncher extends LinearOpMode {

    @Override
    public void runOpMode() {
        Launcher launcher = new Launcher(hardwareMap);

        // Test-only direct motor access for diagnostics (Code Structure rule 8)
        DcMotorEx directMotor;
        try {
            directMotor = hardwareMap.get(DcMotorEx.class, Launcher.FLYWHEEL_NAME);
            directMotor.setDirection(Launcher.MOTOR_REVERSED ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
        } catch (Exception e) {
            directMotor = null;
        }

        double testPower = 0.5; // Default test power level
        boolean isSpinning = false;
        boolean directionReversed = Launcher.MOTOR_REVERSED; // Locked to REVERSED by default

        // INIT loop
        while (opModeInInit()) {
            telemetry.addLine("=== LAUNCHER SPIN TEST ===");
            if (launcher.isConnected()) {
                telemetry.addData("Hardware Status", "CONNECTED ('%s')", Launcher.FLYWHEEL_NAME);
                telemetry.addData("Direction Lock", "REVERSED (Default)");
            } else {
                telemetry.addData("Hardware Status", "⚠️ NOT FOUND ('%s' missing in DS config)", Launcher.FLYWHEEL_NAME);
                telemetry.addLine("Check Driver Station configuration: name must be 'launcherMotor'");
            }
            telemetry.addLine();
            telemetry.addLine("Press START to test motor spin.");
            telemetry.update();
        }

        // Active loop
        while (opModeIsActive()) {
            // Dpad Up/Down adjusts test power preset
            if (gamepad1.dpadUpWasPressed()) {
                testPower = Math.min(1.0, testPower + 0.1);
            } else if (gamepad1.dpadDownWasPressed()) {
                testPower = Math.max(0.1, testPower - 0.1);
            }

            // Quick presets
            if (gamepad1.x) {
                testPower = 0.5;
            }
            if (gamepad1.y) {
                testPower = 1.0;
            }

            // Direction toggle
            if (gamepad1.dpadLeftWasPressed() || gamepad1.dpadRightWasPressed()) {
                directionReversed = !directionReversed;
                if (directMotor != null) {
                    directMotor.setDirection(directionReversed ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
                }
            }

            // Motor power control logic
            if (gamepad1.right_trigger > 0.05) {
                // Trigger control has priority
                launcher.spinUp(gamepad1.right_trigger);
                isSpinning = true;
            } else if (gamepad1.aWasPressed()) {
                isSpinning = !isSpinning;
                if (isSpinning) {
                    launcher.spinUp(testPower);
                } else {
                    launcher.stop();
                }
            } else if (gamepad1.bWasPressed()) {
                isSpinning = false;
                launcher.stop();
            } else if (isSpinning) {
                // Continue spinning at testPower if toggled on
                launcher.spinUp(testPower);
            }

            // Trigger servo: hold left bumper = FIRE, let go = REST
            if (gamepad1.leftBumperWasPressed()) {
                launcher.fire();
            } else if (gamepad1.leftBumperWasReleased()) {
                launcher.resetTrigger();
            }

            // Telemetry output
            telemetry.addLine("=== LAUNCHER TEST CONTROLS ===");
            telemetry.addLine("A = Toggle Spin | B = STOP | Right Trigger = Variable Power");
            telemetry.addLine("Dpad UP/DOWN = Adjust Power | X = 50% | Y = 100%");
            telemetry.addLine("Dpad LEFT/RIGHT = Toggle Direction | HOLD Left Bumper = Trigger FIRE");
            telemetry.addLine();

            telemetry.addData("Spin Toggle State", isSpinning ? "ON" : "OFF");
            telemetry.addData("Test Power Setting", "%.0f%%", testPower * 100);
            telemetry.addData("Motor Direction", directionReversed ? "REVERSED (Locked)" : "FORWARD");
            telemetry.addLine();

            telemetry.addLine("--- Subsystem Telemetry ---");
            launcher.addTelemetry(telemetry);

            if (directMotor != null) {
                telemetry.addLine("--- Raw Motor Diagnostics ---");
                telemetry.addData("Raw Motor Power", "%.2f", directMotor.getPower());
                telemetry.addData("Encoder Velocity", "%.0f ticks/s", directMotor.getVelocity());
                telemetry.addData("Motor Current", "%.2f Amps", directMotor.getCurrent(CurrentUnit.AMPS));
                telemetry.addData("Encoder Position", "%d ticks", directMotor.getCurrentPosition());
            }

            telemetry.update();
        }

        launcher.stop();
    }
}
