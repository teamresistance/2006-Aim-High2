package frc.io.hdw_io;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import frc.io.joysticks.JS_IO;

import com.revrobotics.ColorSensorV3;

/* temp to fill with latest faults */
import com.ctre.phoenix.motorcontrol.*;

import edu.wpi.first.wpilibj.SPI;
import com.kauailabs.navx.frc.AHRS;

public class IO {
    // Drive
    public static Victor drvMotor_L = new Victor(2);
    public static Victor drvMotor_R = new Victor(3);
    //Banner sensors shooting thru holes.  Not quad, no direction. Apply sign of power to get direction.
    public static Counter whlEnc_L = new Counter(2);    //Banner sensor shooting thru holes
    public static Counter whlEnc_R = new Counter(3);    //Banner sensor shooting thru holes
    private static double distFPP_L = 0.39; //3.14 * 0.5 / 4; //0.39 est for 6" whl
    private static double distFPP_R = 0.39; //3.14 * 0.5 / 4; //0.39 est for 6" whl

    // navX
    // public static NavX navX = new NavX();
    public static AHRS ahrs = new AHRS(SPI.Port.kMXP);

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
        drvMotor_L.setInverted(false);
        drvMotor_R.setInverted(true);
        whlEnc_L.setDistancePerPulse(distFPP_L);
        whlEnc_R.setDistancePerPulse(distFPP_R);
        whlEnc_L.reset();
        whlEnc_R.reset();

        ahrs.reset();
        shooter.setInverted(false);     //Inverts motor direction and encoder if attached
        shooter.setSensorPhase(false);  //Adjust this to correct phasing with motor
        turret.setInverted(false);
        lifter.setInverted(false);
    }

    public static void update() {
        // whlEnc_L.setReverseDirection(drvMotor_L.get() < 0.0);
        // whlEnc_R.setReverseDirection(drvMotor_R.get() < 0.0);

        SmartDashboard.putNumber("Drv L Cmd", drvMotor_L.get());
        SmartDashboard.putNumber("Drv R Cmd", drvMotor_R.get());
        SmartDashboard.putNumber("Enc Dist L", IO.whlEnc_L.getDistance());
        SmartDashboard.putNumber("Enc Dist R", IO.whlEnc_R.getDistance());
        SmartDashboard.putNumber("Shooter Amps", pdp.getCurrent(0));
        SmartDashboard.putNumber("Turret Pot", turretPot.get());

        // ------- Shooter Talon pidf control setup -------------
        /* check our live faults */
        shooter.getFaults(_faults);
    }
}
