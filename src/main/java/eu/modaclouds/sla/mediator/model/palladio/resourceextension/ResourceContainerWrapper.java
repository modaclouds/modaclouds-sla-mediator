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
