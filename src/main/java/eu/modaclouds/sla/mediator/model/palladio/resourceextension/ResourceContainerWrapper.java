package eu.modaclouds.sla.mediator.model.palladio.resourceextension;

import it.polimi.modaclouds.qos_models.schema.ResourceContainer;
import eu.modaclouds.sla.mediator.model.palladio.IReferrable;

public class ResourceContainerWrapper implements IReferrable {

    private ResourceContainer container;
    
    
    public ResourceContainerWrapper(ResourceContainer resourceContainer) {
        super();
        this.container = resourceContainer;
    }

    @Override
    public String getId() {
        return container.getId();
    }

    @Override
    public String getEntityName() {
        return "";
    }

    public ResourceContainer getWrapped() {
        return this.container;
    }
}
