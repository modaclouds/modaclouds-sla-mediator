package eu.modaclouds.sla.mediator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polimi.modaclouds.qos_models.schema.Action;
import it.polimi.modaclouds.qos_models.schema.Actions;
import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;
import it.polimi.modaclouds.qos_models.schema.Parameter;

/**
 * Functions and vocabulary related to QosConstraints and MonitoringRules (qos-models)
 */
public class QosModels {
    public static final String OUTPUT_METRIC_ACTION = "OutputMetric";
    public static final String METRIC_PARAM_NAME = "metric";
    
    public static final String BUSINESS_ACTION = "Business";

    public static Logger logger = LoggerFactory.getLogger(QosModels.class);
    
    public static MonitoringRule NOT_FOUND_RULE = new MonitoringRule();
    static {
        NOT_FOUND_RULE.setActions(new Actions());
    }
    
    /**
     * Get the first OutputMetric name of a rule (i.e., the name of stream to subscribe to) if defined; 
     * empty string otherwise.
     */
    public static String getOutputMetric(MonitoringRule rule) {
        
        for (Action action : rule.getActions().getActions()) {
            if (OUTPUT_METRIC_ACTION.equalsIgnoreCase(action.getName())) {
                for (Parameter param : action.getParameters()) {
                    if (METRIC_PARAM_NAME.equals(param.getName())) {
                        return param.getValue();
                    }
                }
            }
        }
        return "";
    }
    
    /**
     * Get the actions of a rule filtered by its name
     */
    public static List<Action> getActions(MonitoringRule rule, String nameFilter) {
        List<Action> result = new ArrayList<Action>();
        
        if (nameFilter == null) {
            throw new NullPointerException("nameFilter cannot be null");
        }
        for (Action action : getActions(rule)) {
            if (nameFilter.equalsIgnoreCase(action.getName())) {
                result.add(action);
            }
        }
        return result;
    }
    
    /**
     * Wrapper to avoid a NPE
     */
    public static List<Action> getActions(MonitoringRule rule) {
        List<Action> result;
        
        if (rule.getActions() != null && rule.getActions().getActions() != null) {
            result = rule.getActions().getActions();
        }
        else {
            result = Collections.<Action>emptyList();
        }
        return result;
    }

    /**
     * Returns the rule associated to a constraint; NOT_FOUND_RULE if not found.
     */
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
