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
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.ProjectEnvironmentContext;
import org.ballerinalang.compiler.CompilerPhase;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;

/**
 * Contains cases to test the JarWriter.
 *
 * @since 2.0.0
 */
public class TestJarWriter {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");

    @Test (enabled = false, description = "tests writing of the executable")
    public void testJarWriter() throws IOException {
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

        ProjectEnvironmentContext environmentContext = project.environmentContext();
        CompilerContext compilerContext = environmentContext.getService(CompilerContext.class);
        CompilerOptions compilerOptions = CompilerOptions.getInstance(compilerContext);
        compilerOptions.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());

        Path tempDirectory = Files.createTempDirectory("ballerina-test-" + System.nanoTime());
        Path tempFile = tempDirectory.resolve("test.jar");
        Assert.assertFalse(tempFile.toFile().exists());
//        JarWriter.write(currentPackage, tempFile);
//        Assert.assertTrue(tempFile.toFile().exists());
//        Assert.assertTrue(tempFile.toFile().length() > 0);
    }
}
