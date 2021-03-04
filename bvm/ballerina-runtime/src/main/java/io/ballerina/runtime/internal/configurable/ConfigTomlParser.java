/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.configurable;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.configurable.exceptions.TomlException;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.ListInitialValueEntry;
import io.ballerina.runtime.internal.values.TableValueImpl;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlArrayValueNode;
import io.ballerina.toml.semantic.ast.TomlBasicValueNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlNode;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.runtime.internal.configurable.ConfigUtils.getEffectiveTomlType;
import static io.ballerina.runtime.internal.configurable.ConfigUtils.getLineRange;
import static io.ballerina.runtime.internal.configurable.ConfigUtils.getTomlTypeString;
import static io.ballerina.runtime.internal.configurable.ConfigUtils.isPrimitiveType;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.CONFIGURATION_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.CONFIG_FILE_NOT_FOUND;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.CONSTRAINT_TYPE_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.DEFAULT_MODULE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.EMPTY_CONFIG_FILE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.FIELD_TYPE_NOT_SUPPORTED;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_ADDITIONAL_FIELD_IN_RECORD;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_BYTE_RANGE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_MODULE_STRUCTURE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.INVALID_TOML_TYPE;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.REQUIRED_FIELD_NOT_PROVIDED;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.SUBMODULE_DELIMITER;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.TABLE_KEY_NOT_PROVIDED;
import static io.ballerina.runtime.internal.configurable.ConfigurableConstants.VALUE_NOT_PROVIDED;
import static io.ballerina.runtime.internal.util.RuntimeUtils.isByteLiteral;

/**
 * Toml parser for configurable implementation.
 *
 * @since 2.0.0
 */
public class ConfigTomlParser {

    private ConfigTomlParser() {
    }

    private static TomlTableNode getConfigurationData(Path configFilePath, Set<String> requiredModules) {
        boolean hasRequired = !requiredModules.isEmpty();
        TomlTableNode tomlNode = null;
        if (!Files.exists(configFilePath)) {
            if (hasRequired) {
                throw new TomlException(String.format(CONFIG_FILE_NOT_FOUND, configFilePath));
            } else {
                return tomlNode;
            }
        }
        ConfigToml configToml = new ConfigToml(configFilePath);
        tomlNode = configToml.tomlAstNode();
        if (tomlNode.entries().isEmpty() && hasRequired) {
            throw new TomlException(String.format(EMPTY_CONFIG_FILE, configFilePath));
        }
        return tomlNode;
    }

    public static void populateConfigMap(Path filePath, Map<Module, VariableKey[]> configurationData) {
        if (configurationData.isEmpty()) {
            return;
        }
        Set<String> requiredModules = getRequiredDetails(configurationData);
        TomlTableNode tomlNode = getConfigurationData(filePath, requiredModules);
        if (tomlNode == null) {
            // modules contain only defaultable configurables
            return;
        }
        for (Map.Entry<Module, VariableKey[]> moduleEntry : configurationData.entrySet()) {
            Module module = moduleEntry.getKey();
            String moduleName = module.getName();
            boolean hasRequired = requiredModules.contains(moduleName);
            TomlTableNode moduleNode = retrieveModuleNode(tomlNode, module, hasRequired);
            if (moduleNode == null) {
                //Module contains optional configurable variable
                continue;
            }
            checkAndPopulateVariables(moduleEntry.getValue(), moduleName, moduleNode);
        }
    }

    private static void checkAndPopulateVariables(VariableKey[] variableKeys, String moduleName,
                                                  TomlTableNode moduleNode) {
        for (VariableKey key : variableKeys) {
            String configVarName = key.variable;
            if (!moduleNode.entries().containsKey(configVarName)) {
                if (key.isRequired) {
                    configVarName =
                            (moduleName.equals(DEFAULT_MODULE)) ? configVarName : moduleName + ":" + configVarName;
                    throw new TomlException(
                            String.format(VALUE_NOT_PROVIDED, configVarName) + getLineRange(moduleNode));
                } else {
                    //It is an optional configurable variable
                    continue;
                }
            }
            ConfigurableMap.put(key, validateNodeAndExtractValue(key, moduleNode.entries()));
        }
    }

    private static Set<String> getRequiredDetails(Map<Module, VariableKey[]> configurationData) {
        Set<String> modules = new HashSet<>();
        for (Map.Entry<Module, VariableKey[]> moduleEntry : configurationData.entrySet()) {
            for (VariableKey key : moduleEntry.getValue()) {
                if (key.isRequired) {
                    modules.add(key.module.getName());
                }
            }
        }
        return modules;
    }

