package com.ibm.dba.ontology;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by ahquamar on 2/5/19.
 * utility for adding ontology elements
 */
public class OntoUtils {


    /***
     * Adds a concept to the given ontology with a specific IRI
     * @param owlOntology
     * @param iri
     * @param conceptName
     */
    public static OWLClass addOWLConcept(OWLOntology owlOntology, IRI iri, String conceptName){
        //normalize
        conceptName=normalize(conceptName);


        OWLDataFactory df = owlOntology.getOWLOntologyManager().getOWLDataFactory();
        OWLClass concept = df.getOWLClass(iri+"#"+conceptName);
        OWLDeclarationAxiom axiom = df.getOWLDeclarationAxiom(concept);
        owlOntology.addAxiom(axiom);
        return concept;
    }

    /***
     * Adds a concept to the given ontology with a specific IRI and a label
     * @param owlOntology
     * @param iri
     * @param conceptName
     */
    public static OWLClass addOWLConcept(OWLOntology owlOntology, IRI iri, String conceptName, String label){
        //normalize
        conceptName=normalize(conceptName);
        label = normalize(label);

        OWLDataFactory df = owlOntology.getOWLOntologyManager().getOWLDataFactory();
        OWLClass concept = df.getOWLClass(iri+"#"+conceptName);
        OWLDeclarationAxiom axiom = df.getOWLDeclarationAxiom(concept);
        owlOntology.addAxiom(axiom);

        //add a label to the class
        OWLAnnotation classAnnotation = df.getOWLAnnotation(df.getRDFSLabel(),df.getOWLLiteral(label));
        OWLAxiom annotationAxiom = df.getOWLAnnotationAssertionAxiom(concept.getIRI(), classAnnotation);
        owlOntology.addAxiom(annotationAxiom);
        return concept;
    }

    /***
     * Creates a parent child (SubClass)  relationbship
     * @param owlOntology
     * @param parent
     * @param child
     */
    public static void addOWLConceptSubClass(OWLOntology owlOntology, OWLClass parent,OWLClass child){
        OWLDataFactory df = owlOntology.getOWLOntologyManager().getOWLDataFactory();
        OWLSubClassOfAxiom subClassAxiom = df.getOWLSubClassOfAxiom(child,parent);
        owlOntology.addAxiom(subClassAxiom);
    }




    /***
     * Add data property to a domain concept
     * @param owlOntology
     * @param iri
     * @param domain
     * @param propertyName
     * @param dataType
     * @return
     */
    public static OWLDataProperty addOWLDataProperty(OWLOntology owlOntology, IRI iri, OWLClass domain, String propertyName,  String dataType ){



        //normalize
        propertyName=normalize(propertyName);


        OWLDataFactory df = owlOntology.getOWLOntologyManager().getOWLDataFactory();
        OWLDataProperty dataProperty = df.getOWLDataProperty(iri+"#"+propertyName);

        //add data property axiom

        OWLDataPropertyAxiom dpAxiom = df.getOWLFunctionalDataPropertyAxiom(dataProperty);
        owlOntology.addAxiom(dpAxiom);

        //add domain axiom for the data property
        OWLDataPropertyDomainAxiom dpDomainAxiom = df.getOWLDataPropertyDomainAxiom(dataProperty, domain);
        owlOntology.addAxiom(dpDomainAxiom);

        //add data type or range axiom
        //OWLDataPropertyRangeAxiom rangeAxiom = df.getOWLDataPropertyRangeAxiom(dataProperty, OntoUtils.getOWLDataType(owlOntology,dataType));
        dataType = normalizeDataType(dataType);
        OWLDataPropertyRangeAxiom rangeAxiom = df.getOWLDataPropertyRangeAxiom(dataProperty, OntoUtils.getOWL2DataType(owlOntology,dataType));

        owlOntology.addAxiom(rangeAxiom);


        //add sub data property as sub data property of top level dataproperty
        OWLDataProperty topdataProperty = df.getOWLDataProperty("http://www.w3.org/2002/07/owl#topDataProperty");
        OWLSubDataPropertyOfAxiom owlSubDataPropertyOfAxiom = df.getOWLSubDataPropertyOfAxiom(dataProperty,topdataProperty);
        owlOntology.addAxiom(owlSubDataPropertyOfAxiom);


        return dataProperty;


    }


