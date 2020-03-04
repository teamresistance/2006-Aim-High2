package frc.Auto;

public interface ICommand {
	
	public void init();
	
	public void execute();
	
	public boolean done();
	public void reset();

}
