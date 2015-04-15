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

import it.polimi.modaclouds.qos_models.schema.Condition;
import it.polimi.modaclouds.qos_models.schema.Constraints;
import it.polimi.modaclouds.qos_models.schema.MonitoredTarget;
import it.polimi.modaclouds.qos_models.schema.MonitoredTargets;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import eu.atos.sla.datamodel.IAgreement;
import eu.atos.sla.parser.data.wsag.Agreement;
import eu.atos.sla.parser.data.wsag.AllTerms;
import eu.atos.sla.parser.data.wsag.Context;
import eu.atos.sla.parser.data.wsag.GuaranteeTerm;
import eu.atos.sla.parser.data.wsag.KPITarget;
import eu.atos.sla.parser.data.wsag.ServiceDescriptionTerm;
import eu.atos.sla.parser.data.wsag.ServiceLevelObjective;
import eu.atos.sla.parser.data.wsag.ServiceProperties;
import eu.atos.sla.parser.data.wsag.Template;
import eu.atos.sla.parser.data.wsag.Terms;
import eu.modaclouds.sla.mediator.model.ModelUtils;
import eu.modaclouds.sla.mediator.model.constraints.TargetClass;
import eu.modaclouds.sla.mediator.model.palladio.IReferrable;
import eu.modaclouds.sla.mediator.model.palladio.Model;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.Component;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.SeffSpecification;
import eu.modaclouds.sla.mediator.model.palladio.resourceenvironment.ResourceEnvironment.ResourceContainer;

public class TierTemplateGenerator {
    private static Logger logger = LoggerFactory.getLogger(TierTemplateGenerator.class);

    private static GuaranteeTerm NULL_GUARANTEE_TERM = new GuaranteeTerm();
    
    private ContextInfo highContext;
    private Agreement highAgreement;

    private ObjectMapper jsonMapper;
    
    public TierTemplateGenerator(ContextInfo highContext, Agreement highAgreement) {
        this.highContext= highContext;
        this.highAgreement = highAgreement;
    }
    
    public Template generateTemplate(
            Constraints constraints, MonitoringRules rules, Model model, String tierId, ContextInfo lowContext) {
        
        String templateId = UUID.randomUUID().toString();
        Template t = this.generateTemplate(constraints, rules, model, tierId, lowContext, templateId);
        return t;
    }
    
    public Template generateTemplate(
            Constraints constraints, 
            MonitoringRules rules, 
            Model model, 
            String tierId, 
            ContextInfo lowContext, 
            String templateId) {

        ResourceContainer tier = model.getResourceContainer(tierId);
        Template t = new Template();
        t.setTemplateId(templateId);
        
        Context context = new Context();
        t.setContext(context);
        context.setServiceProvider(IAgreement.Context.ServiceProvider.AGREEMENT_RESPONDER.toString());
        context.setAgreementResponder(lowContext.getProvider());
        context.setService(lowContext.getService());

        /*
         * ServiceInitiator and Master to be filled in AgreementGenerator
         */
        
        Terms terms = new Terms();
        terms.setAllTerms(new AllTerms());
        terms.getAllTerms().setServiceDescriptionTerm(new ServiceDescriptionTerm());
        terms.getAllTerms().setServiceProperties(Collections.<ServiceProperties>emptyList());

        List<GuaranteeTerm> gts = new ArrayList<>();
        terms.getAllTerms().setGuaranteeTerms(gts);
        
        for (MonitoringRule rule : rules.getMonitoringRules()) {
            
            if (isSuitableRule(rule, model, tier)) {
                GuaranteeTerm gt = generateGuaranteeTerm(rule, model);
                if (gt != NULL_GUARANTEE_TERM) {
                    gts.add(gt);
                }
            }
        }
        t.setTerms(terms);
        
        return t;
    }

