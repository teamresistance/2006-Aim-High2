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

    private static int state;
    private static int prvState;
    private static boolean prvLifterReq = false;
    private static OnDly llOnDly = new OnDly( 250 );

    //Constructor
    public Lifter() {
        init();
    }

    public static void init() {
        SmartDashboard.putNumber("Lift Spd", lifterPct);
        cmdUpdate(0.0);
        state = 0;
    }

    // I am the determinator
    private static void determ(){
        state = 0;
        if(JS_IO.lifterUp.get()) state = 1;
        if(JS_IO.lifterDn.get()) state = 2;

        if(JS_IO.shooterRun.get() &&
           Shooter.isAtSpd() &&
           LL_IO.llHasTarget() &&
           llOnDly.get(LL_IO.llOnTarget(3.0) == 0 )){
               
            state = 1;
        }

        if(Turret.lifterReq) state = 1;
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

    // Smartdashboard shtuff
    private static void sdbUpdate(){
        SmartDashboard.putBoolean("lifter req", Turret.lifterReq);
        lifterPct = SmartDashboard.getNumber("Lift Spd", 0.7);
    }

    // Send commands to shooter motor
    private static void cmdUpdate(double spd){
        lifter.set(spd);
    }

    //Returns if motor is off.
    public static boolean get(){
        return Math.abs(lifter.get()) < -0.1;
    }
}
