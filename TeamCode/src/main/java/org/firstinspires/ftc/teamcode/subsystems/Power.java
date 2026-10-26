package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit;

import java.util.List;

/**
 * Power subsystem: battery voltage and how much current each REV hub is drawing.
 *
 * Nothing to control here; it only reads. Shown on every test OpMode and Main TeleOp so we can
 * see a low battery (motors get weak below ~11 V, the hub browns out near 7 V) and spot a mechanism
 * that is drawing too much current (a jammed intake, a stalled motor).
 *
 * The hubs (Control Hub + Expansion Hub) are found automatically; no config name needed.
 */
public class Power {

    // Below this the robot drives noticeably weaker; swap the battery between matches.
    public static final double LOW_BATTERY_VOLTS = 11.5;

    private final List<LynxModule> hubs;

    public Power(HardwareMap hardwareMap) {
        hubs = hardwareMap.getAll(LynxModule.class);
    }

    /** Battery voltage in volts: the lowest reading across the hubs (they share the same battery). */
    public double getBatteryVolts() {
        double lowest = Double.MAX_VALUE;
        for (LynxModule hub : hubs) {
            lowest = Math.min(lowest, hub.getInputVoltage(VoltageUnit.VOLTS));
        }
        return hubs.isEmpty() ? 0.0 : lowest;
    }

    /** Total current in amps drawn through all hubs (motors, servos and sensors together). */
    public double getTotalAmps() {
        double total = 0.0;
        for (LynxModule hub : hubs) {
            total += hub.getCurrent(CurrentUnit.AMPS);
        }
        return total;
    }

    /** True when the battery is getting low. */
    public boolean isBatteryLow() {
        return getBatteryVolts() < LOW_BATTERY_VOLTS;
    }

    /** Nothing to stop; here so Robot can treat every subsystem the same. */
    public void stop() {
    }

    /** Show this subsystem's status on the Driver Station. */
    public void addTelemetry(Telemetry telemetry) {
        telemetry.addData("Battery (V)", "%.2f%s", getBatteryVolts(), isBatteryLow() ? "  LOW - swap battery" : "");
        telemetry.addData("Current (A) total", "%.2f", getTotalAmps());
        for (LynxModule hub : hubs) {
            telemetry.addData("  " + hub.getDeviceName(), "%.2f V  %.2f A",
                    hub.getInputVoltage(VoltageUnit.VOLTS), hub.getCurrent(CurrentUnit.AMPS));
        }
    }
}
