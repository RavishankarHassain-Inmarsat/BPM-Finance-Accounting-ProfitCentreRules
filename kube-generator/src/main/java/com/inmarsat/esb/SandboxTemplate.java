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

public class SandboxTemplate extends Template {

	public SandboxTemplate() {
		super.builder = new KubernetesListBuilder();
		super.templateItems = builder.addNewTemplateItem();
	}

	public void addTemplateMetadata() {
		Map<String, String> annotations = new HashMap<>();
		annotations.put("description", Template.DESCRIPTION);
		annotations.put("iconClass", "icon-java");
		super.templateItems.withNewMetadata()
			.withName(Template.ARTIFACT_NAME + "-sandbox")
			.withAnnotations(annotations)
		.endMetadata();
	}

	public void addParameters() {
		super.addParameters();
		this.templateItems.addNewParameter()
			.withGenerate("expression")
			.withName("BUILD_SECRET")
			.withDescription("The secret needed to trigger a build")
			.withFrom("[a-zA-Z0-9]{8}")
		.endParameter();
	}

	public void addImageStream() {
        this.builder.addNewImageStreamItem()
        .withNewMetadata()
            .withName(Template.ARTIFACT_NAME)
            .withCreationTimestamp(null)
            .withLabels(getLabels())
        .endMetadata()
        .withNewSpec()
        .endSpec()
        .withNewStatus()
        	.withDockerImageRepository("")
        .endStatus()
        .endImageStreamItem();
	}

	public void addBuildConfig() {
		this.builder.addNewBuildConfigItem()
		.withNewMetadata()
			.withName(Template.ARTIFACT_NAME)
			.withCreationTimestamp(null)
			.withLabels(getLabels())
		.endMetadata()
		.withNewSpec()
			.addNewTrigger()
				.withType("GitHub")
				.withNewGithub()
					.withSecret("${BUILD_SECRET}")
				.endGithub()
			.endTrigger()
			.addNewTrigger()
				.withType("Generic")
				.withNewGeneric()
					.withSecret("${BUILD_SECRET}")
				.endGeneric()
			.endTrigger()
			.addNewTrigger()
				.withType("ConfigChange")
			.endTrigger()
			.addNewTrigger()
				.withType("ImageChange")
				.withNewImageChange()
				.endImageChange()
			.endTrigger()
			.withNewSource()
				.withType("Git")
				.withNewGit()
					.withUri(Template.GIT_REPO)
					.withRef(Template.GIT_REF)
				.endGit()
				.withNewSourceSecret()
					.withName(Template.OPENSHIFT_SECRET)
				.endSourceSecret()
				.withContextDir(Template.GIT_SUBFOLDER)
			.endSource()
			.withNewStrategy()
				.withType("Source")
				.withNewSourceStrategy()
                	.withNewFrom()
                		.withKind("ImageStreamTag")
                		.withNamespace("openshift")
                		.withName("fis-java-openshift:1.0")
                	.endFrom()
					.withIncremental(true)
					.withForcePull(true)
					.addNewEnv()
						.withName("BUILD_LOGLEVEL")
						.withValue("5")
					.endEnv()
					.addNewEnv()
						.withName("ARTIFACT_DIR")
						.withValue("")
					.endEnv()
					.addNewEnv()
						.withName("MAVEN_ARGS")
						.withValue("package -DskipTests -e")
					.endEnv()
					.addNewEnv()
						.withName("MAVEN_ARGS_APPEND")
						.withValue("")
					.endEnv()
				.endSourceStrategy()
			.endStrategy()
			.withNewOutput()
				.withNewTo()
					.withKind("ImageStreamTag")
					.withName(Template.ARTIFACT_NAME + ":latest")
				.endTo()
			.endOutput()
			.withNewResources()
			.endResources()
		.endSpec()
		.withNewStatus()
			.withLastVersion(0)
		.endStatus()
		.endBuildConfigItem();
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
				.withNewResources()
				.endResources()
			.endStrategy()
			.addNewTrigger()
				.withType("ConfigChange")
			.endTrigger()
			.addNewTrigger()
				.withType("ImageChange")
				.withNewImageChangeParams()
					.withAutomatic(true)
					.addToContainerNames(Template.ARTIFACT_NAME)
					.withNewFrom()
						.withKind("ImageStreamTag")
						.withName(Template.ARTIFACT_NAME + ":latest")
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
						.withImage("library/" + Template.ARTIFACT_NAME + ":latest")
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
		this.addImageStream();
		this.addBuildConfig();
		this.addDeploymentConfig();
    	super.builder = super.templateItems.endTemplateItem();
    	return super.builder.build();
    }
}
