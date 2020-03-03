package frc.subsystem.drive;
/*
Original Author: Team 86
History:
JCH - 12/6/2019 - Original release

TODO: Checkout

Desc: 
Controls a differential style drive system.  Can switch between Tank & Arcade joysticks.
The maximum responce can be adjusted, scaled to the JSs -1 to 1 input.
A fixed angle can be held using the pov buttons.  Forward is still JS controlled.
The hold angle is calc'ed using a simple proportional calculation.

Sequence:
(0)Off
(1)When button pressed, use JS's to drive in Tank Drive mode.
(2)When button pressed, use JS's to drive in Arcade Drive mode.
(10)When pov is pressed hold angle as long as pressed else use R JS X
    Fwd is still controlled by the R JS Y.
    When not pressed same as (2).
(30)Auto drive per an array of "hdg, pwr & dist".
    Init steer method. Turn to hdg then go to 31.
(31)Steer to heading and distance.  When distance done go to 32.
(32)Increment Trajectory index.  Check for last one and go to 0, Off.
    Else go to 30, run next trajectory.
*/

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.kauailabs.navx.frc.AHRS;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
// import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import frc.io.hdw_io.IO;
import frc.io.joysticks.JS_IO;
import frc.util.BotMath;

//Class for taking Joystick inputs and turning them into speed values to move the robot
public class Drive {

    // Hardware
    private static Victor right = IO.drvMotor_L; // right motor
    private static Victor left = IO.drvMotor_R;  // left motor
    private static DifferentialDrive diffDrv = new DifferentialDrive(left, right);
    private static double dist_L = 0.0;
    private static double dist_R = 0.0;
    private static boolean prvRev_L = false;    // Direction is reverse else fwd 
    private static boolean prvRev_R = false;    // Direction is reverse else fwd 
    private static double prvEncDist_L = 0.0;     // prv encoder dist since last direction chg
    private static double prvEncDist_R = 0.0;     // prv encoder dist since last direction chg
    private static double psntEncDist_L = 0.0;     // prv encoder dist since last direction chg
    private static double psntEncDist_R = 0.0;     // prv encoder dist since last direction chg
    private static double dist_Avg = 0.0;

    // General
    private static int state = 0;       // 0=tank, 1=arcade
    private static int prvState = 0;    // 0=tank, 1=arcade
    private static double strCmd[] = {0.0, 0.0}; //Cmds returned, X, Y
    //Heading Control
    private static double hdgFB = 0.0;      //Gyro reading
    private static double hdgOut = 0.0;     //X (Hdg) output
    //Distance Control
    private static double distFB = 0.0;     //Dist reading
    private static double distOut = 0.0;    //Y (Fwd) cmd

    /*        [0][]=hdg [1][]=dist       SP,     PB,  DB,  Mn,  Mx, Xcl */
    private static double[][] parms = {{0.0,90.0, 3.0, 0.7, 1.0, 0.20},
    /*                             */  {0.0,  4.5, 0.5,  0.2, 0.8, 0.07}};
    private static Steer steer = new Steer(parms);  //Used to steer to a hdg with power for distance

    // Steer to heading at power for distance.
    private static int trajIdx = 0;  // strCmds Index
    //                                {hdg, %pwr, dist}
    private static double traj[][] = {{0.0, 100.0, 4.0},
    /*                           */   {90.0, 100.0, 0.0},
    /*                           */   {90.0, 100.0, 3.0},
    /*                           */   {-90.0, 100.0, 0.0},
    /*                           */   {-90.0, 100.0, 5.0},
    /*                           */   {0.0, 100.0, 0.0},
    /*                           */   {0.0, 100.0, -4.5}};
    // /*                           */   {0.0, 70.0, 5.0},
    // /*                           */   {-90.0, 70.0, 5.0},
    // /*                           */   {135.0, 70.0, 7.1},
    // /*                           */   {-90.0, 70.0, 5.0},
    // /*                           */   {0.0, 70.0, -0.5}};

    //Segmented curve to compensate for min required of 0.65 when rotating.  parm[1][3] is hdgOutMn.
    // private static double[][] xOutAr  = {{-1.0, -parms[0][3], -parms[0][3], parms[0][3], parms[0][3], 1.0},
    // /*                               */  {-1.0,        -0.35,          0.0,         0.0,        0.30, 1.0}};

