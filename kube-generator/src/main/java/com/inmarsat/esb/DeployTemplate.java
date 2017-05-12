/*
 * Copyright © 2017, Inmarsat Global Ltd.
 * This file cannot be copied and/or distributed outside Inmarsat without the express permission given by
 * Inmarsat’s Legal Affairs.  All permission requests should be requested via LegalCompliance@inmarsat.com.
 */

package com.inmarsat.esb;

import java.util.HashMap;
import java.util.Map;

import io.fabric8.kubernetes.api.model.EditableKubernetesList;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.Quantity;

public class DeployTemplate extends Template {
	
	public DeployTemplate() {
		super.builder = new KubernetesListBuilder();
		super.templateItems = builder.addNewTemplateItem();
	}
	
	public void addTemplateMetadata() {
		Map<String, String> annotations = new HashMap<>();
		annotations.put("description", Template.DESCRIPTION);
		annotations.put("iconClass", "icon-java");
		super.templateItems.withNewMetadata()
			.withName(Template.ARTIFACT_NAME + "-prod")
			.withAnnotations(annotations)
		.endMetadata();
	}
	
	public void addParameters() {
		super.addParameters();
		this.templateItems.addNewParameter()
			.withName("IMAGE_NAME")
			.withValue(Template.ARTIFACT_NAME)
		.endParameter();
		this.templateItems.addNewParameter()
			.withName("IMAGE_TAG")
			.withValue("stage")
		.endParameter();	
		this.templateItems.addNewParameter()
			.withName("SOURCE_NAMESPACE")
			.withValue("staging")
		.endParameter();	
	}
	
	public void addDeploymentConfig() {			
		this.builder.addNewDeploymentConfigItem()
		.withNewMetadata()
			.withName(Template.ARTIFACT_NAME)
			.withCreationTimestamp(null)
			.withLabels(getDeployConfigLabels())
		.endMetadata()
		.withNewSpec()
			.withNewStrategy()
				.withType("Rolling")
				.withNewRollingParams()
					.withNewMaxSurge("30%")
					.withNewMaxUnavailable("20%")
					.withTimeoutSeconds(new Long(240))
				.endRollingParams()
				.withNewResources()
				.endResources()
			.endStrategy()
			.addNewTrigger()
				.withType("ConfigChange")
			.endTrigger()
			.addNewTrigger()
				.withType("ImageChange")
				.withNewImageChangeParams()
					.withAutomatic(false)
					.addToContainerNames(Template.ARTIFACT_NAME)
					.withNewFrom()
						.withKind("ImageStreamTag")
						.withNamespace("${SOURCE_NAMESPACE}")
						.withName("${IMAGE_NAME}:${IMAGE_TAG}")
					.endFrom()
				.endImageChangeParams()
			.endTrigger()
			.withReplicas(1)
			.addToSelector(getDeployConfigLabels())
			.withNewTemplate()
				.withNewMetadata()
					.withCreationTimestamp(null)
					.withLabels(getDeployConfigLabels())
				.endMetadata()
				.withNewSpec()
					.addNewContainer()
						.withName(Template.ARTIFACT_NAME)
						.withImage("${SOURCE_NAMESPACE}/${IMAGE_NAME}:${IMAGE_TAG}")
						.withNewReadinessProbe()
							.withNewExec()
								.addToCommand("/bin/bash")
								.addToCommand("-c")
								.addToCommand("(curl -f 127.0.0.1:8778) >/dev/null 2>&1; test $? != 7")
							.endExec()
							.withInitialDelaySeconds(30)
							.withTimeoutSeconds(5)
						.endReadinessProbe()
						.addNewPort()
							.withContainerPort(8778)
							.withName("jolokia")
							.withProtocol("TCP")
						.endPort()
						.addNewEnv()
							.withName("TZ")
							.withValue("Etc/UTC")
						.endEnv()
						.addNewEnv()
							.withName("JAVA_OPTIONS")
							.withValue(Template.JAVA_OPTIONS)
						.endEnv()
						.withNewResources()
							.addToLimits("memory", new Quantity(Template.CONTAINER_MEMORY_LIMIT))
						.endResources()
						.addNewVolumeMount()
							.withMountPath("/etc/configmap")
							.withName(Template.ARTIFACT_NAME + "-configmap")
							.withReadOnly(true)
						.endVolumeMount()
						.addNewVolumeMount()
							.withMountPath("/etc/secret")
							.withName(Template.ARTIFACT_NAME + "-secret")
							.withReadOnly(true)
						.endVolumeMount()						
					.endContainer()
					.addNewVolume()
						.withNewConfigMap()
							.withName(Template.ARTIFACT_NAME)
						.endConfigMap()
						.withName(Template.ARTIFACT_NAME + "-configmap")
					.endVolume()
					.addNewVolume()
						.withNewSecret()
							.withSecretName(Template.ARTIFACT_NAME)
						.endSecret()
						.withName(Template.ARTIFACT_NAME + "-secret")
					.endVolume()
				.endSpec()
			.endTemplate()
		.endSpec()
		.withNewStatus()
		.endStatus()
		.endDeploymentConfigItem();
	}
	
	private Map<String, String> getDeployConfigLabels() {
		Map<String, String> deployConfigLabels = new HashMap<>();
		deployConfigLabels.put("app", Template.ARTIFACT_NAME);
		deployConfigLabels.put("artifact", Template.ARTIFACT_NAME);
		deployConfigLabels.put("component", Template.ARTIFACT_NAME);
		deployConfigLabels.put("container", "java");
		deployConfigLabels.put("deploymentconfig", Template.ARTIFACT_NAME);
		deployConfigLabels.put("group", Template.GROUP_ID);
		deployConfigLabels.put("project", Template.ARTIFACT_NAME);
		deployConfigLabels.put("provider", "s2i");
		return deployConfigLabels;
	}
	
    public EditableKubernetesList build() {	
    	super.build();
		this.addTemplateMetadata();
		this.addParameters();
		this.addDeploymentConfig();
    	super.builder = super.templateItems.endTemplateItem();
    	return super.builder.build();
    }

}