    /***
     * Add data property to a domain concept with a label
     * @param owlOntology
     * @param iri
     * @param domain
     * @param propertyName
     * @param dataType
     * @return
     */
    public static OWLDataProperty addOWLDataProperty(OWLOntology owlOntology, IRI iri, OWLClass domain, String propertyName,  String dataType, String label ){



        //normalize
        propertyName=normalize(propertyName);


        OWLDataFactory df = owlOntology.getOWLOntologyManager().getOWLDataFactory();
        OWLDataProperty dataProperty = df.getOWLDataProperty(iri+"#"+propertyName);

        //add data property axiom

        OWLDataPropertyAxiom dpAxiom = df.getOWLFunctionalDataPropertyAxiom(dataProperty);
        owlOntology.addAxiom(dpAxiom);

        //add domain axiom for the data property
        OWLDataPropertyDomainAxiom dpDomainAxiom = df.getOWLDataPropertyDomainAxiom(dataProperty, domain);
        owlOntology.addAxiom(dpDomainAxiom);

        //add data type or range axiom
        //OWLDataPropertyRangeAxiom rangeAxiom = df.getOWLDataPropertyRangeAxiom(dataProperty, OntoUtils.getOWLDataType(owlOntology,dataType));
        dataType = normalizeDataType(dataType);
        OWLDataPropertyRangeAxiom rangeAxiom = df.getOWLDataPropertyRangeAxiom(dataProperty, OntoUtils.getOWL2DataType(owlOntology,dataType));

        owlOntology.addAxiom(rangeAxiom);


        //add data property as sub data property of top level dataproperty
        OWLDataProperty topdataProperty = df.getOWLDataProperty("http://www.w3.org/2002/07/owl#topDataProperty");
        OWLSubDataPropertyOfAxiom owlSubDataPropertyOfAxiom = df.getOWLSubDataPropertyOfAxiom(dataProperty,topdataProperty);
        owlOntology.addAxiom(owlSubDataPropertyOfAxiom);


        //add label to data property
        OWLAnnotation dataPropertyAnnotation = df.getOWLAnnotation(df.getRDFSLabel(),df.getOWLLiteral(label));
        OWLAxiom annotationAxiom = df.getOWLAnnotationAssertionAxiom(dataProperty.getIRI(), dataPropertyAnnotation);
        owlOntology.addAxiom(annotationAxiom);

        return dataProperty;


    }

    /***
     * Get the owl ontology data type for data properties
     * @param owlOntology
     * @param dataType
     * @return
     */

    public static OWLDatatype getOWLDataType(OWLOntology owlOntology, String dataType){
        OWLDatatype owlDataType = null;
        OWLDataFactory df = owlOntology.getOWLOntologyManager().getOWLDataFactory();

        switch(dataType.toLowerCase()){
            case "string":
                owlDataType = df.getStringOWLDatatype();
                break;
            case "long":
                owlDataType = df.getDoubleOWLDatatype();
                break;
            case "double":
                owlDataType = df.getDoubleOWLDatatype();
                break;
            case "boolean":
                owlDataType = df.getBooleanOWLDatatype();
                break;
            case "decimal":
                owlDataType = df.getDoubleOWLDatatype();
                break;

            default:
                owlDataType = df.getStringOWLDatatype();
        }
        return owlDataType;
    }


    /***
     * Get the owl ontology data type (OWL2Datatype for data properties
     * @param owlOntology
     * @param dataType
     * @return
     */

