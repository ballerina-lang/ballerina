/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.subproviders.parsercontext;

import org.antlr.v4.runtime.Token;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.providers.contextproviders.GlobalVarDefContextProvider;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion Item Resolver for the Function Definition Context.
 * 
 * @since v0.982.0
 */
public class ParserRuleDefinitionCompletionProvider extends LSCompletionProvider {
    @Override
    public List<CompletionItem> getCompletions(LSContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<String> consumedTokens = ctx.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY).stream()
                .map(Token::getText)
                .collect(Collectors.toList());
        List<String> poppedTokens = CommonUtil.getPoppedTokenStrings(ctx);
        if (poppedTokens.size() >= 1) {
            // Provides completions after public keyword
            completionItems.addAll(new GlobalVarDefContextProvider().getCompletions(ctx));
        }

        if (!consumedTokens.get(0).equals(UtilSymbolKeys.FUNCTION_KEYWORD_KEY)) {
            return completionItems;
        }
        return completionItems;
    }
}
