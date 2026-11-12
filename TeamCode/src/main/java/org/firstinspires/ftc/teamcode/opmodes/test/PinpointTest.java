package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.robotcore.external.navigation.UnnormalizedAngleUnit;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.opmodes.teleop.DriverControls;
import org.firstinspires.ftc.teamcode.subsystems.Odometry;
import org.firstinspires.ftc.teamcode.subsystems.Power;

/**
 * TEST: check and tune the Pinpoint odometry (issue #6).
 *
 * Follow the step-by-step test procedure in issue #6, and record your results there.
 * The settings you tune (pod type, offsets, directions) are at the top of Odometry.java.
 *
 * Controls (gamepad 1):
 *   X          reset position to 0,0,0 (do this before each test run)
 *   B          reset AND recalibrate the IMU (robot must be STILL)
 *   A          straight-line assist on / off (starts ON, same as Main TeleOp): a stick pointed almost straight
 *              drives exactly straight, so a "drive forward 48 in" test really goes straight
 *   dpad up / down   speed dial: raise / lower the driving speed (starts at SPEED_DIAL_START)
 *   sticks     drive (bumper = slow, trigger = turbo, as in Main TeleOp, times the speed dial)
 *
 * The drive motors run on plain power (no encoder speed control) and BRAKE when the sticks are released, the same as
 * Main TeleOp and Basic Auton (decisions 054, 023, 049), so the robot stops where you let go and a measured drive
 * ends cleanly. It is harder to push by hand; for the
 * push-by-hand steps, push slowly or drive instead.
 *
 * Printable worksheet (steps, Expected / Actual / Fix, numbers for the code): TeamCode/docs/worksheets/pinpoint-test.pdf
 */
// Hidden on the Driver Station (decision 052). To use it: delete the @Disabled line below, then build and deploy.
@TeleOp(name = "Pinpoint Test", group = "Tests")
public class PinpointTest extends LinearOpMode {

    // The speed dial (dpad up / down) multiplies every drive power. Start low: the test drives are short and measured.
    static final double SPEED_DIAL_START = 0.5;   // where the dial is at START (0.5 = half of normal)
    static final double SPEED_DIAL_STEP = 0.1;    // one dpad press changes it this much
    static final double SPEED_DIAL_MIN = 0.1;     // the dial never goes below this (the robot would not move)

    @Override
    public void runOpMode() {
        Power power = new Power(hardwareMap); // battery voltage + current on every test
        Robot robot = new Robot(hardwareMap);
        DriverControls driver = new DriverControls();   // A = straight-line assist, starts ON
        Odometry odometry = new Odometry(hardwareMap);
        // Test-only: the same Pinpoint, used directly for resets and extra tuning details (code-structure rule 8).
        GoBildaPinpointDriver pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, Odometry.PINPOINT_NAME);

        robot.drivetrain.setBrakeOnStop(true);          // stop where the sticks are released (BRAKE, decision 023)
        robot.drivetrain.setEncoderSpeedControl(false); // plain power, like every OpMode (decision 054)
        double speedDial = SPEED_DIAL_START;

        // Wait for START, showing whether the IMU calibration has finished.
        while (opModeInInit()) {
            odometry.update();
            if (odometry.isReady()) {
                telemetry.addLine("Pinpoint READY. Press START.");
            } else {
                telemetry.addLine("Keep the robot STILL until the status says READY...");
            }
            telemetry.addData("Odometry status", odometry.getStatus());
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        while (opModeIsActive()) {
            odometry.update();

            if (gamepad1.xWasPressed()) {
                // Back to 0,0,0 without recalibrating the IMU.
                pinpoint.setPosition(new Pose2D(DistanceUnit.INCH, 0, 0, AngleUnit.DEGREES, 0));
            }
            if (gamepad1.bWasPressed()) {
                // Back to 0,0,0 AND recalibrate the IMU. The robot must be STILL.
                pinpoint.resetPosAndIMU();
            }
            if (gamepad1.dpadUpWasPressed()) {
                speedDial = Math.min(1.0, speedDial + SPEED_DIAL_STEP);
            }
            if (gamepad1.dpadDownWasPressed()) {
                speedDial = Math.max(SPEED_DIAL_MIN, speedDial - SPEED_DIAL_STEP);
            }

            DriverControls.DriveSpeed speed = driver.drive(robot.drivetrain, gamepad1, speedDial);

            telemetry.addLine("X = reset to 0,0,0     B = reset + recalibrate (keep STILL)");
            telemetry.addLine("A = straight-line assist     dpad up/down = speed dial");
            telemetry.addData("Speed dial (dpad)", "%.0f %%", speedDial * 100);
            driver.addTelemetry(telemetry, speed);
            robot.drivetrain.addTelemetry(telemetry);
            telemetry.addLine();
            odometry.addTelemetry(telemetry);
            telemetry.addData("Total turning (°)", "%.1f", pinpoint.getHeading(UnnormalizedAngleUnit.DEGREES));
            telemetry.addData("Raw encoder X / Y", "%d  /  %d", pinpoint.getEncoderX(), pinpoint.getEncoderY());
            telemetry.addData("Pinpoint update rate (Hz)", "%.0f", pinpoint.getFrequency());
            telemetry.addLine();
            telemetry.addLine("Push forward: X goes UP");
            telemetry.addLine("Push left: Y goes UP");
            telemetry.addLine("Turn left: heading goes UP");
            power.addTelemetry(telemetry);
            telemetry.update();
        }

        robot.stop();
    }
}
