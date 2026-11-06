package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Alliance;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.subsystems.Light;
import org.firstinspires.ftc.teamcode.subsystems.Odometry;
import org.firstinspires.ftc.teamcode.subsystems.Power;

/**
 * BASIC AUTON (issue #22, decisions 011, 014, 020, 026): LEAVE + PARK, drive-only.
 *
 *   LEAVE (3 pts): the robot stops touching the wall it started against.
 *   PARK  (5 pts): at the end of AUTO, the robot is at least partly in our LOADING ZONE.
 *   Both alliance robots doing LEAVE + PARK = 16 points = the SWARM ranking point.
 *
 * This is the SHARED code. The four OpModes (Red/Blue x Near/Far) only pick an alliance and a route.
 *
 * How it drives: the Pinpoint (Odometry) knows where the robot is on the field. For each waypoint, we drive
 * toward it, faster when far and slower when close, until we're close enough ("proportional control").
 *
 * Positions are written ONCE, for RED, in field coordinates (decision 011: origin at field center,
 * +X toward the far wall, +Y toward the red wall, heading counter-clockwise, 0 = facing the far wall).
 * BLUE is calculated: Blue = Red rotated 180 degrees, (x, y, heading) -> (-x, -y, heading + 180).
 *
 * Rules this code follows:
 *   - Nothing moves during INIT (G304.H). The drive team sets a start DELAY with the dpad during INIT
 *   - Every waypoint stays on OUR half of the field (G402) and away from the HIVE (G417)
 *   - Everything stops before 30 s, so nothing moves in the transition (G403)
 *   - Uses BRAKE so the robot stops exactly where it should (decision 023)
 */
public abstract class BasicAuton extends LinearOpMode {

    /** Which starting spot. NEAR = against our alliance wall, next to the LOADING ZONE. FAR = against the audience wall. */
    public enum Route { NEAR, FAR }

    /** Which alliance this OpMode is for. */
    protected abstract Alliance alliance();

    /** Which route this OpMode runs. */
    protected abstract Route route();

    // ------------------------------------------------------------------------------------------------
    // ROBOT SIZE, measured 11/3: 456.59 mm x 406.19 mm (hardware-config.md). Update if the robot changes.
    // ------------------------------------------------------------------------------------------------
    public static final double ROBOT_LENGTH_IN = 17.98; // front to back (456.59 mm)
    public static final double ROBOT_WIDTH_IN = 15.99;  // left to right (406.19 mm)

    // ------------------------------------------------------------------------------------------------
    // RED positions (inches, degrees). See TeamCode/docs/game/field.md for the map.
    // TODO(#22): check each one on a taped practice field and adjust.
    // ------------------------------------------------------------------------------------------------
    // NEAR: start in tile A6, LEFT side touching the RED wall (y = 72), facing the far wall.
    // The LOADING ZONE (A5) is right next to it, toward the audience.
    static final Pose2D RED_NEAR_START = redPose(60, 72 - ROBOT_WIDTH_IN / 2, 0);
    static final Pose2D[] RED_NEAR_PATH = {
            redPose(60, 50, 0),   // LEAVE: slide right, off the red wall
            redPose(40, 61, 0),   // PARK: in the wall end of the LOADING ZONE (zone: x 24.5 to 47.5, y 61 to 72)
    };

    // FAR: start in tile B1, BACK touching the AUDIENCE wall (x = -72), facing the far wall.
    static final Pose2D RED_FAR_START = redPose(-72 + ROBOT_LENGTH_IN / 2, 36, 0);
    static final Pose2D[] RED_FAR_PATH = {
            redPose(-48, 40, 0),  // LEAVE: drive forward, off the audience wall
            redPose(24, 40, 0),   // drive up column B, clear of the HIVE (column C, y < 24)
            redPose(30, 58, 0),   // PARK: in the field end of the LOADING ZONE
    };

