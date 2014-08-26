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

import java.util.ArrayList;
import java.util.HashMap;
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
import eu.atos.sla.parser.data.wsag.AllTerms;
import eu.atos.sla.parser.data.wsag.Context;
import eu.atos.sla.parser.data.wsag.GuaranteeTerm;
import eu.atos.sla.parser.data.wsag.KPITarget;
import eu.atos.sla.parser.data.wsag.ServiceDescriptionTerm;
import eu.atos.sla.parser.data.wsag.ServiceLevelObjective;
import eu.atos.sla.parser.data.wsag.ServiceProperties;
import eu.atos.sla.parser.data.wsag.ServiceScope;
import eu.atos.sla.parser.data.wsag.Template;
import eu.atos.sla.parser.data.wsag.Terms;
import eu.modaclouds.sla.mediator.model.constraints.TargetClass;
import eu.modaclouds.sla.mediator.model.monitoringrules.Action;
import eu.modaclouds.sla.mediator.model.monitoringrules.MonitoringRule;
import eu.modaclouds.sla.mediator.model.monitoringrules.MonitoringRules;
import eu.modaclouds.sla.mediator.model.monitoringrules.Parameter;
import eu.modaclouds.sla.mediator.model.palladio.IReferrable;
import eu.modaclouds.sla.mediator.model.palladio.RepositoryDocument;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.Component;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.Operation;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.SeffSpecification;
import eu.modaclouds.sla.mediator.model.qosconstraints.Constraint;
import eu.modaclouds.sla.mediator.model.qosconstraints.Constraints;
import eu.modaclouds.sla.mediator.model.qosconstraints.Range;

public class TemplateGenerator {
    private static Logger logger = LoggerFactory.getLogger(TemplateGenerator.class);

    private static MonitoringRule NOT_FOUND_RULE = new MonitoringRule();
    private static Component NOT_FOUND_COMPONENT = new Component();

    private String consumer;
    private String provider;
    private String service;
    
    public TemplateGenerator(ContextInfo ctx) {
        this.provider = ctx.getProvider();
        this.consumer = ctx.getConsumer();
        this.service = ctx.getService();
    }
    
    static {
//        NOT_FOUND_COMPONENT.setEntityName("null");
    }
    
    public Template generateTemplate(Constraints constraints, MonitoringRules rules, RepositoryDocument document) {

        logger.debug("generateTemplate");
        
        Template result = new Template();
        result.setTemplateId("template-test");  /* TODO change to uuid4 */
        
        Context context = new Context();
        result.setContext(context);
        context.setServiceProvider(IAgreement.Context.ServiceProvider.AGREEMENT_RESPONDER.toString());
        context.setAgreementResponder(provider);
        context.setAgreementInitiator(consumer);
        context.setService(service);
        
        Terms terms = new Terms();
        AllTerms allTerms = new AllTerms();
        result.setTerms(terms);
        terms.setAllTerms(allTerms);

        ServiceDescriptionTerm sdt = new ServiceDescriptionTerm();
        allTerms.setServiceDescriptionTerm(sdt);
        
        List<ServiceProperties> serviceProperties = new ArrayList<>();
        allTerms.setServiceProperties(serviceProperties);
        
        List<GuaranteeTerm> gts = new ArrayList<>();
        allTerms.setGuaranteeTerms(gts);
        
        for (Constraint constraint : constraints.getConstraints()) {
            
            TargetClass target = TargetClass.fromString(constraint.getTargetClass());
            
            switch (target) {
            case INTERNAL_COMPONENT:
            case METHOD:
                MonitoringRule rule = getRelatedRule(constraint, rules);
                GuaranteeTerm gt = generateGuaranteeTerm(constraint, rule, document);
                gts.add(gt);

            default:
                /*
                 * do not generate guarantee term
                 */
                break;
            }
        }
        
        return result;
    }
    
    private MonitoringRule getRelatedRule(Constraint constraint, MonitoringRules rules) {
        
        String constraintId = constraint.getId();
        if (constraintId == null) {
            throw new NullPointerException("Not valid constraint: id is null");
        }
        for (MonitoringRule rule : rules.getMonitoringRules()) {
            if (constraintId.equals(rule.getRelatedQosConstraintId())) {
                return rule;
            }
        }
        return NOT_FOUND_RULE;
    }
    
