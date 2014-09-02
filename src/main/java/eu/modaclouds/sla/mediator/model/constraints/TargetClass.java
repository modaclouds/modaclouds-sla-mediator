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
package eu.modaclouds.sla.mediator.model.constraints;

public enum TargetClass {

    NONE(Names.NONE),
    METHOD(Names.METHOD),
    INTERNAL_COMPONENT(Names.INTERNAL_COMPONENT),
    VM(Names.VM);
    
    private static class Names {
        static String NONE = "";
        static String METHOD = "Method";
        static String INTERNAL_COMPONENT = "InternalComponent";
        static String VM = "VM";
    }
    
    String repr;
    
    private TargetClass(String repr) {
        this.repr = repr;
    }
    
    public static TargetClass fromString(String str) {

        if (Names.METHOD.equals(str)) {
            return METHOD;
        }
        if (Names.INTERNAL_COMPONENT.equals(str)) {
            return INTERNAL_COMPONENT;
        }
        if (Names.VM.equals(str)) {
            return VM;
        }
        return NONE;
    }
}
