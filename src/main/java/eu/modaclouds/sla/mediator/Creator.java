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
import it.polimi.modaclouds.qos_models.schema.ResourceModelExtension;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.Cli;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

import eu.atos.sla.client.SlaClient;
import eu.atos.sla.client.SlaException;
import eu.atos.sla.parser.data.Provider;
import eu.atos.sla.parser.data.wsag.Agreement;
import eu.atos.sla.parser.data.wsag.Template;
import eu.modaclouds.sla.mediator.generation.AgreementGenerator;
import eu.modaclouds.sla.mediator.generation.TemplateGenerator;
import eu.modaclouds.sla.mediator.generation.TierTemplateGenerator;
import eu.modaclouds.sla.mediator.model.palladio.Model;
import eu.modaclouds.sla.mediator.model.palladio.allocation.Allocation;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository;
import eu.modaclouds.sla.mediator.model.palladio.resourceenvironment.ResourceEnvironment;
import eu.modaclouds.sla.mediator.model.palladio.resourceenvironment.ResourceEnvironment.ResourceContainer;
import eu.modaclouds.sla.mediator.model.palladio.resourceextension.ResourceContainerWrapper;
import eu.modaclouds.sla.mediator.model.palladio.system.System;

/**
 * Generate agreements/templates based on constraints and monitoring rules,
 * and stores them in the sla core.
 * 
 * @author rsosa
 *
 */
public class Creator {

    public static class Factory {
        
        private final SlaCoreConfig slaCoreConfig;
        public Factory(String slaCoreUrl, String slaCoreUser,
                String slaCorePassword) {

            this.slaCoreConfig = new SlaCoreConfig(slaCoreUrl, slaCoreUser, slaCorePassword);
        }
        
        public Creator getCreator(ContextInfo ctx) {
            return new Creator(slaCoreConfig, ctx);
        }
    }
    
    /**
     * Result of run Sla method.
     * 
     * Contains:
     * <li>highId: customer – application provider agreement id 
     * <li>lowIds: map of (resource container id, application provider – cloud providers agreement id). 
     * The resource container id is the one in resourceenvironment file/resourceContainer_ResourceEnvironment element.
     * <li>model: the model built from the input files (useful for debugging)
     */
    public static class Result {
        private final String highId;
        private final Map<String, String> lowIds;
        private final Model model;
        
        /**
         * Constructs Result of runSla method
         * 
         * @param highId AgreementId of high agreement
         * @param lowIds AgreementId of low agreements
         * @param model Model built from the input files
         */
        public Result(String highId, Map<String, String> lowIds, Model model) {
            this.highId = highId;
            this.lowIds = Collections.<String, String>unmodifiableMap(lowIds);
            this.model = model;
        }
        
        public String getHighId() {
            return highId;
        }
        
        public Map<String, String> getLowIds() {
            return lowIds;
        }
        
        public Model getModel() {
            return model;
        }
    }
    
    private static final Logger logger = LoggerFactory.getLogger(Creator.class);
  
    private static final Provider PROVIDER_NOT_FOUND;
    static {
        PROVIDER_NOT_FOUND = new Provider(0L, "<NOTFOUND>", "<NOTFOUND>");
    }
    
    private String slaCoreUrl;
    private SlaClient client;
    
    private TemplateGenerator generator;
    private ContextInfo ctx;
    private boolean persist;

    /**
     * Constructs a Creator.
     * 
     * @param slaCoreConfig config to access sla core.
     * @param ctx info 
     */
    public Creator(SlaCoreConfig slaCoreConfig, ContextInfo ctx) {
        this.slaCoreUrl = slaCoreConfig.getSlaCoreUrl();
        this.client = new SlaClient(
                slaCoreUrl, 
                MediaType.APPLICATION_XML_TYPE, 
                slaCoreConfig.getSlaCoreUser(), 
                slaCoreConfig.getSlaCorePassword()
        );
        this.generator = new TemplateGenerator(ctx);
        this.ctx = ctx;
        this.persist = false;   /* XXX: change when persist ready */
    }

    public Creator(SlaCoreConfig slaCoreConfig, ContextInfo ctx, boolean persist) {
        this(slaCoreConfig, ctx);
        this.persist = persist;
    }
    
