/*
 * Copyright © 2017, Inmarsat Global Ltd.
 * This file cannot be copied and/or distributed outside Inmarsat without the express permission given by
 * Inmarsat’s Legal Affairs.  All permission requests should be requested via LegalCompliance@inmarsat.com.
 */

package com.inmarsat.esb.templates.beans;

import org.apache.camel.Handler;
import org.apache.log4j.Logger;

/**
 * Represents the service where the customer accepts messages.
 * This implementation is a simple console message.
 * @author andrew.hickey
 *
 */
public class CustomerServiceBean {
	
	private static final Logger logger = Logger.getLogger(CustomerServiceBean.class);
	
	@Handler
    public void sendMessage(String message) {
		logger.info(message);
    }

}
