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
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import com.inmarsat.profitcentre.ServiceOrder;
import com.inmarsat.profitcentre.ServiceOrderUtil;

public class TestServiceOrderFlow extends JbpmJUnitBaseTestCase {
	public static final String EXTERNAL_URL = "http://profitcentre-harishankar-gnanavel.89d9.dev-inmarsat.openshiftapps.com/profitcenter/InvokeData/updateProfitCenterForESAS1";
	public static final String RULES_NODE = "Profit Centre Determination For I4 Services";
	public static final String MANUAL_VERIFICATION_NODE = "Manual Verification";
	public static final String MANUAL_SEGMENTATION_NODE = "manual Segmenetation";
	public static final String WORKITEM_NODE = "RestServiceOrder";
	public static final String SERVICEORDER_PROCESS_ID = "ProfitCentre.serviceOrder";
	public static final String KIE_USER = "kieserver";
	public static final String REST_URL = "restUrl";
	public static final String SERVICE_ORDER = "serviceOrder";
	public static final String SERVICE_ORDER_UTIL = "serviceOrderUtil";
	public static final String MILITARY_GOVERNMENT = "Military Government";
	public static final String GLOBAL_GOVERNMENT = "Global/US Govt.";
	public static final String AIR_TRANSPORT ="Air Transport";
	
	
	
	Map<String, Object> param = new HashMap<>();
	private Map<String, String> salesRegCdBuMap = new HashMap<>();
	private Map<String, String> industryCodeBUMap = new HashMap<>();
	private Map<String, String> ratePlanBUMap = new HashMap<>();
	private Map<String, String> customerBUMap = new HashMap<>();
	private Map<String, String> profitCentreMap = new HashMap<>();
	TestWorkItemHandler testHandler = new TestWorkItemHandler();
	ServiceOrder order;
	ServiceOrderUtil util;

