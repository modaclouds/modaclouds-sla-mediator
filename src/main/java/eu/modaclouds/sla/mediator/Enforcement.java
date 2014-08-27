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

import it.polimi.modaclouds.qos_models.schema.MonitoringRules;

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import com.lexicalscope.jewel.cli.Cli;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

import eu.atos.sla.client.SlaClient;
import eu.atos.sla.parser.data.wsag.Agreement;

public class Enforcement {

    public static class Factory {
        private final SlaCoreConfig slaCoreConfig;
        private final String metricsBaseUrl;
        private final String callbackBaseUrl;

        public Factory(
                SlaCoreConfig slaCoreConfig,
                String metricsBaseUrl, 
                String callbackBaseUrl) {

            this.slaCoreConfig = slaCoreConfig;
            this.metricsBaseUrl = metricsBaseUrl;
            this.callbackBaseUrl = callbackBaseUrl;
        }
        
        public Enforcement getEnforcement(ViolationSubscriber.Factory subscriberFactory) {
            
            return new Enforcement(slaCoreConfig, metricsBaseUrl, callbackBaseUrl, subscriberFactory);
        }
    }

    private final SlaClient client;
    private final String metricsBaseUrl;
    private final String callbackBaseUrl;
    private final ViolationSubscriber.Factory subscriberFactory;
    
    public Enforcement(
            SlaCoreConfig slaCoreConfig, 
            String metricsBaseUrl, 
            String callbackBaseUrl, 
            ViolationSubscriber.Factory subscriberFactory) {
        
        this.client = new SlaClient(
                slaCoreConfig.getSlaCoreUrl(), 
                MediaType.APPLICATION_XML_TYPE, 
                slaCoreConfig.getSlaCoreUser(), 
                slaCoreConfig.getSlaCorePassword()
        );
        this.metricsBaseUrl = metricsBaseUrl;
        this.callbackBaseUrl = callbackBaseUrl;
        this.subscriberFactory = subscriberFactory;
    }

    public void run(String agreementId, MonitoringRules rules) {
        
        Agreement agreement = client.agreements().getById(agreementId);
        
        try {
            Utils.print(agreement, System.err);
        } catch (JAXBException e) {
            /* does nothing */
        }
        
        this.client.enforcements().start(agreementId);
        ViolationSubscriber subscriber = subscriberFactory.getSubscriber(agreement, rules);
        subscriber.subscribeObserver(agreement, rules);
    }
    
    public static void main(String[] args) throws FileNotFoundException, JAXBException {
        final Cli<Arguments> cli = CliFactory.createCli(Arguments.class);

        Arguments parsedArgs = cli.parseArguments(args);

        MonitoringRules rules = loadRules(parsedArgs.getDirectory());

        String[] credentials = Utils.splitCredentials(parsedArgs.getCredentials());
        SlaCoreConfig slaCoreConfig = new SlaCoreConfig(parsedArgs.getSlaCoreUrl(), credentials[0], credentials[1]);
        String callbackBaseUrl = buildCallbackUrl(parsedArgs);

        /*
         * Create/inject instances
         */
        
        ViolationSubscriber.Factory vsFactory = new ViolationSubscriber.Factory(
                parsedArgs.getMetricsUrl(), callbackBaseUrl);
        
        Enforcement.Factory eFactory = new Enforcement.Factory(
                slaCoreConfig, parsedArgs.getMetricsUrl(), callbackBaseUrl);
        
        Enforcement enforcement = eFactory.getEnforcement(vsFactory);
        
        enforcement.run(parsedArgs.getAgreementId(), rules);
    }

    private static String buildCallbackUrl(Arguments parsedArgs) {
        return parsedArgs.getCallbackUrl() != null? 
                parsedArgs.getCallbackUrl() : parsedArgs.getSlaCoreUrl() + "/metricsReceiver";
    }

    private static MonitoringRules loadRules(String dir)
            throws FileNotFoundException, JAXBException {
        InputStream rulesIs = Utils.getInputStream(dir, "rules.xml");
        MonitoringRules rules = Utils.load(MonitoringRules.class, rulesIs);
        return rules;
    }

    public interface Arguments {
        
        /*
         * At deployment time, this directory is maybe not available. If so, think of storing 
         * the monitoringrules in some kind of store, or include the action in the agreement.
         */
        @Option(shortName="d", longName="dir", description="Directory where to find xml files")
        String getDirectory();
        
        @Option(longName="sla", description="Sla Core Url")
        String getSlaCoreUrl();
        
        @Option(shortName="u", longName="user", description="Sla Credentials (user:password)")
        String getCredentials();
        
        @Option(longName="id", description="Agreement Id")
        String getAgreementId();
        
        @Option(longName="metrics", description="Monitoring Platform Metrics Url")
        String getMetricsUrl();
        
        @Option(longName="callback", 
                description="Callback Url. If not set, fallback to $sla/metricsReceiver", 
                defaultToNull=true)
        String getCallbackUrl();
    }
    
}
