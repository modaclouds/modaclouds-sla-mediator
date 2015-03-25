package eu.modaclouds.sla.mediator.model.palladio.allocation;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.modaclouds.sla.mediator.model.palladio.IDocument;
import eu.modaclouds.sla.mediator.model.palladio.IReferrable;
import eu.modaclouds.sla.mediator.model.palladio.Referrable;
import eu.modaclouds.sla.mediator.model.palladio.resourceenvironment.ResourceEnvironment;
import eu.modaclouds.sla.mediator.model.palladio.resourceenvironment.ResourceEnvironment.ResourceContainer;
import eu.modaclouds.sla.mediator.model.palladio.system.System;
import eu.modaclouds.sla.mediator.model.palladio.system.System.AssemblyContext;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Allocation")
public class Allocation extends Referrable implements IReferrable {

    @XmlElement(name="allocationContexts_Allocation")
    private List<AllocationContext> allocationContexts;
    
    public List<AllocationContext> getAllocationContexts() {
        return allocationContexts;
    }
    
    @Override
    public String toString() {
        return String.format(
                "Allocation [id=%s, entityName=%s, allocationContexts=%s]", getId(),
                getEntityName(), allocationContexts);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class AllocationContext extends Referrable implements IReferrable {
        
        @XmlElement(name="resourceContainer_AllocationContext")
        private AllocResourceContainer resourceContainer;
        
        @XmlElement(name="assemblyContext_AllocationContext")
        private AllocAssemblyContext assemblyContext;
        
        private Allocation parent;
        
        public AllocResourceContainer getResourceContainer() {
            return resourceContainer;
        }
        
        public AllocAssemblyContext getAssemblyContext() {
            return assemblyContext;
        }
        
        public AssemblyContext getAssemblyContext(
                IDocument<System> doc) {
            String id = doc.getIdFromHref(assemblyContext.getHref());
            IReferrable e = doc.getElementById(id);
            
            if (e instanceof AssemblyContext) {
                return (AssemblyContext)e;
            }
            if (e == null) {
                return AssemblyContext.NOT_FOUND;
            }
            throw new IllegalStateException(e.getClass() + "[id='" + id + "] should be a System.AssemblyContext");
        }
        
        public ResourceContainer getResourceContainer(IDocument<ResourceEnvironment> doc) {
            
            String id = doc.getIdFromHref(resourceContainer.getHref());
            IReferrable e = doc.getElementById(id);
            if (e instanceof ResourceContainer) {
                return (ResourceContainer)e;
            }
            if (e == null) {
                return ResourceContainer.NOT_FOUND;
            }
            throw new IllegalStateException(
                    e.getClass() + "[id='" + id + "] should be a ResourceEnvironment.ResourceContainer");
        }

        @Override
        public String toString() {
            return String.format("AllocationContext [id=%s, entityName=%s]",
                    getId(), getEntityName());
        }
        
        public void afterUnmarshal(Unmarshaller u, Object parent) {
            this.parent = (Allocation)parent;
        }
        
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class AllocResourceContainer {
        
        @XmlAttribute
        private String href;
        
        private AllocationContext parent;
        
        public String getHref() {
            return href;
        }

        @Override
        public String toString() {
            return String.format("ResourceContainer [href=%s]", href);
        }

        public void afterUnmarshal(Unmarshaller u, Object parent) {
            this.parent = (AllocationContext)parent;
        }
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class AllocAssemblyContext {

        @XmlAttribute
        private String href;
        
        private AllocationContext parent;

        public String getHref() {
            return href;
        }

        @Override
        public String toString() {
            return String.format("AssemblyContext [href=%s]", href);
        }
        
        public void afterUnmarshal(Unmarshaller u, Object parent) {
            this.parent = (AllocationContext)parent;
        }
    }

}