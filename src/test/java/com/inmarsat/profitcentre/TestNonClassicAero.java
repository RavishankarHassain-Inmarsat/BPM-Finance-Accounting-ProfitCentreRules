package com.inmarsat.profitcentre;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Task;
import com.inmarsat.profitcentre.ESASRequest;
import com.inmarsat.profitcentre.ESASUtil;

public class TestNonClassicAero extends JbpmJUnitBaseTestCase {
	ESASRequest esasRequest;
	ESASUtil esasUtil;
	Map<String, String> compIdBUMap = new HashMap<>();
	Map<String, String> esasProdIndBuMap = new HashMap<>();
	Map<String, String> esasCntryOfRegBuMap = new HashMap<>();
	Map<String, String> esasProdBuCdBuMap = new HashMap<>();
	Map<String, String> esasProdCdMap = new HashMap<>();
	Map<String, String> proftCtMap = new HashMap<>();
	public static final String EXTERNAL_URL = "http://profitcentre-harishankar-gnanavel.89d9.dev-inmarsat.openshiftapps.com/profitcenter/InvokeData/updateProfitCenterForESAS1";
	public static final String RULES_NODE = "Profit Centre determination for I3 services";
	public static final String MANUAL_VERIFICATION_NODE = "Manual Verification";
	public static final String MANUAL_SEGMENTATION_NODE = "Manual Segmentation";
	public static final String WORKITEM_NODE = "RestESAS";
	public static final String ESAS_PROCESS_ID = "serviceProvisioning.TerminalrequestProcessing";
	public static final String US_GOVERNMENT =  "US Government";
	public static final String ESAS_REQUEST = "esasRequest";
	public static final String ESAS_UTIL = "esasUtil";
	public static final String REST_URL = "restUrl";
	public static final String KIE_USER = "kieserver";
	public static final String GLOBAL_GOVERNMENT = "Global Government";
	public static final String AVIATION = "Aviation";
	public static final String OTHER =  "Other";
	
	TestWorkItemHandler testHandler=new TestWorkItemHandler();

	public TestNonClassicAero() {
		// Set up a data source and enable persistence:
		super(true, true);
		userGroupCallback = new UserGroupCallback() {
			public List<String> getGroupsForUser(String userId, List<String> groupIds,
					List<String> allExistingGroupIds) {
				List<String> groups = new ArrayList<>();
				groups.add("kie-server");
				return groups;
			}
			public boolean existsUser(String userId) {
			
				return true;
			}
			public boolean existsGroup(String groupId) {
			
				return true;
			}
		};
	}

