/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.command.executors;

import com.google.gson.internal.LinkedTreeMap;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.command.LSCommandExecutor;
import org.ballerinalang.langserver.command.LSCommandExecutorException;
import org.ballerinalang.langserver.command.docs.DocAttachmentInfo;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSCompilerException;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import static org.ballerinalang.langserver.command.CommandUtil.applySingleTextEdit;
import static org.ballerinalang.langserver.command.docs.DocumentationGenerator.getDocumentationEditForNodeByPosition;

/**
 * Command executor for adding single documentation.
 * 
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.command.LSCommandExecutor")
public class AddDocumentationExecutor implements LSCommandExecutor {

    private static final String COMMAND = "ADD_DOC";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {
        String nodeType = "";
        String documentUri;
        int line = 0;
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();
        for (Object arg : context.get(ExecuteCommandKeys.COMMAND_ARGUMENTS_KEY)) {
            if (((LinkedTreeMap) arg).get(ARG_KEY).equals(CommandConstants.ARG_KEY_DOC_URI)) {
                documentUri = (String) ((LinkedTreeMap) arg).get(ARG_VALUE);
                textDocumentIdentifier.setUri(documentUri);
                context.put(DocumentServiceKeys.FILE_URI_KEY, documentUri);
            } else if (((LinkedTreeMap) arg).get(ARG_KEY).equals(CommandConstants.ARG_KEY_NODE_TYPE)) {
                nodeType = (String) ((LinkedTreeMap) arg).get(ARG_VALUE);
            } else if (((LinkedTreeMap) arg).get(ARG_KEY).equals(CommandConstants.ARG_KEY_NODE_LINE)) {
                line = Integer.parseInt((String) ((LinkedTreeMap) arg).get(ARG_VALUE));
            }
        }

        BLangPackage bLangPackage = null;
        try {
            WorkspaceDocumentManager documentManager = context.get(ExecuteCommandKeys.DOCUMENT_MANAGER_KEY);
            LSCompiler lsCompiler = context.get(ExecuteCommandKeys.LS_COMPILER_KEY);
            bLangPackage = lsCompiler.getBLangPackage(context, documentManager, false,
                                                      LSCustomErrorStrategy.class, false);
        } catch (LSCompilerException e) {
            throw new LSCommandExecutorException("Couldn't compile the source", e);
        }

        String relativeSourcePath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        context.put(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY, bLangPackage);
        BLangPackage srcOwnerPkg = CommonUtil.getSourceOwnerBLangPackage(relativeSourcePath, bLangPackage);

        DocAttachmentInfo docAttachmentInfo = getDocumentationEditForNodeByPosition(nodeType, srcOwnerPkg, line);

        if (docAttachmentInfo == null) {
            return new Object();
        }

        Range range = new Range(docAttachmentInfo.getDocStartPos(), docAttachmentInfo.getDocStartPos());

        return applySingleTextEdit(docAttachmentInfo.getDocAttachment(), range, textDocumentIdentifier,
                                   context.get(ExecuteCommandKeys.LANGUAGE_SERVER_KEY).getClient());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
