package com.ibm.dba.ontology;


import com.fasterxml.jackson.databind.JsonNode;
import com.ibm.dba.exceptions.InvalidJsonException;
import com.ibm.dba.util.Utilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * Reads Json Input Logs
 */
public class InputReader {

    private final String LOGPATH;
    private String inputFile;
    private String outputFile;
    private JsonNode inputLog;


    public InputReader(String _LOGPATH, String _inputFile, String _outputFile){
        this.LOGPATH = _LOGPATH;
        this.inputFile = _inputFile;
        this.outputFile = _outputFile;

    }

    /***
     * Reads input Json into a log Json data structure
     */
    public void readInputFile() throws InvalidJsonException, IOException {
        this.inputLog = Utilities.readJsonFromFile(this.LOGPATH+this.inputFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(this.LOGPATH+this.outputFile,false));
        // write header
        writer.write("sessionID,query_id,content,measure,dimension,filter,BIPattern\n");
        System.out.println("Input File read completed..");
        int stateId= 1;
        String prevSessionID="";
        for(JsonNode state: this.inputLog){
            StringBuilder stateFeatures = new StringBuilder();
            if(state.get("metaAgent")!=null && state.get("metaAgent").size()>0){
                if(state.get("metaAgent").get("action")!=null && String.valueOf(state.get("metaAgent").get("action")).equals("\"deadEnd\""))
                    continue;
                stateFeatures.append(state.get("sessionID") + ",");
                if (prevSessionID.equalsIgnoreCase(String.valueOf(state.get("sessionID"))))
                    stateId++;
                else
                    stateId = 1;
                stateFeatures.append(stateId + ",");
                stateFeatures.append(state.get("content") + ",");
                stateFeatures.append(extractMeasures(state) + ",");
                stateFeatures.append(extractDimensions(state) + ",");
                stateFeatures.append(extractFilters(state) + ",");
                stateFeatures.append("\""+extractBIPattern(state)+"\"" + "\n");

                writer.write(String.valueOf(stateFeatures));
                prevSessionID = String.valueOf(state.get("sessionID"));


            }
        }
        writer.close();
    }



    /***
     * Extracts Measures from the state log
     * @param state
     * @return
     */
    private String extractMeasures(JsonNode state) {
        String measures="";
        HashMap<String,String> measureMap = new HashMap<>();

        if(!String.valueOf(state.get("intent")).equalsIgnoreCase("chartCompare")){
            String measure = String.valueOf(state.get("metaAgent").get("measures"));
            //@TODO Extract measure from the content
            String content = String.valueOf(state.get("content"));
            content= content.replaceAll("^\"|\"$", "");
            Pattern pattern = Pattern.compile("Here's the chart for(.*?)by", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                measure = matcher.group(1);
            }

            measureMap.put(measure.trim().replaceAll("\\(.*\\)", "").trim(), generateAggregation());
        }else{
            //@TODO Extract both measures and add to the hashmap
            measureMap.put(String.valueOf(state.get("metaAgent").get("measures")),
                    generateAggregation());
        }
        int count=0;
        for (Map.Entry<String, String> entry : measureMap.entrySet()) {
            measures="\""+entry.getValue()+"(" +entry.getKey()+")\"";
            if(measureMap.size()>1 && count<measureMap.size()-1){
                measures+=";";
            }
            count++;
         //   measures+="]";
        }

        return measures;
    }

    /***
     * Generates random aggregations for measures
     * @return
     */
    private String generateAggregation(){
        String aggregation="";
        int randomNum = ThreadLocalRandom.current().nextInt(1, 5 + 1);
        switch (randomNum){
            case 1:
                aggregation="MIN";
                break;
            case 2:
                aggregation="MAX";
                break;
            case 3:
                aggregation="SUM";
                break;
            case 4:
                aggregation="COUNT";
                break;
            case 5:
                aggregation="AVERAGE";
                break;
            default:
                aggregation="SUM";

        }
        return aggregation;
    }

    /***
     * Extracts Dimensions from the state log
     * @param state
     * @return
     */
    private String extractDimensions(JsonNode state) {
        String dimensions="";
        //Extract dimension from the content
        String[] dimArray = String.valueOf(state.get("metaAgent").get("dimensions")).split(":");
        String dimStr = "";
        int count = 0;
        for (String dim : dimArray) {
            if (dim.length() > 0) {
                dimStr += dim;
            }
            if (count < dimArray.length - 2) {
                dimStr += ";";
            }
            count++;
        }
     //   incorrect dimensions start with C_ which need an extraction
        if(dimStr.startsWith("\"C_")) {
            dimStr = "";
            String content = String.valueOf(state.get("content"));
            content = content.replaceAll("^\"|\"$", "");
            Pattern pattern = Pattern.compile("Here's the chart for(.*?)by(.*)", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                dimStr = matcher.group(2);
            }
            dimStr = dimStr.split("for")[0];
            if(dimStr.contains(". {")) {
                dimStr = dimStr.split("\\.")[0];
            }
            dimensions += "\"" + dimStr.trim().replaceAll("\\(.*\\)", "").trim() + "\"";
        } else{
            dimensions += dimStr.trim().replaceAll("\\(.*\\)", "").trim();
        }
        //dimensions+=String.valueOf(state.get("metaAgent").get("dimensions"));
        return dimensions;
    }

    /***
     * Extracts Filters from the state log
     * @param state
     * @return
     */
    private String extractFilters(JsonNode state) {
        String filter="N/A";
        if(state.get("metaAgent").get("time")!=null){
            filter = String.valueOf(state.get("metaAgent").get("time").get("type"));
        }
        if(filter.isEmpty()|| filter.equals("\"\"") || filter.length()==0){
            filter="\""+"N/A"+"\"";
        }
        return filter;
    }

    /***
     * Returns the BI pattern extracted from the state log
     * @param state
     * @return
     */
    private String extractBIPattern(JsonNode state) {
        String intent="";
        switch(String.valueOf(state.get("intent"))){
            case "\"chartRequest\"":
                intent = "BI Analysis Query Pattern";
                break;
            case "\"chartCompare\"":
                intent = "BI Comparison Pattern";
                break;
            case "\"chartGeneric\"":
                intent = "BI Analysis Query Pattern";
                break;
            case "\"chartTrend\"":
                intent = "BI Trend Pattern";
                break;
            case "\"chartBenchmark\"":
                intent = "BI Comparison Pattern";
                break;
            default:
                intent = "BI Analysis Query Pattern";


        }
        return intent;
        //@TODO: extract appropriate BI pattern from intent
    }


    public static void main(String[] args) throws InvalidJsonException, IOException {
        //_LOGPATH="/Users/ahquamar/Documents/WCS/HealthCare/ConversationalBI/SummerProject_2020/ConversationalBI-Recommendations/";
        /*
        ======= To Compile: =====
        mvn -DskipTests clean package
        ======= To Run: =====
        mvn exec:java -Dexec.mainClass="InputReader.java" -Dexec.args="/Users/Vamsi.Meduri/Documents/Work/software/ConversationalBI-Recommendations/" -Dexec.cleanupDaemonThreads=false
         */
        assert args.length == 1;
        InputReader ir = new InputReader(args[0]+
                                                    "WAALogs/","waa-logs-jan-22.json","stateFeaturesFile.txt");
        ir.readInputFile();
    }
}
