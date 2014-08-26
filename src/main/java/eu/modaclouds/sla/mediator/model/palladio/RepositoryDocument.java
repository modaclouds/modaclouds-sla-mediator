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
package eu.modaclouds.sla.mediator.model.palladio;

import java.util.HashMap;
import java.util.Map;

import eu.modaclouds.sla.mediator.model.palladio.repository.Repository;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.Component;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.Interface;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.Operation;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository.SeffSpecification;

public class RepositoryDocument implements IDocument<Repository> {
    private Repository repository;
    private Map<String, IReferrable> map;
    
    public static IReferrable NOT_FOUND = new IReferrable() {
        
        @Override
        public String getId() {
            return "";
        }
        
        @Override
        public String getEntityName() {
            return "";
        }
    };

    public RepositoryDocument(Repository repository) {
    
        this.repository = repository;
        
    }
    
    public IReferrable getElementById(String id) {

        if (map == null) {
            map = initMap();
        }
        return map.get(id);
    }

    @Override
    public Repository getJAXBNode() {
        return repository;
    }
    
    private Map<String, IReferrable> initMap() {
        HashMap<String, IReferrable> map;
        
        map = new MapInitializer(repository).initMap();
        return map;
    }
    
    /*
     * This should be done with an Iterator, but making a nontrivial iterator in 
     * java is a total hell. How I miss python's yield...
     */
    private static class MapInitializer {
        Repository repository;
        
        public MapInitializer(Repository repository) {
            this.repository = repository;
        }

        private HashMap<String, IReferrable> cycle(Repository repository) {
            HashMap<String, IReferrable> map = new HashMap<String, IReferrable>();
            map.put(repository.getId(), repository);
            for (Component comp : repository.getComponents()) {
                map.put(comp.getId(), comp);
                for (SeffSpecification spec : comp.getSeffSpecifications()) {
                    map.put(spec.getId(), spec);
                }
            }
            for (Interface iface : repository.getInterfaces()) {
                map.put(iface.getId(), iface);
                for (Operation operation : iface.getOperations()) {
                    map.put(operation.getId(), operation);
                }
            }
            return map;
        }
        
        public HashMap<String, IReferrable> initMap() {
            
            HashMap<String, IReferrable> map = cycle(repository);
            return map;
        }
    }
    
}