package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.hardware.limelightvision.LLStatus;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.teamcode.Alliance;

import java.util.ArrayList;
import java.util.List;

/**
 * Camera subsystem: the Limelight 3A (issue #7, decisions 009, 015, 016, 019, 022).
 *
 * The camera is mounted ON THE TURRET at an upward tilt angle so it can see the
 * BIOBUZZ CELL AprilTag clusters mounted under the HIVE (TU03 Section 9.9).
 *
 * Hardware config name: "limelight" (decision 009). This has been set also in the REV Control Hub for the IronDogs configuration
 * Must be plugged into the Control Hub USB 3.0 port or will not work.
 *
 * BIOBUZZ CELL AprilTag IDs (TU03 Fig 9-17):
 *   - Red, far side:        30, 31, 32, 33
 *   - Red, audience side:   34, 35, 36, 37
 *   - Blue, audience side:  38, 39, 40, 41
 *   - Blue, far side:       42, 43, 44, 45
 *
 * OUR ALLIANCE (decision 022):
 * In a match we only care about OUR alliance's tags: red tags when we are Red, blue tags when we are Blue.
 * The OpMode tells the camera which alliance we are on (constructor or setAlliance()). After that:
 *   - "our tag"   = a tag of our alliance's color. Only these count as a target.
 *   - "other tag" = the other alliance's tag. The camera still SEES them (shown on telemetry as ignored)
 *                   but getTargetDegreesLeft() / Up() / getTargetArea() never aim at them.
 * If more than one of our tags is visible, the target is the one with the biggest area (the closest one).
 * If no alliance is set (plain "Camera Test"), every BIOBUZZ tag counts, which is handy on the bench.
 */
public class Camera {

    // Config name (decision 009). Must match the Driver Station robot configuration.
    public static final String LIMELIGHT_NAME = "limelight";

    // Which Limelight pipeline slot (0-9) has our AprilTag pipeline.
    public static final int APRILTAG_PIPELINE = 0;

    // ---- Measurements for distance math (#7). Measure these on the real robot! ----
    // Upward tilt of the camera, in degrees from level: 0 = looking straight ahead, 90 = straight up.
    // The HIVE cells are ~44 in above the floor with AprilTags facing DOWN, so the camera tilts UP.
    // 10/25: bench bot camera stands upright on a flat surface looking straight ahead = 0.
    public static final double CAMERA_MOUNT_TILT_DEGREES = 0.0; // <-- EDIT when the mount changes (e.g. 25.0)

    // Height of the camera LENS above the floor, in inches. TODO(#7): measure on the robot (placeholder).
    public static final double CAMERA_HEIGHT_INCHES = 13.0;

    // Height of the AprilTag above the floor, in inches. The CELL pivot is ~44 in up (field.md 9.6);
    // the tag is on the CELL's bottom face. TODO(#7): measure on a real HIVE.
    public static final double TAG_HEIGHT_INCHES = 44.0;

    // AprilTag size, in inches (field.md 9.9). The Limelight pipeline's "marker size" MUST be set to this
    // (82.55 mm) or the 3D pose distances will be wrong.
    public static final double TAG_SIZE_INCHES = 3.25;

    // BIOBUZZ AprilTag ID Ranges Template (may change)
    public static final int TAG_RED_FAR_MIN       = 30;
    public static final int TAG_RED_FAR_MAX       = 33;
    public static final int TAG_RED_AUDIENCE_MIN  = 34;
    public static final int TAG_RED_AUDIENCE_MAX  = 37;
    public static final int TAG_BLUE_AUDIENCE_MIN = 38;
    public static final int TAG_BLUE_AUDIENCE_MAX = 41;
    public static final int TAG_BLUE_FAR_MIN      = 42;
    public static final int TAG_BLUE_FAR_MAX      = 45;

    private final Limelight3A limelight;

    // Our alliance this match. null = not chosen yet: every BIOBUZZ tag counts as ours (bench testing).
    private Alliance alliance;

    /** Camera with no alliance chosen: every BIOBUZZ tag (30-45) counts. Use for bench tests. */
    public Camera(HardwareMap hardwareMap) {
        this(hardwareMap, null);
    }

    /** Camera for a match: only OUR alliance's tags count as targets (decision 022). */
    public Camera(HardwareMap hardwareMap, Alliance alliance) {
        limelight = hardwareMap.get(Limelight3A.class, LIMELIGHT_NAME);
        this.alliance = alliance;

        // Start polling connection first, then switch to the AprilTag pipeline slot.
        limelight.start();
        limelight.pipelineSwitch(APRILTAG_PIPELINE);
    }

    /** Change our alliance (for example after the driver picks it during INIT). null = every tag counts. */
    public void setAlliance(Alliance alliance) {
        this.alliance = alliance;
    }

    /** Our alliance, or null if none was chosen. */
    public Alliance getAlliance() {
        return alliance;
    }

    /** True if this tag ID is one WE care about: our alliance's CELL tags (or any BIOBUZZ tag if no alliance is set). */
    public boolean isOurTag(int tagId) {
        if (alliance == null) {
            return isBiobuzzTag(tagId);
        }
        return alliance == Alliance.RED ? isRedAllianceTag(tagId) : isBlueAllianceTag(tagId);
    }

