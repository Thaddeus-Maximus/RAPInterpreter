package com.ppi.oorapinterpreter;

import java.util.HashMap;
import java.util.Map;

public class Unit implements Comparable<Unit>{
	// This error is thrown when you attempt to convert between two unit types that aren't the same, like feet and seconds.
	public class NonMatchingUnitTypeError extends Exception{
		public NonMatchingUnitTypeError(String a, String b){
			super("Unit types "+a+" and "+b+" are not of same type.");
		}
	};
	
	public static Map<String, String> unitNames = new HashMap<String, String>();
		static{
			unitNames.put("ft", "feet");
			unitNames.put("feet", "feet");
			unitNames.put("foot", "feet");
			
			unitNames.put("in", "inches");
			unitNames.put("inch", "inches");
			unitNames.put("inches", "inches");
			
			unitNames.put("m", "meters");
			unitNames.put("meter", "meters");
			unitNames.put("meters", "meters");
			
			unitNames.put("cm", "centimeters");
			unitNames.put("centimeter", "centimeters");
			unitNames.put("centimeters", "centimeters");
			
			unitNames.put("yd", "yards");
			unitNames.put("yds", "yards");
			unitNames.put("yard", "yards");
			unitNames.put("yards", "yards");
			
			unitNames.put("s", "seconds");
			unitNames.put("sec", "seconds");
			unitNames.put("secs", "seconds");
			unitNames.put("seconds", "seconds");
			unitNames.put("second", "seconds");
			
			unitNames.put("ms", "milliseconds");
			unitNames.put("msec", "milliseconds");
			unitNames.put("msecs", "milliseconds");
			unitNames.put("milliseconds", "milliseconds");
			unitNames.put("millisecond", "milliseconds");
			
			unitNames.put("ns", "nanoseconds");
			unitNames.put("nsec", "nanoseconds");
			unitNames.put("nsecs", "nanoseconds");
			unitNames.put("nilliseconds", "nanoseconds");
			unitNames.put("nillisecond", "nanoseconds");
			
			unitNames.put("min", "minutes");
			unitNames.put("mins", "minutes");
			unitNames.put("minute", "minutes");
			unitNames.put("minutes", "minutes");
			
			unitNames.put("pct", "percent");
			unitNames.put("percent", "percent");
			unitNames.put("%", "percent");
			
			unitNames.put("points", "points");
			unitNames.put("point", "points");
			unitNames.put("pts", "points");
			unitNames.put("p", "points");
			
			unitNames.put("rad", "radians");
			unitNames.put("radians", "radians");
			unitNames.put("radian", "radians");
			unitNames.put("rads", "radians");
			
			unitNames.put("deg", "degrees");
			unitNames.put("degrees", "degrees");
			unitNames.put("degree", "degrees");
			unitNames.put("degs", "degrees");
			
			unitNames.put("rev", "revolutions");
			unitNames.put("revs", "revolutions");
			unitNames.put("revolution", "revolutions");
			unitNames.put("revolutions", "revolutions");
			
			unitNames.put("times", "times");
			unitNames.put("time", "times");
			unitNames.put("", "");
			unitNames.put("count", "counts");
			unitNames.put("counts", "counts");
			unitNames.put("unit", "units");
			unitNames.put("units", "units");
		}
	
	public static Map<String, String> unitTypes = new HashMap<String, String>();
		static{
		unitTypes.put("feet", "length");
		unitTypes.put("inches", "length");
		unitTypes.put("meters", "length");
		unitTypes.put("centimeters", "length");
		unitTypes.put("yards", "length");
		
		unitTypes.put("seconds", "time");
		unitTypes.put("milliseconds", "time");
		unitTypes.put("nanoseconds", "time");
		unitTypes.put("minutes", "time");
		
		unitTypes.put("percent", "amount");
		unitTypes.put("points", "amount");
		
		unitTypes.put("radians", "rotational");
		unitTypes.put("degrees", "rotational");
		unitTypes.put("revolutions", "rotational");
		
		unitTypes.put("times", "units");
		unitTypes.put("counts", "units");
		unitTypes.put("units", "units");
		unitTypes.put("", "units");
		}
	
	public static Map<String, Double> unitValues = new HashMap<String, Double>();
		static{
			unitValues.put("feet", 1.0);
			unitValues.put("inches", 1/12.0);
			unitValues.put("yards", 3.0);
			unitValues.put("meters", 3.28);
			unitValues.put("centimeters", 0.0328);
			
			unitValues.put("seconds", 1.0);
			unitValues.put("milliseconds", 0.001);
			unitValues.put("nanoseconds", 1.0/1000000000);
			unitValues.put("minutes", 60.0);
			
			unitValues.put("percent", 1.0);
			unitValues.put("points", 100.0);
			
			unitValues.put("radians", 57.2957795);
			unitValues.put("degrees", 1.0);
			unitValues.put("revolutions", 360.0);
			
			unitValues.put("times", 1.0);
			unitValues.put("counts", 1.0);
			unitValues.put("units", 1.0);
			unitValues.put("", 1.0);
		}
	
	
	public Double amount;
	public String unit;
	public Unit(Double amount, String unit){
		this.amount=amount;
		this.unit=unitNames.get(unit);
		if (this.unit == null)
			this.unit = "times";
	}
	public Unit(Integer amount, String unit){
		this.amount=Double.valueOf(amount);
		this.unit=unitNames.get(unit);
		if (this.unit == null)
			this.unit = "times";
	}
	
