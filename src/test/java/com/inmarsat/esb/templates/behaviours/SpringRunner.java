/*
 * Copyright © 2017, Inmarsat Global Ltd.
 * This file cannot be copied and/or distributed outside Inmarsat without the express permission given by
 * Inmarsat’s Legal Affairs.  All permission requests should be requested via LegalCompliance@inmarsat.com.
 */

package com.inmarsat.esb.templates.behaviours;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.Main;

public class SpringRunner {
	
	private Main main;
	
	public SpringRunner() {
		this.main = new Main();
	}
	
	public static void main(String[] args) throws Exception {
		new SpringRunner().start();
	}
	
	public CamelContext getCamelContext() {
		return main.getCamelContexts().get(0);
	}
	
	public void start() throws Exception {
	    main.setApplicationContextUri("META-INF/spring/camelContextTest.xml");
	    main.start();    
	}
	
	public void stop() throws Exception {
		main.stop();
	}

}
