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
package eu.modaclouds.sla.mediator;

import it.polimi.modaclouds.qos_models.schema.Action;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;
import it.polimi.modaclouds.qos_models.schema.Parameter;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import eu.atos.sla.parser.data.wsag.Agreement;
import eu.atos.sla.parser.data.wsag.GuaranteeTerm;
import eu.atos.sla.parser.data.wsag.Template;
import eu.atos.sla.parser.data.wsag.Terms;

public class ViolationSubscriber {
    
    public static class Factory {
        
        private String metricsBaseUrl;
        private String callbackBaseUrl;

        /**
         * Factory for ViolationSubscriber's.
         * 
         * @param metricsBaseUrl url where to POST a new metric observer. It should be something like 
         * http://MONITORING_MANAGER_ADDRESS:MONITORING_MANAGER_LISTENING_PORT/v1/metrics. The final url 
         * is $baseurl/$action
         * @param callbackBaseUrl is the url that the Monitoring Platform will use to communicate the 
         * metric violations. The final url $baseurl/$agreement_id/$term_name
         */
        public Factory(String metricsBaseUrl, String callbackBaseUrl) {

            this.metricsBaseUrl = metricsBaseUrl;
            this.callbackBaseUrl = callbackBaseUrl;
        }
        
        public ViolationSubscriber getSubscriber(Agreement agreement, MonitoringRules rules) {
            return new ViolationSubscriber(metricsBaseUrl, callbackBaseUrl, agreement, rules);
        }
        
    }
    private Logger logger = LoggerFactory.getLogger(ViolationSubscriber.class);

    private static MonitoringRule NOT_FOUND_RULE = new MonitoringRule();

    private final String metricsBaseUrl;
    private final String callbackBaseUrl;
    private final Agreement agreement;
    private final MonitoringRules rules;
    
    public ViolationSubscriber(
            String metricsBaseUrl, String callbackBaseUrl, Agreement agreement, MonitoringRules rules) {
        this.metricsBaseUrl = metricsBaseUrl;
        this.callbackBaseUrl = callbackBaseUrl;
        this.agreement = agreement;
        this.rules = rules;
    }

//    public void subscribeObserver(
//            Template template, 
//            MonitoringRules rules) {
//        
//        subscribeObserver(template, template.getTerms(), rules);
//    }
    
    public void subscribeObserver(
            Agreement agreement, 
            MonitoringRules rules) {
        
        subscribeObserver(agreement, agreement.getTerms(), rules);
    }

    private void subscribeObserver(
            Agreement agreement,
            Terms terms, 
            MonitoringRules rules) {

        logger.debug("Subscribing {}", getAgreementDescription(agreement));
        for (GuaranteeTerm gt : terms.getAllTerms().getGuaranteeTerms()) {
            MonitoringRule rule = getRelatedRule(gt, rules);
            
            if (rule == NOT_FOUND_RULE) {
                
                logger.warn("Rule not found for {}.GuaranteeTerm[name={}] ",
                        getAgreementDescription(agreement), gt.getName());
                continue;
            }
            for (Action action : rule.getActions().getActions()) {
                if ("OutputMetric".equals(action.getName())) {
                    for (Parameter param : action.getParameters()) {
                        process(gt, rule, action, param);
                    }
                }
            }
        }
    }
    
    private String getAgreementDescription(Agreement agreement) {
        
        return agreement.toString();
    }
    
    private String getAgreementDescription(Template template) {
        
        return String.format("Template[id='%s']", template.getTemplateId());
    }
    
    private MonitoringRule getRelatedRule(GuaranteeTerm guarantee, MonitoringRules rules) {
        for (MonitoringRule rule : rules.getMonitoringRules()) {
            
            if (guarantee.getName().equals(rule.getRelatedQosConstraintId())) {
                
                return rule;
            }
        }
        return NOT_FOUND_RULE;
    }
    
    private void process(GuaranteeTerm term, MonitoringRule rule, Action action, Parameter parameter) {
        
        logger.debug("Subscribing to rule[id='{}', actionName='{}', paramValue='{}']", 
                rule.getId(), action.getName(), parameter.getValue());

        String url = getPostUrl(parameter);
        logger.debug("POST {}", url);
        
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        WebResource service = client.resource(UriBuilder.fromUri(url).build());
            
        String callbackUrl = getCallbackUrl(this.callbackBaseUrl, this.agreement, term);
        
        ClientResponse response = 
                service.type(MediaType.TEXT_PLAIN).
                accept(MediaType.TEXT_PLAIN).
                post(ClientResponse.class, callbackUrl);
        
        logger.debug("{} POST {}\n{}", response.getStatus(), url, response.getEntity(String.class));
        if (!isOk(response)) {
            logger.warn("Could not attach observer to {}", parameter.getValue());
        }
        else {
        }
    }
    
    private boolean isOk(ClientResponse response) {
        
        return response.getStatus() == Response.Status.CREATED.getStatusCode() ||
                response.getStatus() == Response.Status.OK.getStatusCode();
    }

    private String getPostUrl(Parameter parameter) {
        return String.format("%s/%s", metricsBaseUrl, parameter.getValue());
    }

    public String getMetricsBaseUrl() {
        return metricsBaseUrl;
    }

    public String getCallbackUrl() {
        return callbackBaseUrl;
    }
    
    private static String getCallbackUrl(String callbackBaseUrl, Agreement agreement, GuaranteeTerm term) {

        String path = "{base}";
        String result = UriBuilder.fromPath(path).
                queryParam("agreement", agreement.getAgreementId()).
                queryParam("term", term.getName()).
                build(callbackBaseUrl).toString();
        
//        String path = "{base}/{agreement}/{term}";
//        String result = UriBuilder.fromPath(path).build(
//                callbackBaseUrl, agreement.getAgreementId(), term.getName()
//        ).toString();
        
//        String result = Utils.join("/", callbackBaseUrl, agreement.getAgreementId(), term.getName());
        
        return result;
    }
}
