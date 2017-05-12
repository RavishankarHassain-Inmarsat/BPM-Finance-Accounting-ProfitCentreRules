/*
 * Copyright © 2017, Inmarsat Global Ltd.
 * This file cannot be copied and/or distributed outside Inmarsat without the express permission given by
 * Inmarsat’s Legal Affairs.  All permission requests should be requested via LegalCompliance@inmarsat.com.
 */

package com.inmarsat.esb.templates.routes;

import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;

/**
 * Accepts a message and appends a secret message from the properties file.
 * @author andrew.hickey
 *
 */
public class HelloCustomerRoute extends RouteBuilder {

	@PropertyInject("HelloCustomerRoute.AutoStart")
	private boolean helloCustomerRouteAutoStart;

	@PropertyInject("HelloCustomerRoute.Endpoint")
	private String helloCustomerRouteEndpoint;

	@PropertyInject("HelloCustomerRoute.SecretMessage")
	private String helloCustomerRouteSecretMessage;

	@PropertyInject("CustomerService.Endpoint")
	private String customerServiceEndpoint;

	@Override
	public void configure() throws Exception {
		from(this.helloCustomerRouteEndpoint)
			.routeId(HelloCustomerRoute.class.getSimpleName())
			.autoStartup(helloCustomerRouteAutoStart)
			.transform(body().append(" " + helloCustomerRouteSecretMessage))
			.to(this.customerServiceEndpoint);
	}

	public void setHelloCustomerRouteAutoStart(boolean helloCustomerRouteAutoStart) {
		this.helloCustomerRouteAutoStart = helloCustomerRouteAutoStart;
	}

	public void setHelloCustomerRouteEndpoint(String helloCustomerRouteEndpoint) {
		this.helloCustomerRouteEndpoint = helloCustomerRouteEndpoint;
	}

	public void setHelloCustomerRouteSecretMessage(String helloCustomerRouteSecretMessage) {
		this.helloCustomerRouteSecretMessage = helloCustomerRouteSecretMessage;
	}

	public void setCustomerServiceEndpoint(String customerServiceEndpoint) {
		this.customerServiceEndpoint = customerServiceEndpoint;
	}

}
