/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.bindgen.model;

import org.ballerinalang.bindgen.utils.BindgenEnv;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.ballerinalang.bindgen.command.BindingsGenerator.setExceptionList;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getAlias;

/**
 * Class for storing details pertaining to a specific Java constructor used for Ballerina bridge code generation.
 *
 * @since 1.2.0
 */
public class JConstructor extends BFunction  {

    private Class parentClass;
    private String exceptionName;
    private String shortClassName;
    private String exceptionConstName;
    private Constructor constructor;

    private boolean returnError = false;
    private boolean hasException = false; // identifies if the Ballerina returns should have an error declared
    private boolean handleException = false; // identifies if the Java constructor throws an error
    private boolean javaArraysModule = false;

    private List<JParameter> parameters = new ArrayList<>();
    private StringBuilder paramTypes = new StringBuilder();
    private Set<String> importedPackages = new HashSet<>();

    JConstructor(Constructor c, BindgenEnv env, JClass jClass, String constructorName) {
        super(BFunctionKind.CONSTRUCTOR, env);
        this.constructor = c;
        parentClass = c.getDeclaringClass();
        super.setDeclaringClass(parentClass);
        shortClassName = getAlias(c.getDeclaringClass());
        setExternalReturnType("handle");

        // Loop through the parameters of the constructor to populate a list.
        for (Parameter param : c.getParameters()) {
            JParameter parameter = new JParameter(param, parentClass, env);
            parameters.add(parameter);
            importedPackages.add(param.getType().getPackageName());
            paramTypes.append(getAlias(param.getType()).toLowerCase(Locale.ENGLISH));
            if (parameter.getIsPrimitiveArray() || param.getType().isArray()) {
                javaArraysModule = true;
                returnError = true;
                hasException = true;
            }
        }

        // Populate fields to identify error return types.
        for (Class<?> exceptionType : c.getExceptionTypes()) {
            try {
                if (!this.getClass().getClassLoader().loadClass(RuntimeException.class.getCanonicalName())
                        .isAssignableFrom(exceptionType)) {
                    JError jError = new JError(exceptionType);
                    exceptionName = jError.getShortExceptionName();
                    exceptionConstName = jError.getExceptionConstName();
                    if (env.getModulesFlag()) {
                        exceptionName = getPackageAlias(exceptionName, exceptionType);
                        exceptionConstName = getPackageAlias(exceptionConstName, exceptionType);
                    }
                    setExceptionList(jError);
                    setThrowable(jError);
                    hasException = true;
                    handleException = true;
                    break;
                }
            } catch (ClassNotFoundException ignore) {
            }
        }
        setErrorType(exceptionName);
        setFunctionName(constructorName);
        setExternalFunctionName(parentClass.getName().replace(".", "_").replace("$", "_") + "_" + constructorName);
    }

    private String getPackageAlias(String shortClassName, Class objectType) {
        if (objectType.getPackage() != parentClass.getPackage()) {
            return objectType.getPackageName().replace(".", "") + ":" + shortClassName;
        }
        return shortClassName;
    }

    String getParamTypes() {
        return paramTypes.toString();
    }

    boolean requireJavaArrays() {
        return javaArraysModule;
    }

    Set<String> getImportedPackages() {
        return importedPackages;
    }

    public List<JParameter> getParameters() {
        return parameters;
    }

    public String getFunctionReturnType() {
        StringBuilder returnString = new StringBuilder();
        returnString.append(shortClassName);
        if (hasException) {
            if (handleException) {
                returnString.append("|").append(exceptionName);
            }
            if (returnError) {
                returnString.append("|error");
            }
        }
        return returnString.toString();
    }

    public boolean isHandleException() {
        return handleException;
    }

    public String getExceptionName() {
        return exceptionName;
    }

    public String getExceptionConstName() {
        return exceptionConstName;
    }

    @Override
    public String getReturnType() {
        return shortClassName;
    }

    public Constructor getConstructor() {
        return constructor;
    }
}
