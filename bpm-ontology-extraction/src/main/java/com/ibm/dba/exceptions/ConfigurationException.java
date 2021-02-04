/*
 * BEGIN_COPYRIGHT
 *
 * IBM Confidential
 * OCO Source Materials
 *
 * com.ibm.watson.cnc:cnc-ingestion-batch
 * (C) Copyright IBM Corp. 2018 All Rights Reserved.
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 *
 * END_COPYRIGHT
 */
package com.ibm.dba.exceptions;

/**
 * Created by ahquamar on 11/13/18.
 */
public class ConfigurationException extends FatalException {

    public ConfigurationException(String message) {
        super(message);
    }
    
    public ConfigurationException() {
        super();
    }
}
