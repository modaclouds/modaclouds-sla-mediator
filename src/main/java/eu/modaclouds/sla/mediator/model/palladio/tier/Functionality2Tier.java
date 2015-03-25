package eu.modaclouds.sla.mediator.model.palladio.tier;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
