package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.subsystems.FlowerFlipper;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Launcher;

/**
 * The whole robot: one object that holds every subsystem.
 *
 * Every OpMode starts the same way:
 *     Robot robot = new Robot(hardwareMap);
 * and then uses robot.drivetrain, robot.intake, robot.launcher, robot.flowerFlipper.
 *
 * Intake, Launcher and FlowerFlipper show OFFLINE for a device that isn't in the Driver Station config,
 * so a robot that doesn't have them yet still drives.
 * FlowerFlipper shakes only if the OpMode calls robot.flowerFlipper.update() every loop.
 *
 * Adding a new mechanism? Create its class in the subsystems folder,
 * then add it here in three places: the field, the constructor, and stop()/addTelemetry().
 */
public class Robot {

    public final Drivetrain drivetrain;
    public final Intake intake;
    public final Launcher launcher;
    public final FlowerFlipper flowerFlipper;

    public Robot(HardwareMap hardwareMap) {
        drivetrain = new Drivetrain(hardwareMap);
        intake = new Intake(hardwareMap);
        launcher = new Launcher(hardwareMap);
        flowerFlipper = new FlowerFlipper(hardwareMap);
    }

    /** Stop every mechanism. */
    public void stop() {
        drivetrain.stop();
        intake.stop();
        launcher.stop();
        flowerFlipper.stop();
    }

    /** Show every subsystem's status on the Driver Station. */
    public void addTelemetry(Telemetry telemetry) {
        drivetrain.addTelemetry(telemetry);
        intake.addTelemetry(telemetry);
        launcher.addTelemetry(telemetry);
        flowerFlipper.addTelemetry(telemetry);
    }
}
