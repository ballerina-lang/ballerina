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

package io.ballerina.plugins.idea.psi.impl;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.containers.ContainerUtil;
import io.ballerina.plugins.idea.psi.BallerinaAlias;
import io.ballerina.plugins.idea.psi.BallerinaCallableUnitSignature;
import io.ballerina.plugins.idea.psi.BallerinaCompletePackageName;
import io.ballerina.plugins.idea.psi.BallerinaFormalParameterList;
import io.ballerina.plugins.idea.psi.BallerinaFunctionDefinition;
import io.ballerina.plugins.idea.psi.BallerinaFunctionNameReference;
import io.ballerina.plugins.idea.psi.BallerinaImportDeclaration;
import io.ballerina.plugins.idea.psi.BallerinaOrgName;
import io.ballerina.plugins.idea.psi.BallerinaPackageName;
import io.ballerina.plugins.idea.psi.BallerinaReturnType;
import io.ballerina.plugins.idea.psi.BallerinaTupleTypeName;
import io.ballerina.plugins.idea.psi.BallerinaTypeName;
import io.ballerina.plugins.idea.sdk.BallerinaSdkUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Util class which contains methods related to PSI manipulation.
 */
public class BallerinaPsiImplUtil {

    private static final List<String> BUILTIN_DIRECTORIES = new LinkedList<>();

    private static final List<String> BUILTIN_VARIABLE_TYPES = new LinkedList<>();

    static {
        BUILTIN_DIRECTORIES.add(File.separator + "builtin");

        BUILTIN_VARIABLE_TYPES.add("future"); //async
        BUILTIN_VARIABLE_TYPES.add("blob");
        BUILTIN_VARIABLE_TYPES.add("json");
        BUILTIN_VARIABLE_TYPES.add("map");
        BUILTIN_VARIABLE_TYPES.add("stream");
        BUILTIN_VARIABLE_TYPES.add("string");
        BUILTIN_VARIABLE_TYPES.add("table");
        BUILTIN_VARIABLE_TYPES.add("xml");
    }

    public static final String LOCAL_PACKAGE_PLACEHOLDER = "$LOCAL_PROJECT$";
    // Since instances of "com.intellij.openapi.project.Project" returns system-independant paths for project directory
    // File.seperator should not be used
    private static final String FILE_SEPARATOR = "/";

    @Nullable
    public static String getName(@NotNull BallerinaPackageName ballerinaPackageName) {
        PsiElement identifier = ballerinaPackageName.getIdentifier();
        return identifier.getText();
    }

    @Nullable
    public static String getName(@NotNull BallerinaOrgName ballerinaOrgName) {
        PsiElement identifier = ballerinaOrgName.getIdentifier();
        return identifier.getText();
    }

    @Nullable
    public static String getName(@NotNull BallerinaAlias ballerinaAlias) {
        PsiElement identifier = ballerinaAlias.getIdentifier();
        return identifier != null ? identifier.getText() : null;
    }

    @Nullable
    public static PsiElement getIdentifier(BallerinaFunctionDefinition ballerinaFunctionDefinition) {
        BallerinaCallableUnitSignature callableUnitSignature = ballerinaFunctionDefinition.getCallableUnitSignature();
        return callableUnitSignature != null ? callableUnitSignature.getAnyIdentifierName().getIdentifier() : null;
    }

    @Nullable
    public static String getName(BallerinaFunctionDefinition ballerinaFunctionDefinition) {
        BallerinaCallableUnitSignature callableUnitSignature = ballerinaFunctionDefinition.getCallableUnitSignature();
        return callableUnitSignature != null && callableUnitSignature.getAnyIdentifierName().getIdentifier() != null ?
                callableUnitSignature.getAnyIdentifierName().getIdentifier().getText() :
                "";
    }

    public static boolean isInLocalPackage(@NotNull BallerinaFunctionNameReference nameReference) {
        return nameReference.getPackageReference() == null;
    }

