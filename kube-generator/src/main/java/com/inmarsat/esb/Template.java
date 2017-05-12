/*
 * Copyright © 2017, Inmarsat Global Ltd.
 * This file cannot be copied and/or distributed outside Inmarsat without the express permission given by
 * Inmarsat’s Legal Affairs.  All permission requests should be requested via LegalCompliance@inmarsat.com.
 */

package com.inmarsat.esb;

import java.util.HashMap;
import java.util.Map;

import io.fabric8.kubernetes.api.model.BaseKubernetesListFluent.TemplateItemsNested;
import io.fabric8.kubernetes.api.model.EditableKubernetesList;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;

public abstract class Template {
	
    protected KubernetesListBuilder builder;
    protected TemplateItemsNested<KubernetesListBuilder> templateItems;
    // should be the same as the artifact id in the pom:
    public static final String ARTIFACT_NAME = "accounting-profit-centre-rules";
    // can be named anything, but should be under 24 characters:
    public static final String SERVICE_NAME = "accounting-pro-cen-rules"; // must be < 24 chars
    // should be the same as the group id in the pom:
    public static final String GROUP_ID = "com.inmarsat.bpm.finance";
    // description of the application
    public static final String DESCRIPTION = "";
    // location of the source repo for sandbox development.
    public static final String GIT_REPO = "https://github.com/Inmarsat-itcloudservices/BPM-Finance-Accounting-ProfitCentreRules.git";
    // specifies the repo branch for sandbox development.
    public static final String GIT_REF = "develop";
    // specifies the repo subfolder for sandbox development (empty string if not necessary).
    public static final String GIT_SUBFOLDER = "";
    // specifies the secret name for sandbox development. Shouldn't have to change.
    public static final String OPENSHIFT_SECRET = "sshsecret";
    // specifies the host name for sandbox development. This should be unique. Don't change the
    // .89d9.dev-inmarsat.openshiftapps.com part unless you know why.
    public static final String ROUTE_HOST = "accounting-profit-centre-rules-v1-sandbox.89d9.dev-inmarsat.openshiftapps.com ";
    // At a minimum we must apply reasonable limitations on the JVM heap
	public static final String JAVA_OPTIONS = "-Xmx30m";
	// Apply a reasonable limit to the size of the container's memory
	public static final String CONTAINER_MEMORY_LIMIT = "256Mi";

	protected Map<String, String> getLabels() {
		Map<String, String> labels = new HashMap<>();
		labels.put("app", Template.ARTIFACT_NAME);
		labels.put("artifact", Template.ARTIFACT_NAME);
		labels.put("component", Template.ARTIFACT_NAME);
		labels.put("container", "java");
		labels.put("group", Template.GROUP_ID);
		labels.put("project", Template.ARTIFACT_NAME);
		labels.put("provider", "s2i");
		return labels;
	}
	
	protected Map<String, String> getSelector() {
		Map<String, String> selector = new HashMap<>();
		selector.put("group", Template.GROUP_ID);
		selector.put("project", Template.ARTIFACT_NAME);
		selector.put("provider", "s2i");
		return selector;
	}
	
	public void addParameters() {	
		this.templateItems.addNewParameter()
			.withValue(ROUTE_HOST)
			.withName("ROUTE_HOST")
			.withDescription("The Host of the route.")
		.endParameter();	
	}
    
    public void addService() {
    	// no service here.
    }
    
    public void addRoute() {
    	// no route here.
    }
    
    public EditableKubernetesList build() {	
    	this.addService();
    	this.addRoute();
    	return null;
    }
    
}

