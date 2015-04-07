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
import it.polimi.modaclouds.qos_models.schema.Actions;
import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.qos_models.schema.Constraints;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;
import it.polimi.modaclouds.qos_models.schema.Parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import eu.atos.sla.datamodel.IAgreement;
import eu.atos.sla.parser.data.wsag.AllTerms;
import eu.atos.sla.parser.data.wsag.BusinessValueList;
import eu.atos.sla.parser.data.wsag.Context;
import eu.atos.sla.parser.data.wsag.GuaranteeTerm;
import eu.atos.sla.parser.data.wsag.KPITarget;
import eu.atos.sla.parser.data.wsag.ServiceDescriptionTerm;
import eu.atos.sla.parser.data.wsag.ServiceLevelObjective;
import eu.atos.sla.parser.data.wsag.ServiceProperties;
import eu.atos.sla.parser.data.wsag.ServiceScope;
import eu.atos.sla.parser.data.wsag.Template;
import eu.atos.sla.parser.data.wsag.Terms;
import eu.atos.sla.parser.data.wsag.custom.CustomBusinessValue;
import eu.atos.sla.parser.data.wsag.custom.Penalty;
import eu.modaclouds.sla.mediator.model.constraints.TargetClass;
import eu.modaclouds.sla.mediator.model.palladio.IDocument;
import eu.modaclouds.sla.mediator.model.palladio.IReferrable;
import eu.modaclouds.sla.mediator.model.palladio.Model;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.Component;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.Operation;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.SeffSpecification;

public class TemplateGenerator {
    public static final String CONSTRAINT = "constraint";

    private static Logger logger = LoggerFactory.getLogger(TemplateGenerator.class);

    private static final String DEFAULT_SERVICE_NAME = "service";

    private static MonitoringRule NOT_FOUND_RULE = new MonitoringRule();
    
    private static GuaranteeTerm NULL_GUARANTEE_TERM = new GuaranteeTerm();
    
    static {
        NOT_FOUND_RULE.setActions(new Actions());
    }
    private static Component NOT_FOUND_COMPONENT = new Component();

    private BusinessActionParser businessActionParser;
    private String consumer;
    private String provider;
    private String service;
    
    private ObjectMapper jsonMapper;
    
    public TemplateGenerator(ContextInfo ctx) {
        this.provider = ctx.getProvider();
        this.consumer = ctx.getConsumer();
        this.service = ctx.getService();
        
        try {
            this.businessActionParser = new BusinessActionParser();
        } catch (DatatypeConfigurationException e) {
            throw new MediatorException("Error creating BusinessActionParser", e);
        }
    }
    
    static {
//        NOT_FOUND_COMPONENT.setEntityName("null");
    }
    
    public Template generateTemplate(Constraints constraints, MonitoringRules rules, Model model) {
        String templateId = UUID.randomUUID().toString();
        Template t = this.generateTemplate(constraints, rules, model, templateId);
        return t;
    }
    
