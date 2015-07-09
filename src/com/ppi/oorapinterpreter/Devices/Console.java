package com.ppi.oorapinterpreter.Devices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import com.ppi.oorapinterpreter.Device;
import com.ppi.oorapinterpreter.Unit;
import com.ppi.oorapinterpreter.Device.NoSuchCall;

public class Console extends Device {
	BufferedReader br = null;
	public Console() {
		super("");
		br = new BufferedReader(new InputStreamReader(System.in));
	}

	public Console(String port) {
	super(port);
	br = new BufferedReader(new InputStreamReader(System.in));
	}
	
	public Console (BufferedReader br) {
		super("");
		this.br = br;
	}
	
	public Object call(String function, Map<String, Object> args) throws NoSuchCall {
  	if (function.equals("write")) {
  		if (args.containsKey("error")) {
  			System.err.println(args.get("error"));
  		} else if (args.containsKey("line")) {
  			System.out.println(args.get("line"));
  		} else if (args.containsKey("text")) {
  			System.out.print(args.get("text"));
  		} else if (args.containsKey("words")) {
  			System.out.print(args.get("words"));
  		}
  		return null;
  	} else if (function.equals("read")) {
  		
  		if (args.containsKey("unit") || args.containsKey("number")) {
  			try {
					String val = br.readLine().trim().toLowerCase();
					int index = val.indexOf(' ');
					if (index>=0)
					  return new Unit(Double.valueOf(val.substring(0, index)), val.substring(index+1));
					else
						return new Unit(Double.valueOf(val), "times");
				} catch (IOException e) {
					return null;
				}
  			
  			
  		} else if (args.containsKey("truth") || args.containsKey("boolean")) {
  			try {
					String val = br.readLine().toLowerCase();
					if (val.startsWith("t") || val.startsWith("y")) return Boolean.valueOf(true);
					else return Boolean.valueOf(false);
				} catch (IOException e) {
					return null;
				}
  		} else {
  			try {
					return br.readLine();
				} catch (IOException e) {
					return null;
				}
  		}
		}
	  throw new NoSuchCall();
	}

}
