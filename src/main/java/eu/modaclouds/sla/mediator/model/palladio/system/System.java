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
package eu.modaclouds.sla.mediator.model.palladio.system;

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
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.Component;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "System")
public class System extends Referrable implements IReferrable {

    @XmlElement(name="assemblyContexts__ComposedStructure")
    private List<AssemblyContext> assemblyContexts;
    
    public List<AssemblyContext> getAssemblyContexts() {
        return assemblyContexts;
    }
    
    @Override
    public String toString() {
        return String.format(
                "System [id=%s, entityName=%s, assemblyContexts=%s]", getId(),
                getEntityName(), assemblyContexts);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class AssemblyContext extends Referrable implements IReferrable {
        
        public static AssemblyContext NOT_FOUND = new AssemblyContext();
        
        @XmlElement(name="encapsulatedComponent__AssemblyContext")
        private EncapsulatedComponent encapsulatedComponent;
        
        private System parent;

        public EncapsulatedComponent getEncapsulatedComponent() {
            return encapsulatedComponent;
        }

        public Component getEncapsulatedComponent(IDocument<Repository> doc) {
            if (this == NOT_FOUND) {
                return Component.NOT_FOUND;
            }
            String id = doc.getIdFromHref(encapsulatedComponent.getHref());
            IReferrable e = doc.getElementById(id);
            if (e instanceof Component) {
                return (Component)e;
            }
            throw new IllegalStateException("Element[id='" + id + "] should be a Repository.Component");
        }

        @Override
        public String toString() {
            return String
                    .format("AssemblyContext [id=%s, entityName=%s, encapsulatedComponent=%s]",
                            getId(), getEntityName(), encapsulatedComponent);
        }
        
        public void afterUnmarshal(Unmarshaller u, Object parent) {
            this.parent = (System)parent;
        }
        
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class EncapsulatedComponent {
        
        @XmlAttribute
        private String href;
        
        public String getHref() {
            return href;
        }

        private AssemblyContext parent;
        
        @Override
        public String toString() {
            return String.format("EncapsulatedComponent [href=%s]", href);
        }
        
        public void afterUnmarshal(Unmarshaller u, Object parent) {
            this.parent = (AssemblyContext)parent;
        }
    }

}
