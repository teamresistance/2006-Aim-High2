/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.Auto.Auto;
import frc.io.hdw_io.IO;
import frc.io.hdw_io.TestColor1;
import frc.io.hdw_io.TestColor2;
import frc.io.joysticks.JS_IO;
import frc.io.limelight.LL_IO;
import frc.subsystem.drive.Drive;
import frc.subsystem.Shooter;
import frc.subsystem.Turret;
import frc.subsystem.Lifter;

public class Robot extends TimedRobot {


    Auto auto;
    private boolean once = true;
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    @Override
    public void robotInit() {
        auto = new Auto();
        IO.init();
        LL_IO.init();
        JS_IO.init();
        Drive.init();
        Shooter.init();
        Turret.init();
        Lifter.init();
        TestColor1.init();
        TestColor2.init();
    }

    /**
     * This function is called every robot packet, no matter the mode. Use
     * this for items like diagnostics that you want ran during disabled,
     * autonomous, teleoperated and test.
     */
    @Override
    public void robotPeriodic() {

    }

    /**
     * This function is called once at the beginning of autonomous.
     */
    @Override
    public void autonomousInit() {
        auto.init();
        auto.reset();
    }

    /**
     * This function is called periodically during autonomous.
     */
    @Override
    public void autonomousPeriodic() {
        LL_IO.sdbUpdate();
		if(once){		//start the execution, then check if the entire sequence is done before stopping the run
			auto.execute();
		once = false;       
		}else if(!auto.done()){
			auto.execute();
		}
        //IO.update();
        Drive.update();

    }

    /**
     * This function is called periodically during operator control.
     */
    @Override
    public void teleopPeriodic() {
        IO.update();
        //LL_IO.update();
        LL_IO.sdbUpdate();
        Drive.update();
        Shooter.update();
        Turret.update();
        Lifter.update();
        // TestColor1.update();
        // TestColor2.update();
    }

    /**
     * This function is called periodically during test mode.
     */
    @Override
    public void testPeriodic() {

    }
}