    private static TomlTableNode retrieveModuleNode(TomlTableNode tomlNode, Module module, boolean hasRequired) {
        String orgName = module.getOrg();
        String moduleName = module.getName();
        if (moduleName.equals(DEFAULT_MODULE)) {
            return tomlNode;
        }
        if (tomlNode.entries().containsKey(orgName)) {
            tomlNode = validateAndGetModuleStructure(tomlNode, orgName, orgName + SUBMODULE_DELIMITER + moduleName);
        }
        return extractModuleNode(tomlNode, moduleName, moduleName, hasRequired);
    }

    private static TomlTableNode validateAndGetModuleStructure(TomlTableNode tomlNode, String key, String moduleName) {
        TomlNode retrievedNode = tomlNode.entries().get(key);
        if (retrievedNode != null && retrievedNode.kind() != TomlType.TABLE) {
            throw new TomlException(String.format(INVALID_MODULE_STRUCTURE, moduleName, moduleName) +
                    getLineRange(retrievedNode));
        }
        return (TomlTableNode) retrievedNode;
    }

    private static TomlTableNode extractModuleNode(TomlTableNode orgNode, String moduleName, String fullModuleName,
                                                   boolean hasRequired) {
        if (orgNode == null) {
            return orgNode;
        }
        TomlTableNode moduleNode = orgNode;
        int subModuleIndex = moduleName.indexOf(SUBMODULE_DELIMITER);
        if (subModuleIndex == -1) {
            moduleNode = validateAndGetModuleStructure(orgNode, moduleName, fullModuleName);
            if (moduleNode == null && hasRequired) {
                throw new TomlException(String.format(INVALID_MODULE_STRUCTURE, fullModuleName, fullModuleName) +
                        getLineRange(orgNode));
            }
        } else if (subModuleIndex != moduleName.length()) {
            String parent = moduleName.substring(0, subModuleIndex);
            String submodule = moduleName.substring(subModuleIndex + 1);
            moduleNode = extractModuleNode(validateAndGetModuleStructure(moduleNode, parent, fullModuleName), submodule,
                    fullModuleName, hasRequired);
        }
        return moduleNode;
    }

    private static Object validateNodeAndExtractValue(VariableKey key, Map<String, TopLevelNode> valueMap) {
        String variableName = key.variable;
        Type type = key.type;
        TomlNode tomlValue =  valueMap.get(variableName);
        variableName = key.module.getName() + ":" + variableName;
        switch (type.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.BYTE_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.STRING_TAG:
                return retrievePrimitiveValue(tomlValue , variableName, type, "");
            case TypeTags.INTERSECTION_TAG:
                return retrieveComplexValue((BIntersectionType) type, tomlValue, variableName);
            default:
                throw new TomlException(String.format(CONFIGURATION_NOT_SUPPORTED, variableName, type.toString()) +
                        getLineRange(tomlValue));
        }
    }

    private static Object retrieveComplexValue(BIntersectionType type, TomlNode tomlValue, String variableName) {
        Type effectiveType = type.getEffectiveType();
        switch (effectiveType.getTag()) {
            case TypeTags.ARRAY_TAG:
                return retrieveArrayValues(tomlValue, variableName, (ArrayType) effectiveType, "");
            case TypeTags.RECORD_TYPE_TAG:
                return retrieveRecordValues(tomlValue, variableName, type);
            case TypeTags.TABLE_TAG:
                return retrieveTableValues(tomlValue, variableName, (TableType) effectiveType);
            default:
                throw new TomlException(
                        String.format(CONFIGURATION_NOT_SUPPORTED, variableName, effectiveType.toString()));
        }
    }

    private static Object retrievePrimitiveValue(TomlNode tomlValue, String variableName, Type type,
                                                 String errorPrefix) {
        TomlType tomlType = tomlValue.kind();
        if (tomlType != TomlType.KEY_VALUE) {
            throw new TomlException(errorPrefix + String.format(INVALID_TOML_TYPE,
                    variableName, type, getTomlTypeString(tomlValue)) + getLineRange(tomlValue));
        }
        TomlValueNode value = ((TomlKeyValueNode) tomlValue).value();
        tomlType = value.kind();
        if (tomlType != getEffectiveTomlType(type, variableName)) {
            throw new TomlException(errorPrefix + String.format(INVALID_TOML_TYPE,
                    variableName, type, getTomlTypeString(value)) + getLineRange(value));
        }
        return getBalValue(variableName, type.getTag(), value);
    }

