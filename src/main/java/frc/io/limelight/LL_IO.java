package frc.io.limelight;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.IO;

public class LL_IO {

    private static NetworkTable limeTable = IO.limelight;
    private static int ledmode, cammode, pipeline;

    public static boolean llHasTarget() {
        double valid = limeTable.getEntry("tv").getDouble(0);

        if (valid == 1) {
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
    public static void setLED(int choice) {
        limeTable.getEntry("ledMode").setNumber(choice);
    }

    //set vision (0) or driver mode (1)
    public static void setCamMode (int choice) {
        limeTable.getEntry("camMode").setNumber(choice);
    }

    public static void setPipeline(int choice) {
        limeTable.getEntry("pipeline").setNumber(choice);
    }

    public static void sdbUpdate() {
        SmartDashboard.putNumber("limelight x offset", getLLX());
        SmartDashboard.putNumber("limelight y offset", getLLY());
        SmartDashboard.putNumber("limelight percent area", getLLArea() * 100);

        SmartDashboard.getNumber("led mode", ledmode);
        SmartDashboard.getNumber("cam mode", cammode);
        SmartDashboard.getNumber("pipeline", pipeline);
    }

}