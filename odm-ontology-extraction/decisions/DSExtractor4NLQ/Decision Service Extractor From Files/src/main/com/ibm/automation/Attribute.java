package com.ibm.automation;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Attribute {

	public String name;
	public String type;
	public String roleLabel;
	public String pluralRoleLabel;
	public String verbalizationExample;

	public Attribute()  {
		
	}
			
	public Attribute(String attrName, String type, String roleLabel, String pluralRoleLabel, String verbalizationExample) {
		this.name = attrName;
		this.type = type;
		this.roleLabel = roleLabel;
		this.pluralRoleLabel = pluralRoleLabel;
		this.verbalizationExample = verbalizationExample;
	}
}
