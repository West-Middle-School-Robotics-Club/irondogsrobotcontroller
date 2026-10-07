package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Light subsystem: the goBILDA RGB Indicator Light (decision 018).
 *
 * It plugs into a SERVO port. The "servo position" we send picks the color. It doesn't move anything,
 * so it's OK to use during INIT (handy for showing drivers that the turret faces forward before the match).
 *
 * This class only knows colors. The OpMode decides WHAT each color means,
 * for example: GREEN = turret facing forward.
 */
public class Light {

    // Config name (decision 009). Configure it as a "Servo" in the Driver Station.
    public static final String LIGHT_NAME = "indicatorLight";

    /**
     * Colors and their servo positions, from goBILDA's color chart (with the standard 500-2500 µs servo range).
     * TODO(#7): check each color with the "Light Test" OpMode and fix any that look wrong.
     */
    public enum Color {
        OFF(0.0),
        RED(0.277),
        ORANGE(0.333),
        YELLOW(0.388),
        SAGE(0.444),
        GREEN(0.500),
        AZURE(0.555),
        BLUE(0.611),
        INDIGO(0.666),
        VIOLET(0.722),
        WHITE(1.0);

        public final double position;

        Color(double position) {
            this.position = position;
        }
    }

    private final Servo indicatorLight;
    private Color color = Color.OFF;

    public Light(HardwareMap hardwareMap) {
        indicatorLight = hardwareMap.get(Servo.class, LIGHT_NAME);
        setColor(Color.OFF);
    }

    /** Change the light's color. */
    public void setColor(Color newColor) {
        color = newColor;
        indicatorLight.setPosition(newColor.position);
    }

    /** Turn the light off. */
    public void off() {
        setColor(Color.OFF);
    }

    /** The color we last set. */
    public Color getColor() {
        return color;
    }

    /** Show this subsystem's status on the Driver Station. */
    public void addTelemetry(Telemetry telemetry) {
        telemetry.addData("Light", color);
    }
}
