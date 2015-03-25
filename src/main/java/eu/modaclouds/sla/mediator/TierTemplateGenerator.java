package eu.modaclouds.sla.mediator;

import it.polimi.modaclouds.qos_models.schema.Constraints;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;
import it.polimi.modaclouds.qos_models.schema.ResourceContainer;

import java.util.Collections;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.atos.sla.datamodel.IAgreement;
import eu.atos.sla.parser.data.wsag.AllTerms;
import eu.atos.sla.parser.data.wsag.Context;
import eu.atos.sla.parser.data.wsag.GuaranteeTerm;
import eu.atos.sla.parser.data.wsag.ServiceDescriptionTerm;
import eu.atos.sla.parser.data.wsag.ServiceProperties;
import eu.atos.sla.parser.data.wsag.Template;
import eu.atos.sla.parser.data.wsag.Terms;
import eu.modaclouds.sla.mediator.model.palladio.Model;
import eu.modaclouds.sla.mediator.model.palladio.resourceextension.ResourceContainerWrapper;

public class TierTemplateGenerator {
    private static Logger logger = LoggerFactory.getLogger(TierTemplateGenerator.class);

    private ContextInfo ctx;
    
    public TierTemplateGenerator(ContextInfo ctx) {
        this.ctx = ctx;
    }
    
    public Template generateTemplate(Constraints constraints, MonitoringRules rules, Model model, String tierName) {
        String templateId = UUID.randomUUID().toString();
        Template t = this.generateTemplate(constraints, rules, model, tierName, templateId);
        return t;
    }
    
    public Template generateTemplate(
            Constraints constraints, MonitoringRules rules, Model model, String tierName, String templateId) {

        Template t = new Template();
        t.setTemplateId(templateId);
        
        Context context = new Context();
        t.setContext(context);
        context.setServiceProvider(IAgreement.Context.ServiceProvider.AGREEMENT_RESPONDER.toString());
        
        ResourceContainerWrapper wrapper = 
                (ResourceContainerWrapper) model.getResourceModelExtension().getElementById(tierName);
        ResourceContainer container = wrapper.getWrapped();
        
        context.setAgreementResponder(container.getProvider());
        context.setAgreementInitiator(ctx.getProvider());
        context.setService(container.getCloudResource().getServiceName());
        
        Terms terms = new Terms();
        terms.setAllTerms(new AllTerms());
        terms.getAllTerms().setServiceDescriptionTerm(new ServiceDescriptionTerm());
        terms.getAllTerms().setServiceProperties(Collections.<ServiceProperties>emptyList());
        terms.getAllTerms().setGuaranteeTerms(Collections.<GuaranteeTerm>emptyList());
        t.setTerms(terms);
        
        return t;
    }
}
