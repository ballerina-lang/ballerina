/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.psi.scopeprocessors;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import io.ballerina.plugins.idea.completion.BallerinaCompletionUtils;
import io.ballerina.plugins.idea.psi.BallerinaBlock;
import io.ballerina.plugins.idea.psi.BallerinaCallableUnitSignature;
import io.ballerina.plugins.idea.psi.BallerinaDefaultableParameter;
import io.ballerina.plugins.idea.psi.BallerinaForeachStatement;
import io.ballerina.plugins.idea.psi.BallerinaFormalParameterList;
import io.ballerina.plugins.idea.psi.BallerinaFunctionDefinition;
import io.ballerina.plugins.idea.psi.BallerinaLambdaFunction;
import io.ballerina.plugins.idea.psi.BallerinaNameReference;
import io.ballerina.plugins.idea.psi.BallerinaObjectBody;
import io.ballerina.plugins.idea.psi.BallerinaObjectFieldDefinition;
import io.ballerina.plugins.idea.psi.BallerinaObjectFunctionDefinition;
import io.ballerina.plugins.idea.psi.BallerinaParameter;
import io.ballerina.plugins.idea.psi.BallerinaParameterWithType;
import io.ballerina.plugins.idea.psi.BallerinaRestParameter;
import io.ballerina.plugins.idea.psi.BallerinaSimpleVariableReference;
import io.ballerina.plugins.idea.psi.BallerinaStatement;
import io.ballerina.plugins.idea.psi.BallerinaTypeDefinition;
import io.ballerina.plugins.idea.psi.BallerinaVariableReference;
import io.ballerina.plugins.idea.psi.BallerinaVariableReferenceList;
import io.ballerina.plugins.idea.psi.BallerinaWorkerDefinition;
import io.ballerina.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Responsible for resolving and completing definitions in code blocks.
 */
public class BallerinaBlockProcessor extends BallerinaScopeProcessorBase {

    @Nullable
    private final CompletionResultSet myResult;
    @NotNull
    private final PsiElement myElement;

    public BallerinaBlockProcessor(@Nullable CompletionResultSet result, @NotNull PsiElement element,
                                   boolean isCompletion) {
        super(element, element, isCompletion);
        myResult = result;
        myElement = element;
    }

    protected boolean accept(@NotNull PsiElement element) {
        return element instanceof BallerinaBlock;
    }

    @Override
    public boolean execute(@NotNull PsiElement scopeElement, @NotNull ResolveState state) {
        ProgressManager.checkCanceled();
        if (accept(scopeElement)) {
            BallerinaBlock block = (BallerinaBlock) scopeElement;

            while (block != null) {
                // Check for workers in all blocks.
                List<BallerinaWorkerDefinition> ballerinaWorkerDefinitions = PsiTreeUtil.getChildrenOfTypeAsList(block,
                        BallerinaWorkerDefinition.class);
                for (BallerinaWorkerDefinition ballerinaWorkerDefinition : ballerinaWorkerDefinitions) {
                    PsiElement identifier = ballerinaWorkerDefinition.getIdentifier();
                    if (identifier == null) {
                        continue;
                    }
                    if (myResult != null) {
                        myResult.addElement(BallerinaCompletionUtils.createWorkerLookupElement(identifier));
                    } else if (myElement.getText().equals(identifier.getText())) {
                        add(identifier);
                    }
                }

                // Check for enclosing lambda function.
                BallerinaLambdaFunction ballerinaLambdaFunction = PsiTreeUtil.getParentOfType(block,
                        BallerinaLambdaFunction.class);
                if (ballerinaLambdaFunction != null) {
                    BallerinaFormalParameterList formalParameterList = ballerinaLambdaFunction.getFormalParameterList();
                    if (formalParameterList != null) {
                        processFormalParameterList(formalParameterList);
                    }
                }

                // Check for enclosing foreach statements.
                BallerinaForeachStatement ballerinaForeachStatement = PsiTreeUtil.getParentOfType(block,
                        BallerinaForeachStatement.class);
                if (ballerinaForeachStatement != null) {
                    BallerinaVariableReferenceList variableReferenceList = ballerinaForeachStatement
                            .getVariableReferenceList();
                    if (variableReferenceList != null) {
                        List<BallerinaVariableReference> referenceList = variableReferenceList
                                .getVariableReferenceList();

                        for (int i = 0; i < referenceList.size(); i++) {
                            String type = null;
                            if (referenceList.size() == 2 && i == 0) {

                                type = "int";
                                BallerinaVariableReference ballerinaVariableReference = referenceList.get(0);
                                BallerinaNameReference nameReference = ((BallerinaSimpleVariableReference)
                                        ballerinaVariableReference).getNameReference();
                                PsiElement identifier = nameReference.getIdentifier();
                                if (myResult != null) {
                                    myResult.addElement(BallerinaCompletionUtils
                                            .createVariableLookupElement(identifier, type));
                                } else if (myElement.getText().equals(identifier.getText())) {
                                    add(identifier);
                                    return false;
                                }
                                continue;
                            }

                            BallerinaVariableReference ballerinaVariableReference = referenceList.get(i);
                            if (ballerinaVariableReference instanceof BallerinaSimpleVariableReference) {
                                PsiElement ballerinaVariableReferenceType = ballerinaVariableReference.getType();
                                if (ballerinaVariableReferenceType != null) {
                                    type = ballerinaVariableReferenceType.getText();
                                }

                                BallerinaNameReference nameReference = ((BallerinaSimpleVariableReference)
                                        ballerinaVariableReference).getNameReference();
                                PsiElement identifier = nameReference.getIdentifier();
                                if (myResult != null) {
                                    myResult.addElement(BallerinaCompletionUtils
                                            .createVariableLookupElement(identifier, type));
                                } else if (myElement.getText().equals(identifier.getText())) {
                                    add(identifier);
                                    return false;
                                }
                            }
                        }
                    }
                }

                // Get variable definitions from statements.
                List<BallerinaStatement> statements = block.getStatementList();
                for (BallerinaStatement statement : statements) {
                    int statementEndOffset = statement.getTextRange().getEndOffset();
                    if (statementEndOffset >= myElement.getTextRange().getEndOffset()) {
                        continue;
                    }

                    PsiElement firstChild = statement.getFirstChild();
                    if (firstChild == null) {
                        continue;
                    }
                }
                if (!isCompletion() && getResult() != null) {
                    return false;
                }

                block = PsiTreeUtil.getParentOfType(block, BallerinaBlock.class);
            }

            // Todo - check return value and continue only if needed

            if (!isCompletion() && getResult() != null) {
                return false;
            }
            processObjectFunctions(scopeElement);
            if (!isCompletion() && getResult() != null) {
                return false;
            }
            processFunctionSignature(scopeElement);
            if (!isCompletion() && getResult() != null) {
                return false;
            }
            processObjectFunctionSignature(scopeElement);
            if (!isCompletion() && getResult() != null) {
                return false;
            }
            // Object fields should be processed after object functions. Otherwise the variable might match with a
            // field which should be matched to a parameter.
            processObjectFields(scopeElement);
            return isCompletion() || getResult() == null;
        }
        return true;
    }

