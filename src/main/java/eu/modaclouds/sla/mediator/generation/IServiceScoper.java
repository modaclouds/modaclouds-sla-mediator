package eu.modaclouds.sla.mediator.generation;

import it.polimi.modaclouds.qos_models.schema.Constraint;
import eu.atos.sla.parser.data.wsag.ServiceScope;
import eu.modaclouds.sla.mediator.model.palladio.IDocument;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository;

public interface IServiceScoper {
    ServiceScope generate(Constraint constraint, IDocument<Repository> document);
    
    ServiceScope NOT_FOUND = new ServiceScope();
}