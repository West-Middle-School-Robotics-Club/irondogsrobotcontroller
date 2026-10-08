package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.subsystems.Light;
import org.firstinspires.ftc.teamcode.subsystems.Turret;

/**
 * TEST: turn the turret with the dpad and learn how the ELC encoder works (issue #31).
 *
 * Controls (gamepad 1), all after START:
 *   HOLD dpad left / right   turn the turret left / right. Hold a bumper too for SLOW (fine) moves
 *   Y                        set a MARK: "Since mark" counts from here. Use it to measure a move
 *   A                        turret.turnForward()  (uses the calibrated constants)
 *   X / B                    turret.turnTo(+45) / turnTo(-45)  (left / right)
 *
 * The light turns GREEN when the turret faces forward (the forward switch).
 * The first time that happens, the encoder is HOMED: that spot becomes 0 degrees.
 *
 * If dpad LEFT turns the turret RIGHT, make TURRET_DEGREES_PER_SERVO_RANGE negative in Turret.java.
 * The dpad does NOT use the MAX_LEFT/RIGHT limits, so move slowly near the ends!
 *
 * How the ELC encoder works:
 *   It counts tiny steps as its shaft turns: 4000 counts = 1 full turn, so about 11 counts = 1 degree.
 *   Turning one way counts UP, the other way counts DOWN. It starts at 0 when the robot powers on,
 *   which is why we home it at the forward switch.
 */
@TeleOp(name = "Turret Test", group = "Tests")
public class TurretTest extends LinearOpMode {

    // How fast the dpad turns the servo, in servo positions per second (the full range is 0.0 to 1.0).
    static final double DPAD_SPEED = 0.25;       // full range in 4 seconds
    static final double DPAD_SLOW_SPEED = 0.05;  // full range in 20 seconds

    @Override
    public void runOpMode() {
        Turret turret = new Turret(hardwareMap);
        Light light = new Light(hardwareMap);
        // Test-only: the same servo and encoder, used directly for calibration (code-structure rule 8).
        Servo turretServo = hardwareMap.get(Servo.class, Turret.SERVO_NAME);
        DcMotorEx turretEncoder = hardwareMap.get(DcMotorEx.class, Turret.ENCODER_NAME);

        double servoPosition = Turret.SERVO_FORWARD_POSITION;
        int markCounts = turret.getEncoderCounts();
        // Dpad LEFT should turn LEFT. Flip the direction if the servo is mounted the other way.
        double leftDirection = Math.signum(Turret.TURRET_DEGREES_PER_SERVO_RANGE);

        // INIT: nothing moves until START (game rule G304.H). Readings, homing and the light work already.
        while (opModeInInit()) {
            turret.update();
            showForward(turret, light);
            telemetry.addLine("Press START. The turret will go to SERVO_FORWARD_POSITION.");
            telemetry.addLine("Don't force the turret by hand: the servo's gears can strip.");
            telemetry.addLine();
            addReadings(turret, turretEncoder, servoPosition, markCounts);
            telemetry.update();
        }

        turretServo.setPosition(servoPosition);
        ElapsedTime loopTimer = new ElapsedTime();

        while (opModeIsActive()) {
            turret.update();

            // Hold the dpad to turn. Speed x time = how far to move this loop.
            double seconds = loopTimer.seconds();
            loopTimer.reset();
            double speed = (gamepad1.left_bumper || gamepad1.right_bumper) ? DPAD_SLOW_SPEED : DPAD_SPEED;
            if (gamepad1.dpad_left) {
                servoPosition += leftDirection * speed * seconds;
            }
            if (gamepad1.dpad_right) {
                servoPosition -= leftDirection * speed * seconds;
            }
            if (gamepad1.dpad_left || gamepad1.dpad_right) {
                servoPosition = Math.max(0.0, Math.min(1.0, servoPosition));
                turretServo.setPosition(servoPosition);
            }

            if (gamepad1.yWasPressed()) {
                markCounts = turret.getEncoderCounts();
            }
            // Buttons that use the calibrated constants. Keep servoPosition in sync so the dpad continues from there.
            if (gamepad1.aWasPressed()) {
                turret.turnForward();
                servoPosition = turretServo.getPosition();
            }
            if (gamepad1.xWasPressed()) {
                turret.turnTo(45);
                servoPosition = turretServo.getPosition();
            }
            if (gamepad1.bWasPressed()) {
                turret.turnTo(-45);
                servoPosition = turretServo.getPosition();
            }

            showForward(turret, light);

            telemetry.addLine("HOLD dpad = turn (+bumper = slow)   Y = mark   A = forward   X/B = 45° L/R");
            telemetry.addLine();
            addReadings(turret, turretEncoder, servoPosition, markCounts);
            telemetry.update();
        }

        light.off();
    }

    /** GREEN when the turret faces forward, OFF otherwise. The light is OK during INIT: it doesn't move anything. */
    private void showForward(Turret turret, Light light) {
        light.setColor(turret.isFacingForward() ? Light.Color.GREEN : Light.Color.OFF);
    }

    private void addReadings(Turret turret, DcMotorEx turretEncoder, double servoPosition, int markCounts) {
        int counts = turret.getEncoderCounts();
        int sinceMark = counts - markCounts;

        telemetry.addLine("--- Forward switch ---");
        telemetry.addData("Facing forward", turret.isFacingForward() ? "YES (light GREEN)" : "no");
        telemetry.addData("Homed", turret.isHomed() ? "yes: forward = 0°" : "NO: turn the turret to forward once");

        telemetry.addLine("--- Servo (what we ASK for) ---");
        telemetry.addData("Servo position", "%.3f  (0.0 to 1.0)", servoPosition);
        telemetry.addData("Expected angle", "%.1f°  (from the servo position)",
                (servoPosition - Turret.SERVO_FORWARD_POSITION) * Turret.TURRET_DEGREES_PER_SERVO_RANGE);

        telemetry.addLine("--- ELC encoder (what REALLY happens) ---");
        telemetry.addData("Raw counts", "%d  (0 at power-on)", counts);
        telemetry.addData("Since mark (Y)", "%d counts = %.1f° of encoder turn",
                sinceMark, sinceMark / Turret.ENCODER_COUNTS_PER_REV * 360.0);
        telemetry.addData("Encoder turns", "%.3f  (%.0f counts = 1 turn)",
                counts / Turret.ENCODER_COUNTS_PER_REV, Turret.ENCODER_COUNTS_PER_REV);
        telemetry.addData("Speed", "%.0f counts/s = %.0f°/s",
                turretEncoder.getVelocity(), turretEncoder.getVelocity() / Turret.ENCODER_COUNTS_PER_REV * 360.0);
        telemetry.addData("Turret angle", turret.isHomed() ? String.format("%.1f°  (+ = left)", turret.getAngle()) : "unknown until homed");

        telemetry.addLine();
        telemetry.addLine("Expected and Turret angle should match. If not, check the TODO constants in Turret.java.");
    }
}
