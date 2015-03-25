package eu.modaclouds.sla.mediator.model.palladio.resourceenvironment;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.modaclouds.sla.mediator.model.palladio.IReferrable;
import eu.modaclouds.sla.mediator.model.palladio.Referrable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ResourceEnvironment")
public class ResourceEnvironment  {

    private static final List<ResourceContainer> EMPTY_RESOURCE_CONTAINERS = 
            Collections.<ResourceContainer>emptyList();
    
    @XmlElement(name="resourceContainer_ResourceEnvironment")
    private List<ResourceContainer> resourceContainers;
    
    public List<ResourceContainer> getResourceContainers() {
        return resourceContainers != null? resourceContainers : EMPTY_RESOURCE_CONTAINERS;
    }

    @Override
    public String toString() {
        return String.format(
                "ResourceEnvironment [resourceContainers=%s]",
                resourceContainers);
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ResourceContainer extends Referrable implements IReferrable {
        public static ResourceContainer NOT_FOUND = new ResourceContainer();
        
        private ResourceEnvironment parent;
        
        @Override
        public String toString() {
            return String.format("ResourceContainer [id=%s, entityName=%s]",
                    getId(), getEntityName());
        }
        
        public void afterUnmarshal(Unmarshaller u, Object parent) {
            this.parent = (ResourceEnvironment)parent;
        }
        
    }

}