    @NotNull
    public static String formatBallerinaFunctionParameters(@Nullable BallerinaFormalParameterList parameterList) {
        if (parameterList == null) {
            return "()";
        }
        // Todo - Update formatting logic.
        // Todo - Format anonymous structs correctly.
        return "(" + parameterList.getText().replaceAll("\n", "").replaceAll("\\s+", " ") + ")";
    }

    @Nullable
    public static String formatBallerinaFunctionReturnType(@Nullable BallerinaReturnType ballerinaReturnType) {
        if (ballerinaReturnType == null) {
            return null;
        }
        BallerinaTypeName typeName = ballerinaReturnType.getTypeName();
        if (typeName instanceof BallerinaTupleTypeName) {
            List<BallerinaTypeName> typeNameList = ((BallerinaTupleTypeName) typeName).getTypeNameList();
            if (!typeNameList.isEmpty()) {
                return typeNameList.get(0).getText();
            }
        }
        return typeName.getText();
    }

    /**
     * Finds a file in the project SDK.
     *
     * @param project current project
     * @param path    relative file path in the SDK
     * @return {@code null} if the file cannot be found, otherwise returns the corresponding {@link VirtualFile}.
     */
    @Nullable
    public static VirtualFile findFileInProjectSDK(@NotNull Project project, @NotNull String path) {
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectSdk == null) {
            return null;
        }
        VirtualFile[] roots = projectSdk.getSdkModificator().getRoots(OrderRootType.SOURCES);
        VirtualFile file;
        for (VirtualFile root : roots) {
            file = VfsUtilCore.findRelativeFile(path, root);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    @Nullable
    public static VirtualFile getSDKSrcRoot(@NotNull Project project, @Nullable Module module) {
        LinkedHashSet<VirtualFile> sources = BallerinaSdkUtil.getSourcesPathsToLookup(project, module);
        if (sources.isEmpty()) {
            return null;
        }
        return ContainerUtil.getFirstItem(sources);
    }

    /**
     * Find the specified file in the project and returns the corresponding {@link PsiFile}.
     *
     * @param project a project
     * @param path    file path
     * @return corresponding psi file
     */
    @Nullable
    public static PsiFile findFileInProject(@NotNull Project project, @NotNull String path) {
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
        if (virtualFile == null) {
            return null;
        }
        return PsiManager.getInstance(project).findFile(virtualFile);
    }

    @NotNull
    public static String getPackage(@NotNull PsiFile file) {
        Project project = file.getProject();
        String modulePath = project.getBasePath() + FILE_SEPARATOR;
        String filePath = file.getVirtualFile().getPath();
        filePath = filePath.replace(modulePath, "");
        if (!filePath.contains(FILE_SEPARATOR)) {
            return "";
        }
        int index = filePath.indexOf(FILE_SEPARATOR);
        return filePath.substring(0, index);
    }

    @NotNull
    public static String getPackage(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        String modulePath = project.getBasePath() + FILE_SEPARATOR;
        String filePath = virtualFile.getPath();
        filePath = filePath.replace(modulePath, "");
        if (!filePath.contains(FILE_SEPARATOR)) {
            return "";
        }
        int index = filePath.indexOf(FILE_SEPARATOR);
        return filePath.substring(0, index);
    }

    @NotNull
    public static String getFilePathInPackage(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        String projectPath = project.getBasePath() + FILE_SEPARATOR;
        String filePath = virtualFile.getPath();
        filePath = filePath.replace(projectPath, "");
        if (!filePath.contains(FILE_SEPARATOR)) {
            return "";
        }
        int index = filePath.indexOf(FILE_SEPARATOR);
        return filePath.substring(index + 1);
    }

    public static boolean isAlreadyImported(@NotNull List<BallerinaImportDeclaration> allImportsInPackage,
            @NotNull String currentPackageName) {
        for (BallerinaImportDeclaration ballerinaImportDeclaration : allImportsInPackage) {
            BallerinaCompletePackageName completePackageName = ballerinaImportDeclaration.getCompletePackageName();
            if (completePackageName == null) {
                continue;
            }
            if (completePackageName.getText().equalsIgnoreCase(currentPackageName)) {
                return true;
            }
        }
        return false;
    }
}
