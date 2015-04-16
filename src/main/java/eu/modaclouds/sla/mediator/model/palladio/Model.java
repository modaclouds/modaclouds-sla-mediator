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
package eu.modaclouds.sla.mediator.model.palladio;

import it.polimi.modaclouds.qos_models.schema.ResourceModelExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.modaclouds.sla.mediator.model.palladio.Document.IMapInitializer;
import eu.modaclouds.sla.mediator.model.palladio.allocation.Allocation;
import eu.modaclouds.sla.mediator.model.palladio.allocation.Allocation.AllocationContext;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.Component;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.Interface;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.Operation;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.SeffSpecification;
import eu.modaclouds.sla.mediator.model.palladio.resourceenvironment.ResourceEnvironment;
import eu.modaclouds.sla.mediator.model.palladio.resourceenvironment.ResourceEnvironment.ResourceContainer;
import eu.modaclouds.sla.mediator.model.palladio.resourceextension.ResourceContainerWrapper;
import eu.modaclouds.sla.mediator.model.palladio.system.System;
import eu.modaclouds.sla.mediator.model.palladio.system.System.AssemblyContext;

/**
 * Facade to access the model defined by the palladio files and modaclouds extensions.
 */
public class Model {

    private IDocument<Repository> repository;
    private IDocument<System> system;
    private IDocument<Allocation> allocation;
    private IDocument<ResourceEnvironment> resourceEnvironment;
    private IDocument<ResourceModelExtension> resourceModelExtension;
    
    public Model(
            Repository repository, 
            System system,
            Allocation allocation, 
            ResourceEnvironment resourceEnvironment, 
            ResourceModelExtension resourceModelExtension) {
        
        this.repository = new Document<Repository>(repository, "repository", new RepositoryMapInitializer());
        this.system = new Document<System>(system, "system", new SystemMapInitializer());
        this.allocation = new Document<Allocation>(allocation, "allocation", new AllocationMapInitializer());
        this.resourceEnvironment = new Document<ResourceEnvironment>(
                resourceEnvironment, "resourceenvironment", new ResourceEnvironmentMapInitializer());
        this.resourceModelExtension = new Document<ResourceModelExtension>(
                resourceModelExtension, "resourceModelExtension", new ResourceModelExtensionMapInitializer()); 
    }

    public IDocument<Repository> getRepository() {
    
        return repository;
    }
    
    public IDocument<System> getSystem() {
        return system;
    }
    
    public IDocument<Allocation> getAllocation() {
        return allocation;
    }
    
    public IDocument<ResourceEnvironment> getResourceEnvironment() {
        return resourceEnvironment;
    }
    
    public IDocument<ResourceModelExtension> getResourceModelExtension() {
        return resourceModelExtension;
    }
    
    public List<Component> getComponents() {
        return repository.getJAXBNode().getComponents();
    }
    
    public List<ResourceContainer> getResourceContainers() {
        return resourceEnvironment.getJAXBNode().getResourceContainers();
    }
    
    public List<Component> getComponents(ResourceContainer container) {
        if (container == null) {
            throw new NullPointerException("container cannot be null");
        }
        List<Component> result = new ArrayList<Component>();
        for (AllocationContext allocation : this.allocation.getJAXBNode().getAllocationContexts()) {
            if (container.equals(allocation.getResourceContainer())) {
                Component c = allocation.getAssemblyContext(system).getEncapsulatedComponent(repository);
                result.add(c);
            }
        }
        return result;
    }
    
    public ResourceContainer getResourceContainer(Component component) {
        if (component == null) {
            throw new NullPointerException("component cannot be null");
        }
        for (AllocationContext allocCtx : this.allocation.getJAXBNode().getAllocationContexts()) {
            AssemblyContext assemblyContext = allocCtx.getAssemblyContext(system);
            Component encapsulatedComponent = assemblyContext.getEncapsulatedComponent(repository);
            if (component.equals(encapsulatedComponent)) {
                return allocCtx.getResourceContainer(resourceEnvironment);
            }
        }
        return ResourceContainer.NOT_FOUND;
    }
    
    public ResourceContainer getResourceContainer(String containerId) {
        ResourceContainer result;
        IReferrable ref = this.resourceEnvironment.getElementById(containerId);
        if (ref == IDocument.NOT_FOUND || !(ref instanceof ResourceContainer)) {
            result = ResourceContainer.NOT_FOUND;
        }
        else {
            result = (ResourceContainer) ref;
        }
        return result;
    }
    
    public static class RepositoryMapInitializer implements IMapInitializer<Repository> {

        @Override
        public Map<String, IReferrable> initMap(Repository repository) {
            HashMap<String, IReferrable> map = new HashMap<String, IReferrable>();
            map.put(repository.getId(), repository);
            for (Component comp : repository.getComponents()) {
                map.put(comp.getId(), comp);
                for (SeffSpecification spec : comp.getSeffSpecifications()) {
                    map.put(spec.getId(), spec);
                }
            }
            for (Interface iface : repository.getInterfaces()) {
                map.put(iface.getId(), iface);
                for (Operation operation : iface.getOperations()) {
                    map.put(operation.getId(), operation);
                }
            }
            return map;
        }
    }
    
    public static class SystemMapInitializer implements IMapInitializer<System> {

        @Override
        public Map<String, IReferrable> initMap(System root) {
            HashMap<String, IReferrable> map = new HashMap<String, IReferrable>();
            map.put(root.getId(), root);
            for (AssemblyContext assemblyContext : root.getAssemblyContexts()) {
                map.put(assemblyContext.getId(), assemblyContext);
            }
            return map;
        }
    }
    
    public static class AllocationMapInitializer implements IMapInitializer<Allocation> {

        @Override
        public Map<String, IReferrable> initMap(Allocation root) {
            HashMap<String, IReferrable> map = new HashMap<String, IReferrable>();
            map.put(root.getId(), root);
            for (AllocationContext allocationContext : root.getAllocationContexts()) {
                map.put(allocationContext.getId(), allocationContext);
            }
            return map;
        }
    }
    
    public static class ResourceEnvironmentMapInitializer implements IMapInitializer<ResourceEnvironment> {
    
        @Override
        public Map<String, IReferrable> initMap(ResourceEnvironment root) {
            HashMap<String, IReferrable> map = new HashMap<String, IReferrable>();
            for (ResourceContainer resourceContainer : root.getResourceContainers()) {
                map.put(resourceContainer.getId(), resourceContainer);
            }
            return map;
        }
    }

    public static class ResourceModelExtensionMapInitializer implements IMapInitializer<ResourceModelExtension> {

        @Override
        public Map<String, IReferrable> initMap(ResourceModelExtension root) {
            HashMap<String, IReferrable> map = new HashMap<String, IReferrable>();
            for (it.polimi.modaclouds.qos_models.schema.ResourceContainer container : root.getResourceContainer()) {
                map.put(container.getId(), new ResourceContainerWrapper(container));
            }
            return map;
        }
        
    }
    @Override
    public String toString() {
        return String
                .format("Model [repository=%s, system=%s, allocation=%s, resourceEnvironment=%s]",
                        repository.getJAXBNode(), 
                        system.getJAXBNode(), 
                        allocation.getJAXBNode(), 
                        resourceEnvironment.getJAXBNode(),
                        resourceModelExtension.getJAXBNode());
    }

    
}