    public static OWL2Datatype getOWL2DataType(OWLOntology owlOntology, String dataType){
        OWL2Datatype owlDataType = null;


        switch(dataType.toLowerCase()){
            case "string":
                owlDataType = OWL2Datatype.XSD_STRING;
                break;
            case "long":
                owlDataType = OWL2Datatype.XSD_LONG;
                break;
            case "double":
                owlDataType = OWL2Datatype.XSD_DOUBLE;
                break;
            case "boolean":
                owlDataType = OWL2Datatype.XSD_BOOLEAN;
                break;
            case "decimal":
                owlDataType = OWL2Datatype.XSD_DOUBLE;
                break;
            case "timestamp":
                owlDataType = OWL2Datatype.XSD_DATE_TIME;
                break;
            case "datetime":
                owlDataType = OWL2Datatype.XSD_DATE_TIME;
                break;
            case "char":
                owlDataType = OWL2Datatype.XSD_STRING;
                break;
            case "smallint":
                owlDataType = OWL2Datatype.XSD_UNSIGNED_SHORT;
                break;
            case "integer":
                owlDataType = OWL2Datatype.XSD_INTEGER;
                break;
            default:
                owlDataType = OWL2Datatype.XSD_STRING;
        }
        return owlDataType;
    }


    /***
     * Add OWL object property
     * @param owlOntology
     * @param iri
     * @param domainConcept
     * @param rangeConcept
     * @param propertyName
     * @return
     */
    public static OWLObjectProperty addOWLObjectProperty(OWLOntology owlOntology, IRI iri, String domainConcept, String rangeConcept, String propertyName){

        //normalize
        propertyName=normalize(propertyName);

        domainConcept=normalize(domainConcept);

        rangeConcept=normalize(rangeConcept);

        OWLDataFactory df = owlOntology.getOWLOntologyManager().getOWLDataFactory();

        //Create object property
        OWLObjectProperty objectProperty = df.getOWLObjectProperty(iri+"#"+propertyName);


        //Get reference to domain and range classes
        OWLClass domainClass = df.getOWLClass(iri+"#"+domainConcept);
        OWLClass rangeClass = df.getOWLClass(iri+"#"+rangeConcept);


        //add object property domain axiom
        OWLObjectPropertyDomainAxiom domainAxiom = df.getOWLObjectPropertyDomainAxiom(objectProperty,domainClass);
        owlOntology.addAxiom(domainAxiom);

        //add object property range axiom
        OWLObjectPropertyRangeAxiom rangeAxiom = df.getOWLObjectPropertyRangeAxiom(objectProperty,rangeClass);
        owlOntology.addAxiom(rangeAxiom);

        //make the relationship functional
        OWLFunctionalObjectPropertyAxiom functionalObjectPropertyAxiom = df.getOWLFunctionalObjectPropertyAxiom(objectProperty);
        owlOntology.addAxiom(functionalObjectPropertyAxiom);


        return objectProperty;
    }


    /***
     * Add OWL object property with name and label
     * @param owlOntology
     * @param iri
     * @param domainConcept
     * @param rangeConcept
     * @param propertyName
     * @return
     */
    public static OWLObjectProperty addOWLObjectProperty(OWLOntology owlOntology, IRI iri, String domainConcept, String rangeConcept, String propertyName, String label){

        //normalize
        propertyName=normalize(propertyName);

        domainConcept=normalize(domainConcept);

        rangeConcept=normalize(rangeConcept);

        label = normalize(label);

        OWLDataFactory df = owlOntology.getOWLOntologyManager().getOWLDataFactory();

        //Create object property
        OWLObjectProperty objectProperty = df.getOWLObjectProperty(iri+"#"+propertyName);


        //Get reference to domain and range classes
        OWLClass domainClass = df.getOWLClass(iri+"#"+domainConcept);
        OWLClass rangeClass = df.getOWLClass(iri+"#"+rangeConcept);


        //add object property domain axiom
        OWLObjectPropertyDomainAxiom domainAxiom = df.getOWLObjectPropertyDomainAxiom(objectProperty,domainClass);
        owlOntology.addAxiom(domainAxiom);

        //add object property range axiom
        OWLObjectPropertyRangeAxiom rangeAxiom = df.getOWLObjectPropertyRangeAxiom(objectProperty,rangeClass);
        owlOntology.addAxiom(rangeAxiom);

        //make the relationship functional
        OWLFunctionalObjectPropertyAxiom functionalObjectPropertyAxiom = df.getOWLFunctionalObjectPropertyAxiom(objectProperty);
        owlOntology.addAxiom(functionalObjectPropertyAxiom);

        //add label to object property
        OWLAnnotation objectPropertyAnnotation = df.getOWLAnnotation(df.getRDFSLabel(),df.getOWLLiteral(label));
        OWLAxiom annotationAxiom = df.getOWLAnnotationAssertionAxiom(objectProperty.getIRI(), objectPropertyAnnotation);
        owlOntology.addAxiom(annotationAxiom);


        return objectProperty;
    }

