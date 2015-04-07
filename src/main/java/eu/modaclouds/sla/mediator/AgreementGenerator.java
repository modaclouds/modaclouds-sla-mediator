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

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.atos.sla.parser.data.wsag.Agreement;
import eu.atos.sla.parser.data.wsag.AllTerms;
import eu.atos.sla.parser.data.wsag.Context;
import eu.atos.sla.parser.data.wsag.GuaranteeTerm;
import eu.atos.sla.parser.data.wsag.ServiceDescriptionTerm;
import eu.atos.sla.parser.data.wsag.ServiceProperties;
import eu.atos.sla.parser.data.wsag.Template;
import eu.atos.sla.parser.data.wsag.Terms;

public class AgreementGenerator {
    
    public static class AgreementGeneratorFactory {
        private Template template;

        public AgreementGeneratorFactory(Template template) {
            this.template = template;
        }
        
        public AgreementGenerator getGenerator(ContextInfo ctx) {
            return new AgreementGenerator(template, ctx);
        }
    }

    private static Logger logger = LoggerFactory.getLogger(AgreementGenerator.class);
    private final Template template;
    private final ContextInfo ctx;
    private final String master;

    /**
     * AgreementGenerator of an agreement without master
     * 
     * @param template template to be based on
     * @param ctx context of agreement
     */
    public AgreementGenerator(Template template, ContextInfo ctx) {
        this(template, ctx, "");
    }

    /**
     * AgreementGenerator of an agreement with master
     * 
     * @param template template to be based on
     * @param ctx context of agreement
     * @param master agreement id of master
     */
    public AgreementGenerator(Template template, ContextInfo ctx, String master) {
        this.template = template;
        this.ctx = ctx;
        this.master = master;
    }
    
    public Agreement generateAgreement() {
        String agreementId = UUID.randomUUID().toString();
        
        Agreement a = generateAgreement(agreementId);
        return a;
    }
    
    public Agreement generateAgreement(String agreementId) {
    
        Agreement agreement = new Agreement();
        
        agreement.setAgreementId(agreementId);
        
        String name = String.format("%s-%s-%s", ctx.getConsumer(), ctx.getProvider(), ctx.getService());
        agreement.setName(name);
        
        agreement.setContext(generateContext());
        agreement.setTerms(generateTerms());
        
        return agreement;
    }
    
    private Context generateContext() {
        
        Context result = new Context();
        result.setAgreementInitiator(ctx.getConsumer());
        result.setAgreementResponder(ctx.getProvider());
        result.setServiceProvider(Context.ServiceProvider.AGREEMENT_RESPONDER.toString());
        result.setExpirationTime(ctx.getValidity().add(new Date()));
        result.setTemplateId(template.getTemplateId());
        result.setService(ctx.getService());
        if (!"".equals(master) ) {
            result.setMaster(master);
        }
        
        return result;
    }

    private Terms generateTerms() {
        Terms result = new Terms();
        AllTerms all = new AllTerms();
        result.setAllTerms(all);
        
        all.setServiceDescriptionTerm(generateServiceDescriptionTerms());
        all.setServiceProperties(generateServiceProperties());
        all.setGuaranteeTerms(generateGuaranteeTerms());
        
        return result;
    }
    
    private ServiceDescriptionTerm generateServiceDescriptionTerms() {
        /* XXX quick and dirty */
        return template.getTerms().getAllTerms().getServiceDescriptionTerm();
    }
    
    private List<ServiceProperties> generateServiceProperties() {
        /* XXX quick and dirty */
        return template.getTerms().getAllTerms().getServiceProperties();
    }
    
    private List<GuaranteeTerm> generateGuaranteeTerms() {
        /* XXX quick and dirty */
        return template.getTerms().getAllTerms().getGuaranteeTerms();
    }
}
