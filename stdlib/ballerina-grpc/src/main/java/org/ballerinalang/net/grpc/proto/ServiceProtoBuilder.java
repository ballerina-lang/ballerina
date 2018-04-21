/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc.proto;

import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.proto.definition.File;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.proto.ServiceProtoConstants.ANN_MESSAGE_LISTENER;

/**
 * This class validates annotations attached to Ballerina service and resource nodes.
 *
 * @since 1.0
 */
@SupportedAnnotationPackages(
        value = PROTOCOL_STRUCT_PACKAGE_GRPC
)
public class ServiceProtoBuilder extends AbstractCompilerPlugin {

    private boolean canProcess;
    private DiagnosticLog dlog;
    private Map<String, File> serviceFileMap = new HashMap<>();
    private static final PrintStream error = System.err;

    private SymbolResolver symResolver;
    private SymbolTable symTable;
    private Names names;

    @Override
    public void setCompilerContext(CompilerContext context) {
        this.symResolver = SymbolResolver.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
    }

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dlog = diagnosticLog;
        this.canProcess = false;
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        for (AnnotationAttachmentNode annotationNode : annotations) {
            if (ANN_MESSAGE_LISTENER.equals(annotationNode.getAnnotationName().getValue())) {
                return;
            }
        }

        try {
            File fileDefinition = ServiceProtoUtils.generateProtoDefinition(serviceNode);
            ServiceProtoUtils.writeServiceFiles(Paths.get("."), serviceNode.getName().getValue(), fileDefinition);
            serviceFileMap.put(serviceNode.getName().getValue(), fileDefinition);
        } catch (GrpcServerException e) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(), e.getMessage());
        }
        addDescriptorAnnotation(serviceNode);
    }

    @Override
    public void codeGenerated(Path binaryPath) {
        if (canProcess) {
            if (binaryPath == null) {
                error.print("Error while generating service proto file. Binary file path is null");
                return;
            }
            Path filePath = binaryPath.toAbsolutePath();
            Path parentDirPath = filePath.getParent();
            if (parentDirPath == null) {
                parentDirPath = filePath;
            }
            Path targetDirPath = Paths.get(parentDirPath.toString(), "grpc");
            for (Map.Entry<String, File> entry : serviceFileMap.entrySet()) {
                try {
                    ServiceProtoUtils.writeServiceFiles(targetDirPath, entry.getKey(), entry.getValue());
                } catch (GrpcServerException e) {
                    error.print("Error while generating service proto file. " + e.getMessage());
                }
            }
        }
    }

    private void addDescriptorAnnotation(ServiceNode serviceNode) {
        BLangService service = (BLangService) serviceNode;
        DiagnosticPos pos = service.pos;

        // Create Annotation Attachment.
        BLangAnnotationAttachment annoAttachment = (BLangAnnotationAttachment) TreeBuilder.createAnnotAttachmentNode();
        serviceNode.addAnnotationAttachment(annoAttachment);

        final SymbolEnv pkgEnv = symTable.pkgEnvMap.get((BPackageSymbol) service.symbol.getEnclosingSymbol());
        annoAttachment.annotationSymbol = (BAnnotationSymbol) symResolver.lookupSymbolInPackage(service.pos, pkgEnv,
                names.fromString("grpc"), names.fromString("ServiceDescriptor"), SymTag.ANNOTATION);
        annoAttachment.annotationName = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        annoAttachment.annotationName.value = "ServiceDescriptor";
        annoAttachment.pos = pos;

        BLangRecordLiteral literalNode = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
        annoAttachment.expr = literalNode;
        literalNode.pos = pos;
        BStructSymbol bStructSymbol = (BStructSymbol) symResolver.lookupSymbolInPackage(service.pos, pkgEnv,
                names.fromString("grpc"), names.fromString("ServiceDescriptorData"), SymTag.STRUCT);
        literalNode.type = bStructSymbol.type;


        BLangRecordLiteral.BLangRecordKeyValue keyValue = (BLangRecordLiteral.BLangRecordKeyValue) TreeBuilder
                .createRecordKeyValue();
        literalNode.keyValuePairs.add(keyValue);

        BLangLiteral keyLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        keyLiteral.value = "descriptor";
        keyLiteral.typeTag = TypeTags.STRING;
        keyLiteral.type = symTable.stringType;

        BLangLiteral valueLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        valueLiteral.value = "fixme";
        valueLiteral.typeTag = TypeTags.STRING;
        valueLiteral.type = symTable.stringType;

        keyValue.key = new BLangRecordLiteral.BLangRecordKey(keyLiteral);
        BSymbol fieldSymbol = symResolver.resolveStructField(service.pos, pkgEnv,
                names.fromString("descriptor"), bStructSymbol);
        keyValue.key.fieldSymbol = (BVarSymbol) fieldSymbol;
        keyValue.valueExpr = valueLiteral;
    }
}
