package frc.Auto;

import frc.subsystem.Shooter;

public class AShooter implements ICommand{
    private int state;
    public AShooter(int state){
        this.state = state;
    }
    @Override
    public void init() {
    }

    @Override
    public void execute() {
        Shooter.setState(state);
    }

    @Override
    public boolean done() {
        if(state == 2){
            if(Shooter.isAtSpd()){
                return true;
            }
        }
        if(state != 2 && Shooter.getState() != 2){
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }
}
