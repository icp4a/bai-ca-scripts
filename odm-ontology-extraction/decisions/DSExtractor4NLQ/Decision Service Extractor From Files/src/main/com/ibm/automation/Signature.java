package com.ibm.automation;

import java.util.ArrayList;

public class Signature {

	private ArrayList<Parameter> parameters = new ArrayList<Parameter>();
	
	public Signature()  {
		
	}

	public ArrayList<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(ArrayList<Parameter> parameters) {
		this.parameters = parameters;
	}
	
	public void addParameter(Parameter parameter)  {
		this.parameters.add(parameter);
	}
}
