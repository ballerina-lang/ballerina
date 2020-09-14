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

import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.utils.ProjectConstants;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Contains a set of utility methods to create a project.
 *
 * @since 2.0.0
 */
public class ProjectLoader {

    /**
     * Returns a project by deriving the type from the path provided.
     *
     * @param path ballerina project or standalone file path
     * @return project of applicable type
     */
    public static Project loadProject(Path path) {
        Path absProjectPath = Optional.of(path.toAbsolutePath()).get();
        Path projectRoot;
        if (absProjectPath.toFile().isDirectory()) {
            if (ProjectConstants.MODULES_ROOT.equals(
                    Optional.of(absProjectPath.getParent()).get().toFile().getName())) {
                projectRoot = Optional.of(Optional.of(absProjectPath.getParent()).get().getParent()).get();
            } else {
                projectRoot = absProjectPath;
            }
            return BuildProject.loadProject(projectRoot);
        }
        // check if the file is a source file in the default module
        projectRoot = Optional.of(absProjectPath.getParent()).get();
        if (hasBallerinaToml(projectRoot)) {
            return BuildProject.loadProject(projectRoot);
        }

        // check if the file is a test file in the default module
        Path testsRoot = Optional.of(absProjectPath.getParent()).get();
        projectRoot = Optional.of(testsRoot.getParent()).get();
        if (ProjectConstants.TEST_DIR_NAME.equals(testsRoot.toFile().getName()) && hasBallerinaToml(projectRoot)) {
            return BuildProject.loadProject(projectRoot);
        }

        // check if the file is a source file in a non-default module
        Path moduleDirPath = Optional.of(absProjectPath.getParent()).get();
        if (!Files.exists(moduleDirPath)) {
            throw new RuntimeException("module directory path does not exist: " + moduleDirPath.toString());
        }
        Path modulesRoot = Optional.of(moduleDirPath.getParent()).get();
        projectRoot = modulesRoot.getParent();
        if (ProjectConstants.MODULES_ROOT.equals(modulesRoot.toFile().getName()) && hasBallerinaToml(projectRoot)) {
            return BuildProject.loadProject(projectRoot);
        }

        // check if the file is a test file in a non-default module
        if (!Files.exists(testsRoot)) {
            throw new RuntimeException("module tests directory path does not exist: " + testsRoot.toString());
        }
        modulesRoot = Optional.of(Optional.of(testsRoot.getParent()).get().getParent()).get();
        projectRoot = modulesRoot.getParent();

        if (ProjectConstants.MODULES_ROOT.equals(modulesRoot.toFile().getName()) && hasBallerinaToml(projectRoot)) {
            return BuildProject.loadProject(projectRoot);
        }

        return SingleFileProject.loadProject(absProjectPath);
    }

    private static boolean hasBallerinaToml(Path filePath) {
        return filePath.resolve(ProjectConstants.BALLERINA_TOML).toFile().exists();
    }

    /**
     * Returns the documentId of the provided file path.
     *
     * @param documentFilePath file path of the document
     * @param project project that the file belongs to
     * @return documentId of the document
     */
    public static DocumentId getDocumentId(Path documentFilePath, Project project) {
        Path parent = Optional.of(documentFilePath.getParent()).get();
        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            if (parent.toString().equals(moduleId.moduleDirPath()) || parent.toString().equals(
                    Paths.get(moduleId.moduleDirPath()).resolve(ProjectConstants.TEST_DIR_NAME).toString())) {
                Module module = project.currentPackage().module(moduleId);
                for (DocumentId documentId : module.documentIds()) {
                    if (documentId.documentPath().equals(documentFilePath.toString())) {
                        return documentId;
                    }
                }

                for (DocumentId documentId : module.testDocumentIds()) {
                    if (documentId.documentPath().equals(documentFilePath.toString())) {
                        return documentId;
                    }
                }
            }
        }
        return null;
    }
}
