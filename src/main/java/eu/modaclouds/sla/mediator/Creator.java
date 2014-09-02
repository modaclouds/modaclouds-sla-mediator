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

import java.io.FileNotFoundException;
import java.io.InputStream;

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
import eu.modaclouds.sla.mediator.model.palladio.RepositoryDocument;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository;

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
    
    private static final Logger logger = LoggerFactory.getLogger(Creator.class);
  
    private static final Provider PROVIDER_NOT_FOUND;
    static {
        PROVIDER_NOT_FOUND = new Provider(0L, "<NOTFOUND>", "<NOTFOUND>");
    }
    
    private String slaCoreUrl;
    private SlaClient client;
    
    private TemplateGenerator generator;
    private ContextInfo ctx;

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
    public String runTemplate(InputStream constraintsIs, InputStream rulesIs, InputStream repositoryIs) {
        
        Provider p = loadProvider(ctx.getProvider());
        if (p == PROVIDER_NOT_FOUND) {
            storeProvider(ctx.getProvider());
        }
        
        Template t = generateTemplate(constraintsIs, rulesIs, repositoryIs);
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
    public Template generateTemplate(InputStream constraintsIs, InputStream rulesIs, InputStream repositoryIs) {
        Constraints constraints;
        MonitoringRules rules;
        Repository repository;
        try {
            constraints = Utils.load(Constraints.class, constraintsIs);
            rules = Utils.load(MonitoringRules.class, rulesIs);
            repository = Utils.load(Repository.class, repositoryIs);
        } catch (JAXBException e) {
            throw new MediatorException(e.getMessage(), e);
        }
        
        RepositoryDocument model = new RepositoryDocument(repository);
        
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

            Arguments parsedArgs = cli.parseArguments(args);
            System.err.println(
                    String.format("dir=%s; url=%s", parsedArgs.getDirectory(), parsedArgs.getSlaCoreUrl()));
            
            String dir = parsedArgs.getDirectory();
            InputStream constraintsIs = Utils.getInputStream(dir, "constraint.xml");
            InputStream rulesIs = Utils.getInputStream(dir, "rules.xml");
            InputStream repositoryIs = Utils.getInputStream(dir, "default.repository");
            
            String[] credentials = Utils.splitCredentials(parsedArgs.getCredentials());
            ContextInfo ctx = new ContextInfo(
                    parsedArgs.getProvider(), 
                    parsedArgs.getConsumer(), 
                    parsedArgs.getService(), 
                    parsedArgs.getDuration());
            Factory factory = new Factory(parsedArgs.getSlaCoreUrl(), credentials[0], credentials[1]);
            Creator mediator = factory.getCreator(ctx);
            
            String templateId = mediator.runTemplate(constraintsIs, rulesIs, repositoryIs);
            
            if (!"".equals(parsedArgs.getConsumer())) {
                
                String agreementId = mediator.runAgreement(templateId);
                Agreement agreement = mediator.loadAgreement(agreementId);
                logEntity("Loaded agreement: {}", agreement);
            }
            
        } catch (ArgumentValidationException e) {
            System.err.print(cli.getHelpMessage());
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
    }
    

}
