# DBA-OntologyCreator
Creates ontology from DBA Process meta-data

Running the DBA ontology builder:

1. cd to the dir containing the DBA ontology creator jar file "dba-ontology-creator-1.0-SNAPSHOT-jar-with-dependencies.jar"
2. The executable jar takes the following 5 arguments:
	i) Ontology IRI
	ii) Ontology version number
	iii) Input dir path for the twx folder: This folder contains all the twx files
	iv) Output owl filename with complete path for the generated ontology
	v) Output mapping filename with complete path for the generated mapping file containing mappings from the ontology elements to the path in json documents stored in elastic search.
3. Execute the following command:
java -jar dba-ontology-creator-1.0-SNAPSHOT-jar-with-dependencies.jar Ontology_IRI Ontology_Version twx_folder_name ontology_filename mapping_filename
4. Sample execution command: 
java -jar dba-ontology-creator-1.0-SNAPSHOT-jar-with-dependencies.jar http://dba.ibm.com/project/enhanced 1.0 path_to_twx_folder/NLQ_Sample-2.twx path_to_ontology_file/process-model.owl path_to_mapping_file/processMapping.json
