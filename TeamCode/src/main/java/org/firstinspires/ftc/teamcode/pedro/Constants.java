package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Localizer;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.subsystems.Odometry;

/**
 * Pedro Pathing's setup for our robot (decision 012, issue #25). This is Pedro 3 (the "revhub" library).
 *
 * Pedro has three parts, and this file builds each one:
 *   1. the LOCALIZER: where is the robot? Our Pinpoint, with the settings from Odometry.java
 *   2. the DRIVETRAIN: our four mecanum motors, with the names and directions from Drivetrain.java
 *   3. the ALGORITHM ("Foresight"): how hard to push the wheels to stay on the path. These numbers come from tuning
 * A Follower puts the three together. Advanced Auton gets one with:   Follower follower = Constants.create(hardwareMap);
 *
 * Decision 013: Pedro's Follower drives the wheels ONLY in Advanced Auton (and the Pedro tests). TeleOp and the other
 * autons drive with our own Drivetrain. Pedro READS our constants (motor names, directions, Pinpoint settings), so each
 * setting lives in one place: change it in Drivetrain.java or Odometry.java, never here.
 *
 * Pedro's units: inches, and RADIANS for headings (use Math.toRadians(), or PoseFactory.degrees()).
 * Pedro's directions match ours (decision 007) when the robot starts at (0, 0, 0): +X forward, +Y left,
 * +heading counter-clockwise.
 */
public class Constants {

    // ---------------------------------------------------------------------------------------------------------------
    // Foresight tuning values. ALL are placeholders 🔧 until we run Pedro's AutoTune "Foresight Tuner" on OUR robot.
    // The placeholders are the example numbers from Pedro's docs (someone else's robot), only so the code builds.
    // After tuning, AutoTune shows a block of code with our numbers: copy each number into the matching line here,
    // then set FORESIGHT_TUNED = true. Every value has a row in TeamCode/docs/tuning-guide.md.
    // ---------------------------------------------------------------------------------------------------------------

    // false = these are still the placeholder numbers, so Pedro Test will NOT drive (it would follow someone else's
    // robot's tuning). Set to true only after copying in the Foresight Tuner's results.
    public static final boolean FORESIGHT_TUNED = false;

    // Top speeds (inches per second) at full power, and how fast the robot slows down by itself when the power is cut
    // (inches per second, per second). Measured by the tuner's first steps: "Max Forward / Strafe Velocity" and
    // "Forward / Strafe Deceleration".
    public static final double MAX_FORWARD_VELOCITY = 72.72923108818539;
    public static final double MAX_STRAFE_VELOCITY = 52.34323936525474;
    public static final double NATURAL_FORWARD_DECELERATION = 85.01144677379789;
    public static final double NATURAL_STRAFE_DECELERATION = 104.49787535782846;

    // Turning: how hard to correct a heading error (kP), and how the robot brakes a turn ("Heading Tuner",
    // "Heading Braking").
    public static final double HEADING_KP = 5.258721785960744;
    public static final double HEADING_BRAKE_LINEAR = 0.05642143125655298;
    public static final double HEADING_BRAKE_QUADRATIC = 0.0063829525363003695;

    // Braking forward and sideways ("Forward Braking", "Strafe Braking").
    public static final double FORWARD_BRAKE_LINEAR = 0.10605894992901523;
    public static final double FORWARD_BRAKE_QUADRATIC = 0.0014663966976606565;
    public static final double STRAFE_BRAKE_LINEAR = 0.08719146175596092;
    public static final double STRAFE_BRAKE_QUADRATIC = 0.0013837064502458813;

    // Staying on the path ("Forward Translational", "Strafe Translational"). PRIMARY = more than
    // TRANSLATIONAL_SWITCH_IN inches off the path (push hard), SECONDARY = closer than that (push gently).
    // COAST / BRAKE = how much power per inch-per-second of speed when coasting or braking.
    public static final double FORWARD_TRANSLATIONAL_PRIMARY_KP = 0.3;
    public static final double FORWARD_TRANSLATIONAL_SECONDARY_KP = 0.1;
    public static final double STRAFE_TRANSLATIONAL_PRIMARY_KP = 0.3;
    public static final double STRAFE_TRANSLATIONAL_SECONDARY_KP = 0.1;
    public static final double TRANSLATIONAL_SWITCH_IN = 2.5;   // Pedro's default; the tuner doesn't change it
    public static final double COAST_KV = 0.010978350889324107;
    public static final double BRAKE_KV = 0.008731598255925491;

    // ---------------------------------------------------------------------------------------------------------------
    // The three parts. Nothing below needs tuning.
    // ---------------------------------------------------------------------------------------------------------------

