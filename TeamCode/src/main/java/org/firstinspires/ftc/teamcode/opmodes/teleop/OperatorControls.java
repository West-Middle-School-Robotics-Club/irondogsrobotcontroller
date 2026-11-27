package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.subsystems.Intake;

/**
 * The ONE place that turns the OPERATOR's buttons (gamepad 2) into mechanism commands (decisions 029, 035).
 * DriverControls does the same job for the DRIVER (gamepad 1).
 *
 * Operator buttons:
 *   Y             HOLD to launch: the launcher trigger moves out of the way. Let go = back to rest (blocks launching)
 *   (no button)   OFF for now (decision 065: Intake.NECTAR_DETECTOR_ENABLED = false), so Y launches every ball.
 *                 When turned back on: NECTARs launch by THEMSELVES (AUTO_FIRE_NECTAR, decision 059): when a NECTAR
 *                 presses a nectar detector switch, the trigger opens, and stays open AUTO_FIRE_HOLD_S after the
 *                 switch lets go, so the ball gets through. POLLEN still needs Y. Not while ejecting.
 *   (no button)   The INTAKE runs IN the whole match, from START (INTAKE_ALWAYS_ON = true, decision 067).
 *   A             WATER WHEEL on / off (toggle). Only the water wheel: the intake keeps turning.
 *                 Starts OFF (WATER_WHEEL_ON_AT_START = false): press A to start it, A again to stop.
 *                 It runs the SHOOTING way: water wheel UP.
 *                 (INTAKE_ALWAYS_ON = false brings back the old way: A starts and stops the intake AND the water wheel.)
 *   B             does NOTHING for now (DIRECTION_TOGGLE_ENABLED = false): the direction is always IN.
 *                 To push balls out, hold the left trigger (EJECT).
 *                 When turned back on, B = DIRECTION (toggle): IN (intake in + water wheel UP) or OUT (intake out +
 *                 water wheel DOWN, to push balls out). Starts as IN. Works whether the system is on or off
 *   Left trigger  HOLD to EJECT: water wheel DOWN + intake OUT, for a quick push out. Eject beats the toggles.
 *                 When you let go, the water wheel is OFF and the intake goes back to IN (always on).
 *   (TURRET_ENABLED = false for now, decision 050: the turret does NOT move, so X and the left stick do nothing.)
 *   X             TURRET REST SPOT: turn the turret onto the home magnet (decision 042), from
 *                 wherever it is. If it isn't homed yet it first turns left to find the home switch, then goes there.
 *                 Moving the left stick cancels it, so you can always take over.
 *   Left stick    push LEFT / RIGHT to turn the turret by hand (further = faster). Let go = stop.
 *                 Slow until the turret is homed (turn it past the home switch, at forward, once). Stops at its limits.
 * The flywheel (launcherMotor) isn't on a button: Main TeleOp runs it the whole match.
 *
 * Printable diagram: TeamCode/docs/worksheets/controller-layout.pdf
 *
 * Unlike DriverControls, this needs an object (new OperatorControls()), because toggles have to
 * REMEMBER whether they're on or off between loops.
 */
public class OperatorControls {

    // The triggers are analog (0 = released, 1 = all the way down). Pressed past this = "held".
    public static final double TRIGGER_HELD = 0.5;

    // Does the intake run the WHOLE match (from START), so A only starts / stops the water wheel? (decision 067)
    // false = the old way (062): A starts and stops the intake AND the water wheel together.
    public static final boolean INTAKE_ALWAYS_ON = true;

    // Does the water wheel start ON when the match starts? (A toggles it, decisions 041, 062, 067)
    // When INTAKE_ALWAYS_ON is false, this is for the intake too (they run together).
    public static final boolean WATER_WHEEL_ON_AT_START = false;

    // Can B flip the intake + water wheel to OUT? false = B does nothing, so the operator can't reverse them by
    // accident (the left trigger EJECT still pushes balls out). Set true to turn the B toggle back on.
    public static final boolean DIRECTION_TOGGLE_ENABLED = false;

    // Can the turret move in Main TeleOp? false = the left stick and X do nothing, and the turret does not move
    // at START either (MainTeleOp checks this too). Set true to turn the turret controls back on (decision 050).
    public static final boolean TURRET_ENABLED = false;

    // Turret stick: ignore tiny stick movement, and the fastest the stick can turn it (0 to 1).
    public static final double TURRET_STICK_DEADZONE = 0.05;
    public static final double TURRET_MAX_STICK_SPEED = 1.0;

    // Launch NECTARs automatically: a nectar detector switch pressed = open the trigger (decision 059).
    // false = only Y opens the trigger, like before. Does nothing while Intake.NECTAR_DETECTOR_ENABLED is false (065).
    public static final boolean AUTO_FIRE_NECTAR = true;

    // After the switch lets go (the ball has moved on), keep the trigger open this long so it doesn't close on the ball.
    public static final double AUTO_FIRE_HOLD_S = 0.5;

