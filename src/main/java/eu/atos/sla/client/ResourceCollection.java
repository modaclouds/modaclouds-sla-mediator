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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.core.util.UnmodifiableMultivaluedMap;

import eu.atos.sla.client.RestClient.ExceptionFactory;
import eu.atos.sla.client.RestClient.RequestResponse;

/**
 * Base class that provides basic operations to all resources.
 * 
 * To use this class, extend and implement the additional (optional overriding existing) operations.
 * 
 * <br/>Example:
 * <pre><code>
 *   @Path("agreements")
 *   public static class AgreementResource<E> extends ResourceCollection<E> {
 *       public AgreementResource(final Class<E> clazz, String baseUrl, MediaType preferredType) {
 *           super(AgreementResource.class, clazz, baseUrl, preferredType);
 *       }
 *       
 *       @Path("active")
 *       public Agreement getActive() {}
 *       
 *       @Path("{uuid}/status")
 *       public Status getAgreementStatus(String uuid) {}
 *   }
 * </code></pre>
 *
 * @param <E> entity class that this class is going to handle (i.e., create, retrieve...) 
 * 
 * @author rsosa
 */
public abstract class ResourceCollection<E> {

    private String resourceUrl;
    private final RestClient client;
    private Class<E> clazz;
    @SuppressWarnings("rawtypes")
    private final ResourceHelper<? extends ResourceCollection> helper;
    private final MediaType preferredType;
    private final GenericType<List<E>> listType;
    protected final MultivaluedMap<String, String> defaultHeaders; 
       
    /* 
     * Local class trick to get current method from: 
     * http://stackoverflow.com/a/15377634 (Getting the name of the current executing method)
     */
    
