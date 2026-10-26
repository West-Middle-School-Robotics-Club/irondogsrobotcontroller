package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.teamcode.Alliance;
import org.firstinspires.ftc.teamcode.subsystems.Camera;
import org.firstinspires.ftc.teamcode.subsystems.Power;

/**
 * TEST: check that the camera only targets OUR alliance's AprilTags (issue #7, decision 022).
 *
 * This is the shared part of "Red Camera Test" and "Blue Camera Test"; those two just pick the alliance.
 * Nothing moves in this test.
 *
 * How to test: hold one red tag (30-37) and one blue tag (38-45) in front of the camera.
 *   - "AprilTags seen" should list BOTH.
 *   - "Our tags" should list only our color; "Other tags (ignored)" only the other color.
 *   - "Target tag" must always be one of OUR color, even when the other tag is closer or more centered.
 *   - Hide our tag: "Target" should say none of our tags is in view, and the angles go to 0.
 *
 * DISTANCE CHECK (#7): put our tag at a measured distance (try 36, 72 and 120 in, tape measure from the
 * camera lens) and compare "Distance: trig" and "Distance: pose" with the tape. Trig needs the camera
 * height, tag height and tilt in Camera.java to be right; pose needs the tag size set in the Limelight
 * pipeline. Then walk sideways around the tag and watch the raw yaw / pitch / roll lines to learn which
 * one changes: that's the "angle we're seeing the tag from".
 */
public abstract class AllianceCameraTest extends LinearOpMode {

    /** Which alliance this test pretends we are on. */
    protected abstract Alliance alliance();

    @Override
    public void runOpMode() {
        Power power = new Power(hardwareMap); // battery voltage + current on every test
        Camera camera = new Camera(hardwareMap, alliance());

        telemetry.addLine("Hold a RED tag (30-37) and a BLUE tag (38-45) in front of the camera.");
        telemetry.addLine("Target tag must always be " + alliance() + ".");

        while (opModeInInit() || opModeIsActive()) {
            telemetry.addData("Testing as", alliance());
            telemetry.addData("Sees our tag", camera.seesOurTag());
            telemetry.addData("Target tag id (-1 = none)", camera.getTargetTagId());
            camera.addTelemetry(telemetry);

            // Raw 3D pose, for learning which angle means what (test-only detail).
            Pose3D pose = camera.getTargetPose();
            if (pose != null) {
                Position p = pose.getPosition().toUnit(DistanceUnit.INCH);
                telemetry.addLine("--- raw pose (camera space: x right, y down, z forward) ---");
                telemetry.addData("x / y / z (in)", "%.1f  %.1f  %.1f", p.x, p.y, p.z);
                telemetry.addData("yaw / pitch / roll (°)", "%.1f  %.1f  %.1f",
                        pose.getOrientation().getYaw(AngleUnit.DEGREES),
                        pose.getOrientation().getPitch(AngleUnit.DEGREES),
                        pose.getOrientation().getRoll(AngleUnit.DEGREES));
            }
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        camera.stop();
    }
}
