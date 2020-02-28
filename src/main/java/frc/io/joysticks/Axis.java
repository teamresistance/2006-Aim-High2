package frc.io.joysticks;
/*
Original Author: Sherya
Rewite Author: Jim Hofmann
History:
JCH - 11/6/2019 - rework
S - 3/6/2017 - Original release

TODO: more testing.  maybe add an array handler?

Desc: Allows use of various joystick/gamepad configurations.
Constructor get JS_ID and axisID.  However, if the it needs to pass a default (axis may not
exist for some combinations) then in axisID pass 10 * default value + 100 ( 10 * -default - 100).
IE., to default 0.0 pass 100.  For -1.0 pass -110.  For 1.0 pass 110.
*/

import edu.wpi.first.wpilibj.Joystick;

public class Axis{
	
	private Joystick joystick;
	private int axisID;
	private boolean exists;
	private double exDefault;
	
	// Constructor, normal
	// Exists muxed with axisID, if GT 100 (LT 0) does not exist
	public Axis(Joystick injoystick, int inaxisID) {
		joystick = injoystick;
		axisID = inaxisID;
		exists = joystick != null;
		exDefault = (inaxisID % 100)/10.0;	// get last 2 digits then divide by 10, 110=>1.0
	}

	// Constructor, defaults set to does not exist & passed value
	public Axis(double exDefault) {
		this.exists = false;
		this.exDefault = exDefault;
	}

	// Constructor, defaults set to does not exist & 0.0
	public Axis() {
		this.exists = false;
		this.exDefault = 0.0;
	}

	// Assign a different joystick & button
	public void setAxis(Joystick injoystick, int inAxisID){
		joystick = injoystick;
		axisID = inAxisID;
		exists = joystick != null;
		exDefault = (axisID % 100)/10.0;	// get last 2 digits then divide by 10, 110=>1.0
	}

	// Unassign button, Will now return assigned default value on a get().
	public void setAxis(double _default){
		exists = false;
		exDefault = _default;	// assign default value
	}

	// Unassign button, Will now return 0.0 on a get().
	public void setAxis(){
		exists = false;
		exDefault = 0.0;	// assign default as 0.0
	}

	// get the axis value
	public double get() {
		return exists ? joystick.getRawAxis(axisID) : exDefault;
	}

	// true if axis value is > db
	public boolean isDown() {
		return Math.abs(exists ? joystick.getRawAxis(axisID) : exDefault) > 0.2;
	}

	// true if axis value is < db
	public boolean isUp() {
		return !isDown();
	}
}
