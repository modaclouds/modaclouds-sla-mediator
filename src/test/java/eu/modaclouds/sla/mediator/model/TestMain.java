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
package eu.modaclouds.sla.mediator.model;

import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.atos.sla.parser.data.wsag.Template;
import eu.modaclouds.sla.mediator.ContextInfo;
import eu.modaclouds.sla.mediator.TemplateGenerator;
import eu.modaclouds.sla.mediator.Creator;
import eu.modaclouds.sla.mediator.Creator.Factory;
import eu.modaclouds.sla.mediator.Utils;
import eu.modaclouds.sla.mediator.ViolationSubscriber;
import eu.modaclouds.sla.mediator.model.monitoringrules.MonitoringRules;
import eu.modaclouds.sla.mediator.model.palladio.RepositoryDocument;
import eu.modaclouds.sla.mediator.model.palladio.repository.Repository;
import eu.modaclouds.sla.mediator.model.qosconstraints.Constraints;


public class TestMain {

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;

    public TestMain() throws JAXBException, ParserConfigurationException {
        db =  dbf.newDocumentBuilder();
    }
    
    private <E> E load(Class<E> clazz, String path) throws JAXBException {
        
        InputStream is = getInputStream(path);
        return Utils.load(clazz, is);
    }

    private InputStream getInputStream(String path) {
        return this.getClass().getResourceAsStream(path);
    }
    
    private void run() throws JAXBException, JsonProcessingException {

        Factory factory = new Factory(
                "http://localhost:8080/sla-service", 
                "user", 
                "password"
        );
        Creator mediator = factory.getCreator(
                new ContextInfo("provider-a", "random-client", "modaclouds")
        );
        mediator.generateTemplate(
                getInputStream("/constraint.xml"),
                getInputStream("/rules.xml"),
                getInputStream("/default.repository.xml"));
        
        Constraints constraints = load(Constraints.class, "/constraint.xml");
        MonitoringRules rules = load(MonitoringRules.class, "/rules.xml");
        Repository repository = load(Repository.class, "/default.repository.xml");
        
        RepositoryDocument model = new RepositoryDocument(repository);
        TemplateGenerator generator = new TemplateGenerator(new ContextInfo("provider-a", "random-client", "modaclouds"));
        
        Template template = generator.generateTemplate(constraints, rules, model);
        
//        print(repository);
        Utils.print(template);
        
        ViolationSubscriber.Factory vsFactory = new ViolationSubscriber.Factory(
                "http://127.0.0.1:8888/v1/metrics", "http://sla.example.com/mp-observer");
        
//        ViolationSubscriber subscriber = vsFactory.getSubscriber();
//        subscriber.setMetricsBaseUrl("http://192.168.56.101:8888/v1/metrics");
//        subscriber.subscribeObserver(template, rules);
    }
    
    public static void main(String[] args) throws JAXBException, JsonProcessingException, ParserConfigurationException {
        TestMain testmain = new TestMain();
        testmain.run();
    }

}
