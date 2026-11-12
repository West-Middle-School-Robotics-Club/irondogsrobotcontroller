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
 * BASIC AUTON (issue #22, decisions 011, 014, 020, 026, 046): LEAVE + PARK, drive-only.
 *
 *   LEAVE (3 pts): the robot stops touching the wall it started against.
 *   PARK  (5 pts): at the end of AUTO, the robot is at least partly in our LOADING ZONE.
 *   Both alliance robots doing LEAVE + PARK = 16 points = the SWARM ranking point.
 *
 * This is the SHARED code. The four OpModes (Red/Blue x Near/Far) only pick an alliance and a route.
 *
 * How it drives: at START the Pinpoint (Odometry) is told where the robot is on the field (the start position for
 * the route). From then on it reports field positions. For each waypoint the robot drives toward it, faster when far
 * and slower when close, brakes a little early so it doesn't slide past, and moves on when it has stopped close
 * enough ("proportional control").
 *
 * Positions are written ONCE, for RED, in field coordinates (decision 011: origin at field center,
 * +X toward the far wall, +Y toward the red wall, heading counter-clockwise, 0 = facing the far wall).
 * BLUE is calculated: Blue = Red rotated 180 degrees, (x, y, heading) -> (-x, -y, heading + 180).
 *
 * CAREFUL when writing a waypoint: the start HEADING decides which field direction is "forward". NEAR starts facing
 * -90 (toward -Y), so "forward 23" LOWERS y by 23 and leaves x alone; "right" is -X there. FAR starts facing 0, so
 * forward is +X and left is +Y. Each waypoint comment below says the move in robot words (forward / right / left)
 * and the field numbers do the math. Main Auton (MainAuton.java, was Test Auton) runs robot moves from a (0, 0, 0) start (decision 060).
 *
 * Rules this code follows:
 *   - Nothing moves during INIT (G304.H). The drive team sets a start DELAY with the dpad during INIT
 *   - Every waypoint stays on OUR half of the field (G402) and away from the HIVE (G417)
 *   - Everything stops before 30 s, so nothing moves in the transition (G403)
 *   - Uses BRAKE so the robot stops exactly where it should (decision 023)
 */
public abstract class BasicAuton extends LinearOpMode {

    /** Measures the time between telemetry updates = one pass of the loop (shown as "Loop"). */
    private final ElapsedTime loopTimer = new ElapsedTime();

    /** The robot, kept here so every status screen can show the four wheel powers. Set at the start of runOpMode(). */
    private Robot robot;

    /** Which starting spot. NEAR = against our alliance wall, next to the LOADING ZONE. FAR = against the audience wall. */
    public enum Route { NEAR, FAR }

    /** Which alliance this OpMode is for (the lights during INIT). */
    protected abstract Alliance alliance();

    /** Which route this OpMode runs. */
    protected abstract Route route();

    /**
     * Where the robot starts, in the coordinates the Pinpoint will use: the RED field start for the route, rotated
     * for BLUE. Main Auton overrides this (it starts at 0, 0, 0).
     */
    protected Pose2D startPose() {
        return forAlliance(route() == Route.NEAR ? RED_NEAR_START : RED_FAR_START, alliance());
    }

    /** The waypoints to drive to, in order, in the same coordinates as startPose(). A test OpMode can override this. */
    protected Pose2D[] path() {
        Pose2D[] redPath = route() == Route.NEAR ? RED_NEAR_PATH : RED_FAR_PATH;
        Pose2D[] path = new Pose2D[redPath.length];
        for (int i = 0; i < redPath.length; i++) {
            path[i] = forAlliance(redPath[i], alliance());
        }
        return path;
    }

    /** The "SET UP:" line shown during INIT. A test OpMode can override this to say where it really starts. */
    protected String setupInstructions() {
        return setupText(route(), alliance());
    }

    // ------------------------------------------------------------------------------------------------
    // ROBOT SIZE, measured 11/3: 456.59 mm x 406.19 mm (hardware-config.md). Update if the robot changes.
    // ------------------------------------------------------------------------------------------------
    public static final double ROBOT_LENGTH_IN = 17.98; // front to back (456.59 mm)
    public static final double ROBOT_WIDTH_IN = 15.99;  // left to right (406.19 mm)

    // ------------------------------------------------------------------------------------------------
    // RED positions (inches, degrees), field coordinates (decision 011). See TeamCode/docs/game/field.md for the map.
    // Every position is where the robot's CENTER is. The moves were measured on the floor 11/10-11/11 with Test Auton
    // (MainAuton.java started from the same moves; it has since been tuned on its own, decision 060). Re-run make_diagrams.py after
    // changing a position so the route sheets match.
    // Every Basic Auton starts with the robot's BACK touching a wall (decision 046), so the drive team has one rule.
    // ------------------------------------------------------------------------------------------------
    // NEAR: start in tile A6, BACK touching the RED wall (y = 72), FRONT facing into the field (heading -90).
    // The LOADING ZONE (A5) is right next to it, toward the audience (to the robot's RIGHT).
    // Facing -90: forward = -Y, right = -X. Center starts at y = 72 - 9 = 63.
    static final Pose2D RED_NEAR_START = pose(60, 72 - ROBOT_LENGTH_IN / 2, -90);
    static final Pose2D[] RED_NEAR_PATH = {
            pose(60, 40, -90),  // LEAVE: 23 in forward, off the red wall (y 63 -> 40)
            pose(48, 40, -90),  // 12 in RIGHT, toward the audience (x 60 -> 48), in front of the LOADING ZONE
            pose(48, 58, -90),  // PARK: 18 in back (y 40 -> 58), into the LOADING ZONE. Back edge at y 67, 5 in off the wall
    };
    // Same goal moves as Timed Auton NEAR (forward 23, right 12, back 18), which is what we run for NEAR (060), so the
    // route sheet shows it. Before 11/12 this held (83, 40), (83, 28), (65, 28): x 83 is past the red wall (x 72).
    // LOADING ZONE: x 24.5 (audience end) to 47.5 (far-wall end), y 61 to 72. PARK only needs the robot PARTLY in it:
    // at (48, 58) the robot spans x 40-56, y 49-67, so its far-wall side is in. The audience end is left for our partner.

    // FAR: start in tile B1, BACK touching the AUDIENCE wall (x = -72), FRONT facing the far wall (heading 0).
    // Facing 0: forward = +X, left = +Y. Center starts at x = -72 + 9 = -63.
    static final Pose2D RED_FAR_START = pose(-72 + ROBOT_LENGTH_IN / 2, 36, 0);
    static final Pose2D[] RED_FAR_PATH = {
            pose(22, 36, 0),    // LEAVE: 85 in forward, up column B (x -63 -> 22). The HIVE is in column C, to the right
            pose(22, 58, 0),    // PARK: 22 in LEFT (y 36 -> 58), on the line between rows 4 and 5, partly in the LOADING ZONE
    };
    // Same moves as Main Auton FAR (MAIN_FAR_PATH: forward 85, left 22), so the route sheet shows what we run (060).
    // Park between rows 4 and 5: the robot spans x 13-31, y 50-66; the row 4/5 line is x = 24. The LOADING ZONE is
    // x 24.5-47.5, y 61-72, so the robot is partly in it, and x 31-47.5 (most of row 5) is left for our partner.

    // ------------------------------------------------------------------------------------------------
    // DRIVING. Tune on the robot (tuning-guide.md has a row for each).
    // ------------------------------------------------------------------------------------------------
    // How the drive motors take "power" during Basic Auton (decision 055: plain power everywhere):
    //   false = plain power (RUN_WITHOUT_ENCODER): power = % of battery voltage. Same as Main TeleOp
    //   true  = encoder speed control (RUN_USING_ENCODER): power = % of top speed, the hub holds that speed.
    //           At very low powers the hub's speed loop surged (lurch, stop, lurch), which is why we stopped using it.
    //           If it ever goes back to true, the powers below mean something else: MIN_DRIVE_POWER can drop to ~0.05
    // Main Auton inherits this.
    static final boolean USE_ENCODER_SPEED_CONTROL = false;
    static final double DRIVE_GAIN = 0.02;           // power per inch away. Slowing starts MAX_DRIVE_POWER / DRIVE_GAIN = 10 in out
    // Powers below are PLAIN POWER starting points (11/11). With encoders the team had 0.1 / 0.1 / 0.03; those mean
    // "% of top speed" and are too low to move the robot on plain power. Re-tune on the floor.
    static final double MAX_DRIVE_POWER = 0.5;       // fastest we drive forward/back
    static final double MAX_SIDEWAYS_POWER = 0.25;   // fastest we drive sideways (strafing needs more power for the same speed, but slides more)
    static final double MIN_DRIVE_POWER = 0.2;      // slowest we drive (a crawl). Plain power: below ~0.12 the robot doesn't move at all
    static final double BRAKE_LOOKAHEAD_S = 0.15;    // brake this many seconds early (forward/back): a moving robot slides past the spot otherwise
    static final double BRAKE_LOOKAHEAD_SIDEWAYS_S = 0.3; // same for sideways moves: braked mecanum wheels still roll sideways on their rollers, so brake earlier
    static final double TURN_GAIN = 0.02;            // turn power per degree away
    static final double MIN_TURN_POWER = 0.12;       // slowest turn that still moves the robot (less and it sits 3 deg off forever). Was 0.05 with encoders
    static final double MAX_TURN_POWER = 0.2;
    static final double POSITION_TOLERANCE_IN = 1;   // "close enough" to a waypoint
    static final double HEADING_TOLERANCE_DEG = 2.0;
    static final double SETTLED_SPEED_IN_PER_S = 2.0; // "stopped" = slower than this. A waypoint counts only once the robot has stopped on it
    static final double WAYPOINT_TIMEOUT_S = 4.0;    // give up on a waypoint after this long, PLUS...
    static final double WAYPOINT_TIMEOUT_PER_INCH_S = 0.15; // ...this much per inch of the move (100 in: 4 + 15 = 19 s). Long drives need longer
    static final double WAYPOINT_PAUSE_S = 2.0;      // stop and sit still this long after each waypoint (settle, read the position). 5.0 while measuring 11/10
    static final double STOP_BY_S = 28.0;            // stop everything by this time (AUTO is 30 s)
    static final int MAX_DELAY_S = 10;               // start delay the drive team can choose in INIT
    static final boolean RECALIBRATE_AT_START = true; // re-calibrate the Pinpoint gyro at START, when the robot is surely still (see Odometry.recalibrateImu)
    static final double RECALIBRATE_MIN_S = 0.5;     // the gyro calibration takes about this long; don't trust READY before then
    static final double PINPOINT_READY_TIMEOUT_S = 3.0; // after START, wait this long for the Pinpoint to be READY, else don't drive

    // What the lights mean in Basic Auton. Colors are in Light.Color.
    static final Light.Color RED_ALLIANCE_COLOR = Light.Color.RED;   // during INIT
    static final Light.Color BLUE_ALLIANCE_COLOR = Light.Color.BLUE; // during INIT
    static final Light.Color PARKED_COLOR = Light.Color.GREEN;       // reached every waypoint
    static final Light.Color STOPPED_EARLY_COLOR = Light.Color.YELLOW; // a waypoint timed out, or out of time

    @Override
    public void runOpMode() {
        Alliance alliance = alliance();
        Route route = route();

        robot = new Robot(hardwareMap);
        Odometry odometry = new Odometry(hardwareMap);   // resets the Pinpoint: robot must be STILL in INIT
        Light leftLight = new Light(hardwareMap, Light.LEFT_LIGHT_NAME);
        Light rightLight = new Light(hardwareMap, Light.RIGHT_LIGHT_NAME);
        Power power = new Power(hardwareMap);

        robot.drivetrain.setBrakeOnStop(true);           // autonomous stops precisely (decision 023)
        robot.drivetrain.setEncoderSpeedControl(USE_ENCODER_SPEED_CONTROL);

        Pose2D start = startPose();
        Pose2D[] path = path();

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
            telemetry.addLine("SET UP: " + setupInstructions());
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

        // The robot is surely sitting still right now (just placed, hands off), so this is the best moment to
        // calibrate the Pinpoint's gyro. INIT's calibration is ruined if anyone nudges the robot while it runs,
        // and then the heading drifts for the whole run (the robot slowly turns, and sideways moves go crooked).
        if (RECALIBRATE_AT_START) {
            odometry.recalibrateImu();
        }
        // Wait for the Pinpoint to be READY before telling it where we are. If it is still calibrating,
        // setPose could be ignored or overwritten, and the robot would drive from a wrong position.
        // If it never gets READY, we do NOT drive: a robot that sits still is safer than one that drives blind.
        while (opModeIsActive() && autoTimer.seconds() < PINPOINT_READY_TIMEOUT_S) {
            odometry.update();
            boolean calibrationHadTime = !RECALIBRATE_AT_START || autoTimer.seconds() >= RECALIBRATE_MIN_S;
            if (odometry.isReady() && calibrationHadTime) {
                break;
            }
            showStatus("Waiting for Pinpoint (" + odometry.getStatus() + ")", autoTimer, odometry, null);
        }
        if (!opModeIsActive()) {
            return;
        }
        odometry.update();
        if (!odometry.isReady()) {
            leftLight.setColor(STOPPED_EARLY_COLOR);
            rightLight.setColor(STOPPED_EARLY_COLOR);
            while (opModeIsActive()) {
                odometry.update();
                showStatus("NOT DRIVING: Pinpoint not READY (" + odometry.getStatus() + ")", autoTimer, odometry, null);
            }
            robot.stop();
            return;
        }
        odometry.setPose(start);   // the Pinpoint now reports FIELD positions (Main Auton: positions from its 0, 0, 0 start)

        // Start delay, for when our partner's route crosses ours
        while (opModeIsActive() && autoTimer.seconds() < delaySeconds) {
            odometry.update();
            showStatus("Waiting (start delay)", autoTimer, odometry, null);
        }

        // Drive to each waypoint in order
        boolean allReached = true;
        for (int i = 0; i < path.length && opModeIsActive(); i++) {
            Pose2D target = path[i];
            boolean reached = driveTo(robot, odometry, target, autoTimer, "Waypoint " + (i + 1) + " of " + path.length);
            allReached &= reached;
            // Pause: let the robot settle (BRAKE) and show where the Pinpoint thinks it is, before the next move
            ElapsedTime pauseTimer = new ElapsedTime();
            while (opModeIsActive() && pauseTimer.seconds() < WAYPOINT_PAUSE_S && autoTimer.seconds() < STOP_BY_S) {
                odometry.update();
                showStatus("Paused after waypoint " + (i + 1) + (reached ? "" : " (NOT reached)"), autoTimer, odometry, target);
            }
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
     * Drive to one position (proportional control), then stop on it.
     * Returns true if we got there and stopped, false if we ran out of time.
     */
    private boolean driveTo(Robot robot, Odometry odometry, Pose2D target, ElapsedTime autoTimer, String label) {
        ElapsedTime waypointTimer = new ElapsedTime();
        odometry.update();
        // A long move gets a longer timeout: 100 in at a careful speed takes well over 8 s
        double timeoutSeconds = WAYPOINT_TIMEOUT_S + WAYPOINT_TIMEOUT_PER_INCH_S * distanceBetween(odometry.getPose(), target);

        while (opModeIsActive()) {
            odometry.update();
            Pose2D pose = odometry.getPose();

            // How far, in FIELD directions (the Pinpoint's X and Y)
            double dx = target.getX(DistanceUnit.INCH) - pose.getX(DistanceUnit.INCH);
            double dy = target.getY(DistanceUnit.INCH) - pose.getY(DistanceUnit.INCH);
            double distance = Math.hypot(dx, dy);
            double headingError = angleWrap(target.getHeading(AngleUnit.DEGREES) - pose.getHeading(AngleUnit.DEGREES));
            double speed = odometry.getSpeedInchesPerSecond();

            // Done = close enough AND stopped. "Close but still sliding" is not done: wait for the brake to finish,
            // then check again. If it slid out of tolerance, the loop below nudges it back.
            boolean closeEnough = distance < POSITION_TOLERANCE_IN && Math.abs(headingError) < HEADING_TOLERANCE_DEG;
            if (closeEnough && speed < SETTLED_SPEED_IN_PER_S) {
                robot.drivetrain.stop();
                return true;
            }
            if (waypointTimer.seconds() > timeoutSeconds || autoTimer.seconds() > STOP_BY_S) {
                robot.drivetrain.stop();
                return false;
            }

            // Turn the FIELD direction into ROBOT directions (forward / left), using the robot's heading.
            // (NEAR faces -90, so a field -Y move comes out as "forward" here. This is where the start heading matters.)
            double heading = pose.getHeading(AngleUnit.RADIANS);
            double forward = dx * Math.cos(heading) + dy * Math.sin(heading);
            double left = -dx * Math.sin(heading) + dy * Math.cos(heading);

            // How sideways is this move? 0 = straight forward/back, 1 = pure strafe. Sideways moves brake worse
            // (the mecanum rollers roll), so they get a longer brake lookahead and a lower top power.
            double sideways = distance > 0 ? Math.abs(left) / distance : 0.0;
            double lookahead = BRAKE_LOOKAHEAD_S + (BRAKE_LOOKAHEAD_SIDEWAYS_S - BRAKE_LOOKAHEAD_S) * sideways;
            double maxPower = MAX_DRIVE_POWER + (MAX_SIDEWAYS_POWER - MAX_DRIVE_POWER) * sideways;

            // Brake early: how fast is the robot closing in on the waypoint? (Its velocity, in the same field
            // directions as dx and dy, measured along the line to the waypoint. Positive = getting closer, negative = moving
            // away, for example after sliding past it.) Steer on where the robot will be in `lookahead` seconds,
            // so the power drops to 0 just before the waypoint and the slide ends on it instead of past it.
            // Moving AWAY makes the predicted distance bigger, so the robot pushes back harder: that is correct.
            double closingSpeed = distance > 0
                    ? (odometry.getVelocityXInchesPerSecond() * dx + odometry.getVelocityYInchesPerSecond() * dy) / distance
                    : 0.0;
            double predictedDistance = distance - closingSpeed * lookahead;

            // Faster when far, slower when close, nothing once the predicted spot is inside the tolerance
            double power = 0.0;
            if (predictedDistance >= POSITION_TOLERANCE_IN) {
                power = Math.max(MIN_DRIVE_POWER, Math.min(maxPower, DRIVE_GAIN * predictedDistance));
            }
            double scale = distance > 0 ? power / distance : 0.0;

            // Turn only if the heading is really off, and then hard enough to actually move (MIN_TURN_POWER).
            // Without the minimum, a 3 degree error gives 0.06 power, the robot doesn't budge, and the waypoint never
            // counts as reached.
            double turn = 0.0;
            if (Math.abs(headingError) >= HEADING_TOLERANCE_DEG) {
                double turnPower = Math.max(MIN_TURN_POWER, Math.min(MAX_TURN_POWER, TURN_GAIN * Math.abs(headingError)));
                turn = Math.copySign(turnPower, headingError);
            }

            robot.drivetrain.drive(forward * scale, left * scale, turn);
            showStatus(label + String.format(": %.1f in to go, %.1f in/s, power %.2f", distance, closingSpeed, power),
                    autoTimer, odometry, target);
        }
        robot.drivetrain.stop();
        return false;
    }

    private void showStatus(String step, ElapsedTime autoTimer, Odometry odometry, Pose2D target) {
        telemetry.addData("BASIC AUTON", "%s  %s", alliance(), route());
        telemetry.addData("Step", step);
        telemetry.addData("Time", "%.1f s", autoTimer.seconds());
        telemetry.addData("Loop", "%.0f ms  (how often the position is refreshed)", loopTimer.milliseconds());
        loopTimer.reset();
        telemetry.addData("Robot", poseText(odometry.getPose()));
        if (target != null) {
            telemetry.addData("Target", poseText(target));
        }
        // The power each of the 4 wheels is getting right now (-1 to 1), plus the drive mode and BRAKE / FLOAT.
        // Driving straight forward: all 4 the same. Strafing: front-left and back-right the opposite of the other two.
        robot.drivetrain.addTelemetry(telemetry);
        telemetry.update();
    }

    /** How to place the robot, in plain words for the drive team (shown during INIT). Always BACK to a wall (046). */
    static String setupText(Route route, Alliance alliance) {
        boolean red = alliance == Alliance.RED;
        if (route == Route.NEAR) {
            return red ? "tile A6, BACK touching the RED wall, FRONT facing into the field"
                       : "tile F1, BACK touching the BLUE wall, FRONT facing into the field";
        }
        return red ? "tile B1, BACK touching the AUDIENCE wall, FRONT facing the FAR wall"
                   : "tile E6, BACK touching the FAR wall, FRONT facing the AUDIENCE";
    }

    /** A position, in inches and degrees (a RED field position, or a Main Auton position from its 0, 0, 0 start). */
    protected static Pose2D pose(double xInches, double yInches, double headingDegrees) {
        return new Pose2D(DistanceUnit.INCH, xInches, yInches, AngleUnit.DEGREES, headingDegrees);
    }

    /** RED positions are used as-is. BLUE = RED rotated 180 degrees: (x, y, h) -> (-x, -y, h + 180). */
    static Pose2D forAlliance(Pose2D redPose, Alliance alliance) {
        if (alliance == Alliance.RED) {
            return redPose;
        }
        return pose(-redPose.getX(DistanceUnit.INCH), -redPose.getY(DistanceUnit.INCH),
                angleWrap(redPose.getHeading(AngleUnit.DEGREES) + 180));
    }

    /** Straight-line distance between two positions, in inches. */
    static double distanceBetween(Pose2D a, Pose2D b) {
        return Math.hypot(b.getX(DistanceUnit.INCH) - a.getX(DistanceUnit.INCH),
                b.getY(DistanceUnit.INCH) - a.getY(DistanceUnit.INCH));
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
