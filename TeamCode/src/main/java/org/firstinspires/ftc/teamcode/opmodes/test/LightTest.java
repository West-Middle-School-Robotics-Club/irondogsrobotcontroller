package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.subsystems.Light;

/**
 * TEST: check the goBILDA RGB Indicator Light colors (decision 018).
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
        Light light = new Light(hardwareMap);
        // Test-only: the same light, set directly for fine-tuning (code-structure rule 8).
        Servo indicatorLight = hardwareMap.get(Servo.class, Light.LIGHT_NAME);

        Light.Color[] colors = Light.Color.values();
        int index = 0;
        double position = colors[index].position;

        while (opModeInInit() || opModeIsActive()) {
            if (gamepad1.dpadRightWasPressed()) {
                index = (index + 1) % colors.length;
                light.setColor(colors[index]);
                position = colors[index].position;
            }
            if (gamepad1.dpadLeftWasPressed()) {
                index = (index + colors.length - 1) % colors.length;
                light.setColor(colors[index]);
                position = colors[index].position;
            }
            if (gamepad1.dpadUpWasPressed()) {
                position = Math.min(1.0, position + 0.005);
                indicatorLight.setPosition(position);
            }
            if (gamepad1.dpadDownWasPressed()) {
                position = Math.max(0.0, position - 0.005);
                indicatorLight.setPosition(position);
            }

            telemetry.addLine("dpad left/right = change color    dpad up/down = fine-tune");
            telemetry.addLine();
            telemetry.addData("Color name", colors[index]);
            telemetry.addData("Position", "%.3f", position);
            telemetry.update();
        }

        light.off();
    }
}