    /**
     * @param collectionClass class extending {@link ResourceCollection.ResourceHelper} (see example in class header)
     * @param entityClass class that acts as resource (see example in class header); if a jaxb annotated class,
     *   the class will be de/serialized on each request/response.
     * @param baseUrl base url of the resource collection.
     * @param preferredType preferred type to use; it will be used as content-type and accept headers.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ResourceCollection(
            final Class<? extends ResourceCollection> collectionClass, 
            final Class<E> entityClass, 
            String baseUrl, 
            MediaType preferredType) {
        
        this.clazz = entityClass;
        this.client = new RestClient(baseUrl);
        this.helper = new ResourceHelper<ResourceCollection>((Class<ResourceCollection>) collectionClass);
        this.preferredType = preferredType;
        this.defaultHeaders = buildHeaders(preferredType);
        /*
         * http://jersey.576304.n2.nabble.com/Using-GenericType-in-a-generic-method-td6650758.html
         */
        this.listType = new GenericType<List<E>>(new ListParameterizedType(entityClass)){};
    }
    
    /**
     * Does a GET on a collection with optional query params.
     * @return list of entities.
     */
    @Path("")
    public List<E> get(MultivaluedMap<String, String> queryParams) {
        class Local{};
        
        String relativeUrl = helper.getPath(Local.class.getEnclosingMethod());
        RequestResponse requestResponse = client.get(relativeUrl, queryParams, defaultHeaders);

        checkResponse(requestResponse);
        List<E> result = (List<E>)requestResponse.getEntity(listType);
        return result;
    }

    /**
     * Does a GET on a collection with no query parameters (probably equivalent to a getAll()).
     * @return list of entities
     */
    public List<E> get() {
        return this.get(RestClient.EMPTY_MAP);
    }
    
    /**
     * Does a GET on an entity.
     * @param id
     * @return entity
     */
    @Path("{uuid}")
    public E getById(String id) {
        class Local{};
        
        String relativeUrl = helper.getPath(Local.class.getEnclosingMethod(), id);
        RequestResponse requestResponse = client.get(relativeUrl, RestClient.EMPTY_MAP, defaultHeaders);

        checkResponse(requestResponse);
        E result = requestResponse.getEntity(this.clazz);
        return result;
    }
    
    /**
     * Does a POST on a resource collection.
     * @param entity
     * @return body of the response
     */
    @Path("")
    public String create(E entity) {
        class Local{};
        
        String relativeUrl = helper.getPath(Local.class.getEnclosingMethod());
        RequestResponse requestResponse = client.post(relativeUrl, entity, RestClient.EMPTY_MAP, defaultHeaders);

        checkResponse(requestResponse);
        String result = requestResponse.getEntity(String.class);
        return result;
    }
    
    protected final void checkResponse(RequestResponse requestResponse) {
        if (!requestResponse.isOk()) {
            throw ExceptionFactory.getException(requestResponse);
        }
    }
    
    @SuppressWarnings("rawtypes")
    protected final ResourceHelper<? extends ResourceCollection> getHelper() {
        return helper;
    }
    
    protected final RestClient getClient() {
        return client;
    }

    protected final GenericType<List<E>> getListType() {
        return listType;
    }
    
    private UnmodifiableMultivaluedMap<String, String> buildHeaders(MediaType preferredType) {
        MultivaluedMapImpl auxHeaders = new MultivaluedMapImpl();
        auxHeaders.putSingle("Content-type", preferredType.toString());
        auxHeaders.putSingle("Accept", preferredType.toString());
        
        return new UnmodifiableMultivaluedMap<>(auxHeaders);
    }
    
    /**
     * Implements some operations for ResourceCollection regarding @Path annotations.
     * 
     * @param <R> {@link ResourceCollection} class that uses this helper.
     * 
     * @author rsosa
     */
    static class ResourceHelper<R> {
        private final Class<R> resourceClass;
        
        public ResourceHelper(Class<R> resourceClass) {
            this.resourceClass = resourceClass;
        }
    
        /**
         * Build a relative path joining the @Path in ResourceCollection and the @Path
         * in the <code>method</code>.
         * 
         * <br/>Example that returns "/providers/p1":
         * <code><pre>
         * 
         * @Path("/providers") 
         * class Providers {
         *   @Path("{uuid}") 
         *   public Provider getByUuid(String providerId) { 
         *     class Local{};
         *     String relativeUrl =
         *       helper.getPath(Local.class.getEnclosingMethod(), providerId);
         *     ... 
         *   } 
         * } 
         * Providers().getByUuid("p1")
         * </pre></code>
         * 
         * @param method
         *            Method with a @Path annotation
         * @param values
         *            to replace in the placeholders.
         * @return final relative url
         */
        public String getPath(Method method, Object... values) {
            
            String classPath = getPathValue(resourceClass.getAnnotation(Path.class));
            String methodPath = getPathValue(method.getAnnotation(Path.class));
            
            String path = join(classPath, methodPath);
            
            String result = replaceValues(path, values);
            return result;
        }
    
        private String replaceValues(String path, Object... values) {
            return UriBuilder.fromPath(path).build(values).toString();
        }
    
        /**
         * Build a relative path joining the @Path in ResourceCollection and the @Path
         * in the <code>method</code>.
         * 
         * The method is obtained through reflection by its name and parameters.
         * {@link #getPath(Method, Object...)} is preferred as it is a lot safer than this.
         * 
         * @see #getPath(Method, Object...)
         */
        public String getPath(String methodName, Class<?> parameterTypes) {
    
            Method method = getMethod(methodName, parameterTypes);
            
            String result = getPath(method);
            return result;
        }
        
        private String getPathValue(Path path) {
            
            return path == null? "" : path.value();
        }
        
        private Method getMethod(String methodName, Class<?> parameterTypes) {
    
            Method method;
            try {
                method = resourceClass.getMethod(methodName, parameterTypes);
                return method;
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        
        private static String join(String... elems) {
            StringBuilder joined = new StringBuilder();
            String sep = "";
            for (String elem : elems) {
                joined.append(sep);
                joined.append(elem);
                sep = "/";
            }
            return joined.toString();
        }
        
    }

    /**
     * Needed to deserialize a list of entities.
     */
    static class ListParameterizedType<E> implements ParameterizedType {
        
        private Class<E> clazz;
        
        public ListParameterizedType(Class<E> clazz) {
            this.clazz = clazz;
        }
        
        public Type[] getActualTypeArguments() { 
            return new Type[] { clazz }; 
        }
        
        public Type getRawType() { 
            return List.class; 
        }
        
        public Type getOwnerType() { 
            return List.class; 
        } 
    }
}