    /** 1. Where the robot is: our Pinpoint, set up exactly like Odometry.java (pod type, offsets, directions). */
    public static final PinpointConfig LOCALIZER_CONFIG = new PinpointConfig(c -> {
        c.name.set(Odometry.PINPOINT_NAME);
        c.podType.set(Odometry.POD_TYPE);
        c.offsetUnits.set(DistanceUnit.MM);   // Odometry.java measures the offsets in millimeters
        c.xPodOffset.set(Odometry.X_POD_OFFSET_MM);
        c.yPodOffset.set(Odometry.Y_POD_OFFSET_MM);
        c.xPodDirection.set(Odometry.X_POD_DIRECTION);
        c.yPodDirection.set(Odometry.Y_POD_DIRECTION);
        c.globalDistanceUnit.set(DistanceUnit.INCH);   // Pedro works in inches
    });

    /** 2. The wheels: our four drive motors, with the names and directions from Drivetrain.java. */
    public static final MecanumConfig DRIVETRAIN_CONFIG = new MecanumConfig(c -> {
        c.frontLeftName.set(Drivetrain.FRONT_LEFT_DRIVE_NAME);
        c.backLeftName.set(Drivetrain.BACK_LEFT_DRIVE_NAME);
        c.frontRightName.set(Drivetrain.FRONT_RIGHT_DRIVE_NAME);
        c.backRightName.set(Drivetrain.BACK_RIGHT_DRIVE_NAME);
        c.frontLeftDirection.set(Drivetrain.LEFT_SIDE_DIRECTION);
        c.backLeftDirection.set(Drivetrain.LEFT_SIDE_DIRECTION);
        c.frontRightDirection.set(Drivetrain.RIGHT_SIDE_DIRECTION);
        c.backRightDirection.set(Drivetrain.RIGHT_SIDE_DIRECTION);
    });

    /** 3. How hard to push the wheels: the tuning values above. */
    public static ForesightConfig foresightConfig() {
        return new ForesightConfig(c -> {
            c.forwardTranslational.set(Controller.piecewise(Controller.proportional(FORWARD_TRANSLATIONAL_SECONDARY_KP))
                    .put(TRANSLATIONAL_SWITCH_IN, Controller.proportional(FORWARD_TRANSLATIONAL_PRIMARY_KP)));
            c.strafeTranslational.set(Controller.piecewise(Controller.proportional(STRAFE_TRANSLATIONAL_SECONDARY_KP))
                    .put(TRANSLATIONAL_SWITCH_IN, Controller.proportional(STRAFE_TRANSLATIONAL_PRIMARY_KP)));

            c.coast.set(Controller.proportionalFeedforward(COAST_KV));
            c.brake.set(Controller.proportionalFeedforward(BRAKE_KV));

            c.headingFeedback.set(Controller.proportional(HEADING_KP));
            c.headingBrakeCoefficients.set(Vector2D.cartesian(HEADING_BRAKE_LINEAR, HEADING_BRAKE_QUADRATIC));

            c.linearBrakeCoefficients.set(Matrix.diag(FORWARD_BRAKE_LINEAR, STRAFE_BRAKE_LINEAR));
            c.quadraticBrakeCoefficients.set(Matrix.diag(FORWARD_BRAKE_QUADRATIC, STRAFE_BRAKE_QUADRATIC));

            c.maxAchievableForwardVelocity.set(MAX_FORWARD_VELOCITY);
            c.maxAchievableStrafeVelocity.set(MAX_STRAFE_VELOCITY);
            c.naturalForwardDeceleration.set(NATURAL_FORWARD_DECELERATION);
            c.naturalStrafeDeceleration.set(NATURAL_STRAFE_DECELERATION);
        });
    }

    /** The Pinpoint, as Pedro's localizer. Calibrates the Pinpoint's gyro: the robot must be STILL (do it in INIT). */
    public static Localizer createLocalizer(HardwareMap hardwareMap) {
        return new PinpointLocalizer(hardwareMap, LOCALIZER_CONFIG);
    }

    /**
     * The drive motors, as Pedro's drivetrain.
     * Pedro never sets the motor mode (decision 008), so first we make our own Drivetrain: its constructor puts the
     * motors in our mode (plain power, RUN_WITHOUT_ENCODER, decision 055) with BRAKE, exactly like every other OpMode.
     * That way Pedro is always tuned and run in the same mode. Nothing moves.
     */
    public static com.pedropathing.drivetrain.Drivetrain createDrivetrain(HardwareMap hardwareMap) {
        new Drivetrain(hardwareMap);   // OUR Drivetrain: sets the motor mode and BRAKE
        return new Mecanum(hardwareMap, DRIVETRAIN_CONFIG);
    }

    /** The whole Follower: Pinpoint + motors + tuning. Call it during INIT, with the robot still. Nothing moves. */
    public static Follower create(HardwareMap hardwareMap) {
        return new Follower(createLocalizer(hardwareMap), createDrivetrain(hardwareMap), new Foresight(foresightConfig()));
    }
}
