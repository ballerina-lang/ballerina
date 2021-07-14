/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter;

import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.OutputEventArgumentsCategory;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;

/**
 * Wrapper implementation for the DAP `client/output` endpoint, which can be used to send debug server and target VM
 * outputs to the debugger client side.
 *
 * @since 2.0.0
 */
public class DebugOutputLogger {

    private final IDebugProtocolClient client;

    public DebugOutputLogger(IDebugProtocolClient client) {
        this.client = client;
    }

    /**
     * Processes messages which should treated as the program output.
     *
     * @param output string output produced in the debugger/target VM.
     */
    public void sendProgramOutput(String output) {
        if (isInternalLog(output)) {
            return;
        }
        if (!output.endsWith(System.lineSeparator())) {
            output += System.lineSeparator();
        }
        OutputEventArguments outputEventArguments = new OutputEventArguments();
        outputEventArguments.setOutput(output);
        outputEventArguments.setCategory(OutputEventArgumentsCategory.STDOUT);
        client.output(outputEventArguments);
    }

    /**
     * Processes messages which should treated as the console output.
     *
     * @param output string output produced in the debugger/target VM.
     */
    public void sendConsoleOutput(String output) {
        if (isInternalLog(output)) {
            return;
        }
        if (!output.endsWith(System.lineSeparator())) {
            output += System.lineSeparator();
        }
        OutputEventArguments outputArguments = new OutputEventArguments();
        outputArguments.setOutput(output);
        // Since Ballerina compiler logs and errors are redirected to the same stream (STDERR) by design, output
        // category has to be derived based on the output prefix.
        if (containsBalErrorPrefix(output)) {
            outputArguments.setCategory(OutputEventArgumentsCategory.STDERR);
        } else {
            outputArguments.setCategory(OutputEventArgumentsCategory.CONSOLE);
        }
        client.output(outputArguments);
    }

    /**
     * Processes messages which should treated as the error output.
     *
     * @param output string output produced in the debugger/target VM.
     */
    public void sendErrorOutput(String output) {
        if (isInternalLog(output)) {
            return;
        }
        if (!output.endsWith(System.lineSeparator())) {
            output += System.lineSeparator();
        }
        OutputEventArguments outputEventArguments = new OutputEventArguments();
        outputEventArguments.setOutput(output);
        outputEventArguments.setCategory(OutputEventArgumentsCategory.STDERR);
        client.output(outputEventArguments);
    }

    private static boolean containsBalErrorPrefix(String output) {
        // Todo: Add more error patterns
        return output.startsWith("ERROR") || output.startsWith("error:");
    }

    private static boolean isInternalLog(String output) {
        return output.startsWith("Listening for transport dt_socket")
                || output.startsWith("Please start the remote debugging client to continue")
                || output.startsWith("JAVACMD")
                || output.startsWith("Stream closed");
    }
}
