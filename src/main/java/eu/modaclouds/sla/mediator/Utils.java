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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

public class Utils {

    public static <E> E load(Class<E> clazz, InputStream is) throws JAXBException {
        
        JAXBContext jaxbContext;
        Unmarshaller jaxbUnmarshaller;
        
        jaxbContext = JAXBContext.newInstance(clazz);
        
        jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        
        @SuppressWarnings("unchecked")
        E result = (E) jaxbUnmarshaller.unmarshal(is);
        return result;
    }

    public static <E> void print(E e) throws JAXBException {
        print(e, e.getClass());
    }
    
    public static <E> void print(E e, Class<?>... classesToBeBound) throws JAXBException {
        print(e, System.out, classesToBeBound);
    }

    public static <E> String toString(E e) throws JAXBException {
        String charsetName = "utf-8";
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        print(e, os);
        try {
            return os.toString(charsetName);
        } catch (UnsupportedEncodingException e1) {
            throw new IllegalArgumentException(charsetName + " is not supported");
        }
    }
    
    public static <E> void print(E e, File f) throws JAXBException, FileNotFoundException {
        FileOutputStream fos = new FileOutputStream(f);
        print(e, fos);
    }
    
    public static <E> void print(E e, OutputStream os) throws JAXBException {
        print(e, os, e.getClass());
    }
    
    public static <E> void print(E e, OutputStream os, Class<?>... classesToBeBound) throws JAXBException {
        JAXBContext jaxbContext;
        Marshaller jaxbMarshaller;
        jaxbContext = JAXBContext.newInstance(classesToBeBound);
        jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        /*
         * http://stackoverflow.com/a/22756191
         */
        jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
        jaxbMarshaller.marshal(e, os);
    }
    
    private <E> void printJson(E e) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector introspector = new JacksonAnnotationIntrospector();
//        mapper.getDeserializationConfig().setAnnotationIntrospector(introspector);
        mapper.getSerializationConfig().with(introspector);

        System.out.println(mapper.writeValueAsString(e));
    }

    static InputStream getInputStream(String... subpaths) throws FileNotFoundException {
        
        /*
         * Really! Doesn't JDK have a join function??
         */
        String path = join(File.separator, subpaths);
        File file = new File(path);
        return new FileInputStream(file);
    }

    public static String join(String separator, String... elems) {
        StringBuilder joined = new StringBuilder();
        String sep = "";
        for (String elem : elems) {
            joined.append(sep);
            joined.append(elem);
            sep = separator;
        }
        return joined.toString();
    }

    static String[] splitCredentials(String combinedCredentials) {
        if (combinedCredentials == null) {
            combinedCredentials = "";
        }

        int splitIndex = combinedCredentials.indexOf(':');

        /*
         * separator not found
         */
        if (splitIndex == -1) {
            return new String[] { combinedCredentials, "" };
        }
        return new String[] { 
                combinedCredentials.substring(0, splitIndex), 
                combinedCredentials.substring(splitIndex + 1) };
    }
    
//  private <E> RepositoryDocument loadWithBinder(Class<E> clazz, String path) throws JAXBException {
//  
//  JAXBContext jaxbContext;
//
//  File file = new File(this.getClass().getResource(path).getFile());
//  Document document;
//  try {
//      document = db.parse(file);
//  } catch (SAXException e1) {
//      throw new JAXBException(e1);
//  } catch (IOException e1) {
//      throw new JAXBException(e1);
//  }
//
//  Element root = document.getDocumentElement();
//  System.out.println(String.format("{%s}%s local=%s prefix=%s baseuri=%s nodename=%s", 
//          root.getNamespaceURI(), root.getTagName(), root.getLocalName(), root.getPrefix(),
//          root.getBaseURI(), root.getNodeName() ) );
////  Element comp = (Element)root.getElementsByTagName("components__Repository").item(0);
////  System.out.println(String.format("{%s}%s", comp.getNamespaceURI(), comp.getNodeName() ) );
//  jaxbContext = JAXBContext.newInstance(clazz);
//  Binder<Node> binder = jaxbContext.createBinder();
//  E e = (E) binder.unmarshal(document);
//
//  RepositoryDocument result = new RepositoryDocument(document, binder, (Repository) e);
//  return result;
//}
    
//  private <E> void save(E e, String path) throws JAXBException {
//  JAXBContext jaxbContext;
//  Marshaller jaxbMarshaller;
//  
//  jaxbContext = JAXBContext.newInstance(e.getClass());
//  jaxbMarshaller = jaxbContext.createMarshaller();
//}
    
}
