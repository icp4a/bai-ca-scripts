package com.ibm.dba.ontology;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.dba.exceptions.ConfigurationException;
import com.ibm.dba.exceptions.InvalidJsonException;
import com.ibm.dba.util.Utilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ibm.nlq.dba.twxparser;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by ahquamar on 2/8/19.
 */
public abstract class OntologyGenerator {

    final Logger logger = LoggerFactory.getLogger(OntologyGeneratorDBA.class);
    protected JsonNode dataModule;
    protected JsonNode dataModuleUG;
    protected OWLOntologyManager ontologyManager;
    protected OWLDataFactory df;
    protected OWLOntology ontology;
    protected File owloutputFile;
    protected String mappingFile;
    protected IRI iri;
    protected String version="";
    protected String inputDataModule;
    protected HashMap<String, HashSet<String>> processMap;



    /***
     * Constructor
     * Takes the input file located in resources and produces the output file in the target classes folder
     * @param _iri
     * @param _version
     * @param inputModule
     * @param outputFile
     */
    public OntologyGenerator(String _iri, String _version, String outputFile, String inputModule){
        //generate the ontology manager object
        this.ontologyManager = OWLManager.createOWLOntologyManager();

        //create the ontology IRI
        this.iri = IRI.create(_iri+"/"+_version);

        this.inputDataModule = inputModule;


        //create a new empty ontology with the given IRI
        try{
            this.ontology = this.ontologyManager.createOntology(this.iri);
            this.df = this.ontology.getOWLOntologyManager().getOWLDataFactory();
            System.out.println(this.ontology);
        }catch(OWLOntologyCreationException e){
            e.printStackTrace();
        }


        //create output file in appropriate location
        this.owloutputFile = new File(new File(getClass().getClassLoader().getResource(inputModule).getPath()).getParent()+"/"+outputFile);

        try {
            this.owloutputFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /***
     * Constructor
     * Takes as input only iri and verision
     * @param _iri
     * @param _version
     */
    public OntologyGenerator(String _iri, String _version){
        //generate the ontology manager object
        this.ontologyManager = OWLManager.createOWLOntologyManager();

        //create the ontology IRI
        this.iri = IRI.create(_iri+"/"+_version);


        //create a new empty ontology with the given IRI
        try{
            this.ontology = this.ontologyManager.createOntology(this.iri);
            this.df = this.ontology.getOWLOntologyManager().getOWLDataFactory();
            System.out.println(this.ontology);
        }catch(OWLOntologyCreationException e){
            e.printStackTrace();
        }




    }


    /***
     * Generate ontology
     * @throws InvalidJsonException
     * @throws FileNotFoundException
     * @throws OWLOntologyStorageException
     * @throws ConfigurationException
     */
    public void generateOntology() throws InvalidJsonException, IOException, OWLOntologyStorageException, ConfigurationException {



        //read the JSON data module
        readDataModule();

        //create concepts for the ontology including their data properties
        createConcepts();

        //add relationships between concepts
        createRelationships();

        //save generated ontology to owl output file
        saveOntology();


    }

    /***
     * Generate ontology
     * @throws InvalidJsonException
     * @throws FileNotFoundException
     * @throws OWLOntologyStorageException
     * @throws ConfigurationException
     */
    public void generateOntology(String inputFilePath, String outputFile) throws InvalidJsonException, IOException, OWLOntologyStorageException, ConfigurationException {



        //read the JSON data module
        readDataModule(inputFilePath);

        //create concepts for the ontology including their data properties
        createConcepts();

        //add relationships between concepts
        createRelationships();

        //create output file in appropriate location
        this.owloutputFile = new File(outputFile);
        try {
            this.owloutputFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //save generated ontology to owl output file
        saveOntology();


    }

    /***
     * Generate ontology and pkfk mapping
     * @param inputFilePath
     * @param outputFile
     * @param _mappingFile
     * @throws InvalidJsonException
     * @throws FileNotFoundException
     * @throws OWLOntologyStorageException
     * @throws ConfigurationException
     */
    public void generateOntology(String inputFilePath, String outputFile, String _mappingFile) throws InvalidJsonException, IOException, OWLOntologyStorageException, ConfigurationException {



        //read the JSON data module
        //readDataModule(inputFilePath);
        readTwxDataModule(inputFilePath);

        //create concepts for the ontology including their data properties
        createConcepts();

        //add relationships between concepts
        createRelationships();

        //create output file in appropriate location
        this.owloutputFile = new File(outputFile);
        try {
            this.owloutputFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.mappingFile = _mappingFile;

        //save generated ontology to owl output file
        saveOntology();


    }

    abstract void createConcepts();
    abstract void createRelationships();


    /***
     * Reads data module from resources into a Json data structure
     */
    public void readDataModule() throws ConfigurationException {
        try{

            this.dataModule = Utilities.readJsonFromFile(getClass().getClassLoader().getResource(this.inputDataModule).getFile());

        }
        catch(InvalidJsonException e){
            e.printStackTrace();
            throw new ConfigurationException();
        }

    }

    /***
     * Reads data module from resources into a Json data structure
     */
    public void readDataModule(String filePath) throws ConfigurationException {
        try{

            this.dataModule = Utilities.readJsonFromFile(filePath);

        }
        catch(InvalidJsonException e){
            e.printStackTrace();
            throw new ConfigurationException();
        }

    }

    /***
     * Reads twx data module and uses the twx parser to extract input Json
     * @param dirPath
     * @throws IOException
     */
    public void readTwxDataModule(String dirPath) throws IOException {
        twxparser tp = new twxparser();
        boolean b = tp.parse(dirPath);
        if(b){
            ObjectMapper objectMapper = new ObjectMapper();
            String inputJson = tp.getJSON().toString();
            JsonNode jn = objectMapper.readTree(inputJson);
            this.dataModule = jn.findPath("ProcessModelJSONs").get(1).findPath("jsonData");
            this.dataModuleUG = jn.findPath("UserGroupJSONs");

        }
    }

    /**
     * Save the ontolgy to a specified owl file
     * @throws FileNotFoundException
     * @throws OWLOntologyStorageException
     */
    private void saveOntology() throws IOException, OWLOntologyStorageException {
        //this.ontologyManager.saveOntology(this.ontology,new FunctionalSyntaxDocumentFormat(), new FileOutputStream(this.owloutputFile));
        //this.ontologyManager.saveOntology(this.ontology,new OWLXMLOntologyFormat(), new FileOutputStream(this.owloutputFile));
        this.ontologyManager.saveOntology(this.ontology,new RDFXMLOntologyFormat(), new FileOutputStream(this.owloutputFile));

        if(this.processMap!=null){
            //write the mapping to a file
            JsonObject o=new JsonObject();

            for(Map.Entry e: this.processMap.entrySet()){
                //Gson gson = new Gson();
                //o.add(e.getKey().toString(),gson.toJsonTree(e.getValue()));
                o.addProperty(e.getKey().toString(),e.getValue().toString().replaceAll("\\[", "").replaceAll("\\]",""));
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(this.mappingFile,false));
            writer.write(o.toString());

            writer.close();

        }

    }


}
