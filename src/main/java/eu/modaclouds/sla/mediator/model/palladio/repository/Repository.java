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
package eu.modaclouds.sla.mediator.model.palladio.repository;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.modaclouds.sla.mediator.model.palladio.IReferrable;
import eu.modaclouds.sla.mediator.model.palladio.RepositoryDocument;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Repository")
public class Repository implements IReferrable {
    
    private static final List<Component> EMPTY_COMPONENTS = Collections.<Component>emptyList();
    private static final List<Interface> EMPTY_INTERFACES = Collections.<Interface>emptyList();

    @XmlAttribute
    private String id;
    
    @XmlAttribute
    private String entityName;
    
    @XmlElement(name="components__Repository")
    private List<Component> components;

    @XmlElement(name="interfaces__Repository")
    private List<Interface> interfaces;
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getEntityName() {
        return entityName;
    }

    public List<Component> getComponents() {
        return components != null? components : EMPTY_COMPONENTS;
    }

    public List<Interface> getInterfaces() {
        return interfaces != null? interfaces : EMPTY_INTERFACES;
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Component implements IReferrable {

        private static final List<SeffSpecification> EMPTY_SEFF_SPECIFICATIONS = 
                Collections.<SeffSpecification>emptyList();

        @XmlAttribute
        private String id;
        
        @XmlAttribute
        private String entityName;
        
        @XmlElement(name="serviceEffectSpecifications__BasicComponent")
        private List<SeffSpecification> seffBasicComponents;

        private Repository parent;
        
        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getEntityName() {
            return entityName;
        }

        public List<SeffSpecification> getSeffSpecifications() {
            return seffBasicComponents != null? seffBasicComponents : EMPTY_SEFF_SPECIFICATIONS;
        }
        
        public Repository getParent() {
            return parent;
        }
        
        @Override
        public String toString() {
            return String.format("Component [id=%s, entityName=%s]", 
                    id,
                    entityName);
        }
        
        public void afterUnmarshal(Unmarshaller u, Object parent) {
            this.parent = (Repository)parent;
        }
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SeffSpecification implements IReferrable {

        @XmlAttribute
        private String id;

        @XmlAttribute(name="describedService__SEFF")
        private String describedService;
        
        private Component parent;
        
        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getEntityName() {
            return "";
        }

        public Component getParent() {
            return parent;
        }
        
        @Override
        public String toString() {
            return String.format("SeffBasicComponent [id=%s]", id);
        }
        
        public Operation getOperation(RepositoryDocument doc) {
            return (Operation) doc.getElementById(describedService);
        }

        public void afterUnmarshal(Unmarshaller u, Object parent) {
            this.parent = (Component)parent;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Interface implements IReferrable {
        
        @XmlAttribute
        private String id;
        
        @XmlAttribute
        private String entityName;
        
        @XmlElement(name="signatures__OperationInterface")
        private List<Operation> operations;

        private Repository parent;
        
        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getEntityName() {
            return entityName;
        }

        public List<Operation> getOperations() {
            return operations;
        }

        public Repository getParent() {
            return parent;
        }

        @Override
        public String toString() {
            return String.format("Interface [id=%s, entityName=%s]", 
                    id,
                    entityName);
        }
        
        public void afterUnmarshal(Unmarshaller u, Object parent) {
            this.parent = (Repository)parent;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Operation implements IReferrable {
        
        @XmlAttribute
        private String id;
        
        @XmlAttribute
        private String entityName;
        
        private Interface parent;
        
        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getEntityName() {
            return entityName;
        }

        public Interface getParent() {
            return parent;
        }

        @Override
        public String toString() {
            return String.format("Operation [id=%s, entityName=%s]", 
                    id,
                    entityName);
        }
        
        public void afterUnmarshal(Unmarshaller u, Object parent) {
            this.parent = (Interface)parent;
        }
    }
}
