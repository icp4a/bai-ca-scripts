package com.ibm.automation;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Parameter {
	
	private String name;
	private String verbalization;
	private String direction;
	private String type;
	private String initialValue;
	
	public Parameter()  {
		
	}
	
	public Parameter(String name, String direction)  {
		this.name = name;
		this.direction = direction;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVerbalization() {
		return verbalization;
	}

	public void setVerbalization(String verbalization) {
		this.verbalization = verbalization;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(String initialValue) {
		this.initialValue = initialValue;
	}
	
	public String toString() {
		return "name: "+ name + ", type:" + type + ", direction:" + direction + ", verbalization:" + verbalization;
	}

}
