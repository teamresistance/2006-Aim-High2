package frc.io.limelight;

import com.fasterxml.jackson.core.StreamWriteFeature;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.IO;

public class LL_IO {

    private static NetworkTable limeTable = IO.limelight;
    private static double ledmode = 0, cammode = 0, pipeline = 0;

    public static void init() {
        SmartDashboard.putNumber("led mode", ledmode);
        SmartDashboard.putNumber("cam mode", cammode);
        SmartDashboard.putNumber("pipeline", pipeline);
    }

    public static boolean llHasTarget() {
        double valid = limeTable.getEntry("tv").getDouble(0);

        if (valid == 1.0) {
            return true;
        } else {
            return false;
        }
    }

    public static double getLLX() {
        return limeTable.getEntry("tx").getDouble(0);
    }

    public static double getLLY() {
        return limeTable.getEntry("ty").getDouble(0);
    }

    public static double getLLArea() {
        return limeTable.getEntry("ta").getDouble(0);
    }

    //default of current pipeline (0), off (1), blinking? (2), on (3)
    public static void setLED() {
        limeTable.getEntry("ledMode").setNumber(ledmode);
    }

    //set vision (0) or driver mode (1)
    public static void setCamMode () {
        limeTable.getEntry("camMode").setNumber(cammode);
    }

    public static void setPipeline() {
        limeTable.getEntry("pipeline").setNumber(pipeline);
    }

    public static void sdbUpdate() {
        SmartDashboard.putNumber("limelight x offset", getLLX());
        SmartDashboard.putNumber("limelight y offset", getLLY());
        SmartDashboard.putNumber("limelight percent area", getLLArea() * 100);

        ledmode = SmartDashboard.getNumber("led mode", ledmode);
        cammode = SmartDashboard.getNumber("cam mode", cammode);
        pipeline = SmartDashboard.getNumber("pipeline", pipeline);
    }

}