    /***
     * Add OWL object property with name and label using OWL Classes as domain and range concepts
     * @param owlOntology
     * @param iri
     * @param domainClass
     * @param rangeClass
     * @param propertyName
     * @return
     */
    public static OWLObjectProperty addOWLObjectProperty(OWLOntology owlOntology, IRI iri, OWLClass domainClass, OWLClass rangeClass, String propertyName, String label){

        //normalize
        propertyName=normalize(propertyName);


        label = normalize(label);

        OWLDataFactory df = owlOntology.getOWLOntologyManager().getOWLDataFactory();

        //Create object property
        OWLObjectProperty objectProperty = df.getOWLObjectProperty(iri+"#"+propertyName);


        //add object property domain axiom
        OWLObjectPropertyDomainAxiom domainAxiom = df.getOWLObjectPropertyDomainAxiom(objectProperty,domainClass);
        owlOntology.addAxiom(domainAxiom);

        //add object property range axiom
        OWLObjectPropertyRangeAxiom rangeAxiom = df.getOWLObjectPropertyRangeAxiom(objectProperty,rangeClass);
        owlOntology.addAxiom(rangeAxiom);

        //make the relationship functional
        OWLFunctionalObjectPropertyAxiom functionalObjectPropertyAxiom = df.getOWLFunctionalObjectPropertyAxiom(objectProperty);
        owlOntology.addAxiom(functionalObjectPropertyAxiom);

        //add label to object property
        OWLAnnotation objectPropertyAnnotation = df.getOWLAnnotation(df.getRDFSLabel(),df.getOWLLiteral(label));
        OWLAxiom annotationAxiom = df.getOWLAnnotationAssertionAxiom(objectProperty.getIRI(), objectPropertyAnnotation);
        owlOntology.addAxiom(annotationAxiom);


        return objectProperty;
    }

    /***
     * Adds an OWL annotation to the given class
     * @param owlOntology
     * @param annotation
     * @param annotationClass
     */
    public static void addOWLAnnotation(OWLOntology owlOntology, String annotation, String value, OWLClass annotationClass){
        OWLDataFactory df = owlOntology.getOWLOntologyManager().getOWLDataFactory();
        //OWLAnnotation classAnnotation = df.getOWLAnnotation(df.getRDFSComment(),df.getOWLLiteral(annotation));
        //OWLAnnotation classAnnotation = df.getOWLAnnotation(df.getOWLAnnotationProperty(IRI.create(OWLRDFVocabulary.RDFS_COMMENT.getIRI().toString(),":"+annotation)),df.getOWLLiteral(value));
        OWLAnnotation classAnnotation = df.getOWLAnnotation(df.getOWLAnnotationProperty(IRI.create("#rdfs:"+annotation)),df.getOWLLiteral(value));

        OWLAxiom annotationAxiom = df.getOWLAnnotationAssertionAxiom(annotationClass.getIRI(), classAnnotation);
        owlOntology.addAxiom(annotationAxiom);

    }


