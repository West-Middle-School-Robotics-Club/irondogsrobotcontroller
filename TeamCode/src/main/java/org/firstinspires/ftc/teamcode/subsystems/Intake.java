package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Intake subsystem: picks up game elements and carries them to the launcher (issue #4).
 *
 * Used by BOTH TeleOp and Autonomous. This class doesn't know where its commands come from.
 *
 * Hardware (decision 024 names):
 *   intakeServo        CONTINUOUS servo: pulls game elements into the robot
 *   waterWheelServo    CONTINUOUS servo: the "water wheel" (the team's name, from how it looks).
 *                      Moves game elements up to the launcher
 *   nectarDetectorLeftSwitch, nectarDetectorRightSwitch
 *                      goBILDA micro limit switches (bump lever), one on each side of the ball path.
 *                      A NECTAR (~3.6 in ball) is wide enough to press them; a POLLEN (~2.8 in) is not.
 *                      If EITHER switch is pressed, the NEXT ball to be launched is a NECTAR.
 *                      Use isNectarDetected(): it gives a plain true/false, already translated from the
 *                      switch's raw reading (set NECTAR_SWITCH_WIRING once, below).
 * (These names don't start with "intake": the team picked them, decision 028.)
 *
 * Driver Station config types: "Continuous Rotation Servo" for the two servos, "Digital Device" for the switches.
 * A device that isn't in the config shows OFFLINE instead of crashing every OpMode.
 */
public class Intake {

    // Config names (decisions 009, 024). Must match the Driver Station robot configuration.
    public static final String INTAKE_SERVO_NAME = "intakeServo";
    public static final String WATER_WHEEL_SERVO_NAME = "waterWheelServo";
    public static final String NECTAR_LEFT_SWITCH_NAME = "nectarDetectorLeftSwitch";
    public static final String NECTAR_RIGHT_SWITCH_NAME = "nectarDetectorRightSwitch";

    /**
     * How a switch is wired. This tells the code how to read it, so nobody has to remember
     * whether "pressed" is true or false.
     *   NORMALLY_OPEN    pressing the lever CONNECTS the circuit (raw reading: pressed = false). Most common.
     *   NORMALLY_CLOSED  pressing the lever BREAKS the circuit   (raw reading: pressed = true)
     */
    public enum SwitchWiring { NORMALLY_OPEN, NORMALLY_CLOSED }

    // TODO(#4): check with "Intake Test": press a lever and "Nectar left/right" must say PRESSED.
    // If it says PRESSED when NOT pressed, change this to the other wiring.
    // Note 11/6: said PRESSED when not pressed, so changed to NORMALLY_CLOSED
    public static final SwitchWiring NECTAR_SWITCH_WIRING = SwitchWiring.NORMALLY_CLOSED;

    // Speeds, from 0 to 1. TODO(#4): tune on the robot with "Intake Test".
    //Note 11/6: changed from 1.0 to 0.8 for intake and water wheel
    public static final double INTAKE_SPEED = 0.8;
    public static final double WATER_WHEEL_SPEED = 0.8;

    // TODO(#4): set to true if "in" / "waterWheelUp" spins the wrong way.
    public static final boolean INTAKE_REVERSED = false;
    public static final boolean WATER_WHEEL_REVERSED = false;

    private final CRServo intakeServo;        // null if not in the Driver Station config
    private final CRServo waterWheelServo;  // null if not in the Driver Station config
    private final DigitalChannel nectarDetectorLeftSwitch;   // null if not in the Driver Station config
    private final DigitalChannel nectarDetectorRightSwitch;  // null if not in the Driver Station config
    private double intakeSpeed = 0.0;
    private double waterWheelSpeed = 0.0;

    public Intake(HardwareMap hardwareMap) {
        intakeServo = hardwareMap.tryGet(CRServo.class, INTAKE_SERVO_NAME);
        waterWheelServo = hardwareMap.tryGet(CRServo.class, WATER_WHEEL_SERVO_NAME);
        nectarDetectorLeftSwitch = hardwareMap.tryGet(DigitalChannel.class, NECTAR_LEFT_SWITCH_NAME);
        nectarDetectorRightSwitch = hardwareMap.tryGet(DigitalChannel.class, NECTAR_RIGHT_SWITCH_NAME);
        if (nectarDetectorLeftSwitch != null) {
            nectarDetectorLeftSwitch.setMode(DigitalChannel.Mode.INPUT);
        }
        if (nectarDetectorRightSwitch != null) {
            nectarDetectorRightSwitch.setMode(DigitalChannel.Mode.INPUT);
        }

        if (intakeServo != null) {
            intakeServo.setDirection(INTAKE_REVERSED ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
        }
        if (waterWheelServo != null) {
            waterWheelServo.setDirection(WATER_WHEEL_REVERSED ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
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

    /** Run the water wheel to move game elements UP to the launcher. */
    public void waterWheelUp() {
        setWaterWheelSpeed(WATER_WHEEL_SPEED);
    }

    /** Run the water wheel backwards (DOWN), to clear a jam. */
    public void waterWheelDown() {
        setWaterWheelSpeed(-WATER_WHEEL_SPEED);
    }

    /** Stop only the intake (the water wheel keeps doing what it was doing). */
    public void stopIntake() {
        setIntakeSpeed(0.0);
    }

    /** Stop only the water wheel. */
    public void stopWaterWheel() {
        setWaterWheelSpeed(0.0);
    }

    /** True when the NEXT ball to be launched is a NECTAR: either nectar detector switch is pressed. */
    // Note 11/6, have noticed for nectars it only ever really presses one of the detector switches which is fine, but noting it down
    public boolean isNectarDetected() {
        return isPressed(nectarDetectorLeftSwitch) || isPressed(nectarDetectorRightSwitch);
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

    /** Stop the intake AND the water wheel. */
    public void stop() {
        setIntakeSpeed(0.0);
        setWaterWheelSpeed(0.0);
    }

    private void setIntakeSpeed(double speed) {
        intakeSpeed = speed;
        if (intakeServo != null) {
            intakeServo.setPower(speed);
        }
    }

    private void setWaterWheelSpeed(double speed) {
        waterWheelSpeed = speed;
        if (waterWheelServo != null) {
            waterWheelServo.setPower(speed);
        }
    }

    /** Show this subsystem's status on the Driver Station. */
    public void addTelemetry(Telemetry telemetry) {
        telemetry.addData("Intake", intakeServo != null ? String.format("%.2f", intakeSpeed)
                : "OFFLINE ('" + INTAKE_SERVO_NAME + "' not in DS config)");
        telemetry.addData("Water wheel", waterWheelServo != null ? String.format("%.2f", waterWheelSpeed)
                : "OFFLINE ('" + WATER_WHEEL_SERVO_NAME + "' not in DS config)");
        telemetry.addData("NECTAR detected", isNectarDetected() ? "YES (next ball is a NECTAR)" : "no");
        telemetry.addData("  Nectar left switch", switchStatus(nectarDetectorLeftSwitch, NECTAR_LEFT_SWITCH_NAME));
        telemetry.addData("  Nectar right switch", switchStatus(nectarDetectorRightSwitch, NECTAR_RIGHT_SWITCH_NAME));
    }
}
