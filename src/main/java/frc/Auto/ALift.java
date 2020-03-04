/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.Auto;

import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.IO;
import frc.subsystem.Lifter;
import frc.util.timers.TimerAnt;

/**
 * Add your docs here.
 */
public class ALift implements ICommand{

    private Victor lifter = IO.lifter;
    private double time;
    TimerAnt timePass;
    public ALift(double time){
        this.time = time;
    }

    @Override
    public void init() {
        lifter.set(0);
        timePass = new TimerAnt(time);
        SmartDashboard.putString("init has run","yes it has");
    }

    @Override
    public void execute() {
        Lifter.aUpdate(1);
    }

    @Override
    public boolean done() {
        if(timePass.hasPassedDelay()){
            Lifter.aUpdate(0);
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }
}
