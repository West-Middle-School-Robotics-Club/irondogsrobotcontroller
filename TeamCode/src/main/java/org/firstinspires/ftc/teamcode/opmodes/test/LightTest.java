package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.subsystems.Light;
import org.firstinspires.ftc.teamcode.subsystems.Power;

/**
 * TEST: check the goBILDA RGB Indicator Light colors on BOTH lights (decision 018).
 * Both lights always show the same color here, so you can also check they match each other.
 *
 * Controls (gamepad 1), work during INIT and after START (the light doesn't move anything):
 *   dpad right / left   next / previous color in Light.Color
 *   dpad up / down      fine-tune the position (+/- 0.005) to see where colors change
 *
 * For each color, check the light matches the name. If one is wrong, fine-tune it and
 * put the better number in Light.java.
 */
@TeleOp(name = "Light Test", group = "Tests")
public class LightTest extends LinearOpMode {

    @Override
    public void runOpMode() {
        Power power = new Power(hardwareMap); // battery voltage + current on every test
        Light leftLight = new Light(hardwareMap, Light.LEFT_LIGHT_NAME);
        Light rightLight = new Light(hardwareMap, Light.RIGHT_LIGHT_NAME);
        // Test-only: the same lights, set directly for fine-tuning (code-structure rule 8). null = not configured.
        Servo leftIndicatorLight = hardwareMap.tryGet(Servo.class, Light.LEFT_LIGHT_NAME);
        Servo rightIndicatorLight = hardwareMap.tryGet(Servo.class, Light.RIGHT_LIGHT_NAME);

        Light.Color[] colors = Light.Color.values();
        int index = 0;
        double position = colors[index].position;

        while (opModeInInit() || opModeIsActive()) {
            if (gamepad1.dpadRightWasPressed()) {
                index = (index + 1) % colors.length;
                leftLight.setColor(colors[index]);
                rightLight.setColor(colors[index]);
                position = colors[index].position;
            }
            if (gamepad1.dpadLeftWasPressed()) {
                index = (index + colors.length - 1) % colors.length;
                leftLight.setColor(colors[index]);
                rightLight.setColor(colors[index]);
                position = colors[index].position;
            }
            if (gamepad1.dpadUpWasPressed()) {
                position = Math.min(1.0, position + 0.005);
                setBoth(leftIndicatorLight, rightIndicatorLight, position);
            }
            if (gamepad1.dpadDownWasPressed()) {
                position = Math.max(0.0, position - 0.005);
                setBoth(leftIndicatorLight, rightIndicatorLight, position);
            }

            telemetry.addLine("dpad left/right = change color    dpad up/down = fine-tune");
            telemetry.addLine();
            telemetry.addData("Color name", colors[index]);
            telemetry.addData("Position", "%.3f", position);
            leftLight.addTelemetry(telemetry);
            rightLight.addTelemetry(telemetry);
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        leftLight.off();
        rightLight.off();
    }

    private static void setBoth(Servo left, Servo right, double position) {
        if (left != null) {
            left.setPosition(position);
        }
        if (right != null) {
            right.setPosition(position);
        }
    }
}
