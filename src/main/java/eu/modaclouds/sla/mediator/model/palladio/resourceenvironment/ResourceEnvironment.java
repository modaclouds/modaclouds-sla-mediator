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
