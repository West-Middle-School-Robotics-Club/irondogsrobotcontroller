package org.firstinspires.ftc.teamcode.opmodes.test;

import static com.pedropathing.api.Paths.line;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.subsystems.Power;

/**
 * TEST: our first Pedro Pathing drive (issue #25, decision 012). Pedro's Follower drives forward TEST_DISTANCE_IN
 * inches, then back to where it started, and shows how far off it ended.
 *
 * Before this test: tune Pedro with the AutoTune page (opmodes/test/pedro/PedroTuners.java), copy the numbers into
 * pedro/Constants.java and set Constants.FORESIGHT_TUNED = true. Until then this test does NOT drive.
 *
 * Setup: robot anywhere with TEST_DISTANCE_IN + 1 ft of open floor in front of it. Keep it STILL during INIT (the
 * Pinpoint calibrates its gyro). The start is (0, 0, 0): +X = forward, +Y = left (decision 007), in inches.
 *
 * Pedro drives the wheels here, not our Drivetrain (decision 013). Plain power, BRAKE, the same mode as every OpMode
 * (decisions 008, 055): Constants.create() sets it. Nothing moves during INIT (G304.H).
 */
// Hidden on the Driver Station (decision 052). To use it: delete the @Disabled line below, then build and deploy.
@Disabled
@Autonomous(name = "Pedro Test", group = "Tests")
public class PedroTest extends LinearOpMode {

    static final double TEST_DISTANCE_IN = 24.0;   // how far forward to drive, then back (inches)
    static final double LEG_TIMEOUT_S = 5.0;       // give up on a move after this many seconds (robot stuck?)

    @Override
    public void runOpMode() throws InterruptedException {
        Power power = new Power(hardwareMap);   // battery voltage + current on every test
        Robot robot = new Robot(hardwareMap);
        Follower follower = Constants.create(hardwareMap);   // Pinpoint + motors + tuning. Nothing moves

        Pose start = new Pose(0, 0, 0);                    // x, y in inches, heading in RADIANS
        Pose ahead = new Pose(TEST_DISTANCE_IN, 0, 0);
        // A straight line each way. linear(...) = keep the heading going from the first pose's to the second's
        // (both 0 here, so the robot keeps facing forward and drives back in reverse).
        Path forward = line(start, ahead).linear(start, ahead);
        Path back = line(ahead, start).linear(ahead, start);

        follower.setPose(start);

        while (opModeInInit()) {
            follower.update();   // reads the Pinpoint. Pedro isn't following anything yet, so the wheels stay still
            if (!Constants.FORESIGHT_TUNED) {
                telemetry.addLine("NOT TUNED: Constants.FORESIGHT_TUNED = false. This test will NOT drive.");
                telemetry.addLine("Run the Foresight Tuner first (PedroTuners.java).");
            } else {
                telemetry.addLine("Keep the robot STILL. Then press START: forward " + TEST_DISTANCE_IN + " in and back.");
            }
            addPoseTelemetry(follower);
            power.addTelemetry(telemetry);
            telemetry.update();
        }
        if (isStopRequested()) return;
        robot.launcher.resetTrigger();   // START: trigger CLOSED (blocks the balls) until something opens it

        if (Constants.FORESIGHT_TUNED) {
            follower.setPose(start);
            boolean forwardDone = drive(follower, forward, "forward");
            boolean backDone = forwardDone && drive(follower, back, "back");
            follower.stop();   // stop following and holding
            follower.update();
            robot.stop();

            // Show the result until STOP.
            while (opModeIsActive()) {
                follower.update();
                telemetry.addLine(backDone ? "DONE. It should be back at (0, 0, 0)." : "STOPPED EARLY: a move timed out.");
                Pose pose = follower.pose();
                telemetry.addData("Ended off by (in)", "%.2f", Math.hypot(pose.x(), pose.y()));
                addPoseTelemetry(follower);
                power.addTelemetry(telemetry);
                telemetry.update();
            }
        } else {
            while (opModeIsActive()) {
                telemetry.addLine("NOT TUNED: nothing drives. See Constants.FORESIGHT_TUNED.");
                telemetry.update();
            }
        }
        robot.stop();
    }

    /** Follow one path until Pedro reaches its end. Returns false if it timed out or STOP was pressed. */
    private boolean drive(Follower follower, Path path, String name) {
        ElapsedTime timer = new ElapsedTime();
        follower.follow(path);
        while (opModeIsActive() && !follower.atParametricEnd()) {
            if (timer.seconds() > LEG_TIMEOUT_S) {
                return false;
            }
            follower.update();   // Pedro: read the Pinpoint, then set the wheel powers. Call it EVERY loop
            telemetry.addData("Driving", name);
            addPoseTelemetry(follower);
            telemetry.update();
        }
        return opModeIsActive();
    }

    private void addPoseTelemetry(Follower follower) {
        Pose pose = follower.pose();
        telemetry.addData("X (in)       +forward", "%.2f", pose.x());
        telemetry.addData("Y (in)       +left", "%.2f", pose.y());
        telemetry.addData("Heading (°)  +counter-clockwise", "%.1f", Math.toDegrees(pose.heading()));
        telemetry.addData("Pedro mode", follower.mode());
    }
}
