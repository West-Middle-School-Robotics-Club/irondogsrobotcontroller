package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayList;
import java.util.List;

/**
 * Camera subsystem: the Limelight 3A (issue #7, decisions 015, 016).
 *
 * SKELETON: it starts the camera and reports which AprilTags it sees. How we aim the turret with this
 * data is still being designed (issue #7).
 *
 * The camera is mounted ON THE TURRET at a fixed up/down angle, so it turns with the turret.
 * Only the turret moves based on camera data, never the whole robot.
 *
 * Setup before this works:
 *   1. Plug the Limelight into the Control Hub USB port labeled "USB 3.0". The USB 2.0 port will not work.
 *   2. Add it to the Driver Station config as "limelight".
 *   3. Set up an AprilTag pipeline in the Limelight's web page, in the slot set by APRILTAG_PIPELINE.
 */
public class Camera {

    // Config name (decision 009). Must match the Driver Station robot configuration.
    public static final String LIMELIGHT_NAME = "limelight";

    // TODO(#7): which Limelight pipeline slot (0-9) has our AprilTag pipeline.
    public static final int APRILTAG_PIPELINE = 0;

    private final Limelight3A limelight;

    public Camera(HardwareMap hardwareMap) {
        limelight = hardwareMap.get(Limelight3A.class, LIMELIGHT_NAME);

        limelight.pipelineSwitch(APRILTAG_PIPELINE);
        // Start reading results. Nothing moves, so this is OK during INIT.
        limelight.start();
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
        telemetry.addData("AprilTags seen", getVisibleTagIds());
    }
}