    /***
     * Adds an OWL annotation to the given data property
     * @param owlOntology
     * @param annotation
     * @param value
     * @param annotationDataProperty
     */
    public static void addOWLAnnotation(OWLOntology owlOntology, String annotation, String value, OWLDataProperty annotationDataProperty){
        OWLDataFactory df = owlOntology.getOWLOntologyManager().getOWLDataFactory();
        //OWLAnnotation classAnnotation = df.getOWLAnnotation(df.getRDFSComment(),df.getOWLLiteral(annotation));
        //OWLAnnotation classAnnotation = df.getOWLAnnotation(df.getOWLAnnotationProperty(IRI.create(OWLRDFVocabulary.RDFS_COMMENT.getIRI().toString(),":"+annotation)),df.getOWLLiteral(value));
        OWLAnnotation dataPropertyAnnotation = df.getOWLAnnotation(df.getOWLAnnotationProperty(IRI.create("#rdfs:"+annotation)),df.getOWLLiteral(value));

        OWLAxiom annotationAxiom = df.getOWLAnnotationAssertionAxiom(annotationDataProperty.getIRI(), dataPropertyAnnotation);
        owlOntology.addAxiom(annotationAxiom);

    }
    /***
     * Normalize concept and relationship names
     * @param name
     */
    public static String normalize(String name){
        return name.replaceAll("\\(","_").replaceAll("\\)","");

    }

    /***
     * Normalizes data type removing arguemnts
     * @param name
     * @return
     */
    public static String normalizeDataType(String name){
        return name.split("\\(")[0];
    }



    ////Methods to traverse and access the ontology

    /**
     * Get a reasoner for the ontology
     * @param owlOntology
     * @return
     */
    public static OWLReasoner getOWLResasoner(OWLOntology owlOntology){
        OWLReasoner owlReasoner = null;
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
        OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
        owlReasoner = reasonerFactory.createReasoner(owlOntology, config);
        return owlReasoner;
    }


    /***
     * Checks consistency of ontology
     * @param owlOntology
     * @return
     */
    public static boolean checkConsistency(OWLOntology owlOntology){
        OWLReasoner reasoner = getOWLResasoner(owlOntology);
        reasoner.precomputeInferences();
        return reasoner.isConsistent();

    }

    public static OWLOntology loadOntology(String path) {
        OWLOntology owlOntology = null;
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        File file = new File(path);
        try{
            owlOntology = manager.loadOntologyFromOntologyDocument(file);
            System.out.println("Ontology Loaded");

        }catch(OWLOntologyCreationException e){
            System.out.println("Could not load ontology..");
            e.printStackTrace();
        }

        //check consistency of ontology
        if(checkConsistency(owlOntology)){
            System.out.println("Ontology consistent");
        }else{
            System.out.println("Ontology not consistent");
        }


        return owlOntology;
    }

    /****
     * Get all concepts from the ontology
     * @return
     */
    public static Set<OWLClass> getAllConcepts(OWLOntology owlOntology){
        return owlOntology.getClassesInSignature();
    }

    /***
     * Returns the set of immediate children for a given parent.
     * @param owlOntology
     * @param parent
     * @return
     */
    public static NodeSet<OWLClass> getSubConcepts(OWLOntology owlOntology, String parent){
        OWLDataFactory df = owlOntology.getOWLOntologyManager().getOWLDataFactory();
        OWLReasoner reasoner = getOWLResasoner(owlOntology);
        OWLClass parentClass = df.getOWLClass(owlOntology.getOntologyID().getOntologyIRI().get()+"#"+parent);
        return reasoner.getSubClasses(parentClass, true);
    }


    /****
     *
     * @param owlOntology
     * @param parent
     * @return
     */
    public static Set<OWLClass> getSubConcepts(OWLOntology owlOntology, OWLClass parent){
        //OWLDataFactory df = owlOntology.getOWLOntologyManager().getOWLDataFactory();
        OWLReasoner reasoner = getOWLResasoner(owlOntology);
        return reasoner.getSubClasses(parent, true).getFlattened();
    }

