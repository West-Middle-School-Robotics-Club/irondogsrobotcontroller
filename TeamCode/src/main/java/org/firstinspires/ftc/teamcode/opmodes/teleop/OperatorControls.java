package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Robot;

/**
 * The ONE place that turns the OPERATOR's buttons (gamepad 2) into mechanism commands (decisions 029, 035).
 * DriverControls does the same job for the DRIVER (gamepad 1).
 *
 * Operator buttons:
 *   Y             HOLD to launch: the launcher trigger moves out of the way. Let go = back to rest (blocks launching)
 *   A             INTAKE SYSTEM on / off (toggle): the intake AND the water wheel run together.
 *                 Starts ON when the match starts (INTAKE_ON_AT_START)
 *   B             DIRECTION (toggle): IN (intake in + water wheel UP) or OUT (intake out + water wheel DOWN, to
 *                 push balls out). Starts as IN. Works whether the system is on or off
 *   Left trigger  HOLD to EJECT: water wheel DOWN + intake OUT, for a quick push out. Eject beats the toggles.
 *                 When you let go, the intake and water wheel are OFF.
 *   X             TURRET REST SPOT: turn the turret to its rest spot, dead forward (decisions 039, 040), from
 *                 wherever it is. If it isn't homed yet it homes first (decision 042: turns LEFT to find the switch,
 *                 so the turret must start roughly forward), then goes there.
 *                 Moving the left stick cancels it, so you can always take over.
 *   Left stick    push LEFT / RIGHT to turn the turret by hand (further = faster). Let go = stop.
 *                 Slow until the turret is homed (turn it past the home switch, 90° left, once). Stops at its limits.
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

    // Does the intake system (intake + water wheel) start ON when the match starts? (A toggles it, decision 041)
    public static final boolean INTAKE_ON_AT_START = true;

    // Turret stick: ignore tiny stick movement, and the fastest the stick can turn it (0 to 1).
    public static final double TURRET_STICK_DEADZONE = 0.05;
    public static final double TURRET_MAX_STICK_SPEED = 1.0;

    private boolean intakeSystemOn = INTAKE_ON_AT_START;  // intake + water wheel, running together
    private boolean directionOut = false;    // false = IN (intake in, water wheel up), true = OUT (balls leave)
    private boolean ejecting = false;

    /** Read the operator's buttons and run the mechanisms. Call this once per loop. */
    public void update(Robot robot, Gamepad gamepad) {
        // --- Toggles. aWasPressed() is true only ONCE per press, so holding A doesn't flicker on/off ---
        if (gamepad.aWasPressed()) {
            intakeSystemOn = !intakeSystemOn;
        }
        if (gamepad.bWasPressed()) {
            directionOut = !directionOut;
        }

        // --- Eject (hold left trigger) beats the toggles ---
        ejecting = gamepad.left_trigger > TRIGGER_HELD;
        if (ejecting) {
            intakeSystemOn = false;  // so both are OFF when the trigger is let go
            robot.intake.waterWheelDown();
            robot.intake.out();
        } else if (intakeSystemOn) {
            // The intake and the water wheel always go the same way: both IN/UP, or both OUT/DOWN.
            if (directionOut) {
                robot.intake.out();
                robot.intake.waterWheelDown();
            } else {
                robot.intake.in();
                robot.intake.waterWheelUp();
            }
        } else {
            robot.intake.stop();
        }

        // --- Turret (left stick, left / right) ---
        // Stick pushed LEFT is negative x, but turret + = LEFT (decision 007), so we flip it here.
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

        // --- Launch (hold Y) ---
        if (gamepad.y) {
            robot.launcher.fire();          // trigger out of the way: balls can reach the flywheel
        } else {
            robot.launcher.resetTrigger();  // rest: blocks launching
        }
    }

    /** Show what the operator has switched on. */
    public void addTelemetry(Telemetry telemetry) {
        telemetry.addData("Operator", ejecting ? "EJECTING"
                : String.format("intake system %s, direction %s", intakeSystemOn ? "ON" : "off",
                directionOut ? "OUT (balls leave)" : "IN"));
    }
}
