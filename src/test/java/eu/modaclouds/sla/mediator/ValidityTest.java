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
import static java.util.Calendar.YEAR;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.DAY_OF_MONTH;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import eu.modaclouds.sla.mediator.ContextInfo.Validity;

public class ValidityTest {

    private Date getDate(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        
        c.set(year, month - 1, day);
        Date result = c.getTime();

        return result;
    }
    
    private void assertDate(int expectedYear, int expectedMonth, int expectedDay, Date realDate) {
        Calendar expected = Calendar.getInstance();
        Calendar real = Calendar.getInstance();
        
        real.setTime(realDate);
        expected.set(expectedYear, expectedMonth - 1, expectedDay);
        
        assertEquals(expected.get(YEAR), real.get(YEAR));
        assertEquals(expected.get(MONTH), real.get(MONTH));
        assertEquals(expected.get(DAY_OF_MONTH), real.get(DAY_OF_MONTH));
    }

    
    private void testAdd(Validity v, int expectedYear, int expectedMonth, int expectedDay) {
        Date d1 = getDate(2014, 1, 1);

        Date d2 = v.add(d1);
        assertDate(expectedYear, expectedMonth, expectedDay, d2);
    }
    
    @Test
    public void testAddDay() {
        testAdd(
            new Validity(0, 0, 32),
            2014, 2, 2
        );
        testAdd(
            new Validity(0, 0, 365),
            2015, 1, 1
        );
    }
    
    @Test
    public void testAddMonth() {
        testAdd(
            new Validity(0, 1, 0),
            2014, 2, 1
        );
        testAdd(
            new Validity(0, 12, 0),
            2015, 1, 1
        );
    }
    
    @Test
    public void testAddMonthAtEOM() {
        /*
         * more complicated test
         */
        Date d1 = getDate(2014, 1, 31);
        Validity v = new Validity(0, 1, 0);
        assertDate(2014, 2, 28, v.add(d1));
    }

    @Test
    public void testAddYear() {
        testAdd(
            new Validity(1, 0, 0),
            2015, 1, 1
        );
        
    }
}
