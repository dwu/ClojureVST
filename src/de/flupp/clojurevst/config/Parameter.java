package de.flupp.clojurevst.config;

import java.util.ArrayList;
import java.util.List;

public class Parameter {

	private String variable;
	private String name;
	private String label;
	
	private boolean bMultiplier;
	private float multiplier;
	
	private List<Float> values;
	
	public Parameter() {
		values = new ArrayList<Float>();
	}
	
	public Parameter(String variable, String name, String label) {
		this();
		this.variable = variable;
		this.name = name;
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getVariableName() {
		return variable;
	}

	public void setVariableName(String variable) {
		this.variable = variable;
	}

	public float getValue(int program) {
		return values.get(program);
	}

	public void addValue(int program, float value) {
		values.add(program, value);
	}
	
	public void setValue(int program, float value) {
		values.set(program, value);
	}

	public float getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(float multiplier) {
		this.multiplier = multiplier;
	}

	public void setHasMultiplier(boolean hasMultiplier) {
		this.bMultiplier = hasMultiplier;
	}
	
	public boolean hasMultiplier() {
		return this.bMultiplier;
	}

	public String toString()
	{
	    final String TAB = "    ";
	    
	    String retValue = "";
	    
	    retValue = "Parameter ( "
	        + super.toString() + TAB
	        + "variable = " + this.variable + TAB
	        + "name = " + this.name + TAB
	        + "label = " + this.label + TAB
	        + "bMultiplier = " + this.bMultiplier + TAB
	        + "multiplier = " + this.multiplier + TAB
	        + "values = " + this.values + TAB
	        + " )";
	
	    return retValue;
	}
	
}
