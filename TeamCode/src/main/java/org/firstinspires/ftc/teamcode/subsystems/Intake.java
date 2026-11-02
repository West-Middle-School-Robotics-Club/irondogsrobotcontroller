package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Intake subsystem: picks up game elements and loads them toward the launcher (issue #4).
 *
 * Used by BOTH TeleOp and Autonomous. This class doesn't know where its commands come from.
 *
 * Hardware (decision 024 names):
 *   intakeServo        CONTINUOUS servo: pulls game elements into the robot
 *   intakeLoaderServo  CONTINUOUS servo: moves game elements up to the launcher
 *   intakeNectarLeftSwitch, intakeNectarRightSwitch
 *                      goBILDA micro limit switches (bump lever), one on each side of the intake path.
 *                      A NECTAR (~3.6 in ball) is wide enough to press them; a POLLEN (~2.8 in) is not.
 *                      Use isNectarDetected(): it gives a plain true/false, already translated from the
 *                      switch's raw reading (set NECTAR_SWITCH_WIRING once, below).
 *
 * Driver Station config types: "Continuous Rotation Servo" for the two servos, "Digital Device" for the switches.
 * A device that isn't in the config shows OFFLINE instead of crashing every OpMode.
 */
public class Intake {

    // Config names (decisions 009, 024). Must match the Driver Station robot configuration.
    public static final String INTAKE_SERVO_NAME = "intakeServo";
    public static final String LOADER_SERVO_NAME = "intakeLoaderServo";
    public static final String NECTAR_LEFT_SWITCH_NAME = "intakeNectarLeftSwitch";
    public static final String NECTAR_RIGHT_SWITCH_NAME = "intakeNectarRightSwitch";

    /**
     * How a switch is wired. This tells the code how to read it, so nobody has to remember
     * whether "pressed" is true or false.
     *   NORMALLY_OPEN    pressing the lever CONNECTS the circuit (raw reading: pressed = false). Most common.
     *   NORMALLY_CLOSED  pressing the lever BREAKS the circuit   (raw reading: pressed = true)
     */
    public enum SwitchWiring { NORMALLY_OPEN, NORMALLY_CLOSED }

    // TODO(#4): check with "Intake Test": press a lever and "Nectar left/right" must say PRESSED.
    // If it says PRESSED when NOT pressed, change this to the other wiring.
    public static final SwitchWiring NECTAR_SWITCH_WIRING = SwitchWiring.NORMALLY_OPEN;

    // Speeds, from 0 to 1. TODO(#4): tune on the robot with "Intake Test".
    public static final double INTAKE_SPEED = 1.0;
    public static final double LOADER_SPEED = 1.0;

    // TODO(#4): set to true if "in" / "load" spins the wrong way.
    public static final boolean INTAKE_REVERSED = false;
    public static final boolean LOADER_REVERSED = false;

    private final CRServo intakeServo;        // null if not in the Driver Station config
    private final CRServo intakeLoaderServo;  // null if not in the Driver Station config
    private final DigitalChannel intakeNectarLeftSwitch;   // null if not in the Driver Station config
    private final DigitalChannel intakeNectarRightSwitch;  // null if not in the Driver Station config
    private double intakeSpeed = 0.0;
    private double loaderSpeed = 0.0;

    public Intake(HardwareMap hardwareMap) {
        intakeServo = hardwareMap.tryGet(CRServo.class, INTAKE_SERVO_NAME);
        intakeLoaderServo = hardwareMap.tryGet(CRServo.class, LOADER_SERVO_NAME);
        intakeNectarLeftSwitch = hardwareMap.tryGet(DigitalChannel.class, NECTAR_LEFT_SWITCH_NAME);
        intakeNectarRightSwitch = hardwareMap.tryGet(DigitalChannel.class, NECTAR_RIGHT_SWITCH_NAME);
        if (intakeNectarLeftSwitch != null) {
            intakeNectarLeftSwitch.setMode(DigitalChannel.Mode.INPUT);
        }
        if (intakeNectarRightSwitch != null) {
            intakeNectarRightSwitch.setMode(DigitalChannel.Mode.INPUT);
        }

        if (intakeServo != null) {
            intakeServo.setDirection(INTAKE_REVERSED ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
        }
        if (intakeLoaderServo != null) {
            intakeLoaderServo.setDirection(LOADER_REVERSED ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
        }
        // Nothing spins until the first command (game rule G304.H: INIT must not move).
    }

    /** Run the intake to pull game elements in. */
    public void in() {
        setIntakeSpeed(INTAKE_SPEED);
    }

    /** Run the intake backwards to push game elements out. */
    public void out() {
        setIntakeSpeed(-INTAKE_SPEED);
    }

    /** Run the loader to move game elements UP to the launcher. */
    public void load() {
        setLoaderSpeed(LOADER_SPEED);
    }

    /** Run the loader backwards, to clear a jam. */
    public void unload() {
        setLoaderSpeed(-LOADER_SPEED);
    }

    /** Stop only the intake (the loader keeps doing what it was doing). */
    public void stopIntake() {
        setIntakeSpeed(0.0);
    }

    /** Stop only the loader. */
    public void stopLoader() {
        setLoaderSpeed(0.0);
    }

    /** True when a NECTAR is in the intake: either nectar switch is pressed. */
    public boolean isNectarDetected() {
        return isPressed(intakeNectarLeftSwitch) || isPressed(intakeNectarRightSwitch);
    }

    /** Turns a switch's raw reading into "pressed or not", using NECTAR_SWITCH_WIRING. A missing switch = not pressed. */
    private static boolean isPressed(DigitalChannel nectarSwitch) {
        if (nectarSwitch == null) {
            return false;
        }
        boolean rawReading = nectarSwitch.getState();
        // Normally open: pressed connects the line to ground, which reads false.
        return NECTAR_SWITCH_WIRING == SwitchWiring.NORMALLY_OPEN ? !rawReading : rawReading;
    }

    private static String switchStatus(DigitalChannel nectarSwitch, String name) {
        if (nectarSwitch == null) {
            return "OFFLINE ('" + name + "' not in DS config)";
        }
        return isPressed(nectarSwitch) ? "PRESSED" : "not pressed";
    }

    /** Stop the intake AND the loader. */
    public void stop() {
        setIntakeSpeed(0.0);
        setLoaderSpeed(0.0);
    }

    private void setIntakeSpeed(double speed) {
        intakeSpeed = speed;
        if (intakeServo != null) {
            intakeServo.setPower(speed);
        }
    }

    private void setLoaderSpeed(double speed) {
        loaderSpeed = speed;
        if (intakeLoaderServo != null) {
            intakeLoaderServo.setPower(speed);
        }
    }

    /** Show this subsystem's status on the Driver Station. */
    public void addTelemetry(Telemetry telemetry) {
        telemetry.addData("Intake", intakeServo != null ? String.format("%.2f", intakeSpeed)
                : "OFFLINE ('" + INTAKE_SERVO_NAME + "' not in DS config)");
        telemetry.addData("Intake loader", intakeLoaderServo != null ? String.format("%.2f", loaderSpeed)
                : "OFFLINE ('" + LOADER_SERVO_NAME + "' not in DS config)");
        telemetry.addData("NECTAR detected", isNectarDetected() ? "YES" : "no");
        telemetry.addData("  Nectar left switch", switchStatus(intakeNectarLeftSwitch, NECTAR_LEFT_SWITCH_NAME));
        telemetry.addData("  Nectar right switch", switchStatus(intakeNectarRightSwitch, NECTAR_RIGHT_SWITCH_NAME));
    }
}
