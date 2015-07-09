package com.ppi.oorapinterpreter;

import java.util.Map;

public class Timer extends Device {
  public Timer() {
  	super("");
  }
  public Timer(String port) {
  	super(port);
  }
  
  public Object call(String function, Map<String, Object> args) throws NoSuchCall {
	  return null;
  	
  }
}
