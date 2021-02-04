package com.ibm.decisions;

import java.util.List;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ibm.tatzia.ontology.Concept;
import com.ibm.tatzia.ontology.Ontology;
import com.ibm.tatzia.ontology.Property;

import Text2OWL.FileProcess;


//import com.ibm.translator.directExecutor.DirectConfiguration;

public class OntologyExtractor2 {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
    	String folder="/Users/ming_imac/Desktop/test";
    	String filename="test";
    	extractOntologyandMapping(folder, filename);
   }
	
	
	
	public static List<List<String>> extractOntologyandMapping(String folder, String filename) throws Exception {
		List<List<String>> values = new ArrayList<List<String>>();
		String jsonPath = folder + File.separator+filename+".json";
		//String jsonPath = folder;
        JSONParser parser=new JSONParser();
        JSONObject jsonData = (JSONObject) parser.parse(new FileReader(jsonPath));
        String assetPath = (String) jsonData.get("assetPath");
        String[] pathList = assetPath.split("/");
        String prefix = pathList[1] + "." + pathList[3];
        HashMap<String,List<LinkedList<String> >> conceptAttMap = new HashMap<String, List<LinkedList<String>>>();
        HashSet<String> conceptSet = new HashSet<String>();
        JSONArray conceptArr = (JSONArray) jsonData.get("concepts");
        HashMap<String, String> typeNameMap = new HashMap<String, String>();
        
        for (int i = 0; i < conceptArr.size(); i++) {
        	JSONObject object = (JSONObject) conceptArr.get(i);
        	JSONArray attArr = (JSONArray) object.get("attributes");
        	List<LinkedList<String>> allList = new LinkedList<LinkedList<String>>();
        	if(attArr.size() > 0) {
//        		String nameCon = object.get("label").toString().toLowerCase();
        		String nameCon = object.get("name").toString();
        		nameCon = getConceptAttName(nameCon).toLowerCase();
        		//nameCon = getConceptAttName(nameCon);
        		conceptSet.add(nameCon);
        		typeNameMap.put(nameCon,nameCon);
        		for (int j = 0; j < attArr.size(); j++) {
	        		JSONObject attObj = (JSONObject) attArr.get(j);
	        		String nameAtt = attObj.get("name").toString();
	        		nameAtt = getConceptAttName(nameAtt);
					if(nameAtt.toLowerCase().contains("name")) {
						List<String> con_att = new ArrayList<String>();
						con_att.add(nameCon);
						con_att.add(nameAtt);
						values.add(con_att);
					}
	        		String type = (String) attObj.get("type");
	        		type = getType(type);
	        		LinkedList<String> tempList = new LinkedList<String>();
	        		tempList.add(nameAtt);
	        		tempList.add(type);
	        		allList.add(tempList);
	        	}
	        	conceptAttMap.put(nameCon,allList);
	        }
       }
       JSONArray sigArr = (JSONArray)((JSONObject) jsonData.get("signature")).get("parameters");
       HashMap<String, String> conceptDirectionMap = new HashMap<String, String>();
       for (int i = 0; i < sigArr.size(); i++) {
    	   JSONObject object = (JSONObject) sigArr.get(i);
    	   //String name = object.get("name").toString().toLowerCase();
    	   String name = object.get("name").toString();
    	   String type = (String) object.get("type");
    	   type = getType(type);
    	   if(conceptSet.contains(type)) {
    	   typeNameMap.put(type, name);
    	   }
    	   String direction = (String) object.get("direction");
    	   conceptDirectionMap.put(type, direction);

    		   LinkedList<String> tempList = new LinkedList<String>();
    		   tempList.add(name);
    		   tempList.add(type);
    		   List<LinkedList<String>> value = null;
    		   if(conceptAttMap.containsKey(direction)){
    			   value = conceptAttMap.get(direction);
    		   }else {
    			   value = new LinkedList<LinkedList<String>>();
    		   }
			   value.add(tempList);
    		   conceptAttMap.put(direction, value);
    	   
    	}
       	conceptAttMap.put(pathList[1],new LinkedList<LinkedList<String>>());
       	conceptAttMap.put(pathList[3],new LinkedList<LinkedList<String>>());
       	conceptAttMap.put("data",new LinkedList<LinkedList<String>>());
        HashMap<String, String> typemap = new HashMap<String, String>();
        typemap.put("string","STRING");
        typemap.put("integer","BIGINT");
        typemap.put("float","FLOAT");
        typemap.put("double","DOUBLE");
        typemap.put("boolean","BOOLEAN");
        typemap.put("date","DATE");
        typemap.put("long","LONG");
        typemap.put("varchar","VARCHAR");
        typemap.put("list", "STRING");
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(folder+"/"+filename+".txt")));
        bw.write("concepts:\n");
        for (String key : conceptAttMap.keySet()) {
        	List<LinkedList<String>> value = conceptAttMap.get(key);
        	String outStr = key+":";
        	int cnt = 0;
        	for(int k=0;k<value.size();k++) {
        		List<String> list = value.get(k);
        		if(!conceptSet.contains(list.get(1))) {
	        		if(typemap.containsKey(list.get(1))) {
	        		outStr += list.get(0)+"("+typemap.get(list.get(1))+"),";
	        		}else {
	        			outStr += list.get(0)+"("+list.get(1)+"),";
	        		}
	        		cnt++;
        		}
        	}
        	if(cnt>0) {
        		bw.write(outStr.substring(0,outStr.length()-1));
        	}else {
        		bw.write(outStr);
        	}
        	bw.write("\n");
        }
        
        bw.write("relations:\n");
        for (String key : conceptAttMap.keySet()) {
        	List<LinkedList<String>> value = conceptAttMap.get(key);
        	for(int k=0;k<value.size();k++) {
        		List<String> list = value.get(k);
        		String type = list.get(1);
        		if(conceptSet.contains(type)){
        			bw.write("functional:"+key+":"+type+":1:1:"+key+"_"+type);
        			bw.write("\n");
        		}
        	}
        }
        bw.write("functional:data:"+pathList[1]+":1:1:data_"+pathList[1]+"\n");
        bw.write("functional:"+pathList[1]+":"+pathList[3]+":1:1:"+pathList[1]+"_"+pathList[3]+"\n");
        bw.write("functional:"+pathList[3]+":in:1:1:"+pathList[3]+"_in\n");
        bw.write("functional:"+pathList[3]+":out:1:1:"+pathList[3]+"_out\n");
        bw.close();
        System.out.print("Extract ontology file done!");
        
        bw = new BufferedWriter(new FileWriter(new File(folder+"/"+filename+"Mapping.txt")));
        List<LinkedList<String>> in_list = conceptAttMap.get("in");
        List<LinkedList<String>> out_list = conceptAttMap.get("out");
        HashMap<String, LinkedList<String>> conMappingOut = new HashMap<String, LinkedList<String>>();
        HashMap<String, HashSet<String>> mappingRelation = new HashMap<String, HashSet<String>>();
        
        // get all concept mapping relation
        for(String key: conceptAttMap.keySet()) {
        	List<LinkedList<String>> value = conceptAttMap.get(key);
        	if(value.size()>0) {
	        	for(int k=0;k<value.size();k++) {
	        		List<String> list = value.get(k);
	        		String type = list.get(1);
	        		if(conceptSet.contains(type)) {
	        			String name = typeNameMap.get(type);
	        			if(name.equals(key)) {
	        				continue;
	        			}
	        			HashSet<String> tmpSet = null;
	        			if(!mappingRelation.containsKey(name)) {
	        				tmpSet = new HashSet<String>();
	        			}else {
	        				tmpSet = mappingRelation.get(name);
	        			}
	        			
	        			tmpSet.add(key+"."+name);
	        			mappingRelation.put(name, tmpSet);
	        		}
	        	}

    			HashSet<String> tmpSet = null;
    			if(typeNameMap.containsKey(key)) {
    				key = typeNameMap.get(key);
    			}
    			if(!mappingRelation.containsKey(key)) {
    				tmpSet = new HashSet<String>();
    			}else {
    				tmpSet = mappingRelation.get(key);
    			}
    			tmpSet.add(key);
    			mappingRelation.put(key, tmpSet);

        	}
        }
        
        // expand and combine
        HashMap<String, HashSet<String>> mappingRelation_expand = new HashMap<String, HashSet<String>>();
        int count  = 1;
        while(count>0) {
        	count  = 0;
	        for(String keyout:mappingRelation.keySet()) {
	        	HashSet<String> tmpSet = new HashSet<String>();
	        	HashSet<String> valueout = mappingRelation.get(keyout);
	        	Iterator valueout_it = valueout.iterator();
	        	while(valueout_it.hasNext()) {
	        		String out_str =  valueout_it.next().toString();
	        		String[] out = out_str.split("\\.");
	        		String first_out = "";
	        		String last_out = "";
	        		if(out.length>0) {
		        		first_out = out[0];
		        		last_out = out[out.length-1];
	        		}else {
		        		first_out = out_str;
		        		last_out = out_str;
	        		}
		            for(String keyin:mappingRelation.keySet()) {
		            	if(!keyout.equals(keyin)) {
		            		HashSet<String> valuein = mappingRelation.get(keyin);
		            		Iterator valuein_it = valuein.iterator(); 
		            		while(valuein_it.hasNext()) {
		            			String in_str = valuein_it.next().toString();
		                    	String[] in = in_str.split("\\.");
		                		String first_in = "";
		                		String last_in = "";
		                		if(in.length>0) {
		        	        		first_in = in[0];
		        	        		last_in = in[in.length-1];
		                		}else {
		        	        		first_in = in_str;
		        	        		last_in = in_str;
		                		}
		                    	if(last_in.equals(first_out)) {
		                    		String tmp_str = out_str;
		                    		tmp_str = tmp_str.replace(first_out, in_str);
		                    		tmpSet.add(tmp_str);
		                    		if(!(last_in.equals("in")||last_in.equals("out"))) {
		                    			count++;
		                    		}
		                    	}
		            		}
		                }
		            }
	            }
	        	tmpSet.add(keyout);
	        	mappingRelation_expand.put(keyout,tmpSet);
	        }
	        
	     // combine same prefix string
	        HashMap<String, HashSet<String>> mappingRelation_fusion = new HashMap<String, HashSet<String>>();
	        for(String key:mappingRelation_expand.keySet()) {
	        	HashSet<String> tmpSet = new HashSet<String>();
	        	List value = new ArrayList(mappingRelation_expand.get(key));
	        	for(int i=0;i<value.size();i++) {
	        		String out = value.get(i).toString();
	        		Boolean flag = false;
	        		for(int j=i;j<value.size();j++) {
	        			String in = value.get(j).toString();
	        			if(!in.equals(out)) {
	            			if(in.contains(out)) {
	            				value.remove(out);
	            			}else if(out.contains(in)) {
	            				value.remove(in);
	            			}
	            		}
	        		}
	            	if(!flag) {
	            		tmpSet.add(out);
	            	}
	        	}
	 
	        	mappingRelation_fusion.put(key, new HashSet(value));
	        }
	        mappingRelation = mappingRelation_fusion;
        }
        HashMap<String, HashSet<String>> finalMapping = new HashMap<String, HashSet<String>>();
        for (String key : conceptAttMap.keySet()) {
        	String mappingStr = "";
        	List<LinkedList<String>> value = conceptAttMap.get(key);
        	HashSet<String> finalSet = new HashSet<String>();
        	if(value.size()>0) {
        		for(int k=0;k<value.size();k++) {
	        		List<String> list = value.get(k);
	        		String name = list.get(0);
	        		String type = list.get(1);
	        		mappingStr = name+"=";
	        		HashSet<String> tmpSet = null;
	        		if(typeNameMap.containsKey(key)) {
	        			tmpSet = mappingRelation.get(typeNameMap.get(key));
	        		}else {
	        			tmpSet = mappingRelation.get(key);
	        		}
        			for(String str: tmpSet) {
        				mappingStr += prefix + "." + str + "." + name + ",";
        				HashSet<String> tmpfinalSet = new HashSet<String>();
        				if(finalMapping.containsKey(name)) {
        					tmpfinalSet = finalMapping.get(name);
        				}
    					tmpfinalSet.add(prefix + "." + str + "." + name);
    					finalMapping.put(name, tmpfinalSet);
//		        		if(!conceptSet.contains(typeNameMap.get(type))) {
//		        			// if attribute is not a concept
//		        				mappingStr += prefix + "." + str + "." + name + ",";
//		        				fianlSet.add(prefix + "." + str + "." + name);
//		        			}else {
//		        				mappingStr += prefix + "." + str + ",";
//		        				fianlSet.add(prefix + "." + str);
//		        		}
		        	}
//        			bw.write(mappingStr.substring(0, mappingStr.length()-1) + "\n");
        			
        		}
        			
        	}
        }
        bw.write("data=data\n");
        bw.write(pathList[1]+"="+pathList[1]+"\n");
        bw.write(pathList[3]+"="+prefix+"\n");
        bw.write("in="+prefix+".in\n");
        bw.write("out="+prefix+".out\n");
        for(String key:finalMapping.keySet()) {
        	String mappingStr = key+"=";
        	for(String str:finalMapping.get(key)) {
        		mappingStr += str + ",";
        	}
        	bw.write(mappingStr.substring(0, mappingStr.length()-1)+"\n");
        }
        bw.close();
        //change txt to json
        String file = folder+"/"+filename+"Mapping.txt";
        BufferedReader reader = null;
        JSONObject ontology_path_mapping = new JSONObject();
        try{
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			while((tempString = reader.readLine()) != null){
				String[] list = tempString.split("=");
				String key = list[0];
				String valuess = list[1];
				String[] valuesList = valuess.split(",");
				String value = valuesList[0];
				ontology_path_mapping.put(key, valuess);
			}
		}catch (IOException e) {
            e.printStackTrace();
        }finally{
            if (reader != null) {
				try {
					reader.close();  
				} catch (IOException e1) {
					e1.printStackTrace();
                }
            }
        }
        String fileJson = folder+"/"+filename+"Mapping.json";
        FileWriter fw;
        try {
            fw = new FileWriter(fileJson);
            PrintWriter out = new PrintWriter(fw);
            out.write(ontology_path_mapping.toJSONString());
            out.println();
            fw.close();
            out.close();
  
        } catch (IOException e) {
            e.printStackTrace();
        }
        	

        
        
        return values;
	}
	
	//<actual value>@<swimlane name>, .. , .. :Concept.Property
	public static void parseBPM(String folder, JSONObject JSONdata) {
		try {
			JSONObject jobj = JSONdata;
			JSONObject diagram = (JSONObject)jobj.get("Diagram");
			JSONArray step = (JSONArray)diagram.get("step");
			String titable = "";
			if (step.size() > 0) {
				for (int i = 0; i < step.size(); i++) {
					JSONObject element = (JSONObject)step.get(i);
					String actName = element.get("name").toString();
					String lane = element.get("lane").toString();
					String aliasName = "";
					if (lane == null)
						aliasName = actName;
					else 
						aliasName = lane; //treat swimlane as alias
					if (i != step.size()-1)
						titable += (actName+"@"+lane+",");
					else titable += (actName+"@"+lane+":");
				}
			}
			String concept = "rootDocument";
			String property = "name";
			titable += (concept + "." + property);
			System.out.println(titable);
			// write into the file
			//String user_dir = System.getProperty("user.dir");
    		//String file_path = user_dir + "/src/main/resources/bpm-loan/";
			String file_path = folder;
    		String file = "titable";
    		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file_path+"/"+file+".txt")));
    		bw.write(titable+"\n");
			bw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static List<List<String>> extractTitabelAndTivalues(String folder, JSONObject JSONdata) {
		List<List<String>> values = new ArrayList<List<String>>();
		try {
			//generate titable
			//parseBPM(folder, JSONdata);
			JSONObject mapping = JSONdata;
			Set<String> iter = mapping.keySet();
			String rootConcept = "Process";
			String dataConcept = "Data";
			for(String key:iter) {
				if(key.toLowerCase().contains("name")) {
					List<String> con_att = new ArrayList<String>();
					String value = (String) mapping.get(key);
					if (value.toLowerCase().contains("data")) {
						con_att.add(dataConcept);
						con_att.add(key);
						values.add(con_att);
					}
					else {
						con_att.add(rootConcept);
						con_att.add(key);
						values.add(con_att);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return values;
	}
	
	public static List<List<String>> extractTitabelAndTivaluesODM(String folder) {
		List<List<String>> tiValues = new ArrayList<List<String>>();
		String currentDir = System.getProperty("user.dir");
		String sysPath = currentDir + "/src/main/resources";
		String fileName = folder;
		String fullName = fileName;
		File file = new File(fullName);
		BufferedReader reader = null;
		String dataConcept = "";
		try{
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			while((tempString = reader.readLine()) != null){
				String[] list = tempString.split("=");
				String key = list[0];
				String values = list[1];
				String[] valuesList = values.split(",");
				String value = valuesList[0];
				if(key.toLowerCase().contains("name")) {
					List<String> con_att = new ArrayList<String>();
					String[] splits = value.split("\\.");
					dataConcept = splits[splits.length-2];
					con_att.add(dataConcept);
					con_att.add(key);
					tiValues.add(con_att);
					}
				}
		}catch (IOException e) {
            e.printStackTrace();
        }finally{
            if (reader != null) {
				try {
					reader.close();  
				} catch (IOException e1) {
					e1.printStackTrace();
                }
            }
            //setConfigJson.put("ontology_path_mapping", ontology_path_mapping);
//			System.out.println(jsonObj);
        }
		return tiValues;
	}
	public static void useJSONToGetSyn(Ontology o, String sysPath, String schema) {
		// TODO Auto-generated method stub
    		System.out.println("looking for "+sysPath+"/"+schema+".bom");
    		String fileName= sysPath+"/"+schema+".json";
		JSONParser jsonParser = new JSONParser();
        
        try (FileReader reader = new FileReader(fileName))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
            JSONObject json = (JSONObject) obj;
            JSONArray concepts = (JSONArray)json.get("concepts");
            if(concepts != null) {
                for(int i=0;i<concepts.size();i++) {
            		JSONObject concept = (JSONObject) concepts.get(i);
            		String name = (String) concept.get("name");
            		String cname = getOntologyConceptName(name);
            		
            		Concept C = o.getConcept(cname);
    				if(C==null)
    					continue;
    					
    				String conceptLabel = (String) concept.get("label");
    				List<String> syns = getAliases(conceptLabel);
    				for(String syn:syns) {
    					C.addAlias(syn);
    				}
    				
    				String pluralLabel = (String) concept.get("pluralLabel");
    				List<String> pSyns = getAliases(pluralLabel);
    				for(String syn:pSyns) {
    					C.addAlias(syn);
    				}
    				
    				JSONArray properties = (JSONArray) concept.get("attributes");
    				for(int j=0;j<properties.size();j++) {
    					JSONObject property = (JSONObject) properties.get(j);
    					String attrName = (String) property.get("name");
    					String pname = getPropertyName(attrName,o);
    					Property P = o.getProperty(pname, C);
    					if(P==null)
    						continue;
    						
    					String roleLabel = (String) property.get("roleLabel");
    					List<String> propsyns = getAliases(roleLabel);
        				for(String syn:propsyns) {
        					P.addAlias(syn);
        				}
    					String pluralRoleLabel = (String) property.get("pluralRoleLabel");
    					List<String> proppsyns2 = getAliases(pluralRoleLabel);
        				for(String syn:proppsyns2) {
        					P.addAlias(syn);
        				}

    					String verbalizations = (String) property.get("verbalizationExample");
    					List<String> proppsyns3 = getAliases(verbalizations);
        				for(String syn:proppsyns3) {
        					P.addAlias(syn);
        				}
    						
    				}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
    
      //  writeSynFile(o,sysPath);
	}
	
	public static String getPropertyName(String attrName, Ontology o) {
		// TODO Auto-generated method stub
		
		if(attrName.contains(".")) {
			String [] parts2 = attrName.split("\\.");
			String pName = parts2[parts2.length-1];
			return pName;
		}
		else
			return attrName;
	
		
	}

	public static List<String> getAliases(String conceptLabel) {
		// TODO Auto-generated method stub
		List<String> syns = new ArrayList();
		if(conceptLabel.contains(",")) {
			String [] labels = conceptLabel.split(",");
			for(int j=0;j<labels.length;j++) {
				String label = labels[j];
				syns.add(label);
			}
		}
		else {
			syns.add(conceptLabel);
		}
		
		return syns;
	}

	public static String getOntologyConceptName(String conceptName) {
		// TODO Auto-generated method stub
		String cname="";
		if(conceptName.contains(".")) {
			String [] parts= conceptName.split("\\.");
			cname = parts[parts.length-1];
		}
		else
			cname = conceptName;
		
		return cname;
	}
	public static void writeSynFile(Ontology o, String input, String sysPath) throws IOException {
		// TODO Auto-generated method stub
		String fname = sysPath + File.separator + "syn.xml";
		System.out.println(fname);
		FileWriter bw = new FileWriter(input, true);  
        bw.write("synonyms:\n");
		PrintWriter writer;
		try {
			writer = new PrintWriter(fname, "UTF-8");
			writer.println("<synonyms>");
			for (Concept c : o.getAllConcepts()) {
				writer.println("<concept id=\"" + c.getId() + "\">");
				//System.out.println("concept: "+ c.getName());
				bw.write(c.getName()+": ");
				for(String alias:c.getAliases()) {
					writer.println("<alias>"+alias.trim()+"</alias>");
					
					for(String aliasC:c.getAliases()) {
						bw.write(aliasC.trim() + " | ");
					}					
				}
				bw.write("\n");
				for(Property p:c.getProperties()) {
					writer.println("\t"+"<property id=\""+p.getId()+"\">");
					//System.out.println("property: "+ p.getName());
					bw.write(c.getName()+"."+p.getName()+": ");
					for(String alias:p.getAliases()) {
						writer.println("\t"+"<alias>"+alias.trim()+"</alias>");
						bw.write(alias.trim()+" | ");
					}
					writer.println("\t"+"</property>");
					bw.write("\n");
				}
				
				writer.println("</concept>");
			}
			writer.println("</synonyms>");
			writer.close();
			bw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static List<List<String>> extractOntologyForBpm(String folder, String rootFile, String schema, JSONObject JSONdata, String mappingParameter) {
		List<List<String>> values = new ArrayList<List<String>>();
		try {
			//generate titable
			parseBPM(folder, JSONdata);
			String bpmPath = folder + "/" + rootFile;

			File file = new File(bpmPath);
			BufferedReader reader = null;
			
			reader = new BufferedReader(new FileReader(file));
			
	        String rootDocument = "rootDocument";
	        String dataDocument = "data";
	        String trackDocument = "trackingGroup";
			
	        //String temporary = "at1558105511544151559050291068"; //use specific ID for blueDemo, User needs to provide, configs
	        //String temporary = "at*"; //use wildcard to do fuzzy matching and searching
	        String temporary = mappingParameter;
	        
			String tempString = null;
			List<List<String>> rootAtts = new ArrayList<List<String>>();
			boolean firstline = true;
			while((tempString = reader.readLine()) != null) {
				String[] list = tempString.split("\t");
				if(firstline) {
					firstline = false;
					continue;
				}
				if(list.length != 4)continue;
				
				String nameAttr = list[0];
				String typeDesc = list[3];
				if(nameAttr.toLowerCase().contains("name")) {
					List<String> con_att = new ArrayList<String>();
					con_att.add(rootDocument);
					con_att.add(nameAttr);
					values.add(con_att);
				}
				String type = "STRING";
				typeDesc = typeDesc.toLowerCase();
				if(typeDesc.contains("date")) {
					type = "DATE";
				}else if(typeDesc.contains("number")) {
					type = "BIGINT";
				}else if(typeDesc.contains("json object")) {
					continue;
				}else{
					type = "STRING";
				}
				List<String> name_type = new ArrayList<String>();
				name_type.add(nameAttr);
				name_type.add(type);
				rootAtts.add(name_type);
			}
			
			JSONObject jobj = JSONdata;
			JSONObject datamodel = (JSONObject)jobj.get("DataModel");
			JSONObject inputModel = (JSONObject)datamodel.get("inputs");
			JSONObject outputModel = (JSONObject)datamodel.get("outputs");

	        HashMap<String, String> typemap = new HashMap<String, String>();
	        typemap.put("string","STRING");
	        typemap.put("integer","BIGINT");
	        typemap.put("float","FLOAT");
	        typemap.put("double","DOUBLE");
	        typemap.put("boolean","BOOLEAN");
	        typemap.put("date","DATE");
	        typemap.put("long","LONG");
	        typemap.put("varchar","VARCHAR");
	        typemap.put("list", "LIST");
	        typemap.put("decimal", "BIGINT");
	        
	        HashMap<String, String> suffixMap = new HashMap<String, String>();
	        suffixMap.put("STRING","string");
	        suffixMap.put("BIGINT","integer");
	        suffixMap.put("FLOAT","float");
	        suffixMap.put("DOUBLE","double");
	        suffixMap.put("BOOLEAN","boolean");
	        suffixMap.put("DATE","dateTime");
	        suffixMap.put("LONG","long");
	        suffixMap.put("VARCHAR","varchar");
	        suffixMap.put("LIST", "list");
	        
	        List<List<String>> dataAtts = new ArrayList<List<String>>();
	        HashSet<String> attNames = new HashSet<String>();
	        List<JSONObject> input_output = new ArrayList<JSONObject>();
	        input_output.add(inputModel);
	        input_output.add(outputModel);
	        
	        for(int i = 0; i < input_output.size(); i++) {
	        	JSONObject model = input_output.get(i);//input_output rescan
				Set<String> iter = model.keySet();
				for(String key:iter) {
					if(attNames.contains(key)){
						continue;
					}
					if(key.toLowerCase().contains("name")) {
						List<String> con_att = new ArrayList<String>();
						con_att.add(trackDocument);
						con_att.add(key);
						//if (!(values.contains(con_att)))
						values.add(con_att);
					}
					JSONObject attr = (JSONObject)inputModel.get(key);
					String typeOrigin = attr.get("type").toString().toLowerCase();
					String type = typemap.get(typeOrigin);
					List<String> attrPair = new ArrayList<String>();
					attrPair.add(key);
					attrPair.add(type);
					dataAtts.add(attrPair);
				}
	        }
	        
	        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(folder+"/"+schema+".txt")));
	        bw.write("concepts:\n");
	        String conceptRoot = rootDocument + ":";
	        // dataAtts + rootAtts;
	        for(int i = 0; i < rootAtts.size(); i++) {
	        	List<String> attr = rootAtts.get(i);
	        	String attr_str = attr.get(0) + "(" + attr.get(1) + ")" + ",";
	        	conceptRoot += attr_str;
	        }
	        bw.write(conceptRoot.substring(0, conceptRoot.length()-1) + "\n");
	        
	        String conceptData = dataDocument + ":";
	        bw.write(conceptData.substring(0, conceptData.length()) + "\n");
	        
	        String conceptTracking = trackDocument + ":";
	        // dataAtts + rootAtts;
	        for(int i = 0; i < dataAtts.size(); i++) {
	        	List<String> attr = dataAtts.get(i);
	        	String attr_str = attr.get(0) + "(" + attr.get(1) + ")" + ",";
	        	conceptTracking += attr_str;
	        }
	        bw.write(conceptTracking.substring(0, conceptTracking.length()-1) + "\n");

	        bw.write("relations:\n");
	        bw.write("functional:"+rootDocument+":"+dataDocument+":1:1:"+rootDocument+"_"+dataDocument+"\n");
	        bw.write("functional:"+dataDocument+":"+trackDocument+":1:1:"+dataDocument+"_"+trackDocument);
	        bw.close();
	        
	        HashMap<String, HashSet<String>> attrMap = new HashMap<String, HashSet<String>>();
	        List<String> attributesList = new ArrayList<String>();
	        for(int i = 0; i < rootAtts.size(); i++) {
	        	List<String> attr = rootAtts.get(i);
	        	String name = attr.get(0);
	        	HashSet<String> namePath = null;
	        	if(attrMap.containsKey(name)) {
	        		namePath = attrMap.get(name);
	        	}else {
	        		namePath = new HashSet<String>();
	        		attributesList.add(name);
	        	}
//	        	namePath.add(rootDocument + "." + name);
	        	namePath.add(name);
	        	attrMap.put(name, namePath);
	        }
	        for(int i = 0; i < dataAtts.size(); i++) {
	        	List<String> attr = dataAtts.get(i);
	        	String name = attr.get(0);
	        	HashSet<String> namePath = null;
	        	if(attrMap.containsKey(name)) {
	        		namePath = attrMap.get(name);
	        	}else {
	        		namePath = new HashSet<String>();
	        		attributesList.add(name);
	        	}
	        	String type = attr.get(1);
	        	String suffix = "." + suffixMap.get(type);
	        	// String suffix = "";
//	        	namePath.add(rootDocument + "." + temporary + dataDocument + suffix);
	        	namePath.add(dataDocument + "." + temporary + "." + name + suffix);
	        	attrMap.put(name, namePath);
	        }
	        
	        bw = new BufferedWriter(new FileWriter(new File(folder+"/"+schema+"Mapping"+".txt")));
//	        bw.write(rootDocument+"="+rootDocument+"\n");
//	        bw.write(dataDocument+"="+rootDocument+"."+dataDocument+"\n");
//	        bw.write(rootDocument+"=\n");
	        bw.write(dataDocument+"=" + dataDocument+"\n");
	        bw.write(trackDocument+"=" + dataDocument+"."+temporary+"\n");
	        for(int i = 0; i < attributesList.size(); i++) {
	        	String name = attributesList.get(i);
	        	String namePathString = "";
	        	HashSet<String> paths = attrMap.get(name);
	        	for(String path:paths) {
	        		namePathString += path + ",";
	        	}
	        	bw.write(name + "=" + namePathString.substring(0, namePathString.length()-1) + "\n");
	        }
	        bw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return values;
	}
	public static String getConceptAttName(String name) throws Exception {
		String[] nameList = name.split("\\.");
		if(nameList.length>0) {
			return nameList[nameList.length-1];
		}else {
			throw new Exception("Got empty concept or attribute name, check it!");
		}
	}
	public static String getType(String type) throws Exception {
		String[] typeList = type.split("\\.");
		if(typeList.length>0) {
			return typeList[typeList.length-1].toLowerCase();
		}else {
			throw new Exception("Got empty type, check it!");
		}
	}
}
