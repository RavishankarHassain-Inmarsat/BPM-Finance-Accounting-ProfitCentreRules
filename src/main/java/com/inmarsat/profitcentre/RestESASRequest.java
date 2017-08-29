package com.inmarsat.profitcentre;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import com.fasterxml.jackson.databind.ObjectMapper;



public class RestESASRequest implements WorkItemHandler {
	
	private static final Logger LOGGER = Logger.getLogger(RestESASRequest.class.getName());
	private KieSession ksession;

	public RestESASRequest(KieSession ksession) {
		this.ksession = ksession;
	}
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		
		LOGGER.info(
				"Session info ::" + ksession.getProcessInstance(workItem.getProcessInstanceId()).getProcessId());
		try {
			ESASRequest request = (ESASRequest) workItem.getParameters().get("esasRequest");
			String url = (String) workItem.getParameters().get("Url");
			
			
			ObjectMapper mapper = new ObjectMapper();
			String jsonInString = mapper.writeValueAsString(request);
			
			ClientRequest clientRequest = new ClientRequest(url);
			clientRequest.accept("application/json");

			
			clientRequest.body("application/json", jsonInString);

			ClientResponse<String> response = clientRequest.post(String.class);
			if (response.getStatus() != 200) {
				LOGGER.info("On Service Unavailable ::" + response.getStatus());
				ksession.signalEvent("ExceptionESAS", request);
				LOGGER.info("Signal Triggered");
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new ByteArrayInputStream(response.getEntity().getBytes())));

				String output;
				LOGGER.info("Output from Server .... \n");
				while ((output = br.readLine()) != null) {
					LOGGER.info( "Output ::"+output);
				}
		
			workItem.getParameters().put("Result", output);
			manager.completeWorkItem(workItem.getId(), workItem.getParameters());
		} catch (Exception e) {
			LOGGER.error(e);
		  
		}
	}
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
	
		manager.abortWorkItem(workItem.getId());
	}
}