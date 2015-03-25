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
package eu.modaclouds.sla.mediator.model;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import eu.modaclouds.sla.mediator.Utils;
import eu.modaclouds.sla.mediator.model.palladio.Model;
import eu.modaclouds.sla.mediator.model.palladio.allocation.Allocation;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.Component;
import eu.modaclouds.sla.mediator.model.palladio.resourceenvironment.ResourceEnvironment;
import eu.modaclouds.sla.mediator.model.palladio.resourceenvironment.ResourceEnvironment.ResourceContainer;
import eu.modaclouds.sla.mediator.model.palladio.resourceextension.ResourceModelExtension;
import eu.modaclouds.sla.mediator.model.palladio.system.System;

public class ModelTest {
    Repository repository;
    System system;
    Allocation allocation;
    ResourceEnvironment resourceEnvironment;
    it.polimi.modaclouds.qos_models.schema.ResourceModelExtension resourceModelExtension;
    Model model;

    @Before
    public void setUp() throws Exception {
        repository = Utils.load(Repository.class, getInputStream("/ofbiz/default.repository"));
        system = Utils.load(System.class, getInputStream("/ofbiz/default.system"));
        allocation = Utils.load(Allocation.class, getInputStream("/ofbiz/default.allocation"));
        resourceEnvironment = Utils.load(ResourceEnvironment.class, 
                getInputStream("/ofbiz/default.resourceenvironment"));
        resourceModelExtension = Utils.load(it.polimi.modaclouds.qos_models.schema.ResourceModelExtension.class, 
                getInputStream("/ofbiz/ContainerExtension.xml"));
        model = new Model(repository, system, allocation, resourceEnvironment, resourceModelExtension);
    }

    @Test
    public final void testModel() {
        assertNotNull(model);
        java.lang.System.out.println(model);
    }
    
    @Test
    public void testGetComponents() {
        List<Component> components = model.getComponents();
        assertEquals(3, components.size());
    }
    
    @Test
    public void testGetTiers() {
        List<ResourceContainer> containers = model.getResourceContainers();
        assertEquals(2, containers.size());
    }
    
    @Test
    public void testGetTier() {
        Component requestHandler = (Component)model.getRepository().getElementById("__o5G4MhnEeKON4DtRoKCMw");
        Component db = (Component)model.getRepository().getElementById("_kwoOgMhoEeKON4DtRoKCMw");
        Component paymentService = (Component)model.getRepository().getElementById("_kwoOgMhoEeKON4DtRoKCMw");
        ResourceContainer frontend = 
                (ResourceContainer)model.getResourceEnvironment().getElementById("_-sJ1AMhrEeKON4DtRoKCMw");
        ResourceContainer backend = 
                (ResourceContainer)model.getResourceEnvironment().getElementById("_ervoQKhyEeOVLLp4qCj_jg");
        
        assertEquals(frontend, model.getResourceContainer(requestHandler));
        assertEquals(backend, model.getResourceContainer(db));
        assertEquals(backend, model.getResourceContainer(paymentService));
    }

    private InputStream getInputStream(String path) {
        return this.getClass().getResourceAsStream(path);
    }
}
