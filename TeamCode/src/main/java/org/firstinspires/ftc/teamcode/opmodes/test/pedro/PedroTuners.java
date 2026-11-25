package org.firstinspires.ftc.teamcode.opmodes.test.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;

import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * TEST: Pedro Pathing's tuning steps for our robot (issue #25). Test code (code-structure rule 8).
 *
 * These are NOT Driver Station OpModes. Pedro's "AutoTune" is a web page on the robot that walks you through each
 * step, drives the robot, and shows the numbers to copy into pedro/Constants.java. Each @Tuner method below adds
 * one entry to that page. To use it:
 *   1. Connect a laptop to the robot's Wi-Fi (the same network as the Driver Station).
 *   2. Open the AutoTune page in a browser. Pedro's docs give the address
 *      (pedropathing.com/docs/pathing/tuning); the tuning library serves it on port 10158 of the Control Hub,
 *      so try http://192.168.43.1:10158
 *   3. Pick a tuner, follow the steps on the page. Give the robot room: several steps drive about 4 ft (48 in)
 *      forward AND sideways, at full power. Battery charged; someone ready to press STOP on the Driver Station.
 *
 * We skip Pedro's Pinpoint Tuner and Mecanum Tuner: our Pinpoint (Odometry.java) and motor directions
 * (Drivetrain.java) are already tested, and Pedro reads them from there.
 *
 * Order: 1) "Pedro Tests" > Localization Test (drive by hand, check the position goes the right way),
 *        2) "Foresight Tuner" (all the numbers), copy them into Constants.java, set FORESIGHT_TUNED = true,
 *        3) "Pedro Tests" > Line Test / Curve Test, then the "Pedro Test" OpMode on the Driver Station.
 */
public class PedroTuners {

    /** Measures the top speeds, slow-down, braking and correction numbers for Constants.java (the long one). */
    @Tuner(name = "Foresight Tuner")
    public static Procedure foresightTuner() {
        return new ForesightTuner(Constants::createLocalizer, Constants::createDrivetrain);
    }

    /** Pedro's checks: Localization (drive by hand), Line, Curve, Hold and more. Line / Curve need the tuned numbers. */
    @Tuner(name = "Pedro Tests")
    public static Procedure tests() {
        return new Tests(Constants::createDrivetrain, Constants::createLocalizer,
                () -> new Foresight(Constants.foresightConfig()));
    }
}
