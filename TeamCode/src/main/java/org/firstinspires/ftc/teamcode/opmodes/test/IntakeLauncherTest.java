package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCCommand;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import org.firstinspires.ftc.teamcode.opmodes.teleop.OperatorControls;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Launcher;
import org.firstinspires.ftc.teamcode.subsystems.Power;

import java.util.List;

/**
 * TEST: Intake Test and Launcher Test in one (issues #4, #5), to check the whole ball path:
 * intake pulls a ball IN, the water wheel carries it UP, the trigger opens, and the flywheel launches it.
 * The intake and the launcher run on their own, so you can keep the intake running while you fire.
 *
 * Needs "intakeServo", "waterWheelMotor", "launcherMotor" and "launcherTriggerServo" in the Driver Station config.
 * A missing one shows OFFLINE (the rest still works).
 *
 * Controls (gamepad 1), after START. Everything starts STOPPED, at the numbers saved in Intake.java / Launcher.java:
 *   INTAKE (same as Intake Test)
 *   A                     intake ON / OFF (toggle)
 *   Dpad UP / DOWN        intake speed +0.05 / -0.05 of whichever is running
 *   Dpad LEFT / RIGHT     intake direction (toggle): IN + UP, or OUT + DOWN
 *   X                     which one runs (toggle through): BOTH, INTAKE only, WATER WHEEL only
 *   LAUNCHER
 *   Y                     flywheel ON / OFF (toggle)
 *   Right / Left BUMPER   flywheel power +0.1 / -0.1
 *   HOLD Right TRIGGER    trigger servo to FIRE (gate open). Let go = REST (gate closed)
 *   (no button)           a NECTAR on a nectar detector switch opens the gate by itself, like Main TeleOp
 *                         (decision 059; OperatorControls.AUTO_FIRE_NECTAR and AUTO_FIRE_HOLD_S)
 *   BOTH
 *   B                     STOP everything (intake, water wheel and flywheel; trigger back to REST)
 *
 * Check, one ball at a time:
 *   1. Y: flywheel on. Wait for "Launcher Status: READY".
 *   2. A: intake on. The ball is pulled in and carried up to the closed gate (it stops there).
 *   3. Hold the right trigger: the gate opens, the ball goes into the flywheel and is launched.
 *   Watch the screen: the flywheel slows down after each ball (velocity drops, then comes back to READY).
 *   A NECTAR should make "NECTAR detected" say YES on the way up; a POLLEN should not.
 *
 * Trigger servo current: a REV hub can NOT measure one servo on its own, only ALL the servos on that hub together
 * (the "servo current" lines, one per hub). launcherTriggerServo is on the Expansion Hub (hardware-config.md),
 * which also has intakeServo, the turret servo, the flower flipper and a light. So to read the trigger alone:
 * intake OFF, then compare REST vs FIRE. "peak" is the highest reading since the trigger last moved, because the
 * servo only draws a lot for the moment it is moving. A reading that STAYS high while you hold FIRE (or at REST)
 * means the servo is pushing against something: the gate is jammed, or TRIGGER_ positions go past where it can turn.
 *
 * Tuning: like the two tests it combines. The numbers here are NOT saved: when one feels right, copy it into
 * INTAKE_SPEED / WATER_WHEEL_SPEED (Intake.java) or DEFAULT_SPIN_POWER (Launcher.java). For the trigger
 * positions, the flywheel's direction and variable power, use Launcher Test; for the switches, Intake Test.
 */
// Shown on the Driver Station for testing. Before a competition, add @Disabled on the line below to hide it (decision 052).
@TeleOp(name = "Intake + Launcher Test", group = "Tests")
public class IntakeLauncherTest extends LinearOpMode {

    private static final double INTAKE_SPEED_STEP = 0.05;
    private static final double FLYWHEEL_POWER_STEP = 0.1;
    private static final double MIN_SPEED = 0.1;
    private static final double TRIGGER_PRESSED = 0.5; // right trigger pushed more than halfway = FIRE

    private enum Run { BOTH, INTAKE_ONLY, WATER_WHEEL_ONLY }