    public Result runSla(
            InputStream constraintsIs, 
            InputStream rulesIs, 
            InputStream repositoryIs,
            InputStream allocationIs, 
            InputStream systemIs, 
            InputStream resourceEnvironmentIs,
            InputStream resourceModelExtensionIs,
            InputStream functionality2TiersIs) {
        
        Constraints constraints;
        MonitoringRules rules;
        Repository repository;
        Allocation allocation;
        System system;
        ResourceEnvironment resourceEnvironment;
        ResourceModelExtension resourceModelExtension;
        
        try {
            constraints = Utils.load(Constraints.class, constraintsIs);
            rules = Utils.load(MonitoringRules.class, rulesIs);
            repository = Utils.load(Repository.class, repositoryIs);
            allocation = Utils.load(Allocation.class, allocationIs);
            system = Utils.load(System.class, systemIs);
            resourceEnvironment = Utils.load(ResourceEnvironment.class, resourceEnvironmentIs);
            resourceModelExtension = Utils.load(ResourceModelExtension.class, resourceModelExtensionIs);
        } catch (JAXBException e) {
            throw new MediatorException(e.getMessage(), e);
        }

        Model model = new Model(repository, system, allocation, resourceEnvironment, resourceModelExtension);

        Agreement high = runHigh(constraints, rules, model);
        
        Map<String, Agreement> low = runLow(high, constraints, rules, model);

        Map<String, String> lowIds = buildLowerIds(low);
        return new Result(high.getAgreementId(), lowIds, model);
    }
    
    private Agreement runHigh(Constraints constraints, MonitoringRules rules, Model model) {
        
        Template template = generator.generateTemplate(constraints, rules, model);
        logEntity("Generated high template: {}", template);

        AgreementGenerator agreementer = new AgreementGenerator(template, ctx);
        Agreement agreement = agreementer.generateAgreement();
        logEntity("Generated high agreement: {}", agreement);
        
        if (persist) {
            Provider p = loadProvider(ctx.getProvider());
            if (p == PROVIDER_NOT_FOUND) {
                storeProvider(ctx.getProvider());
            }
            storeTemplate(template);
            storeAgreement(agreement);
        }
        return agreement;
    }
    
    private Map<String, Agreement> runLow(
            Agreement highAgreement, Constraints constraints, MonitoringRules rules, Model model) {
        ContextInfo highCtx = this.ctx;
        
        Map<String, Template> templates = new HashMap<String, Template>();
        Map<String, Agreement> agreements = new HashMap<String, Agreement>();
        TierTemplateGenerator templater = new TierTemplateGenerator(highCtx, highAgreement);
        
        for (ResourceContainer tier : model.getResourceContainers()) {
            String tierName = tier.getId();
            ContextInfo lowCtx = buildLowContext(highCtx, model, tierName);
            Template t = templater.generateTemplate(constraints, rules, model, tierName, lowCtx);
            logEntity("Generated low template: {}", t);
            
            AgreementGenerator agreementer = new AgreementGenerator(t, lowCtx, highAgreement.getAgreementId());
            Agreement a = agreementer.generateAgreement();
            logEntity("Generated low agreement: {}", a);
            
            templates.put(tierName, t);
            agreements.put(tierName, a);
        }
        if (persist) {
            /* 
             * TODO
             * Read model and find cloud providers: store providers
             */
            for (ResourceContainer tier : model.getResourceContainers()) {
                String tierId = tier.getId();
                Template t = templates.get(tierId);
                Agreement a = agreements.get(tierId);

                storeTemplate(t);
                storeAgreement(a);
            }
        }

        return agreements;
    }

    private ContextInfo buildLowContext(ContextInfo highCtx, Model model, String tierId) {
        
        ResourceContainerWrapper wrapper = 
                (ResourceContainerWrapper) model.getResourceModelExtension().getElementById(tierId);
        it.polimi.modaclouds.qos_models.schema.ResourceContainer container = wrapper.getWrapped();
        
        String provider = container.getProvider() != null? container.getProvider() : "[unknown]";
        String consumer = highCtx.getProvider();
        String service = container.getCloudElement() != null? 
                container.getCloudElement().getServiceName() : "";
        ContextInfo result = new ContextInfo(provider, consumer, service, highCtx.getValidity());
        
        return result;
    }
    
    private Map<String, String> buildLowerIds(Map<String, Agreement> map) {
        Map<String, String> result = new HashMap<String, String>();
        
        for (String key : map.keySet()) {
            Agreement item = map.get(key);
            result.put(key, item.getAgreementId());
        }
        return result;
    }
    
