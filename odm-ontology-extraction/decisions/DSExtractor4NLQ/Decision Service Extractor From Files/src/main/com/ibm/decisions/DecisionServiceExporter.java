package com.ibm.decisions;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.ibm.automation.Attribute;
import com.ibm.automation.AutomationService;
import com.ibm.automation.Concept;
import com.ibm.automation.Constant;
import com.ibm.automation.Parameter;
import com.ibm.automation.Signature;
import com.ibm.decisions.OntologyExtractor2;
import com.ibm.rules.bal.language.Bal;
import com.ibm.rules.build.BuildHelper;
import com.ibm.rules.files.BomEntry;
import com.ibm.rules.files.BomPathEntry;
import com.ibm.rules.files.DecisionDirectory;
import com.ibm.rules.files.DecisionResourceManager;
import com.ibm.rules.files.Deployment;
import com.ibm.rules.files.DeploymentOperation;
import com.ibm.rules.files.Operation;
import com.ibm.rules.files.OperationVariable;
import com.ibm.rules.files.RuleProject;
import com.ibm.rules.files.Variable;
import com.ibm.rules.files.VariableSet;
import com.ibm.rules.parse.ParsingEnvironment;
import com.ibm.rules.parse.*;
import com.ibm.rules.parse.ParsingEnvironmentCreationResults;
import com.ibm.tatzia.ontology.Ontology;


import Text2OWL.Load3;
import Text2OWL.Write;
import ilog.rules.bom.IlrAttribute;
import ilog.rules.bom.IlrMember;
import ilog.rules.bom.IlrObjectModel;
import ilog.rules.vocabulary.model.IlrConcept;
import ilog.rules.vocabulary.model.IlrConceptInstance;
import ilog.rules.vocabulary.model.IlrFactType;
import ilog.rules.vocabulary.model.IlrRole;
import ilog.rules.vocabulary.model.IlrSentence;
import ilog.rules.vocabulary.model.bom.IlrBOMVocabulary;
import ilog.rules.vocabulary.model.bom.IlrBOMVocabularyHelper;
import ilog.rules.vocabulary.model.helper.IlrVocabularyHelper;
import ilog.rules.vocabulary.verbalization.IlrVerbalizerHelper;

public class DecisionServiceExporter {

	static void usage() {
		System.out.println(
				"usage : [-verbose] -p <path to ODM project> -d <deployment name> [-o <operation name>] -r <rule set path> -output <output json file path>\n"
						+ "<path to ODM project>: The fully specified path to the ODM eclipse project\n"
						+ "<deployment name>: The name of the deployment in the ODM project\n"
						+ "<operation name>: The name of the operation in the specified deployment, optional if only one operation exist in the deployment.\n"
						+ "<rule set path>>: The ruleset path (example :/mydeployment/1.0/Miniloan_ServiceRuleset/1.0) \n"
		                + "<output json file path>>: The path and file name of the output json file ");

		System.exit(1);
	}