    // Constructor
    public Drive() {
		init();
		IO.ahrs.reset();
    }

    // Initialize
    public static void init(){
		sdbInit();
		
    }

    // I am the determinator
    private static void determ(){
        //Set steering mode
        if(JS_IO.offMode.get()) state = 0;      //GP 1, A
        if(JS_IO.tankMode.get()) state = 1;     //GP 2, B
        if(JS_IO.arcadeMode.get()) state = 2;   //GP 3, X
        if(JS_IO.autoTest.onButtonPressed()){   //GP 4, Y
            state = 30;
            trajIdx = 0;
        }

        //Set Auto Hold Angle, 0, 45, 90, ... , 315.  Press a pov direction.
        if(!JS_IO.pov_SP.isNone()){
            state = 10;     //Set steering mode to hold angle
        }
    }

    // Update drive commands
    public static void update() {
        determ();
        sdbUpdate();
        stfUpdate();

        switch(state){
            case 0:     //Off
                diffDrv.tankDrive(0.0, 0.0, true);
                prvState = state;
                break;
            case 1:     //Tank Mode Drive by JS
                diffDrv.tankDrive(JS_IO.dvrLY.get(), JS_IO.dvrRY.get(), false);
                prvState = state;
                break;
            case 2:     //Arcade Mode Drive by JS
                hdgOut = -JS_IO.dvrRX.get();
                // hdgOut = BotMath.SegLine(hdgOut, xOutAr);  //Compensate for poor turning.
                diffDrv.arcadeDrive(JS_IO.dvrRY.get(), hdgOut, false);
                prvState = state;
                break;
            case 10:     //Auto Hold Heading Mode, Fwd/Bkwd by RJSY
                // hdgOut steerTo(pov, pwr, dist) else by JS;
                //Set Auto Hold Angle, 0, 45, 90, ... , 315.  Press a pov direction.
                if(!JS_IO.pov_SP.isNone()){
                    steer.steerTo(JS_IO.pov_SP.get(), 100.0, 0.0);
                    strCmd = steer.update(hdgFB, dist_Avg);
                    hdgOut = strCmd[0];
                }else{
                    hdgOut = JS_IO.dvrRX.get();
                }
                // hdgOut = BotMath.SegLine(hdgOut, xOutAr);  //Compensate for poor turning.
                diffDrv.arcadeDrive(JS_IO.dvrRY.get(), hdgOut, false);
                prvState = state;
                break;
            case 30:     //Init Trajectory, turn to hdg then (31) ...
                if(prvState != state){
                    steer.steerTo(traj[trajIdx]);
                    resetDist();
                }else{
                    // Calc heading & dist output.  rotation X, speed Y
                    strCmd = steer.update(hdgFB, dist_Avg);
                    hdgOut = strCmd[0]; // Get hdg output, Y
                    distOut = 0.0;      // Get distance output, X
                    // Apply as a arcade joystick input
                    // hdgOut = BotMath.SegLine(hdgOut, xOutAr);  //Compensate for poor turning.
                    diffDrv.arcadeDrive(distOut, hdgOut, false);

                    //Chk if trajectory is done
                    if(steer.isHdgDone()){
                        state = 31;    //Chk hdg only
                        resetDist();
                    }
                }
                prvState = state;
                break;
            case 31:     //steer Auto Heading and Dist
                // Calc heading & dist output.  rotation X, speed Y
                strCmd = steer.update(hdgFB, dist_Avg);
                hdgOut = strCmd[0];
                distOut = strCmd[1];
                // Apply as a arcade joystick input
                // hdgOut = BotMath.SegLine(hdgOut, xOutAr);  //Compensate for poor turning.
                diffDrv.arcadeDrive(distOut, hdgOut, false);

                //Chk if trajectory is done
                if(steer.isDistDone()){
                    state = 32;    //Chk distance only
                }
                prvState = state;
                break;
            case 32:     //Increment Auto Index & chk for done all traj.
                diffDrv.arcadeDrive(0.0, 0.0);
                if(prvState != state){
                    prvState = state;   //Let other states see change of state, COS
                }else{
                    trajIdx++;
                    state = ((trajIdx) < traj.length) ? 30 : 0; //Next Traj else Off
                }
                break;
            default:
                diffDrv.tankDrive(0.0, 0.0, true);
                System.out.println("Bad Drive State" + state);
        }
    }

