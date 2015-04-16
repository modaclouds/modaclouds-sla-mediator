/**
 * Copyright 2014 Atos
 * Contact: Atos <roman.sosa@atos.net>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package eu.modaclouds.sla.mediator.generation;

import it.polimi.modaclouds.qos_models.schema.Action;
import it.polimi.modaclouds.qos_models.schema.Parameter;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.modaclouds.sla.mediator.model.QosModels;

/**
 * This class parses a Business Action of a MonitoringRule. 
 * 
 * Usage: 
 * <pre><code>
 *   BusinessActionParser.Result data = new BusinessActionParser().parse(action);
 *   System.out.println(String.format("count=%d interval=%s", data.getCount(), data.getInterval());
 * </code></pre>
 * 
 * An example of MonitoringRule with a Business Action: 
 * <pre><code>
 * &lt;mo:monitoringRule id="..." label="ResponseTime_wa_auth" relatedQosConstraintId="..." startEnabled="true" timeStep="60" timeWindow="60">
 *   &lt;mo:collectedMetric inherited="false" metricName="ResponseTime">
 *     &lt;mo:parameter name="samplingProbability">1&lt;/mo:parameter>
 *   &lt;/mo:collectedMetric>
 *   &lt;mo:monitoredTargets>
 *     &lt;mo:monitoredTarget class="Method" type="..."/>
 *   &lt;/mo:monitoredTargets>
 *   &lt;mo:metricAggregation aggregateFunction="Average" groupingClass="Method"/>
 *   &lt;mo:condition>METRIC &gt; 10000.0&lt;/mo:condition>
 *   &lt;mo:actions>
 *     &lt;mo:action name="outputMetric">
 *       &lt;mo:parameter name="name">wa_auth_violated&lt;/mo:parameter>
 *     &lt;/mo:action>
 *     &lt;mo:action name="business">
 *       &lt;mo:parameter name="count">5&lt;/mo:parameter>
 *       &lt;mo:parameter name="duration">PT1H&lt;/mo:parameter>
 *       &lt;mo:parameter name="type">discount&lt;/mo:parameter>
 *       &lt;mo:parameter name="value">35&lt;/mo:parameter>
 *       &lt;mo:parameter name="unit">%&lt;/mo:parameter>
 *       &lt;mo:parameter name="validity">P1D&lt;/mo:parameter>
 *     &lt;/mo:action>
 *     &lt;mo:action name="business">...&lt;/mo:action>
 *   &lt;/mo:actions>
 * &lt;/mo:monitoringRule>
 *
 * </code></pre>
 */
public class BusinessActionParser {

    private static final String PARAM_VALIDITY = "validity";
    private static final String PARAM_UNIT = "unit";
    private static final String PARAM_VALUE = "value";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_DURATION = "duration";
    private static final String PARAM_COUNT = "count";

    private static Logger logger = LoggerFactory.getLogger(BusinessActionParser.class);
    private static DatatypeFactory dtFactory;
    
    static {
        try {
            dtFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public BusinessActionParser() throws DatatypeConfigurationException {
        if (dtFactory == null) {
            throw new DatatypeConfigurationException("Could not construct DatatypeFactory");
        }
    }
    
    public Result parse(Action action) {
        if (!QosModels.BUSINESS_ACTION.equalsIgnoreCase(action.getName())) {
            throw new IllegalArgumentException(
                    String.format("Action[name=\"%s\"] is not a Business Action", action.getName()));
        }

        int count = 1;
        Duration interval = dtFactory.newDuration(0);
        String type = "";
        String value = "";
        String unit = "";
        String validity = "";
        
        for (Parameter param : action.getParameters()) {
            
            switch (param.getName()) {
            case PARAM_COUNT:
                try {
                    count = Integer.parseInt(param.getValue());
                } catch (NumberFormatException e) {
                    logger.warn("Invalid count value: " + param.getValue());
                }
                break;
            case PARAM_DURATION:
                try {
                    interval = dtFactory.newDuration(param.getValue());
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid interval value: " + param.getValue());
                }
                break;
            case PARAM_TYPE:
                type = param.getValue();
                break;
            case PARAM_VALUE:
                value = param.getValue();
                break;
            case PARAM_UNIT:
                unit = param.getValue();
                break;
            case PARAM_VALIDITY:
                validity = param.getValue();
                break;
            default:
                break;
            }
        }
        return new Result(count, interval, type, value, unit, validity);
    }

    /**
     * Stores parsed data of a Business Action.
     * 
     * The duration property holds a "" if count <= 1
     */
    public static class Result {
        private int count;
        private Duration duration;
        private String type;
        private String value;
        private String unit;
        private String validity;
        
        public Result(int count, Duration duration, String type, String value,
                String unit, String validity) {
            if (count <= 1) {
                duration = BusinessActionParser.dtFactory.newDuration(0);
            }
            this.count = count;
            this.duration = duration;
            this.type = type;
            this.value = value;
            this.unit = unit;
            this.validity = validity;
        }

        public int getCount() {
            return count;
        }
        
        public Duration getDuration() {
            return duration;
        }
        
        public String getType() {
            return type;
        }
        
        
        public String getValue() {
            return value;
        }
        
        public String getUnit() {
            return unit;
        }
        
        public String getValidity() {
            return validity;
        }
    }

}
