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

import static org.junit.Assert.fail;

import java.io.InputStream;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.modaclouds.sla.mediator.Creator.Factory;

/**
 * Test to show Creator usage.
 * 
 * Assume there is a running slacore at localhost:8080/sla-service with "user:password" credentials.
 * 
 * To run:
 *  mvn test -Dtest-profile=IntegrationTest
 *  
 * @author rsosa
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("application-context.xml")
@IfProfileValue(name="test-profile", value="IntegrationTest")
public class CreatorIntegrationTest {

    private static Factory factory;
    private Creator mediator;
    private ContextInfo ctx;
    
    private InputStream getInputStream(String path) {
        return this.getClass().getResourceAsStream(path);
    }

    @BeforeClass
    public static void initClass() {
        
        factory = new Factory(
                "http://localhost:8080/sla-service", 
                "user", 
                "password"
        );
    }

    @Before
    public void init() {
        
        ctx = new ContextInfo(
                "application-provider", 
                "application-client", 
                "modaclouds", 
                "P1Y");
        mediator = factory.getCreator(ctx);
    }

    
    @Test
    public void testRunTemplate() {
        
//        String templateId = mediator.runTemplate(
//                getInputStream("/constraint.xml"), 
//                getInputStream("/rules.xml"), 
//                getInputStream("/default.repository"),
//                getInputStream("/mic/"));
//        
//        System.err.println("Generated template id = " + templateId);
        fail("To be done");
    }

    @Test
    public void testRunAgreementWithExistingTemplate() {
//        String templateId = mediator.runTemplate(
//                getInputStream("/constraint.xml"), 
//                getInputStream("/rules.xml"), 
//                getInputStream("/default.repository"));
//        
//        String agreementId = mediator.runAgreement(templateId);
//        System.err.println("Generated agreement id = " + agreementId);
    }

    @Test
    public void testRunAgreementWithNonExistingTemplateShouldFail() {
//        try {
//            String agreementId = mediator.runAgreement("non-existent-template-id");
//            System.err.println("Generated agreement id = " + agreementId);
//        } catch (SlaException e) {
//            
//            if (e.getStatus() == 404) {
//                return;
//            }
//        }
//        fail("The agreement creation should throw exception");
    }
}
