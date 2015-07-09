/* Copyright (C) Thaddeus Joseph Hughes - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Thaddeus Hughes <hughes.thad@gmail.com>, March 2015
 */

package com.ppi.oorapinterpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.ppi.oorapinterpreter.Devices.Console;
import com.ppi.oorapinterpreter.Devices.Logger;
import com.ppi.oorapinterpreter.Devices.Network;
import com.ppi.oorapinterpreter.Devices.RAPSystem;
import com.ppi.oorapinterpreter.Devices.RotationalArm;

public final class OORAPInterpreter {
	public Map<String, Object> variables = new HashMap<String, Object>();
	public Map<String, Integer> variableLevels = new HashMap<String, Integer>();
	public int currentLevel = 0, lastLevel = 0;
	private ArrayList<String[]> lines = new ArrayList<String[]>();
	private ArrayList<Integer> indentations = new ArrayList<Integer>();
	
	String[] operators = { "<", ">", "!", "!=", "=", "==", "<=", ">=", "*",
			"+", "-", "/", "%", "!", "|", "&",   "~", "^", "not", "and", "or", "xor"};

	public class LoopInterrupt extends Exception {
		public LoopInterrupt() {
			super();
		}

		public LoopInterrupt(String message) {
			super(message);
		}

		public LoopInterrupt(String message, Throwable cause) {
			super(message, cause);
		}

		public LoopInterrupt(Throwable cause) {
			super(cause);
		}
	}

