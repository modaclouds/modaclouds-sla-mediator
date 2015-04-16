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
import it.polimi.modaclouds.qos_models.schema.Constraints;
import it.polimi.modaclouds.qos_models.schema.MonitoringRules;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import eu.atos.sla.parser.data.wsag.Template;
import eu.modaclouds.sla.mediator.generation.TemplateGenerator;
import eu.modaclouds.sla.mediator.model.palladio.Model;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository;

public class TemplateGeneratorTest {

    public TemplateGeneratorTest() {
        
    }
    
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testGenerateTemplateShouldPass() {
        try {
            InputStream rulesIs = getInputStream("/ehealth/MonitoringRules.xml");
            MonitoringRules rules = Utils.load(MonitoringRules.class, rulesIs);
            
            InputStream constraintsIs = getInputStream("/ehealth/QoSConstraints.xml");
            Constraints constraints = Utils.load(Constraints.class, constraintsIs);
            
            InputStream repositoryIs = getInputStream("/ehealth/test.repository");
            Repository repository = Utils.load(Repository.class, repositoryIs);
            
            Model model = new Model(repository, null, null, null, null);
            
            ContextInfo ctx = new ContextInfo("provider", "consumer", "service", "P1Y");
            TemplateGenerator generator = new TemplateGenerator(ctx);
            
            Template template = generator.generateTemplate(constraints, rules, model);
            
            Utils.print(template, Template.class);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private InputStream getInputStream(String path) {
        return this.getClass().getResourceAsStream(path);
    }
    
}
