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

public class Logger extends Device {
	public Logger(String port) {
		super(port);
	}
	public Logger() {
		super("log.txt");
	}
	
	public Object call(Map<String, Object> args) throws NoSuchCall {
		if (args.get("FUNCTION").equals("log")) {				
			PrintWriter out;
			try {
				if (port.equals("console")) {
					TimeZone tz = TimeZone.getTimeZone("UTC");
		    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    	df.setTimeZone(tz);
					if (args.containsKey("error")) {
			    	System.out.println(df.format(new Date()) + " (UTC) !!Error!!: "+args.get("error"));
			    } else if (args.containsKey("info")) {
			    	System.out.println(df.format(new Date()) + " (UTC) Info: "+args.get("info"));
			    } else if (args.containsKey("detail")) {
			    	System.out.println(df.format(new Date()) + " (UTC) Detail: "+args.get("detail"));
			    	
			    }
				}else{
  				out = new PrintWriter(new BufferedWriter(new FileWriter(port, true)));
				
					TimeZone tz = TimeZone.getTimeZone("UTC");
		    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    	df.setTimeZone(tz);
					if (args.containsKey("error")) {
			    	out.println(df.format(new Date()) + " (UTC) !!Error!!: "+args.get("error"));
			    } else if (args.containsKey("info")) {
			    	out.println(df.format(new Date()) + " (UTC) Info: "+args.get("info"));
			    } else if (args.containsKey("detail")) {
			    	out.println(df.format(new Date()) + " (UTC) Detail: "+args.get("detail"));
			    	
			    }
					out.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    
	    
		}else
			throw new NoSuchCall();
		return null;
	}
}
