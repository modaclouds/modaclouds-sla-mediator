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

import org.junit.Test;

import eu.modaclouds.sla.mediator.ContextInfo.DurationParser;
import eu.modaclouds.sla.mediator.ContextInfo.Validity;

public class DurationParserTest {

    @Test
    public void testParse() {

        assertEquals(
                new Validity(1, 0, 0),
                DurationParser.parse("P1Y"));
        
        assertEquals(
                new Validity(0, 1, 0),
                DurationParser.parse("P1M"));
        
        assertEquals(
                new Validity(0, 0, 60),
                DurationParser.parse("P60D"));

        assertEquals(
                new Validity(1, 5, 60),
                DurationParser.parse("P1Y5M60D"));
    }

}
