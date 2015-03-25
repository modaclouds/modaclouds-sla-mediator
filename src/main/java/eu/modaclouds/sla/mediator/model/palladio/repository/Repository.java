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

import eu.modaclouds.sla.mediator.model.palladio.IDocument;
import eu.modaclouds.sla.mediator.model.palladio.IReferrable;
import eu.modaclouds.sla.mediator.model.palladio.Referrable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Repository")
public class Repository extends Referrable implements IReferrable {
    
    private static final List<Component> EMPTY_COMPONENTS = Collections.<Component>emptyList();
    private static final List<Interface> EMPTY_INTERFACES = Collections.<Interface>emptyList();

    @XmlElement(name="components__Repository")
    private List<Component> components;

    @XmlElement(name="interfaces__Repository")
    private List<Interface> interfaces;
    
    public List<Component> getComponents() {
        return components != null? components : EMPTY_COMPONENTS;
    }

    public List<Interface> getInterfaces() {
        return interfaces != null? interfaces : EMPTY_INTERFACES;
    }
    
    @Override
    public String toString() {
        return String.format("Repository [components=%s, interfaces=%s]", components, interfaces);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Component extends Referrable implements IReferrable {

        public static final Component NOT_FOUND = new Component();
        
        private static final List<SeffSpecification> EMPTY_SEFF_SPECIFICATIONS = 
                Collections.<SeffSpecification>emptyList();

        @XmlElement(name="serviceEffectSpecifications__BasicComponent")
        private List<SeffSpecification> seffBasicComponents;

        private Repository parent;
        
        public List<SeffSpecification> getSeffSpecifications() {
            return seffBasicComponents != null? seffBasicComponents : EMPTY_SEFF_SPECIFICATIONS;
        }
        
        public Repository getParent() {
            return parent;
        }
        
        @Override
        public String toString() {
            return String.format("Component [id=%s, entityName=%s]", 
                    getId(),
                    getEntityName());
        }
        
        public void afterUnmarshal(Unmarshaller u, Object parent) {
            this.parent = (Repository)parent;
        }
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SeffSpecification extends Referrable implements IReferrable {

        @XmlAttribute(name="describedService__SEFF")
        private String describedService;
        
        private Component parent;
        
        public Component getParent() {
            return parent;
        }
        
        @Override
        public String toString() {
            return String.format("SeffBasicComponent [id=%s]", getId());
        }
        
        public Operation getOperation(IDocument<Repository> doc) {
            return (Operation) doc.getElementById(describedService);
        }

        public void afterUnmarshal(Unmarshaller u, Object parent) {
            this.parent = (Component)parent;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Interface extends Referrable implements IReferrable {
        
        @XmlElement(name="signatures__OperationInterface")
        private List<Operation> operations;

        private Repository parent;
        
        public List<Operation> getOperations() {
            return operations;
        }

        public Repository getParent() {
            return parent;
        }

        @Override
        public String toString() {
            return String.format("Interface [id=%s, entityName=%s]", 
                    getId(),
                    getEntityName());
        }
        
        public void afterUnmarshal(Unmarshaller u, Object parent) {
            this.parent = (Repository)parent;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Operation extends Referrable implements IReferrable {
        
        private Interface parent;
        
        public Interface getParent() {
            return parent;
        }

        @Override
        public String toString() {
            return String.format("Operation [id=%s, entityName=%s]", 
                    getId(),
                    getEntityName());
        }
        
        public void afterUnmarshal(Unmarshaller u, Object parent) {
            this.parent = (Interface)parent;
        }
    }
}