    /** True if the Limelight sees a valid target right now (any tag, either color). */
    public boolean hasTarget() {
        LLResult result = limelight.getLatestResult();
        return result != null && result.isValid();
    }

    /** True if the Limelight sees at least one AprilTag right now (any tag, either color). */
    public boolean seesAprilTag() {
        return !getVisibleTagIds().isEmpty();
    }

    /** True if the Limelight sees at least one of OUR alliance's tags right now. */
    public boolean seesOurTag() {
        return getOurTag(limelight.getLatestResult()) != null;
    }

    /**
     * Horizontal angle offset to OUR target in degrees (+ = target is to the LEFT, - = RIGHT).
     * Flips Limelight's tx (+ = right) to match our team convention (+ = left, decision 007).
     * Returns 0 if none of our tags is visible.
     * Can be added directly to Turret getAngle() or fed to Turret aiming.
     */
    public double getTargetDegreesLeft() {
        LLResultTypes.FiducialResult tag = getOurTag(limelight.getLatestResult());
        return tag == null ? 0.0 : -tag.getTargetXDegrees();
    }

    /** Vertical angle offset to OUR target in degrees (+ = above crosshair, - = below). 0 if none visible. */
    public double getTargetDegreesUp() {
        LLResultTypes.FiducialResult tag = getOurTag(limelight.getLatestResult());
        return tag == null ? 0.0 : tag.getTargetYDegrees();
    }

    /** OUR target's area as a percentage of the image (0 to 100). Bigger = closer. 0 if none visible. */
    public double getTargetArea() {
        LLResultTypes.FiducialResult tag = getOurTag(limelight.getLatestResult());
        return tag == null ? 0.0 : tag.getTargetArea();
    }

    /** The ID of OUR target tag right now, or -1 if none of our tags is visible. */
    public int getTargetTagId() {
        LLResultTypes.FiducialResult tag = getOurTag(limelight.getLatestResult());
        return tag == null ? -1 : tag.getFiducialId();
    }

    // ---------- Distance and angle to OUR target (#7) ----------

    /**
     * METHOD 1 (trig): distance along the FLOOR from the camera to our target, in inches.
     *
     * The tag is a known height above the camera, and the camera reports the angle it looks up
     * at the tag (mount tilt + ty). That makes a right triangle:
     *
     *        distance = (TAG_HEIGHT - CAMERA_HEIGHT) / tan(tilt + ty)
     *
     * Needs CAMERA_MOUNT_TILT_DEGREES, CAMERA_HEIGHT_INCHES and TAG_HEIGHT_INCHES measured correctly.
     * Returns -1 if no target, or if the angle makes no sense (looking level or down at a tag that is above us).
     */
    public double getTargetDistanceInches() {
        LLResultTypes.FiducialResult tag = getOurTag(limelight.getLatestResult());
        if (tag == null) {
            return -1;
        }
        double angleUpDegrees = CAMERA_MOUNT_TILT_DEGREES + tag.getTargetYDegrees();
        if (angleUpDegrees <= 0 || angleUpDegrees >= 90) {
            return -1;
        }
        return (TAG_HEIGHT_INCHES - CAMERA_HEIGHT_INCHES) / Math.tan(Math.toRadians(angleUpDegrees));
    }

    /**
     * METHOD 2 (Limelight 3D pose): distance along the FLOOR from the camera to our target, in inches,
     * from the Limelight's own 3D solve. Needs the tag size set in the Limelight pipeline (TAG_SIZE_INCHES).
     * Limelight camera space: X = right, Y = down, Z = forward, so floor distance = sqrt(x² + z²).
     * Returns -1 if no target or no pose.
     */
    public double getTargetPoseDistanceInches() {
        Pose3D pose = getTargetPose();
        if (pose == null) {
            return -1;
        }
        Position p = pose.getPosition().toUnit(DistanceUnit.INCH);
        return Math.sqrt(p.x * p.x + p.z * p.z);
    }

    /**
     * METHOD 2: how the tag is turned relative to the camera, in degrees. 0 = we see it head-on;
     * bigger = we are looking at it from the side. Sign (+ = ?) to be checked with the camera tests.
     * Note: the BIOBUZZ tags face DOWN, so check which of yaw/pitch/roll changes when the robot moves
     * sideways around a CELL; this method may need to use a different one. Returns 0 if no target.
     */
    public double getTargetYawDegrees() {
        Pose3D pose = getTargetPose();
        return pose == null ? 0.0 : pose.getOrientation().getYaw(AngleUnit.DEGREES);
    }

    /** Our target's 3D pose relative to the camera (Limelight "target pose, camera space"), or null. */
    public Pose3D getTargetPose() {
        LLResultTypes.FiducialResult tag = getOurTag(limelight.getLatestResult());
        return tag == null ? null : tag.getTargetPoseCameraSpace();
    }

