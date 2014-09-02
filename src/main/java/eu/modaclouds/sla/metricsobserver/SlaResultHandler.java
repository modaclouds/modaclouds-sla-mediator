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
package eu.modaclouds.sla.metricsobserver;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polimi.modaclouds.monitoring.metrics_observer.ResultsHandler;
import it.polimi.modaclouds.monitoring.metrics_observer.model.Variable;

public class SlaResultHandler extends ResultsHandler {

    private Logger logger = LoggerFactory.getLogger(SlaResultHandler.class);
    
    @Override
    public void getData(List<String> varNames, List<Map<String, Variable>> bindings) {
        
        for (Map<String, Variable> m : bindings) {
            String datum = "";
            String sep = "";
            for (String varName : varNames) {
                Variable var = m.get(varName);
                if (var != null) {
                    String value;
                    value = process(varName, var.getValue());
                    datum += sep + value;
                }
                sep = " ";
            }
            System.out.println(datum);
        }
    }

    public String process(String varName, String rawValue) {

        logger.debug("Processing {}={}", varName, rawValue);
        switch (varName) {
        
        case "timestamp":
            logger.debug("Timestamp found");
            return new Date(new Long(rawValue)).toString();
            
        default:
            return rawValue;
        }
    }
}