    // ------------------------------------------------------------------------------------------------
    // DRIVING. TODO(#22): tune on the robot.
    // ------------------------------------------------------------------------------------------------
    static final double DRIVE_GAIN = 0.05;           // power per inch away. 20 in away = 1.0 (then capped)
    static final double MAX_DRIVE_POWER = 0.6;       // fastest we drive
    static final double MIN_DRIVE_POWER = 0.15;      // slowest we drive (any slower and the robot may not move)
    static final double TURN_GAIN = 0.02;            // turn power per degree away
    static final double MAX_TURN_POWER = 0.4;
    static final double POSITION_TOLERANCE_IN = 1.5; // "close enough" to a waypoint
    static final double HEADING_TOLERANCE_DEG = 3.0;
    static final double WAYPOINT_TIMEOUT_S = 8.0;    // give up on one waypoint after this long
    static final double STOP_BY_S = 28.0;            // stop everything by this time (AUTO is 30 s)
    static final int MAX_DELAY_S = 10;               // start delay the drive team can choose in INIT

    // What the lights mean in Basic Auton. Colors are in Light.Color.
    static final Light.Color RED_ALLIANCE_COLOR = Light.Color.RED;   // during INIT
    static final Light.Color BLUE_ALLIANCE_COLOR = Light.Color.BLUE; // during INIT
    static final Light.Color PARKED_COLOR = Light.Color.GREEN;       // reached every waypoint
    static final Light.Color STOPPED_EARLY_COLOR = Light.Color.YELLOW; // a waypoint timed out, or out of time

    @Override
    public void runOpMode() {
        Alliance alliance = alliance();
        Route route = route();

        Robot robot = new Robot(hardwareMap);
        Odometry odometry = new Odometry(hardwareMap);   // resets the Pinpoint: robot must be STILL in INIT
        Light leftLight = new Light(hardwareMap, Light.LEFT_LIGHT_NAME);
        Light rightLight = new Light(hardwareMap, Light.RIGHT_LIGHT_NAME);
        Power power = new Power(hardwareMap);

        robot.drivetrain.setBrakeOnStop(true);           // autonomous stops precisely (decision 023)

        Pose2D start = forAlliance(route == Route.NEAR ? RED_NEAR_START : RED_FAR_START, alliance);
        Pose2D[] path = route == Route.NEAR ? RED_NEAR_PATH : RED_FAR_PATH;

        // ---------- INIT: show the alliance, choose a delay. Nothing moves. ----------
        Light.Color allianceColor = alliance == Alliance.RED ? RED_ALLIANCE_COLOR : BLUE_ALLIANCE_COLOR;
        leftLight.setColor(allianceColor);
        rightLight.setColor(allianceColor);
        int delaySeconds = 0;

        while (opModeInInit()) {
            odometry.update();
            if (gamepad1.dpadUpWasPressed()) {
                delaySeconds = Math.min(MAX_DELAY_S, delaySeconds + 1);
            }
            if (gamepad1.dpadDownWasPressed()) {
                delaySeconds = Math.max(0, delaySeconds - 1);
            }

            telemetry.addData("BASIC AUTON", "%s  %s  (lights are %s)", alliance, route, allianceColor);
            telemetry.addLine("Wrong one? Stop and pick the right Basic Auton.");
            telemetry.addData("Start delay", "%d s   (dpad up/down to change)", delaySeconds);
            telemetry.addData("Start position", poseText(start));
            telemetry.addData("Pinpoint", "%s  (keep the robot STILL until READY)", odometry.getStatus());
            power.addTelemetry(telemetry);
            telemetry.update();
        }
        if (isStopRequested()) {
            leftLight.off();
            rightLight.off();
            return;
        }

        // ---------- AUTO ----------
        ElapsedTime autoTimer = new ElapsedTime();
        odometry.setPose(start);   // the Pinpoint now reports FIELD positions

        // Start delay, for when our partner's route crosses ours
        while (opModeIsActive() && autoTimer.seconds() < delaySeconds) {
            odometry.update();
            showStatus("Waiting (start delay)", autoTimer, odometry, null);
        }

        // Drive to each waypoint in order
        boolean allReached = true;
        for (int i = 0; i < path.length && opModeIsActive(); i++) {
            Pose2D target = forAlliance(path[i], alliance);
            boolean reached = driveTo(robot, odometry, target, autoTimer, "Waypoint " + (i + 1) + " of " + path.length);
            allReached &= reached;
            if (autoTimer.seconds() >= STOP_BY_S) {
                if (i < path.length - 1) {
                    allReached = false;   // out of time with waypoints left: YELLOW, not GREEN
                }
                break;
            }
        }
        robot.drivetrain.stop();

        // Done: GREEN = parked where we planned, YELLOW = stopped early (timeout or time limit)
        Light.Color doneColor = allReached ? PARKED_COLOR : STOPPED_EARLY_COLOR;
        leftLight.setColor(doneColor);
        rightLight.setColor(doneColor);
        while (opModeIsActive()) {
            odometry.update();
            showStatus(allReached ? "PARKED" : "STOPPED EARLY (check telemetry/timeouts)", autoTimer, odometry, null);
        }
        robot.stop();
    }

