package frc.subsystem;
/*
Author: Team 86
History: 
jch - 1/2020 - Original Release

TODO: - Need to add Limelight control.

Desc.
The turret rotates the turret to track a target using CV Limelight.  Can be manually controlled.
A pot is used to limit rotation from -135 to 135 with 0 being forward and setpoint control.

Sequence:
(0)Default, the motor is set to 0.0, off.
(1)JS used to manually rotate.
(2)If a POV is pressed switch to setpoint control, for testing. 0/45/90/.../315
(3)Chgs SP to 0 then rotates forward.
*/

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.Victor;

import frc.io.hdw_io.IO;
import frc.io.joysticks.JS_IO;
import frc.io.limelight.LL_IO;
import frc.util.BotMath;
import frc.util.timers.OnDly;
import frc.util.timers.OnOffDly;

public class Turret {
    private static Victor turret = IO.turret;
    // private static AnalogInput turretPot = IO.turretPot;

    private static double turretPct = 0.7; // Used as -/+ limit
    private static double turretFB = 0.0; // Scaled turret pot to degrees
    private static double turretSP = 0.0; // SP degrees -135 to 135, 0 forward
    private static double prvturPov = 0.0;// SP degrees -135 to 135, 0 forward
    private static int turSeqCntr = 0;

    private static int state;
    private static int prvState;

    private static int llState = 0;

    public static int shooterReq = 0;           // Test, Request shoot to rpm
    public static boolean lifterReq = false;    // Test, Request lifter

    // Constructor
    public Turret() {
        init();
    }

    public static void init() {
        llState = 0;
        cmdUpdate(0.0);
        state = 0;
    }

    // I am the determinator
    private static void determ() {
        if(JS_IO.shooterRun.onButtonPressed()) state = 3;   //GP6
        if(JS_IO.shooterStop.onButtonPressed()) state = 0;  //GP5
        if (JS_IO.turretJSDir.get()) state = 1; //GP7, Ctl by JSs
        if (JS_IO.turretLLProp.get()) state = 3;    //GP8, Ctl Prop to LL
        if (JS_IO.turretLLDB.get()) state = 4;      //GP10, Ctl to LL fixed spd w/DB

        if (!JS_IO.turretSP.isNone()) {         //POV pressed switch to POV SP
            //QnD Debounce for the pov.
            if(JS_IO.turretSP.get() != prvturPov){
                prvturPov = JS_IO.turretSP.get();
            }else{
                turretSP = JS_IO.turretSP.get();
            }
            if( turretSP > 180) turretSP -= 360.0;  // Should be -135 to 180 (limit -90 t0 90)
            state = turretSP == 180.0 ? 0 : 2;      // If 180 pressed go to 0
        }

        //Test btn when pressed starts the turret/shooter/lifter sequence.
        //This allows to push bot and control shooting.
        //1 prs target & idle shooter, 2 prs shtr to sp, 3 prs lift, 4 prs back to 0ff
        if( JS_IO.shooterStop.get()) turSeqCntr = 10;
        switch( turSeqCntr){
            case 0:
            break;
            case 1:
                shooterReq = LL_IO.llHasTarget() ? 2 : 1;   // at SP else idle
            break;
            case 2:
                lifterReq = LL_IO.llHasTarget() && Shooter.isAtSpd();
            break;
            default:
                shooterReq = 0;
                lifterReq = false;
                turSeqCntr = 0;
                state = 0;
            break;
        }
    }

    public static void update() {
        sdbUpdate();
        determ();
        // ------------- Main State Machine --------------
        // cmd update( shooter speed )
        switch (state) {
        case 0: // Default, mtr=0.0
            turretPct = 0.0;
            cmdUpdate(turretPct);
            prvState = state;
            break;
        case 1: // Control with JS
            turretPct = -JS_IO.turretRot.get(); // Neg = CW, Pos = CCW
            cmdUpdate(turretPct);
            prvState = state;
            break;
        case 2: // Control pov sp & to pot fb
            cmdUpdate(propCtl(turretSP, turretFB, -20.0 ));
            prvState = state;
            break;
        case 3: // SP = 0.  Now ctl Prop to LL
            turretSP = 0.0;
            if (LL_IO.llHasTarget()) {
                cmdUpdate(propCtl(0.0, LL_IO.getLLX(), 15.0 ));
            }
            prvState = state;
            break;
        case 4: // limelight control
            if(!(LL_IO.llOnTarget() == null)) {
                cmdUpdate(LL_IO.llOnTarget(3.0) * 0.5);
            }
            break;
        default: // mtr off
            cmdUpdate(0.0);
            prvState = state;
            System.out.println("Bad Turret state - " + state);
            break;
        }
    }

    // Smartdashboard shtuff
    private static void sdbUpdate() {
        SmartDashboard.putNumber("Turret Spd", turretPct);
        SmartDashboard.putNumber("Turret SP", turretSP);
        SmartDashboard.putNumber("Turret FB", turretFB);
        SmartDashboard.putNumber("Turret CCW ES", IO.turretCCWCntr.get());
        SmartDashboard.putNumber("Turret CW ES", IO.turretCWCntr.get());
        SmartDashboard.putNumber("turret state", state);
        SmartDashboard.putNumber("turret LLX", LL_IO.getLLX());
        SmartDashboard.putNumber("tur seq cntr", turSeqCntr);

        turretFB = IO.turretPot.get();
    }

    // Send commands to turret motor
    private static void cmdUpdate(double spd) {
        if ((IO.turretCCWCntr.get() > 0 || turretFB > 80.0) && spd < 0)
            spd = 0;
        if ((IO.turretCWCntr.get() > 0 || turretFB < -80.0) && spd > 0)
            spd = 0;
        SmartDashboard.putNumber("Turret Spd Out", spd);
        turret.set(spd);
        if (spd > 0.3)
            IO.turretCCWCntr.reset();
        if (spd < -0.3)
            IO.turretCWCntr.reset();
    }

    // Returns proportional response. Poorman's P Loop
    public static double propCtl(double sp, double fb, double pb) {
        double err = fb - sp;
        if (Math.abs(err) > 1.0) {  //Calc when in DB
            err = BotMath.Span(err, -pb, pb, 1.0, -1.0, true, false);
            return Math.abs(err) > 0.2 ? err : err > 0.0 ? 0.2 : -0.2;  //Min spd when in DB
        }
        return 0.0;
    }

    // Returns if motor is off.
    public static boolean get() {
        return turret.get() < 0.1;
    }
}