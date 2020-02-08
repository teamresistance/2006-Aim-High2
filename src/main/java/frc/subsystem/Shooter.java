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
(0) Default, the motor is set to 0.0 pct, off.
(1) Normal control, presently fixed value (0.7).
(2) When a ball first enters the shooter the additional load causes the shooter to slow down.  The
first ball may come out hot but other balls will come out short until the pid can compensate.  One
method is to bump the speed up until back to setpoint (or presently, just some time period).
(3) Once the shooter is upto speed idle it when not shooting.
*/

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.ControlMode;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import frc.io.hdw_io.IO;
import frc.io.joysticks.JS_IO;
import frc.util.timers.OnDly;
import frc.util.BotMath;

public class Shooter {
    private static TalonSRX shooter = IO.shooter;

    private static boolean shtrCtlRPM = false;  //Angle at 40 deg
    private static double rpm_WSP = 4050.0; // Working SP
    public static double rpm_SSP = 4050.0;  // Start SP
    public static double rpm_BSP = 6800.0;  // Boost SP
    public static double rpm_ISP = 2200.0;  // Idle SP
    public static double rpm_kP = 3.5;
    public static double rpm_kI = 0.0;
    public static double rpm_kD = 0.0;
    public static double rpm_kF = 1.92;
    public static double rpm_kB = 1.98;
    public static double rpm_FB = 0.0;

    private static boolean shtrCtlPMP = true;// Use Poorman's Prop
    private static double pct_WSP = 0.7;    // Working SP
    public static double pct_SSP = 0.45;     // Start SP
    public static double pct_BSP = 0.85;     // Boost SP
    public static double pct_ISP = 0.3;     // Idle SP
    public static double pct_PMP = 300.0;   // Poorman's PB

    public static double ampTgr = 15.0;     // Switch to boost xxx_BSP

    private static int state;
    private static int prvState;
    private static int prvShooterReq = 0;
    private static OnDly mtrRampUpDly = new OnDly(2000, 1);
    private static double tmpD = 0.0;

    // Constructor
    public Shooter() {
        init();
    }

    // Initialize
    public static void init() {
        shooter.enableVoltageCompensation(true);
        shooter.configVoltageCompSaturation(12,0);
        shooter.configVoltageMeasurementFilter(32,0);
        shooter.setSelectedSensorPosition(0,0,0);

        sdbInit();
        cmdUpdate(0.0, false);
        state = 0;
    }

    // I am the determinator
    // Note: shooterRun also triggers turret & lifter events
    private static void determ() {
        if(JS_IO.shooterRun.onButtonPressed()) state = 1;  //GP6, Shoot WSP
        // if(JS_IO.shooterRun.onButtonReleased()) state = 3; //GP6, Shoot ISP
        if(JS_IO.shooterStop.get()) state = 0;             //GP5, Stop

        if(state < 10 && !shtrCtlRPM) state += 10;    // Do pct SPs
        if(state == 11 && shtrCtlPMP) state = 14;   // Use Poorman's Prop
    }

    public static void update() {
        sdbUpdate();
        determ();
        // ------------- Main State Machine --------------
        // cmd update( shooter speed, ctl by rpm else pct)
        switch (state) {
        case 0: // Default, mtr=0.0 pct
            cmdUpdate(0.0, false);
            rpm_WSP = rpm_SSP;
            pct_WSP = pct_SSP;
            prvState = state;
            break;
        //--------- Handle RPM -----------
        case 1: // Shoot at default rpm
            cmdUpdate( rpm_WSP, true);

            // If slow switch to boost
            if( mtrRampUpDly.get(state == prvState)){   // Wait for motor ramp up
                if(rpm_FB < rpm_WSP - 500.0) state = 2;
                // if(IO.pdp.getCurrent(0) > ampTgr ) state = 2;      // Trigger on first ball
            }
            prvState = state;
            break;
        case 2: // Shooter rpm slow, bump to 100% to compensate
            cmdUpdate( rpm_BSP, true );

            //   If rpm high return to normal
            if( mtrRampUpDly.get(state == prvState)){   // Wait for motor ramp up
                if(rpm_FB > rpm_WSP + 500.0 ) state = 1;
            }
            prvState = state;
            break;
        case 3: // Shooter rpm idle
            cmdUpdate( rpm_ISP, true );
            prvState = state;
            break;
        //--------- Handle Pct -----------
        case 10: // Default, mtr=0.0 pct
            cmdUpdate(0.0, false);
            pct_WSP = pct_SSP;
            rpm_WSP = rpm_SSP;
            prvState = state;
            break;
        case 11: // Shoot at default pct
            cmdUpdate( pct_WSP, true);

            // Switch to boost on after first ball
            if( mtrRampUpDly.get(state == prvState)){   // Wait for motor ramp up
                if(IO.pdp.getCurrent(0) > ampTgr ) state = 2;      // Trigger on first ball
            }
            prvState = state;
            break;
        case 12: // Shooter slow, bump to 100% to compensate
            cmdUpdate( pct_BSP, false );
            prvState = state;
            break;
        case 13: // Shooter idle after shooting once
            cmdUpdate( pct_ISP, false );
            prvState = state;
            break;
        case 14: // Shooter ctld by Poorman's Prop
            tmpD = propCtl(rpm_WSP, rpm_FB, pct_PMP );
            cmdUpdate(tmpD, false);
            prvState = state;
            break;
        default: // Default, mtr=0.0
            cmdUpdate( 0.0, false );
            prvState = state;
            System.out.println("Bad Shooter state - " + state);
            break;
        }
    }

