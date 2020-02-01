package frc.io.joysticks;
/*
Original Author: Joey & Anthony
Rewite Author: Jim Hofmann
History:
J&A - 11/6/2019 - Original Release
JCH - 11/6/2019 - Original rework

TODO: Exception for bad or unattached devices.
      Auto config based on attached devices and position?
      Add enum for jsID & BtnID?  Button(eLJS, eBtn6) or Button(eGP, eBtnA)

Desc: Reads joystick (gamePad) values.  Can be used for different stick configurations
    based on feedback from Smartdashboard.  Various feedbacks from a joystick are
    implemented in classes, Button, Axis & Pov.

    This version is using named joysticks to istantiate axis, buttons & axis
*/

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import frc.io.joysticks.Axis;
import frc.io.joysticks.Button;
import frc.io.joysticks.Pov;

//Declares all joysticks, buttons, axis & pov's.
public class JS_IO {
    public static int jsConfig = 1; // 0=Joysticks, 1=left Joystick only, 2=gamePad only
                                    // 3=Mixed LJS & GP, 4=Nintendo Pad
    // Declare all possible Joysticks
    public static Joystick leftJoystick = new Joystick(0);  // Left JS
    public static Joystick rightJoystick = new Joystick(1); // Right JS
    public static Joystick coJoystick = new Joystick(2);    // Co-Dvr JS
    public static Joystick gamePad = new Joystick(3);       // Normal mode only (not Dual Trigger mode)
    public static Joystick neoPad = new Joystick(4);        // Nintendo style gamepad
    public static Joystick arJS[] = { leftJoystick, rightJoystick, coJoystick, gamePad, neoPad };
    // Declare all stick control
    public static Axis leftDrive = new Axis();  // Left Drive
    public static Axis rightDrive = new Axis(); // Right Drive
    public static Axis turretRot = new Axis();  // Rotate turret

    // Turret buttons
    public static Button shooterRun = new Button(); // Run shooter run (trigger) else idle
    public static Button shooterStop = new Button();// Stop shooter & reset

    public static Button turretJSDir = new Button();    // Directly rotate with JS
    public static Button turretLLDB = new Button();     //Rotate turret with LL using fixed spd w/ DB
    public static Button turretLLProp = new Button();   //Rotate turret prop using LL
    public static Pov turretSP = new Pov(); // Rotate by Pot SP with JS 0/45/90/.../315 minus 180

    public static Button lifterUp = new Button();       // Run motor to lift balls
    public static Button lifterDn = new Button();       // Run motor to lower balls

    // Constructor
    public JS_IO() {
        init();
    }

    // Initial joystick
    public static void init() {
        SmartDashboard.putNumber("JS_Config", jsConfig);
        configJS();
    }

    // If jsConfig != sdb then switch config
    public static void update() { // Chk for Joystick configuration
        if (jsConfig != SmartDashboard.getNumber("JS_Config", 0)) {
            jsConfig = (int) SmartDashboard.getNumber("JS_Config", 0);
            configJS();
        }
    }

    public static void configJS() { // Default Joystick else as gamepad
        switch (jsConfig) {
        case 0: // Normal 3 joystick config
            Norm3JS();
            break;

        case 1: // Gamepad only
            A_GP();
            break;

        case 2: // Left joystick only
            A_JS();
            break;

        case 3: // 1 Joystick & Gamepad
            JS_GP();

        case 4: // Nintendo Gamepad
            NeoGP();
            break;

        default: // Bad assignment
            CaseDefault();
            break;

        }
    }

    // ================ Controller actions ================

    // ----------- Normal 3 Joysticks -------------
    private static void Norm3JS() {

        // All stick axisesssss
        leftDrive = new Axis(leftJoystick, 1);
        rightDrive.setAxis(rightJoystick, 1);
        turretRot.setAxis(coJoystick, 0);

        // Turret buttons
        shooterRun.setButton(rightJoystick, 1);
        shooterStop.setButton(rightJoystick, 6);
        lifterUp.setButton(rightJoystick, 3);
        lifterDn.setButton(rightJoystick, 5);

        turretJSDir.setButton(coJoystick, 7);
        turretSP.setPov(coJoystick, 0);
        turretLLProp.setButton(coJoystick, 9);
        turretLLDB.setButton(coJoystick, 8);

    }

    // ----- gamePad only --------
    private static void A_GP() {

        // All stick axisesssss
        leftDrive.setAxis(gamePad, 1);
        rightDrive.setAxis(gamePad, 5);
        turretRot.setAxis(gamePad, 4); // Neg = CW, Pos = CCW

        // Turret buttons
        shooterRun.setButton(gamePad, 6);   //Run shooter at pct or rpm sp, select by sdb
        shooterStop.setButton(gamePad, 5);  //Stop the shooter

        lifterUp.setButton(gamePad, 4);
        lifterDn.setButton(gamePad, 1);

        turretLLDB.setButton(gamePad, 10);  //Rotate turret with LL using fixed spd w/ DB
        turretLLProp.setButton(gamePad, 8); //Rotate turret prop using LL
        turretJSDir.setButton(gamePad, 7);  //Rotate turret JS, GP axis4
        turretSP.setPov(gamePad, 0);        //Rotate turret to pov setpt with pot fb.
    }

    // ------------ One Joystick only -----------
    private static void A_JS() {

        CaseDefault(); // Too lazy to assign buttons
    }

    // ----- Mixed Left Joystick & gamePad only --------
    private static void JS_GP() {

        A_GP(); // Too lazy to assign buttons
    }

    // ----- Nintendo gamePad only --------
    private static void NeoGP() {

        CaseDefault(); // Not enough buttons to use this style
    }

    // ----------- Case Default -----------------
    private static void CaseDefault() {

        // All stick axisesssss
        leftDrive.setAxis(null, 0);
        rightDrive.setAxis(null, 0);
        turretRot.setAxis(null, 0);

        // Turret buttons
        shooterRun.setButton(null, 0);
        shooterStop.setButton(null, 0);
        lifterUp.setButton(null, 0);
        lifterDn.setButton(null, 0);

        turretJSDir.setButton(null, 0);
        turretSP.setPov(null, -1);
    }
}