    /***
     * Get all super classes for a given class
     * @param owlOntology
     * @param child
     * @return
     */
    public static Set<OWLClass> getSuperClasses(OWLOntology owlOntology, OWLClass child){
        OWLReasoner reasoner = getOWLResasoner(owlOntology);
        return reasoner.getSuperClasses(child,false).getFlattened();
    }


    /***
     * Verifies if a class is a decedent of a given parent
     * @param owlOntology
     * @param c
     * @param parent
     * @return
     */
    public static boolean decendentOf(OWLOntology owlOntology, OWLClass c, String parent){
        OWLClass decendent= OntoUtils.getClass(owlOntology,parent);
        OWLReasoner reasoner = getOWLResasoner(owlOntology);
        return reasoner.getSuperClasses(c,false).containsEntity(decendent);
    }

    /***
     * Returns concepts annotated with a particular type in the ontology
     * @param owlOntology
     * @param type
     * @return
     */
    public static List<OWLClass> getConcepts(OWLOntology owlOntology, String type){
        OWLDataFactory df = owlOntology.getOWLOntologyManager().getOWLDataFactory();
        ArrayList<OWLClass> concepts = new ArrayList<>();
        for(OWLClass c:     getAllConcepts(owlOntology)){
            for(OWLAnnotationAssertionAxiom aa: owlOntology.getAnnotationAssertionAxioms(c.getIRI())){
                if(aa.getProperty().getIRI().getRemainder().get().equalsIgnoreCase("Type")) {
                    //System.out.println(aa.getProperty() + ":" + ((OWLLiteral)aa.getValue()).getLiteral());
                    if(((OWLLiteral)aa.getValue()).getLiteral().equalsIgnoreCase(type)){
                        //System.out.println(aa);
                        concepts.add(c);
                    }
                }

            }
        }

        return concepts;

    }

    /***
     * Gets the rdfs label for the class
     * @param concept
     * @return
     */
    public static String getClassLabel(OWLOntology owlOntology, OWLClass concept){
        String label="";
        for(OWLAnnotationAssertionAxiom aa: owlOntology.getAnnotationAssertionAxioms(concept.getIRI())){
            if(aa.getProperty().isLabel()){
                label = ((OWLLiteral)aa.getValue()).getLiteral();
                break;
            }
        }
        return  label;
    }


    /***
     * Get owl class with a particular label
     * @param owlOntology
     * @param label
     * @return
     */
    public static OWLClass getClass(OWLOntology owlOntology, String label){
        for(OWLClass c: getAllConcepts(owlOntology)){
            for(OWLAnnotationAssertionAxiom aa: owlOntology.getAnnotationAssertionAxioms(c.getIRI())){
                if(aa.getProperty().isLabel()){
                    if(((OWLLiteral)aa.getValue()).getLiteral().equalsIgnoreCase(label)){
                        return c;
                    }
                }
            }
        }
        return null;
    }


    public static OWLDataProperty getDataPropertyFromLabel(OWLOntology owlOntology, String _label) {
        for (OWLDataPropertyDomainAxiom d : owlOntology.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN)) {

            for (OWLDataProperty dp : d.getDataPropertiesInSignature()) {

                for(OWLAnnotationAssertionAxiom aa: owlOntology.getAnnotationAssertionAxioms(dp.getIRI())){
                    if(aa.getProperty().isLabel()){
                        String label = ((OWLLiteral)aa.getValue()).getLiteral();
                        if(label.equalsIgnoreCase(_label)){
                            return dp;
                        }
                        break;
                    }
                }
            }

        }

