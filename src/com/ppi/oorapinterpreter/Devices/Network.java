package com.ppi.oorapinterpreter.Devices;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import com.ppi.oorapinterpreter.Device;
import com.ppi.oorapinterpreter.Unit;
import com.ppi.oorapinterpreter.Device.NoSuchCall;

public class Network extends Device {
	public Network(String port) {
		super(port);
	}
	public Network() {
		super("");
	}
	
	public Object call(Map<String, Object> args) throws NoSuchCall {
		throw new NoSuchCall();
	}
}
