/*
 * Copyright © 2017, Inmarsat Global Ltd.
 * This file cannot be copied and/or distributed outside Inmarsat without the express permission given by
 * Inmarsat’s Legal Affairs.  All permission requests should be requested via LegalCompliance@inmarsat.com.
 */

package com.inmarsat.esb.templates.behaviours;

import org.apache.camel.ConsumerTemplate;
import org.apache.camel.ProducerTemplate;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;

public class HelloWorldRouting {
	
	private SpringRunner springRunner;
	private ProducerTemplate producerTemplate;
	private ConsumerTemplate consumerTemplate;
	private String message;
	
	@Before
	public void beforeScenario() throws Exception {
		this.springRunner = new SpringRunner();
		this.springRunner.start();
		this.producerTemplate = springRunner.getCamelContext().createProducerTemplate();
		this.consumerTemplate = springRunner.getCamelContext().createConsumerTemplate();
		this.consumerTemplate.start();
	}
	
	@After
	public void afterScenario() throws Exception {
    	this.consumerTemplate.stop();
    	this.springRunner.stop();
	}
		
	@Given("^a 'Hello Customer!' message is sent$")
	public void givenGpsMessageInJMSQueue() throws Exception {
		this.message = "Hello Customer!";
	}
	
	@When("^the message is retrieved by the service$")
	public void retreiveMessageByService() {
		this.producerTemplate.sendBody("seda:stubbedIn",this.message);
	}
	
	@Then("^'Hello Customer! This is a secret message!' message is delivered to the customer service$")
	public void contentsOfLinkQualityMessageAreCorrectlyDelivered() throws Exception {
		String response = this.consumerTemplate.receiveBody("seda:stubbedOut", String.class);		
    	Assert.assertEquals("Hello Customer! This is a secret message!", response);
	}

}