    private boolean isSuitableRule(MonitoringRule rule, Model model, ResourceContainer tier) {
        boolean result;
        result = "Average".equals(getAggregationFunction(rule)) &&
                !"".equals(ModelUtils.getOutputMetric(rule)) &&
                isNotEmpty(rule.getCondition()) &&
                isValidTarget(rule.getMonitoredTargets(), tier, model);
        return result;
    }
    
    /**
     * Rule valid if any of the targets is monitoring the tierId container.
     */
    private boolean isValidTarget(MonitoredTargets monitoredTargets, ResourceContainer tier, Model model) {
        boolean result = false;
        
        for (MonitoredTarget target : monitoredTargets.getMonitoredTargets()) {
            ResourceContainer thisTier = getResourceContainerFromTarget(model,
                    target);
            if (tier.equals(thisTier)) {
                result = true;
            }
        }
        return result;
    }

    private ResourceContainer getResourceContainerFromTarget(Model model,
            MonitoredTarget target) {

        ResourceContainer result = ResourceContainer.NOT_FOUND;
        Component comp;

        String targetId = target.getType();
        TargetClass clazz = TargetClass.fromString(target.getClazz());
        IReferrable ref = model.getRepository().getElementById(targetId);
        
        switch (clazz) {
        case METHOD:
            SeffSpecification seff = (SeffSpecification) ref;
            comp = (Component) seff.getParent();
            break;
        case INTERNAL_COMPONENT:
            comp = (Component) ref;
            break;
        default:
            comp = Component.NOT_FOUND;
            break;
        }
        if (comp != Component.NOT_FOUND) {
            result = model.getResourceContainer(comp);
        }
        return result;
    }

    private boolean isNotEmpty(Condition condition) {
        return condition != null && !"".equals(condition);
    }

    private String getAggregationFunction(MonitoringRule rule) {
        String result = "";
        if (rule.getMetricAggregation() != null && rule.getMetricAggregation().getAggregateFunction() != null) {
            result = rule.getMetricAggregation().getAggregateFunction();
        }
        return result;
    }
    
    private GuaranteeTerm generateGuaranteeTerm(
            MonitoringRule rule, 
            Model model) {
        
        logger.debug("Generate guaranteeTerm({}, {}, {}", 
                rule.getId(), model.getRepository().getJAXBNode().getId());
        
        GuaranteeTerm gt;
        String outputMetric = ModelUtils.getOutputMetric(rule);
        
        if ("".equals(outputMetric)) {
            logger.warn("OutputMetric is not defined. GuaranteeTerm cannot be added to agreement");
            gt = new GuaranteeTerm(); //NULL_GUARANTEE_TERM;
            /*
             * fall to return
             */
        }
        else {
        
            gt = new GuaranteeTerm();
            
            gt.setName(rule.getId());
    
            /* TODO */
//            ServiceScope serviceScope = new ServiceScoper().generate(constraint, model.getRepository());
//            gt.setServiceScope(serviceScope);
            
            ServiceLevelObjective slo = new ServiceLevelObjective();
            KPITarget kpi = new KPITarget();
            kpi.setKpiName(rule.getCollectedMetric().getMetricName());
            kpi.setCustomServiceLevel(String.format(
                    "{\"constraint\": \"%s NOT_EXISTS\", \"qos\": %s, \"aggregation\": %s}",
                    outputMetric,
                    rule.getCondition(),
                    getAggregationFunction(rule)));
            slo.setKpitarget(kpi);
            gt.setServiceLevelObjetive(slo);
        }
        
        return gt;
    }
    
    private <T> String toJson(T t) throws JsonProcessingException {
        if (jsonMapper == null) {
            jsonMapper = new ObjectMapper();
            AnnotationIntrospector introspector = new JacksonAnnotationIntrospector();
            jsonMapper.getSerializationConfig().with(introspector);
            jsonMapper.setSerializationInclusion(Include.NON_NULL);
        }

        return jsonMapper.writeValueAsString(t);
    }
    
}