    public Template generateTemplate(
            Constraints constraints, MonitoringRules rules, Model model, String templateId) {

        logger.debug("generateTemplate");
        IDocument<Repository> repository = model.getRepository();
        Template result = new Template();
        result.setTemplateId(templateId);
        
        Context context = new Context();
        result.setContext(context);
        context.setServiceProvider(IAgreement.Context.ServiceProvider.AGREEMENT_RESPONDER.toString());
        context.setAgreementResponder(provider);
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
                if (rule == NOT_FOUND_RULE) {
                    logger.warn("Related rule not found: constraintId={}", constraint.getId());
                    continue;
                }
                GuaranteeTerm gt = generateGuaranteeTerm(constraint, rule, repository);
                if (gt != NULL_GUARANTEE_TERM) {
                    gts.add(gt);
                }

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
    
    private GuaranteeTerm generateGuaranteeTerm(
            Constraint constraint, 
            MonitoringRule rule, 
            IDocument<Repository> document) {
        
        logger.debug("Generate guaranteeTerm({}, {}, {}", 
                constraint.getId(), rule.getId(), document.getJAXBNode().getId());

        GuaranteeTerm gt;
        String outputMetric = getOutputMetric(rule);
        
        if ("".equals(outputMetric)) {
            logger.warn("OutputMetric is not defined. GuaranteeTerm cannot be added to agreement");
            gt = NULL_GUARANTEE_TERM;
            /*
             * fall to return
             */
        }
        else {
        
            gt = new GuaranteeTerm();
            
            gt.setName(constraint.getId());
    
            ServiceScope serviceScope = new ServiceScoper().generate(constraint, document);
            gt.setServiceScope(serviceScope);
            
            ServiceLevelObjective slo = new ServiceLevelObjective();
            KPITarget kpi = new KPITarget();
            kpi.setKpiName(constraint.getMetric());
            try {
                kpi.setCustomServiceLevel(String.format(
                        "{\"%s\": \"%s NOT_EXISTS\", \"qos\": %s, \"aggregation\": %s}",
                        CONSTRAINT,
                        outputMetric,
                        toJson(constraint.getRange()),
                        toJson(constraint.getMetricAggregation())
                        ));
            } catch (JsonProcessingException e) {
                throw new GeneratorException(e.getMessage(), e);
            }
            slo.setKpitarget(kpi);
            gt.setServiceLevelObjetive(slo);
            
            gt = generateBusinessValueList(gt, rule);
        }
        
        return gt;
    }
    
    private String getUuid() {
        return UUID.randomUUID().toString();
    }
    
    private String getOutputMetric(MonitoringRule rule) {
        
        for (Action action : rule.getActions().getActions()) {
            if ("OutputMetric".equalsIgnoreCase(action.getName())) {
                for (Parameter param : action.getParameters()) {
                    if ("name".equals(param.getName())) {
                        return param.getValue();
                    }
                }
            }
        }
        return "";
    }

    private GuaranteeTerm generateBusinessValueList(GuaranteeTerm gt, MonitoringRule rule) {
        
        for (Action action : rule.getActions().getActions()) {
            if ("Business".equalsIgnoreCase(action.getName())) {
                BusinessActionParser.Result data = businessActionParser.parse(action);
                if (gt.getBusinessValueList() == null) {
                    gt.setBusinessValueList(new BusinessValueList());
                }
                CustomBusinessValue customBusinessValue = 
                        new CustomBusinessValue(data.getCount(), data.getDuration());
                Penalty penalty = newPenalty(data);
                
                customBusinessValue.addPenalty(penalty);
                gt.getBusinessValueList().getCustomBusinessValue().add(customBusinessValue);
            }
        }
        return gt;
    }

    private Penalty newPenalty(BusinessActionParser.Result data) {
        return new Penalty(data.getType(), data.getValue(), data.getUnit(), data.getValidity());
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
    
    public interface IServiceScoper {
        ServiceScope generate(Constraint constraint, IDocument<Repository> document);
    }
    
    public static class ServiceScoper implements IServiceScoper {
        static HashMap<TargetClass, IServiceScoper> map = new HashMap<>();
        
        static {
            map.put(TargetClass.METHOD, new MethodServiceScoper());
            map.put(TargetClass.INTERNAL_COMPONENT, new InternalComponentServiceScoper());
        }
        
        @Override
        public ServiceScope generate(Constraint constraint,
                IDocument<Repository> document) {
            
            TargetClass target = TargetClass.fromString(constraint.getTargetClass());

            IServiceScoper scoper = map.get(target);
            
            ServiceScope scope = scoper.generate(constraint, document);
            return scope;
        }
    }
    
    public static class InternalComponentServiceScoper implements IServiceScoper {

        @Override
        public ServiceScope generate(Constraint constraint,
                IDocument<Repository> document) {
            
            IReferrable referrable = 
                    document.getElementById(constraint.getTargetResourceIDRef());
            
            ServiceScope result = new ServiceScope();
            
            if (referrable != IDocument.NOT_FOUND) {
                if (referrable instanceof Component) {
                    result.setServiceName(DEFAULT_SERVICE_NAME);
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
                IDocument<Repository> document) {
            
            IReferrable referrable = 
                    document.getElementById(constraint.getTargetResourceIDRef());
            
            ServiceScope result = new ServiceScope();
            
            if (referrable != IDocument.NOT_FOUND) {
                if (referrable instanceof SeffSpecification) {
                    result = process(constraint, document, (SeffSpecification) referrable);
                }
            }
            /*
             * process errors
             */
            return result;
        }
        
        private ServiceScope process(Constraint constraint, IDocument<Repository> document, SeffSpecification element) {
            
            Component parent = element.getParent();
            Operation operation = element.getOperation(document);
            ServiceScope result = new ServiceScope();
            result.setServiceName(DEFAULT_SERVICE_NAME);
            result.setValue(String.format("%s/%s", parent.getEntityName(), operation.getEntityName()));
            
            return result;
        }
    }
}
