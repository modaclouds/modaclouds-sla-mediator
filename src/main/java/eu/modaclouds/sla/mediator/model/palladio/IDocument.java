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



/**
 * Wrapper class over a Jaxb root node, with operation to obtain element by id.
 * @author rsosa
 */
public interface IDocument<E> {
    
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
    
    E getJAXBNode();
    IReferrable getElementById(String id);
    String getIdFromHref(String href);
}