    /**
     * Generate and post a template. 
     * 
     * If the provider does not exists, the provider is also created in core.
     *  
     * @param constraintsIs InputStream where to obtain the constraints xml.
     * @param rulesIs InputStream where to obtain the monitoring rules xml.
     * @param repositoryIs InputStream where to obtain the repository.xml (Palladio model)
     * @return TemplateId
     */
    @Deprecated
    public String runTemplate(InputStream constraintsIs, InputStream rulesIs, InputStream repositoryIs,
            InputStream allocationIs, InputStream systemIs, InputStream resourceEnvironmentIs) {
        
        Provider p = loadProvider(ctx.getProvider());
        if (p == PROVIDER_NOT_FOUND) {
            storeProvider(ctx.getProvider());
        }
        
        Template t = generateTemplate(
                constraintsIs, rulesIs, repositoryIs, allocationIs, systemIs, resourceEnvironmentIs);
        logEntity("Generated template: {}", t);
        
        storeTemplate(t);
        return t.getTemplateId();
    }
    
    /**
     * Generate and post an agreement.
     * 
     * @param templateId The agreement will be built from this template.
     * @return AgreementId
     */
    @Deprecated
    public String runAgreement(String templateId) {
        
        Template template = loadTemplate(templateId);
        logEntity("Loaded template is {}", template);
        
        Agreement agreement = generateAgreement(template);
        logEntity("Generated agreement: {}", agreement);

        storeAgreement(agreement);
        return agreement.getAgreementId();
    }
    
    /**
     * Generate a template based on the PCM, the defined constraints, and the monitoring rules.
     * 
     * @see #runTemplate(InputStream, InputStream, InputStream)
     */
    @Deprecated
    public Template generateTemplate(InputStream constraintsIs, InputStream rulesIs, InputStream repositoryIs,
            InputStream allocationIs, InputStream systemIs, InputStream resourceEnvironmentIs) {
        Constraints constraints;
        MonitoringRules rules;
        Repository repository;
        Allocation allocation;
        eu.modaclouds.sla.mediator.model.palladio.system.System system;
        ResourceEnvironment resourceEnvironment;
        
        try {
            constraints = Utils.load(Constraints.class, constraintsIs);
            rules = Utils.load(MonitoringRules.class, rulesIs);
            repository = Utils.load(Repository.class, repositoryIs);
            allocation = Utils.load(Allocation.class, allocationIs);
            system = Utils.load(eu.modaclouds.sla.mediator.model.palladio.system.System.class, systemIs);
            resourceEnvironment = Utils.load(ResourceEnvironment.class, resourceEnvironmentIs);
        } catch (JAXBException e) {
            throw new MediatorException(e.getMessage(), e);
        }
        
        Model model = new Model(repository, system, allocation, resourceEnvironment, null);
        
        Template template = generator.generateTemplate(constraints, rules, model);

        return template;
    }

    /**
     * Generate an agreement based on a template.
     * 
     * @return POJO Agreement
     * 
     * @see #runAgreement(String)
     */
    @Deprecated
    private Agreement generateAgreement(String templateId) {
        Template template = loadTemplate(templateId);
        
        Agreement agreement = generateAgreement(template);
        return agreement;
    }
    
    /**
     * Generate an agreement based on a template.
     * 
     * @return POJO Agreement
     * 
     * @see #runAgreement(String)
     */
    @Deprecated
    public Agreement generateAgreement(Template template) {

        AgreementGenerator generator = new AgreementGenerator(template, ctx);
        Agreement result = generator.generateAgreement();
        
        return result;
    }
    
    /**
     * Store the template in the sla core.
     */
    private void storeTemplate(Template template) {
        client.templates().create(template);
    }
    
    /**
     * Store the agreement in the sla core.
     */
    private void storeAgreement(Agreement agreement) {
        client.agreements().create(agreement);
    }
    /**
     * Load template from sla core.
     * @return POJO Template
     */
    private Template loadTemplate(String templateId) {
        return client.templates().getById(templateId);
    }
    
    /**
     * Load agreement from sla core.
     * @return POJO Agreement.
     */
    private Agreement loadAgreement(String agreementId) {
        return client.agreements().getById(agreementId);
    }

    /**
     * Load provider from the sla core.
     */
    private Provider loadProvider(String provider) {
        
        try {
            return client.providers().getById(provider);
        } catch (SlaException e) {
            if (e.getStatus() == 404) {
                return PROVIDER_NOT_FOUND;
            }
            throw e;
        }
    }
    
