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

import it.polimi.modaclouds.qos_models.schema.Constraints;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;

import java.util.Collections;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.atos.sla.datamodel.IAgreement;
import eu.atos.sla.parser.data.wsag.Agreement;
import eu.atos.sla.parser.data.wsag.AllTerms;
import eu.atos.sla.parser.data.wsag.Context;
import eu.atos.sla.parser.data.wsag.GuaranteeTerm;
import eu.atos.sla.parser.data.wsag.ServiceDescriptionTerm;
import eu.atos.sla.parser.data.wsag.ServiceProperties;
import eu.atos.sla.parser.data.wsag.Template;
import eu.atos.sla.parser.data.wsag.Terms;
import eu.modaclouds.sla.mediator.model.palladio.Model;

public class TierTemplateGenerator {
    private static Logger logger = LoggerFactory.getLogger(TierTemplateGenerator.class);

    private ContextInfo highContext;
    private Agreement highAgreement;
    
    public TierTemplateGenerator(ContextInfo highContext, Agreement highAgreement) {
        this.highContext= highContext;
        this.highAgreement = highAgreement;
    }
    
    public Template generateTemplate(
            Constraints constraints, MonitoringRules rules, Model model, String tierName, ContextInfo lowContext) {
        
        String templateId = UUID.randomUUID().toString();
        Template t = this.generateTemplate(constraints, rules, model, tierName, lowContext, templateId);
        return t;
    }
    
    public Template generateTemplate(
            Constraints constraints, 
            MonitoringRules rules, 
            Model model, 
            String tierName, 
            ContextInfo lowContext, 
            String templateId) {

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
        terms.getAllTerms().setGuaranteeTerms(Collections.<GuaranteeTerm>emptyList());
        t.setTerms(terms);
        
        return t;
    }
}
