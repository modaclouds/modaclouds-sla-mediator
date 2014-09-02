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
