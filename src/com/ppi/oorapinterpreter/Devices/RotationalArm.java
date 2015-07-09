package com.ppi.oorapinterpreter.Devices;

import java.util.Map;

import com.ppi.oorapinterpreter.Device;
import com.ppi.oorapinterpreter.Unit;

public class RotationalArm extends Device{
	public RotationalArm(String port) {
		super(port);
	}
	
	public Object call(Map<String, Object> args) throws NoSuchCall {
		if (args.get("FUNCTION").equals("set"))
			System.out.println("CALL: Arm on "+port+" set to "+args.get("to"));
		else if (args.get("FUNCTION").equals("position"))
			return new Unit(50.0, "inches");
		else
			throw new NoSuchCall();
		return null;
	}
}
