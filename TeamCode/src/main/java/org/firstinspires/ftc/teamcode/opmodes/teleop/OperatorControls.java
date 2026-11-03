package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Robot;

/**
 * The ONE place that turns the OPERATOR's buttons (gamepad 2) into mechanism commands (decision 029).
 * DriverControls does the same job for the DRIVER (gamepad 1).
 *
 * Operator buttons:
 *   Y             HOLD to launch: the launcher trigger moves out of the way. Let go = back to rest (blocks launching)
 *   X             water wheel UP   on / off (toggle)
 *   B             water wheel DOWN on / off (toggle). X and B cancel each other: press B while going UP = DOWN
 *   A             intake on / off (toggle)
 *   Left trigger  HOLD to EJECT: water wheel DOWN + intake OUT, to push out stored balls.
 *                 Eject beats the toggles. When you let go, the intake and water wheel are OFF.
 * The flywheel (launcherMotor) isn't on a button: Main TeleOp runs it the whole match.
 *
 * Printable diagram: TeamCode/docs/worksheets/controller-layout.pdf
 *
 * Unlike DriverControls, this needs an object (new OperatorControls()), because toggles have to
 * REMEMBER whether they're on or off between loops.
 */
public class OperatorControls {

    /** What the water wheel is doing. */
    public enum WaterWheel { OFF, UP, DOWN }

    // The triggers are analog (0 = released, 1 = all the way down). Pressed past this = "held".
    public static final double TRIGGER_HELD = 0.5;

    private WaterWheel waterWheel = WaterWheel.OFF;
    private boolean intakeOn = false;
    private boolean ejecting = false;

    /** Read the operator's buttons and run the mechanisms. Call this once per loop. */
    public void update(Robot robot, Gamepad gamepad) {
        // --- Toggles. xWasPressed() is true only ONCE per press, so holding X doesn't flicker on/off ---
        if (gamepad.xWasPressed()) {
            waterWheel = (waterWheel == WaterWheel.UP) ? WaterWheel.OFF : WaterWheel.UP;
        }
        if (gamepad.bWasPressed()) {
            waterWheel = (waterWheel == WaterWheel.DOWN) ? WaterWheel.OFF : WaterWheel.DOWN;
        }
        if (gamepad.aWasPressed()) {
            intakeOn = !intakeOn;
        }

        // --- Eject (hold left trigger) beats the toggles ---
        ejecting = gamepad.left_trigger > TRIGGER_HELD;
        if (ejecting) {
            waterWheel = WaterWheel.OFF;  // so both are OFF when the trigger is let go
            intakeOn = false;
            robot.intake.waterWheelDown();
            robot.intake.out();
        } else {
            runWaterWheel(robot);
            if (intakeOn) {
                robot.intake.in();
            } else {
                robot.intake.stopIntake();
            }
        }

        // --- Launch (hold Y) ---
        if (gamepad.y) {
            robot.launcher.fire();          // trigger out of the way: balls can reach the flywheel
        } else {
            robot.launcher.resetTrigger();  // rest: blocks launching
        }
    }

    private void runWaterWheel(Robot robot) {
        switch (waterWheel) {
            case UP:
                robot.intake.waterWheelUp();
                break;
            case DOWN:
                robot.intake.waterWheelDown();
                break;
            default:
                robot.intake.stopWaterWheel();
                break;
        }
    }

    /** Show what the operator has switched on. */
    public void addTelemetry(Telemetry telemetry) {
        telemetry.addData("Operator", ejecting ? "EJECTING" : "intake %s, water wheel %s",
                intakeOn ? "ON" : "off", waterWheel);
    }
}
