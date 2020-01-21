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

import frc.io.js_btns.Axis;
import frc.io.js_btns.Button;
import frc.io.js_btns.Pov;

//Declares all joysticks, buttons, axis & pov's.
public class JS_IO{
    public static int jsConfig = 0;     //0=Joysticks, 1=left Joystick only, 2=gamePad only
                                        //3=Mixed LJS & GP, 4=Nintendo Pad
    // Declare all possible Joysticks
    public static Joystick leftJoystick = new Joystick(0);      // Left JS
    public static Joystick rightJoystick = new Joystick(1);     // Right JS
    public static Joystick coJoystick = new Joystick(2);        // Co-Dvr JS
    public static Joystick gamePad = new Joystick(3);           // Normal mode only (not Dual Trigger mode)
    public static Joystick neoPad = new Joystick(4);            // Nintendo style gamepad
    public static Joystick arJS[] = {leftJoystick,rightJoystick, coJoystick,
                                     gamePad, neoPad};
    // Declare all stick control
    public static Axis leftDrive;     // Left Drive
    public static Axis rightDrive;    // Right Drive
    public static Axis turretRot;     // Rotate turret

    // Turret buttons
    public static Button shooterRun;    // Run shooter (trigger) else idle
    public static Button shooterStop;   // Stop shooter
    public static Button turretCW;      // Turn turret CW
    public static Button turretCCW;     // Turn turret CCW
    public static Button lifterUp;      // Run motor to lift balls
    public static Button lifterDn;      // Run motor to lower balls
    public static Button turretJSDir;   // Directly rotate with JS
    public static Pov turretSP;         // Rotate by Pot SP with JS 0/45/90/.../315
    public static Button turretZero;    // Rotate forward

    // Shooter testing only on Norm3JS
    public static Button ptrShtrDiag = new Button(coJoystick, 11);

    // Constructor
    public JS_IO(){
        init();
    }

    public static void init(){
        SmartDashboard.putNumber("JS_Config", 0);
        configJS();
    }

    public static void update() {   //Chk for Joystick configuration
        if(jsConfig != SmartDashboard.getNumber("JS_Config", 0)){
            jsConfig = (int)SmartDashboard.getNumber("JS_Config", 0);
            configJS();
        }
    }

    public static void configJS() {   //Default Joystick else as gamepad
        jsConfig = (int)SmartDashboard.getNumber("JS_Config", 0);

        switch( jsConfig ){
            case 0:     // Normal 3 joystick config
                Norm3JS();
            break;

            case 1:     // Gamepad only
                A_GP();
            break;

            case 2:     // Left joystick only
                A_JS();
            break;

            case 3:     // 1 Joystick & Gamepad
                JS_GP();

            case 4:     // Nintendo Gamepad
                NeoGP();
            break;

            default:    // Bad assignment
                CaseDefault();
            break;

        }
    }

    //================ Controller actions ================

    // ----------- Normal 3 Joysticks -------------
    private static void Norm3JS(){

        // All stick axisesssss
        leftDrive = new Axis(leftJoystick, 1);
        rightDrive = new Axis(rightJoystick, 1);
        turretRot = new Axis(coJoystick, 0);

        // Turret buttons
        shooterRun = new Button(rightJoystick, 1);
        shooterStop = new Button(rightJoystick, 6);
        turretCW = new Button(rightJoystick, 12);
        turretCCW = new Button(rightJoystick, 11);
        lifterUp = new Button(rightJoystick, 3);
        lifterDn = new Button(rightJoystick, 5);

        turretJSDir = new Button(coJoystick, 7);
        turretSP = new Pov(coJoystick, 0);
        turretZero = new Button(coJoystick, 9);

        // Shooter testing only on Norm3JS
        ptrShtrDiag = new Button(coJoystick, 11);
    }

    // ----- gamePad only --------
    private static void A_GP(){

        // All stick axisesssss
        leftDrive = new Axis(gamePad, 1);
        rightDrive = new Axis(gamePad, 5);
        turretRot = new Axis(gamePad, 4);

        // Turret buttons
        shooterRun = new Button(gamePad, 6);
        shooterStop = new Button(gamePad, 9);
        turretCW = new Button(gamePad, 2);
        turretCCW = new Button(gamePad, 3);
        lifterUp = new Button(gamePad, 4);
        lifterDn = new Button(gamePad, 1);

        turretJSDir = new Button(gamePad, 7);
        turretSP = new Pov(gamePad, 0);
        turretZero = new Button(gamePad, 8);
        }

    // ------------ One Joystick only -----------
    private static void A_JS(){

        CaseDefault();  // Too lazy to assign buttons
    }

    // ----- Mixed Left Joystick & gamePad only --------
    private static void JS_GP(){

        A_GP();     // Too lazy to assign buttons
    }

    // ----- Nintendo gamePad only --------
    private static void NeoGP(){

        CaseDefault();  // Not enough buttons to use this style
    }

    // ----------- Case Default -----------------
    private static void CaseDefault(){

        // All stick axisesssss
        leftDrive = new Axis(null, 0);
        rightDrive = new Axis(null, 0);
        turretRot = new Axis(null, 0);

        // Turret buttons
        shooterRun = new Button(null, 0);
        shooterStop = new Button(null, 0);
        turretCW = new Button(null, 0);
        turretCCW = new Button(null, 0);
        lifterUp = new Button(null, 0);
        lifterDn = new Button(null, 0);

        turretJSDir = new Button(null, 0);
        turretSP = new Pov(null, -1);
        turretZero = new Button(null, 0);
    }
}