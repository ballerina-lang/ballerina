/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.providers;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.compiler.common.LSDocumentIdentifierImpl;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Code Action provider for importing a package.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ImportModuleCodeAction extends AbstractCodeActionProvider {
    private static final String UNDEFINED_FUNCTION = "undefined function";
    private static final String UNDEFINED_MODULE = "undefined module";
    private static final String UNRESOLVED_MODULE = "cannot resolve module";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> diagnosticsOfRange,
                                                    List<Diagnostic> allDiagnostics) {
        WorkspaceDocumentManager documentManager = lsContext.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        Optional<Path> filePath = CommonUtil.getPathFromURI(lsContext.get(DocumentServiceKeys.FILE_URI_KEY));
        LSDocumentIdentifier document = null;
        try {
            document = documentManager.getLSDocument(filePath.get());
        } catch (WorkspaceDocumentException e) {
            // ignore
        }
        List<CodeAction> actions = new ArrayList<>();

        if (document == null) {
            return actions;
        }
        List<String> missingImportMsgs = new ArrayList<>();
        for (Diagnostic diagnostic : diagnosticsOfRange) {
            String message = diagnostic.getMessage();
            if (message.startsWith(UNDEFINED_MODULE) && !missingImportMsgs.contains(message)) {
                missingImportMsgs.add(message);
            }
        }
        missingImportMsgs.forEach(msg -> {
            List<CodeAction> codeActions = getModuleImportCommand(msg, lsContext);
            actions.addAll(codeActions);
        });
        return actions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                                    List<Diagnostic> allDiagnostics) {
        throw new UnsupportedOperationException("Not supported");
    }

    private static List<CodeAction> getModuleImportCommand(String diagnosticMessage, LSContext context) {
        List<CodeAction> actions = new ArrayList<>();
        String uri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        List<Diagnostic> diagnostics = new ArrayList<>();

        String packageAlias = diagnosticMessage.substring(diagnosticMessage.indexOf("'") + 1,
                                                          diagnosticMessage.lastIndexOf("'"));
        LSDocumentIdentifier sourceDocument = new LSDocumentIdentifierImpl(uri);
        String sourceRoot = LSCompilerUtil.getProjectRoot(sourceDocument.getPath());
        sourceDocument.setProjectRootRoot(sourceRoot);
        List<BallerinaPackage> packagesList = new ArrayList<>();
        packagesList.addAll(LSPackageLoader.getSdkPackages());
        packagesList.addAll(LSPackageLoader.getHomeRepoPackages());

        BLangPackage bLangPackage = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        packagesList.addAll(LSPackageLoader.getCurrentProjectModules(bLangPackage, context));

        packagesList.stream()
                .filter(pkgEntry -> {
                    String fullPkgName = pkgEntry.getFullPackageNameAlias();
                    return fullPkgName.endsWith("." + packageAlias) || fullPkgName.endsWith("/" + packageAlias);
                })
                .forEach(pkgEntry -> {
                    String commandTitle = String.format(CommandConstants.IMPORT_MODULE_TITLE,
                                                        pkgEntry.getFullPackageNameAlias());
                    CodeAction action = new CodeAction(commandTitle);
                    Position insertPos = getImportPosition(bLangPackage, context);
                    String importText = ItemResolverConstants.IMPORT + " " + pkgEntry.getFullPackageNameAlias()
                            + CommonKeys.SEMI_COLON_SYMBOL_KEY + CommonUtil.LINE_SEPARATOR;
                    List<TextEdit> edits = Collections.singletonList(
                            new TextEdit(new Range(insertPos, insertPos), importText));
                    action.setKind(CodeActionKind.QuickFix);
                    action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                            new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, null), edits)))));
                    action.setDiagnostics(diagnostics);
                    actions.add(action);
                });
        return actions;
    }

    private static Position getImportPosition(BLangPackage bLangPackage, LSContext context) {
        // Calculate initial import insertion line
        List<TopLevelNode> nodes = CommonUtil.getCurrentFileTopLevelNodes(bLangPackage, context);
        int lowestLine = 1;
        if (!nodes.isEmpty()) {
            lowestLine = nodes.get(0).getPosition().getStartLine();
            for (TopLevelNode node : nodes) {
                org.ballerinalang.util.diagnostic.Diagnostic.DiagnosticPosition position = node.getPosition();
                if (lowestLine > position.getStartLine()) {
                    lowestLine = position.getStartLine();
                }
            }
        }

        // Filter the imports except the runtime import
        context.put(DocumentServiceKeys.CURRENT_DOC_IMPORTS_KEY, CommonUtil.getCurrentFileImports(context));
        List<BLangImportPackage> imports = CommonUtil.getCurrentFileImports(context);

        Position insertPos = new Position(lowestLine - 1, 0);
        if (!imports.isEmpty()) {
            BLangImportPackage last = CommonUtil.getLastItem(imports);
            insertPos = new Position(last.getPosition().getEndLine(), 0);
        }
        return insertPos;
    }
}
