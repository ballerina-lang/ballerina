/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.services;

import org.ballerinalang.bre.CallableUnitInfo;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.ControlStack;
import org.ballerinalang.bre.StackFrame;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.nio.file.Paths;
import java.util.Stack;

/**
 * Class contains utility methods for ballerina server error handling.
 */
public class ErrorHandlerUtils {

    private static final int STACK_TRACE_LIMIT = 20;

    /**
     * Get the error message of a throwable.
     *
     * @param throwable Throwable
     * @return Error message
     */
    public static String getErrorMessage(Throwable throwable) {
        String errorMsg;
        String errorPrefix = "error in ballerina program: ";
        if (throwable instanceof StackOverflowError) {
            errorMsg = "fatal " + errorPrefix + "stack overflow ";
        } else if (throwable.getMessage() != null) {
            errorMsg = errorPrefix + makeFirstLetterLowerCase(throwable.getMessage());
        } else {
            errorMsg = errorPrefix;
        }
        return errorMsg;
    }

    /**
     * Get the ballerina stack trace from context.
     *
     * @param context Ballerina context
     * @param throwable Throwable associated with the error
     * @return Ballerina stack trace
     */
    public static String getServiceStackTrace(Context context, Throwable throwable) {
        if (context == null && throwable instanceof BallerinaException) {
            context = ((BallerinaException) throwable).getContext();
        }

        // if the context is null, stack trace cannot be generated
        if (context == null) {
            return throwable.getMessage();
        }

        return getStackTrace(context, throwable, 1);
    }

    /**
     * Get the ballerina stack trace for a main function.
     *
     * @param context Ballerina context associated with the main function
     * @param throwable Throwable associated with the error
     * @return Ballerina stack trace
     */
    public static String getMainFuncStackTrace(Context context, Throwable throwable) {
        // Need to omit the main function invocation from the stack trace. Hence the starting index is 1
        return getStackTrace(context, throwable, 1);
    }

    /**
     * Get the stack trace from the context.
     *
     * @param context         Ballerina context
     * @param throwable       Throwable associated with the error occurred
     * @param stackStartIndex Start index of the stack to generate the stack trace
     * @return Stack trace
     */
    private static String getStackTrace(Context context, Throwable throwable, int stackStartIndex) {
        ControlStack controlStack = context.getControlStack();
        StringBuilder sb = new StringBuilder();
        Stack<StackFrame> stack = controlStack.getStack();

        if (throwable instanceof StackOverflowError) {
            populateStackOverflowTrace(sb, stack, stackStartIndex);
        } else if (stack.size() > 0) {
            for (int i = stack.size() - 1; i >= stackStartIndex; i--) {
                CallableUnitInfo frameInfo = stack.get(i).getNodeInfo();
                String enclosingFramName;
                String enclosingFramPkg;
                if (i == 0) {
                    enclosingFramName = frameInfo.getName();
                    enclosingFramPkg = frameInfo.getPackage();
                } else {
                    enclosingFramName = stack.get(i - 1).getNodeInfo().getName();
                    enclosingFramPkg = stack.get(i - 1).getNodeInfo().getPackage();
                }
                String pkgName = (enclosingFramPkg != null) ? enclosingFramPkg + ":" : "";
                sb.append("\t at ").append(pkgName).append(enclosingFramName)
                        .append(getNodeLocation(frameInfo)).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Get the source location as string in the format of '(fileName:lineNumber)'.
     *
     * @param nodeInfo {@link CallableUnitInfo} to get the location
     * @return source location of this {@link CallableUnitInfo}
     */
    private static String getNodeLocation(CallableUnitInfo nodeInfo) {
        NodeLocation nodeLocation = nodeInfo.getNodeLocation();
        if (nodeLocation != null) {
            String fileName = Paths.get(nodeLocation.getFileName()).getFileName().toString();
            int line = nodeLocation.getLineNumber();
            return "(" + fileName + ":" + line + ")";
        } else {
            return "";
        }
    }

    /**
     * Populate the stack trace of a stack overflow error.
     *
     * @param sb    String buffer to populate the stack trace
     * @param stack Current stack
     */
    private static void populateStackOverflowTrace(StringBuilder sb, Stack<StackFrame> stack, int stackStartIndex) {
        if (stack.size() == 0) {
            return;
        }
        
        for (int i = stack.size() - 1; i >= stack.size() - STACK_TRACE_LIMIT; i--) {
            CallableUnitInfo frameInfo = stack.get(i).getNodeInfo();
            String pkgName = (frameInfo.getPackage() != null) ? frameInfo.getPackage() + ":" : "";
            sb.append("\t at " + pkgName + frameInfo.getName() + getNodeLocation(frameInfo)
                    + "\n");
        }
        sb.append("\t ...\n\t ...\n");
        for (int i = STACK_TRACE_LIMIT + stackStartIndex - 1; i >= stackStartIndex; i--) {
            CallableUnitInfo frameInfo = stack.get(i).getNodeInfo();
            String pkgName = (frameInfo.getPackage() != null) ? frameInfo.getPackage() + ":" : "";
            sb.append("\t at " + pkgName + frameInfo.getName() + getNodeLocation(frameInfo)
                    + "\n");
        }
    }

    private static String makeFirstLetterLowerCase(String s) {
        char c[] = s.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }
}