    private final ElapsedTime sinceNectar = new ElapsedTime(); // time since a NECTAR last pressed a switch
    private boolean nectarSeen = false;      // has a NECTAR pressed a switch yet this match?
    private boolean autoFiring = false;

    private boolean waterWheelOn = WATER_WHEEL_ON_AT_START;  // A toggle (the intake too if INTAKE_ALWAYS_ON is false)
    private boolean directionOut = false;    // false = IN (intake in, water wheel up), true = OUT (balls leave)
    private boolean ejecting = false;

    /** Read the operator's buttons and run the mechanisms. Call this once per loop. */
    public void update(Robot robot, Gamepad gamepad) {
        // --- Toggles. aWasPressed() is true only ONCE per press, so holding A doesn't flicker on/off ---
        if (gamepad.aWasPressed()) {
            waterWheelOn = !waterWheelOn;
        }
        if (DIRECTION_TOGGLE_ENABLED && gamepad.bWasPressed()) {
            directionOut = !directionOut;
        }

        // --- Eject (hold left trigger) beats the toggles ---
        ejecting = gamepad.left_trigger > TRIGGER_HELD;
        if (ejecting) {
            waterWheelOn = false;  // so the water wheel is OFF when the trigger is let go
            robot.intake.waterWheelDown();
            robot.intake.out();
        } else {
            // The intake and the water wheel always go the same way: both IN/UP, or both OUT/DOWN.
            // Intake: always on (INTAKE_ALWAYS_ON), or only together with the water wheel (the old way)
            if (INTAKE_ALWAYS_ON || waterWheelOn) {
                if (directionOut) {
                    robot.intake.out();
                } else {
                    robot.intake.in();
                }
            } else {
                robot.intake.stopIntake();
            }
            // Water wheel: A turns it on / off
            if (waterWheelOn) {
                if (directionOut) {
                    robot.intake.waterWheelDown();
                } else {
                    robot.intake.waterWheelUp();
                }
            } else {
                robot.intake.stopWaterWheel();
            }
        }

        // --- Turret (left stick, left / right) ---
        // Stick pushed LEFT is negative x, but turret + = LEFT (decision 007), so we flip it here.
        // Skipped completely when TURRET_ENABLED is false: the turret is never given a command, so it can't move.
        if (TURRET_ENABLED) {
            double turretSpeed = -gamepad.left_stick_x;
            if (Math.abs(turretSpeed) < TURRET_STICK_DEADZONE) {
                turretSpeed = 0.0;
            }
            // Moving the stick takes over from "turret rest spot". An idle stick must NOT cancel it, so we only send
            // "speed 0" when the turret is not moving by itself.
            if (turretSpeed != 0.0 || !robot.turret.isAutoMoving()) {
                robot.turret.setManualSpeed(turretSpeed * TURRET_MAX_STICK_SPEED);
            }
            // X = turn the turret to its rest spot (a press starts it; the turret stops itself there)
            if (gamepad.xWasPressed()) {
                robot.turret.goToRestAngle();
            }
        }

        // --- Auto-launch a NECTAR (no button) ---
        if (robot.intake.isNectarDetected()) {
            nectarSeen = true;
            sinceNectar.reset();             // the switch is pressed right now: restart the hold time
        }
        autoFiring = AUTO_FIRE_NECTAR && !ejecting && nectarSeen && sinceNectar.seconds() < AUTO_FIRE_HOLD_S;

        // --- Launch (hold Y, or a NECTAR on its way) ---
        if (gamepad.y || autoFiring) {
            robot.launcher.fire();          // trigger out of the way: balls can reach the flywheel
        } else {
            robot.launcher.resetTrigger();  // rest: blocks launching
        }
    }

    /**
     * True while balls are being pushed OUT: the left trigger is held (EJECT: water wheel DOWN + intake OUT), or the
     * B direction toggle is set to OUT (off for now, DIRECTION_TOGGLE_ENABLED = false). Main TeleOp shows it on the lights.
     */
    public boolean isEjecting() {
        return ejecting || directionOut;
    }

    /**
     * Show what the operator has switched on. "NECTAR auto-launch" only shows while the nectar detectors are on
     * (Intake.NECTAR_DETECTOR_ENABLED, decision 065), so it comes back by itself if they're switched on again.
     */
    public void addTelemetry(Telemetry telemetry) {
        telemetry.addData("Operator", ejecting ? "EJECTING"
                : String.format("intake %s, water wheel %s (A), direction %s",
                INTAKE_ALWAYS_ON || waterWheelOn ? "ON" : "off", waterWheelOn ? "ON" : "off",
                directionOut ? "OUT (balls leave)" : "IN"));
        if (Intake.NECTAR_DETECTOR_ENABLED) {
            telemetry.addData("NECTAR auto-launch", !AUTO_FIRE_NECTAR ? "OFF (AUTO_FIRE_NECTAR = false)"
                    : autoFiring ? "FIRING (trigger open)" : "on, waiting for a NECTAR");
        }
    }
}
