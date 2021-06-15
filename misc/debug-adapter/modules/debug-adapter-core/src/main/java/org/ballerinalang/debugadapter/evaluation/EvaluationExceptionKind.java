/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.evaluation;

/**
 * Ballerina expression evaluation related exception kinds.
 *
 * @since 2.0.0
 */
public enum EvaluationExceptionKind {

    FUNCTION_NOT_FOUND("Undefined function '%s'"),
    CLASS_NOT_FOUND("Undefined class: %s"),
    OBJECT_METHOD_NOT_FOUND("Undefined method '%s' in object `%s`"),
    REMOTE_METHOD_NOT_FOUND("Undefined remote method '%s' in object `%s`"),
    LANG_LIB_METHOD_NOT_FOUND("Undefined function '%s' in type '%s'"),
    LANG_LIB_NOT_FOUND("Failed to load lang library: '%s'"),
    FIELD_NOT_FOUND("Undefined field '%s' in `%s`"),
    VARIABLE_NOT_FOUND("Undefined variable '%s'"),
    HELPER_UTIL_NOT_FOUND("Failed to load the evaluation helper util method: '%s'"),
    FUNCTION_EXECUTION_ERROR("Error occurred when executing method: '%s'"),
    TYPE_RESOLVING_ERROR("Failed to resolve type: '%s'"),
    STRAND_NOT_FOUND("Failed to resolve the active Ballerina strand instance"),
    METHOD_STRAND_NOT_FOUND("Failed to resolve the active Ballerina strand instance for executing the method: %s"),
    INVALID_ARGUMENT("Unsupported/invalid argument found: %s"),
    TYPE_MISMATCH("Incompatible types: expected `%s`, but found '%s': in '%s'"),
    VARIABLE_EXECUTION_ERROR("Internal error occurred when processing variable: '%s'"),
    BLOCK_EVALUATION("Block expressions/statements are not supported"),
    EMPTY("Empty expressions cannot be evaluated."),
    INVALID("Invalid expression: '%s'"),
    SYNTAX_ERROR("Syntax errors found: " + System.lineSeparator() + "%s"),
    COMPILATION_ERRORS("Compilation error(s) found: " + System.lineSeparator() + "%s"),
    UNSUPPORTED_EXPRESSION("Following expressions/sub-expressions are not supported by the evaluator: " +
            System.lineSeparator() + "%s"),
    UNSUPPORTED_OPERATION("'%s' operation is not supported for types: '%s' and '%s'"),
    INTERNAL_ERROR("Internal error occurred while evaluating the expression: '%s'"),
    CUSTOM_ERROR("%s");

    public static final String PREFIX = "Failed to evaluate." + System.lineSeparator() + "Reason: ";
    private final String value;

    EvaluationExceptionKind(String value) {
        this.value = value;
    }

    public String getString() {
        return PREFIX + this.value;
    }

    public String getReason() {
        return this.value;
    }
}