	public static void main(String[] args) {
		System.out.println("Launching RAPInterpreter...");
		try {
			new OORAPInterpreter(new BufferedReader(new FileReader(new File("script.rap"))), null).run();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/* Parse a file into the lines and indentations arraylists. This is merely a tokenizer.
	 * No code is verified in integrity.
	 */
	public OORAPInterpreter(BufferedReader br, BufferedReader stdin) {

		try {
			//BufferedReader br = new BufferedReader(new FileReader(f));
			String command;
			// Iterate through every line in the code
			while ((command = br.readLine()) != null) {
				String trimmedCommand = command.trim();
				if (trimmedCommand.isEmpty())
					continue;
				if (trimmedCommand.startsWith("#"))
					continue;
				
				List<String> argsBuilder = new ArrayList<String>();
				// Go through each character. I DOES get incremented in more places than the i++ on the loop.
				for (int i=0; i<trimmedCommand.length(); i++) {
					String token = "";
					char c = trimmedCommand.charAt(i);
					// If the character is a quote, it is a string.
					if (c == '"') {
						token = "\"";
						i++;
						// Add to the token until we reach the end of the line or a double quote.
						while(i<trimmedCommand.length()) {
							c = trimmedCommand.charAt(i);
							if (c == '"'){
								token+="\"";
								break;
							} else {
								token+=c;
							}
							i++;
						}
					// If the character is a paren, that's all there is to that token.
					} else if (c == '('){
						token = "(";
					} else if (c == ')'){
						token = ")";
					// If it's just a plain old character, parse until space or a paren.
					} else {
						token="";
						while(i<trimmedCommand.length()) {
							c = trimmedCommand.charAt(i);
							if (c == ' '){
								break;
							// Since we i++, we'll need to i-- in order for the loop to catch the paren.
							} else if (c == '(') {
								i--;
								break;
							} else if (c == ')') {
								i--;
								break;
							} else {
								token+=c;
							}
							i++;
						}
					}
					
					// Only add the token if there's anything in it.
					if (token.length() > 0)
					  argsBuilder.add(token);
				}
				// Convert from an arrayList to a array; execution speed should be increased.
				String[] args = (String[]) argsBuilder.toArray(new String[argsBuilder.size()]);
				
				lines.add(args);

				// Calculate the indentation level. Right now, two spaces defines an indentation, but maybe later we can change this.
				int indent = 0;
				for (indent = 0; indent < command.length(); indent++) {
					if (command.charAt(indent) != ' ')
						break;
				}
				indentations.add(indent / 2);
			}
			br.close();
		} catch (Exception e) {
		}
		if (stdin==null)
			variables.put("console", new Console());
		else
  		variables.put("console", new Console(stdin));
		//variables.put("system", new RAPSystem());
		//variables.put("network", new Network());
	}
  
	/*
	 * Test the truth value of objects.
	 * null is false.
	 * Units less than 0.0001 in magnitude are false; otherwise true;
	 * Booleans are their respective value.
	 * Everything else is false.
	 */
	public boolean testTruth(Object o) {
		if (o == null)
			return false;
		if (o instanceof Unit) {
			return ((Unit) o).amount < -0.0001 || ((Unit) o).amount > 0.0001;
		}
		if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue();
		}
		return false;
	}

	
	/*
	 * Test if an object is a number or unit.
	 */
	public boolean isANumber(Object o) {
		return o instanceof Unit || o instanceof Number;
	}

	/*
	 * Construct a device based on name.
	 */
	public Device makeDevice(String name, String port) {
		name = name.toLowerCase();
		if (name.equals("rotationalarm"))
			return new RotationalArm(port);
		if (name.equals("logger"))
			return new Logger(port);
		if (name.equals("sysconsole"))
			return new Console(port);
		return null;
	}
	
	/*
	 * Removes a set of outside parens iff the inner contents are within the outside parens.
	 * Ex.: (x) (y) would yield (x) (y); (x y) would yield x y.
	 */
	public String[] stripUnneededParens(String[] start) {
		if (start.length <= 0)
			return start;
		if (!start[0].equals("("))
			return start;
		for (int i=1, numberOfParens=1;i<start.length;i++) {
			if (start[i].equals("("))
				numberOfParens++;
			else if (start[i].equals(")"))
				numberOfParens--;
			if (numberOfParens<=0)
				return start;
		}
		return Arrays.copyOfRange(start, 1, start.length-1);
	}

	public Object eval(String[] args) {
		return eval(args, new Unit(0, ""));
	}

	/*
	 * Evaluate an array of lines. This function is recursive.
	 * The amountConsumed parameter will be modified to reflect how many lines were read; if the first two match up to a unit and no operators are present, then only those will be evalled.
	 */
	public Object eval(String[] args, Unit amountConsumed) {
		args = stripUnneededParens(args);
		try{
			
		if (args.length <= 0)
			return null;
		
		// If there's only one arg in what we're working with, which starts and ends with a quote, it's clearly a string.
		if (args.length == 1 && args[0].startsWith("\"") && args[0].endsWith("\"")){
			amountConsumed.amount = 1.0;
			return args[0].substring(1, args[0].length()-1);
		}
		
		// Look for the first operator and do it.
		// Iterate through each arg.
		for (int i = 0, numberOfParens=0; i < args.length; i++) {
			// If the arg is a paren, take note of the level change.
			if (args[i].equals("("))
				numberOfParens++;
			else if (args[i].equals(")"))
				numberOfParens--;
			// We can only do operations on (Relatively) zero level terms. But we still need to do them.
			if (numberOfParens<=0) {
				// Loop through each operator and check if it matches the current arg.
				for (String operator : operators) {
					if (args[i].equalsIgnoreCase(operator)) {
						// After we find that, we need to evaluate the stuff before it and the stuff after it.
						// Recursion is a REALLY nice thing for this...
						Object first = null;
						try{first = eval(stripUnneededParens(Arrays.copyOfRange(args, 0, i)));
						}catch(Exception e){}
						Object second = null;
						try{second = eval(stripUnneededParens(Arrays.copyOfRange(args, i + 1, args.length)));
						}catch(Exception e){}
						// Take note of how many args were taken up in doing this.
						amountConsumed.amount = (double) args.length;
						// Check what kind of operator was used and perform the appropriate action on the two args.
						if (operator.equals("!") || operator.equals("~") || operator.equals("not"))
							return Boolean.valueOf(!testTruth(second));
						else if (operator.equals("=") || operator.equals("=="))
							return first.equals(second);
						else if (operator.equals("!="))
							return !first.equals(second);
						else if (operator.equals("<"))
							return ((Unit)first).lessThan((Unit)second);
						else if (operator.equals(">"))
							return ((Unit)first).greaterThan((Unit)second);
						else if (operator.equals("<="))
							return ((Unit)first).lessThanOrEqualTo((Unit)second);
						else if (operator.equals(">="))
							return ((Unit)first).greaterThanOrEqualTo((Unit)second);
						else if (operator.equals("|") || operator.equals("or"))
							return Boolean.valueOf(testTruth(first) || testTruth(second));
						else if (operator.equals("&") || operator.equals("and"))
							return Boolean.valueOf(testTruth(first) && testTruth(second));
						else if (operator.equals("xor"))
							return Boolean.valueOf(testTruth(first) ^ testTruth(second));
						
						
						if (operator.equals("+") && first instanceof String) {
							return first.toString() + second.toString();
						}
						if (first==null && second instanceof Unit) {
							try{
								if (operator.equals("+"))
								  return new Unit(((Unit)second).amount, ((Unit)second).unit);
								else if (operator.equals("-"))
								  return new Unit(-((Unit)second).amount, ((Unit)second).unit);
							}catch(Exception e){
								return null;
							}
						}
						if (first instanceof Unit && second instanceof Unit) {
							try {
								if (operator.equals("*"))
									return ((Unit) first).multiply(((Unit) second).amount);
								if (operator.equals("/"))
									return ((Unit) first).divide(((Unit) second).amount);
								if (operator.equals("%"))
									return ((Unit) first).modulo(((Unit) second).amount);
								if (operator.equals("^"))
									return new Unit(Math.pow(((Unit) first).amount, ((Unit) second).amount), ((Unit)first).unit);
								if (operator.equals("+"))
									return ((Unit) first).add(((Unit) second));
								if (operator.equals("-"))
									return ((Unit) first).subtract(((Unit) second));
							} catch (Exception e) {
								return null;
							}
						}else if (first instanceof Unit && ! (second instanceof Unit)) {
							try {
								if (operator.equals("*"))
									return ((Unit) first).multiply((Double) second);
								if (operator.equals("/"))
									return ((Unit) first).divide((Double) second);
								if (operator.equals("%"))
									return ((Unit) first).modulo((Double) second);
								if (operator.equals("^"))
									return new Unit(Math.pow(((Unit) first).amount, (Double) second), ((Unit)first).unit);
							} catch (Exception e) {
								return null;
							}
						}else if (! (first instanceof Unit) && (second instanceof Unit)) {
							try {
								if (operator.equals("*"))
									return ((Unit) second).multiply((Double) first);
								/*if (operator.equals("/"))
									return ((Unit) second).divide((Double) first);
								if (operator.equals("%"))
									return ((Unit) second).modulo((Double) first);
								if (operator.equals("^"))
									return ((Unit) first).amount = Math.pow(((Unit) first).amount, ((Unit) second).amount);*/
							} catch (Exception e) {
								return null;
							}
						}
						
						return null;
					}
				}
			}
		}}catch(Exception oe){
		}
		

		// If we made it thus far, it's not an operator eval. So let's try a device call.
		if (variables.containsKey(args[0].toLowerCase())) {
			
			try {
				Map<String, Object> argsDict = new HashMap<String, Object>();
				String functionToCall = args[1].toLowerCase();
				int i;
				for (i = 2; i < args.length;) {
					String[] argsToRun = Arrays.copyOfRange(args, i+1, args.length);
					
					int extra = 0;
					if (i+1<args.length && args[i+1].equals("(")) {
						int numberOfParens = 1;
						for (int j=i+2; j<args.length;j++) {
							if (args[j].equals(")")) 
								numberOfParens--;
							else if (args[j].equals("(")) 
								numberOfParens++;
							if (numberOfParens <= 0) {
								extra+=2;
								argsToRun = Arrays.copyOfRange(args, i + 2, j);
								break;
							}
						}
					}
					Unit amountConsumedInEval = new Unit(0, "");
					argsDict.put(
							args[i].toLowerCase(),
							eval(argsToRun,
									amountConsumedInEval));
					
					i += amountConsumedInEval.amount+1 +extra;
				}
				amountConsumed.amount = (double) args.length;
				return ((Device) variables.get(args[0].toLowerCase())).call(functionToCall, argsDict);
			} catch (Device.NoSuchCall nsc) {
				// Eeek, silly user! That call isn't a thing! I guess we'll just give back the device/variable that owns it...
				amountConsumed.amount = 1.0;
				return variables.get(args[0].toLowerCase());
			} catch (Exception e) {
			  // Eeek! That call isn't a thing! I guess we'll just give back the device/variable that owns it...
				amountConsumed.amount = 1.0;
				return variables.get(args[0].toLowerCase());
			}

		}
		// That didn't work, so if the second arg is "on", let's try to make a device.
		if (args.length > 1 && args[1].equals("on")) {
			amountConsumed.amount = 3.0;
			return makeDevice(args[0], args[2]);

		}


		// Try and make a unit with the remaining args.
		try {
			amountConsumed.amount = 2.0;
			return new Unit(Double.parseDouble(args[0]), args[1]);
		} catch (Exception e) {
		}
		// Try and make a unit with the remaining arg, but just assume "units".
		try {
			amountConsumed.amount = 1.0;
			return new Unit(Double.parseDouble(args[0]), "times");
		} catch (Exception e) {
		}
		
		// If none of that worked, just return a string. Bare words aren't spectacular, but desparate times call for desparate measures...
		amountConsumed.amount = 1.0;
		return args[0];

	}

	public int run() throws LoopInterrupt {
		return run(new Integer(0), 0);
	}
	/*
	 * Execute code starting at the given line, until the level drops beneath the startingLevel.
	 */
	public int run(/*ArrayList<ArrayList<String>> tokens, ArrayList<Integer> indentations, */int line, int startingLevel) throws LoopInterrupt {
		try {
			// Until we run out of lines
			while (line < lines.size()) {
				// Grab the current set of lines to work with
				String[] args = lines.get(line);
				Integer currentLevel = indentations.get(line);
				// If the indentation level dropped below starting, then stop; we've finished the block of code.
				if (indentations.get(line).intValue() < startingLevel) {
					break;
				// If the indentation level grew, start a new block.
				} else if (indentations.get(line).intValue() > startingLevel) {
					run(line, indentations.get(line).intValue());
					for (; line < lines.size(); line++)
						if (indentations.get(line).intValue() <= startingLevel)
							break;
					continue;
				}

				if (args[0].equalsIgnoreCase("define")) {
					if (args[2].toLowerCase().equals("as")) {
						variables.put(args[1].toLowerCase(),
								eval(Arrays.copyOfRange(args, 3, args.length)));
					}
				} else if (args[0].equalsIgnoreCase("loop")) {
					try {
						if (args[1].equalsIgnoreCase("for")) {
							try {
								Unit amount = (Unit) eval(Arrays.copyOfRange(args, 2,
										args.length));
								
								// Try looping either for a # of times for for a period of time.
								if (amount.getType().equals("units")) {
									for (int i = 0; i < amount.amount; i++)
										run(line + 1, indentations.get(line).intValue() + 1);
								} else if (amount.getType().equals("time")) {
									for (long initTime = System.currentTimeMillis(); System
											.currentTimeMillis() < (amount.convertTo("ms").amount + initTime);)
										run(line + 1, indentations.get(line).intValue() + 1);
								}
							} catch (LoopInterrupt li) {
								throw li;
							} catch (Exception e) {
								if (args[2].equalsIgnoreCase("ever"))
									while (true)
										run(line + 1, indentations.get(line).intValue() + 1);
							}
						} else if (args[1].equalsIgnoreCase("forever")) {
							while (true)
								run(line + 1, indentations.get(line).intValue() + 1);
						} else if (args[1].equalsIgnoreCase("while")) {
							try {
								while (testTruth(eval(Arrays.copyOfRange(args, 2, args.length))))
									run(line + 1, indentations.get(line).intValue() + 1);
							} catch (LoopInterrupt li) {
								throw li;
							} catch (Exception e) {

							}
						} else if (args[1].equalsIgnoreCase("until")) {
							try {
								while (!testTruth(eval(Arrays.copyOfRange(args, 2, args.length))))
									run(line + 1, indentations.get(line).intValue() + 1);
							} catch (LoopInterrupt li) {
								throw li;
							} catch (Exception e) {

							}
						}
					// If a loopinterrupt was thrown, stop looping; just proceed on.
					} catch (LoopInterrupt li) {
					}

					// Increment where we are to reflect.
					for (; line < lines.size(); line++)
						if (indentations.get(line + 1).intValue() <= startingLevel)
							break;

				} else if (args[0].equalsIgnoreCase("if")) {
					boolean result = false;
					try {
						result = testTruth(eval(Arrays.copyOfRange(args, 1, args.length)));
						if (result)
							run(line + 1, indentations.get(line).intValue() + 1);
					} catch (LoopInterrupt li) {
						throw li;
					} catch (Exception e) {

					}
					for (; line < lines.size(); line++)
						if (indentations.get(line + 1).intValue() <= startingLevel)
							break;

					args = lines.get(line + 1);
					// Continue an else/elif chain.
					while (args[0].equalsIgnoreCase("else")
							|| args[0].equalsIgnoreCase("elif")) {
						line++;
						args = lines.get(line);
						currentLevel = indentations.get(line);

						if (args[0].equalsIgnoreCase("else")) {

							if (!result) {
								result = true;
								run(line + 1, indentations.get(line).intValue() + 1);

							}
							for (; line < lines.size(); line++)
								if (indentations.get(line + 1).intValue() <= startingLevel)
									break;
							break;
						} else {
							if (!result) {

								try {
									result = testTruth(eval(Arrays.copyOfRange(args, 1,
											args.length)));
									if (result) {
										run(line + 1, indentations.get(line).intValue() + 1);
									}
								} catch (LoopInterrupt li) {
									throw li;
								} catch (Exception e) {
								}
							}
							for (; line < lines.size(); line++)
								if (indentations.get(line + 1).intValue() <= startingLevel)
									break;
						}

					}

				} else if (args[0].equalsIgnoreCase("stoploop")) {
					throw new LoopInterrupt();
				} else {
					eval(args);
				}
				line++;
			}
		} catch (LoopInterrupt li) {
			throw li;
		} catch (Exception e) {
		}
		return line;
	}

}
