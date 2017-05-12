/*
 * Copyright © 2017, Inmarsat Global Ltd.
 * This file cannot be copied and/or distributed outside Inmarsat without the express permission given by
 * Inmarsat’s Legal Affairs.  All permission requests should be requested via LegalCompliance@inmarsat.com.
 */

package com.inmarsat.esb;

import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.generator.annotation.KubernetesProvider;

public class KubeFileGenerator {

	@KubernetesProvider("build-deploy-sandbox.yml")
	public KubernetesList builderForS2iSandboxDevelopment() {		
		SandboxTemplate templateGenerator = new SandboxTemplate();
		return templateGenerator.build();
	}
	
	@KubernetesProvider("build-deploy.yml")
	public KubernetesList builderForBuildDeploy() {		
		BuildDeployTemplate templateGenerator = new BuildDeployTemplate();
		return templateGenerator.build();
	}
	
	@KubernetesProvider("deploy.yml")
	public KubernetesList builderForDeploy() {		
		DeployTemplate templateGenerator = new DeployTemplate();
		return templateGenerator.build();
	}
	
}
