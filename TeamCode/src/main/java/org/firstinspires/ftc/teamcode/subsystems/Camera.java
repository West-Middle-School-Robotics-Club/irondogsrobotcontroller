package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayList;
import java.util.List;

/**
 * Camera subsystem: the Limelight 3A (issue #7, decisions 015 and 016).
 *
 * The camera is mounted ON THE TURRET at a fixed up/down angle, so it turns with the turret.
 * "The tag is 5 degrees left" means "turn the turret 5 degrees left" (see Turret.turnBy()).
 *
 * BIOBUZZ AprilTags are on the BOTTOMS of the CELLS, facing down, so the camera is tilted up.
 *
 * Setup before this works:
 *   1. Add the Limelight to the Driver Station config as "limelight".
 *   2. Set up an AprilTag pipeline in the Limelight's web page, in the slot set by APRILTAG_PIPELINE.
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
        LLResult result = limelight.getLatestResult();
        return result != null && result.isValid() && !result.getFiducialResults().isEmpty();
    }

    /**
     * How many degrees LEFT of the camera's center the AprilTag is (+ = left, - = right, decision 007).
     * Returns NaN if no tag is seen. The Limelight itself uses + = right, so we flip the sign here.
     * TODO(#7): when we know which tag to aim at, pick that tag ID instead of the Limelight's main target.
     */
    public double getTargetDegreesLeft() {
        if (!seesAprilTag()) {
            return Double.NaN;
        }
        return -limelight.getLatestResult().getTx();
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
        double left = getTargetDegreesLeft();
        telemetry.addData("Target (°)  +left", Double.isNaN(left) ? "none" : String.format("%.1f", left));
    }
}
