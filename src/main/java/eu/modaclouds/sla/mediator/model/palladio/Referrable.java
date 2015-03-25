package eu.modaclouds.sla.mediator.model.palladio;

import javax.xml.bind.annotation.XmlAttribute;

public abstract class Referrable implements IReferrable {
    @XmlAttribute
    private String id;
    
    @XmlAttribute
    private String entityName;
    
    @Override
    public final String getId() {
        return id != null? id : "";
    }

    @Override
    public final String getEntityName() {
        return entityName != null? entityName : "";
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Referrable other = (Referrable) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    
}