    // Send commands to shooter motor
    private static void cmdUpdate(double spd, boolean cmdVel) {
        if (cmdVel){    //Cmd must always be pos to rotate shooter correctly
            shooter.set(ControlMode.Velocity, Math.abs(spd) * 47 / 600);
        } else {
            shooter.set(ControlMode.PercentOutput, Math.abs(spd));
        }
        SmartDashboard.putNumber("Shtr Cmd Spd", spd);
    }

    // Returns proportional response. Poorman's P Loop
    public static double propCtl(double sp, double fb, double pb) {
        double err = fb - sp;   // Not used here
        if (Math.abs(err) > -1.0) {  //Calc when out of DB (always)
            err = BotMath.Span(fb, sp, (sp - pb), pct_SSP, pct_BSP, true, false);
            return err;
        }
        return 0.0;
    }

    // Returns if motor is off.
    public static boolean get() {
        return shooter.getMotorOutputPercent() < 0.1;
    }

    public static double getRpmFB(){
        return rpm_FB;
    }

    // Chk rpm if state 4 use rpm else pct.  May need to add OnOffDly 
    public static boolean isAtSpd(double rpm_db) {
        if(shtrCtlRPM)
            return Math.abs(rpm_WSP - rpm_FB) < rpm_db; //rpm_DB
        return Math.abs(shooter.getMotorOutputPercent() - pct_WSP) < 0.2;    //pct db
    }

    // Chk rpm if state 4 use rpm else pct.  May need to add OnOffDly 
    public static boolean isAtSpd() {
        return isAtSpd( 200 );
    }

    //Initialize Smartdashboard shtuff
    private static void sdbInit(){
        SmartDashboard.putNumber("Shooter State", state);
        SmartDashboard.putBoolean("Ctl RPM", shtrCtlRPM);   //Ctl to RPM else pct
        SmartDashboard.putNumber("Ball Amp Tgr", ampTgr);

        SmartDashboard.putNumber("Pct Workg SP", pct_WSP);
        SmartDashboard.putNumber("Pct Start SP", pct_SSP);
        SmartDashboard.putNumber("Pct Boost SP", pct_BSP);
        SmartDashboard.putNumber("Pct Idle SP", pct_ISP);
        SmartDashboard.putNumber("Pct PMs PB", pct_PMP);

        SmartDashboard.putNumber("RPM Workg SP", rpm_WSP);
        SmartDashboard.putNumber("RPM Start SP", rpm_SSP);
        SmartDashboard.putNumber("RPM Boost SP", rpm_BSP);
        SmartDashboard.putNumber("RPM Idle Spd", rpm_ISP);

        SmartDashboard.putNumber("RPM kP", rpm_kP);
        SmartDashboard.putNumber("RPM kI", rpm_kI);
        SmartDashboard.putNumber("RPM kD", rpm_kD);
        SmartDashboard.putNumber("RPM kF", rpm_kF);
        SmartDashboard.putNumber("RPM kB", rpm_kB);
    }
    // Update Smartdashboard shtuff
    private static void sdbUpdate() {
        SmartDashboard.putNumber("Shooter State", state);
        shtrCtlRPM = SmartDashboard.getBoolean("Ctl RPM", shtrCtlRPM);
        ampTgr = SmartDashboard.getNumber("Pct Amp Tgr", ampTgr);

        SmartDashboard.putNumber("Pct Workg SP", pct_WSP);
        pct_SSP = SmartDashboard.getNumber("Pct Start SP", pct_SSP);
        pct_BSP = SmartDashboard.getNumber("Pct Boost SP", pct_BSP);
        pct_ISP = SmartDashboard.getNumber("Pct Idle SP", pct_ISP);
        pct_PMP = SmartDashboard.getNumber("Pct PMs PB", pct_PMP);

        SmartDashboard.putNumber("RPM Workg SP", rpm_WSP);
        rpm_SSP = SmartDashboard.getNumber("RPM Start SP", rpm_SSP);
        rpm_BSP = SmartDashboard.getNumber("RPM Boost SP", rpm_BSP);
        rpm_ISP = SmartDashboard.getNumber("RPM Idle SP", rpm_ISP);

        rpm_kP = SmartDashboard.getNumber("RPM kP", rpm_kP);
        rpm_kI = SmartDashboard.getNumber("RPM kI", rpm_kI);
        rpm_kD = SmartDashboard.getNumber("RPM kD", rpm_kD);
        rpm_kF = SmartDashboard.getNumber("RPM kF", rpm_kF);
        rpm_kB = SmartDashboard.getNumber("RPM kF", rpm_kB);
        shooter.config_kP(0, rpm_kP);
        shooter.config_kI(0, rpm_kI);
        shooter.config_kD(0, rpm_kD);
        shooter.config_kF(0, rpm_kF);

        rpm_FB = shooter.getSelectedSensorVelocity() * 600 / 47;
        SmartDashboard.putNumber("enc position", shooter.getSelectedSensorPosition());
        SmartDashboard.putNumber("enc velocity", shooter.getSelectedSensorVelocity());
        SmartDashboard.putNumber("RPM", rpm_FB);
        SmartDashboard.putNumber("MtrOutPct", (shooter.getMotorOutputPercent()));
        SmartDashboard.putNumber("prv shtr req", prvShooterReq);
    }
}