        return null;
    }

    /***
     * Returns the data properties associated with the class
     * @param owlOntology
     * @param domainConcept
     * @return
     */
    public static List<OWLDataProperty> getDataProperties(OWLOntology owlOntology, OWLClass domainConcept){

        List<OWLDataProperty> dps  = new ArrayList<>();


        for(OWLDataPropertyDomainAxiom d : owlOntology.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN)){
            if(d.getDomain().equals(domainConcept)){
                for(OWLDataProperty dp : d.getDataPropertiesInSignature()){
                    dps.add(dp);
                }
            }
        }
        return dps;

    }

    /****
     * Returns a list of object properties for a given domain concept
     * @param owlOntology
     * @param domainConcept
     * @return
     */
    public static List<OWLObjectProperty> getObjectProperties(OWLOntology owlOntology, OWLClass domainConcept){
        List<OWLObjectProperty> ops = new ArrayList<>();

        for(OWLObjectPropertyDomainAxiom o : owlOntology.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN)){
            if(o.getDomain().equals(domainConcept)){
                for(OWLObjectProperty op: o.getObjectPropertiesInSignature()){
                    ops.add(op);
                }
            }

        }
        return ops;
    }

    /***
     * Returns the range concept of the object property
     * @param owlOntology
     * @param objectProperty
     * @return
     */
    public static OWLClass getObjectPropertyRange(OWLOntology owlOntology, OWLObjectProperty objectProperty){
        OWLClass range =null;

        for(OWLObjectPropertyRangeAxiom o : owlOntology.getAxioms(AxiomType.OBJECT_PROPERTY_RANGE)){
            if(o.getProperty().equals(objectProperty)){
                range=o.getRange().asOWLClass();
            }

        }
        return range;
    }

    /***
     * Return appropriate data property for a particular domain concept
     * @param owlOntology
     * @param domainConcept
     * @param owlDataProperty
     * @return
     */
    public static OWLDataProperty getDataProperty(OWLOntology owlOntology, OWLClass domainConcept, OWLDataProperty owlDataProperty){
        for(OWLDataPropertyDomainAxiom d : owlOntology.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN)){
            if(d.getDomain().equals(domainConcept)){
                for(OWLDataProperty dp : d.getDataPropertiesInSignature()){
                   if(dp.equals(owlDataProperty)){
                       return dp;
                   }
                }
            }
        }
        return null;
    }

    /***
     * Return the domain class of an owl property
     * @param owlOntology
     * @param owlDataProperty
     * @return
     */
    public static OWLClass getDataPropertyDomain(OWLOntology owlOntology,OWLDataProperty owlDataProperty){
        for(OWLDataPropertyDomainAxiom d : owlOntology.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN)){

            for(OWLDataProperty dp : d.getDataPropertiesInSignature()){
                if(dp.equals(owlDataProperty)){
                    return d.getDomain().asOWLClass();
                }

            }
        }
        return null;
    }

    /***
     * Get data property label
     * @param owlOntology
     * @param dataProperty
     * @return
     */
    public static String getDataPropertyLabel(OWLOntology owlOntology, OWLDataProperty dataProperty){
        String label="";
        for(OWLAnnotationAssertionAxiom aa: owlOntology.getAnnotationAssertionAxioms(dataProperty.getIRI())){
            if(aa.getProperty().isLabel()){
                label = ((OWLLiteral)aa.getValue()).getLiteral();
                break;
            }
        }
        return  label;
    }

    /***
     * Get object Propoerty label
     * @param owlOntology
     * @param objectProperty
     * @return
     */
    public static String getObjectPropertyLabel(OWLOntology owlOntology, OWLObjectProperty objectProperty ){
        String label="";
        for(OWLAnnotationAssertionAxiom aa: owlOntology.getAnnotationAssertionAxioms(objectProperty.getIRI())){
            if(aa.getProperty().isLabel()){
                label = ((OWLLiteral)aa.getValue()).getLiteral();
                break;
            }
        }
        return label;
    }

    /***
     * Saves a given ontology to the specified file path
     * @param owlOntology
     * @param outputFile
     * @throws FileNotFoundException
     * @throws OWLOntologyStorageException
     */
    public static void saveOntology(OWLOntology owlOntology,String outputFile) throws FileNotFoundException, OWLOntologyStorageException {
        OWLManager.createOWLOntologyManager().saveOntology(owlOntology,new RDFXMLOntologyFormat(), new FileOutputStream(outputFile));
    }

}