    private void processObjectFunctions(@NotNull PsiElement scopeElement) {
        BallerinaTypeDefinition ballerinaTypeDefinition = PsiTreeUtil.getParentOfType(scopeElement,
                BallerinaTypeDefinition.class);
        if (ballerinaTypeDefinition != null) {
            BallerinaObjectFunctionProcessor ballerinaObjectFunctionProcessor =
                    new BallerinaObjectFunctionProcessor(myResult, myElement, isCompletion());
            ballerinaObjectFunctionProcessor.execute(ballerinaTypeDefinition, ResolveState.initial());
            PsiElement result = ballerinaObjectFunctionProcessor.getResult();
            if (!isCompletion() && result != null) {
                add(result);
            }
        }
    }

    private void processObjectFields(@NotNull PsiElement scopeElement) {
        BallerinaObjectFunctionDefinition ballerinaObjectFunctionDefinition = PsiTreeUtil.getParentOfType(scopeElement,
                BallerinaObjectFunctionDefinition.class);
        if (ballerinaObjectFunctionDefinition == null) {
            return;
        }

        processObjectFunctionDefinition(ballerinaObjectFunctionDefinition);
    }

    // Todo - Move to util and merge with duplicate method in BallerinaObjectFieldProcessor
    private void processObjectFunctionDefinition(@NotNull BallerinaObjectFunctionDefinition definition) {
        BallerinaObjectBody ballerinaObjectBody = PsiTreeUtil.getParentOfType(definition,
                BallerinaObjectBody.class);
        if (ballerinaObjectBody == null) {
            return;
        }

        processObjectDefinition(ballerinaObjectBody);
    }

    private void processObjectDefinition(@NotNull BallerinaObjectBody ballerinaObjectBody) {
        BallerinaTypeDefinition ballerinaTypeDefinition = PsiTreeUtil.getParentOfType(ballerinaObjectBody,
                BallerinaTypeDefinition.class);
        if (ballerinaTypeDefinition == null || ballerinaTypeDefinition.getIdentifier() == null) {
            return;
        }
    }

    private void processObjectFields(@Nullable PsiElement typeName,
                                     @NotNull List<BallerinaObjectFieldDefinition> fieldDefinitionList) {
        if (typeName == null) {
            return;
        }
        for (BallerinaObjectFieldDefinition ballerinaFieldDefinition : fieldDefinitionList) {
            PsiElement identifier = ballerinaFieldDefinition.getIdentifier();
            if (identifier == null) {
                continue;
            }
            if (myResult != null) {
                myResult.addElement(BallerinaCompletionUtils.createFieldLookupElement(identifier, typeName,
                        ballerinaFieldDefinition.getTypeName().getText(),
                        BallerinaPsiImplUtil.getObjectFieldDefaultValue(ballerinaFieldDefinition), null,
                        ballerinaFieldDefinition.getPublic() != null, ballerinaFieldDefinition.getPrivate() != null));
            } else if (myElement.getText().equals(identifier.getText())) {
                add(identifier);
            }
        }
    }

