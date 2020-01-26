package frc.io.limelight;
/*
Original Author: Anthony & Kinfe
History:
11/6/2019 - Original release

TODO: Test

Desc: Reads data from the Limelight camera thru the networktables
    tx - pixel coordinate of X center
    ty - pixel coordinate of y center
    ta - area of id'ed target area
*/

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

// Limelight Class.
public class LL_IO{
    public static int jsConfig = 1;     //0=Joysticks, 1=left Joystick only, 2=gamePad only
                                        //3=Mixed LJS & GP, 4=Nintendo Pad

    // Constructor
    public LL_IO(){
        init();
    }

    public static void init(){
        SmartDashboard.putNumber("JS_Config", jsConfig);
    }

    //can put this under a button press
    public static void update() {   //Chk for Joystick configuration
        if(jsConfig != SmartDashboard.getNumber("JS_Config", 0)){
            jsConfig = (int)SmartDashboard.getNumber("JS_Config", 0);
        }

    switch( jsConfig ){
        case 0:     // Normal 3 joystick config
        break;

        case 1:     // Gamepad only
        break;

        case 2:     // Left joystick only
        break;

        case 3:     // 1 Joystick & Gamepad
        break;
        case 4:     // Nintendo Gamepad
        break;

        default:    // Bad assignment
        break;

        }
    }

    //================ Controller actions ================

}