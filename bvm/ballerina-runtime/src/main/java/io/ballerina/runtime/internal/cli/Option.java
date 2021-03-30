/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.runtime.internal.cli;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents the option passed via the cli.
 */
public class Option {
    private static final String NAMED_ARG_DELIMITER = "=";

    private final RecordType recordType;
    private final BMap<BString, Object> record;
    private final List<String> operandArgs;
    private final Set<BString> recordKeysFound;

    public Option(Type recordType) {
        this((RecordType) recordType,
             ValueCreator.createRecordValue(recordType.getPackage(), recordType.getName()));
    }

    public Option(RecordType recordType, BMap<BString, Object> record) {
        this.recordType = recordType;
        this.record = record;
        operandArgs = new ArrayList<>();
        recordKeysFound = new HashSet<>();
    }

    public BMap<BString, Object> parseRecord(String[] args) {
        // Todo: Improve error messages
        int index = 0;
        while (index < args.length) {
            String arg = args[index++];
            if (CliUtil.isLongOption(arg)) {
                index = handleOption(args, index, arg);
            } else if (isShortOption(arg)) {
                validConfigOption(arg);
            } else {
                operandArgs.add(arg);
            }
        }
        validateRecordKeys();
        return record;
    }

    private boolean isShortOption(String arg) {
        return arg.startsWith("-");
    }

    private void validConfigOption(String arg) {
        // Skip the config options
        if (arg.length() == 1 || arg.charAt(1) != 'C') {
            throw ErrorCreator.createError(
                    StringUtils.fromString("undefined CLI argument: '" + arg + "'"));

        }
    }

    private int handleOption(String[] args, int index, String optionStr) {
        // Handle the case when there's only -- not followed by a string
        if (optionStr.equals("--")) {
            return addOperands(args, index);
        }
        BString optionName = StringUtils.fromString(getOptionName(optionStr));
        validateFieldExists(optionName);
        recordKeysFound.add(optionName);
        if (isNamedArg(optionStr)) {
            processNamedArg(optionStr, optionName);
        } else {
            if (!handleBooleanTrue(optionName)) {
                if (index < args.length) {
                    handleOptionArgument(args[index++], optionStr, optionName);
                } else {
                    // case where the option argument is missing
                    validateOptionArgument(optionStr, optionStr);
                }
            }
        }
        return index;
    }

    private void handleOptionArgument(String val, String optionStr, BString optionName) {
        Type fieldType = recordType.getFields().get(optionName.getValue()).getFieldType();
        validateOptionArgument(optionStr, val);
        if (fieldType.getTag() == TypeTags.ARRAY_TAG) {
            handleArrayParameter(optionName, val, (ArrayType) fieldType);
        } else {
            record.put(optionName, CliUtil.getBValueWithUnionValue(fieldType, val));
        }
    }

    private int addOperands(String[] args, int index) {
        while (index < args.length) {
            operandArgs.add(args[index++]);
        }
        return index;
    }

    private boolean handleBooleanTrue(BString paramName) {
        Type fieldType = recordType.getFields().get(paramName.getValue()).getFieldType();
        if (isABoolean(fieldType)) {
            record.put(paramName, true);
            return true;
        } else if (fieldType.getTag() == TypeTags.ARRAY_TAG) {
            BArray bArray = getBArray(paramName, (ArrayType) fieldType);
            Type elementType = bArray.getElementType();
            if (isABoolean(elementType)) {
                bArray.append(true);
                return true;
            }
        }
        return false;
    }

    private void validateRecordKeys() {
        // Todo: test this as a unit test
        for (BString key : record.getKeys()) {
            if (!recordKeysFound.contains(key) && isRequired(recordType, key.getValue())) {
                Type fieldType = recordType.getFields().get(key.getValue()).getFieldType();
                if (CliUtil.isUnionWithNil(fieldType) || isSupportedArrayType(key, fieldType) ||
                        handleBooleanFalse(key, fieldType)) {
                    continue;
                }
                throw ErrorCreator.createError(
                        StringUtils.fromString("Missing required option argument: '" + key + "'"));
            }
        }
    }

    private boolean handleBooleanFalse(BString key, Type fieldType) {
        if (isABoolean(fieldType)) {
            record.put(key, false);
            return true;
        }
        return false;
    }

    private boolean isSupportedArrayType(BString key, Type fieldType) {
        if (fieldType.getTag() == TypeTags.ARRAY_TAG) {
            BArray bArray = getBArray(key, (ArrayType) fieldType);
            Type elementType = bArray.getElementType();
            if (CliUtil.isSupportedType(elementType.getTag())) {
                if (record.get(key) == null) {
                    record.put(key, bArray);
                }
                return true;
            }
            throw CliUtil.getUnsupportedTypeException(fieldType);
        }
        return false;
    }

    private boolean isRequired(RecordType recordType, String fieldName) {
        return SymbolFlags.isFlagOn(recordType.getFields().get(fieldName).getFlags(), SymbolFlags.REQUIRED);
    }

    private void validateOptionArgument(String arg, String val) {
        if (CliUtil.isLongOption(val)) {
            throw ErrorCreator.createError(StringUtils.fromString("Missing option argument for '" + arg + "'"));
        }
    }

    private BArray getBArray(BString paramName, ArrayType fieldType) {
        BArray bArray = (BArray) record.get(paramName);
        if (bArray == null) {
            bArray = ValueCreator.createArrayValue(fieldType, -1);
            record.put(paramName, bArray);
        }
        return bArray;
    }

    private boolean isABoolean(Type fieldType) {
        return fieldType.getTag() == TypeTags.BOOLEAN_TAG;
    }

    private void processNamedArg(String arg, BString paramName) {
        String val = getValueString(arg);
        Type fieldType = recordType.getFields().get(paramName.getValue()).getFieldType();
        if (fieldType.getTag() == TypeTags.ARRAY_TAG) {
            handleArrayParameter(paramName, val, (ArrayType) fieldType);
            return;
        }
        record.put(paramName, CliUtil.getBValueWithUnionValue(fieldType, val));
    }

    private void handleArrayParameter(BString paramName, String val, ArrayType fieldType) {
        BArray bArray = getBArray(paramName, fieldType);
        Type arrayType = bArray.getElementType();
        bArray.append(CliUtil.getBValue(arrayType, val));
    }

    private void validateFieldExists(BString recordKey) {
        if (!(record.containsKey(recordKey) || recordType.getFields().containsKey(recordKey.getValue()))) {
            throw ErrorCreator.createError(
                    StringUtils.fromString("undefined option: '" + recordKey + "'"));
        }
    }

    private String getValueString(String arg) {
        return arg.split(NAMED_ARG_DELIMITER, 2)[1];
    }

    private boolean isNamedArg(String arg) {
        return arg.contains(NAMED_ARG_DELIMITER);
    }

    private String getOptionName(String arg) {
        return arg.split(NAMED_ARG_DELIMITER, 2)[0].substring(2).trim();
    }

    public List<String> getOperandArgs() {
        return operandArgs;
    }
}
