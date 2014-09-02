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

import javax.ws.rs.core.MultivaluedMap;

public class SlaException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    private RestClient.RequestResponse requestResponse;
    
    public SlaException(RestClient.RequestResponse requestResponse) {
        super(String.format("%d on %s %s. Response body is: '%s'", 
                requestResponse.getStatus(), 
                requestResponse.getMethod(), 
                requestResponse.getUrl(),
                requestResponse.getEntity(String.class)
                ));
        this.requestResponse = requestResponse;
    }
    
    public String getMethod() {
        return requestResponse.getMethod();
    }

    public String getUrl() {
        return requestResponse.getUrl();
    }

    public int getStatus() {
        return requestResponse.getStatus();
    }

    public MultivaluedMap<String, String> getRequestHeaders() {
        return requestResponse.getRequestHeaders();
    }

    public MultivaluedMap<String, String> getResponseHeaders() {
        return requestResponse.getResponseHeaders();
    }
    
    public String getBody() {
        return requestResponse.getEntity(String.class);
    }

}