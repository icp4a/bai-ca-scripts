package com.ibm.dba.util;

/**
 * Created by ahquamar on 1/16/19.
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.dba.exceptions.InvalidJsonException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Abdul Quamar
 * For reading Json input formats
 */
public class Utilities {

    public static JsonNode readJsonFromInputStream(InputStream inputStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(inputStream);
        return json;
    }

    public static JsonNode readJsonFromFile(String filepath) throws InvalidJsonException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(new File(filepath));
            return json;
        } catch (IOException ex) {
            throw new InvalidJsonException(ex.toString());
        }
    }






}