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

import java.util.Map;

/**
 * Wrapper class over a Jaxb root node, with an operation to obtain element by id.
 * 
 * This implementation relies on a MapInitializer (a class that visits all elements from root node
 * and returns a map with ids as key).
 * 
 * @author rsosa
 */
public class Document<T> implements IDocument<T> {

    private T root;
    private String prefix;
    private Map<String, IReferrable> map;
    private IMapInitializer<T> mapInitializer;
    
    public Document(T root, String prefix, IMapInitializer<T> mapInitializer) {
        if (prefix == null) {
            throw new NullPointerException("prefix cannot be null");
        }
        this.root = root;
        this.prefix = prefix;
        this.mapInitializer = mapInitializer;
    }
    
    @Override
    public T getJAXBNode() {
        return root;
    }

    @Override
    public IReferrable getElementById(String id) {
        if (map == null) {
            map = initMap();
        }
        return map.get(id);
    }

    @Override
    public String getIdFromHref(String href) {
        /*
         * An href has the following format: $base.$prefix#$id
         */
        int spos = href.indexOf("#");
        if (spos == -1) {
            throw new IllegalArgumentException(href + " is not a valid href");
        }
        String ns = href.substring(0, spos);
        int dpos = ns.indexOf(".");
        if (dpos == -1) {
            throw new IllegalArgumentException(href + " is not a valid href");
        }
        String prefix = ns.substring(dpos + 1, ns.length());
        if (!this.prefix.equals(prefix)) {
            throw new IllegalArgumentException("Expected href with prefix=" + this.prefix);
        }
        return href.substring(spos + 1);
    }
    
    private Map<String, IReferrable> initMap() {
        Map<String, IReferrable> map;
        
        map = mapInitializer.initMap(root);
        return map;
    }
    
    public static interface IMapInitializer<T> {
        Map<String, IReferrable> initMap(T root);
    }
    
}