    private static Object retrieveArrayValues(TomlNode tomlValue, String variableName,
                                              ArrayType effectiveType, String errorPrefix) {
        String lineRange = getLineRange(tomlValue);
        if (tomlValue.kind() != TomlType.KEY_VALUE) {
            throw new TomlException(errorPrefix + String.format(INVALID_TOML_TYPE,
                    variableName, effectiveType, getTomlTypeString(tomlValue)) + lineRange);
        }
        tomlValue = ((TomlKeyValueNode) tomlValue).value();
        if (tomlValue.kind() != getEffectiveTomlType(effectiveType, variableName)) {
            throw new TomlException(errorPrefix + String.format(INVALID_TOML_TYPE,
                    variableName, effectiveType, getTomlTypeString(tomlValue)) + lineRange);
        }
        Type elementType = effectiveType.getElementType();
        List<TomlValueNode> arrayList = ((TomlArrayValueNode) tomlValue).elements();
        if (!isPrimitiveType(elementType.getTag())) {
            //Remove after supporting all arrays
            throw new TomlException(
                    String.format(CONFIGURATION_NOT_SUPPORTED, variableName, effectiveType.toString()) + lineRange);
        }
        return new ArrayValueImpl(effectiveType, arrayList.size(), createArray(variableName, arrayList, elementType));
    }

    private static ListInitialValueEntry.ExpressionEntry[] createArray(String variableName,
                                                                       List<TomlValueNode> arrayList,
                                                                       Type elementType) {
        int arraySize = arrayList.size();
        ListInitialValueEntry.ExpressionEntry[] arrayEntries =
                new ListInitialValueEntry.ExpressionEntry[arraySize];
        for (int i = 0; i < arraySize; i++) {
            String elementName = variableName + "[" + i + "]";
            TomlNode tomlNode = arrayList.get(i);
            if (tomlNode.kind() != getEffectiveTomlType(elementType, elementName)) {
                throw new TomlException(String.format(INVALID_TOML_TYPE, elementName, elementType,
                        getTomlTypeString(tomlNode)) + getLineRange(tomlNode));
            }
            arrayEntries[i] = new ListInitialValueEntry.ExpressionEntry(
                    getBalValue(variableName, elementType.getTag(), arrayList.get(i)));
        }
        return arrayEntries;
    }

    private static Object retrieveRecordValues(TomlNode tomlNode, String variableName,
                                               BIntersectionType intersectionType) {
        Type effectiveType = intersectionType.getEffectiveType();
        String valueLineRange = getLineRange(tomlNode);
        if (effectiveType.getTag() != TypeTags.RECORD_TYPE_TAG) {
            throw new TomlException(
                    String.format(CONFIGURATION_NOT_SUPPORTED, intersectionType, variableName) + valueLineRange);
        }
        if (tomlNode.kind() != getEffectiveTomlType(effectiveType, variableName)) {
            throw new TomlException(String.format(INVALID_TOML_TYPE, variableName, effectiveType ,
                    getTomlTypeString(tomlNode)) + valueLineRange);
        }
        RecordType recordType = (RecordType) intersectionType.getConstituentTypes().get(0);
        Map<String, Object> initialValueEntries = new HashMap<>();
        for (Map.Entry<String, TopLevelNode> tomlField : ((TomlTableNode) tomlNode).entries().entrySet()) {
            String fieldName = tomlField.getKey();
            TomlNode value = tomlField.getValue();
            Field field = recordType.getFields().get(fieldName);
            String lineRange = getLineRange(value);
            if (field == null) {
                throw new TomlException(String.format(INVALID_ADDITIONAL_FIELD_IN_RECORD,
                        fieldName , variableName, recordType.toString()) + lineRange);
            }
            Type fieldType = field.getFieldType();
            if (!isSupportedType(fieldType)) {
                throw new TomlException(String.format(FIELD_TYPE_NOT_SUPPORTED, fieldType, variableName) + lineRange);
            }
            Object objectValue;
            String errorPrefix = "field '" + fieldName + "' from ";
            switch (fieldType.getTag()) {
                case TypeTags.ARRAY_TAG:
                    objectValue = retrieveArrayValues(value, variableName, (ArrayType) fieldType, errorPrefix);
                    break;
                case TypeTags.INTERSECTION_TAG:
                    ArrayType arrayType = (ArrayType) ((IntersectionType) fieldType).getEffectiveType();
                    objectValue = retrieveArrayValues(value, variableName, arrayType, errorPrefix);
                    break;
                default:
                    objectValue = retrievePrimitiveValue(value, variableName, fieldType, errorPrefix);
            }
            initialValueEntries.put(fieldName, objectValue);
        }
        validateRequiredField(initialValueEntries, recordType, variableName, valueLineRange);
        return ValueCreator
                .createReadonlyRecordValue(recordType.getPackage(), recordType.getName(), initialValueEntries);
    }

