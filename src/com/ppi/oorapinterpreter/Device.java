package com.ppi.oorapinterpreter;

import java.util.Map;

public class Device {
	protected String port;
	
	public class NoSuchCall extends Exception {
		public NoSuchCall() {
			super();
		}

		public NoSuchCall(String message) {
			super(message);
		}

		public NoSuchCall(String message, Throwable cause) {
			super(message, cause);
		}

		public NoSuchCall(Throwable cause) {
			super(cause);
		}
	}
	
	public Device(String port) {
		this.port = port;
	}
	
	public Object call(String function, Map<String, Object> args) throws NoSuchCall {
  	System.out.println("Basic Device ("+port+") "+function+"Call with params "+args.toString());
	  return null;
	}
}