	public static void main(String[] args) throws Exception {
		
		//String outputFolder = System.getProperty("user.dir")+"/odm_model";
		//String projectDir = "/Users/gby-struggle/Desktop/work/ontology_odm/nlq/decisions/Decision Service sample projects/Loan Validation Service/Loan Validation Service";
		//String deploymentName = "test deployment";
		//String operationName = "loan validation with score and grade";
		//String ruleSetPath = "/test_deployment/1.0/loan_validation_with_score_and_grade/1.0";
		//String outputFolder = System.getProperty("user.dir")+"/shipment";
		//String projectDir = "/Users/gby-struggle/Desktop/work/ontology_odm/nlq/decisions/DSExtractor4NLQ/Decision Service Extractor From Files/ShipmentPricing";
		//String deploymentName = "Shipment Pricing Deployment";
		//String operationName = "Shipment Pricing Operation";
		//String ruleSetPath = "/Shipment_Pricing_RuleApp/1.0/Shipment_Pricing/1.0";

		//String schema = "odm-timeseries";
		//String FilePath = outputFolder + "/odm_model";
		//String FilePath = outputFolder;
		//String outputPath = FilePath + "/" + schema + ".json";
		//String schema = "shimpmentPricing";
		//String FilePath = outputFolder + "/odm_model";
		//String FilePath = outputFolder;
		//String outputPath = FilePath + "/" + schema + ".json";
		
		String projectDir = null;
		String deploymentName = null;
		String operationName = null;
		String ruleSetPath = null;
		String outputPath = null;
		
		String absolutePath = "";
		String schema = "";
		
		
		/*File dir = new File(FilePath);
		if(!dir.exists()){
			dir.mkdirs();
		}
		System.out.println(outputPath);*/

		String parsing = "";
		boolean deployementSeen = false;
		boolean operationSeen = false;
		boolean directorySeen = false;
		boolean ruleSetPathSeen = false;
		boolean outputPathSeen = false;
		boolean verbose = false;

		// Parsing arguments

		for (String arg : args) {
			switch (arg) {

			case "-verbose": // verbose mode
				parsing = "";
				verbose = true;
				break;

			case "-d": // deployment name
				if (deployementSeen)
					usage();
				deployementSeen = true;
				parsing = arg;
				break;

			case "-p": // project directory
				if (directorySeen)
					usage();
				directorySeen = true;
				parsing = arg;

				break;

			case "-o": // operation name
				if (operationSeen)
					usage();
				operationSeen = true;
				parsing = arg;

				break;

			case "-r": // operation name
				if (ruleSetPathSeen)
					usage();
				ruleSetPathSeen = true;
				parsing = arg;

				break;

			case "-output": // operation name
				if (outputPathSeen)
					usage();
				outputPathSeen = true;
				parsing = arg;
				
				break;
			default:
				if (arg.length() == 0)
					break;
				switch (parsing) {
				case "-output":
					if (outputPath == null)
						outputPath = arg;
					else
						outputPath += " " + arg;
					break;
				case "-p":
					if (projectDir == null)
						projectDir = arg;
					else
						projectDir += " " + arg;
					break;
				case "-d":
					if (deploymentName == null)
						deploymentName = arg;
					else
						deploymentName += " " + arg;
					break;
				case "-o":
					if (operationName == null)
						operationName = arg;
					else
						operationName += " " + arg;
					break;
				case "-r":
					if (ruleSetPath == null)
						ruleSetPath = arg;
					else
						ruleSetPath += " " + arg;
					break;
				}
				break;
			}
		}
	
		if (projectDir == null || deploymentName == null || ruleSetPath == null || outputPath == null) {
			usage();
		}
		
		File file = new File(outputPath);
		String parent = file.getAbsoluteFile().getParent();
		if (outputPath.contains("/")) {
			String [] parentList = outputPath.split("/");
			absolutePath = parentList[parentList.length-1];
		}
		else 
			absolutePath = outputPath;
		String [] schemaList = absolutePath.split("\\.");
		schema = schemaList[0];

		URI ruleProjectFolderUri = Paths.get(projectDir).toUri();

		if (!Files.isDirectory(Paths.get(ruleProjectFolderUri))) {
			System.out.println("Project directory does not exist");
			usage();
		}
		
		DecisionResourceManager resourceManager = BuildHelper.newDecisionResourceManager();
		DecisionDirectory decisionDirectory = new DecisionDirectory(ruleProjectFolderUri, resourceManager);
		RuleProject ruleProject = decisionDirectory.getRuleProject();

		// Get the operation folder containing the deployments
		String operationFolderName = ruleProject.getOperationFolderName();

		if (operationFolderName == null) {
			System.out.println("the project does not have an operation folder");
			usage();
		}
		URI operationFolderURI = Paths.get(ruleProjectFolderUri).resolve(operationFolderName).toUri();

		// Looking for requested deployment
		Deployment deployment = null;

		List<String> availableDeployments = new ArrayList<String>();

		for (URI depURI : resourceManager.getFileSystem().listFiles(operationFolderURI)) {
			if (depURI.toString().endsWith(".dep")) {
				Deployment dep = decisionDirectory.loadDeployment(depURI);
				if (deploymentName.equals(dep.getName().toString())) {
					deployment = dep; // Found the expected deployment
				}
				availableDeployments.add(dep.getName().toString());
			}
		}

		if (deployment == null) {
			// Expected deployment not found.
			System.out.println("Wrong deployment: " + deploymentName);
			System.out.println("Available deployments are: " + availableDeployments.toString());
			usage();
		}

		// looking for requested deployment operation

		Operation operation = null;
		List<String> operationNames = new ArrayList<String>();
		List<DeploymentOperation> operations = deployment.getOperations();
		if (operationName == null) {
			// No operation name specified, verify that there is only one operation
			if (operations.size() != 1) {
				System.out.println("Please specifiy the operation name using -o");
				usage();
			}
			operation = decisionDirectory.loadOperation(operations.get(0).getOperationUri());
		} else {
			for (DeploymentOperation o : operations) {
				Operation op = decisionDirectory.loadOperation(o.getOperationUri());
				operationNames.add(op.getName());
				if (operationName.equals(op.getName())) {
					operation = op;
					break;
				}
			}
		}

		if (operation == null) {
			System.out.println("Specified operation does not exist. Use one of " + operationNames);
			usage();
		}

		// collecting variables and bom information from the target rule project.
		Map<String, VariableSet> variableSets = new HashMap<>();
		List<URI> bomEntries = new ArrayList<URI>();

		collectBomAndVars(operation.getTargetRuleProjectUri(), bomEntries, variableSets, resourceManager);

		AutomationService automationService = new AutomationService();
		Signature signature = automationService.getSignature();
		if (verbose) {
			System.out
					.println("Signature for operation '" + operation.getName() + "' in deployment '" + deploymentName + "' :");
			System.out.println("Parameters:");
		}
		for (OperationVariable v : operation.getReferencedVariables()) {

			Parameter parameter = new Parameter();
			parameter.setName(v.getVariableName());
			parameter.setDirection(v.getDirection().toString().toLowerCase()); 
			VariableSet variableSet = variableSets.get(v.getVariableSetUuid());
			for (Variable var : variableSet.getVariables()) {
				if (var.getName().equals(v.getVariableName())) {
					parameter.setType(var.getType());
					parameter.setVerbalization(var.getVerbalization());
					break;
				}
			}
			if (verbose) {
				System.out.println(parameter);
			}
			signature.addParameter(parameter);
		}

		ParsingEnvironmentCreationParameters parameters = BuildHelper.newParsingEnvironmentCreationParameters();
		parameters.getBomEntries().addAll(bomEntries);
		parameters.getLocales().add(Locale.US); 
		ParsingEnvironmentCreationResults results = ParsingEnvironment.create(parameters);
		results.printMessages(System.out);
		if (!results.hasErrors()) {
			ParsingEnvironment parsingEnvironment = results.getParsingEnvironment();
			IlrBOMVocabulary voc = parsingEnvironment.getVocabulary(parsingEnvironment.getParserManager(Bal.LANGUAGE),
					Locale.US);
			IlrObjectModel bom = voc.getObjectModel();

			if (voc != null) {
				List<IlrConcept> concepts = voc.getConcepts();
				if (verbose) {
					System.out.println("Vocabulary:");
					System.out.println("Found vocabulary with " + concepts.size() + " concepts");
				}

				for (IlrConcept c : concepts) {
					String conceptName = c.getName();
					String conceptLabel = c.getLabel();
					String pluralLabel = IlrVerbalizerHelper.getPlural(c, voc);

					if (verbose) {
						System.out.println("Concept [" + conceptName + "] " + conceptLabel + " (" + pluralLabel + ")");
					}
					Concept concept = new Concept(conceptName, conceptLabel, pluralLabel);
					automationService.add(concept);

					List<IlrFactType> heldFactTypes = c.getHeldFactTypes();
					for (IlrFactType ft : heldFactTypes) {
						IlrMember member = IlrBOMVocabularyHelper.getMember(voc.getObjectModel(), ft);
						if (member instanceof IlrAttribute) {
							String attrName = member.getFullyQualifiedName();
							String type = ((IlrAttribute) member).getAttributeType().getFullyQualifiedName();
							IlrRole ownedRole = IlrVocabularyHelper.getOwnedRole(ft);
							if (ownedRole != null) {
								String roleLabel = ownedRole.getLabel();
								String pluralRoleLabel = IlrVerbalizerHelper.getPlural(ownedRole, voc);
								String verbalizationExample = IlrVerbalizerHelper.getVerbalizationExample(ownedRole,
										voc);
								List sentences = ft.getSentences();
								Iterator<IlrSentence> itSentence = (Iterator<IlrSentence>) sentences.iterator();
								while (itSentence.hasNext()) {
									IlrSentence sentence = itSentence.next();
									// System.out.println(sentence);
									// System.out.println(sentence.getTextTemplate());
								}

								if (verbose) {
									System.out.println("-- Attribute [" + attrName + ":" + type + "] " + roleLabel
											+ " (" + pluralRoleLabel + ")");
								}
								Attribute attribute = new Attribute(attrName, type, roleLabel, pluralRoleLabel,
										verbalizationExample);
								concept.add(attribute);
							} else {
								// System.out.println("ELSE");
							}
						}
					}

					List<IlrConceptInstance> conceptInstances = voc.getConceptInstances(c);
					if (conceptInstances != null && conceptInstances.size() > 0) {
						for (IlrConceptInstance ci : conceptInstances) {
							IlrAttribute constant = IlrBOMVocabularyHelper.getConstant(voc.getObjectModel(), ci);
							if (constant != null) {
								String attrName = constant.getFullyQualifiedName();
								String constantLabel = ci.getLabel();
								String pluralConstantLabel = IlrVerbalizerHelper.getPlural(ci, voc);

								Constant constant1 = new Constant(attrName, constantLabel, pluralConstantLabel);
								concept.add(constant1);
								if (verbose) {
									System.out.println("-- Constant [" + attrName + "] " + constantLabel + " ("
											+ pluralConstantLabel + ")");
								}
							}
						}
					}
				}

			}
		}
		// Properties
		automationService.setVersion("1.0.0");
		automationService.setAssetPath(ruleSetPath);
		automationService.toJSON(outputPath);
		// take model.json as input to feed into ontology_generatioon function for odm 
		//1. generate the mapping and relation
		OntologyExtractor2.extractOntologyandMapping(parent, schema);
		String input = parent+"/"+schema+".txt";
		String pathOWL = parent;
		//2. generate owl using relation text
		String output = new Write().doit(new Load3().load(input,schema),pathOWL);
		//3. create the ontology with raw .owl
		Ontology o = new Ontology(parent,true);
		//4. generate syn.xml
		OntologyExtractor2.useJSONToGetSyn(o,parent,schema);
		OntologyExtractor2.writeSynFile(o, input, parent);
		//5. generate enhanced owl
		output = new Write().doit(new Load3().load(input,schema),pathOWL);
		System.out.println("Owl generated with syn, mapping json generated!");
		
	}

	private static void collectBomAndVars(URI ruleProjectFolderUri, List<URI> bomEntries,
			Map<String, VariableSet> variableSets, DecisionResourceManager resourceManager) {

		DecisionDirectory directory = new DecisionDirectory(ruleProjectFolderUri, resourceManager);
		RuleProject ruleProject = directory.getRuleProject();
		List<BomPathEntry> entries = ruleProject.getBomPathEntries();

		for (BomPathEntry e : entries) {
			URI uri = ((BomEntry) e).getUri();
			if (uri.toString().endsWith(".bom"))
				bomEntries.add(uri);

			if (Files.isDirectory(Paths.get(uri))) {
				collectBomAndVars(uri, bomEntries, variableSets, resourceManager);
			}
		}
		// looking for variable set
		String sourceFolderName = ruleProject.getSourceFolderName();
		URI sourceFolderURI = Paths.get(ruleProjectFolderUri).resolve(sourceFolderName).toUri();

		for (URI u : resourceManager.getFileSystem().listFiles(sourceFolderURI)) {
			if (u.toString().endsWith(".var")) {
				VariableSet variableSet = directory.loadVariableSet(u);
				variableSets.put(variableSet.getUuid(), variableSet);
			}
		}
	}

}
