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
package eu.modaclouds.sla.mediator.generation;

import it.polimi.modaclouds.qos_models.schema.Constraint;
import it.polimi.modaclouds.qos_models.schema.MonitoredTarget;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import eu.atos.sla.parser.data.wsag.ServiceScope;
import eu.modaclouds.sla.mediator.model.constraints.TargetClass;
import eu.modaclouds.sla.mediator.model.palladio.IDocument;
import eu.modaclouds.sla.mediator.model.palladio.IReferrable;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.Component;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.Operation;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.SeffSpecification;

public class ServiceScoper {

    public static ServiceScope NOT_FOUND = new ServiceScope();

    interface ITargetScoper {
        public String generate(ITarget target, IDocument<Repository> repository);
    }
    
    interface ITarget {
        TargetClass getClazz();
        String getId();
    }

    private static HashMap<TargetClass, ITargetScoper> map = new HashMap<TargetClass, ITargetScoper>();
    private static ITargetScoper nullTargetScoper = new NullTargetScoper();
    
    static {
        map.put(TargetClass.METHOD, new ServiceScoper.MethodTargetScoper());
        map.put(TargetClass.INTERNAL_COMPONENT, new ServiceScoper.InternalComponentTargetScoper());
    }
    
    public static ServiceScope fromConstraint(Constraint constraint, IDocument<Repository> document) {
        ITarget target = new ConstraintTarget(constraint);
        ServiceScope result = new ServiceScoper().generate(Collections.singletonList(target), document);
        
        return result;
    }
    
    public static ServiceScope fromRule(MonitoringRule rule, IDocument<Repository> document) {
        List<ITarget> targets = new ArrayList<ITarget>();
        
        for (MonitoredTarget target : rule.getMonitoredTargets().getMonitoredTargets()) {
            targets.add(new MonitoringRuleTarget(target));
        }
        ServiceScope result = new ServiceScoper().generate(targets, document);
        
        return result;
    }
    
    public ServiceScope generate(List<ITarget> targets, IDocument<Repository> document) {
        
        StringBuilder aux = new StringBuilder();
        String sep = "";
        for (ITarget target : targets) {
            ITargetScoper scoper = map.containsKey(target.getClazz())? map.get(target.getClazz()) : nullTargetScoper;
            aux.append(sep);
            aux.append(scoper.generate(target, document));
            sep = ",";
        }
        
        ServiceScope result = new ServiceScope();
        result.setServiceName(TemplateGenerator.DEFAULT_SERVICE_NAME);
        result.setValue(aux.toString());
        
        return result;
    }
    
    private static class NullTargetScoper implements ITargetScoper {

        @Override
        public String generate(ITarget target, 
                IDocument<Repository> document) {
            
            return "[null]";
        }
    }

    private static class MethodTargetScoper implements ITargetScoper {
        
        @Override
        public String generate(ITarget target, IDocument<Repository> document) {
            
            IReferrable referrable = document.getElementById(target.getId());
            
            String result = "[notfound]";
            
            if (referrable != IDocument.NOT_FOUND) {
                if (referrable instanceof SeffSpecification) {
                    result = process(target, document, (SeffSpecification) referrable);
                }
            }
            return result;
        }
        
        private String process(ITarget target, IDocument<Repository> document, SeffSpecification element) {
            
            Component parent = element.getParent();
            Operation operation = element.getOperation(document);
            String result = String.format("%s/%s", parent.getEntityName(), operation.getEntityName());
            
            return result;
        }
    }

    private static class InternalComponentTargetScoper implements ITargetScoper {
    
        @Override
        public String generate(ITarget target, IDocument<Repository> document) {
            
            IReferrable referrable = document.getElementById(target.getId());
            
            String result = "[notfound]";
            
            if (referrable != IDocument.NOT_FOUND) {
                if (referrable instanceof Component) {
                    result = referrable.getEntityName();
                }
            }
            return result;
        }
    }
    
    private static class ConstraintTarget implements ITarget {
        private final TargetClass clazz;
        private final String id;
        
        public ConstraintTarget(Constraint constraint) {
            this.clazz = TargetClass.fromString(constraint.getTargetClass());
            this.id = constraint.getTargetResourceIDRef();
        }
        
        @Override
        public TargetClass getClazz() {
            return clazz;
        }

        @Override
        public String getId() {
            return id;
        }
        
    }
    
    private static class MonitoringRuleTarget implements ITarget {
        private final TargetClass clazz;
        private final String id;

        public MonitoringRuleTarget(MonitoredTarget target) {
            this.clazz = TargetClass.fromString(target.getClazz());
            this.id = target.getType();
        }
        
        @Override
        public TargetClass getClazz() {
            return clazz;
        }

        @Override
        public String getId() {
            return id;
        }
    }
    
}