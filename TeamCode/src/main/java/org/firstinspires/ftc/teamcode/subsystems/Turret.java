package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Turret subsystem: turns the launcher (flywheel) and the camera left and right, like a tank turret
 * (issue #31, decisions 016, 017, 019, 021, 033, 042).
 *
 * Hardware:
 *   turretRotateServo          CONTINUOUS ROTATION servo (decision 021). We tell it a SPEED (-1 to +1), not a position.
 *                        goBILDA 2000 Series Dual Mode Servo, 25-3 Speed (2000-0025-0003), set to CONTINUOUS
 *                        mode with the goBILDA Servo Programmer.
 *                        Driver Station config type: "Continuous Rotation Servo".
 *   turretEncoder        ELC Encoder V2, "D" (quadrature) connector, on an Expansion Hub MOTOR ENCODER port
 *                        (decision 019). This is how the turret knows where it is.
 *   turretHomeSwitch     REV Magnetic Limit Switch: our HOME sensor (decisions 017, 033). It is NOT a stop.
 *                        The switch triggers when the turret faces 90 degrees to the LEFT of robot forward
 *                        (HOME_ANGLE_DEGREES = 90, decision 034). Was called turretForwardSwitch.
 *   turretHoodServo      POSITIONAL servo: tilts the hood to aim the launch UP or DOWN (decision 024).
 *                        Optional: if it's not in the config, the turret still works and the hood shows OFFLINE.
 * Turning needs ALL THREE of turretRotateServo, turretEncoder and turretHomeSwitch. If any is missing from
 * the config, the turret shows OFFLINE and does nothing (no crash), so Main TeleOp still runs on any robot.
 *
 * How it works:
 *   - A continuous servo doesn't know its own angle, so OUR CODE holds the angle using the encoder.
 *     Call update() EVERY loop. It compares where the turret IS (encoder) with where we WANT it (target),
 *     and spins the servo faster when far away and slower when close ("proportional control").
 *   - HOMING (decision 042): the encoder only counts MOVEMENT (0 at power-on), so we find "home" with the switch.
 *     The switch is ON over a ZONE several degrees wide, not at one exact spot, so we use the MIDDLE of the zone:
 *       1. FIND:      turn toward the switch until it turns ON
 *       2. BACK OFF:  turn back the way we came until it's OFF again (plus a little extra)
 *       3. PASS 1:    turn slowly all the way THROUGH the zone. The middle of the two edges = middle #1
 *       4. PASS 2:    turn slowly back through it the other way = middle #2
 *       5. HOME = the average of middle #1 and #2. Coming from the left reads a little different from coming
 *          from the right (the switch reacts late, and turns ON and OFF at slightly different spots), and
 *          averaging both ways cancels that out.
 *     Which way to turn in step 1? If we already know where home is, MATH decides (the switch is at
 *     HOME_ANGLE_DEGREES). If we don't (a fresh power-on), we turn LEFT: the switch is 90 degrees left of forward,
 *     so the turret MUST START ROUGHLY FACING FORWARD (drive team: set it before the match).
 *     Every move is capped by distance (HOME_SEARCH_MAX_DEGREES), time, and stops if the turret stalls.
 *     findHome() runs it. goToRestAngle() runs it first if the turret isn't homed yet.
 *   - REMEMBERED: the encoder keeps counting between OpModes while the robot stays powered, so once homed the
 *     turret stays homed (Auto -> TeleOp, test OpModes...). Power the robot off, or restart the app, and it
 *     homes again. Any time the turret passes through the switch zone, the code checks home is still right.
 *   - Until it's homed, turnTo() does nothing and manual moves are SLOW. After homing, nothing moves past
 *     MAX_LEFT / MAX_RIGHT. Nothing physical stops the turret, so these limits matter!
 *
 * Angles use our direction convention (decision 007), measured from the ROBOT's front:
 *   0 degrees = turret pointing straight FORWARD.   + = LEFT (counter-clockwise),  - = RIGHT.   180 = REAR.
 *   Angles keep counting past 180 (they don't wrap around), so turning left from the rear goes 180, 200, 270...
 *   turnTo() picks whichever way of writing the angle is inside the limits: turnTo(-90) can mean 270.
 *
 * Calibrate the TODO constants with the "Turret Test" OpMode, IN ORDER (issue #31):
 *   1. SERVO_REVERSED   2. ENCODER_REVERSED   3. ENCODER_REVS_PER_TURRET_REV   4. limits   5. AIM_GAIN
 */
public class Turret {

    // Config names (decision 009). Must match the Driver Station robot configuration.
    public static final String ROTATE_SERVO_NAME = "turretRotateServo";
    public static final String HOOD_SERVO_NAME = "turretHoodServo";
    public static final String ENCODER_NAME = "turretEncoder";
    public static final String HOME_SWITCH_NAME = "turretHomeSwitch";

    // The servo's signal range in microseconds, in CONTINUOUS mode. goBILDA 2000 series: 1000-2000.
    // 1500 = stop, 1000 = full speed one way, 2000 = full speed the other way. With this range,
    // a speed of 1.0 really is full speed. CHECK THIS IF WE CHANGE SERVOS (Axon MAX used 1050-1950).
    public static final double SERVO_PWM_MIN_US = 1000;
    public static final double SERVO_PWM_MAX_US = 2000;

    // TODO(#31) step 1: set to true if a POSITIVE speed turns the turret RIGHT (it should turn LEFT).
    public static final boolean SERVO_REVERSED = false;

    // ELC Encoder V2 data sheet: 4000 counts per revolution.
    public static final double ENCODER_COUNTS_PER_REV = 4000.0;
    // TODO(#31) step 2: set to true if "Turret angle" goes DOWN when the turret turns LEFT.
    public static final boolean ENCODER_REVERSED = false;
    // TODO(#31) step 3: encoder turns per ONE turret turn (the gear ratio between the servo and the turret).
    // The encoder is on the servo. Example: servo gear 20 teeth, turret gear 100 teeth = 100 / 20 = 5.0
    // 11/7: servo gear 18 teeth, turret gear about 84-85 teeth: 84.5 / 18 = 4.69 (counted by hand, so approximate).
    public static final double ENCODER_REVS_PER_TURRET_REV = 4.69;

    // Where the MIDDLE of the home switch zone is (decision 042): 90 degrees to the LEFT of robot forward.
    // Home only tells us what angle we're at. If the turret is remounted, change only this number.
    // TODO(#31): re-tune with the new homing. Press A in Turret Test: if the turret stops a little RIGHT of dead
    // forward, make this number SMALLER by that many degrees; a little LEFT, make it BIGGER.
    // (History: with the old homing, which used the zone's first EDGE instead of its middle, it was 86.2.)
    public static final double HOME_ANGLE_DEGREES = 90.0;

    // Turn limits (decision 042), measured from FORWARD (0): 135 degrees each way. The switch (90 left) is inside,
    // with 45 degrees to spare on the left. Limits are software only and protect the wiring. The turret coasts a
    // little after we stop the servo, so it can go a few degrees past.
    public static final double MAX_LEFT_DEGREES = 135.0;
    public static final double MAX_RIGHT_DEGREES = -135.0;

    // TODO(#31) step 5: tune the aiming. Speed = AIM_GAIN x (degrees away from the target).
    // Example: 0.02 means 10 degrees away = speed 0.2. Bigger = faster but may wobble past the target.
    public static final double AIM_GAIN = 0.02;
    public static final double MAX_SPEED = 0.6;            // fastest the turret is allowed to turn (0 to 1)
    public static final double TOLERANCE_DEGREES = 1.0;    // "close enough" to the target: stop here
    public static final double NOT_HOMED_MAX_SPEED = 0.35; // slower, for moving before the turret is homed
                                                           // (0.15 was too slow to move the Axon turret, 10/15)

    // goToRestAngle() turns here (decisions 039, 042). With the new homing this should be dead forward = 0.
    // (It was 10.5 to make up for the old homing. To fix "not quite forward", tune HOME_ANGLE_DEGREES instead.)
    public static final double REST_ANGLE_DEGREES = 0.0;

    // HOMING (decision 042), see "How it works" above. TODO(#31): check these on the robot with Turret Test.
    public static final double HOME_SEARCH_SPEED = 0.35;       // step 1, looking for the zone (0 to 1)
    public static final double HOME_PASS_SPEED = 0.25;         // steps 2-4, slow, so the edges are read precisely.
                                                               // Too slow and the turret won't move (it stalls)
    public static final double HOME_PASS_EXTRA_DEGREES = 8.0;  // how far past each edge to go before turning back
    public static final double HOME_SEARCH_MAX_DEGREES = 135.0; // step 1 never turns farther than this from where
                                                               // it started: from forward it reaches the switch
                                                               // (90 left) even if it starts up to 45 right of forward
    public static final double HOME_PASS_MAX_DEGREES = 60.0;   // steps 2-4 never turn farther than this each
    public static final double HOME_SEARCH_TIMEOUT_S = 12.0;   // give up on the whole thing after this long
    public static final double STALL_CHECK_S = 0.3;            // every this many seconds while homing...
    public static final int STALL_MIN_COUNTS = 30;             // ...the encoder must move at least this much, or we stop
    // If the turret passes through the switch zone but the middle is this far from where the code thinks home is,
    // the remembered home was wrong (e.g. the Expansion Hub lost power and its count reset): use the new middle.
    public static final double HOME_CHECK_DEGREES = 10.0;

    // Safety: if the turret ends up this much FARTHER from the target than when it started,
    // something is backwards (step 1 or 2), so we stop instead of spinning away.
    public static final double RUNAWAY_DEGREES = 20.0;

    // Hood (positional servo, 0.0 to 1.0). TODO(#31): find these with the hood controls in "Turret Test".
    // Keep the hood between these so it can't press into its hard stops.
    public static final double HOOD_LOWEST_POSITION = 0.0;   // flattest launch
    public static final double HOOD_HIGHEST_POSITION = 1.0;  // steepest launch
    public static final double HOOD_START_POSITION = 0.5;    // a safe middle spot (Turret Test starts here after START)

    // TODO(#31): check with "Turret Test". REV's switch reads false when a magnet is near (active low).
    public static final boolean HOME_SWITCH_TRIGGERED_STATE = false;

    // "At home" zone: once homed, the turret counts as HOME (light GREEN) when the encoder is within
    // this many counts of the home spot (the middle of the switch zone).
    // 200 counts = 18 degrees of ENCODER turn (4000 counts = 360). With gears, less at the turret.
    // TODO(#31): tune. Smaller = stricter "home".
    public static final int HOME_TOLERANCE_COUNTS = 200;

    private final CRServoImplEx turretRotateServo;  // null if not in the Driver Station config
    // Only used to READ the encoder port. We never give it power.
    private final DcMotorEx turretEncoder;           // null if not in the Driver Station config
    private final DigitalChannel turretHomeSwitch;    // null if not in the Driver Station config
    private final boolean connected;                 // true only if all three are in the config
    private final Servo turretHoodServo;   // null if not in the Driver Station config
    private double hoodPosition = Double.NaN; // NaN until the first hood command

    // HOME is remembered in "static" variables, which belong to the class, not to one Turret object. They live as long
    // as the robot app runs, so the next OpMode's Turret still knows where home is (decision 042).
    // Counts here are "left counts": the raw encoder count, flipped if ENCODER_REVERSED, so bigger = more LEFT.
    private static boolean homed = false;
    private static double homeLeftCounts = 0;     // left counts at the middle of the switch zone (HOME_ANGLE_DEGREES)
    private static String homeQuality = "not homed";

    /** The steps of findHome(), in order. See "How it works" at the top. */
    private enum HomeStep { OFF, FIND, BACK_OFF, PASS_1, PASS_2 }
    private HomeStep homeStep = HomeStep.OFF;
    private int findDirection = +1;               // +1 = step 1 turns LEFT toward the switch, -1 = RIGHT
    private boolean restAfterHoming = false;      // goToRestAngle() asked for homing first
    private double stepStartLeftCounts = 0;       // where the current step started (to cap how far it turns)
    private double edgeLeftCounts = Double.NaN;   // where the step's key edge was seen (to go a little past it)
    private double pass1Middle = Double.NaN;
    private final ElapsedTime stallTimer = new ElapsedTime();
    private double stallLeftCounts = 0;
    private String homeFailReason = "";

    // Switch EDGES, watched every update(), so we can find the zone's middle whenever the turret passes through it.
    private boolean wasAtSwitch = false;
    private double lastLeftCounts = 0;
    private double zoneEnterLeftCounts = Double.NaN; // where the switch turned ON (NaN = no clean entry seen)
    private int zoneEnterDirection = 0;              // which way the turret was moving then: +1 left, -1 right
    private double lastZoneWidthDegrees = Double.NaN;
    private double lastPassDifferenceDegrees = Double.NaN; // middle #1 vs middle #2: how much direction matters
    private String homeCheckNote = "";

    // What we've been asked to do. A TARGET angle (turnTo) or a MANUAL speed (setManualSpeed).
    private double targetDegrees = Double.NaN;  // NaN = no target
    private double manualSpeed = 0.0;
    private double startErrorDegrees = 0.0;
    private boolean runawayStopped = false;
    private boolean homeSearchFailed = false;   // true if the last findHome() gave up
    private final ElapsedTime homeTimer = new ElapsedTime();
    private boolean commanded = false;          // false until the first command: never move during INIT
    private double speedSent = 0.0;

    public Turret(HardwareMap hardwareMap) {
        turretRotateServo = hardwareMap.tryGet(CRServoImplEx.class, ROTATE_SERVO_NAME);
        turretEncoder = hardwareMap.tryGet(DcMotorEx.class, ENCODER_NAME);
        turretHomeSwitch = hardwareMap.tryGet(DigitalChannel.class, HOME_SWITCH_NAME);
        turretHoodServo = hardwareMap.tryGet(Servo.class, HOOD_SERVO_NAME);
        connected = turretRotateServo != null && turretEncoder != null && turretHomeSwitch != null;

        if (turretHomeSwitch != null) {
            turretHomeSwitch.setMode(DigitalChannel.Mode.INPUT);
        }
        if (turretRotateServo != null) {
            turretRotateServo.setDirection(SERVO_REVERSED ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
            // Only sets the signal range. Nothing moves until the first command (game rule G304.H: INIT must not move).
            turretRotateServo.setPwmRange(new PwmControl.PwmRange(SERVO_PWM_MIN_US, SERVO_PWM_MAX_US));
        }
        if (connected) {
            lastLeftCounts = leftCounts();
            wasAtSwitch = isAtHomeSwitch();
            // A remembered home from an earlier OpMode: if it says we're far outside the limits, it can't be right
            // (the Expansion Hub probably lost power and reset its count), so forget it and home again.
            if (homed && (getAngle() > MAX_LEFT_DEGREES + 30.0 || getAngle() < MAX_RIGHT_DEGREES - 30.0)) {
                forgetHome();
            }
        }
    }

    /** True if the rotate servo, encoder and home switch are all in the Driver Station config. */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Call this ONCE EVERY LOOP, including during INIT.
     * It homes the encoder at the home switch, and spins the servo toward the target.
     */
    public void update() {
        if (!connected) {
            return; // OFFLINE: nothing to do
        }
        watchSwitchEdges(); // reading sensors is fine during INIT: nothing moves

        if (!commanded) {
            return; // nothing asked yet: send nothing, so the turret can't move during INIT
        }

        double speed;
        if (homeStep != HomeStep.OFF) {
            speed = speedToHome();
        } else if (!Double.isNaN(targetDegrees)) {
            speed = speedToTarget();
        } else {
            speed = manualSpeed;
            if (!homed) {
                speed = clamp(speed, NOT_HOMED_MAX_SPEED);
            }
        }

        // Never go past the limits (once homed, we know where they are). This applies while homing too.
        if (homed && speed > 0 && getAngle() >= MAX_LEFT_DEGREES) {
            speed = 0.0;
        }
        if (homed && speed < 0 && getAngle() <= MAX_RIGHT_DEGREES) {
            speed = 0.0;
        }

        speedSent = speed;
        turretRotateServo.setPower(speed);
    }

    // =============================================================================================================
    // HOMING (decision 042)
    // =============================================================================================================

    /**
     * Find home precisely: the 5 steps in "How it works" at the top. Call after START, never during INIT.
     * Works whether or not the turret is homed already (homed = it re-checks home). setManualSpeed(), turnTo()
     * and stop() cancel it, so the operator can always take over with the stick.
     */
    public void findHome() {
        startHoming(false);
    }

    private void startHoming(boolean thenRest) {
        if (!connected) {
            return;
        }
        commanded = true;
        targetDegrees = Double.NaN;
        manualSpeed = 0.0;
        homeSearchFailed = false;
        homeFailReason = "";
        restAfterHoming = thenRest;
        pass1Middle = Double.NaN;
        homeTimer.reset();
        // Which way is the switch? Known home: math. Unknown: LEFT (the turret must start roughly forward).
        if (homed) {
            findDirection = getAngle() < HOME_ANGLE_DEGREES ? +1 : -1;
        } else {
            findDirection = +1;
        }
        // Already on the switch? Then we've found it: skip straight to backing off.
        startStep(isAtHomeSwitch() ? HomeStep.BACK_OFF : HomeStep.FIND);
    }

    private void startStep(HomeStep step) {
        homeStep = step;
        stepStartLeftCounts = leftCounts();
        edgeLeftCounts = Double.NaN;
        stallTimer.reset();
        stallLeftCounts = leftCounts();
    }

    /** One loop of homing: the speed to send now. The steps move on when update() sees the right switch edge. */
    private double speedToHome() {
        double counts = leftCounts();
        double movedDegrees = Math.abs(counts - stepStartLeftCounts) / countsPerDegree();

        // Safety first: time, distance, stall.
        if (homeTimer.seconds() > HOME_SEARCH_TIMEOUT_S) {
            return failHoming("took longer than " + HOME_SEARCH_TIMEOUT_S + " s");
        }
        double maxDegrees = homeStep == HomeStep.FIND ? HOME_SEARCH_MAX_DEGREES : HOME_PASS_MAX_DEGREES;
        if (movedDegrees > maxDegrees) {
            return failHoming(homeStep + " turned " + Math.round(movedDegrees) + "° without finding the switch edge");
        }
        if (stallTimer.seconds() > STALL_CHECK_S) {
            if (Math.abs(counts - stallLeftCounts) < STALL_MIN_COUNTS) {
                return failHoming("the turret stopped moving (blocked, at a limit, or HOME_PASS_SPEED too slow)");
            }
            stallTimer.reset();
            stallLeftCounts = counts;
        }

        // Each step: which way to turn, and when it's done.
        int direction;
        switch (homeStep) {
            case FIND:      // toward the switch until it turns ON (watchSwitchEdges notices and starts BACK_OFF)
                return findDirection * HOME_SEARCH_SPEED;
            case BACK_OFF:  // back the way we came, until it's OFF, plus a little extra
                direction = -findDirection;
                break;
            case PASS_1:    // through the zone, toward the switch side we came from
                direction = findDirection;
                break;
            default:        // PASS_2: back through the zone
                direction = -findDirection;
                break;
        }
        // Done with this step once the key edge was seen AND we've gone a little past it.
        if (!Double.isNaN(edgeLeftCounts)
                && Math.abs(counts - edgeLeftCounts) / countsPerDegree() >= HOME_PASS_EXTRA_DEGREES) {
            nextStep();
            return 0.0; // stop for one loop before turning the other way
        }
        return direction * HOME_PASS_SPEED;
    }

    private void nextStep() {
        switch (homeStep) {
            case BACK_OFF:
                startStep(HomeStep.PASS_1);
                break;
            case PASS_1:
                startStep(HomeStep.PASS_2);
                break;
            default:        // PASS_2 is finished in onZonePassed()
                break;
        }
    }

    private double failHoming(String reason) {
        homeStep = HomeStep.OFF;
        homeSearchFailed = true;
        homeFailReason = reason;
        restAfterHoming = false;
        manualSpeed = 0.0;
        return 0.0;
    }

    /**
     * Watches the switch every loop for EDGES (OFF -> ON = entering the zone, ON -> OFF = leaving it). When the turret
     * passes all the way THROUGH the zone in one direction, the middle of the two edges is a "zone middle".
     */
    private void watchSwitchEdges() {
        double counts = leftCounts();
        int moving = counts > lastLeftCounts ? +1 : counts < lastLeftCounts ? -1 : 0;
        boolean atSwitch = isAtHomeSwitch();

        if (atSwitch && !wasAtSwitch) {             // entered the zone
            zoneEnterLeftCounts = counts;
            zoneEnterDirection = moving;
            if (homeStep == HomeStep.FIND) {
                startStep(HomeStep.BACK_OFF);       // found it! Now back off and do the slow passes
            }
        } else if (!atSwitch && wasAtSwitch) {      // left the zone
            boolean cleanPass = !Double.isNaN(zoneEnterLeftCounts) && zoneEnterDirection != 0
                    && moving == zoneEnterDirection && Math.signum(counts - zoneEnterLeftCounts) == moving;
            if (cleanPass) {
                onZonePassed((zoneEnterLeftCounts + counts) / 2.0, moving,
                        Math.abs(counts - zoneEnterLeftCounts) / countsPerDegree());
            }
            if (homeStep == HomeStep.BACK_OFF || homeStep == HomeStep.PASS_1) {
                edgeLeftCounts = counts;            // go a little past this edge, then the next step
            }
            zoneEnterLeftCounts = Double.NaN;
        }
        wasAtSwitch = atSwitch;
        lastLeftCounts = counts;
    }

    /** The turret just passed all the way through the switch zone. */
    private void onZonePassed(double middle, int direction, double widthDegrees) {
        lastZoneWidthDegrees = widthDegrees;

        if (homeStep == HomeStep.PASS_1) {
            pass1Middle = middle;
        } else if (homeStep == HomeStep.PASS_2 && !Double.isNaN(pass1Middle)) {
            // Both ways done: home = the average of the two middles. Finished!
            lastPassDifferenceDegrees = Math.abs(middle - pass1Middle) / countsPerDegree();
            setHome((pass1Middle + middle) / 2.0, "precise (both ways)");
            homeStep = HomeStep.OFF;
            if (restAfterHoming) {
                restAfterHoming = false;
                turnTo(REST_ANGLE_DEGREES);
            } else {
                manualSpeed = 0.0;
            }
            return;
        }

        // Any other pass through the zone (e.g. turning by hand with the stick):
        if (!homed) {
            setHome(middle, "rough (one pass)");    // good enough to start; findHome() makes it precise
        } else if (homeStep == HomeStep.OFF) {
            double offBy = (middle - homeLeftCounts) / countsPerDegree();
            if (Math.abs(offBy) > HOME_CHECK_DEGREES) {
                setHome(middle, "corrected (one pass)");
                homeCheckNote = String.format("home was off by %.1f°: corrected", offBy);
            } else {
                homeCheckNote = String.format("home checked: %.1f° from this pass ✅", offBy);
            }
        }
    }

    private static void setHome(double leftCountsAtMiddle, String quality) {
        homeLeftCounts = leftCountsAtMiddle;
        homeQuality = quality;
        homed = true;
    }

    /** Forget home (e.g. it looks wrong). The turret must be homed again before turnTo() works. */
    public void forgetHome() {
        homed = false;
        homeQuality = "not homed";
    }

    /** Encoder counts per degree of TURRET turn (about 52 with our gears). */
    private static double countsPerDegree() {
        return ENCODER_COUNTS_PER_REV * ENCODER_REVS_PER_TURRET_REV / 360.0;
    }

    /** The raw encoder count, flipped if ENCODER_REVERSED, so that bigger always = more to the LEFT. */
    private double leftCounts() {
        int raw = getEncoderCounts();
        return ENCODER_REVERSED ? -raw : raw;
    }

    /** Proportional control: faster when far from the target, slower when close, stop when close enough. */
    private double speedToTarget() {
        double error = targetDegrees - getAngle(); // + = target is to the LEFT
        if (Math.abs(error) > Math.abs(startErrorDegrees) + RUNAWAY_DEGREES) {
            runawayStopped = true;
            targetDegrees = Double.NaN;
            manualSpeed = 0.0;
            return 0.0;
        }
        if (Math.abs(error) < TOLERANCE_DEGREES) {
            return 0.0;
        }
        return clamp(AIM_GAIN * error, MAX_SPEED);
    }

    /**
     * Turn the turret to an angle (0 = forward, + = left, 180 = rear) and hold it there. Only works once homed.
     * The same direction can be written two ways (-90 = 270), so we use the one inside the limits.
     * If it's outside the limits both ways, the turret goes to whichever limit points closest to it.
     */
    public void turnTo(double degrees) {
        commanded = true;
        homeStep = HomeStep.OFF;
        if (!homed) {
            return;
        }
        while (degrees < MAX_RIGHT_DEGREES && degrees + 360.0 <= MAX_LEFT_DEGREES) {
            degrees += 360.0;
        }
        while (degrees > MAX_LEFT_DEGREES && degrees - 360.0 >= MAX_RIGHT_DEGREES) {
            degrees -= 360.0;
        }
        if (degrees < MAX_RIGHT_DEGREES || degrees > MAX_LEFT_DEGREES) {
            // Still outside: can't get there. Stop at the limit that points closest to it.
            boolean leftIsCloser = degreesApart(degrees, MAX_LEFT_DEGREES) < degreesApart(degrees, MAX_RIGHT_DEGREES);
            degrees = leftIsCloser ? MAX_LEFT_DEGREES : MAX_RIGHT_DEGREES;
        }
        targetDegrees = degrees;
        startErrorDegrees = targetDegrees - getAngle();
        runawayStopped = false;
    }

    /** Point the turret straight forward (as far as the limits allow). */
    public void turnForward() {
        turnTo(0.0);
    }

    /**
     * Turn at a speed from -1 (right) to +1 (left), for driver control. 0 = stop.
     * This cancels any turnTo() target. Before homing, it is limited to a slow speed.
     */
    public void setManualSpeed(double speed) {
        commanded = true;
        homeStep = HomeStep.OFF;
        targetDegrees = Double.NaN;
        manualSpeed = clamp(speed, 1.0);
    }

    /**
     * Turn the turret to its REST spot (REST_ANGLE_DEGREES, dead forward) from wherever it is.
     * Call after START, never during INIT.
     *   Homed:     turns straight there.
     *   Not homed: first runs findHome() (turns LEFT to find the switch: the turret must start roughly forward),
     *              then turns to the rest spot. Check wasHomeSearchFailed() if it gives up.
     * setManualSpeed(), turnTo() and stop() cancel it, so the operator can always take over with the stick.
     */
    public void goToRestAngle() {
        if (!connected) {
            return;
        }
        homeSearchFailed = false;
        if (homed) {
            turnTo(REST_ANGLE_DEGREES);
            return;
        }
        startHoming(true);
    }

    /** True while the turret is moving by itself: homing, or turning to a target angle. */
    public boolean isAutoMoving() {
        return homeStep != HomeStep.OFF || !Double.isNaN(targetDegrees);
    }

    /** True while findHome() is running. */
    public boolean isFindingHome() {
        return homeStep != HomeStep.OFF;
    }

    /** True if the last findHome() (or goToRestAngle() while not homed) gave up. See the telemetry for why. */
    public boolean wasHomeSearchFailed() {
        return homeSearchFailed;
    }

    /** How wide the switch zone was the last time the turret passed through it, in degrees (NaN = not seen yet). */
    public double getZoneWidthDegrees() {
        return lastZoneWidthDegrees;
    }

    /**
     * Move the hood to a position from HOOD_LOWEST_POSITION (flat) to HOOD_HIGHEST_POSITION (steep).
     * Call after START, never during INIT.
     */
    public void setHoodPosition(double position) {
        hoodPosition = Math.max(HOOD_LOWEST_POSITION, Math.min(HOOD_HIGHEST_POSITION, position));
        if (turretHoodServo != null) {
            turretHoodServo.setPosition(hoodPosition);
        }
    }

    /** The hood position we last sent (NaN if we haven't moved it yet). */
    public double getHoodPosition() {
        return hoodPosition;
    }

    /** True if the hood servo was found in the Driver Station config. */
    public boolean isHoodConnected() {
        return turretHoodServo != null;
    }

    /** Stop turning and forget the target. The hood stays where it is. */
    public void stop() {
        homeStep = HomeStep.OFF;
        targetDegrees = Double.NaN;
        manualSpeed = 0.0;
        speedSent = 0.0;
        if (turretRotateServo != null) {
            turretRotateServo.setPower(0.0);
        }
    }

    /**
     * True when the turret is at HOME (the switch, 90 degrees LEFT): the switch is triggered, or (once homed)
     * the encoder is within HOME_TOLERANCE_COUNTS of the switch spot.
     * The OpMode can show this on the light: light.setColor(GREEN) (decision 018).
     */
    public boolean isAtHome() {
        return isAtHomeSwitch() || (homed && Math.abs(getCountsFromHome()) <= HOME_TOLERANCE_COUNTS);
    }

    /** Encoder counts away from home (+ = left of it). 0 until homed. */
    public int getCountsFromHome() {
        return homed ? (int) Math.round(leftCounts() - homeLeftCounts) : 0;
    }

    /** True once the turret knows where home is (this OpMode, or remembered from an earlier one). */
    public boolean isHomed() {
        return homed;
    }

    /** True when there is a target and the turret is within TOLERANCE_DEGREES of it. */
    public boolean isAtTarget() {
        return !Double.isNaN(targetDegrees) && Math.abs(targetDegrees - getAngle()) < TOLERANCE_DEGREES;
    }

    /** True if the runaway safety stopped the turret. Check SERVO_REVERSED and ENCODER_REVERSED. */
    public boolean wasRunawayStopped() {
        return runawayStopped;
    }

    /** The real turret angle from the encoder, in degrees (0 = forward, + = left, 180 = rear). NaN until homed. */
    public double getAngle() {
        if (!homed) {
            return Double.NaN;
        }
        return HOME_ANGLE_DEGREES + (leftCounts() - homeLeftCounts) / countsPerDegree();
    }

    /** The angle we're turning to (NaN if there's no target). */
    public double getTargetAngle() {
        return targetDegrees;
    }

    /** The speed we last sent to the servo (-1 to +1). */
    public double getSpeed() {
        return speedSent;
    }

    /** Raw encoder count. Used for calibrating. */
    public int getEncoderCounts() {
        return turretEncoder != null ? turretEncoder.getCurrentPosition() : 0;
    }

    /**
     * True when the magnet is at the home switch: the turret is somewhere in the switch ZONE around
     * HOME_ANGLE_DEGREES (90 degrees left). The zone is several degrees wide, which is why homing uses its middle.
     */
    public boolean isAtHomeSwitch() {
        return turretHomeSwitch != null && turretHomeSwitch.getState() == HOME_SWITCH_TRIGGERED_STATE;
    }

    /** How far apart two directions are, the short way around (0 to 180). E.g. -45 and 270 are 45 apart. */
    private static double degreesApart(double a, double b) {
        double difference = Math.abs(a - b) % 360.0;
        return difference > 180.0 ? 360.0 - difference : difference;
    }

    private static double clamp(double value, double limit) {
        return Math.max(-limit, Math.min(limit, value));
    }

    /** Show this subsystem's status on the Driver Station. */
    public void addTelemetry(Telemetry telemetry) {
        if (!connected) {
            telemetry.addData("Turret", "OFFLINE (needs '%s', '%s' and '%s' in DS config)",
                    ROTATE_SERVO_NAME, ENCODER_NAME, HOME_SWITCH_NAME);
            return;
        }
        telemetry.addData("Turret homed", homed ? homeQuality : "NO: home it (X / Turret Test), or turn through the switch zone once");
        if (homeStep != HomeStep.OFF) {
            telemetry.addData("Turret homing", "step %s, turning %s", homeStep,
                    (homeStep == HomeStep.FIND || homeStep == HomeStep.PASS_1) == (findDirection > 0) ? "LEFT" : "RIGHT");
        }
        if (!Double.isNaN(lastZoneWidthDegrees)) {
            telemetry.addData("Switch zone width", "%.1f°  (narrower = better)", lastZoneWidthDegrees);
        }
        if (!Double.isNaN(lastPassDifferenceDegrees)) {
            telemetry.addData("Left vs right pass", "%.1f° apart  (the error averaging removes)", lastPassDifferenceDegrees);
        }
        if (!homeCheckNote.isEmpty()) {
            telemetry.addData("Home check", homeCheckNote);
        }
        telemetry.addData("Turret at home", "%s  (%d counts from home, zone ±%d)",
                isAtHome() ? "YES" : "no", getCountsFromHome(), HOME_TOLERANCE_COUNTS);
        telemetry.addData("Turret angle (°)  +left", homed ? String.format("%.1f", getAngle()) : "unknown");
        telemetry.addData("Turret target (°)", homeStep != HomeStep.OFF ? "homing..."
                : Double.isNaN(targetDegrees) ? "none (manual)" : String.format("%.1f", targetDegrees));
        if (homeSearchFailed) {
            telemetry.addLine("⚠️ HOMING STOPPED: " + homeFailReason + ". Check the switch, and that the turret started roughly forward");
        }
        telemetry.addData("Turret speed sent", "%.2f  (+ = left)", speedSent);
        if (turretHoodServo == null) {
            telemetry.addData("Turret hood", "OFFLINE ('%s' not in DS config)", HOOD_SERVO_NAME);
        } else {
            telemetry.addData("Turret hood", Double.isNaN(hoodPosition) ? "not moved yet" : String.format("%.2f", hoodPosition));
        }
        if (runawayStopped) {
            telemetry.addLine("⚠️ RUNAWAY STOP: turret moved AWAY from the target. Check SERVO_REVERSED / ENCODER_REVERSED");
        }
    }
}
