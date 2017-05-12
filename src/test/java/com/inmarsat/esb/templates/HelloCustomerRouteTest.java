/*
 * Copyright © 2017, Inmarsat Global Ltd.
 * This file cannot be copied and/or distributed outside Inmarsat without the express permission given by
 * Inmarsat’s Legal Affairs.  All permission requests should be requested via LegalCompliance@inmarsat.com.
 */

package com.inmarsat.esb.templates;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import com.inmarsat.esb.templates.routes.HelloCustomerRoute;

public class HelloCustomerRouteTest extends CamelTestSupport {
	
	
	private static String helloWorldEndpoint = "direct:helloWorldEndpoint";
	private static String customerServiceEndpoint = "mock:customerServiceEndpoint";	
	
	@Test
	public void testHappyPath() throws Exception {
		getMockEndpoint(customerServiceEndpoint).expectedBodiesReceived("Hello Customer! This is a secret message!");
		template.sendBody(helloWorldEndpoint, "Hello Customer!");
		this.assertMockEndpointsSatisfied();
	}

	protected RouteBuilder createRouteBuilder() throws Exception {
		HelloCustomerRoute route = new HelloCustomerRoute();
		route.setHelloCustomerRouteAutoStart(true);
		route.setHelloCustomerRouteEndpoint(helloWorldEndpoint);
		route.setCustomerServiceEndpoint(customerServiceEndpoint);
		route.setHelloCustomerRouteSecretMessage("This is a secret message!");
		return route;
	}

}
