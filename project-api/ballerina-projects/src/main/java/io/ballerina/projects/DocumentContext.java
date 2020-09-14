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
package io.ballerina.projects;

import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;

/**
 * Maintains the internal state of a {@code Document} instance.
 * <p>
 * Works as a document cache.
 *
 * @since 2.0.0
 */
class DocumentContext {
    private final DocumentConfig documentConfig;
    private SyntaxTree syntaxTree;

    private DocumentContext(DocumentConfig documentConfig) {
        this.documentConfig = documentConfig;
    }

    static DocumentContext from(DocumentConfig documentConfig) {
        return new DocumentContext(documentConfig);
    }

    DocumentId documentId() {
        return documentConfig.documentId();
    }

    protected SyntaxTree syntaxTree() {
        if (this.syntaxTree == null) {
            TextDocument textDocument = TextDocuments.from(this.documentConfig.content());
            this.syntaxTree = SyntaxTree.from(textDocument);
        }

        return this.syntaxTree;
    }
}
