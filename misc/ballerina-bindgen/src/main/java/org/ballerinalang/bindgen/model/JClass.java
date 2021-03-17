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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.bindgen.command.BindingsGenerator.setAllClasses;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getAlias;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isAbstractClass;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isFinalField;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isPublicField;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isPublicMethod;

/**
 * Class for storing details pertaining to a specific Java class used for Ballerina bridge code generation.
 *
 * @since 1.2.0
 */
public class JClass {

    private BindgenEnv env;
    private String prefix;
    private String className;
    private String packageName;
    private String accessModifier;
    private String shortClassName;
    private String balPackageName;
    private Class currentClass;

    private boolean modulesFlag = false;
    private boolean isInterface = false;
    private boolean isDirectClass = false;
    private boolean isAbstract = false;
    private boolean importJavaArraysModule = false;

    private Set<String> superClasses = new HashSet<>();
    private Map<String, String> superClassPackage = new HashMap<>();
    private Set<String> importedPackages = new HashSet<>();
    private Set<String> superClassNames = new LinkedHashSet<>();
    private List<JField> fieldList = new ArrayList<>();
    private List<JMethod> methodList = new ArrayList<>();
    private List<JConstructor> constructorList = new ArrayList<>();
    private Map<String, Integer> overloadedMethods = new HashMap<>();

    public JClass(Class c, BindgenEnv env) {
        this.env = env;
        currentClass = c;
        className = c.getName();
        prefix = className.replace(".", "_").replace("$", "_");
        shortClassName = getAlias(c);
        packageName = c.getPackage().getName();
        shortClassName = getExceptionName(c, shortClassName);
        superClassNames.add(c.getName());
        modulesFlag = env.getModulesFlag();
        balPackageName = env.getPackageName();

        setAllClasses(shortClassName);
        if (c.isInterface()) {
            isInterface = true;
            setAllClasses(getAlias(Object.class));
            superClassNames.add(Object.class.getName());
            superClasses.add(getAlias(Object.class));
            superClassPackage.put(getAlias(Object.class), Object.class.getPackageName().replace(".", ""));
        }
        populateImplementedInterfaces(c.getInterfaces());

        Class sClass = c.getSuperclass();
        while (sClass != null) {
            populateImplementedInterfaces(sClass.getInterfaces());
            String simpleClassName = getAlias(sClass).replace("$", "");
            superClassNames.add(sClass.getName());
            superClasses.add(simpleClassName);
            superClassPackage.put(simpleClassName, sClass.getPackageName().replace(".", ""));
            setAllClasses(simpleClassName);
            sClass = sClass.getSuperclass();
        }

        if (isAbstractClass(c)) {
            isAbstract = true;
        }
        if (env.isDirectJavaClass()) {
            isDirectClass = true;
            populateConstructors(c.getConstructors());
            populateMethods(c);
            populateFields(c.getFields());
        }

        if (modulesFlag) {
            importedPackages.remove(c.getPackageName());
        }
    }

    private String getExceptionName(Class exception, String name) {
        try {
            // Append the prefix "J" in front of bindings generated for Java exceptions.
            if (this.getClass().getClassLoader().loadClass(Exception.class.getCanonicalName())
                    .isAssignableFrom(exception)) {
                return "J" + name;
            }
        } catch (ClassNotFoundException ignore) {
            // Silently ignore if the exception class cannot be found.
        }
        return name;
    }

    private List<Method> getMethodsAsList(Class classObject) {
        Method[] declaredMethods = classObject.getDeclaredMethods();
        List<Method> classMethods = new LinkedList<>();
        for (Method m : declaredMethods) {
            if (!m.isSynthetic() && (!m.getName().equals("toString")) && isPublicMethod(m)) {
                classMethods.add(m);
            }
        }
        return classMethods;
    }

