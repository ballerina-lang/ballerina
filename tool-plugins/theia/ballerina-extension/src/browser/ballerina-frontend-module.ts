/**
 * Generated using theia-extension-generator
 */

import { BallerinaCommandContribution, BallerinaMenuContribution } from './ballerina-contribution';
import {
    CommandContribution,
    MenuContribution
} from "@theia/core/lib/common";
import { LanguageClientContribution } from "@theia/languages/lib/browser";
import { LanguageGrammarDefinitionContribution } from "@theia/monaco/lib/browser/textmate";
import { BallerinaGrammarContribution } from "./ballerina-grammar-contribution";
import { BallerinaLanguageClientContribution } from './ballerina-language-client-contribution';
import { PreviewHandler } from '@theia/preview/lib/browser';
import { BallerinaPreviewHandler } from './ballerina-preview-handler';

import { ContainerModule } from "inversify";

export default new ContainerModule(bind => {
    bind(LanguageClientContribution).to(BallerinaLanguageClientContribution).inSingletonScope();
    bind(CommandContribution).to(BallerinaCommandContribution);
    bind(MenuContribution).to(BallerinaMenuContribution);
    bind(LanguageGrammarDefinitionContribution).to(BallerinaGrammarContribution).inSingletonScope();
    bind(BallerinaPreviewHandler).toSelf().inSingletonScope();
    bind(PreviewHandler).toService(BallerinaPreviewHandler);
});