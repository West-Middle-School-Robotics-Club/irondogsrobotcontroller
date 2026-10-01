package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

/**
 * Odometry subsystem: the goBILDA Pinpoint with two dead-wheel pods (decision 005).
 * Tells us where the robot is on the field: X, Y and heading.
 *
 * Directions follow our team convention (decision 007):
 * +X = forward, +Y = LEFT, +heading = COUNTER-CLOCKWISE.
 *
 * The settings below are tuned with the "Pinpoint Test" OpMode (issue #6).
 * Autonomous will use these same settings, so tune them carefully!
 */
public class Odometry {

    // Config name (decision 009). Must match the Driver Station robot configuration.
    public static final String PINPOINT_NAME = "pinpoint";

    // Our pods are 4-BAR pods (32 mm wheels). Found in the #6 test on 9/30: with this set to
    // SWINGARM (48 mm wheels), a 48 in push read about 73 in. 4-bar pods make 1.5x as many
    // encoder ticks per inch, so the wrong setting over-reads by 1.5x (48 x 1.5 = 72).
    public static final GoBildaPinpointDriver.GoBildaOdometryPods POD_TYPE =
            GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD;

    // TODO(#6) Step 1: measure from the robot's center, in millimeters.
    // X pod offset: how far SIDEWAYS the X (forward) pod is.   Left of center = +, right = -
    // Y pod offset: how far FORWARD the Y (strafe) pod is.     In front of center = +, behind = -
    public static final double X_POD_OFFSET_MM = 0.0;
    public static final double Y_POD_OFFSET_MM = 0.0;

    // TODO(#6) Step 2: push the robot forward, X must go UP. Push it left, Y must go UP.
    // If one goes down instead, change that pod to REVERSED.
    public static final GoBildaPinpointDriver.EncoderDirection X_POD_DIRECTION =
            GoBildaPinpointDriver.EncoderDirection.FORWARD;
    public static final GoBildaPinpointDriver.EncoderDirection Y_POD_DIRECTION =
            GoBildaPinpointDriver.EncoderDirection.FORWARD;

    private final GoBildaPinpointDriver pinpoint;

    public Odometry(HardwareMap hardwareMap) {
        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, PINPOINT_NAME);

        pinpoint.setOffsets(X_POD_OFFSET_MM, Y_POD_OFFSET_MM, DistanceUnit.MM);
        pinpoint.setEncoderResolution(POD_TYPE);
        pinpoint.setEncoderDirections(X_POD_DIRECTION, Y_POD_DIRECTION);

        // Sets the position to 0,0,0 and calibrates the IMU.
        // The robot must be STILL until getStatus() says READY.
        pinpoint.resetPosAndIMU();
    }

    /** Read new data from the Pinpoint. Call this ONCE at the start of every loop. */
    public void update() {
        pinpoint.update();
    }

    /** Where the robot is: X, Y and heading. */
    public Pose2D getPose() {
        return pinpoint.getPosition();
    }

    /** READY when it's working. CALIBRATING right after a reset. FAULT_... means a problem. */
    public GoBildaPinpointDriver.DeviceStatus getStatus() {
        return pinpoint.getDeviceStatus();
    }

    /** Show this subsystem's status on the Driver Station. */
    public void addTelemetry(Telemetry telemetry) {
        Pose2D pose = getPose();
        telemetry.addData("Odometry status", getStatus());
        telemetry.addData("X (in)       +forward", "%.2f", pose.getX(DistanceUnit.INCH));
        telemetry.addData("Y (in)       +left", "%.2f", pose.getY(DistanceUnit.INCH));
        telemetry.addData("Heading (°)  +counter-clockwise", "%.1f", pose.getHeading(AngleUnit.DEGREES));
    }
}
