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
package eu.modaclouds.sla.mediator.model.palladio.tier;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.modaclouds.sla.mediator.model.palladio.IReferrable;
import eu.modaclouds.sla.mediator.model.palladio.Referrable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "functionality2Tier")
public class Functionality2Tier extends Referrable implements IReferrable {

    @XmlElement(name="tier")
    private List<Tier> tiers;

    public List<Tier> getTiers() {
        return tiers;
    }
    
    @Override
    public String toString() {
        return String.format("Functionality2Tier [id=%s, tiers=%s]", getId(), tiers);
    }

    public static class Tier extends Referrable implements IReferrable {
        
        @XmlElement(name="functionality")
        private List<Functionality> functionalities;

        public List<Functionality> getFunctionalities() {
            return functionalities;
        }

        public void setFunctionalities(List<Functionality> functionalities) {
            this.functionalities = functionalities;
        }

        @Override
        public String toString() {
            return String.format("Tier [id=%s, functionalities=%s]", getId(),
                    functionalities);
        }
    }
    
    public static class Functionality extends Referrable implements IReferrable {
        
        @Override
        public String toString() {
            return String.format("Functionality [id=%s]", getId());
        }
    }
}
