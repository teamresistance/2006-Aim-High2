// package frc.Auto;

// import com.ctre.phoenix.motorcontrol.ControlMode;
// import com.ctre.phoenix.motorcontrol.can.TalonSRX;

// import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

// public class Mindstorm implements ICommand {
// 	private TalonSRX  left = IO.left;
//     private TalonSRX right = IO.right;
    
//     private int deadband = 150;
//     private double targDist;

//     private boolean once;
//     private final static double TICKS_PER_FEET = 432.9;
//     private final static double TICKS_PER_INCH = 36.075;
//     private double basePower = .3;
//     private double lowPower = .1;

//     //set target distance and if it needs to be inverted or not
//     public Mindstorm(int feet, int inch) {
//         targDist = feet * TICKS_PER_FEET + inch * TICKS_PER_INCH;
//         once = true;
//         if(targDist < 0){
//             basePower = -basePower;
//             lowPower = -lowPower;
//         }
//     }

//     @Override
//     public void init() {
//         left.set(ControlMode.PercentOutput, 0);
//         right.set(ControlMode.PercentOutput, 0);

//         left.setSelectedSensorPosition(0, 0, 0);
//         right.setSelectedSensorPosition(0, 0, 0);
//     }

//     //drive at power until nearing target, then drive slow
//     @Override
//     public void execute() {
//         SmartDashboard.putNumber("targ Mindstorm", targDist);
//         if(once){
//             left.setSelectedSensorPosition(0, 0, 0);
//             right.setSelectedSensorPosition(0, 0, 0);
//             once = false;
//         }
//         if(targDist < 0 ? targDist + 150 > left.getSelectedSensorPosition() : targDist - 150 < left.getSelectedSensorPosition()){
//             left.set(ControlMode.PercentOutput, lowPower);
//             right.set(ControlMode.PercentOutput, lowPower);
//         }else{
//         left.set(ControlMode.PercentOutput, basePower);
//         right.set(ControlMode.PercentOutput, basePower);
//         }
//     }

//     //if within the deadband, move onto the next command
//     @Override
//     public boolean done() {
//         if(left.getSelectedSensorPosition() >= targDist - deadband && left.getSelectedSensorPosition() <= targDist + deadband){
            
//         left.set(ControlMode.PercentOutput, 0);
//         right.set(ControlMode.PercentOutput, 0);
//             return true;
//         }
//         return false;
//     }

// 	@Override
// 	public void reset() {
//         left.setSelectedSensorPosition(0, 0, 0);
//         right.setSelectedSensorPosition(0, 0, 0);
		
// 	}



// }
