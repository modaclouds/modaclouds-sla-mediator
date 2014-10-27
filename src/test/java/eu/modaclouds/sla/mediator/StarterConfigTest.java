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

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import eu.modaclouds.sla.mediator.Starter.Arguments;
import eu.modaclouds.sla.mediator.Starter.Config;
import eu.modaclouds.sla.mediator.Starter.ConfigException;
import eu.modaclouds.sla.mediator.Starter.EnvArgument;

public class StarterConfigTest {

    private static final String METRICS_URL = "http://localhost:8175/v1/metrics";
    private static final String SLA_CREDENTIALS = "user:password";
    private static final String SLA_CORE_URL = "http://localhost:8080/sla-service";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testConfigAllEmptyShouldFail() {
        try {
            
            Arguments args = new ArgumentsImpl(null, null, null);
            Map<String, String> env = new EnvMap(null, null, "");
            new Config(env, args);
            fail();
        } catch (ConfigException e) {
            assertEquals(3, e.getErrors().size());
        }
    }
    
    @Test
    public final void testConfigFilledShouldSuccess() {
        Arguments args = new ArgumentsImpl(null, "", null);
        Map<String, String> env = new EnvMap(SLA_CORE_URL, SLA_CREDENTIALS, METRICS_URL);
        new Config(env, args);
    }
    
    @Test
    public final void testConfigOverlappedShouldSuccess() {
        
        String slaCoreUrl = "http://eu.modaclouds.sla:8080";
        String user = "u";
        String pwd = "p";
        String credentials = user + ":" + pwd;
        String metricsUrl = "http://eu.modaclouds.sla:8175/v1";
        
        Arguments args = new ArgumentsImpl(slaCoreUrl, credentials, metricsUrl);
        Map<String, String> env = new EnvMap(SLA_CORE_URL, SLA_CREDENTIALS, METRICS_URL);
        
        Config c = new Config(env, args);
        
        assertEquals(slaCoreUrl, c.getSlaCoreUrl());
        assertEquals(user, c.getSlaCoreUser());
        assertEquals(pwd, c.getSlaCorePassword());
        assertEquals(metricsUrl, c.getMetricsUrl());
    }

    public static class ArgumentsImpl implements Arguments {
        private String slaCoreUrl;
        private String credentials;
        private String metricsUrl;

        public ArgumentsImpl(String slaCoreUrl, String credentials, String metricsUrl) {
            super();
            this.slaCoreUrl = slaCoreUrl;
            this.credentials = credentials;
            this.metricsUrl = metricsUrl;
        }

        public String getSlaCoreUrl() {
            return slaCoreUrl;
        }

        public String getMetricsUrl() {
            return metricsUrl;
        }

        public String getAgreementId() {
            return null;
        }

        public String getCallbackUrl() {
            return null;
        }

        public String getCredentials() {
            return credentials;
        }

        public String getDirectory() {
            return null;
        }
    }
    
    public static class EnvMap extends HashMap<String, String> {
        private static final long serialVersionUID = 1L;

        public EnvMap(String slaCoreUrl, String slaCoreCredentials, String metricsUrl) {
            this.put(EnvArgument.SLA_CORE_URL.getLabel(), slaCoreUrl);
            this.put(EnvArgument.SLA_CORE_CREDENTIALS.getLabel(), slaCoreCredentials);
            this.put(EnvArgument.MONITORING_METRICS_URL.getLabel(), metricsUrl);
        }
    }
}
