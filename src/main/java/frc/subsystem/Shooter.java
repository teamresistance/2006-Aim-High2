package frc.subsystem;
/*
Author: Team 86
History: 
jch - 1/2020 - Original Release

TODO: - Need to add pid control with encoder

Desc.
The shooter spins a wheel to shoot a 7" ball at approx. 35 fps.  It starts when a button is pressed
and stop on the press of another button.
Presently, the speed is fixed, set thru the sdb.
Future, Control the speed of the shooter to a setpoint thru a pid loop.  Best guess is approx.
3300 rpm.  This requires an encoder on the wheel for feedback.
Future future, change the victor to a TalonSRX and move the pid to the Talon.

Sequence:
(0) Default, the motor is set to 0.0, off.
When button is pressed the shooter is set to 0.0.
(1) Normal control, presently fixed value (0.7), future pid loop encoder.
(2) When a ball first enters the shooter the additional load causes the shooter to slow down.  The
first ball may come out hot but other balls will come out short until the pid can compensate.  One
method is to bump the speed up until back to setpoint (or presently, just some time period).
(3) Once the shooter is upto speed idle it when not shooting.
*/

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.ControlMode;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import frc.io.hdw_io.IO;
import frc.io.joysticks.JS_IO;
import frc.util.timers.OnDly;
import frc.util.timers.OnOffDly;

public class Shooter {
    private static TalonSRX shooter = IO.shooter;

    private static double shooterPct = -0.7;
    private static double shtrIdlePct = -0.3;

    public static double kP = 55;
    public static double kI = 0.0;
    public static double kD = 0.0;
    public static double kF = 1.47;
    public static double setpoint = -415;

    private static int state;
    private static int prvState;

    // Constructor
    public Shooter() {
        init();
    }

    public static void init() {
        shooter.enableVoltageCompensation(true);
        shooter.configVoltageCompSaturation(12,0);
        shooter.configVoltageMeasurementFilter(32,0);
        
        SmartDashboard.putNumber("Shooter Spd", shooterPct);
        SmartDashboard.putNumber("Shtr Idle Spd", shtrIdlePct);
        SmartDashboard.putNumber("Shooter State", state);
        shooter.setSelectedSensorPosition(0);
        cmdUpdate(0.0, false);
        state = 0;

        SmartDashboard.putNumber("kP", kP);
        SmartDashboard.putNumber("kI", kI);
        SmartDashboard.putNumber("kD", kD);
        SmartDashboard.putNumber("kF", kF);
        SmartDashboard.putNumber("setpoint", setpoint);


     //   shooter.config_kP(0, kP);
      //  shooter.config_kI(0, kI);
      //  shooter.config_kD(0, kD);
 
    }

    // I am the determinator
    /*TODO: Suggest, GP6=ShtrRunPct, GP5=ShtrRunPID,
     combine stop & reset=GP9.
    */
    private static void determ() {
        if (JS_IO.shooterRun.get())     //GP6
            state = 1;
        if (JS_IO.shooterStop.get())    //GP5
            state = 0;
        if (JS_IO.shooterTest.get())    //GP10
            state = 4;
        if(JS_IO.shooterReset.get())    //GP9
            state = 0;
            shooter.setSelectedSensorPosition(0,0,0);
    }

    public static void update() {
        sdbUpdate();
        determ();
        // ------------- Main State Machine --------------
        // cmd update( shooter speed, ctl by rpm else pct)
        switch (state) {
        case 0: // Default, mtr=0.0
            cmdUpdate(0.0, false);
            prvState = state;
            break;
        case 1: // Shoot at default speed
            cmdUpdate(shooterPct, false);
            prvState = state;
            if (!JS_IO.shooterRun.get())
                state = 3;
            break;
        case 2: // Shooter slow, bump to compensate
            cmdUpdate(1, false);
            prvState = state;
            break;
        case 3: // Shooter idle after shooting once
            cmdUpdate(shtrIdlePct, false);
            prvState = state;
            break;
        case 4: // PID control
            //cmdUpdate(((setpoint * 47 ) / 600), true);
            
            cmdUpdate(setpoint, true);
            //shooter.set(ControlMode.Velocity, 200);
            prvState = state;
            break;
        default: // Default, mtr=0.0
            cmdUpdate(0.0, false);
            prvState = state;
            System.out.println("Bad Shooter state - " + state);
            break;
        }
    }

    // Smartdashboard shtuff
    private static void sdbUpdate() {
        shooterPct = SmartDashboard.getNumber("Shooter Spd", shooterPct);
        shtrIdlePct = SmartDashboard.getNumber("Shtr Idle Spd", shtrIdlePct);
        SmartDashboard.putNumber("Shooter State", state);
        SmartDashboard.putNumber("encoder pos", shooter.getSelectedSensorPosition());
        SmartDashboard.putNumber("enc velocity", shooter.getSelectedSensorVelocity());
        SmartDashboard.putNumber("RPM", (shooter.getSelectedSensorVelocity() * 600) / 47);
        SmartDashboard.putNumber("MtrOutPct", (shooter.getMotorOutputPercent() * 600) / 47);

        setpoint = SmartDashboard.getNumber("setpoint", setpoint);
        kP = SmartDashboard.getNumber("kP", kP);
        kI = SmartDashboard.getNumber("kI", kI);
        kD = SmartDashboard.getNumber("kD", kD);
        kF = SmartDashboard.getNumber("kF", kF);
        shooter.config_kP(0, kP);
        shooter.config_kI(0, kI);
        shooter.config_kD(0, kD);
        shooter.config_kF(0, kF);
    }

    // Send commands to shooter motor
    private static void cmdUpdate(double spd, boolean controlWithPID) {
        if (controlWithPID){
            shooter.set(ControlMode.Velocity, spd);
            System.out.println("Shtr Spd out - " + spd);
        } else {
            shooter.set(ControlMode.PercentOutput, spd);
        }
        SmartDashboard.putNumber("Shtr Cmd Spd", spd);
    }

    // Returns if motor is off.
    public static boolean get() {
        return shooter.getMotorOutputPercent() < 0.1;
    }

    // TODO: Need to add code once we have the Talon encoder reading.
    public static boolean isAtSpd() {
        return true;
    }
}