	public RuntimeEngine getRuntimeEngineESAS() {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("TerminalrequestProcessing.bpmn2").getFile());
		File file1 = new File(classLoader.getResource("DeriveProfitCentre.drl").getFile());
		Map<String, ResourceType> resources = new HashMap<>();
		resources.put(file.getName(), ResourceType.BPMN2);
		resources.put(file1.getName(), ResourceType.DRL);
		createRuntimeManager(Strategy.PROCESS_INSTANCE, resources);
		return getRuntimeEngine();
	}

	@Test
	public void testProcess_ESAS_Rule1_Non_ClassicAero() {
		RuntimeEngine engine = getRuntimeEngineESAS();
		esasRequest = new ESASRequest();
		esasRequest.setDpId(null);
		esasRequest.setCompanyId("127480");
		esasRequest.setTerminalId("14568");
		esasRequest.setProfitCentre(null);
		esasRequest.setSatisfiedRuleName(null);
		esasUtil = new ESASUtil();
		compIdBUMap.put("127480", US_GOVERNMENT);
		esasUtil.setCompIdBUMap(compIdBUMap);
		Map<String, Object> param = new HashMap<>();
		param.put(ESAS_REQUEST, esasRequest);
		param.put(ESAS_UTIL, esasUtil);
		KieSession ksession = engine.getKieSession();
		ksession.getWorkItemManager().registerWorkItemHandler(WORKITEM_NODE, testHandler);
		param.put(REST_URL, EXTERNAL_URL);
		ProcessInstance processInstance = ksession.startProcess(ESAS_PROCESS_ID, param);
		ksession.fireAllRules();
		WorkflowProcessInstance wpi = (WorkflowProcessInstance) processInstance;
		esasRequest = (ESASRequest) wpi.getVariable(ESAS_REQUEST);
		assertEquals(US_GOVERNMENT, esasRequest.getBusinessUnitPC());
		assertNodeTriggered(processInstance.getId(), RULES_NODE);
		assertNodeTriggered(processInstance.getId(), WORKITEM_NODE);
		assertNodeTriggered(processInstance.getId(), MANUAL_VERIFICATION_NODE);
		assertNodeActive(processInstance.getId(), ksession, MANUAL_VERIFICATION_NODE);
		List<Long> taskId = engine.getTaskService().getTasksByProcessInstanceId(processInstance.getId());
		Task task = engine.getTaskService().getTaskById(taskId.get(0));
		engine.getTaskService().start(task.getId(), KIE_USER);
		param = engine.getTaskService().getTaskContent(task.getId());
		engine.getTaskService().complete(task.getId(), KIE_USER, param);
		WorkItem workItem = testHandler.getWorkItem();
		assertNotNull(workItem);
		assertEquals(WORKITEM_NODE, workItem.getName());
		ESASRequest so = (ESASRequest) workItem.getParameters().get(ESAS_REQUEST);
		assertEquals(US_GOVERNMENT, so.getBusinessUnitPC());
		ksession.getWorkItemManager().abortWorkItem(workItem.getId());
		assertProcessInstanceCompleted(processInstance.getId());
	}
	
	@Test
	public void testProcess_ESAS_Rule2_CountryCode_USA() {
		RuntimeEngine engine = getRuntimeEngineESAS();
		esasRequest = new ESASRequest();
		esasUtil = new ESASUtil();
		esasRequest.setDpId(null);
		esasRequest.setCatCode("1");
		esasRequest.setCountryOfRegistration("USA");
		esasRequest.setBusinessUnit("M");
		esasCntryOfRegBuMap.put("USA,1,M", US_GOVERNMENT);
		esasUtil.setEsasCntryOfRegBu(esasCntryOfRegBuMap);
		Map<String, Object> param = new HashMap<>();
		param.put(ESAS_REQUEST, esasRequest);
		param.put(ESAS_UTIL, esasUtil);
		KieSession ksession = engine.getKieSession();
		ksession.getWorkItemManager().registerWorkItemHandler(WORKITEM_NODE, testHandler);
		param.put(REST_URL, EXTERNAL_URL);
		ProcessInstance processInstance = ksession.startProcess(ESAS_PROCESS_ID, param);
		ksession.fireAllRules();
		WorkflowProcessInstance wpi = (WorkflowProcessInstance) processInstance;
		esasRequest = (ESASRequest) wpi.getVariable(ESAS_REQUEST);
		assertEquals(US_GOVERNMENT, esasRequest.getBusinessUnitPC());
		assertNodeTriggered(processInstance.getId(), RULES_NODE);
		assertNodeTriggered(processInstance.getId(), WORKITEM_NODE);
		assertNodeTriggered(processInstance.getId(), MANUAL_VERIFICATION_NODE);
		assertNodeActive(processInstance.getId(), ksession, MANUAL_VERIFICATION_NODE);
		List<Long> taskId = engine.getTaskService().getTasksByProcessInstanceId(processInstance.getId());
		Task task = engine.getTaskService().getTaskById(taskId.get(0));
		engine.getTaskService().start(task.getId(), KIE_USER);
		param = engine.getTaskService().getTaskContent(task.getId());
		engine.getTaskService().complete(task.getId(), KIE_USER, param);
		WorkItem workItem = testHandler.getWorkItem();
		assertNotNull(workItem);
		assertEquals(WORKITEM_NODE, workItem.getName());
		ESASRequest so = (ESASRequest) workItem.getParameters().get(ESAS_REQUEST);
		assertEquals(US_GOVERNMENT, so.getBusinessUnitPC());
		ksession.getWorkItemManager().abortWorkItem(workItem.getId());
		assertProcessInstanceCompleted(processInstance.getId());
	}

	@Test
	public void testProcess_ESAS_Rule2_CountryCode_Non_USA() {
		RuntimeEngine engine = getRuntimeEngineESAS();
		esasRequest = new ESASRequest();
		esasUtil = new ESASUtil();
		esasRequest.setDpId(null);
		esasRequest.setCatCode("1");
		esasRequest.setCountryOfRegistration("GBR");
		esasRequest.setBusinessUnit("M");
		esasCntryOfRegBuMap.put("Non-USA,1,M", GLOBAL_GOVERNMENT);
		esasUtil.setEsasCntryOfRegBu(esasCntryOfRegBuMap);
		Map<String, Object> param = new HashMap<>();
		param.put(ESAS_REQUEST, esasRequest);
		param.put(ESAS_UTIL, esasUtil);
		KieSession ksession = engine.getKieSession();
		ksession.getWorkItemManager().registerWorkItemHandler(WORKITEM_NODE, testHandler);
		param.put(REST_URL, EXTERNAL_URL);
		ProcessInstance processInstance = ksession.startProcess(ESAS_PROCESS_ID, param);
		ksession.fireAllRules();
		WorkflowProcessInstance wpi = (WorkflowProcessInstance) processInstance;
		esasRequest = (ESASRequest) wpi.getVariable(ESAS_REQUEST);
		assertEquals(GLOBAL_GOVERNMENT, esasRequest.getBusinessUnitPC());
		assertNodeTriggered(processInstance.getId(), RULES_NODE);
		assertNodeTriggered(processInstance.getId(), WORKITEM_NODE);
		assertNodeTriggered(processInstance.getId(), MANUAL_VERIFICATION_NODE);
		assertNodeActive(processInstance.getId(), ksession, MANUAL_VERIFICATION_NODE);
		List<Long> taskId = engine.getTaskService().getTasksByProcessInstanceId(processInstance.getId());
		Task task = engine.getTaskService().getTaskById(taskId.get(0));
	
		engine.getTaskService().start(task.getId(), KIE_USER);
		param = engine.getTaskService().getTaskContent(task.getId());
		engine.getTaskService().complete(task.getId(), KIE_USER, param);
		WorkItem workItem = testHandler.getWorkItem();
		assertNotNull(workItem);
		assertEquals(WORKITEM_NODE, workItem.getName());
		ESASRequest so = (ESASRequest) workItem.getParameters().get(ESAS_REQUEST);
		assertEquals(GLOBAL_GOVERNMENT, so.getBusinessUnitPC());
		ksession.getWorkItemManager().abortWorkItem(workItem.getId());
		assertProcessInstanceCompleted(processInstance.getId());
	}

	@Test
	public void testProcess_ESAS_Rule3() {
		RuntimeEngine engine = getRuntimeEngineESAS();
		esasRequest = new ESASRequest();
		esasUtil = new ESASUtil();
		esasRequest.setDpId(null);
		esasRequest.setStandardId("R");
		esasRequest.setAntennaId("U");
		esasRequest.setMesCategoryCode("Z");
		esasRequest.setAeroServiceType("L");
		esasRequest.setCatCode("53");
		esasRequest.setProductCode("RH");
		esasProdCdMap.put("R,U,Z,L", "RH");
		esasProdIndBuMap.put("RH,53", AVIATION);
		esasUtil.setEsasProdBuCdBu(esasProdCdMap);
		esasUtil.setEsasProdIndBu(esasProdIndBuMap);
		Map<String, Object> param = new HashMap<>();
		param.put(ESAS_REQUEST, esasRequest);
		param.put(ESAS_UTIL, esasUtil);
		KieSession ksession = engine.getKieSession();
		ksession.getWorkItemManager().registerWorkItemHandler(WORKITEM_NODE, testHandler);
		param.put(REST_URL, EXTERNAL_URL);
		ProcessInstance processInstance = ksession.startProcess(ESAS_PROCESS_ID, param);
		ksession.fireAllRules();
		WorkflowProcessInstance wpi = (WorkflowProcessInstance) processInstance;
		esasRequest = (ESASRequest) wpi.getVariable(ESAS_REQUEST);
		assertEquals(AVIATION, esasRequest.getBusinessUnitPC());
		assertNodeTriggered(processInstance.getId(), RULES_NODE);
		assertNodeTriggered(processInstance.getId(), WORKITEM_NODE);
		assertNodeTriggered(processInstance.getId(), MANUAL_VERIFICATION_NODE);
		assertNodeActive(processInstance.getId(), ksession, MANUAL_VERIFICATION_NODE);
		List<Long> taskId = engine.getTaskService().getTasksByProcessInstanceId(processInstance.getId());
		Task task = engine.getTaskService().getTaskById(taskId.get(0));
		engine.getTaskService().start(task.getId(), KIE_USER);
		param = engine.getTaskService().getTaskContent(task.getId());
		engine.getTaskService().complete(task.getId(), KIE_USER, param);
		WorkItem workItem = testHandler.getWorkItem();
		assertNotNull(workItem);
		assertEquals(WORKITEM_NODE, workItem.getName());
		ESASRequest so = (ESASRequest) workItem.getParameters().get(ESAS_REQUEST);
		assertEquals(AVIATION, so.getBusinessUnitPC());
		ksession.getWorkItemManager().abortWorkItem(workItem.getId());
	
		assertProcessInstanceCompleted(processInstance.getId());
	}

	@Test
	public void testProcess_ESAS_Rule4() {
		RuntimeEngine engine = getRuntimeEngineESAS();
		esasRequest = new ESASRequest();
		esasUtil = new ESASUtil();
		esasRequest.setDpId(null);
		esasRequest.setStandardId("R");
		esasRequest.setAntennaId("U");
		esasRequest.setMesCategoryCode("Z");
		esasRequest.setAeroServiceType("L");
		esasRequest.setBusinessUnit("U");
		esasRequest.setProductCode("RH");
		esasProdCdMap.put("RH,U", OTHER);
		esasUtil.setEsasProdBuCdBu(esasProdCdMap);
		Map<String, Object> param = new HashMap<>();
		param.put(ESAS_REQUEST, esasRequest);
		param.put(ESAS_UTIL, esasUtil);
		KieSession ksession = engine.getKieSession();
		ksession.getWorkItemManager().registerWorkItemHandler(WORKITEM_NODE, testHandler);
		param.put(REST_URL, EXTERNAL_URL);
		ProcessInstance processInstance = ksession.startProcess(ESAS_PROCESS_ID, param);
		ksession.fireAllRules();
		WorkflowProcessInstance wpi = (WorkflowProcessInstance) processInstance;
		esasRequest = (ESASRequest) wpi.getVariable(ESAS_REQUEST);
		assertEquals(OTHER, esasRequest.getBusinessUnitPC());
		assertNodeTriggered(processInstance.getId(), RULES_NODE);
		assertNodeTriggered(processInstance.getId(), WORKITEM_NODE);
		assertNodeTriggered(processInstance.getId(), MANUAL_VERIFICATION_NODE);
		assertNodeActive(processInstance.getId(), ksession, MANUAL_VERIFICATION_NODE);
		List<Long> taskId = engine.getTaskService().getTasksByProcessInstanceId(processInstance.getId());
		Task task = engine.getTaskService().getTaskById(taskId.get(0));
		engine.getTaskService().start(task.getId(), KIE_USER);
		param = engine.getTaskService().getTaskContent(task.getId());
		engine.getTaskService().complete(task.getId(), KIE_USER, param);
		WorkItem workItem = testHandler.getWorkItem();
		assertNotNull(workItem);
		assertEquals(WORKITEM_NODE, workItem.getName());
		ESASRequest so = (ESASRequest) workItem.getParameters().get(ESAS_REQUEST);
		assertEquals(OTHER, so.getBusinessUnitPC());
		ksession.getWorkItemManager().abortWorkItem(workItem.getId());
		assertProcessInstanceCompleted(processInstance.getId());
	}
}
