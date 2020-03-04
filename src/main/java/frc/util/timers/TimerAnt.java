package frc.util.timers;

public class TimerAnt {

        //sets timer in seconds
        private double delay; //in seconds
        private double initTime;
        private int state = -1;
        public TimerAnt(double delay){
            initTime = System.currentTimeMillis();
            this.delay = delay * 1000;
        }
    
        public boolean hasPassedandSet(double delay, int state){ //set once, run over and over
            if(this.state != state){
                initTime = System.currentTimeMillis();
                this.delay = delay * 1000;
            } 
            this.state = state;
            return hasPassedDelay();
        }
    
        public boolean hasPassedDelay(){
            if(System.currentTimeMillis() >= (initTime + delay)){
                return true;
            }
            return false;
        }
    public double timeEllapsed(){
        return System.currentTimeMillis() - initTime;
    }
    
}
