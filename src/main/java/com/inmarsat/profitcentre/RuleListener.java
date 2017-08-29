package com.inmarsat.profitcentre;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.kie.api.definition.rule.Rule;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.AgendaGroupPoppedEvent;
import org.kie.api.event.rule.AgendaGroupPushedEvent;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.MatchCancelledEvent;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.kie.api.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.rule.Match;




public class RuleListener implements RuleRuntimeEventListener,AgendaEventListener {
	
	private static final Logger LOGGER = Logger.getLogger(RuleListener.class.getName());
	private List<Match> matchList = new ArrayList<>();
	public void objectDeleted(ObjectDeletedEvent arg0) {
		
		LOGGER.info("objectDeleted:");
	}

	public void objectInserted(ObjectInsertedEvent insertedObject) {
		
		
		LOGGER.info("Inserted Object :"+insertedObject.getObject());
	}

	public void objectUpdated(ObjectUpdatedEvent updatedObject) {
		
		LOGGER.info("Updated Object :"+updatedObject.getObject().toString());
	}

	public void afterMatchFired(AfterMatchFiredEvent event) {
		
		 Rule rule = event.getMatch().getRule();
		 
	        String ruleName = rule.getName();
	        Map<String, Object> ruleMetaDataMap = rule.getMetaData();
            LOGGER.info("Rule Map size :"+ruleMetaDataMap.size());
	        matchList.add(event.getMatch());
	        StringBuilder sb = new StringBuilder("Rule fired: " + ruleName);

	        if (ruleMetaDataMap.size() > 0) {
	            sb.append("\n  With [" + ruleMetaDataMap.size() + "] meta-data:");
	            for (Entry<String, Object> key : ruleMetaDataMap.entrySet()) {
	                sb.append("\n    key=" + key + ", value="
	                        + ruleMetaDataMap.get(key));
	            }
	        }

	       LOGGER.info(sb.toString());
		
	}

	public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent arg0) {
		
	LOGGER.info("Rule flow Group activated ::" +arg0.getRuleFlowGroup().getName());
	}

	public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent arg0) {
		
		LOGGER.info("Rule flow Group Deactivated ::" +arg0.getRuleFlowGroup().getName());
	}

	public void agendaGroupPopped(AgendaGroupPoppedEvent arg0) {
		
		LOGGER.info("Rule flow Group agendaGroupPopped ::" +arg0.getAgendaGroup().getName());
	}

	public void agendaGroupPushed(AgendaGroupPushedEvent arg0) {
		LOGGER.info("Rule flow Group agendaGroupPushed ::" +arg0.getAgendaGroup().getName());
		
	}

	public void beforeMatchFired(BeforeMatchFiredEvent arg0) {
		
		LOGGER.info("beforeMatchFired ::" +arg0.getMatch().getRule());
	}

	public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent arg0) {
		LOGGER.info("beforeRuleFlowGroupActivated ::" +arg0.getRuleFlowGroup().getName());
		
	}

	public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent arg0) {
		LOGGER.info("beforeRuleFlowGroupDeactivated ::" +arg0.getRuleFlowGroup().getName());
		
	}

	public void matchCancelled(MatchCancelledEvent arg0) {
		
		LOGGER.info("matchCancelled ::" +arg0.getCause());
		
	}

	public void matchCreated(MatchCreatedEvent event) {
		
		LOGGER.info("Match Created!!");
		 Rule rule = event.getMatch().getRule();
		 
	        String ruleName = rule.getName();
	        Map<String, Object> ruleMetaDataMap = rule.getMetaData();

	        matchList.add(event.getMatch());
	        StringBuilder sb = new StringBuilder("Rule fired: " + ruleName);

	        if (ruleMetaDataMap.size() > 0) {
	            sb.append("\n  With [" + ruleMetaDataMap.size() + "] meta-data:");
	            for (Entry<String, Object> key : ruleMetaDataMap.entrySet()) {
	                sb.append("\n    key=" + key + ", value="
	                        + ruleMetaDataMap.get(key));
	            }
	        }

	        LOGGER.info(sb.toString());
	}
}
