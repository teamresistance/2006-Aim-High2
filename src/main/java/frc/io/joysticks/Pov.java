package frc.io.joysticks;
/*
Original Author: Sherya
Rewite Author: Jim Hofmann
History:
JCH - 11/6/2019 - Original rework
S - 3/6/2017 - Original release

TODO: more testing.  maybe add an array handler?

Desc: Allows use of various joystick/gamepad configurations.
Constructor get JS_ID and povID.  But povID is not needed.  However, if it needs to pass
a default (povID may not exist for some combinations) then in povID pass -1 for no press,
and a pov value 0, 45, ... 315.
*/

import edu.wpi.first.wpilibj.Joystick;

public class Pov{
	
	private Joystick joystick;
	private int povID;		// no povId needed, used for exists & default value
	private boolean exists;
	private int existDflt;
	
	// Constructor, normal
	public Pov(Joystick injoystick, int inpovID) {
		joystick = injoystick;
		povID = inpovID;
		exists = joystick != null;
		existDflt = inpovID;		// null js, use pov default
	}

	// Constructor, defaults set to does not exist & passed value
	public Pov(int exDefault) {
		exists = false;
		existDflt = exDefault;
	}

	// Constructor, defaults set to does not exist & 0.0
	public Pov() {
		exists = false;
		existDflt = -1;
	}

	// Assign a different joystick & POV
	public void setPov(Joystick injoystick, int inpovID){
		joystick = injoystick;
		povID = inpovID;
		exists = joystick != null;
		existDflt = povID < 0 ? -1 : inpovID - (inpovID % 45);		// null js, use pov default
	}

	// Unassign joystick & and set a default
	public void setPov(int _default){
		exists = false;
		existDflt = _default;		// assign default
	}

	// Unassign joystick & and set default to -1, no press
	public void setPov(){
		exists = false;
		existDflt = -1;		// assign default as -1
	}

	// get POV value
	public int get() {
		return exists ? joystick.getPOV() : existDflt;
	}

	// test POV match
	public boolean equals( int test ){ return test == get(); }
	public boolean isNone(){ return get() < 0; }
	public boolean isPressed(){ return get() >= 0; }
	public boolean is0(){ return 0 == get(); }
	public boolean is45(){ return 45 == get(); }
	public boolean is90(){ return 90 == get(); }
	public boolean is135(){ return 135 == get(); }
	public boolean is180(){ return 180 == get(); }
	public boolean is225(){ return 225 == get(); }
	public boolean is270(){ return 270 == get(); }
	public boolean is315(){ return 315 == get(); }
}
