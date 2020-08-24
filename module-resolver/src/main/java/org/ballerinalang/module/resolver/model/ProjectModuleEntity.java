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

package org.ballerinalang.module.resolver.model;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.module.resolver.exceptions.ModuleResolveException;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.repository.PackageSource;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_SOURCE_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.RESOURCE_DIR_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.TEST_DIR_NAME;

/**
 * Concrete package entry for project module.
 */
public class ProjectModuleEntity implements PackageSource {
    private final PackageID moduleId;
    private final List<CompilerInput> sourceFiles;
    private final Path sourcePath;
    private final boolean testsEnabled;

    public ProjectModuleEntity(PackageID moduleId, Path sourcePath, boolean testsEnabled) {
        this.moduleId = moduleId;
        this.sourcePath = sourcePath;
        this.testsEnabled = testsEnabled;
        this.sourceFiles = getSourceFiles();
    }

    private List<CompilerInput> getSourceFiles() {
        List<CompilerInput> compilerInputs = new ArrayList<>();
        File[] listOfFiles = new File(String.valueOf(this.sourcePath)).listFiles();
        if (listOfFiles != null && listOfFiles.length > 0) {
            traverseDirectory(listOfFiles, compilerInputs);
        }
        return compilerInputs;
    }

    private void traverseDirectory(File[] files, List<CompilerInput> compilerInputs) {
        for (File file : files) {
            // exclude bal files inside `resources` directory
            if (file.isDirectory() && !file.getName().equals(RESOURCE_DIR_NAME)) {
                if (!(!this.testsEnabled && file.getName().equals(TEST_DIR_NAME))) {
                    File[] listOfFiles = file.listFiles();
                    if (listOfFiles != null && listOfFiles.length > 0) {
                        traverseDirectory(listOfFiles, compilerInputs);
                    }
                }
            } else if (file.getPath().endsWith(BLANG_SOURCE_EXT)) {
                try {
                    compilerInputs.add(new CompilerInputImpl(Files.readAllBytes(file.toPath()), file.toPath(),
                            this.sourcePath));
                } catch (IOException e) {
                    throw new ModuleResolveException("reading module source files failed");
                }
            }
        }
    }

    @Override
    public List<String> getEntryNames() {
        return null;
    }

    @Override
    public CompilerInput getPackageSourceEntry(String name) {
        return null;
    }

    @Override
    public List<CompilerInput> getPackageSourceEntries() {
        return this.sourceFiles;
    }

    @Override
    public PackageID getPackageId() {
        return this.moduleId;
    }

    @Override
    public Kind getKind() {
        return Kind.SOURCE;
    }

    @Override
    public String getName() {
        return this.moduleId.getName().getValue();
    }

    @Override
    public RepoHierarchy getRepoHierarchy() {
        throw new UnsupportedOperationException();
    }
}
