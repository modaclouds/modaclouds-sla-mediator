package eu.modaclouds.sla.mediator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polimi.modaclouds.qos_models.schema.Action;
import it.polimi.modaclouds.qos_models.schema.Actions;
import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;
import it.polimi.modaclouds.qos_models.schema.Parameter;

public class ModelUtils {
    public static Logger logger = LoggerFactory.getLogger(ModelUtils.class);
    
    public static MonitoringRule NOT_FOUND_RULE = new MonitoringRule();
    static {
        NOT_FOUND_RULE.setActions(new Actions());
    }
    
    public static String getOutputMetric(MonitoringRule rule) {
        
        for (Action action : rule.getActions().getActions()) {
            if ("OutputMetric".equalsIgnoreCase(action.getName())) {
                for (Parameter param : action.getParameters()) {
                    if ("metric".equals(param.getName())) {
                        return param.getValue();
                    }
                }
            }
        }
        return "";
    }
    
    public static MonitoringRule getRelatedRule(Constraint constraint, MonitoringRules rules) {
        MonitoringRule result = NOT_FOUND_RULE;
        
        String constraintId = constraint.getId();
        if (constraintId == null) {
            logger.warn("Not valid constraint: id is null");
        } 
        else {
            for (MonitoringRule rule : rules.getMonitoringRules()) {
                if (constraintId.equals(rule.getRelatedQosConstraintId())) {
                    result = rule;
                    break;
                }
            }
        }
        return result;
    }
    
    
}
