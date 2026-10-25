package org.firstinspires.ftc.teamcode;

/**
 * Which alliance we are on in this match: RED or BLUE.
 *
 * The alliance changes what the robot cares about: which AprilTags the camera
 * aims at (Camera, #7), and later which side of the field autonomous starts on
 * (decision 011: Blue = Red rotated 180°).
 *
 * The OpMode picks the alliance (for example "Red Camera Test" vs "Blue Camera Test",
 * or a Red and a Blue autonomous). Subsystems are told the alliance and never guess it.
 */
public enum Alliance {
    RED,
    BLUE;

    /** The other alliance (RED -> BLUE, BLUE -> RED). */
    public Alliance other() {
        return this == RED ? BLUE : RED;
    }
}