    private void processFunctionSignature(@NotNull PsiElement scopeElement) {
        BallerinaFunctionDefinition ballerinaFunctionDefinition = PsiTreeUtil.getParentOfType(scopeElement,
                BallerinaFunctionDefinition.class);
        if (ballerinaFunctionDefinition == null) {
            return;
        }

        BallerinaCallableUnitSignature callableUnitSignature = ballerinaFunctionDefinition.getCallableUnitSignature();
        if (callableUnitSignature == null) {
            return;
        }

        // Todo - Needed here? Cannot access without self?
        //        BallerinaAttachedObject attachedObject = ballerinaFunctionDefinition.getAttachedObject();
        //        if (attachedObject != null) {
        //            PsiElement identifier = callableUnitSignature.getIdentifier();
        //            PsiReference reference = identifier.getReference();
        //            if (reference != null) {
        //                PsiElement resolvedElement = reference.resolve();
        //                if (resolvedElement != null) {
        //                    BallerinaObjectFunctionDefinition objectFunctionDefinition =
        //                            PsiTreeUtil.getParentOfType(resolvedElement, BallerinaObjectFunctionDefinition
        // .class);
        //                    if (objectFunctionDefinition != null) {
        //                        processObjectFunctionDefinition(objectFunctionDefinition);
        //                    }
        //                }
        //            }
        //        }

        BallerinaFormalParameterList formalParameterList = callableUnitSignature.getFormalParameterList();
        if (formalParameterList == null) {
            return;
        }
        processFormalParameterList(formalParameterList);
    }

    private void processObjectFunctionSignature(@NotNull PsiElement scopeElement) {
        BallerinaObjectFunctionDefinition objectFunctionDefinition = PsiTreeUtil.getParentOfType(scopeElement,
                BallerinaObjectFunctionDefinition.class);
        if (objectFunctionDefinition == null) {
            return;
        }

        BallerinaCallableUnitSignature callableUnitSignature =
                objectFunctionDefinition.getCallableUnitSignature();
        if (callableUnitSignature == null) {
            return;
        }

        BallerinaFormalParameterList formalParameterList = callableUnitSignature.getFormalParameterList();
        if (formalParameterList == null) {
            return;
        }
        processFormalParameterList(formalParameterList);
    }

    private void processFormalParameterList(@NotNull BallerinaFormalParameterList formalParameterList) {
        processParameterList(formalParameterList.getParameterList());

        List<BallerinaDefaultableParameter> defaultableParameterList =
                formalParameterList.getDefaultableParameterList();
        for (BallerinaDefaultableParameter ballerinaDefaultableParameter : defaultableParameterList) {
            BallerinaParameter parameter = ballerinaDefaultableParameter.getParameter();
            List<BallerinaParameterWithType> parameterWithTypeList = parameter
                    .getParameterWithTypeList();
            for (BallerinaParameterWithType ballerinaParameterWithType : parameterWithTypeList) {
                PsiElement identifier = ballerinaParameterWithType.getIdentifier();
                if (identifier == null) {
                    continue;
                }
                if (myResult != null) {
                    myResult.addElement(BallerinaCompletionUtils.createParameterLookupElement(identifier, null, null));
                } else if (myElement.getText().equals(identifier.getText())) {
                    add(identifier);
                }
            }
        }

        BallerinaRestParameter restParameter = formalParameterList.getRestParameter();
        if (restParameter != null) {
            PsiElement identifier = restParameter.getIdentifier();
            if (identifier != null) {
                if (myResult != null) {
                    myResult.addElement(BallerinaCompletionUtils.createParameterLookupElement(identifier, null, null));
                } else if (myElement.getText().equals(identifier.getText())) {
                    add(identifier);
                }
            }
        }
    }

    private void processParameterList(@NotNull List<BallerinaParameter> parameterList) {
        for (BallerinaParameter parameter : parameterList) {
            List<BallerinaParameterWithType> parameterWithTypeList = parameter.getParameterWithTypeList();
            for (BallerinaParameterWithType ballerinaParameterWithType : parameterWithTypeList) {
                PsiElement identifier = ballerinaParameterWithType.getIdentifier();
                if (identifier == null) {
                    continue;
                }
                if (myResult != null) {
                    myResult.addElement(BallerinaCompletionUtils.createParameterLookupElement(identifier,
                            BallerinaPsiImplUtil.formatBallerinaTypeName(ballerinaParameterWithType.getTypeName()),
                            null));
                } else if (myElement.getText().equals(identifier.getText())) {
                    add(identifier);
                }
            }
        }
    }

    @Override
    public boolean isCompletion() {
        return myIsCompletion;
    }

    @Override
    protected boolean crossOff(@NotNull PsiElement e) {
        return false;
    }
}
