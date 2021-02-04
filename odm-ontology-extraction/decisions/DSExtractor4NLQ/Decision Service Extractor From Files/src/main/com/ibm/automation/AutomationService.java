package com.ibm.automation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AutomationService {

	private String version;
	private String assetPath;
	private String timestamp;
	private Signature signature = new Signature();
	
	public ArrayList<Concept> concepts = new ArrayList<Concept>();
	
	public AutomationService()  {
		
	}

	public void add(Concept concept) {
		concepts.add(concept);
	}

	public void toJSON(String filepath) {

		ObjectMapper objectMapper = new ObjectMapper();
		try {
			objectMapper.writeValue(new File(filepath), this);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAssetPath() {
		return assetPath;
	}

	public void setAssetPath(String assetPath) {
		this.assetPath = assetPath;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public Signature getSignature() {
		return signature;
	}

	public void setSignature(Signature signature) {
		this.signature = signature;
	}
	
}
