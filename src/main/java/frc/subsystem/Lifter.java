package frc.subsystem;
/*
Author: Team 86
History: 
jch - 1/2020 - Original Release

TODO: - Need to test

Desc.
The lifter lifts the balls to the shooter.

Sequence:
(0)Default, the motor is set to 0.0, off, when no buttons pressed.
(1)Balls move up when up button is pressed.
(2)Balls move dn when dn button is pressed.
*/

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import edu.wpi.first.wpilibj.Victor;

import frc.io.hdw_io.IO;
import frc.io.joysticks.JS_IO;
import frc.io.limelight.LL_IO;
import frc.util.Timer;
import frc.util.timers.OnDly;

public class Lifter {
    private static Victor lifter = IO.lifter;

    private static double lifterPct = 0.30;
    private static double shtrRpm_db = 500;
    private static long shtrRpm_dly = 100;
    private static boolean lifterShtrSU = false;

    private static int state;
    private static int prvState;
    private static boolean prvLifterReq = false;
    private static int lifterEnaNum = 0;
    private static OnDly shOnDly = new OnDly( shtrRpm_dly, 2 );
    private static OnDly llOnDly = new OnDly( 250, 3 );

    //Constructor
    public Lifter() {
        init();
    }

    public static void init() {
        sdbInit();
        cmdUpdate(0.0);
        state = 0;
    }

    // I am the determinator
    private static void determ(){
        state = 0;
        if(JS_IO.lifterUp.get() || JS_IO.gp_LTgr.isDown()) state = 1;
        // if(JS_IO.lifterDn.get() || JS_IO.gp_RTgr.isDown()) state = 2;

        if (JS_IO.povLifter.is0()) state = 1;
        if (JS_IO.povLifter.is180()) state = 2;

        if(Shooter.getRpmFB() > Shooter.rpm_SSP - 1000.0) lifterShtrSU = true;
        if(lifterShtrSU){

            lifterEnaNum = getEnaNum();
            if(lifterEnaNum > 8) state = 1;
        }
    }

    public static void update() {
        sdbUpdate();
        determ();
        //------------- Main State Machine --------------
        // cmd update( shooter speed )
        switch(state){
        case 0: // Default, mtr=0.0
            cmdUpdate( 0.0 );
            prvState = state;
            lifterShtrSU = false;
            break;
        case 1: // Move balls up
            cmdUpdate( lifterPct );
            prvState = state;
            break;
        case 2: // Move balls down
            cmdUpdate( -lifterPct );
            prvState = state;
            break;
        default: // Bad state, mtr off
            cmdUpdate( 0.0 );
            prvState = state;
            System.out.println("Bad Lifter state - " + state);
            break;
        }
    }

    public static void aUpdate(int aState){
                //------------- Main State Machine --------------
        // cmd update( shooter speed )
        switch(aState){
            case 0: // Default, mtr=0.0
                cmdUpdate( 0.0 );
                prvState = state;
                lifterShtrSU = false;
                break;
            case 1: // Move balls up            
                if(LL_IO.llOnTarget() != null){
                if(Shooter.isAtSpd() && LL_IO.llOnTarget() == 0)
                cmdUpdate( lifterPct );
                     }   
                prvState = state;
                break;
            case 2: // Move balls down
                cmdUpdate( -lifterPct );
                prvState = state;
                break;
            default: // Bad state, mtr off
                cmdUpdate( 0.0 );
                prvState = state;
                System.out.println("Bad Lifter state - " + state);
                break;
            }
    }

    // Send commands to shooter motor
    private static void cmdUpdate(double spd){
        lifter.set(spd);
    }

    //Returns if motor is off.
    public static boolean get(){
        return Math.abs(lifter.get()) < -0.1;
    }

    // 1=JS btn pressed + 2=shooter above limit + 4=LL on target
    private static int getEnaNum(){
        int tmp = 0;
        if(JS_IO.shooterRun.get()) tmp += 1;
        // if(shOnDly.get(Shooter.getRpmFB() > 4400.0)) tmp += 0;
        if(shOnDly.get(Shooter.isAtSpd(shtrRpm_db))) tmp += 2;
        if(LL_IO.llOnTarget(3.0) != null){  //Do we see a target?
            if(llOnDly.get(LL_IO.llOnTarget(3.0) == 0)) tmp += 4;
        }
        return tmp;
    }

    // Initialize Smartdashboard shtuff
    private static void sdbInit(){
        SmartDashboard.putNumber("Lift Spd", lifterPct);
        SmartDashboard.putNumber("Lift Shtr DB", shtrRpm_db);
        SmartDashboard.putNumber("Lift Shtr Dly", shtrRpm_dly);
        SmartDashboard.putNumber("Shooter State", state);
       }

    // Update Smartdashboard shtuff
    private static void sdbUpdate(){
        lifterPct = SmartDashboard.getNumber("Lift Spd", 0.7);
        shtrRpm_db = (int) SmartDashboard.getNumber("Lift Shtr DB", shtrRpm_db);
        shtrRpm_dly = (int) SmartDashboard.getNumber("Lift Shtr Dly", shtrRpm_dly);

        SmartDashboard.putNumber("Lifter Ena Num", lifterEnaNum);
        SmartDashboard.putBoolean("Lifter SSU Ena", lifterShtrSU);
    }
}
