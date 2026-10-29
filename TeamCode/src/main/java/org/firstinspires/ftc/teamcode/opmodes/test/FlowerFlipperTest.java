package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.subsystems.FlowerFlipper;
import org.firstinspires.ftc.teamcode.subsystems.Power;

/**
 * TEST: shake the flower flipper, and find its positions (decision 024).
 *
 * Needs "flowerFlipperServo" as a Servo in the Driver Station config.
 *
 * Controls (gamepad 1), after START:
 *   A                   start shaking      B   stop (goes to REST_POSITION)
 *   dpad up / down      move the servo by hand (+/- 0.01), when NOT shaking.
 *                       Use it to find REST_POSITION, POSITION_A and POSITION_B for FlowerFlipper.java
 */
@TeleOp(name = "Flower Flipper Test", group = "Tests")
public class FlowerFlipperTest extends LinearOpMode {

    @Override
    public void runOpMode() {
        Power power = new Power(hardwareMap); // battery voltage + current on every test
        FlowerFlipper flowerFlipper = new FlowerFlipper(hardwareMap);
        // Test-only: the same servo, moved directly to find positions (code-structure rule 8). null = not configured.
        Servo flowerFlipperServo = hardwareMap.tryGet(Servo.class, FlowerFlipper.SERVO_NAME);
        double handPosition = FlowerFlipper.REST_POSITION;

        // INIT: nothing moves until START (game rule G304.H).
        while (opModeInInit()) {
            telemetry.addLine("Press START. A = shake, B = stop, dpad up/down = move by hand.");
            flowerFlipper.addTelemetry(telemetry);
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        while (opModeIsActive()) {
            if (gamepad1.aWasPressed()) {
                flowerFlipper.startShaking();
            }
            if (gamepad1.bWasPressed()) {
                flowerFlipper.stop();
                handPosition = FlowerFlipper.REST_POSITION;
            }

            if (!flowerFlipper.isShaking() && flowerFlipperServo != null) {
                if (gamepad1.dpadUpWasPressed()) {
                    handPosition = Math.min(1.0, handPosition + 0.01);
                    flowerFlipperServo.setPosition(handPosition);
                }
                if (gamepad1.dpadDownWasPressed()) {
                    handPosition = Math.max(0.0, handPosition - 0.01);
                    flowerFlipperServo.setPosition(handPosition);
                }
            }

            flowerFlipper.update(); // needed every loop for the shaking

            telemetry.addLine("A = shake   B = stop   dpad up/down = move by hand (when stopped)");
            telemetry.addLine();
            flowerFlipper.addTelemetry(telemetry);
            telemetry.addData("Hand position (dpad)", "%.2f", handPosition);
            telemetry.addData("Settings", "rest %.2f, A %.2f, B %.2f, every %.2f s",
                    FlowerFlipper.REST_POSITION, FlowerFlipper.POSITION_A, FlowerFlipper.POSITION_B,
                    FlowerFlipper.SWITCH_SECONDS);
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        flowerFlipper.stop();
    }
}
