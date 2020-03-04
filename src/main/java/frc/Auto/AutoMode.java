package frc.Auto;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class AutoMode implements ICommand {

	private ICommand[] commands;
	private int index;
	
	private boolean done;
	private boolean once;
	//construct seqence of commands into array
	public AutoMode(ICommand...commands) {
		this.commands = commands;
		this.index = 0;
		this.done = false;
		once = true;
	}

	//run all inits inside the sequence on call
	@Override
	public void init() {
		for (ICommand a : commands) {
			a.init();
		}
	}
	//TODO: make inits execute with reset so it's called right before its command
	
	@Override
	public void execute() {
		
		SmartDashboard.putString("Where am I", "Literally nothing happened");
		if(once){
			commands[index].init();
			SmartDashboard.putString("Where am I", "First Init");
			once = false;
		}
		SmartDashboard.putNumber("index", index);
		SmartDashboard.putBoolean(" ind 1 done", commands[0].done());
		
		//SmartDashboard.putBoolean("done", commands[index].done());
		SmartDashboard.putBoolean("don1", done);
		SmartDashboard.putNumber("commands1", commands.length);
		
		//if not complete yet and current command is not done then run the current command
		if ((index < commands.length)  && !commands[index].done()) {
			commands[index].execute();
			
			SmartDashboard.putString("Where am I", "execution");
		} else {
			//else the current command is done, and increment to the next command,  checking to see if we reached the end of the sequence
			index++;
			if (index >= commands.length) {
				this.done = true;
				
			SmartDashboard.putString("Where am I", "I am Done");
			SmartDashboard.putBoolean("don1", done);
			SmartDashboard.putNumber("commands1", commands.length);	
			return;
			}
			commands[index].init(); //TODO: this might be wrong 
		}
	}

	@Override
	//done is true when the index has overpassed the # of commands
	public boolean done() {
		return done;
	}

	//no implementation yet, but could set index = 0
	@Override
	public void reset() {

	}
}
