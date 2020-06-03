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
package io.ballerinalang.compiler.internal.parser.tree;

import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.RecordFieldNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a generated internal syntax tree node.
 *
 * @since 2.0.0
 */
public class STRecordFieldNode extends STNode {
    public final STNode metadata;
    public final STNode typeName;
    public final STNode fieldName;
    public final STNode questionMarkToken;
    public final STNode semicolonToken;

    STRecordFieldNode(
            STNode metadata,
            STNode typeName,
            STNode fieldName,
            STNode questionMarkToken,
            STNode semicolonToken) {
        this(
                metadata,
                typeName,
                fieldName,
                questionMarkToken,
                semicolonToken,
                Collections.emptyList());
    }

    STRecordFieldNode(
            STNode metadata,
            STNode typeName,
            STNode fieldName,
            STNode questionMarkToken,
            STNode semicolonToken,
            Collection<STNodeDiagnostic> diagnostics) {
        super(SyntaxKind.RECORD_FIELD, diagnostics);
        this.metadata = metadata;
        this.typeName = typeName;
        this.fieldName = fieldName;
        this.questionMarkToken = questionMarkToken;
        this.semicolonToken = semicolonToken;

        addChildren(
                metadata,
                typeName,
                fieldName,
                questionMarkToken,
                semicolonToken);
    }

    public STNode modifyWith(Collection<STNodeDiagnostic> diagnostics) {
        return new STRecordFieldNode(
                this.metadata,
                this.typeName,
                this.fieldName,
                this.questionMarkToken,
                this.semicolonToken,
                diagnostics);
    }

    public Node createFacade(int position, NonTerminalNode parent) {
        return new RecordFieldNode(this, position, parent);
    }
}
