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
package eu.atos.sla.client;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import eu.atos.sla.client.RestClient.RequestResponse;
import eu.atos.sla.parser.data.EnforcementJob;
import eu.atos.sla.parser.data.Provider;
import eu.atos.sla.parser.data.wsag.Agreement;
import eu.atos.sla.parser.data.wsag.Template;

public class SlaClient {
    private static final Logger logger = LoggerFactory.getLogger(SlaClient.class);
    
    private String baseUrl;
    private MediaType preferredType;
    
    /**
     * If more complex initialization is needed, a Builder could be used.
     */
    public SlaClient(String baseUrl, MediaType preferredType, String user, String password) {
        this.baseUrl = baseUrl;
        this.preferredType = preferredType;
        RestClient.ClientFactory.setup(user, password);    /* ugly setup */
    }
    
    public ProviderResource<Provider> providers() {
        return new ProviderResource<>(Provider.class, baseUrl, preferredType);
    }
    
    public TemplateResource<Template> templates() {
        return new TemplateResource<>(Template.class, baseUrl, preferredType);
    }

    public AgreementResource<Agreement> agreements() {
        return new AgreementResource<>(Agreement.class, baseUrl, preferredType);
    }
    
    public EnforcementResource<EnforcementJob> enforcements() {
        return new EnforcementResource<EnforcementJob>(EnforcementJob.class, baseUrl, preferredType);
    }
    

    @Path("providers")
    public static class ProviderResource<E> extends ResourceCollection<E> {
        public ProviderResource(final Class<E> clazz, String baseUrl, MediaType preferredType) {
            super(ProviderResource.class, clazz, baseUrl, preferredType);
        }
    }
    
    @Path("templates")
    public static class TemplateResource<E> extends ResourceCollection<E> {
        public TemplateResource(final Class<E> clazz, String baseUrl, MediaType preferredType) {
            super(TemplateResource.class, clazz, baseUrl, preferredType);
        }
    }
    
    @Path("agreements")
    public static class AgreementResource<E> extends ResourceCollection<E> {
        public AgreementResource(final Class<E> clazz, String baseUrl, MediaType preferredType) {
            super(AgreementResource.class, clazz, baseUrl, preferredType);
        }
        
        @Path("active")
        public List<Agreement> getActive() {
            class Local{};
            
            String relativeUrl = getHelper().getPath(Local.class.getEnclosingMethod());
            RequestResponse requestResponse = getClient().get(relativeUrl, RestClient.EMPTY_MAP, defaultHeaders);

            checkResponse(requestResponse);
            @SuppressWarnings("unchecked")
            List<Agreement> result = (List<Agreement>)requestResponse.getEntity(getListType());
            return result;
        }
    }

    @Path("violations")
    public static class ViolationResource<E> extends ResourceCollection<E> {
        public ViolationResource(final Class<E> clazz, String baseUrl, MediaType preferredType) {
            super(ViolationResource.class, clazz, baseUrl, preferredType);
        }
    }

    @Path("enforcements")
    public static class EnforcementResource<E> extends ResourceCollection<E> {
        public EnforcementResource(final Class<E> clazz, String baseUrl, MediaType preferredType) {
            super(EnforcementResource.class, clazz, baseUrl, preferredType);
        }

        @PUT
        @Path("{uuid}/start")
        public void start(String agreementId) {
            class Local{};
            
            String relativeUrl = getHelper().getPath(Local.class.getEnclosingMethod(), agreementId);
            RequestResponse requestResponse = getClient().put(relativeUrl, "", RestClient.EMPTY_MAP, defaultHeaders);

            checkResponse(requestResponse);
            return;
        }

        @PUT
        @Path("{uuid}/stop")
        public void stop(String agreementId) {
            class Local{};
            
            String relativeUrl = getHelper().getPath(Local.class.getEnclosingMethod(), agreementId);
            RequestResponse requestResponse = getClient().put(relativeUrl, "", RestClient.EMPTY_MAP, defaultHeaders);

            checkResponse(requestResponse);
            return;
        }
    }

    public static void main(String[] args) throws Exception {

        SlaClient client = new SlaClient(
                "http://localhost:8080/sla-service", 
                MediaType.APPLICATION_XML_TYPE, 
                "user", 
                "password");
        
//        client.providers().getById("noexiste");
        Provider p = new Provider();
        p.setUuid(UUID.randomUUID().toString());
        p.setName("provider-a");
//        client.providers().create(p);
        
        List<Provider> l = client.providers().get();
        System.out.println(Arrays.deepToString(l.toArray()));
//        Utils.<List<Provider>>print(l);

        MultivaluedMapImpl params = new MultivaluedMapImpl();
        params.add("key", "value");
        List<Template> lt = client.templates().get(params);
        System.out.println(Arrays.deepToString(lt.toArray()));
        
        client.agreements().getActive();
        
        client.enforcements().stop("agreement03");
    }
    
    
}
