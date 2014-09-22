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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.Cli;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

import eu.atos.sla.client.SlaClient;
import eu.atos.sla.parser.data.wsag.Agreement;

public class Starter {

//    public static class Factory {
//        private final SlaCoreConfig slaCoreConfig;
//        private final String metricsBaseUrl;
//        private final String callbackBaseUrl;
//
//        public Factory(
//                SlaCoreConfig slaCoreConfig,
//                String metricsBaseUrl, 
//                String callbackBaseUrl) {
//
//            this.slaCoreConfig = slaCoreConfig;
//            this.metricsBaseUrl = metricsBaseUrl;
//            this.callbackBaseUrl = callbackBaseUrl;
//        }
//        
//        public Starter getEnforcement(ViolationSubscriber.Factory subscriberFactory) {
//            
//            return new Starter(slaCoreConfig, metricsBaseUrl, callbackBaseUrl, subscriberFactory);
//        }
//    }

    private final SlaClient client;
    private final String metricsBaseUrl;
    private final String callbackBaseUrl;
    private final ViolationSubscriber.Factory subscriberFactory;
    
    public Starter(
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

        try {
            
            Arguments parsedArgs = cli.parseArguments(args);
            Config config = new Config(parsedArgs);
            
            MonitoringRules rules = parsedArgs.getDirectory() == null? 
                    null : 
                    loadRules(parsedArgs.getDirectory());
       
            SlaCoreConfig slaCoreConfig = new SlaCoreConfig(
                    config.getSlaCoreUrl(), config.getSlaCoreUser(), config.getSlaCorePassword());
            String callbackBaseUrl = buildCallbackUrl(config, parsedArgs);
       
            /*
             * Create/inject instances
             */
            
            ViolationSubscriber.Factory vsFactory = new ViolationSubscriber.Factory(
                    config.getMetricsUrl(), callbackBaseUrl);
            
            Starter starter = new Starter(slaCoreConfig, config.getMetricsUrl(), callbackBaseUrl, vsFactory);
            starter.run(parsedArgs.getAgreementId(), rules);
        } catch (ArgumentValidationException e) {
            System.err.print(cli.getHelpMessage());
            System.exit(2);
        } catch (ConfigException e) {
            System.err.print(e.getMessage());
            System.exit(2);
        }
    }

    private static String buildCallbackUrl(Config config, Arguments parsedArgs) {
        return parsedArgs.getCallbackUrl() != null? 
                parsedArgs.getCallbackUrl() : config.getSlaCoreUrl() + "/metrics";
    }

    private static MonitoringRules loadRules(String dir)
            throws FileNotFoundException, JAXBException {
        InputStream rulesIs = Utils.getInputStream(dir, "rules.xml");
        MonitoringRules rules = Utils.load(MonitoringRules.class, rulesIs);
        return rules;
    }

    /**
     * Enum that holds the possible env vars for Starter program.
     * 
     * Env vars are defined in OS as LABEL=VALUE
     */
    public enum EnvArgument {
        SLA_CORE_URL(true),
        SLA_CORE_CREDENTIALS(true),
        MONITORING_METRICS_URL(true);
        
        private boolean mandatory;
        private String label;
        private EnvArgument(boolean mandatory) {
            this.mandatory = mandatory;
            this.label = this.name();
        }
        
        public String getLabel() {
            
            return label;
        }
        
        public boolean isMandatory() {
            
            return mandatory;
        }
        
        /**
         * @param env map where to extract env values
         * @return value of env var or empty string if not defined.
         */
        public String getValue(Map<String, String> env) {
            String val = env.get(this.label);
            if (val == null) {
                val = "";
            }
            return val;
        }
    }
    
    /**
     * This class stores config values for Starter program : sla-core url, sla-core credentials, 
     * monitoring-platform url.
     * 
     * The config values are defined by env vars, and are overriden by command line parameters.
     * 
     * If a value is not defined at constructor time, a ConfigException is thrown.
     *
     */
    public static class Config {
        
        private Map<String, String> env;
        private Arguments args;
        
        private String slaCoreUrl;
        private String slaCoreUser;
        private String slaCorePassword;
        private String metricsUrl;
        
        public Config(Arguments args) {
            this(System.getenv(), args);
        }
        
        /**
         * @param env Env vars
         * @param args Command line parameters
         * @throws ConfigException if any config parameter is empty or not defined 
         */
        public Config(Map<String, String> env, Arguments args) {
            
            this.env = env;
            this.args = args;
            List<String> errors = setValuesAndCheck();
            
            if (errors.size() > 0) {
                throw new ConfigException(errors);
            }
        }
        
        private List<String> setValuesAndCheck() {
            
            slaCoreUrl = getValue(EnvArgument.SLA_CORE_URL.getValue(env), args.getSlaCoreUrl());
            
            String creds = getValue(EnvArgument.SLA_CORE_CREDENTIALS.getValue(env), args.getCredentials());
            String[] credsArray = Utils.splitCredentials(creds);
            slaCoreUser = credsArray[0];
            slaCorePassword = credsArray[1];
            
            metricsUrl = getValue(EnvArgument.MONITORING_METRICS_URL.getValue(env), args.getMetricsUrl());
            
            List<String> errors = new ArrayList<>();
            
            errors = checkVar(EnvArgument.SLA_CORE_URL, slaCoreUrl, errors);
            errors = checkVar(EnvArgument.SLA_CORE_CREDENTIALS, creds, errors);
            errors = checkVar(EnvArgument.MONITORING_METRICS_URL, metricsUrl, errors);
            
            return errors;
        }

        private List<String> checkVar(EnvArgument env, String value, List<String> errors) {
            
            if ("".equals(value) && env.isMandatory()) {
                errors.add("Mandatory argument " + env.getLabel() + " not set.");
            }
            return errors;
        }
        
        private String getValue(String envValue, String paramValue) {
            
            String result = paramValue;
            
            if (result == null || "".equals(result)) {
                
                result = envValue;
            }
            return result;
        }
        
        public String getSlaCoreUrl() {

            return slaCoreUrl;
        }
        
        public String getSlaCoreUser() {

            return slaCoreUser;
        }
        
        public String getSlaCorePassword() {
            
            return slaCorePassword;
        }
        
        public String getMetricsUrl() {
            
            return metricsUrl;
        }
    }

    public static class ConfigException extends MediatorException {
        private static final long serialVersionUID = 1L;
        
        private List<String> errors;
        
        public ConfigException(List<String> errors) {
            super("Not all config values have been set", null);
            this.errors = errors;
        }
        
        public List<String> getErrors() {
            return errors;
        }
    }
    
    public interface Arguments {
        
        /*
         * At deployment time, this directory is maybe not available. If so, think of storing 
         * the monitoringrules in some kind of store, or include the action in the agreement.
         */
        @Option(shortName="d", longName="dir", defaultToNull=true,
                description="Directory where to find xml files. If not specified, tries to retrieve OutputMetric "
                        + "information from agreement")
        String getDirectory();
        
        @Option(longName="sla", description="Sla Core Url", defaultValue="")
        String getSlaCoreUrl();
        
        @Option(shortName="u", longName="user", description="Sla Credentials (user:password)", defaultValue="")
        String getCredentials();
        
        @Option(longName="id", description="Agreement Id")
        String getAgreementId();
        
        @Option(longName="metrics", description="Monitoring Platform Metrics Url", defaultValue="")
        String getMetricsUrl();
        
        @Option(longName="callback", 
                description="Callback Url. If not set, fallback to $sla/metrics", 
                defaultToNull=true)
        String getCallbackUrl();
    }
    
}
