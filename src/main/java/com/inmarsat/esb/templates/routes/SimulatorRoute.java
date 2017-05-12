/*
 * Copyright © 2017, Inmarsat Global Ltd.
 * This file cannot be copied and/or distributed outside Inmarsat without the express permission given by
 * Inmarsat’s Legal Affairs.  All permission requests should be requested via LegalCompliance@inmarsat.com.
 */

package com.inmarsat.esb.templates.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;

/**
 * A simple route to simulate incoming messages.
 * Implemented as a timer that transmits "Hello Customer!"
 * @author andrew.hickey
 *
 */
public class SimulatorRoute extends RouteBuilder {

	@PropertyInject("SimulatorRoute.AutoStart")
	private boolean simulatorRouteAutoStart;
	
	@PropertyInject("HelloCustomerRoute.Endpoint")
	private String helloCustomerRouteEndpoint;

	@Override
	public void configure() throws Exception {
		from("timer://foo?fixedRate=true&period=6000&repeatCount=20")
			.routeId(SimulatorRoute.class.getSimpleName())
				.autoStartup(simulatorRouteAutoStart).process(new Processor() {
					public void process(Exchange exchange) {
						Message in = exchange.getIn();
						in.setBody("Hello Customer!");
					}
				}).to(this.helloCustomerRouteEndpoint);
	}

	public void setSimulatorRouteAutoStart(boolean incomingRouteAutoStart) {
		this.simulatorRouteAutoStart = incomingRouteAutoStart;
	}

}
