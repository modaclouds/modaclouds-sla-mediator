package eu.modaclouds.sla.mediator.model.palladio.resourceextension;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.modaclouds.sla.mediator.model.palladio.IReferrable;
import eu.modaclouds.sla.mediator.model.palladio.Referrable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "resourceModelExtension")
public class ResourceModelExtension {

    private static final List<ResourceContainer> EMPTY_RESOURCE_CONTAINERS = 
            Collections.<ResourceContainer>emptyList();
    
    @XmlElement(name="resourceContainer")
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

        @XmlAttribute
        private String name;
        
        @XmlAttribute
        private String provider;

        @XmlElement
        private List<CloudResource> cloudResources;
        
        private ResourceModelExtension parent;
        
        public String getProvider() {
            return provider;
        }
        
        public List<CloudResource> getCloudResources() {
            return cloudResources;
        }
        
        
        @Override
        public String toString() {
            return String
                    .format("ResourceContainer [id=%s, name=%s, provider=%s, cloudResources=%s]",
                            getId(), name, provider, cloudResources);
        }

        public void afterUnmarshal(Unmarshaller u, Object parent) {
            this.parent = (ResourceModelExtension)parent;
        }
    }
    
    public static class CloudResource {
        
        @XmlAttribute
        private String serviceName;
        
        @XmlAttribute
        private String serviceType;
        
        private ResourceContainer parent;

        public String getServiceName() {
            return serviceName;
        }
        
        public String getServiceType() {
            return serviceType;
        }
        
        
        @Override
        public String toString() {
            return String.format(
                    "CloudResource [serviceName=%s, serviceType=%s]",
                    serviceName, serviceType);
        }

        public void afterUnmarshal(Unmarshaller u, Object parent) {
            this.parent = (ResourceContainer)parent;
        }
    }
}
