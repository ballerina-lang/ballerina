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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.syntax.tree.TypeParameterNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;

import java.util.List;

/**
 * Completion Provider for {@link TypeParameterNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class TypeParameterNodeContext extends TypeParameterContextProvider<TypeParameterNode> {

    public TypeParameterNodeContext() {
        super(TypeParameterNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, TypeParameterNode node)
            throws LSCompletionException {
        return super.getCompletions(context, node);
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, TypeParameterNode node) {
        int cursor = context.getCursorPositionInTree();
        int gtToken = node.gtToken().textRange().endOffset();
        int ltToken = node.ltToken().textRange().startOffset();

        return ltToken < cursor && gtToken > cursor;
    }
}
