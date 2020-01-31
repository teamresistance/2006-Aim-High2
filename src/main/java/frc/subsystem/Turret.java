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

    private static int state;
    private static int prvState;

    private static int llState = 0;

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
        if (JS_IO.turretJSDir.get())        //GP7
            state = 1;
        if (!JS_IO.turretSP.isNone())       //Pov
            state = 2; // If POV pressed switch to POV SP
        if (JS_IO.turretZero.get())         //GP8, Ctl Prop to LL
            state = 3;
        if (JS_IO.llControl.get())          //GP10, Ctl to LL fixed spd
            state = 4;
    }

    public static void update() {
        sdbUpdate();
        determ();
        // turretPotUpd();
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
            // prvState = state;
            break;
        case 2: // Control position to LL
            turretSP = JS_IO.turretSP.get() - 180.0;
            cmdUpdate(propCtl(turretSP, turretFB));
            prvState = state;
            break;
        case 3: // Was Zero Position, SP = 0.  Now ctl Prop to LL
            turretSP = 0.0;
            if (LL_IO.llHasTarget()) {
                cmdUpdate(propCtl(0.0, LL_IO.getLLX()));
            }
            prvState = state;
            break;
        case 4: // limelight control
            if (LL_IO.llHasTarget()) {
                if (LL_IO.getLLX() > 3 ) {
                    cmdUpdate(-0.5);
                } else if (LL_IO.getLLX() < -3) {
                    cmdUpdate(0.5);
                } else if (LL_IO.getLLX() < 3 && LL_IO.getLLX() > -3) {
                    cmdUpdate(0);
                }
            } else {
                cmdUpdate(0);
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
    }

    // Send commands to turret motor
    private static void cmdUpdate(double spd) {
        turretFB = IO.turretPot.get();
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

    // Scale turret pot
    /*
     * private static void turretPotUpd(){ turretFB =
     * BotMath.Span(turretPot.getAverageVoltage(), 0.0, 5.0, -135.0, 135.0, false,
     * false); }
     */
    // Returns proportional response. Poorman's P Loop
    public static double propCtl(double sp, double fb) {
        double err = fb - sp;
        if (Math.abs(err) > 1.0) {  //Calc when in DB
            err = BotMath.Span(err, -25.0, 25.0, 1.0, -1.0, true, false);
            return Math.abs(err) > 0.2 ? err : err > 0.0 ? 0.2 : -0.2;  //Min spd when in DB
        }
        return 0.0;
    }

    // Returns if motor is off.
    public static boolean get() {
        return turret.get() < 0.1;
    }

    public static boolean isAtSpd() {
        return true;
    }

    private static double limelightCtl() {

        return 69;
    }

}