    private GuaranteeTerm generateGuaranteeTerm(
            Constraint constraint, 
            MonitoringRule rule, 
            RepositoryDocument document) {
        
        logger.debug("Generate guaranteeTerm({}, {}, {}", 
                constraint.getId(), rule.getId(), document.getJAXBNode().getId());
        
        GuaranteeTerm gt = new GuaranteeTerm();
        
//        gt.setName(getUuid());
        gt.setName(constraint.getId());

        ServiceScope serviceScope = new ServiceScoper().generate(constraint, document);
        gt.setServiceScope(serviceScope);
        
        ServiceLevelObjective slo = new ServiceLevelObjective();
        KPITarget kpi = new KPITarget();
        kpi.setKpiName(constraint.getMetric());
//        kpi.setCustomServiceLevel(String.format("{\"constraint\": \"NOT(%s)\"}", rule.getCondition().getValue()));
        try {
            kpi.setCustomServiceLevel(String.format(
                    "{\"constraint\": \"%s EXISTS\", \"qos\": %s}",
                    getOutputMetric(rule),
                    rangeAsJson(constraint.getRange())
                    ));
        } catch (JsonProcessingException e) {
            throw new GeneratorException(e.getMessage(), e);
        }
        slo.setKpitarget(kpi);
        gt.setServiceLevelObjetive(slo);
        
        return gt;
    }
    
    private String getUuid() {
        return UUID.randomUUID().toString();
    }
    
    private String getOutputMetric(MonitoringRule rule) {
        
        for (Action action : rule.getActions().getActions()) {
            if ("OutputMetric".equals(action.getName())) {
                for (Parameter param : action.getParameters()) {
                    return param.getValue();
                }
            }
        }
        return "";
    }
    
    private String rangeAsJson(Range range) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector introspector = new JacksonAnnotationIntrospector();
        mapper.getSerializationConfig().with(introspector);
        mapper.setSerializationInclusion(Include.NON_NULL);

        return mapper.writeValueAsString(range);
    }
    
    public interface IServiceScoper {
        ServiceScope generate(Constraint constraint, RepositoryDocument document);
    }
    
    public static class ServiceScoper implements IServiceScoper {
        static HashMap<TargetClass, IServiceScoper> map = new HashMap<>();
        
        static {
            map.put(TargetClass.METHOD, new MethodServiceScoper());
            map.put(TargetClass.INTERNAL_COMPONENT, new InternalComponentServiceScoper());
        }
        
        @Override
        public ServiceScope generate(Constraint constraint,
                RepositoryDocument document) {
            
            TargetClass target = TargetClass.fromString(constraint.getTargetClass());

            IServiceScoper scoper = map.get(target);
            
            ServiceScope scope = scoper.generate(constraint, document);
            return scope;
        }
    }
    
    public static class InternalComponentServiceScoper implements IServiceScoper {

        @Override
        public ServiceScope generate(Constraint constraint,
                RepositoryDocument document) {
            
            IReferrable referrable = 
                    document.getElementById(constraint.getTargetResourceIDRef());
            
            ServiceScope result = new ServiceScope();
            
            if (referrable != RepositoryDocument.NOT_FOUND) {
                if (referrable instanceof Component) {
                    result.setServiceName("");
                    result.setValue(referrable.getEntityName());
                }
            }
            /*
             * process errors
             */
            return result;
        }
    }

    public static class MethodServiceScoper implements IServiceScoper {

        @Override
        public ServiceScope generate(Constraint constraint,
                RepositoryDocument document) {
            
            IReferrable referrable = 
                    document.getElementById(constraint.getTargetResourceIDRef());
            
            ServiceScope result = new ServiceScope();
            
            if (referrable != RepositoryDocument.NOT_FOUND) {
                if (referrable instanceof SeffSpecification) {
                    result = process(constraint, document, (SeffSpecification) referrable);
                }
            }
            /*
             * process errors
             */
            return result;
        }
        
        private ServiceScope process(Constraint constraint, RepositoryDocument document, SeffSpecification element) {
            
            Component parent = element.getParent();
            Operation operation = element.getOperation(document);
            ServiceScope result = new ServiceScope();
            result.setServiceName("");
            result.setValue(String.format("%s/%s", parent.getEntityName(), operation.getEntityName()));
            
            return result;
        }
    }
}
