package eu.modaclouds.sla.mediator.generation;

import it.polimi.modaclouds.qos_models.schema.Constraint;

import java.util.HashMap;

import eu.atos.sla.parser.data.wsag.ServiceScope;
import eu.modaclouds.sla.mediator.model.constraints.TargetClass;
import eu.modaclouds.sla.mediator.model.palladio.IDocument;
import eu.modaclouds.sla.mediator.model.palladio.IReferrable;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.Component;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.Operation;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.SeffSpecification;

public class ServiceScoper implements IServiceScoper {
    
    static HashMap<TargetClass, IServiceScoper> map = new HashMap<>();
    static IServiceScoper nullServiceScoper = new ServiceScoper.NullServiceScoper();
    
    static {
        map.put(TargetClass.METHOD, new ServiceScoper.MethodServiceScoper());
        map.put(TargetClass.INTERNAL_COMPONENT, new ServiceScoper.InternalComponentServiceScoper());
    }
    
    @Override
    public ServiceScope generate(Constraint constraint,
            IDocument<Repository> document) {
        
        TargetClass target = TargetClass.fromString(constraint.getTargetClass());

        IServiceScoper scoper = map.containsKey(target)? map.get(target) : nullServiceScoper;
        
        ServiceScope scope = scoper.generate(constraint, document);
        return scope;
    }
    
    private static class NullServiceScoper implements IServiceScoper {
        @Override
        public ServiceScope generate(Constraint constraint,
                IDocument<Repository> document) {
            return NOT_FOUND;
        }
    }

    private static class MethodServiceScoper implements IServiceScoper {
        
        @Override
        public ServiceScope generate(Constraint constraint,
                IDocument<Repository> document) {
            
            IReferrable referrable = 
                    document.getElementById(constraint.getTargetResourceIDRef());
            
            ServiceScope result = NOT_FOUND;
            
            if (referrable != IDocument.NOT_FOUND) {
                if (referrable instanceof SeffSpecification) {
                    result = process(constraint, document, (SeffSpecification) referrable);
                }
            }
            /*
             * process errors
             */
            return result;
        }
        
        private ServiceScope process(Constraint constraint, IDocument<Repository> document, SeffSpecification element) {
            
            Component parent = element.getParent();
            Operation operation = element.getOperation(document);
            ServiceScope result = new ServiceScope();
            result.setServiceName(TemplateGenerator.DEFAULT_SERVICE_NAME);
            result.setValue(String.format("%s/%s", parent.getEntityName(), operation.getEntityName()));
            
            return result;
        }
    }

    private static class InternalComponentServiceScoper implements IServiceScoper {
    
        @Override
        public ServiceScope generate(Constraint constraint,
                IDocument<Repository> document) {
            
            IReferrable referrable = 
                    document.getElementById(constraint.getTargetResourceIDRef());
            
            ServiceScope result = NOT_FOUND;
            
            if (referrable != IDocument.NOT_FOUND) {
                if (referrable instanceof Component) {
                    result.setServiceName(TemplateGenerator.DEFAULT_SERVICE_NAME);
                    result.setValue(referrable.getEntityName());
                }
            }
            /*
             * process errors
             */
            return result;
        }
    }
}