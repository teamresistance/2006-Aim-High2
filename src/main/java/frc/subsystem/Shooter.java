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

import javax.lang.model.util.ElementScanner6;

import com.ctre.phoenix.motorcontrol.ControlMode;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import frc.io.hdw_io.IO;
import frc.io.joysticks.JS_IO;
import frc.util.timers.OnDly;
import frc.util.timers.OnOffDly;

public class Shooter {
    private static TalonSRX shooter = IO.shooter;

    private static boolean shtrCtlRPM = true;
    private static double pct_SP = 0.7;
    private static double pctIdleSP = 0.3;

    public static double rpm_SP = 4400.0;
    public static double rpmIdleSP = 1000.0;
    public static double rpm_kP = 55;
    public static double rpm_kI = 0.0;
    public static double rpm_kD = 0.0;
    public static double rpm_kF = 1.47;
    public static double rpm_FB = 0.0;

    private static int state;
    private static int prvState;
    private static int prvShooterReq = 0;

    // Constructor
    public Shooter() {
        init();
    }

    // Initialize
    public static void init() {
        shooter.enableVoltageCompensation(true);
        shooter.configVoltageCompSaturation(12,0);
        shooter.configVoltageMeasurementFilter(32,0);

        shooter.setSelectedSensorPosition(0);
        cmdUpdate(0.0);
        state = 0;

        SmartDashboard.putBoolean("Ctl RPM", shtrCtlRPM);   //defaults to RPM
        SmartDashboard.putNumber("RPM SP", rpm_SP);
        SmartDashboard.putNumber("RPM Idle Spd", rpmIdleSP);
        SmartDashboard.putNumber("Pct SP", pct_SP);
        SmartDashboard.putNumber("Pct Idle Spd", pctIdleSP);
        SmartDashboard.putNumber("Shooter State", state);

        SmartDashboard.putNumber("RPM kP", rpm_kP);
        SmartDashboard.putNumber("RPM kI", rpm_kI);
        SmartDashboard.putNumber("RPM kD", rpm_kD);
        SmartDashboard.putNumber("RPM kF", rpm_kF);
        SmartDashboard.putNumber("RPM SP", rpm_SP);
    }

    // I am the determinator
    private static void determ() {
        if (JS_IO.shooterRun.onButtonReleased()) state = 1;  //GP6, Shoot or idle
        if (JS_IO.shooterStop.get()) state = 0; //GP5, Stop
        shooter.setSelectedSensorPosition(0,0,0);
            
        //shooterReq 0-off, 1=idle, 2=sp
        if(Turret.shooterReq != prvShooterReq){
            prvShooterReq = Turret.shooterReq;
            switch(prvShooterReq){
                case 0:
                    state = 0;
                break;
                case 1:
                    state = 3;
                break;
                case 2:
                    state = 1;
                break;
            }
        }
    }

    public static void update() {
        sdbUpdate();
        determ();
        // ------------- Main State Machine --------------
        // cmd update( shooter speed, ctl by rpm else pct)
        switch (state) {
        case 0: // Default, mtr=0.0
            cmdUpdate(0.0);
            prvState = state;
            break;
        case 1: // Shoot at default rpm else percent
            cmdUpdate( shtrCtlRPM ? rpm_SP : pct_SP );
            // if( rpm_FB - rpm_SP < -200) state =2;
            prvState = state;
            if (JS_IO.shooterRun.onButtonReleased())
                state = 3;
            break;
        case 2: // Shooter slow, bump to 100% to compensate
            cmdUpdate( shtrCtlRPM ? 6800.0 : 1.0 );
            // if( rpm_FB - rpm_SP > 100) state =1;
            if (!JS_IO.shooterRun.get())
                state = 3;
            prvState = state;
            break;
        case 3: // Shooter idle after shooting once
            cmdUpdate( shtrCtlRPM ? rpmIdleSP : pctIdleSP );
            prvState = state;
            break;
        default: // Default, mtr=0.0
            cmdUpdate( 0.0 );
            prvState = state;
            System.out.println("Bad Shooter state - " + state);
            break;
        }
    }

    // Smartdashboard shtuff
    private static void sdbUpdate() {
        pct_SP = SmartDashboard.getNumber("Pct SP", pct_SP);
        pctIdleSP = SmartDashboard.getNumber("Pct Idle Spd", pctIdleSP);
        rpm_FB = shooter.getSelectedSensorVelocity() * 600 / 47;
        SmartDashboard.putNumber("Shooter State", state);
        SmartDashboard.putNumber("enc position", shooter.getSelectedSensorPosition());
        SmartDashboard.putNumber("enc velocity", shooter.getSelectedSensorVelocity());
        SmartDashboard.putNumber("RPM", rpm_FB);
        SmartDashboard.putNumber("MtrOutPct", (shooter.getMotorOutputPercent() * 600) / 47);    //???
        SmartDashboard.putNumber("prv shtr req", prvShooterReq);

        rpm_SP = SmartDashboard.getNumber("RPM SP", rpm_SP);
        rpmIdleSP = SmartDashboard.getNumber("RMP Idle SP", rpmIdleSP);
        rpm_kP = SmartDashboard.getNumber("RMP kP", rpm_kP);
        rpm_kI = SmartDashboard.getNumber("RMP kI", rpm_kI);
        rpm_kD = SmartDashboard.getNumber("RMP kD", rpm_kD);
        rpm_kF = SmartDashboard.getNumber("RMP kF", rpm_kF);
        shooter.config_kP(0, rpm_kP);
        shooter.config_kI(0, rpm_kI);
        shooter.config_kD(0, rpm_kD);
        shooter.config_kF(0, rpm_kF);
    }

    // Send commands to shooter motor
    private static void cmdUpdate(double spd) {
        if (shtrCtlRPM){    //Cmd must always be pos to rotate shooter correctly
            shooter.set(ControlMode.Velocity, Math.abs(spd) * 47 / 600);
        } else {
            shooter.set(ControlMode.PercentOutput, Math.abs(spd));
        }
        SmartDashboard.putNumber("Shtr Cmd Spd", spd);
    }

    // Returns if motor is off.
    public static boolean get() {
        return shooter.getMotorOutputPercent() < 0.1;
    }

    // Chk rpm if state 4 use rpm else pct.  May need to add OnOffDly 
    public static boolean isAtSpd() {
        if(state == 4)
            return Math.abs(rpm_SP - rpm_FB) < 200; //rpm_DB
        return Math.abs(shooter.getMotorOutputPercent() - pct_SP) < 0.2;    //pct db
    }
}
