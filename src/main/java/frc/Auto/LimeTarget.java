/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.Auto;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.limelight.LL_IO;
import frc.subsystem.Turret;

/**
 * Add your docs here.
 */
public class LimeTarget implements ICommand{

    public LimeTarget(){

    }

    @Override
    public void init() {
        Turret.setState(0);
    }

    @Override
    public void execute() {
        Turret.setState(3); //targeting 
    }

    @Override
    public boolean done() {
        if(LL_IO.llOnTarget() == null){
            return false;
        }else{
        if(LL_IO.llOnTarget() == 0){
            Turret.setState(0);
            return true;
        }
    }
        return false;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }
}
