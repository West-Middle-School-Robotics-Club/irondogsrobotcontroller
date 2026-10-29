package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Light subsystem: ONE goBILDA RGB Indicator Light (decision 018). The robot has two:
 *     Light leftLight  = new Light(hardwareMap, Light.LEFT_LIGHT_NAME);
 *     Light rightLight = new Light(hardwareMap, Light.RIGHT_LIGHT_NAME);
 *
 * It plugs into a SERVO port. The "servo position" we send picks the color. It doesn't move anything,
 * so it's OK to use during INIT (handy for showing drivers that the turret faces forward before the match).
 *
 * This class only knows colors. The OpMode decides WHAT each color means,
 * for example: GREEN = turret facing forward.
 */
public class Light {

    // Config names (decisions 009, 024). Configure each as a "Servo" in the Driver Station.
    public static final String LEFT_LIGHT_NAME = "leftIndicatorLight";
    public static final String RIGHT_LIGHT_NAME = "rightIndicatorLight";

    /**
     * Colors and their servo positions, from goBILDA's color chart. Uses the FTC SDK's default servo signal range (600-2400 µs).
     * TODO(#31): check each color with the "Light Test" OpMode and fix any that look wrong.
     */
    public enum Color {
        OFF(0.0),
        RED(0.282),
        ORANGE(0.308),
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

    private final String name;
    private final Servo indicatorLight;   // null if this light isn't in the Driver Station config
    private Color color = Color.OFF;

    /** One light. name = LEFT_LIGHT_NAME or RIGHT_LIGHT_NAME. A missing light shows OFFLINE instead of crashing. */
    public Light(HardwareMap hardwareMap, String name) {
        this.name = name;
        indicatorLight = hardwareMap.tryGet(Servo.class, name);
        setColor(Color.OFF);
    }

    /** Change the light's color. */
    public void setColor(Color newColor) {
        color = newColor;
        if (indicatorLight != null) {
            indicatorLight.setPosition(newColor.position);
        }
    }

    /** True if this light was found in the Driver Station config. */
    public boolean isConnected() {
        return indicatorLight != null;
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
        telemetry.addData("Light " + name, indicatorLight != null ? color.toString() : "OFFLINE (not in DS config)");
    }
}