	public TestServiceOrderFlow() {
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
	protected Map<String, List<String>> getTestUserGroupsAssignments() {
		Map<String, List<String>> assign = new HashMap<>();
		List<String> user1Groups = new ArrayList<>();
		user1Groups.add("kie-server");
		assign.put(KIE_USER, user1Groups);
		return assign;
	}
	public RuntimeEngine getRuntimeEngineESAS() {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("serviceOrder.bpmn2").getFile());
		File file1 = new File(classLoader.getResource("DetermineProfitCentre.drl").getFile());
		Map<String, ResourceType> resources = new HashMap<>();
		resources.put(file.getName(), ResourceType.BPMN2);
		resources.put(file1.getName(), ResourceType.DRL);
		createRuntimeManager(Strategy.PROCESS_INSTANCE, resources);
		return  getRuntimeEngine(ProcessInstanceIdContext.get());
	}
	@Test
	public void testProcess_ServiceOrder_Rule1() {
		RuntimeEngine engine = getRuntimeEngineESAS();
		order = new ServiceOrder();
		util = new ServiceOrderUtil();
		salesRegCdBuMap.put("ENTENGIN", "Enterprise Energy"); // Rule 1
		profitCentreMap.put("Enterprise Energy,1020", "2344"); // Rule 1 1020
		order.setDpId("1020");
		order.setSalesRegionCode("ENTENGIN");
		util.setProftCtMap(profitCentreMap);
		util.setSalesRegCdBuMap(salesRegCdBuMap);
		KieSession ksession = engine.getKieSession();
		ksession.getWorkItemManager().registerWorkItemHandler(WORKITEM_NODE, testHandler);
		param.put(REST_URL, EXTERNAL_URL);
		param.put(SERVICE_ORDER, order);
		param.put(SERVICE_ORDER_UTIL, util);
		ProcessInstance processInstance = ksession.startProcess(SERVICEORDER_PROCESS_ID, param);
		ksession.fireAllRules();
		WorkflowProcessInstance wpi = (WorkflowProcessInstance) processInstance;
		order = (ServiceOrder) wpi.getVariable(SERVICE_ORDER);
		assertEquals("2344", order.getProfitCenter());
		assertNodeTriggered(processInstance.getId(), RULES_NODE);
		assertNodeTriggered(processInstance.getId(), WORKITEM_NODE);
		WorkItem workItem = testHandler.getWorkItem();
		assertNotNull(workItem);
		assertEquals(WORKITEM_NODE, workItem.getName());
		ServiceOrder so = (ServiceOrder) workItem.getParameters().get(SERVICE_ORDER);
		assertEquals("2344", so.getProfitCenter());
		ksession.getWorkItemManager().abortWorkItem(workItem.getId());
		assertProcessInstanceAborted(processInstance.getId(), ksession);
		assertProcessInstanceCompleted(processInstance.getId());
	}
	@Test
	public void testProcess_ServiceOrder_Rule1_3033() {
		RuntimeEngine engine = getRuntimeEngineESAS();
		order = new ServiceOrder();
		util = new ServiceOrderUtil();
		profitCentreMap.put("Maritime,3033", "4005"); // Rule 1 3033
		order.setDpId("3033");
		order.setRatePlanChangeInd(false);
		order.setIndustryCode("Agents");
		util.setProftCtMap(profitCentreMap);
		KieSession ksession = engine.getKieSession();
		ksession.getWorkItemManager().registerWorkItemHandler(WORKITEM_NODE, testHandler);
		param.put(REST_URL, EXTERNAL_URL);
		param.put(SERVICE_ORDER, order);
		param.put(SERVICE_ORDER_UTIL, util);
		ProcessInstance processInstance = ksession.startProcess(SERVICEORDER_PROCESS_ID, param);
		ksession.fireAllRules();
		WorkflowProcessInstance wpi = (WorkflowProcessInstance) processInstance;
		order = (ServiceOrder) wpi.getVariable(SERVICE_ORDER);
		assertEquals("4005", order.getProfitCenter());
		assertNodeTriggered(processInstance.getId(), RULES_NODE);
		assertNodeTriggered(processInstance.getId(), WORKITEM_NODE);
		WorkItem workItem = testHandler.getWorkItem();
		assertNotNull(workItem);
		assertEquals(WORKITEM_NODE, workItem.getName());
		ServiceOrder so = (ServiceOrder) workItem.getParameters().get(SERVICE_ORDER);
		assertEquals("4005", so.getProfitCenter());
		ksession.getWorkItemManager().abortWorkItem(workItem.getId());
		assertProcessInstanceAborted(processInstance.getId(), ksession);
		assertProcessInstanceCompleted(processInstance.getId());
	}
	@Test
	public void testProcess_ServiceOrder_Rule3() {
		RuntimeEngine engine = getRuntimeEngineESAS();
		order = new ServiceOrder();
		util = new ServiceOrderUtil();
		profitCentreMap.put("Global Government,1234", "2344"); // Rule 3
		ratePlanBUMap.put("2513,Military Government", "Global Government"); // Rule
																			// 3
		order.setDpId("1234");
		order.setRatePlanId("2513");
		order.setIndustryCode(MILITARY_GOVERNMENT);
		util.setProftCtMap(profitCentreMap);
		util.setRatePlanBuMap(ratePlanBUMap);
		KieSession ksession = engine.getKieSession();
		ksession.getWorkItemManager().registerWorkItemHandler(WORKITEM_NODE, testHandler);
		param.put(REST_URL, EXTERNAL_URL);
		param.put(SERVICE_ORDER, order);
		param.put(SERVICE_ORDER_UTIL, util);
		ProcessInstance processInstance = ksession.startProcess(SERVICEORDER_PROCESS_ID, param);
		ksession.fireAllRules();
		WorkflowProcessInstance wpi = (WorkflowProcessInstance) processInstance;
		order = (ServiceOrder) wpi.getVariable(SERVICE_ORDER);
		assertEquals("2344", order.getProfitCenter());
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
		ServiceOrder so = (ServiceOrder) workItem.getParameters().get(SERVICE_ORDER);
		assertEquals("2344", so.getProfitCenter());
		ksession.getWorkItemManager().abortWorkItem(workItem.getId());
		assertProcessInstanceAborted(processInstance.getId(), ksession);
		assertProcessInstanceCompleted(processInstance.getId());
	}
	@Test
	public void testProcess_ServiceOrder_Rule4() {
		RuntimeEngine engine = getRuntimeEngineESAS();
		order = new ServiceOrder();
		util = new ServiceOrderUtil();
		customerBUMap.put("1234,Military Government", "Maritime"); // Rule
																	// 4
		profitCentreMap.put("Maritime,1234", "4001"); // Rule 4
		order.setDpId("1234");
		order.setCustomerName("USSecurenet LLC");
		order.setIndustryCode(MILITARY_GOVERNMENT);
		util.setCustomerBuMap(customerBUMap);
		util.setProftCtMap(profitCentreMap);
		KieSession ksession = engine.getKieSession();
		ksession.getWorkItemManager().registerWorkItemHandler(WORKITEM_NODE, testHandler);
		param.put(REST_URL, EXTERNAL_URL);
		param.put(SERVICE_ORDER, order);
		param.put(SERVICE_ORDER_UTIL, util);
		ProcessInstance processInstance = ksession.startProcess(SERVICEORDER_PROCESS_ID, param);
		ksession.fireAllRules();
		WorkflowProcessInstance wpi = (WorkflowProcessInstance) processInstance;
		order = (ServiceOrder) wpi.getVariable(SERVICE_ORDER);
		assertEquals("4001", order.getProfitCenter());
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
		ServiceOrder so = (ServiceOrder) workItem.getParameters().get(SERVICE_ORDER);
		assertEquals("4001", so.getProfitCenter());
		ksession.getWorkItemManager().abortWorkItem(workItem.getId());
		assertProcessInstanceAborted(processInstance.getId(), ksession);
		assertProcessInstanceCompleted(processInstance.getId());
	}
	@Test
	public void testProcess_ServiceOrder_Rule5_USA() {
		RuntimeEngine engine = getRuntimeEngineESAS();
		order = new ServiceOrder();
		util = new ServiceOrderUtil();
		profitCentreMap.put("US Government,4562", "4002");
		industryCodeBUMap.put(MILITARY_GOVERNMENT, GLOBAL_GOVERNMENT);
		order.setCountryName("US");
		order.setProductCode("FLET");
		order.setDpId("4562");
		order.setIndustryCode(MILITARY_GOVERNMENT);
		util.setProftCtMap(profitCentreMap);
		util.setIndCdBuMap(industryCodeBUMap);
		KieSession ksession = engine.getKieSession();
		ksession.getWorkItemManager().registerWorkItemHandler(WORKITEM_NODE, testHandler);
		param.put(REST_URL, EXTERNAL_URL);
		param.put(SERVICE_ORDER, order);
		param.put(SERVICE_ORDER_UTIL, util);
		ProcessInstance processInstance = ksession.startProcess(SERVICEORDER_PROCESS_ID, param);
		ksession.fireAllRules();
		WorkflowProcessInstance wpi = (WorkflowProcessInstance) processInstance;
		order = (ServiceOrder) wpi.getVariable(SERVICE_ORDER);
		assertEquals("4002", order.getProfitCenter());
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
		ServiceOrder so = (ServiceOrder) workItem.getParameters().get(SERVICE_ORDER);
		assertEquals("4002", so.getProfitCenter());
		ksession.getWorkItemManager().abortWorkItem(workItem.getId());
		assertProcessInstanceAborted(processInstance.getId(), ksession);
		assertProcessInstanceCompleted(processInstance.getId());
	}
	@Test
	public void testProcess_ServiceOrder_Rule5_Non_USA() {
		RuntimeEngine engine = getRuntimeEngineESAS();
		order = new ServiceOrder();
		util = new ServiceOrderUtil();
		profitCentreMap.put("Global Government,4562", "4001");
		industryCodeBUMap.put(MILITARY_GOVERNMENT, GLOBAL_GOVERNMENT);
		order.setCountryName("UK");
		order.setProductCode("FLET");
		order.setDpId("4562");
		order.setIndustryCode(MILITARY_GOVERNMENT);
		util.setProftCtMap(profitCentreMap);
		util.setIndCdBuMap(industryCodeBUMap);
		KieSession ksession = engine.getKieSession();
		ksession.getWorkItemManager().registerWorkItemHandler(WORKITEM_NODE, testHandler);
		param.put(REST_URL, EXTERNAL_URL);
		param.put(SERVICE_ORDER, order);
		param.put(SERVICE_ORDER_UTIL, util);
		ProcessInstance processInstance = ksession.startProcess(SERVICEORDER_PROCESS_ID, param);
		ksession.fireAllRules();
		WorkflowProcessInstance wpi = (WorkflowProcessInstance) processInstance;
		order = (ServiceOrder) wpi.getVariable(SERVICE_ORDER);
		assertEquals("4001", order.getProfitCenter());
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
		ServiceOrder so = (ServiceOrder) workItem.getParameters().get(SERVICE_ORDER);
		assertEquals("4001", so.getProfitCenter());
		ksession.getWorkItemManager().abortWorkItem(workItem.getId());
		assertProcessInstanceAborted(processInstance.getId(), ksession);
		assertProcessInstanceCompleted(processInstance.getId());
	}
	@Test
	public void testProcess_ServiceOrder_Rule6_USA() {
		RuntimeEngine engine = getRuntimeEngineESAS();
		order = new ServiceOrder();
		util = new ServiceOrderUtil();
		profitCentreMap.put("US Government,4562", "2344");
		industryCodeBUMap.put(MILITARY_GOVERNMENT, GLOBAL_GOVERNMENT);
		order.setCountryName("US");
		order.setProductCode("SWFT");
		order.setDpId("4562");
		order.setIndustryCode(MILITARY_GOVERNMENT);
		util.setProftCtMap(profitCentreMap);
		util.setIndCdBuMap(industryCodeBUMap);
		KieSession ksession = engine.getKieSession();
		ksession.getWorkItemManager().registerWorkItemHandler(WORKITEM_NODE, testHandler);
		param.put(REST_URL, EXTERNAL_URL);
		param.put(SERVICE_ORDER, order);
		param.put(SERVICE_ORDER_UTIL, util);
		ProcessInstance processInstance = ksession.startProcess(SERVICEORDER_PROCESS_ID, param);
		ksession.fireAllRules();
		WorkflowProcessInstance wpi = (WorkflowProcessInstance) processInstance;
		order = (ServiceOrder) wpi.getVariable(SERVICE_ORDER);
		assertEquals("2344", order.getProfitCenter());
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
		ServiceOrder so = (ServiceOrder) workItem.getParameters().get(SERVICE_ORDER);
		assertEquals("2344", so.getProfitCenter());
		ksession.getWorkItemManager().abortWorkItem(workItem.getId());
		assertProcessInstanceAborted(processInstance.getId(), ksession);
		assertProcessInstanceCompleted(processInstance.getId());
	}
	@Test
	public void testProcess_ServiceOrder_Rule6_Non_USA() {
		RuntimeEngine engine = getRuntimeEngineESAS();
		order = new ServiceOrder();
		util = new ServiceOrderUtil();
		profitCentreMap.put("Global Government,4562", "4001");
		industryCodeBUMap.put(MILITARY_GOVERNMENT, GLOBAL_GOVERNMENT);
		order.setCountryName("UK");
		order.setProductCode("SWFT");
		order.setDpId("4562");
		order.setIndustryCode(MILITARY_GOVERNMENT);
		util.setProftCtMap(profitCentreMap);
		util.setIndCdBuMap(industryCodeBUMap);
		KieSession ksession = engine.getKieSession();
		ksession.getWorkItemManager().registerWorkItemHandler(WORKITEM_NODE, testHandler);
		param.put(REST_URL, EXTERNAL_URL);
		param.put(SERVICE_ORDER, order);
		param.put(SERVICE_ORDER_UTIL, util);
		ProcessInstance processInstance = ksession.startProcess(SERVICEORDER_PROCESS_ID, param);
		ksession.fireAllRules();
		WorkflowProcessInstance wpi = (WorkflowProcessInstance) processInstance;
		order = (ServiceOrder) wpi.getVariable(SERVICE_ORDER);
		assertEquals("4001", order.getProfitCenter());
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
		ServiceOrder so = (ServiceOrder) workItem.getParameters().get(SERVICE_ORDER);
		assertEquals("4001", so.getProfitCenter());
		ksession.getWorkItemManager().abortWorkItem(workItem.getId());
		assertProcessInstanceAborted(processInstance.getId(), ksession);
		assertProcessInstanceCompleted(processInstance.getId());
	}
	@Test
	public void testProcess_ServiceOrder_Rule7() {
		RuntimeEngine engine = getRuntimeEngineESAS();
		order = new ServiceOrder();
		util = new ServiceOrderUtil();
		profitCentreMap.put("Aviation,2222", "5001");
		industryCodeBUMap.put(AIR_TRANSPORT, "Aviation");
		order.setProductCode("SWFT");
		order.setDpId("2222");
		order.setIndustryCode(AIR_TRANSPORT);
		util.setProftCtMap(profitCentreMap);
		util.setIndCdBuMap(industryCodeBUMap);
		KieSession ksession = engine.getKieSession();
		ksession.getWorkItemManager().registerWorkItemHandler(WORKITEM_NODE, testHandler);
		param.put(REST_URL, EXTERNAL_URL);
		param.put(SERVICE_ORDER, order);
		param.put(SERVICE_ORDER_UTIL, util);
		ProcessInstance processInstance = ksession.startProcess(SERVICEORDER_PROCESS_ID, param);
		ksession.fireAllRules();
		WorkflowProcessInstance wpi = (WorkflowProcessInstance) processInstance;
		order = (ServiceOrder) wpi.getVariable(SERVICE_ORDER);
		assertEquals("5001", order.getProfitCenter());
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
		ServiceOrder so = (ServiceOrder) workItem.getParameters().get(SERVICE_ORDER);
		assertEquals("5001", so.getProfitCenter());
		ksession.getWorkItemManager().abortWorkItem(workItem.getId());
		assertProcessInstanceAborted(processInstance.getId(), ksession);
		assertProcessInstanceCompleted(processInstance.getId());
	}
	@Test
	public void testProcess_ServiceOrder_Rule8() {
		RuntimeEngine engine = getRuntimeEngineESAS();
		order = new ServiceOrder();
		util = new ServiceOrderUtil();
		profitCentreMap.put("Aviation,2222", "5001");
		industryCodeBUMap.put(AIR_TRANSPORT, "Aviation");
		ratePlanBUMap.put("2513,Military Government", "Global Government");
		order.setDpId("2222");
		order.setIndustryCode(AIR_TRANSPORT);
		util.setProftCtMap(profitCentreMap);
		util.setIndCdBuMap(industryCodeBUMap);
		util.setRatePlanBuMap(ratePlanBUMap);
		KieSession ksession = engine.getKieSession();
		ksession.getWorkItemManager().registerWorkItemHandler(WORKITEM_NODE, testHandler);
		param.put(REST_URL, EXTERNAL_URL);
		param.put(SERVICE_ORDER, order);
		param.put(SERVICE_ORDER_UTIL, util);
		ProcessInstance processInstance = ksession.startProcess(SERVICEORDER_PROCESS_ID, param);
		ksession.fireAllRules();
		WorkflowProcessInstance wpi = (WorkflowProcessInstance) processInstance;
		order = (ServiceOrder) wpi.getVariable(SERVICE_ORDER);
		assertEquals("5001", order.getProfitCenter());
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
		ServiceOrder so = (ServiceOrder) workItem.getParameters().get(SERVICE_ORDER);
		assertEquals("5001", so.getProfitCenter());
		ksession.getWorkItemManager().abortWorkItem(workItem.getId());
		assertProcessInstanceAborted(processInstance.getId(), ksession);
		assertProcessInstanceCompleted(processInstance.getId());
	}
}