    private void populateConstructors(Constructor[] constructors) {
        int i = 1;
        List<JConstructor> tempList = new ArrayList<>();
        for (Constructor constructor : constructors) {
            tempList.add(new JConstructor(constructor, env, this, null));
        }
        tempList.sort(Comparator.comparing(JConstructor::getParamTypes));
        for (JConstructor constructor:tempList) {
            JConstructor jConstructor = new JConstructor(constructor.getConstructor(), env,
                    this, "new" + shortClassName + i);
            if (modulesFlag) {
                importedPackages.addAll(jConstructor.getImportedPackages());
            }
            constructorList.add(jConstructor);
            if (jConstructor.requireJavaArrays()) {
                importJavaArraysModule = true;
            }
            i++;
        }
    }

    private void populateMethods(Class c) {
        List<JMethod> tempList = new ArrayList<>();
        for (Method method : getMethodsAsList(c)) {
            tempList.add(new JMethod(method, env, prefix, currentClass, 0));
        }
        tempList.sort(Comparator.comparing(JMethod::getParamTypes));
        for (JMethod method : tempList) {
            setMethodCount(method.getJavaMethodName());
            JMethod jMethod = new JMethod(method.getMethod(), env, prefix, currentClass,
                    isOverloaded(method.getMethod()) ? getMethodCount(method.getJavaMethodName()) : 0);
            if (jMethod.requireJavaArrays()) {
                importJavaArraysModule = true;
            }
            if (modulesFlag) {
                importedPackages.addAll(jMethod.getImportedPackages());
            }
            methodList.add(jMethod);
        }
        methodList.sort(Comparator.comparing(JMethod::getMethodName));
    }

    private void populateFields(Field[] fields) {
        boolean addField = true;
        for (Field field : fields) {
            // To prevent the duplication of fields resulting from super classes.
            for (JField jField : fieldList) {
                if (jField.getFieldName().equals(field.getName())) {
                    addField = false;
                }
            }
            if (addField) {
                JField jFieldGetter = new JField(field, BFunction.BFunctionKind.FIELD_GET, env, this);
                fieldList.add(jFieldGetter);
                if (jFieldGetter.requireJavaArrays()) {
                    importJavaArraysModule = true;
                }
                if (!isFinalField(field) && isPublicField(field)) {
                    fieldList.add(new JField(field, BFunction.BFunctionKind.FIELD_SET, env, this));
                    if (modulesFlag) {
                        importedPackages.add(field.getDeclaringClass().getPackageName());
                    }
                }
            }
        }
    }

    private void populateImplementedInterfaces(Class[] interfaces) {
        for (Class interfaceClass : interfaces) {
            setAllClasses(getAlias(interfaceClass));
            superClasses.add(getAlias(interfaceClass));
            superClassNames.add(interfaceClass.getName());
            if (interfaceClass.getInterfaces() != null) {
                populateImplementedInterfaces(interfaceClass.getInterfaces());
            }
        }
    }

    private boolean isOverloaded(Method method) {
        boolean overloaded = false;
        int count = 0;
        for (Method m : getMethodsAsList(currentClass)) {
            if (m.getName().equals(method.getName())) {
                count++;
            }
        }
        if (count > 1) {
            overloaded = true;
        }
        return overloaded;
    }

    public String getShortClassName() {
        return shortClassName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setAccessModifier(String accessModifier) {
        this.accessModifier = accessModifier;
    }

    public void setMethodCount(String methodName) {
        Integer methodCount = overloadedMethods.get(methodName);
        if (methodCount == null) {
            overloadedMethods.put(methodName, 1);
        } else {
            overloadedMethods.replace(methodName, methodCount + 1);
        }
    }

    public Integer getMethodCount(String methodName) {
        return overloadedMethods.get(methodName);
    }

    public Class getCurrentClass() {
        return currentClass;
    }

    public boolean isImportJavaArraysModule() {
        return importJavaArraysModule;
    }

    public Set<String> getImportedPackages() {
        return importedPackages;
    }

    public List<JField> getFieldList() {
        return fieldList;
    }

    public List<JMethod> getMethodList() {
        return methodList;
    }

    public List<JConstructor> getConstructorList() {
        return constructorList;
    }

    public Map<String, String> getSuperClassPackage() {
        return superClassPackage;
    }
}