    /**
     * Drive to one field position (proportional control), then stop.
     * Returns true if we got there, false if we ran out of time.
     */
    private boolean driveTo(Robot robot, Odometry odometry, Pose2D target, ElapsedTime autoTimer, String label) {
        ElapsedTime waypointTimer = new ElapsedTime();
        while (opModeIsActive()) {
            odometry.update();
            Pose2D pose = odometry.getPose();

            // How far, in FIELD directions
            double dx = target.getX(DistanceUnit.INCH) - pose.getX(DistanceUnit.INCH);
            double dy = target.getY(DistanceUnit.INCH) - pose.getY(DistanceUnit.INCH);
            double distance = Math.hypot(dx, dy);
            double headingError = angleWrap(target.getHeading(AngleUnit.DEGREES) - pose.getHeading(AngleUnit.DEGREES));

            if (distance < POSITION_TOLERANCE_IN && Math.abs(headingError) < HEADING_TOLERANCE_DEG) {
                robot.drivetrain.stop();
                return true;
            }
            if (waypointTimer.seconds() > WAYPOINT_TIMEOUT_S || autoTimer.seconds() > STOP_BY_S) {
                robot.drivetrain.stop();
                return false;
            }

            // Turn the FIELD direction into ROBOT directions (forward / left), using the robot's heading
            double heading = pose.getHeading(AngleUnit.RADIANS);
            double forward = dx * Math.cos(heading) + dy * Math.sin(heading);
            double left = -dx * Math.sin(heading) + dy * Math.cos(heading);

            // Faster when far, slower when close
            double power = 0.0;
            if (distance >= POSITION_TOLERANCE_IN) {
                power = Math.max(MIN_DRIVE_POWER, Math.min(MAX_DRIVE_POWER, DRIVE_GAIN * distance));
            }
            double scale = distance > 0 ? power / distance : 0.0;
            double turn = Math.max(-MAX_TURN_POWER, Math.min(MAX_TURN_POWER, TURN_GAIN * headingError));

            robot.drivetrain.drive(forward * scale, left * scale, turn);
            showStatus(label + String.format(": %.1f in to go", distance), autoTimer, odometry, target);
        }
        robot.drivetrain.stop();
        return false;
    }

    private void showStatus(String step, ElapsedTime autoTimer, Odometry odometry, Pose2D target) {
        telemetry.addData("BASIC AUTON", "%s  %s", alliance(), route());
        telemetry.addData("Step", step);
        telemetry.addData("Time", "%.1f s", autoTimer.seconds());
        telemetry.addData("Robot", poseText(odometry.getPose()));
        if (target != null) {
            telemetry.addData("Target", poseText(target));
        }
        telemetry.update();
    }

    /** A RED field position, in inches and degrees. */
    static Pose2D redPose(double xInches, double yInches, double headingDegrees) {
        return new Pose2D(DistanceUnit.INCH, xInches, yInches, AngleUnit.DEGREES, headingDegrees);
    }

    /** RED positions are used as-is. BLUE = RED rotated 180 degrees: (x, y, h) -> (-x, -y, h + 180). */
    static Pose2D forAlliance(Pose2D redPose, Alliance alliance) {
        if (alliance == Alliance.RED) {
            return redPose;
        }
        return redPose(-redPose.getX(DistanceUnit.INCH), -redPose.getY(DistanceUnit.INCH),
                angleWrap(redPose.getHeading(AngleUnit.DEGREES) + 180));
    }

    /** Keep an angle between -180 and 180 degrees, so the robot turns the short way. */
    static double angleWrap(double degrees) {
        while (degrees > 180) {
            degrees -= 360;
        }
        while (degrees <= -180) {
            degrees += 360;
        }
        return degrees;
    }

    private static String poseText(Pose2D pose) {
        return String.format("x %.1f  y %.1f  heading %.0f°",
                pose.getX(DistanceUnit.INCH), pose.getY(DistanceUnit.INCH), pose.getHeading(AngleUnit.DEGREES));
    }
}