    /**
     * Pick OUR target from a result packet: the visible tag of our alliance with the biggest area
     * (the closest one). Returns null if none of our tags is visible.
     * The other alliance's tags are skipped here, which is how "ignore the other color" works.
     */
    private LLResultTypes.FiducialResult getOurTag(LLResult result) {
        LLResultTypes.FiducialResult best = null;
        if (result != null && result.isValid()) {
            List<LLResultTypes.FiducialResult> fiducials = result.getFiducialResults();
            if (fiducials != null) {
                for (LLResultTypes.FiducialResult tag : fiducials) {
                    if (!isOurTag(tag.getFiducialId())) {
                        continue; // other alliance (or not a BIOBUZZ tag): ignore
                    }
                    if (best == null || tag.getTargetArea() > best.getTargetArea()) {
                        best = tag;
                    }
                }
            }
        }
        return best;
    }

    /** The IDs of OUR alliance's tags the Limelight sees right now. */
    public List<Integer> getOurVisibleTagIds() {
        List<Integer> ours = new ArrayList<>();
        for (int id : getVisibleTagIds()) {
            if (isOurTag(id)) {
                ours.add(id);
            }
        }
        return ours;
    }

    /** The IDs of the OTHER alliance's tags the Limelight sees right now (seen, but ignored for aiming). */
    public List<Integer> getOtherVisibleTagIds() {
        List<Integer> others = new ArrayList<>();
        for (int id : getVisibleTagIds()) {
            if (isBiobuzzTag(id) && !isOurTag(id)) {
                others.add(id);
            }
        }
        return others;
    }

    /** The ID numbers of every AprilTag the Limelight sees right now (BIOBUZZ uses IDs 30-45). */
    public List<Integer> getVisibleTagIds() {
        return getVisibleTagIds(limelight.getLatestResult());
    }

    /** Helper method using an existing LLResult packet to avoid double-polling the SDK. */
    public List<Integer> getVisibleTagIds(LLResult result) {
        List<Integer> ids = new ArrayList<>();
        if (result != null && result.isValid()) {
            List<LLResultTypes.FiducialResult> fiducials = result.getFiducialResults();
            if (fiducials != null) {
                for (LLResultTypes.FiducialResult tag : fiducials) {
                    ids.add(tag.getFiducialId());
                }
            }
        }
        return ids;
    }

    /** Check if a given AprilTag ID belongs to a BIOBUZZ CELL target (IDs 30-45). If not ignore (overkill but) */
    public static boolean isBiobuzzTag(int tagId) {
        return tagId >= TAG_RED_FAR_MIN && tagId <= TAG_BLUE_FAR_MAX;
    }

    /** Check if a tag ID belongs to Red Alliance CELLs (IDs 30-37). */
    public static boolean isRedAllianceTag(int tagId) {
        return tagId >= TAG_RED_FAR_MIN && tagId <= TAG_RED_AUDIENCE_MAX;
    }

    /** Check if a tag ID belongs to Blue Alliance CELLs (IDs 38-45). */
    public static boolean isBlueAllianceTag(int tagId) {
        return tagId >= TAG_BLUE_AUDIENCE_MIN && tagId <= TAG_BLUE_FAR_MAX;
    }

    /** Stop reading results from the Limelight. Call at the end of an OpMode. */
    public void stop() {
        limelight.stop();
    }

    /** Show clean connection and detection details on the Driver Station. */
    public void addTelemetry(Telemetry telemetry) {
        boolean connected = limelight.isConnected();
        telemetry.addData("Camera connected", connected);

        if (!connected) {
            telemetry.addData("Camera status", "Disconnected - Check USB 3.0 port");
            return;
        }

        LLStatus status = limelight.getStatus();
        if (status != null) {
            telemetry.addData("FPS", status.getFps());
        }

        LLResult result = limelight.getLatestResult();
        List<Integer> visibleIds = getVisibleTagIds(result);
        LLResultTypes.FiducialResult ourTag = getOurTag(result);

        telemetry.addData("Alliance", alliance == null ? "none (all tags count)" : alliance);
        telemetry.addData("Camera mount tilt (°)", "%.1f", CAMERA_MOUNT_TILT_DEGREES);

        if (visibleIds.isEmpty()) {
            telemetry.addData("AprilTags seen", "None");
            return;
        }

        telemetry.addData("AprilTags seen", visibleIds);
        telemetry.addData("Our tags", getOurVisibleTagIds());
        telemetry.addData("Other tags (ignored)", getOtherVisibleTagIds());

        if (ourTag == null) {
            telemetry.addData("Target", "None of OUR tags in view");
        } else {
            telemetry.addData("Target tag", ourTag.getFiducialId());
            telemetry.addData("Target angle left (°)", "%.1f", -ourTag.getTargetXDegrees());
            telemetry.addData("Target angle up (°)", "%.1f", ourTag.getTargetYDegrees());
            telemetry.addData("Target area (%)", "%.2f", ourTag.getTargetArea());
            telemetry.addData("Distance: trig (in)", "%.1f", getTargetDistanceInches());
            telemetry.addData("Distance: pose (in)", "%.1f", getTargetPoseDistanceInches());
            telemetry.addData("Tag yaw (°)", "%.1f", getTargetYawDegrees());
        }
    }
}