  //Return a human-readable string representation of this unit in the format of "xxx.xxx units".
	public String toString(){
		return amount.toString()+" "+unit;
	}
	
  // Return this unit in another type. No side effects, just a return value. (I.E. 24 inches in feet is 2 feet. This is returned, and this unit remains 24 inches, not 2 feet)
	public Unit convertTo(String type) throws NonMatchingUnitTypeError{
		String newUnitName = unitNames.get(type);
		if (!unitTypes.get(newUnitName).equals(unitTypes.get(unit)))
			throw new NonMatchingUnitTypeError(unit, newUnitName);
		return new Unit(this.amount*unitValues.get(unit)/unitValues.get(newUnitName), type);
	}
	
  //Get what type of unit this is (length, time, rotational, etc)
	public String getType() {
		return unitTypes.get(this.unit);
	}
	
	// Add this unit and another one together and return the result in the units of this unit. (I.E. 12 in + 1 ft = 24 in, not 2 ft)
	public Unit add(Unit otherUnit) throws NonMatchingUnitTypeError{
		if (!unitTypes.get(otherUnit.unit).equals(unitTypes.get(this.unit)))
			throw new NonMatchingUnitTypeError(this.unit, otherUnit.unit);
		return new Unit(this.amount+otherUnit.amount*unitValues.get(otherUnit.unit)/unitValues.get(this.unit), this.unit);
	}
  //Subtract another unit from this unit and return the result in the units of this unit. (I.E. 18 in - 1 ft = 6 in, not 0.5 ft)
	public Unit subtract(Unit otherUnit) throws NonMatchingUnitTypeError{
		if (!unitTypes.get(otherUnit.unit).equals(unitTypes.get(this.unit)))
			throw new NonMatchingUnitTypeError(this.unit, otherUnit.unit);
		return new Unit(this.amount-otherUnit.amount*unitValues.get(otherUnit.unit)/unitValues.get(this.unit), this.unit);
	}
  //Multiply this unit by some pure number and return the result in the units of this unit. (I.E. 6 in * 4 = 24 in)
	public Unit multiply(double times){
		return new Unit(this.amount*times, this.unit);
	}
  //Divide this unit by some pure number and return the result in the units of this unit. (I.E. 9 in / 3 = 3 in)
	public Unit divide(double times){
		return new Unit(this.amount/times, this.unit);
	}
	public Unit modulo(double times){
		return new Unit(this.amount%times, this.unit);
	}

  public boolean equals(Object otherUnit) {
  	if (otherUnit instanceof Unit) {
	  	try{
	  		return Math.abs(((Unit) otherUnit).convertTo(unit).amount - this.amount) < 0.001;
	  	}catch(NonMatchingUnitTypeError unme){
	  		
	  	}
	  	return false;
  	}
  	return false;
  }

  public int compareTo(Unit otherUnit) {
  	try{
  		otherUnit = otherUnit.convertTo(this.unit);
  		if (otherUnit.amount > this.amount+0.001)
  			return -1;
  		else if (otherUnit.amount < this.amount-0.001)
  			return 1;
  		else
  			return 0;
  	}catch(NonMatchingUnitTypeError unme) {
  		
  	}
  	return 0;
  }
  
  public boolean lessThan(Unit otherUnit) {
  	try{
  		otherUnit = otherUnit.convertTo(this.unit);
  		if (otherUnit.amount > this.amount)
  			return true;
  	}catch(NonMatchingUnitTypeError unme) {
  		
  	}
  	return false;
  }
  public boolean greaterThan(Unit otherUnit) {
  	try{
  		otherUnit = otherUnit.convertTo(this.unit);
  		if (otherUnit.amount < this.amount)
  			return true;
  	}catch(NonMatchingUnitTypeError unme) {
  		
  	}
  	return false;
  }
  public boolean lessThanOrEqualTo(Unit otherUnit) {
  	try{
  		otherUnit = otherUnit.convertTo(this.unit);
  		if (otherUnit.amount >= this.amount+0.001)
  			return true;
  	}catch(NonMatchingUnitTypeError unme) {
  		
  	}
  	return false;
  }
  public boolean greaterThanOrEqualTo(Unit otherUnit) {
  	try{
  		otherUnit = otherUnit.convertTo(this.unit);
  		if (otherUnit.amount <= this.amount+0.001)
  			return true;
  	}catch(NonMatchingUnitTypeError unme) {
  		
  	}
  	return false;
  }
  public boolean equalTo(Unit otherUnit) {
  	try{
  		otherUnit = otherUnit.convertTo(this.unit);
  		if (Math.abs(otherUnit.amount - this.amount)<0.001)
  			return true;
  	}catch(NonMatchingUnitTypeError unme) {
  		
  	}
  	return false;
  }
}