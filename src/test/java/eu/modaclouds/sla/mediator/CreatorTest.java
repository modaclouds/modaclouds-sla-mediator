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

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import eu.modaclouds.sla.mediator.ContextInfo.Validity;
import eu.modaclouds.sla.mediator.model.palladio.resourceenvironment.ResourceEnvironment.ResourceContainer;

public class CreatorTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testRunSlaOfbiz() {
        
        ContextInfo ctx = new ContextInfo("modaclouds", "consumer", "ofbiz", new Validity (1, 0, 0));
        SlaCoreConfig cfg = new SlaCoreConfig("", "", "");
        Creator creator = new Creator(cfg, ctx, false);
        
        Creator.Result result = creator.runSla(
                getInputStream("/ofbiz/constraint.xml"), 
                getInputStream("/ofbiz/rules.xml"), 
                getInputStream("/ofbiz/default.repository"), 
                getInputStream("/ofbiz/default.allocation"), 
                getInputStream("/ofbiz/default.system"), 
                getInputStream("/ofbiz/default.resourceenvironment"), 
                getInputStream("/ofbiz/ContainerExtension.xml"), 
                getInputStream("/ofbiz/Functionality2Tier.xml"),
                getInputStream("/ofbiz/rules_s4c.xml"));
        
        assertNotNull(result.getHighId());
        assertEquals(2, result.getLowIds().size());

        for (ResourceContainer container : result.getModel().getResourceContainers()) {
            String containerId = container.getId();
            assertTrue(result.getLowIds().containsKey(containerId));

            String wsagId = result.getLowIds().get(containerId);
            assertNotNull(wsagId);
        }
    }

    @Test
    public final void testRunSlaSimple() {
        ContextInfo ctx = new ContextInfo("modaclouds", "consumer", "simple", new Validity (1, 0, 0));
        SlaCoreConfig cfg = new SlaCoreConfig("", "", "");
        Creator creator = new Creator(cfg, ctx, false);
        
        String root = "/SimpleModel/";
        String prefix = "test";
        Creator.Result result = creator.runSla(
                getInputStream(root + "QoSConstraints.xml"), 
                getInputStream(root + "MonitoringRules.xml"), 
                getInputStream(root + String.format("%s.repository", prefix)), 
                getInputStream(root + String.format("%s.allocation", prefix)), 
                getInputStream(root + String.format("%s.system", prefix)), 
                getInputStream(root + String.format("%s.resourceenvironment", prefix)), 
                getInputStream(root + "ResourceContainerExtension.xml"), 
                getInputStream(root + "Functionality2Tier.xml"),
                getInputStream(root + "MonitoringRules_s4c.xml"));
        
        for (ResourceContainer container : result.getModel().getResourceContainers()) {
            String containerId = container.getId();
            assertTrue(result.getLowIds().containsKey(containerId));

            String wsagId = result.getLowIds().get(containerId);
            assertNotNull(wsagId);
        }
    }
    
    @Test
    public final void testRunSlaEhealth() {
        ContextInfo ctx = new ContextInfo("modaclouds", "consumer", "ehealth", new Validity (1, 0, 0));
        SlaCoreConfig cfg = new SlaCoreConfig("", "", "");
        Creator creator = new Creator(cfg, ctx, false);
        
        String root = "/ehealth/";
        String prefix = "test";
        Creator.Result result = creator.runSla(
                getInputStream(root + "QoSConstraints.xml"), 
                getInputStream(root + "MonitoringRules.xml"), 
                getInputStream(root + String.format("%s.repository", prefix)), 
                getInputStream(root + String.format("%s.allocation", prefix)), 
                getInputStream(root + String.format("%s.system", prefix)), 
                getInputStream(root + String.format("%s.resourceenvironment", prefix)), 
                getInputStream(root + "ResourceContainerExtension.xml"), 
                getInputStream(root + "Functionality2Tier.xml"),
                getInputStream(root + "MonitoringRules_s4c.xml"));
        
        for (ResourceContainer container : result.getModel().getResourceContainers()) {
            String containerId = container.getId();
            assertTrue(result.getLowIds().containsKey(containerId));

            String wsagId = result.getLowIds().get(containerId);
            assertNotNull(wsagId);
        }
    }
    
    private InputStream getInputStream(String path) {
        return this.getClass().getResourceAsStream(path);
    }
}
