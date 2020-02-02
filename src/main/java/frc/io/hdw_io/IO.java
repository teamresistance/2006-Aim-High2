package frc.io.hdw_io;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.interfaces.Potentiometer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import edu.wpi.first.networktables.*;

import frc.io.joysticks.JS_IO;

import com.revrobotics.ColorSensorV3;

/* temp to fill with latest faults */
import com.ctre.phoenix.motorcontrol.*;
import com.ctre.phoenix.motorcontrol.can.*;

import com.kauailabs.navx.frc.AHRS;

public class IO {
    // navX
    // public static NavX navX = new NavX();
    public static AHRS ahrs;

    // PDP
    public static PowerDistributionPanel pdp = new PowerDistributionPanel(1);

    // Turret
    public static Victor lifter = new Victor(0);
    public static Victor turret = new Victor(1);
    public static TalonSRX shooter = new TalonSRX(12);

    // public static Encoder shooterRPM = new Encoder(0, 1);
    public static AnalogPotentiometer turretPot = new AnalogPotentiometer(0, 185.0, -90.0);

    public static Counter turretCCWCntr = new Counter(0); // CCW counter
    public static Counter turretCWCntr = new Counter(1); // CW Counter
    // Test button.  Allows me to push bot around w/o computer, GP.
    // Press once to start shooter & LL target. Next press starts feeder
    // Next press stop all?
    public static Counter turretTestCntr = new Counter(9);

    // ---------- WoF, Color Sensor -----------------
    /**
     * Change the I2C port below to match the connection of your color sensor
     */
    private static final I2C.Port i2cPort = I2C.Port.kOnboard;

    /**
     * A Rev Color Sensor V3 object is constructed with an I2C port as a parameter.
     * The device will be automatically initialized with default parameters.
     */
    public static ColorSensorV3 m_colorSensor = new ColorSensorV3(i2cPort);

    // ----------- Talon pidf control ------------------
    public static Faults _faults = new Faults(); /* temp to fill with latest faults */

    public static void init() {
        shooter.setInverted(false);     //Inverts motor direction and encoder if attached
        shooter.setSensorPhase(false);  //Adjust this to correct phasing with motor
        turret.setInverted(false);
        lifter.setInverted(false);
    }

    public static void update() {
        SmartDashboard.putNumber("Shooter Amps", pdp.getCurrent(0));
        SmartDashboard.putNumber("Turret Pot", turretPot.get());

        // ------- Shooter Talon pidf control setup -------------
        /* check our live faults */
        shooter.getFaults(_faults);
    }
}
