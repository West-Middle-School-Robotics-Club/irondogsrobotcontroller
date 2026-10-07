package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayList;
import java.util.List;

/**
 * Camera subsystem: the Limelight 3A camera and the servo that aims it (issue #7, decision 015).
 *
 * The camera and its servo are ONE subsystem, because aiming the camera only matters for what the camera sees.
 *
 * BIOBUZZ AprilTags are on the BOTTOMS of the CELLS, facing down, so the camera must tilt UP to see them.
 *
 * Setup before this works:
 *   1. Add the Limelight and the servo to the Driver Station config with the names below.
 *   2. Set up an AprilTag pipeline in the Limelight's web page, in the slot set by APRILTAG_PIPELINE.
 *   3. Find the servo positions with the "Camera Test" OpMode, then fill in the TODO(#7) constants.
 */
public class Camera {

    // Config names (decision 009). Must match the Driver Station robot configuration.
    public static final String LIMELIGHT_NAME = "limelight";
    public static final String CAMERA_SERVO_NAME = "cameraServo";

    // TODO(#7): which Limelight pipeline slot (0-9) has our AprilTag pipeline.
    public static final int APRILTAG_PIPELINE = 0;

    // TODO(#7): find these with the "Camera Test" OpMode. Servo positions go from 0.0 to 1.0.
    public static final double LOOK_FORWARD_POSITION = 0.5;
    public static final double LOOK_UP_POSITION = 0.5;

    private final Limelight3A limelight;
    private final Servo cameraServo;

    // The last position we sent to the servo. NaN until the first command.
    private double servoPosition = Double.NaN;

    public Camera(HardwareMap hardwareMap) {
        limelight = hardwareMap.get(Limelight3A.class, LIMELIGHT_NAME);
        cameraServo = hardwareMap.get(Servo.class, CAMERA_SERVO_NAME);

        limelight.pipelineSwitch(APRILTAG_PIPELINE);
        // Start reading results. Nothing moves, so this is OK during INIT.
        limelight.start();

        // We do NOT move the servo here. INIT must not move anything (game rule G304.H).
        // Call lookForward() or lookUp() after START.
    }

    /** Point the camera straight ahead. Call after START, never during INIT. */
    public void lookForward() {
        setServoPosition(LOOK_FORWARD_POSITION);
    }

    /** Tilt the camera up to see the AprilTags under the CELLS. Call after START, never during INIT. */
    public void lookUp() {
        setServoPosition(LOOK_UP_POSITION);
    }

    /** Move the camera servo to a position from 0.0 to 1.0. Prefer lookForward() / lookUp() in real code. */
    public void setServoPosition(double position) {
        servoPosition = Math.max(0.0, Math.min(1.0, position));
        cameraServo.setPosition(servoPosition);
    }

    /** The last position we sent to the servo (NaN if we haven't moved it yet). */
    public double getServoPosition() {
        return servoPosition;
    }

    /** True if the Limelight sees at least one AprilTag right now. */
    public boolean seesAprilTag() {
        return !getVisibleTagIds().isEmpty();
    }

    /** The ID numbers of every AprilTag the Limelight sees right now (BIOBUZZ uses IDs 30-45). */
    public List<Integer> getVisibleTagIds() {
        List<Integer> ids = new ArrayList<>();
        LLResult result = limelight.getLatestResult();
        if (result != null && result.isValid()) {
            for (LLResultTypes.FiducialResult tag : result.getFiducialResults()) {
                ids.add(tag.getFiducialId());
            }
        }
        return ids;
    }

    /** Stop reading results from the Limelight. Call at the end of an OpMode. */
    public void stop() {
        limelight.stop();
    }

    /** Show this subsystem's status on the Driver Station. */
    public void addTelemetry(Telemetry telemetry) {
        telemetry.addData("Camera connected", limelight.isConnected());
        telemetry.addData("Camera servo", Double.isNaN(servoPosition) ? "not moved yet" : String.format("%.2f", servoPosition));
        telemetry.addData("AprilTags seen", getVisibleTagIds());
    }
}
