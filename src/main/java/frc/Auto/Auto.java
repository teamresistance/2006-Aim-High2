package frc.Auto;

import org.ejml.simple.AutomaticSimpleMatrixConvert;

import frc.subsystem.Lifter;

public class Auto implements ICommand{
	private AutoMode autoMode;
	public Auto(){
		autoMode = new AutoMode(new LimeTarget(), new AShooter(2), new ALift(10), new AShooter(0), new ALift(3));	//sequence created
		//autoMode = new AutoMode(new )
	}
	
	//for all autoMode._____ functions, see AutoMode
	@Override
	public void init() {
		//autoMode.init();
		
	}
	
	@Override
	public void execute() {
		autoMode.execute();	
		
		
	}

	@Override
	public boolean done() {
		
		return autoMode.done();
	}

	@Override
	public void reset() {
		autoMode = new AutoMode(new LimeTarget(),new AShooter(2), new ALift(10), new AShooter(0), new ALift(3));	//restarting the sequence when called- all places are re initilized
	}
	

}
