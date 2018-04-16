/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile.ProgramFile;

import java.util.stream.Stream;

/**
 * @since 0.94
 */
public class Compiler {

    private static final CompilerContext.Key<Compiler> COMPILER_KEY =
            new CompilerContext.Key<>();

    private final SourceDirectoryManager sourceDirectoryManager;
    private final SymbolEnter symbolEnter;
    private final CompilerDriver compilerDriver;
    private final BinaryFileWriter binaryFileWriter;
    private final DependencyTree dependencyTree;
    private final BLangDiagnosticLog dlog;
    private final PackageLoader pkgLoader;

    public static Compiler getInstance(CompilerContext context) {
        Compiler compiler = context.get(COMPILER_KEY);
        if (compiler == null) {
            compiler = new Compiler(context);
        }
        return compiler;
    }

    private Compiler(CompilerContext context) {
        context.put(COMPILER_KEY, this);

        this.sourceDirectoryManager = SourceDirectoryManager.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.compilerDriver = CompilerDriver.getInstance(context);
        this.binaryFileWriter = BinaryFileWriter.getInstance(context);
        this.dependencyTree = DependencyTree.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.pkgLoader = PackageLoader.getInstance(context);
    }

    public BLangPackage compile(String sourcePackage) {
        PackageID packageID = this.sourceDirectoryManager.getPackageID(sourcePackage);
        if (packageID == null) {
            throw ProjectDirs.getPackageNotFoundError(sourcePackage);
        }

        return compile(packageID);
    }

    public BLangPackage compile(PackageID packageID) {
        // TODO This is hack to load the builtin package. We will fix this with BALO support
        this.compilerDriver.loadBuiltinPackage();

        // 1) Load the source package. i.e. source-code -> BLangPackageNode
        BLangPackage packageNode = this.pkgLoader.loadPackage(packageID);
        if (packageNode == null) {
            throw ProjectDirs.getPackageNotFoundError(packageID);
        }

        // Check for syntax errors
        if (dlog.errorCount > 0) {
            return packageNode;
        }

        // 2) Define all package level symbols for all the packages including imported packages in the AST
        // 3) Invoke compiler phases. e.g. type_check, code_analyze, taint_analyze, desugar etc.
        this.compilerDriver.define(packageNode);
        if (dlog.errorCount > 0) {
            return packageNode;
        }

        this.compilerDriver.compilePackage(packageNode);
        return packageNode;
    }

    public Stream<BLangPackage> loadAllPackages() {
        // TODO This is hack to load the builtin package. We will fix this with BALO support
        this.compilerDriver.loadBuiltinPackage();

        // 1) Load all source packages. i.e. source-code -> BLangPackageNode
        // 2) Define all package level symbols for all the packages including imported packages in the AST
        // 3) Invoke compiler phases. e.g. type_check, code_analyze, taint_analyze, desugar etc.
        Stream<BLangPackage> packages = this.sourceDirectoryManager.listSourceFilesAndPackages()
                                                                   .map(this.pkgLoader::loadPackage)
                                                                   .map(this::define)
                                                                   .map(this.compilerDriver::compilePackage)
                                                                   .filter(bLangPackage -> this.dlog.errorCount == 0);

        if (dlog.errorCount > 0) {
            // Check for compilation errors if there are any compilation errors don't write BALOs or BALXs
            return Stream.empty();
        }
        return packages;
    }
    public void build() {
        loadAllPackages().forEach(this.binaryFileWriter::writeExecutableBinary);
    }

    public void list() {
        loadAllPackages().forEach(this.dependencyTree::listDependencyPackages);
    }

    public void build(String sourcePackage, String targetFileName) {
        BLangPackage bLangPackage = compile(sourcePackage);
        if (this.dlog.errorCount > 0) {
            return;
        }

        // Code gen and save...
        this.binaryFileWriter.writeExecutableBinary(bLangPackage, targetFileName);
    }

    public ProgramFile getExecutableProgram(BLangPackage entryPackageNode) {
        if (dlog.errorCount > 0) {
            return null;
        }
        return this.binaryFileWriter.genExecutable(entryPackageNode);
    }


    // private methods

    public BLangPackage define(BLangPackage packageNode) {
        return this.symbolEnter.definePackage(packageNode);
    }
}
