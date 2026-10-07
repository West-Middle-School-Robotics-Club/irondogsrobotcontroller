package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.subsystems.Light;
import org.firstinspires.ftc.teamcode.subsystems.Turret;

/**
 * TEST: calibrate the turret (issue #7). Follow the steps in issue #7 and record the numbers there.
 *
 * Controls (gamepad 1), all after START:
 *   dpad left / right   move the SERVO a little (+/- 0.01). Hold a bumper for big steps (0.05)
 *   A                   turret.turnForward()  (uses the calibrated constants)
 *   X / B               turret.turnTo(+45) / turnTo(-45)  (left / right)
 *
 * The dpad moves the servo DIRECTLY by position, so you can calibrate before the constants are right.
 * It does NOT use the MAX_LEFT/RIGHT limits, so move slowly near the ends!
 *
 * Tip: the forward switch says YES (and the light turns GREEN) when the turret faces forward.
 * Use it to find SERVO_FORWARD_POSITION and ENCODER_FORWARD_DEGREES.
 */
@TeleOp(name = "Turret Test", group = "Tests")
public class TurretTest extends LinearOpMode {

    @Override
    public void runOpMode() {
        Turret turret = new Turret(hardwareMap);
        Light light = new Light(hardwareMap);
        // Test-only: the same servo, moved directly for calibration (code-structure rule 8).
        Servo turretServo = hardwareMap.get(Servo.class, Turret.SERVO_NAME);
        double servoPosition = Turret.SERVO_FORWARD_POSITION;

        // INIT: nothing moves until START (game rule G304.H). Readings work already.
        while (opModeInInit()) {
            showForward(turret, light);
            telemetry.addLine("Press START. The turret will go to SERVO_FORWARD_POSITION.");
            addReadings(turret, servoPosition);
            telemetry.update();
        }

        turretServo.setPosition(servoPosition);

        while (opModeIsActive()) {
            double step = (gamepad1.left_bumper || gamepad1.right_bumper) ? 0.05 : 0.01;

            if (gamepad1.dpadLeftWasPressed()) {
                servoPosition = Math.min(1.0, servoPosition + step);
                turretServo.setPosition(servoPosition);
            }
            if (gamepad1.dpadRightWasPressed()) {
                servoPosition = Math.max(0.0, servoPosition - step);
                turretServo.setPosition(servoPosition);
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

            showForward(turret, light);

            telemetry.addLine("dpad = move servo (bumper = big)   A = forward   X = 45° left   B = 45° right");
            telemetry.addLine();
            addReadings(turret, servoPosition);
            telemetry.update();
        }

        light.off();
    }

    /** GREEN when the turret faces forward, OFF otherwise. The light is OK during INIT: it doesn't move anything. */
    private void showForward(Turret turret, Light light) {
        light.setColor(turret.isFacingForward() ? Light.Color.GREEN : Light.Color.OFF);
    }

    private void addReadings(Turret turret, double servoPosition) {
        turret.addTelemetry(telemetry);
        telemetry.addLine();
        telemetry.addData("Servo position (dpad)", "%.2f", servoPosition);
        telemetry.addData("Encoder voltage", "%.3f V", turret.getEncoderVoltage());
        telemetry.addData("Encoder degrees (0-360)", "%.1f", turret.getEncoderVoltage() / 3.3 * 360.0);
    }
}
