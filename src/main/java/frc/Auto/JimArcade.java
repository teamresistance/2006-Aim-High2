package frc.Auto;

import static frc.io.hdw_io.IO.drvMotor_R;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import frc.io.hdw_io.IO;

public class JimArcade implements ICommand {
    private double heading;
    private double targHead;
    private double targDist;
    private boolean once;
    private double gyroAngle;
    private double targetAngle;

    private double prevError;
    private double error;

    private final static double TICKS_PER_FEET = 432.9;
    private final static double TICKS_PER_INCH = 36.075;
    private final static double xSpeed = .3;

    private Victor left = IO.drvMotor_L;
    private Victor right = drvMotor_R;
    private DifferentialDrive drive;
    SimpleProp prop;

    public JimArcade(int feet, int inch, double heading){
        drive = new DifferentialDrive(left,right);
        targDist = feet + (inch/12);
        targHead = heading;
        once = true;
    }


    @Override
    public void init() {
       IO.whlEnc_L.reset();
       IO.whlEnc_R.reset();
        heading = IO.ahrs.getAngle();
        prop = new SimpleProp(heading, 150, 5, -1, 1, -180, 180);
        }

    @Override
    public void execute() {
        drive.arcadeDrive(xSpeed, joyError());
        //TODO: try prop on speed?
    }

    @Override
    public boolean done() {
        if((IO.whlEnc_L.getDistance() + IO.whlEnc_R.getDistance())/2 >= targDist){
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
    }

    private double joyError(){
        gyroAngle = IO.ahrs.getAngle();
		targetAngle = ((heading % 360) + 360) % 360;
		error = targetAngle - gyroAngle;
		if (Math.abs(error) > 180) { // if going around the other way is closer
			if (error > 0) { // if positive
				error = error - 360;
			} else { // if negative
			    error =  error + 360;
            }
        }
        return prop.calcProp(error);

    }


    
}
