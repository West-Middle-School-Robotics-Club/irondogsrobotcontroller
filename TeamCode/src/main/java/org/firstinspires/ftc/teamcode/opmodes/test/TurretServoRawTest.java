package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.PwmControl;

import org.firstinspires.ftc.teamcode.subsystems.Turret;
import org.firstinspires.ftc.teamcode.subsystems.Power;

/**
 * TEST: the turret servo ALONE, with nothing else in the way (issue #31).
 *
 * No homing, no encoder, no limits: just the servo and a joystick. Use it to check the servo itself works
 * in continuous mode before testing the whole turret with "Turret Test".
 * Only needs "turretServo" (Continuous Rotation Servo) in the Driver Station config.
 *
 * Controls (gamepad 1), after START:
 *   LEFT STICK left / right   speed (push farther = faster). Let go = stop
 *   dpad left / right         full speed left / right, while held
 *
 * What you should see (continuous mode):
 *   - It keeps SPINNING as long as you hold the stick, and stops where it is when you let go
 *   - Faster the farther you push
 * If instead it moves a little, STOPS while you still hold, and goes BACK when you let go,
 * the servo is still in standard (positional) mode: reprogram it with its servo programmer (goBILDA or Axon).
 * If it creeps when "Speed" is 0.00, recenter it with the servo programmer.
 */
@TeleOp(name = "Turret Servo Raw Test", group = "Tests")
public class TurretServoRawTest extends LinearOpMode {

    @Override
    public void runOpMode() {
        Power power = new Power(hardwareMap); // battery voltage + current on every test
        CRServoImplEx turretServo = hardwareMap.get(CRServoImplEx.class, Turret.SERVO_NAME);
        turretServo.setPwmRange(new PwmControl.PwmRange(Turret.SERVO_PWM_MIN_US, Turret.SERVO_PWM_MAX_US));

        // INIT: nothing moves until START (game rule G304.H).
        while (opModeInInit()) {
            telemetry.addLine("Press START, then use the LEFT STICK (left/right).");
            telemetry.addData("Signal range", "%.0f to %.0f µs (stop = %.0f µs)",
                    Turret.SERVO_PWM_MIN_US, Turret.SERVO_PWM_MAX_US,
                    (Turret.SERVO_PWM_MIN_US + Turret.SERVO_PWM_MAX_US) / 2);
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        while (opModeIsActive()) {
            // Stick left = positive = turn LEFT (our convention). Sticks read negative when pushed left.
            double speed = -gamepad1.left_stick_x;
            if (gamepad1.dpad_left) {
                speed = 1.0;
            } else if (gamepad1.dpad_right) {
                speed = -1.0;
            }
            if (Math.abs(speed) < 0.05) {
                speed = 0.0; // dead zone so a resting stick sends a clean stop
            }

            turretServo.setPower(speed);

            double stopUs = (Turret.SERVO_PWM_MIN_US + Turret.SERVO_PWM_MAX_US) / 2;
            double halfRangeUs = (Turret.SERVO_PWM_MAX_US - Turret.SERVO_PWM_MIN_US) / 2;
            telemetry.addLine("LEFT STICK = speed    dpad left/right = full speed");
            telemetry.addLine();
            telemetry.addData("Speed sent", "%.2f  (+ = left, - = right)", speed);
            telemetry.addData("Signal to servo", "%.0f µs  (stop = %.0f)", stopUs + speed * halfRangeUs, stopUs);
            telemetry.addLine();
            telemetry.addLine("Continuous mode: spins while held, stops in place when released.");
            telemetry.addLine("Stops while held + goes BACK when released = still in servo mode.");
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        turretServo.setPower(0.0);
    }
}
