package eu.modaclouds.sla.mediator;

import static org.junit.Assert.*;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import eu.modaclouds.sla.mediator.ContextInfo.Validity;
import eu.modaclouds.sla.mediator.model.palladio.resourceenvironment.ResourceEnvironment.ResourceContainer;

public class CreatorTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testRunSla() {
        
        ContextInfo ctx = new ContextInfo("modaclouds", "consumer", "ofbiz", new Validity (1, 0, 0));
        SlaCoreConfig cfg = new SlaCoreConfig("", "", "");
        Creator creator = new Creator(cfg, ctx, false);
        
        Creator.Result result = creator.runSla(
                getInputStream("/ofbiz/constraint.xml"), 
                getInputStream("/ofbiz/rules.xml"), 
                getInputStream("/ofbiz/default.repository"), 
                getInputStream("/ofbiz/default.allocation"), 
                getInputStream("/ofbiz/default.system"), 
                getInputStream("/ofbiz/default.resourceenvironment"), 
                getInputStream("/ofbiz/ContainerExtension.xml"), 
                getInputStream("/ofbiz/Functionality2Tier.xml"));
        
        assertNotNull(result.getHighId());
        assertEquals(2, result.getLowIds().size());

        for (ResourceContainer container : result.getModel().getResourceContainers()) {
            String containerId = container.getId();
            assertTrue(result.getLowIds().containsKey(containerId));

            String wsagId = result.getLowIds().get(containerId);
            assertNotNull(wsagId);
        }
    }

    private InputStream getInputStream(String path) {
        return this.getClass().getResourceAsStream(path);
    }
}
