package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.subsystems.Camera;
import org.firstinspires.ftc.teamcode.subsystems.Turret;

/**
 * TEST: the camera aims the turret at an AprilTag (issue #7). Do "Turret Test" first!
 *
 * Controls (gamepad 1), all after START:
 *   hold RIGHT TRIGGER   aim: the turret turns toward the AprilTag the camera sees
 *   A                    turret forward
 *
 * The camera is on the turret, so when the tag is centered, the turret is pointing at it.
 */
@TeleOp(name = "Turret Aim Test", group = "Tests")
public class TurretAimTest extends LinearOpMode {

    // Only turn part of the way each loop, so the turret doesn't overshoot and wobble.
    // TODO(#7): tune. Bigger = faster but wobblier. Smaller = smoother but slower.
    static final double AIM_FRACTION = 0.3;
    // Close enough: don't move for tiny errors.
    static final double AIM_TOLERANCE_DEGREES = 1.0;

    @Override
    public void runOpMode() {
        Camera camera = new Camera(hardwareMap);
        Turret turret = new Turret(hardwareMap);

        // INIT: nothing moves until START (game rule G304.H).
        while (opModeInInit()) {
            telemetry.addLine("Press START, then hold RIGHT TRIGGER to aim.");
            camera.addTelemetry(telemetry);
            turret.addTelemetry(telemetry);
            telemetry.update();
        }

        turret.turnForward();

        while (opModeIsActive()) {
            double tagLeft = camera.getTargetDegreesLeft();

            if (gamepad1.right_trigger > 0.5 && !Double.isNaN(tagLeft) && Math.abs(tagLeft) > AIM_TOLERANCE_DEGREES) {
                turret.turnBy(tagLeft * AIM_FRACTION);
            }
            if (gamepad1.aWasPressed()) {
                turret.turnForward();
            }

            turret.update();

            telemetry.addLine("Hold RIGHT TRIGGER = aim     A = forward");
            telemetry.addLine();
            camera.addTelemetry(telemetry);
            turret.addTelemetry(telemetry);
            telemetry.update();
        }

        camera.stop();
    }
}
