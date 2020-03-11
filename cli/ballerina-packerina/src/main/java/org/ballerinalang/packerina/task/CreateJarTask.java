/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.task;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.model.ModuleJar;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.BackendDriver;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;

/**
 * Task for creating jar file.
 */
public class CreateJarTask implements Task {

    private boolean dumpBir;

    private boolean skipCopyLibsFromDist = false;

    public CreateJarTask(boolean dumpBir) {

        this.dumpBir = dumpBir;
    }

    public CreateJarTask(boolean dumpBir, boolean skipCopyLibsFromDist) {

        this.dumpBir = dumpBir;
        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
    }

    @Override
    public void execute(BuildContext buildContext) {
        // This will avoid initializing Config registry during jar creation.
        ConfigRegistry.getInstance().setInitialized(true);
        Path sourceRoot = buildContext.get(BuildContextField.SOURCE_ROOT);
        Path runtimeJar = getRuntimeAllJar(buildContext);

        CompilerContext context = buildContext.get(BuildContextField.COMPILER_CONTEXT);
        PackageCache packageCache = PackageCache.getInstance(context);

        BackendDriver backendDriver = BackendDriver.getInstance(context);
        Set<PackageID> alreadyImportedModulesSet = new HashSet<>();
        List<BLangPackage> moduleBirMap = buildContext.getModules();

        // Collect imported modules as flat stack.
        for (BLangPackage module : moduleBirMap) {
            List<ModuleJar> moduleList = new LinkedList<>();
            BLangPackage bLangPackage = packageCache.get(module.packageID);
            if (bLangPackage == null) {
                continue;
            }
            // If there is a testable package we will create testable jar.
            if (!buildContext.skipTests() && bLangPackage.hasTestablePackage()) {
                for (BLangPackage testPkg : bLangPackage.getTestablePkgs()) {
                    // write its child imports jar file to cache
                    Path testJarOutput = buildContext.getTestJarPathFromTargetCache(testPkg.packageID);
                    moduleList.add(new ModuleJar(testPkg.symbol, testJarOutput));
                    collectImportModules(testPkg.symbol.imports, sourceRoot, buildContext, alreadyImportedModulesSet,
                                         moduleList);
                }
            }
            alreadyImportedModulesSet.add(module.packageID);
            Path jarOutput = buildContext.getJarPathFromTargetCache(module.packageID);
            moduleList.add(new ModuleJar(bLangPackage.symbol, jarOutput));
            collectImportModules(bLangPackage.symbol.imports, sourceRoot, buildContext, alreadyImportedModulesSet,
                                 moduleList);

            // Module imports will be code gen first. Hence module list will be code gen in reverse order.
            for (int i = moduleList.size(); i-- > 0; ) {
                ModuleJar moduleJar = moduleList.get(i);
                BPackageSymbol bImport = moduleJar.bPackageSymbol;
                if (bImport.bir != null) {
                    HashSet<Path> moduleDependencySet =
                            buildContext.moduleDependencyPathMap.get(bImport.pkgID).moduleLibs;
                    if (!skipCopyLibsFromDist) {
                        moduleDependencySet.add(runtimeJar);
                    }
                    backendDriver.execute(bImport.bir, dumpBir, moduleJar.jarOutput, moduleDependencySet);
                }
            }
        }
        ConfigRegistry.getInstance().setInitialized(false);
    }

    private void collectImportModules(List<BPackageSymbol> imports, Path sourceRoot, BuildContext buildContext,
                                      Set<PackageID> alreadyImportedModulesSet , List< ModuleJar> moduleList) {
        for (BPackageSymbol bimport : imports) {
            PackageID id = bimport.pkgID;
            // skip ballerina and ballerinax
            if (alreadyImportedModulesSet.contains(id) || id.orgName.value.equals("ballerina") ||
                    id.orgName.value.equals("ballerinax")) {
                continue;
            }
            Path jarFilePath;
            // If the module is part of the project write it to project jar cache check if file exist
            // If not write it to home jar cache
            if (ProjectDirs.isModuleExist(sourceRoot, id.name.value) ||
                    buildContext.getImportPathDependency(id).isPresent()) {
                jarFilePath = buildContext.getJarPathFromTargetCache(id);
            } else {
                jarFilePath = buildContext.getJarPathFromHomeCache(id);
            }
            alreadyImportedModulesSet.add(id);
            moduleList.add(new ModuleJar(bimport, jarFilePath));
            collectImportModules(bimport.imports, sourceRoot, buildContext, alreadyImportedModulesSet, moduleList);
        }
    }

    private Path getRuntimeAllJar(BuildContext buildContext) {

        if (skipCopyLibsFromDist) {
            return null;
        }
        String balHomePath = buildContext.get(BuildContextField.HOME_REPO).toString();
        String ballerinaVersion = RepoUtils.getBallerinaVersion();
        String runtimeJarName = "ballerina-rt-" + ballerinaVersion + BLANG_COMPILED_JAR_EXT;
        return Paths.get(balHomePath, "bre", "lib", runtimeJarName);
    }
}
