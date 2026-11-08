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
 * (issue #31, decisions 016, 017, 019, 021, 033, 042-045, 047).
 *
 * Hardware:
 *   turretRotateServo          CONTINUOUS ROTATION servo (decision 021). We tell it a SPEED (-1 to +1), not a position.
 *                        goBILDA 2000 Series Dual Mode Servo, 25-3 Speed (2000-0025-0003), set to CONTINUOUS
 *                        mode with the goBILDA Servo Programmer.
 *                        Driver Station config type: "Continuous Rotation Servo".
 *   turretEncoder        ELC Encoder V2, "D" (quadrature) connector, on an Expansion Hub MOTOR ENCODER port
 *                        (decision 019). This is how the turret knows where it is.
 *   turretHomeSwitch     REV Magnetic Limit Switch: our HOME sensor (decisions 017, 033). It is NOT a stop.
 *                        At the switch (magnet) the turret faces ROBOT FORWARD. It is turret angle 0
 *                        (decisions 047, 048). Was called turretForwardSwitch.
 *   turretHoodServo      POSITIONAL servo: tilts the hood to aim the launch UP or DOWN (decision 024).
 *                        Optional: if it's not in the config, the turret still works and the hood shows OFFLINE.
 * Turning needs ALL THREE of turretRotateServo, turretEncoder and turretHomeSwitch. If any is missing from
 * the config, the turret shows OFFLINE and does nothing (no crash), so Main TeleOp still runs on any robot.
 *
 * How it works:
 *   - A continuous servo doesn't know its own angle, so OUR CODE holds the angle using the encoder.
 *     Call update() EVERY loop. It compares where the turret IS (encoder) with where we WANT it (target),
 *     and spins the servo faster when far away and slower when close ("proportional control").
 *   - HOMING: the encoder counts from 0 at power-on. When the home switch triggers, that spot becomes angle 0
 *     (HOME_ANGLE_DEGREES), then the middle of the magnet once the turret has passed through it (decision 043).
 *     Before a match, point the turret at the magnet (the light turns GREEN in Turret Test) and it homes during INIT.
 *   - Until it's homed, turnTo() does nothing and manual moves are SLOW. After homing, nothing moves past
 *     MAX_LEFT / MAX_RIGHT. Nothing physical stops the turret, so these limits matter!
 *
 * TURRET ANGLES (decision 047), in degrees:
 *   0 = the HOME MAGNET.   + = LEFT (counter-clockwise, decision 007),  - = RIGHT.
 *   At the magnet the turret faces robot FORWARD, so FORWARD_ANGLE_DEGREES = 0 (decision 048).
 *     magnet = forward 0  |  straight left of the robot +90  |  straight right of the robot -90
 *   Thinking "degrees from robot forward" (the camera, field aiming, autonomous)? Use robotToTurret(...) and
 *   turretToRobot(...). Today they change nothing (forward = 0), but if the turret or magnet is ever remounted,
 *   only FORWARD_ANGLE_DEGREES changes and everything that uses them stays right.
 *   Angles don't wrap around. turnTo() picks whichever way of writing an angle is inside the limits.
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
    // 74/18: servo gear 18 teeth, turret gear 74 teeth: 74 / 18 = 4.11 (exact amount, so approximate).
    // 11/7: servo gear 18 teeth, turret gear about 84-85 teeth: 84.5 / 18 = 4.69 (counted by hand, so approximate).
    public static final double ENCODER_REVS_PER_TURRET_REV = 4.11;

    // The angle the code calls the HOME MAGNET (decisions 042, 047): 0. Keep it 0; everything is measured from here.
    public static final double HOME_ANGLE_DEGREES = 0;

    // Where ROBOT FORWARD is, in turret angles (decisions 047, 048). At the magnet the turret faces forward, so 0:
    // there is NO offset. This is the ONE place an offset would live: if the turret or the magnet is ever remounted,
    // change only this number. (11/8: it was -90 by mistake, which aimed X/B and the limits 90 degrees off.)
    public static final double FORWARD_ANGLE_DEGREES = 0.0;

    // Turn limits (decision 047): 135 degrees each way from ROBOT FORWARD, for the wiring.
    //   left:  forward + 135 = 0 + 135 =  135
    //   right: forward - 135 = 0 - 135 = -135
    // Limits are software only, so keep them inside anything the turret could hit. The turret coasts a few degrees
    // past a limit after we stop the servo. TODO(#31): turn slowly to both limits once and check the wiring.
    public static final double MAX_LEFT_DEGREES = 120.0;
    public static final double MAX_RIGHT_DEGREES = -120.0;

    // TODO(#31) step 5: tune the aiming. Speed = AIM_GAIN x (degrees away from the target).
    // Example: 0.02 means 10 degrees away = speed 0.2. Bigger = faster but may wobble past the target.
    public static final double AIM_GAIN = 0.02;
    public static final double MAX_SPEED = 0.6;            // fastest the turret is allowed to turn (0 to 1)
    // Slowest aiming speed. A continuous servo doesn't move at all below some power (its dead zone). Without this,
    // AIM_GAIN x error gets so small near the target (0.02 x 3 degrees = 0.06) that the turret STOPS 3 to 5 degrees short.
    // TODO(#31): tune. Raise it if the turret stops short of the target, lower it if it jumps past (decision 045).
    public static final double MIN_AIM_SPEED = 0.15;
    public static final double TOLERANCE_DEGREES = 1.0;    // "close enough" to the target: stop here
    public static final double NOT_HOMED_MAX_SPEED = 0.35; // slower, for moving before the turret is homed
                                                           // (0.15 was too slow to move the Axon turret, 10/15)

    // goToRestAngle(): the turret's REST spot (decisions 039, 040) is the HOME MAGNET (HOME_ANGLE_DEGREES, decision 042). If the turret is already
    // homed it turns straight there. If it is NOT homed it doesn't know where anything is, so it first turns LEFT at
    // NOT_HOMED_MAX_SPEED to find the home switch (the magnet is at forward, decision 048),
    // then turns to the rest spot. Nothing physical stops the
    // turret, so the search gives up after HOME_SEARCH_TIMEOUT_S. TODO: test the search direction on the robot.
    public static final double HOME_SEARCH_TIMEOUT_S = 10.0;   // 11/8: was 6.0, now the search may have to turn BOTH ways (decision 044)
    public static final double REST_ANGLE_DEGREES = HOME_ANGLE_DEGREES;   // Where goToRestAngle() stops: right on the home magnet (decision 042). Was 10.5 = dead forward (decisions 039, 040)

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
    // this many counts of the home switch spot. The magnet spot alone was too narrow to return to exactly.
    // 200 counts = 18 degrees of ENCODER turn (4000 counts = 360). With gears, less at the turret.
    // TODO(#31): tune. Smaller = stricter "home".
    public static final int HOME_TOLERANCE_COUNTS = 50;

    // HOME SEARCH, BOTH WAYS (decision 044). The search starts by turning LEFT. If the turret stops moving for
    // SEARCH_STALL_S (it hit a hard stop: the encoder count stops changing), it turns around and goes RIGHT.
    // If it hits the other stop too, it gives up (HOME NOT FOUND). SEARCH_STALL_COUNTS = the least the encoder
    // must change to count as "still moving" (52 counts = 1 degree of turret).
    public static final double SEARCH_STALL_S = 0.4;
    public static final int SEARCH_STALL_COUNTS = 15;

    // LAST PART OF A MOVE TO THE REST SPOT: always arrive moving the SAME WAY the turret was moving when it homed.
    // Gears have a little slack (backlash), so the turret lands a degree or two different depending on which way it
    // arrives. If we are on the wrong side, we first go this many degrees PAST the rest spot, then come back to it.
    public static final double APPROACH_OVERSHOOT_DEGREES = 8.0;

    // HOMING ON THE CENTER OF THE MAGNET (decision 043). The switch turns on when the magnet ENTERS its range and off when
    // it LEAVES. The first trigger (the edge) moves 1-2 degrees depending on speed and direction. So when the turret
    // has passed all the way through the magnet's range, we re-home on the MIDDLE: (enter count + leave count) / 2.
    // The turret is homed at the edge first, then corrected once, so nothing waits. Set false for the old edge homing.
    public static final boolean CENTER_HOME_ON_MAGNET = true;
    // A pass through the magnet shorter than this many counts is ignored (the turret turned around inside the range).
    public static final int MIN_MAGNET_ZONE_COUNTS = 20;

    private final CRServoImplEx turretRotateServo;  // null if not in the Driver Station config
    // Only used to READ the encoder port. We never give it power.
    private final DcMotorEx turretEncoder;           // null if not in the Driver Station config
    private final DigitalChannel turretHomeSwitch;    // null if not in the Driver Station config
    private final boolean connected;                 // true only if all three are in the config
    private final Servo turretHoodServo;   // null if not in the Driver Station config
    private double hoodPosition = Double.NaN; // NaN until the first hood command

    // Homing: the encoder count when the turret was at the home switch (HOME_ANGLE_DEGREES).
    private boolean homed = false;
    private int homeCounts = 0;
    private boolean inMagnetZone = false;    // the switch is triggered right now
    private int magnetEnterCounts = 0;       // encoder count when the switch turned on
    private int magnetZoneWidthCounts = 0;   // width of the last full pass through the magnet, for telemetry
    private boolean homeCentered = false;    // true once we re-homed on the middle of the magnet
    private boolean homePassMovedLeft = true; // which way the turret was moving when it passed the magnet (to home)
    private int searchDirection = 1;         // home search: +1 = turning LEFT, -1 = turning RIGHT
    private int searchReversals = 0;         // how many times the search hit a hard stop and turned around
    private final ElapsedTime stallTimer = new ElapsedTime();
    private int stallCheckCounts = 0;
    private double pendingTarget = Double.NaN; // second half of a two-step move (see turnToRest)

    // What we've been asked to do. A TARGET angle (turnTo) or a MANUAL speed (setManualSpeed).
    private double targetDegrees = Double.NaN;  // NaN = no target
    private double manualSpeed = 0.0;
    private double startErrorDegrees = 0.0;
    private boolean runawayStopped = false;
    private boolean targetReached = false;      // true once the turret got to the target, or went past it
    private boolean findingHome = false;        // true while goToRestAngle() is turning left to find the home switch
    private boolean homeSearchFailed = false;   // true if the last goToRestAngle() gave up without finding the switch
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
        boolean atSwitch = isAtHomeSwitch();
        int counts = turretEncoder.getCurrentPosition();
        if (!homed && atSwitch) {
            homeCounts = counts;   // the EDGE of the magnet: good enough to start, refined below
            homed = true;
        }
        if (atSwitch && !inMagnetZone) {
            inMagnetZone = true;
            magnetEnterCounts = counts;
        } else if (!atSwitch && inMagnetZone) {
            // Through the magnet. Home on the middle of the range (only once, so a later sweep can't move it).
            inMagnetZone = false;
            magnetZoneWidthCounts = Math.abs(counts - magnetEnterCounts);
            if (CENTER_HOME_ON_MAGNET && !homeCentered && magnetZoneWidthCounts >= MIN_MAGNET_ZONE_COUNTS) {
                homeCounts = (magnetEnterCounts + counts) / 2;
                homeCentered = true;
                homePassMovedLeft = ((counts - magnetEnterCounts) > 0) != ENCODER_REVERSED;
            }
        }

        if (!commanded) {
            return; // nothing asked yet: send nothing, so the turret can't move during INIT
        }

        // goToRestAngle() was started while not homed, and the switch has now triggered: turn to the rest spot.
        // With CENTER_HOME_ON_MAGNET we keep turning left until we are through the magnet, so we can find its middle.
        boolean throughMagnet = !CENTER_HOME_ON_MAGNET || !inMagnetZone || homeTimer.seconds() > HOME_SEARCH_TIMEOUT_S;
        if (findingHome && homed && throughMagnet) {
            findingHome = false;
            turnToRest();
        }
        // First half of a two-step move is done: do the second half.
        if (!Double.isNaN(pendingTarget) && isAtTarget()) {
            double next = pendingTarget;
            turnTo(next);   // clears pendingTarget
        }

        double speed;
        if (findingHome) {
            speed = speedToFindHome();
        } else if (!Double.isNaN(targetDegrees)) {
            speed = speedToTarget();
        } else {
            speed = manualSpeed;
            if (!homed) {
                speed = clamp(speed, NOT_HOMED_MAX_SPEED);
            }
        }

        // Never go past the limits (once homed, we know where they are).
        if (homed && speed > 0 && getAngle() >= MAX_LEFT_DEGREES) {
            speed = 0.0;
        }
        if (homed && speed < 0 && getAngle() <= MAX_RIGHT_DEGREES) {
            speed = 0.0;
        }

        speedSent = speed;
        turretRotateServo.setPower(speed);
    }

    /** Not homed yet: turn LEFT, slowly, until the home switch triggers (update() notices it). Give up on a timeout. */
    private double speedToFindHome() {
        if (homeTimer.seconds() > HOME_SEARCH_TIMEOUT_S && !homed) {
            findingHome = false;
            homeSearchFailed = true;
            return 0.0;
        }
        if (!homed) {
            // Not moving for a moment = pushing against a hard stop. Turn around. A second stop = give up.
            int counts = getEncoderCounts();
            if (Math.abs(counts - stallCheckCounts) >= SEARCH_STALL_COUNTS) {
                stallCheckCounts = counts;
                stallTimer.reset();
            } else if (stallTimer.seconds() > SEARCH_STALL_S) {
                searchReversals++;
                if (searchReversals >= 2) {
                    findingHome = false;
                    homeSearchFailed = true;
                    return 0.0;
                }
                searchDirection = -searchDirection;
                stallCheckCounts = counts;
                stallTimer.reset();
            }
        }
        return searchDirection * NOT_HOMED_MAX_SPEED;
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
        if (error * startErrorDegrees < 0) {
            targetReached = true;   // the turret went PAST the target (it coasts a little). Stop here, don't chase it back and forth
        }
        if (targetReached || Math.abs(error) < TOLERANCE_DEGREES) {
            return 0.0;
        }
        double speed = AIM_GAIN * error;
        if (Math.abs(speed) < MIN_AIM_SPEED) {
            speed = Math.signum(error) * MIN_AIM_SPEED;   // never slower than the servo can move
        }
        return clamp(speed, MAX_SPEED);
    }

    /**
     * Turn the turret to an angle (0 = the home magnet, + = left, - = right) and hold it there. Only works once homed.
     * The same direction can be written two ways (-90 = 270), so we use the one inside the limits.
     * If it's outside the limits both ways, the turret goes to whichever limit points closest to it.
     */
    public void turnTo(double degrees) {
        commanded = true;
        findingHome = false;
        pendingTarget = Double.NaN;
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
        targetReached = false;
    }

    /** Point the turret straight at ROBOT FORWARD (FORWARD_ANGLE_DEGREES = 0, the magnet), as far as the limits allow. */
    public void turnForward() {
        turnTo(FORWARD_ANGLE_DEGREES);
    }

    /**
     * Turn "degrees from robot forward" (+ = left) into a turret angle, e.g. robotToTurret(10) = 10 (forward = 0).
     * Use this for anything measured from the robot's front: the camera (fixed on the robot), field aiming, autonomous.
     */
    public static double robotToTurret(double degreesFromForward) {
        return FORWARD_ANGLE_DEGREES + degreesFromForward;
    }

    /** Turn a turret angle into "degrees from robot forward" (+ = left), e.g. turretToRobot(0) = 0 (the magnet = forward). */
    public static double turretToRobot(double turretDegrees) {
        return turretDegrees - FORWARD_ANGLE_DEGREES;
    }

    /**
     * Turn at a speed from -1 (right) to +1 (left), for driver control. 0 = stop.
     * This cancels any turnTo() target. Before homing, it is limited to a slow speed.
     */
    public void setManualSpeed(double speed) {
        commanded = true;
        findingHome = false;
        pendingTarget = Double.NaN;
        targetDegrees = Double.NaN;
        manualSpeed = clamp(speed, 1.0);
    }

    /**
     * Turn the turret to its REST spot (REST_ANGLE_DEGREES = HOME_ANGLE_DEGREES: on the home magnet, decision 042) from wherever it is.
     * Call after START, never during INIT.
     *   Homed:     turns straight there.
     *   Not homed: first turns LEFT to find the home switch. If it hits a hard stop it turns around and goes RIGHT
     *              (decision 044). Then it turns to the rest spot.
     *              It gives up after HOME_SEARCH_TIMEOUT_S (check wasHomeSearchFailed()).
     * setManualSpeed(), turnTo() and stop() cancel it, so the operator can always take over with the stick.
     */
    public void goToRestAngle() {
        if (!connected) {
            return;
        }
        homeSearchFailed = false;
        if (homed) {
            turnToRest();
            return;
        }
        commanded = true;
        targetDegrees = Double.NaN;
        pendingTarget = Double.NaN;
        manualSpeed = 0.0;
        homeTimer.reset();
        searchDirection = 1;          // start by turning LEFT
        searchReversals = 0;
        stallTimer.reset();
        stallCheckCounts = getEncoderCounts();
        findingHome = true;
    }

    /**
     * Turn to the rest spot, arriving the SAME WAY the turret was moving when it homed (see APPROACH_OVERSHOOT_DEGREES).
     * If we are on the wrong side, go a few degrees past it first, then come back.
     */
    private void turnToRest() {
        double current = getAngle();
        if (Math.abs(current - REST_ANGLE_DEGREES) < TOLERANCE_DEGREES) {
            turnTo(REST_ANGLE_DEGREES);
        } else if (homePassMovedLeft && current > REST_ANGLE_DEGREES) {
            turnTo(REST_ANGLE_DEGREES - APPROACH_OVERSHOOT_DEGREES);   // go past on the right, then come back LEFT
            pendingTarget = REST_ANGLE_DEGREES;
        } else if (!homePassMovedLeft && current < REST_ANGLE_DEGREES) {
            turnTo(REST_ANGLE_DEGREES + APPROACH_OVERSHOOT_DEGREES);   // go past on the left, then come back RIGHT
            pendingTarget = REST_ANGLE_DEGREES;
        } else {
            turnTo(REST_ANGLE_DEGREES);
        }
    }

    /** True while the turret is moving by itself: finding the home switch, or turning to a target angle. */
    public boolean isAutoMoving() {
        return findingHome || !Double.isNaN(targetDegrees);
    }

    /** True if the last goToRestAngle() gave up without finding the home switch (timeout). */
    public boolean wasHomeSearchFailed() {
        return homeSearchFailed;
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
        findingHome = false;
        pendingTarget = Double.NaN;
        targetDegrees = Double.NaN;
        manualSpeed = 0.0;
        speedSent = 0.0;
        if (turretRotateServo != null) {
            turretRotateServo.setPower(0.0);
        }
    }

    /**
     * True when the turret is at HOME (the switch, facing forward): the switch is triggered, or (once homed)
     * the encoder is within HOME_TOLERANCE_COUNTS of the switch spot.
     * The OpMode can show this on the light: light.setColor(GREEN) (decision 018).
     */
    public boolean isAtHome() {
        return isAtHomeSwitch() || (homed && Math.abs(getCountsFromHome()) <= HOME_TOLERANCE_COUNTS);
    }

    /** Encoder counts away from the home switch spot (+ or -). 0 until homed. */
    public int getCountsFromHome() {
        return homed ? getEncoderCounts() - homeCounts : 0;
    }

    /** True once the encoder has been homed (the home switch has triggered at least once). */
    public boolean isHomed() {
        return homed;
    }

    /** True when there is a target and the turret is within TOLERANCE_DEGREES of it. */
    public boolean isAtTarget() {
        return !Double.isNaN(targetDegrees) && (targetReached || Math.abs(targetDegrees - getAngle()) < TOLERANCE_DEGREES);
    }

    /** True if the runaway safety stopped the turret. Check SERVO_REVERSED and ENCODER_REVERSED. */
    public boolean wasRunawayStopped() {
        return runawayStopped;
    }

    /** The real turret angle from the encoder (0 = the magnet = forward, + = left). NaN until homed. */
    public double getAngle() {
        if (!homed) {
            return Double.NaN;
        }
        double encoderRevs = (getEncoderCounts() - homeCounts) / ENCODER_COUNTS_PER_REV;
        double degrees = encoderRevs / ENCODER_REVS_PER_TURRET_REV * 360.0;
        return HOME_ANGLE_DEGREES + (ENCODER_REVERSED ? -degrees : degrees);
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
     * True when the magnet is at the home switch (turret angle 0: facing robot forward).
     * This is the exact magnet spot. For the light, use isAtHome() (a wider zone).
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
        telemetry.addData("Turret homed", homed ? "yes" : "NO: turn the turret past the home switch (forward) once (slow until then)");
        telemetry.addData("Turret at home", "%s  (%d counts from home, zone ±%d)",
                isAtHome() ? "YES" : "no", getCountsFromHome(), HOME_TOLERANCE_COUNTS);
        if (magnetZoneWidthCounts > 0) {
            telemetry.addData("Magnet range", "%d counts wide (%.1f°), homed on the %s", magnetZoneWidthCounts,
                    magnetZoneWidthCounts / ENCODER_COUNTS_PER_REV / ENCODER_REVS_PER_TURRET_REV * 360.0,
                    homeCentered ? "MIDDLE" : "edge");
        }
        telemetry.addData("Turret angle (°)  +left", homed ? String.format("%.1f", getAngle()) : "unknown");
        telemetry.addData("Turret target (°)", findingHome ? String.format("looking for the home switch (turning %s)...", searchDirection > 0 ? "LEFT" : "RIGHT")
                : Double.isNaN(targetDegrees) ? "none (manual)" : String.format("%.1f", targetDegrees));
        if (homeSearchFailed) {
            telemetry.addLine("⚠️ HOME NOT FOUND: the turret turned left for the switch and gave up. Check the home switch and the search direction");
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
