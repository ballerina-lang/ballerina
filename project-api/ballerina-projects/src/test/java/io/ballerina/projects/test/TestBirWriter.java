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

import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.model.Target;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains cases to test the BirWriter.
 *
 * @since 2.0.0
 */
public class TestBirWriter {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");

    @Test (description = "tests writing of the BIR")
    public void testBirWriter() throws IOException {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");

        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // 2) Load the package
        Package currentPackage = project.currentPackage();
        Target target = new Target(project.sourceRoot());

        Path defaultModuleBirPath = target.birCachePath().resolve("myproject.bir");
        Path storageModuleBirPath = target.birCachePath().resolve("storage.bir");
        Path servicesModuleBirPath = target.birCachePath().resolve("services.bir");
        Assert.assertFalse(defaultModuleBirPath.toFile().exists());
        Assert.assertFalse(storageModuleBirPath.toFile().exists());
        Assert.assertFalse(servicesModuleBirPath.toFile().exists());

        currentPackage.getCompilation().emit(PackageCompilation.OutputType.BIR, target.birCachePath());

        Assert.assertTrue(defaultModuleBirPath.toFile().exists()
                && defaultModuleBirPath.toFile().length() > 0);
        Assert.assertTrue(storageModuleBirPath.toFile().exists()
                && servicesModuleBirPath.toFile().length() > 0);
        Assert.assertTrue(servicesModuleBirPath.toFile().exists()
                && servicesModuleBirPath.toFile().length() > 0);
    }

    @AfterMethod
    public void cleanUp() {
        TestUtils.deleteDirectory(RESOURCE_DIRECTORY.resolve("myproject").resolve("target").toFile());
    }
}
