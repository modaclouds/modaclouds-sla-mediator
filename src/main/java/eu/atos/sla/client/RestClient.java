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

import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.core.util.UnmodifiableMultivaluedMap;

/**
 * Simple REST client using jersey.
 * 
 * It implements the basic REST operations: get, post, put, delete,
 * and uses jersey client libs in the backend. This client is obtained from a 
 * <code>ClientFactory</code> in the constructor.
 * 
 * @author rsosa
 */
public class RestClient {
    private static final Logger logger = LoggerFactory.getLogger(RestClient.class);
    private final Client client;
    private final String baseUrl;
    private final WebResource resource;
    
    public static final MultivaluedMap<String, String> EMPTY_MAP = 
            new UnmodifiableMultivaluedMap<>(new MultivaluedMapImpl());

    public RestClient(String baseUrl) {
        this.baseUrl = baseUrl;

        /*
         * XXX This could be a better design
         */
        this.client = RestClient.ClientFactory.getClient();
        resource = client.resource(UriBuilder.fromUri(baseUrl).build());
    }
    
    /**
     * Executes a method.
     * 
     * @param method method name (one of HttpMethod.*)
     * @param relativeUrl the final url is baseUrl/relativeUrl?queryparam1=queryvalue1&...
     * @param data in the request body: a string, a jaxb annotated pojo, etc.
     * @param queryParams MultivaluedMap of query parameters
     * @param headers MultivaluedMap of headers
     * @return A {@link RequestResponse} with the response (getStatus() to check status; getEntity() to get body)
     */
    public RequestResponse method(
            String method, 
            String relativeUrl, 
            Object data, 
            MultivaluedMap<String, String> queryParams, 
            MultivaluedMap<String, String> headers) {
        
        WebResource finalResource = resource.
            path(relativeUrl).
            queryParams(queryParams);
        
        WebResource.Builder builder = finalResource.getRequestBuilder();
        
        builder = applyHeaders(builder, headers);
        
        ClientResponse response = 
                builder.
                method(method, ClientResponse.class, data);
        
        /*
         * TODO It would be great to log the body (at least) of the response, as
         * response.getEntity(String.class) flushes the stream, and can't be used.
         * Maybe tweaking jersey log levels.
         */
        logger.debug("{} {} {} {}", 
                response.getStatus(), 
                method,
                finalResource.getURI(),
//                    response.getEntity(String.class)
                ""
        );
        RequestResponse result = new RequestResponse(method, finalResource.getURI().toString(), headers, response);
        return result;
    }

    /**
     * Executes a get method.
     * 
     * @see #method(String, String, Object, MultivaluedMap, MultivaluedMap)
     */
    public RequestResponse get(
            String relativeUrl, 
            MultivaluedMap<String, String> queryParams, 
            MultivaluedMap<String, String> headers) {
    
        return this.method(HttpMethod.GET, relativeUrl, null, queryParams, headers);
    }

    /**
     * Executes a post method.
     * 
     * @see #method(String, String, Object, MultivaluedMap, MultivaluedMap)
     */
    public RequestResponse post(
            String relativeUrl, 
            Object data, 
            MultivaluedMap<String, String> queryParams, 
            MultivaluedMap<String, String> headers) {
        
        return this.method(HttpMethod.POST, relativeUrl, data, queryParams, headers);
    }

    /**
     * Executes a put method.
     * 
     * @see #method(String, String, Object, MultivaluedMap, MultivaluedMap)
     */
    public RequestResponse put(
            String relativeUrl, 
            Object data, 
            MultivaluedMap<String, String> queryParams, 
            MultivaluedMap<String, String> headers) {
        
        return this.method(HttpMethod.PUT, relativeUrl, data, queryParams, headers);
    }

    /**
     * Executes a delete method.
     * 
     * @see #method(String, String, Object, MultivaluedMap, MultivaluedMap)
     */
    public RequestResponse delete(
            String relativeUrl, 
            MultivaluedMap<String, String> queryParams, 
            MultivaluedMap<String, String> headers) {
        
        return this.method(HttpMethod.DELETE, relativeUrl, null, queryParams, headers);
    }
    
    private WebResource.Builder applyHeaders(
            WebResource.Builder builder, MultivaluedMap<String, String> headers) {

        for (String key : headers.keySet()) {
            for (String value : headers.get(key)) {
                builder = builder.header(key, value);
            }
        }
        return builder;
    }

    /**
     * Store some info about request and response once a REST connection has been established.
     */
    public static class RequestResponse {
        private String method;
        private String url;
        private MultivaluedMap<String, String> requestHeaders;
        private ClientResponse response;
        
        public RequestResponse(
                String method, 
                String url, 
                MultivaluedMap<String, String> requestHeaders,
                ClientResponse response) {
    
            this.method = method;
            this.url = url;
            this.requestHeaders = requestHeaders;
            this.response = response;
        }
        
        public String getMethod() {
            return method;
        }
        public String getUrl() {
            return url;
        }
        public int getStatus() {
            return response.getStatus();
        }
        public MultivaluedMap<String, String> getRequestHeaders() {
            return requestHeaders;
        }
        public MultivaluedMap<String, String> getResponseHeaders() {
            return response.getHeaders();
        }
        public <E> E getEntity(Class<E> clazz) {
            return response.getEntity(clazz);
        }
        public <E> List<E> getEntity(GenericType<List<E>> gt) {
            return response.getEntity(gt);
        }
        
        public boolean isOk() {
            int statusCode = getStatus();
            return statusCode >= 200 && statusCode < 300;
        }
    }

    /**
     * Factory where to obtain the jersey clients to be used in {@link RestClient}.
     * 
     * It's main purpose is to preconfigure basic auth.
     * 
     * @author rsosa
     */
    public static class ClientFactory {
        private static ClientFilter[] filters;
        
        public static void setup(String user, String password) {
            filters = new ClientFilter[] {
                    new HTTPBasicAuthFilter(user, password)
            };
        }
        
        public static Client getClient() {
            ClientConfig config;
            Client client;
            
            config = new DefaultClientConfig();
            client = Client.create(config);
    
            for (ClientFilter filter : filters) {
                client.addFilter(filter);
            }
            
            return client;
        }
    }

    static class ExceptionFactory {
        public static SlaException getException(RequestResponse requestResponse) {
            
            int statusCode = requestResponse.getStatus();
            if (statusCode >= 200 && statusCode < 300) {
                throw new IllegalArgumentException("Response was succesful. Exception not needed");
            }
            
            return new SlaException(requestResponse);
        }
    }    
}