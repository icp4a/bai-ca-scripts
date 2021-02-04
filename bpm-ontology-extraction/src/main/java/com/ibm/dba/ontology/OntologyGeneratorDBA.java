package com.ibm.dba.ontology;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import com.ibm.dba.exceptions.ConfigurationException;
import com.ibm.dba.exceptions.InvalidJsonException;
import com.ibm.dba.util.Utilities;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/***
 * Created by Abdul Quamar on Oct 01, 2020
 * Generates an ontology for the DBA process model
 */

public class OntologyGeneratorDBA extends OntologyGenerator{

    /***
     * Constructor
     * @param _iri
     * @param _version
     * @param outputFile
     * @param inputModule
     *
     */
    public JsonNode processProperties;
    private List<String> UGDataProperties;

    public OntologyGeneratorDBA( String _iri, String _version, String outputFile, String inputModule) throws ConfigurationException {
        //call parent class constructor
        super(_iri,_version,outputFile,inputModule);
        this.processMap = new HashMap<>();
        //load the process properties from file
        try{
            this.processProperties = Utilities.readJsonFromInputStream(getClass().getClassLoader().getResourceAsStream("BPM-Static-Fields.json"));


        }
        catch (IOException ex) {
            throw new ConfigurationException();
        }
    }


    /***
     * Constructor
     * @param _iri
     * @param _version
     */
    public OntologyGeneratorDBA( String _iri, String _version) throws ConfigurationException {
        //call parent class constructor
        super(_iri,_version);

        this.processMap = new HashMap<>();
        this.UGDataProperties = new ArrayList<>();

        //load the process properties from file
        try{
            this.processProperties = Utilities.readJsonFromInputStream(getClass().getClassLoader().getResourceAsStream("BPM-Static-Fields.json"));


        }
        catch (IOException ex) {
            throw new ConfigurationException();
        }
    }

    /***
     * Adds a mapping to the process map
     * @param key
     * @param value
     */
    public void addMapping(String key, String value){
        if(this.processMap.containsKey(key)){
            this.processMap.get(key).add(value);
        }else{
            HashSet<String> pathList = new HashSet<>();
            pathList.add(value);
            this.processMap.put(key,pathList);
        }
    }


    /**
     * Create ontology concepts and add data properties to these concepts
     */
    public void createConcepts() {


        //create a concept called process and add some static data properties to it
        OWLClass processConcept = OntoUtils.addOWLConcept(this.ontology,this.iri,"Process" );

        //add static data properties
        Iterator<String> itr = this.processProperties.fieldNames();
        while(itr.hasNext()){
            String processProperty = itr.next();
            String dataType = this.processProperties.get(processProperty).asText();
            OntoUtils.addOWLDataProperty(this.ontology, this.iri, processConcept, processProperty, dataType);
            addMapping(processProperty,processProperty);
        }


        //create a concept called data and add the dynamic data properties to it after traversing the process-model.json file
        OWLClass dataConcept = OntoUtils.addOWLConcept(this.ontology,this.iri,"Data" );

        OntoUtils.addOWLDataProperty(this.ontology, this.iri, dataConcept, "autoTrackingName", "string");

        //extract autoTracking name
        String autoTrackingName = this.dataModule.get("rootElement").findPath("bpdExtension").findPath("autoTrackingName").asText();
        addMapping("Data","data."+autoTrackingName);

        //Traverse the process module json and extract dynamic data properties from io specification if any
        JsonNode iore = this.dataModule.get("rootElement").findPath("ioSpecification").findPath("dataInput");
        if(iore!=null && !iore.isMissingNode()) {
            addIOProperties(iore, dataConcept, autoTrackingName);
        }


        //Traverse the process module json and extract private dynamic data properties from flowElement if any
        JsonNode pre = this.dataModule.get("rootElement").findPath("flowElement");
        if(pre!=null && !pre.isMissingNode()){
            addPrivateProperties(pre, dataConcept, autoTrackingName);
        }


        //Traverse the process module Json and extract User Defined Group Data Properties
        if(this.dataModuleUG!=null && !this.dataModuleUG.isMissingNode()){
            for(JsonNode ugre: this.dataModuleUG){
                addUGDataProperties(ugre);
            }
        }


    }

    /***
     * Adds dynamic io data properties for a process
     * @param re
     */
    public void addIOProperties(JsonNode re, OWLClass dataConcept, String autoTrackingName){
        for(JsonNode element: re ){
            JsonNode dataProperty = element.findPath("name");

            //get property name
            String propertyName = dataProperty.asText().replaceAll("^\"+|\"+$", "");
            //.get.get("label").asText().trim().replaceAll("\\s", "");

            //Extract the data type
            JsonNode twxDataType =  element.findPath("itemSubjectRef");
            String dataType = getTwxDdataType(twxDataType.asText().replaceAll("^\"+|\"+$", ""));

            if(dataType==null || dataType.isEmpty())
                dataType="string";

            //add data property to ontology
            OWLDataProperty owlDP = OntoUtils.addOWLDataProperty(this.ontology, this.iri, dataConcept, propertyName, dataType);

            //add annotation for property description
            String dpDescription="";
            if(element.findPath("content").get(0)!=null){
                dpDescription = element.findPath("content").get(0).asText().replaceAll("^\"+|\"+$", "");
            }
            OntoUtils.addOWLAnnotation(this.ontology, "description", dpDescription, owlDP);


            //add appropriate mapping for the data property
            addMapping(propertyName,"data."+autoTrackingName+"."+propertyName+"."+dataType);

        }
    }