    // Update Stuff
    private static void stfUpdate(){
        // // Adjust scaling of Output
        // maxOut += JS_IO.spdShiftUp.onButtonPressed()  ?  0.2 :
        //           JS_IO.spdShiftDn.onButtonReleased() ? -0.2 : 0.0;
        // maxOut = BotMath.Clamp(maxOut, 0.2, 1.0);
        // diffDrv.setMaxOutput(maxOut);

        hdgFB = BotMath.normalizeTo180(IO.ahrs.getAngle()); //either one of these
        if(JS_IO.resetGyro.get()) IO.ahrs.reset();

        if(JS_IO.resetDist.onButtonPressed()){
            resetDist();
        }
        distFB = calcDist();
    }

    private static void resetDist(){
        dist_L = 0.0;
        dist_R = 0.0;
        psntEncDist_L = 0.0;
        prvEncDist_L = 0.0;
        psntEncDist_R = 0.0;
        prvEncDist_R = 0.0;
        IO.whlEnc_L.reset();
        IO.whlEnc_R.reset();
    }

    private static double calcDist(){
        psntEncDist_L = IO.whlEnc_L.getDistance();
        psntEncDist_R = IO.whlEnc_R.getDistance();
        if((IO.drvMotor_L.get() < -0.15) && !prvRev_L){
            prvRev_L = true;
            IO.whlEnc_L.reset();
            psntEncDist_L = 0.0;
            prvEncDist_L = 0.0;
        }else if((IO.drvMotor_L.get() > 0.15) && prvRev_L){
            prvRev_L = false;
            IO.whlEnc_L.reset();
            psntEncDist_L = 0.0;
            prvEncDist_L = 0.0;
        }
        dist_L += (psntEncDist_L - prvEncDist_L) * (prvRev_L ? -1.0 : 1.0); 

        if((IO.drvMotor_R.get() < -0.15) && !prvRev_R){
            prvRev_R = true;
            IO.whlEnc_R.reset();
            psntEncDist_R = 0.0;
            prvEncDist_R = 0.0;
        }else if((IO.drvMotor_R.get() > 0.15) && prvRev_R){
            prvRev_R = false;
            IO.whlEnc_R.reset();
            psntEncDist_R = 0.0;
            prvEncDist_R = 0.0;
        }
        dist_R += (psntEncDist_R - prvEncDist_R) * (prvRev_R ? -1.0 : 1.0); 
        prvEncDist_L = psntEncDist_L;
        prvEncDist_R = psntEncDist_R;
		dist_Avg = (dist_L + dist_R) / 2.0;
        return dist_Avg;
}

    // Handle Smartdashbaord Initialization
    private static void sdbInit(){
        SmartDashboard.putNumber("Drv Mode", state);

        SmartDashboard.putNumber("Hdg Out",hdgOut);
        SmartDashboard.putNumber("Dist Out",distOut);
        // SmartDashboard.putNumber("DistM L", distTPF_L);
        // SmartDashboard.putNumber("DistM R", distTPF_R);
    }

    // Handle Smartdashbaord Updates
    private static void sdbUpdate(){
        SmartDashboard.putNumber("Drv Mode", state);  //Set by JS btns
        SmartDashboard.putNumber("JS Y", JS_IO.dvrRY.get());//Set by JS R Y
        SmartDashboard.putNumber("JS X", JS_IO.dvrRX.get());//Set by JS R X

        SmartDashboard.putNumber("Hdg SP", JS_IO.pov_SP.get());
        SmartDashboard.putNumber("Hdg FB", hdgFB);
        SmartDashboard.putNumber("Hdg Out",hdgOut);

        // SmartDashboard.putNumber("Enc L", enc_L);
        // SmartDashboard.putNumber("Enc R", enc_R);
        // distTPF_L = SmartDashboard.getNumber("DistM L", distTPF_L);
        // distTPF_R = SmartDashboard.getNumber("DistM R", distTPF_R);
        SmartDashboard.putNumber("Dist L", dist_L);
        SmartDashboard.putNumber("Dist R", dist_R);
        SmartDashboard.putNumber("Dist A", dist_Avg);
        SmartDashboard.putNumber("Dist FB", distFB);
        SmartDashboard.putNumber("Dist Out",distOut);
        SmartDashboard.putNumber("Traj Idx",trajIdx);
    }
}