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
package eu.modaclouds.sla.mediator;

import static org.junit.Assert.*;

import java.io.InputStream;

import it.polimi.modaclouds.qos_models.schema.Action;
import it.polimi.modaclouds.qos_models.schema.MonitoringRule;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.junit.Before;
import org.junit.Test;

import eu.modaclouds.sla.mediator.generation.BusinessActionParser;

public class BusinessActionParserTest {

    DatatypeFactory dtFactory;
    BusinessActionParser parser;
    
    public BusinessActionParserTest() throws DatatypeConfigurationException {
        dtFactory = DatatypeFactory.newInstance();
        parser = new BusinessActionParser();
    }
    
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testParse() throws JAXBException {
        InputStream rulesIs = getInputStream("/eu/modaclouds/sla/mediator/businessactionparser-rules.xml");
        MonitoringRules rules = Utils.load(MonitoringRules.class, rulesIs);
        
        ExpectedData[] expectedData = new ExpectedData[] {
                null,
                new ExpectedData(1, "PT0S", "discount1", "1", "%", "P1D"),
                new ExpectedData(2, "PT2H", "discount2", "2", "euro", "P2D")
        };
        
        for (MonitoringRule rule : rules.getMonitoringRules()) {
            int i = 0;
            for (Action action : rule.getActions().getActions()) {
                BusinessActionParser.Result actual;
                ExpectedData expected = expectedData[i];
                try {
                    actual = parser.parse(action);
                    checkParserResult(actual, expected.count, expected.duration, expected.type, 
                            expected.value, expected.unit, expected.validity);
                } catch (IllegalArgumentException e) {
                    if (expected != null) {
                        fail("Non Business Action should throw IllegalArgumentException");
                    }
                }
                
                i++;
            }
        }
        assertEquals(1,  1);
    }
    
    private void checkParserResult(BusinessActionParser.Result actual, 
            int expectedCount, String expectedDuration, String expectedType, String expectedValue, 
            String expectedUnit, String expectedValidity) {
        
        assertEquals(expectedCount, actual.getCount());
        assertEquals(dtFactory.newDuration(expectedDuration), actual.getDuration());
        assertEquals(expectedType, actual.getType());
        assertEquals(expectedValue, actual.getValue());
        assertEquals(expectedUnit, actual.getUnit());
        assertEquals(expectedValidity, actual.getValidity());
    }

    private InputStream getInputStream(String path) {
        return this.getClass().getResourceAsStream(path);
    }

    private static class ExpectedData {
        int count;
        String duration;
        String type;
        String value;
        String unit;
        String validity;
        
        public ExpectedData(int count, String duration, String type,
                String value, String unit, String validity) {
            this.count = count;
            this.duration = duration;
            this.type = type;
            this.value = value;
            this.unit = unit;
            this.validity = validity;
        }
    }
}