    @Override
    public void runOpMode() {
        Power power = new Power(hardwareMap); // battery voltage + current on every test
        Intake intake = new Intake(hardwareMap);
        Launcher launcher = new Launcher(hardwareMap);
        // Test-only: the intake devices, read directly to show the power they really get (code-structure rule 8).
        // Missing from the Driver Station config = null, shown as OFFLINE.
        CRServo intakeServo = hardwareMap.tryGet(CRServo.class, Intake.INTAKE_SERVO_NAME);
        DcMotorEx waterWheelMotor = hardwareMap.tryGet(DcMotorEx.class, Intake.WATER_WHEEL_MOTOR_NAME);
        // Test-only: the hubs, to read the servo current of each one (see "Trigger servo current" above)
        List<LynxModule> hubs = hardwareMap.getAll(LynxModule.class);
        double[] servoPeakAmps = new double[hubs.size()];

        // Intake (each starts at its Intake.java number)
        double intakeSpeed = Intake.INTAKE_SPEED;
        double waterWheelSpeed = Intake.WATER_WHEEL_SPEED;
        boolean intakeOn = false;
        boolean reversed = false;                 // false = intake IN + water wheel UP
        Run run = Run.BOTH;

        // Launcher (starts at its Launcher.java number)
        double flywheelPower = Launcher.DEFAULT_SPIN_POWER;
        boolean flywheelOn = false;
        boolean triggerHeld = false;
        boolean triggerWasHeld = false;
        ElapsedTime sinceNectar = new ElapsedTime(); // NECTAR auto-launch, the same as OperatorControls
        boolean nectarSeen = false;

        // INIT: nothing moves until START (game rule G304.H).
        while (opModeInInit()) {
            telemetry.addLine("Press START. Everything starts STOPPED.");
            telemetry.addLine("INTAKE: A = on/off, dpad = speed / direction, X = which one");
            telemetry.addLine("LAUNCHER: Y = flywheel on/off, bumpers = power, HOLD right trigger = FIRE");
            telemetry.addLine("B = STOP everything");
            telemetry.addLine();
            intake.addTelemetry(telemetry);
            launcher.addTelemetry(telemetry);
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        while (opModeIsActive()) {
            // ---------- INTAKE (same as Intake Test) ----------
            // Speed (0.1 to 1) of whichever is running: INTAKE only, WATER WHEEL only, or both together
            double step = gamepad1.dpadUpWasPressed() ? INTAKE_SPEED_STEP
                    : gamepad1.dpadDownWasPressed() ? -INTAKE_SPEED_STEP : 0.0;
            if (step != 0.0) {
                if (run != Run.WATER_WHEEL_ONLY) {
                    intakeSpeed = stepSpeed(intakeSpeed, step);
                }
                if (run != Run.INTAKE_ONLY) {
                    waterWheelSpeed = stepSpeed(waterWheelSpeed, step);
                }
            }
            if (gamepad1.dpadLeftWasPressed() || gamepad1.dpadRightWasPressed()) {
                reversed = !reversed;
            }
            if (gamepad1.xWasPressed()) {
                run = Run.values()[(run.ordinal() + 1) % Run.values().length];
            }
            if (gamepad1.aWasPressed()) {
                intakeOn = !intakeOn;
            }

            // ---------- LAUNCHER ----------
            if (gamepad1.rightBumperWasPressed()) {
                flywheelPower = stepSpeed(flywheelPower, FLYWHEEL_POWER_STEP);
            }
            if (gamepad1.leftBumperWasPressed()) {
                flywheelPower = stepSpeed(flywheelPower, -FLYWHEEL_POWER_STEP);
            }
            if (gamepad1.yWasPressed()) {
                flywheelOn = !flywheelOn;
            }
            if (intake.isNectarDetected()) {
                nectarSeen = true;
                sinceNectar.reset();
            }
            boolean autoFiring = OperatorControls.AUTO_FIRE_NECTAR && nectarSeen
                    && sinceNectar.seconds() < OperatorControls.AUTO_FIRE_HOLD_S;
            triggerHeld = gamepad1.right_trigger > TRIGGER_PRESSED || autoFiring;

            // ---------- STOP everything ----------
            if (gamepad1.bWasPressed()) {
                intakeOn = false;
                flywheelOn = false;
            }

            // ---------- Run ----------
            // The intake and the water wheel always go the same way: both IN / UP, or both OUT / DOWN
            double sign = reversed ? -1.0 : 1.0;
            if (intakeOn && run != Run.WATER_WHEEL_ONLY) {
                intake.runIntake(sign * intakeSpeed);
            } else {
                intake.stopIntake();
            }
            if (intakeOn && run != Run.INTAKE_ONLY) {
                intake.runWaterWheel(sign * waterWheelSpeed);
            } else {
                intake.stopWaterWheel();
            }

            if (flywheelOn) {
                launcher.spinUp(flywheelPower);
            } else {
                launcher.stop();
            }
            if (triggerHeld) {
                launcher.fire();
            } else {
                launcher.resetTrigger();
            }
            if (triggerHeld != triggerWasHeld) {
                servoPeakAmps = new double[hubs.size()]; // the trigger just moved: start a new peak
                triggerWasHeld = triggerHeld;
            }

            // ---------- Screen ----------
            telemetry.addLine("INTAKE: A = on/off | Dpad UP/DOWN = speed | Dpad L/R = direction | X = which one");
            telemetry.addLine("LAUNCHER: Y = on/off | Bumpers = power | HOLD R trigger = FIRE | B = STOP all");
            telemetry.addLine();
            telemetry.addLine("--- INTAKE ---");
            telemetry.addData("Intake", intakeOn ? "ON" : "OFF");
            telemetry.addData("Intake speed", "%.2f   <- INTAKE_SPEED in Intake.java", intakeSpeed);
            telemetry.addData("Water wheel speed", "%.2f   <- WATER_WHEEL_SPEED in Intake.java", waterWheelSpeed);
            telemetry.addData("Direction", reversed ? "OUT + water wheel DOWN" : "IN + water wheel UP");
            telemetry.addData("Running", run == Run.BOTH ? "BOTH" : run == Run.INTAKE_ONLY ? "INTAKE only" : "WATER WHEEL only");
            telemetry.addData("  Intake servo power", intakeServo != null
                    ? String.format("%.2f", intakeServo.getPower())
                    : "OFFLINE ('" + Intake.INTAKE_SERVO_NAME + "' not in DS config)");
            telemetry.addData("  Water wheel motor power", waterWheelMotor != null
                    ? String.format("%.2f   current %.2f A", waterWheelMotor.getPower(), waterWheelMotor.getCurrent(CurrentUnit.AMPS))
                    : "OFFLINE ('" + Intake.WATER_WHEEL_MOTOR_NAME + "' not in DS config)");
            intake.addTelemetry(telemetry);
            telemetry.addLine();
            telemetry.addLine("--- LAUNCHER ---");
            telemetry.addData("Flywheel", flywheelOn ? "ON" : "OFF");
            telemetry.addData("Flywheel power", "%.2f   <- DEFAULT_SPIN_POWER in Launcher.java", flywheelPower);
            telemetry.addData("Trigger", !triggerHeld ? "REST (gate closed)"
                    : autoFiring ? "FIRE (gate open) - NECTAR auto-launch" : "FIRE (gate open)");
            launcher.addTelemetry(telemetry);
            telemetry.addLine("Servo current, ALL servos on each hub (trigger is on the Expansion Hub; intake OFF to read it alone)");
            for (int i = 0; i < hubs.size(); i++) {
                double amps = servoAmps(hubs.get(i));
                servoPeakAmps[i] = Math.max(servoPeakAmps[i], amps);
                telemetry.addData("  " + hubs.get(i).getDeviceName(), amps >= 0
                        ? String.format("%.2f A   peak %.2f A (since the trigger last moved)", amps, servoPeakAmps[i])
                        : "can't read");
            }
            telemetry.addLine();
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        intake.stop();
        launcher.stop();
        launcher.resetTrigger();
    }

    /**
     * Current (amps) of ALL the servos on one hub together, or -1 if it can't be read.
     * The SDK has no normal method for this, so we ask the hub directly with a "get ADC" command
     * (ENGINEERING mode = the hub sends milliamps).
     */
    private static double servoAmps(LynxModule hub) {
        try {
            LynxGetADCCommand command = new LynxGetADCCommand(hub,
                    LynxGetADCCommand.Channel.SERVO_CURRENT, LynxGetADCCommand.Mode.ENGINEERING);
            return command.sendReceive().getValue() / 1000.0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // the OpMode is stopping
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    /** Add one step to a speed or power and keep it between MIN_SPEED and 1. Rounded so the steps stay exact. */
    private static double stepSpeed(double speed, double step) {
        double next = Math.round((speed + step) * 100) / 100.0;
        return Math.max(MIN_SPEED, Math.min(1.0, next));
    }
}
