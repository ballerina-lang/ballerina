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
package io.ballerina.projects.test;

import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.directory.BuildProject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Contains cases to test the basic package structure.
 *
 * @since 2.0.0
 */
public class TestBuildProject {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");

    @Test
    public void testBuildProjectAPI() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");
        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // 2) Load the package
        Package currentPackage = project.getPackage();
        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();
        Assert.assertEquals(defaultModule.documentIds().size(), 2);

        // TODO find an easy way to test the project structure. e.g. serialize the structure in a json file.
        int noOfSrcDocuments = 0;
        int noOfTestDocuments = 0;
        final Collection<ModuleId> moduleIds = currentPackage.moduleIds();
        Assert.assertEquals(moduleIds.size(), 3);
        for (ModuleId moduleId : moduleIds) {
            Module module = currentPackage.module(moduleId);
            for (DocumentId documentId : module.documentIds()) {
                noOfSrcDocuments++;
            }
            for (DocumentId testDocumentId : module.testDocumentIds()) {
                noOfTestDocuments++;
            }
            for (Document doc : module.documents()) {
                Assert.assertNotNull(doc.syntaxTree());
            }
        }

        Assert.assertEquals(noOfSrcDocuments, 4);
        Assert.assertEquals(noOfTestDocuments, 3);

        Assert.assertTrue(project.target().toFile().exists());
        Assert.assertEquals(project.target().toFile().getParent(), projectPath.toString());
    }

    @Test
    public void testSetBuildOptions() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");
        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        BuildProject.BuildOptions buildOptions = project.getBuildOptions();

        // Verify expected default buildOptions
        Assert.assertEquals(buildOptions.getSourceRoot(), System.getProperty("user.dir"));
        Assert.assertFalse(buildOptions.isObservabilityIncluded());
        Assert.assertFalse(buildOptions.isSkipTests());
        Assert.assertFalse(buildOptions.isOffline());
        Assert.assertFalse(buildOptions.isTestReport());
        Assert.assertFalse(buildOptions.isCodeCoverage());
        Assert.assertFalse(buildOptions.isSkipLock());
        Assert.assertFalse(buildOptions.isExperimental());

        buildOptions.setObservabilityEnabled(false);
        buildOptions.setSkipLock(true);
        buildOptions.setSkipTests(true);
        buildOptions.setSourceRoot(projectPath.toString());
        buildOptions.setCodeCoverage(true);

        // Update and verify buildOptions
        project.setBuildOptions(buildOptions);
        buildOptions = project.getBuildOptions();
        Assert.assertEquals(buildOptions.getSourceRoot(), projectPath.toString());
        Assert.assertFalse(buildOptions.isObservabilityIncluded());
        Assert.assertTrue(buildOptions.isSkipTests());
        Assert.assertFalse(buildOptions.isOffline());
        Assert.assertFalse(buildOptions.isTestReport());
        Assert.assertTrue(buildOptions.isCodeCoverage());
        Assert.assertTrue(buildOptions.isSkipLock());
        Assert.assertFalse(buildOptions.isExperimental());
    }

}