    private static void validateRequiredField(Map<String, Object> initialValueEntries, RecordType recordType,
                                              String variableName, String lineRange) {
        for (Map.Entry<String, Field> field : recordType.getFields().entrySet()) {
            String fieldName = field.getKey();
            if (SymbolFlags.isFlagOn(field.getValue().getFlags(), SymbolFlags.REQUIRED) &&
                    initialValueEntries.get(fieldName) == null) {
                throw new TomlException(String.format(REQUIRED_FIELD_NOT_PROVIDED, fieldName, recordType.toString(),
                        variableName) + lineRange);
            }
        }
    }

    private static boolean isSupportedType(Type type) {
        //Remove this check when we support all field types
        int typeTag = type.getTag();
        if (typeTag == TypeTags.INTERSECTION_TAG) {
            Type effectiveType = ((IntersectionType) type).getEffectiveType();
            if (effectiveType.getTag() != TypeTags.ARRAY_TAG) {
                return false;
            }
            typeTag = ((ArrayType) ((IntersectionType) type).getEffectiveType()).getElementType().getTag();
        } else if (typeTag == TypeTags.ARRAY_TAG) {
            typeTag = ((ArrayType) type).getElementType().getTag();
        }
        return isPrimitiveType(typeTag);
    }

    private static Object retrieveTableValues(TomlNode tomlValue, String variableName,
                                              TableType tableType) {
        Type constraintType = tableType.getConstrainedType();
        String lineRange = getLineRange(tomlValue);
        if (constraintType.getTag() != TypeTags.INTERSECTION_TAG) {
            throw new TomlException(String.format(CONSTRAINT_TYPE_NOT_SUPPORTED, constraintType, variableName) +
                    lineRange);
        }
        if (((BIntersectionType) constraintType).getEffectiveType().getTag() != TypeTags.RECORD_TYPE_TAG) {
            throw new TomlException(String.format(CONSTRAINT_TYPE_NOT_SUPPORTED, constraintType, variableName) +
                    lineRange);
        }
        if (tomlValue.kind() != getEffectiveTomlType(tableType, variableName)) {
            throw new TomlException(String.format(INVALID_TOML_TYPE, variableName, tableType ,
                    getTomlTypeString(tomlValue)) + lineRange);
        }
        List<TomlTableNode> tableNodeList = ((TomlTableArrayNode) tomlValue).children();
        int tableSize = tableNodeList.size();
        ListInitialValueEntry.ExpressionEntry[] tableEntries = new ListInitialValueEntry.ExpressionEntry[tableSize];
        String[] keys = tableType.getFieldNames();
        for (int i = 0; i < tableSize; i++) {
            if (keys != null) {
                validateKeyField(tableNodeList.get(i), keys, tableType, variableName);
            }
            Object value = retrieveRecordValues(tableNodeList.get(i), variableName, (BIntersectionType) constraintType);
            tableEntries[i] = new ListInitialValueEntry.ExpressionEntry(value);
        }
        ArrayValue tableData =
                new ArrayValueImpl(TypeCreator.createArrayType(constraintType), tableSize, tableEntries);
        ArrayValue keyNames = keys == null ? (ArrayValue) ValueCreator.createArrayValue(new BString[]{}) :
                (ArrayValue) StringUtils.fromStringArray(keys);
        return new TableValueImpl<>((BTableType) tableType, tableData, keyNames);
    }

    private static void validateKeyField(TomlTableNode recordTable, String[] fieldNames, Type tableType,
                                         String variableName) {
        for (String key : fieldNames) {
            if (recordTable.entries().get(key) == null) {
                throw new TomlException(String.format(TABLE_KEY_NOT_PROVIDED, key, tableType.toString(),
                        variableName) + getLineRange(recordTable));
            }
        }
    }

    private static Object getBalValue(String variableName, int typeTag, TomlValueNode tomlValueNode) {
        Object tomlValue = ((TomlBasicValueNode<?>) tomlValueNode).getValue();
        if (typeTag == TypeTags.BYTE_TAG) {
            int value = ((Long) tomlValue).intValue();
            if (!isByteLiteral(value)) {
                throw new TomlException(
                        String.format(INVALID_BYTE_RANGE, variableName, value) + getLineRange(tomlValueNode));
            }
            return value;
        }
        if (typeTag == TypeTags.DECIMAL_TAG) {
            return ValueCreator.createDecimalValue(BigDecimal.valueOf((Double) tomlValue));
        }
        if (typeTag == TypeTags.STRING_TAG) {
            String stringVal = (String) tomlValue;
            ConfigSecurityUtils.handleEncryptedValues(variableName, stringVal);
            return StringUtils.fromString(stringVal);
        }
        return tomlValue;
    }
}