    /***
     * Adds dynamic io data properties for a process
     * @param re
     */
    public void addPrivateProperties(JsonNode re, OWLClass dataConcept, String autoTrackingName){
        for(JsonNode element: re ){
            JsonNode dataProperty = element.findPath("extensionElements").findPath("trackedField").findPath("sname");
            if(!dataProperty.isMissingNode()) {


                //get property name
                String propertyName = dataProperty.asText().replaceAll("^\"+|\"+$", "");
                //.get.get("label").asText().trim().replaceAll("\\s", "");

                //Extract the data type
                JsonNode twxDataType = element.findPath("itemSubjectRef");
                String dataType = getTwxDdataType(twxDataType.asText().replaceAll("^\"+|\"+$", ""));

                if (dataType == null || dataType.isEmpty())
                    dataType = "string";

                //add data property to ontology
                OWLDataProperty owlDP = OntoUtils.addOWLDataProperty(this.ontology, this.iri, dataConcept, propertyName, dataType);

                //add annotation for property description
                String dpDescription = "";
                if (element.findPath("name") != null) {
                    dpDescription = element.findPath("name").asText().replaceAll("^\"+|\"+$", "");
                }
                OntoUtils.addOWLAnnotation(this.ontology, "description", dpDescription, owlDP);


                //add appropriate mapping for the data property
                addMapping(propertyName, "data." + autoTrackingName + "." + propertyName + "." + dataType);
            }

        }
    }


    /****
     * Adds User Group Data Properties
     * @param ugre
     */
    public void addUGDataProperties(JsonNode ugre){

        //Extract user group name and add a concept for the same in the ontology
        JsonNode userGroup = ugre.findPath("rootElement").get(0).get("name");

        if(userGroup!=null && !userGroup.isMissingNode()){

            //Create a concept for the user group
            String userGroupName = userGroup.asText().replaceAll("^\"+|\"+$", "");
            OWLClass ugdataConcept = OntoUtils.addOWLConcept(this.ontology,this.iri,userGroupName );
            addMapping(userGroupName,"data."+userGroupName);
            this.UGDataProperties.add(userGroupName);

            //add data properties to the concept group
            JsonNode ugdp = ugre.findPath("rootElement").findPath("extensionElements").findPath("trackedVariables");
            for(JsonNode ugdpe: ugdp){
                JsonNode dataProperty = ugdpe.findPath("name");
                if(dataProperty!=null && !dataProperty.isMissingNode()){
                    String dataPropertyName = dataProperty.asText().replaceAll("^\"+|\"+$", "");
                    JsonNode dataType = ugdpe.findPath("type");
                    if(dataType!=null && !dataType.isMissingNode()){
                        String dataTypeString = dataType.asText().trim().replaceAll("^\"+|\"+$", "");
                        //add data property to ontology
                        OWLDataProperty owlDP = OntoUtils.addOWLDataProperty(this.ontology, this.iri, ugdataConcept, dataPropertyName, dataTypeString);

                        //add appropriate mapping for the data property
                        addMapping(dataPropertyName, "data." + userGroupName + "." + dataPropertyName + "." + dataTypeString);
                    }
                }
            }
        }

    }

    /***
     * Provides the twx data type from encode data type value
     * @param inputDataType
     * @return
     */
    public String getTwxDdataType(String inputDataType){
        String twxDataType = null;


        switch(inputDataType.toLowerCase()){
            case "itm.12.20fdb1a2-f6ec-462e-8627-d49859ba42ae":
                twxDataType = "timestamp";
                break;
            case "itm.12.68474ab0-d56f-47ee-b7e9-510b45a2a8be":
                twxDataType = "datetime";
                break;
            case "itm.12.3fa0d7a0-828a-4d60-99cc-db5ed143fc2d":
                twxDataType = "integer";
                break;
            case "itm.12.536b2aa5-a30f-4eca-87fa-3a28066753ee":
                twxDataType = "decimal";
                break;
            case "itm.12.db884a3c-c533-44b7-bb2d-47bec8ad4022":
                twxDataType = "string";
                break;
            case "itm.12.83ff975e-8dbc-42e5-b738-fa8bc08274a2":
                twxDataType = "boolean";
                break;
            case "itm.12.c09c9b6e-aabd-4897-bef2-ed61db106297":
                twxDataType = "string";
                break;
            default:
                twxDataType = "string";
        }
        return twxDataType;
    }


    /**
     * Create relationships between concepts
     * Uses the meta data tree view in the Data Module definition
     */
    public void createRelationships() {

        //create a relationship between process and its dynamic data
        String domainConcept = "Process";
        String rangeConcept = "Data";
        OntoUtils.addOWLObjectProperty(this.ontology,this.iri,domainConcept,rangeConcept,domainConcept+"_"+rangeConcept);

        //create relationships between data concept and user defined data concepts if any
        if(this.UGDataProperties.size()>0){
            String dc = "Data";
            for(String rc : this.UGDataProperties){
                OntoUtils.addOWLObjectProperty(this.ontology,this.iri,dc,rc,dc+"_"+rc);

            }
        }

    }




    public static void main(String[] args) throws ConfigurationException, IOException {

        OntologyGeneratorDBA og = new OntologyGeneratorDBA(args[0],args[1]);

        try {
            og.generateOntology(args[2],args[3],args[4]);

        } catch (InvalidJsonException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        }
        System.out.println("Done!!");

    }

}