    /**
     * Store a provider in the sla core.
     */
    private void storeProvider(String provider) {
        Provider p = new Provider(0L, provider, provider);
        client.providers().create(p);
    }

    /**
     * Logs with debug level a jaxb entity.
     * 
     * @param msg slf4j format string with one pair of {} where the entity should be inserted.
     * @param t entity to be logged.
     */
    private static <E> void logEntity(String msg, E e) {
        try {
            logger.debug(msg, Utils.toString(e));
        } catch (JAXBException e1) {
            /* does nothing. It shouldn't fail */
            logger.warn("JAXBException printing {}", e.getClass().getName());
        }
    }

    
    public static void main(String[] args) throws FileNotFoundException {
        final Cli<Arguments> cli = CliFactory.createCli(Arguments.class);
        
        try {
            String output = "";

            Arguments parsedArgs = cli.parseArguments(args);
            java.lang.System.err.println(
                    String.format("dir=%s; url=%s", parsedArgs.getDirectory(), parsedArgs.getSlaCoreUrl()));
            
            String dir = parsedArgs.getDirectory();
            InputStream constraintsIs = Utils.getInputStream(dir, parsedArgs.getConstraints());
            InputStream rulesIs = Utils.getInputStream(dir, parsedArgs.getRules());
            InputStream repositoryIs = Utils.getInputStream(dir, parsedArgs.getPrefix() + ".repository");
            InputStream allocationIs = Utils.getInputStream(dir, parsedArgs.getPrefix() + ".allocation");
            InputStream systemIs = Utils.getInputStream(dir, parsedArgs.getPrefix() + ".system");
            InputStream resourceEnvironmentIs = 
                    Utils.getInputStream(dir, parsedArgs.getPrefix() + ".resourceenvironment");
            
            String[] credentials = Utils.splitCredentials(parsedArgs.getCredentials());
            ContextInfo ctx = new ContextInfo(
                    parsedArgs.getProvider(), 
                    parsedArgs.getConsumer(), 
                    parsedArgs.getService(), 
                    parsedArgs.getDuration());
            Factory factory = new Factory(parsedArgs.getSlaCoreUrl(), credentials[0], credentials[1]);
            Creator mediator = factory.getCreator(ctx);
            
            String templateId = mediator.runTemplate(
                    constraintsIs, rulesIs, repositoryIs, allocationIs, systemIs, resourceEnvironmentIs);
            output = templateId;
            if (!"".equals(parsedArgs.getConsumer())) {
                
                String agreementId = mediator.runAgreement(templateId);
                output = agreementId;
                Agreement agreement = mediator.loadAgreement(agreementId);
                logEntity("Loaded agreement: {}", agreement);
            }
            
            java.lang.System.out.print(output);
            
        } catch (ArgumentValidationException e) {
            java.lang.System.err.print(cli.getHelpMessage());
            java.lang.System.exit(2);
        }
    }
    
    public interface Arguments {
        
        @Option(shortName="d", longName="dir", description="Directory where to find xml files")
        String getDirectory();
        
        @Option(longName="url", description="Sla Core Url")
        String getSlaCoreUrl();
        
        @Option(shortName="u", longName="user", description="Sla Credentials (user:password)")
        String getCredentials();
        
        @Option(shortName="p", longName="provider", description="Provider name")
        String getProvider();
        
        @Option(shortName="c", longName="consumer", description="Consumer identifier", defaultValue="")
        String getConsumer();
        
        @Option(shortName="s", longName="service", description="Service Name. Default to 'modaclouds'", 
                defaultValue="modaclouds")
        String getService();
        
        @Option(longName="duration", description="xs:duration with the agreement validity. Default to 'P1Y'",
                defaultValue="P1Y")
        String getDuration();
        
        @Option(longName="prefix", defaultValue="default", 
                description="PMC filenames have 'prefix.kind' format. This options specifies the prefix. "
                        + "Default to 'default'" )
        String getPrefix();
        
        @Option(longName="rules", defaultValue="rules.xml",
                description="Filename of xml containing the monitoring rules. Default to 'rules.xml'")
        String getRules();

        @Option(longName="constraints", defaultValue="constraint.xml", 
                description="Filename of xml containing constraints. Default to 'constraint.xml'")
        String getConstraints();
    }
    

}
