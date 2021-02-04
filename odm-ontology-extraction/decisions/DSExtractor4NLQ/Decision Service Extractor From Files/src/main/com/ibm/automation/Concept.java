package com.ibm.automation;

import java.util.ArrayList;

public class Concept {

	public String name;
	public String label;
	public String pluralLabel;
	
	public ArrayList<Attribute> attributes = new ArrayList<Attribute>();
	public ArrayList<Constant> constants = new ArrayList<Constant>();
	
	public Concept()  {
		
	}
	
	public Concept(String conceptName, String conceptLabel, String pluralLabel)  {
		this.name = conceptName;
		this.label = conceptLabel;
		this.pluralLabel = pluralLabel;
	}
	
	public void add(Attribute attribute)  {
		attributes.add(attribute);
	}
	
	public void add(Constant constant)  {
		constants.add(constant);
	}
}
