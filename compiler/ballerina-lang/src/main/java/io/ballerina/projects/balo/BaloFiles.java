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

package io.ballerina.projects.balo;

import com.google.gson.Gson;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.directory.DocumentData;
import io.ballerina.projects.directory.ModuleData;
import io.ballerina.projects.directory.PackageData;
import io.ballerina.projects.model.PackageJson;
import io.ballerina.projects.util.ProjectUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.projects.directory.ProjectFiles.loadDocuments;
import static io.ballerina.projects.util.ProjectConstants.BLANG_COMPILED_PKG_BINARY_EXT;
import static io.ballerina.projects.util.ProjectConstants.MODULES_ROOT;
import static io.ballerina.projects.util.ProjectConstants.PACKAGE_JSON;

/**
 * Contains a set of utility methods that create an in-memory representation of a Ballerina project using a balo.
 *
 * @since 2.0.0
 */
public class BaloFiles {
    private static Gson gson = new Gson();

    // TODO change class name to utils
    private BaloFiles() {
    }

    static PackageData loadPackageData(String baloPath, PackageDescriptor packageDescriptor) {
        Path absBaloPath = validateBaloPath(baloPath);

        URI zipURI = URI.create("jar:" + absBaloPath.toUri().toString());
        try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
            // Load default module
            String pkgName = packageDescriptor.name().toString();
            Path defaultModulePathInBalo = zipFileSystem.getPath(MODULES_ROOT, pkgName);
            ModuleData defaultModule = loadModule(defaultModulePathInBalo, zipFileSystem, pkgName);

            // load other modules
            Path modulesPathInBalo = zipFileSystem.getPath(MODULES_ROOT);
            List<ModuleData> otherModules = loadOtherModules(modulesPathInBalo,defaultModulePathInBalo, zipFileSystem,
                    pkgName);
            return PackageData.from(absBaloPath, defaultModule, otherModules);
        } catch (IOException e) {
            // TODO add 'unable to load balo: balonamme' as root error message, after exception model
            throw new RuntimeException("cannot read balo:" + baloPath);
        }
    }

    private static Path validateBaloPath(String baloPath) {
        if (baloPath == null) {
            throw new IllegalArgumentException("baloPath cannot be null");
        }

        Path absBaloPath = Paths.get(baloPath).toAbsolutePath();
        if (!absBaloPath.toFile().canRead()) {
            throw new RuntimeException("insufficient privileges to balo: " + absBaloPath);
        }
        if (!absBaloPath.toFile().exists()) {
            throw new RuntimeException("balo does not exists: " + baloPath);
        }

        if (!absBaloPath.toString().endsWith(BLANG_COMPILED_PKG_BINARY_EXT)) {
            throw new RuntimeException("Not a balo: " + baloPath);
        }
        return absBaloPath;
    }

    private static void validatePackageJson(PackageJson packageJson) {
        if (packageJson.getOrganization() == null || "".equals(packageJson.getOrganization())) {
            throw new RuntimeException("'organization' does not exists in 'package.json'");
        }
        if (packageJson.getName() == null || "".equals(packageJson.getName())) {
            throw new RuntimeException("'name' does not exists in 'package.json'");
        }
        if (packageJson.getVersion() == null || "".equals(packageJson.getVersion())) {
            throw new RuntimeException("'version' does not exists in 'package.json'");
        }
    }

    private static ModuleData loadModule(Path modulePath, FileSystem zipFileSystem, String pkgName) {
        // check module path exists
        if (!Files.exists(modulePath)) {
            throw new RuntimeException("module does not exists:" + modulePath);
        }

        String moduleName = String.valueOf(modulePath.getFileName());
        if (moduleName.contains(".")) { // not default module
            moduleName = moduleName.split("\\.")[1];
            moduleName = moduleName.replace("/", "");
        }

        // validate moduleName
        if (!ProjectUtils.validateModuleName(moduleName)) {
            throw new RuntimeException("Invalid module name : '" + moduleName + "' :\n" +
                    "Module name can only contain alphanumerics, underscores and periods " +
                    "and the maximum length is 256 characters");
        }

        List<DocumentData> srcDocs = loadDocuments(modulePath);
        List<DocumentData> testSrcDocs = Collections.emptyList();

        // TODO Read Module.md file. Do we need to? Balo creator may need to package Module.md
        return ModuleData.from(modulePath, moduleName, srcDocs, testSrcDocs);
    }

    private static List<ModuleData> loadOtherModules(Path modulesDirPath,
                                                     Path defaultModulePath,
                                                     FileSystem zipFileSystem,
                                                     String pkgName) {
        if (!Files.isDirectory(modulesDirPath)) {
            throw new RuntimeException("'modules' directory does not exists:" + modulesDirPath);
        }

        Stream<Path> pathStream = null;
        try {
            pathStream = Files.walk(modulesDirPath, 1);
            return pathStream
                    .filter(path -> !path.equals(modulesDirPath))
                    .filter(path -> path.getFileName() != null
                            && !path.getFileName().equals(defaultModulePath.getFileName()))
                    .filter(Files::isDirectory)
                    .map(path -> loadModule(path, zipFileSystem, pkgName))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("unable to load modules from directory:" + e.getMessage());
        } finally {
            if (pathStream != null) {
                pathStream.close();
            }
        }
    }

    static PackageDescriptor createPackageDescriptor(String baloPath) {
        URI zipURI = URI.create("jar:" + Paths.get(baloPath).toAbsolutePath().toUri().toString());
        try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
            Path packageJsonPath = zipFileSystem.getPath(PACKAGE_JSON);
            if (!Files.exists(packageJsonPath)) {
                throw new RuntimeException("package.json does not exists:" + packageJsonPath);
            }
            // Load `package.json`
            PackageJson packageJson;
            try {
                packageJson = gson.fromJson(Files.newBufferedReader(packageJsonPath), PackageJson.class);
            } catch (IOException e) {
                throw new RuntimeException("package.json does not exists:" + packageJsonPath);
            }
            validatePackageJson(packageJson);

            PackageName packageName = PackageName.from(packageJson.getName());
            PackageOrg packageOrg = PackageOrg.from(packageJson.getOrganization());
            PackageVersion packageVersion = PackageVersion.from(packageJson.getVersion());
            List<PackageDescriptor.Dependency> dependencies;
            if (packageJson.getDependencies() != null) {
                dependencies = packageJson.getDependencies();
            } else {
                dependencies = Collections.emptyList();
            }

            return new PackageDescriptor(packageName, packageOrg, packageVersion, dependencies, Collections.emptyMap(),
                    Collections.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("unable to load balo:" + baloPath);
        }
    }
}
