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
package io.ballerina.projects.internal;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.bir.BIRGen;
import org.wso2.ballerinalang.compiler.bir.codegen.CodeGenerator;
import org.wso2.ballerinalang.compiler.desugar.ConstantPropagation;
import org.wso2.ballerinalang.compiler.desugar.Desugar;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.analyzer.CodeAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.CompilerPluginRunner;
import org.wso2.ballerinalang.compiler.semantics.analyzer.DataflowAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.DocumentationAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.ObserverbilitySymbolCollectorRunner;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemanticAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TaintAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.spi.ObservabilitySymbolCollector;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;


import static org.ballerinalang.compiler.CompilerOptionName.TOOLING_COMPILATION;
import static org.ballerinalang.model.elements.PackageID.ANNOTATIONS;
import static org.ballerinalang.model.elements.PackageID.ARRAY;
import static org.ballerinalang.model.elements.PackageID.BOOLEAN;
import static org.ballerinalang.model.elements.PackageID.DECIMAL;
import static org.ballerinalang.model.elements.PackageID.ERROR;
import static org.ballerinalang.model.elements.PackageID.FLOAT;
import static org.ballerinalang.model.elements.PackageID.FUTURE;
import static org.ballerinalang.model.elements.PackageID.INT;
import static org.ballerinalang.model.elements.PackageID.INTERNAL;
import static org.ballerinalang.model.elements.PackageID.MAP;
import static org.ballerinalang.model.elements.PackageID.OBJECT;
import static org.ballerinalang.model.elements.PackageID.QUERY;
import static org.ballerinalang.model.elements.PackageID.STREAM;
import static org.ballerinalang.model.elements.PackageID.STRING;
import static org.ballerinalang.model.elements.PackageID.TABLE;
import static org.ballerinalang.model.elements.PackageID.TRANSACTION;
import static org.ballerinalang.model.elements.PackageID.TYPEDESC;
import static org.ballerinalang.model.elements.PackageID.VALUE;
import static org.ballerinalang.model.elements.PackageID.XML;

/**
 * This class drives the compilation of packages through various phases
 * such as symbol enter, semantic analysis, type checking, code analysis,
 * desugar and code generation.
 *
 * @since 2.0.0
 */
public class CompilerPhaseRunner {

    private static final CompilerContext.Key<CompilerPhaseRunner> COMPILER_DRIVER_KEY =
            new CompilerContext.Key<>();

    private final CompilerOptions options;
    private final BLangDiagnosticLog dlog;
    private final PackageLoader pkgLoader;
    private final PackageCache pkgCache;
    private final SymbolTable symbolTable;
    private final SymbolEnter symbolEnter;
    private final SymbolResolver symResolver;
    private final SemanticAnalyzer semAnalyzer;
    private final CodeAnalyzer codeAnalyzer;
    private final TaintAnalyzer taintAnalyzer;
    private final ConstantPropagation constantPropagation;
    private final DocumentationAnalyzer documentationAnalyzer;
    private final CompilerPluginRunner compilerPluginRunner;
    private final ObservabilitySymbolCollector observabilitySymbolCollector;
    private final Desugar desugar;
    private final BIRGen birGenerator;
    private final CodeGenerator codeGenerator;
    private final CompilerPhase compilerPhase;
    private final DataflowAnalyzer dataflowAnalyzer;
    private boolean isToolingCompilation;


    public static CompilerPhaseRunner getInstance(CompilerContext context) {
        CompilerPhaseRunner compilerDriver = context.get(COMPILER_DRIVER_KEY);
        if (compilerDriver == null) {
            compilerDriver = new CompilerPhaseRunner(context);
        }
        return compilerDriver;
    }

    private CompilerPhaseRunner(CompilerContext context) {
        context.put(COMPILER_DRIVER_KEY, this);

        this.options = CompilerOptions.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.pkgLoader = PackageLoader.getInstance(context);
        this.pkgCache = PackageCache.getInstance(context);
        this.symbolTable = SymbolTable.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.semAnalyzer = SemanticAnalyzer.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.codeAnalyzer = CodeAnalyzer.getInstance(context);
        this.documentationAnalyzer = DocumentationAnalyzer.getInstance(context);
        this.taintAnalyzer = TaintAnalyzer.getInstance(context);
        this.constantPropagation = ConstantPropagation.getInstance(context);
        this.compilerPluginRunner = CompilerPluginRunner.getInstance(context);
        this.observabilitySymbolCollector = ObserverbilitySymbolCollectorRunner.getInstance(context);
        this.desugar = Desugar.getInstance(context);
        this.birGenerator = BIRGen.getInstance(context);
        this.codeGenerator = CodeGenerator.getInstance(context);
        this.compilerPhase = this.options.getCompilerPhase();
        this.dataflowAnalyzer = DataflowAnalyzer.getInstance(context);
        this.isToolingCompilation = this.options.isSet(TOOLING_COMPILATION)
                && Boolean.parseBoolean(this.options.get(TOOLING_COMPILATION));
    }

    public BPackageSymbol getLangModuleFromSource(PackageID modID) {

        BLangPackage pkg = taintAnalyze(
                documentationAnalyzer.analyze(codeAnalyze(semAnalyzer.analyze(pkgLoader.loadAndDefinePackage(modID)))));
        if (dlog.errorCount() > 0) {
            return null;
        }

        return codeGen(birGen(desugar(pkg))).symbol;
    }

    /**
     * Load required lang modules needed for compilation.
     */
    public void loadRequiredLangModules() {
        symbolTable.langAnnotationModuleSymbol = pkgLoader.loadPackageSymbol(ANNOTATIONS, null, null);
        symbolTable.langInternalModuleSymbol = pkgLoader.loadPackageSymbol(INTERNAL, null, null);
        symResolver.reloadErrorAndDependentTypes();
        symResolver.reloadIntRangeType();
        symbolTable.langArrayModuleSymbol = pkgLoader.loadPackageSymbol(ARRAY, null, null);
        symbolTable.langDecimalModuleSymbol = pkgLoader.loadPackageSymbol(DECIMAL, null, null);
        symbolTable.langErrorModuleSymbol = pkgLoader.loadPackageSymbol(ERROR, null, null);
        symbolTable.langFloatModuleSymbol = pkgLoader.loadPackageSymbol(FLOAT, null, null);
        symbolTable.langFutureModuleSymbol = pkgLoader.loadPackageSymbol(FUTURE, null, null);
        symbolTable.langIntModuleSymbol = pkgLoader.loadPackageSymbol(INT, null, null);
        symbolTable.langMapModuleSymbol = pkgLoader.loadPackageSymbol(MAP, null, null);
        symbolTable.langObjectModuleSymbol = pkgLoader.loadPackageSymbol(OBJECT, null, null);
        symResolver.loadRawTemplateType();
        symbolTable.langStreamModuleSymbol = pkgLoader.loadPackageSymbol(STREAM, null, null);
        symbolTable.langTableModuleSymbol = pkgLoader.loadPackageSymbol(TABLE, null, null);
        symbolTable.langStringModuleSymbol = pkgLoader.loadPackageSymbol(STRING, null, null);
        symbolTable.langTypedescModuleSymbol = pkgLoader.loadPackageSymbol(TYPEDESC, null, null);
        symbolTable.langValueModuleSymbol = pkgLoader.loadPackageSymbol(VALUE, null, null);
        symbolTable.langXmlModuleSymbol = pkgLoader.loadPackageSymbol(XML, null, null);
        symbolTable.langBooleanModuleSymbol = pkgLoader.loadPackageSymbol(BOOLEAN, null, null);
        symbolTable.langQueryModuleSymbol = pkgLoader.loadPackageSymbol(QUERY, null, null);
        symbolTable.langTransactionModuleSymbol = pkgLoader.loadPackageSymbol(TRANSACTION, null, null);
        symbolTable.loadPredeclaredModules();
        symResolver.loadFunctionalConstructors();
    }

    public void compile(BLangPackage pkgNode) {
        if (this.stopCompilation(pkgNode, CompilerPhase.TYPE_CHECK)) {
            return;
        }

        typeCheck(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.CODE_ANALYZE)) {
            return;
        }

        codeAnalyze(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.DATAFLOW_ANALYZE)) {
            return;
        }

        dataflowAnalyze(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.DOCUMENTATION_ANALYZE)) {
            return;
        }

        documentationAnalyze(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.TAINT_ANALYZE)) {
            return;
        }

        taintAnalyze(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.CONSTANT_PROPAGATION)) {
            return;
        }

        propagateConstants(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.COMPILER_PLUGIN)) {
            return;
        }

        annotationProcess(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.OBSERVABILITY_DATA_GEN)) {
            return;
        }

        generateObservabilityData(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.DESUGAR)) {
            return;
        }

        desugar(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.BIR_GEN)) {
            return;
        }

        birGen(pkgNode);
        if (this.stopCompilation(pkgNode, CompilerPhase.CODE_GEN)) {
            return;
        }

        codeGen(pkgNode);
    }

    private BLangPackage codeGen(BLangPackage pkgNode) {
        return this.codeGenerator.generate(pkgNode);
    }

    private void generateObservabilityData(BLangPackage pkgNode) {
        this.observabilitySymbolCollector.process(pkgNode);
    }

    public BLangPackage define(BLangPackage pkgNode) {
        return this.symbolEnter.definePackage(pkgNode);
    }

    private BLangPackage typeCheck(BLangPackage pkgNode) {
        return this.semAnalyzer.analyze(pkgNode);
    }

    private BLangPackage documentationAnalyze(BLangPackage pkgNode) {
        return this.documentationAnalyzer.analyze(pkgNode);
    }

    private BLangPackage codeAnalyze(BLangPackage pkgNode) {
        return this.codeAnalyzer.analyze(pkgNode);
    }

    private BLangPackage dataflowAnalyze(BLangPackage pkgNode) {
        return this.dataflowAnalyzer.analyze(pkgNode);
    }

    private BLangPackage taintAnalyze(BLangPackage pkgNode) {
        return this.taintAnalyzer.analyze(pkgNode);
    }

    private BLangPackage propagateConstants(BLangPackage pkgNode) {
        return this.constantPropagation.perform(pkgNode);
    }

    private BLangPackage annotationProcess(BLangPackage pkgNode) {
        return this.compilerPluginRunner.runPlugins(pkgNode);
    }

    public BLangPackage desugar(BLangPackage pkgNode) {
        return this.desugar.perform(pkgNode);
    }

    public BLangPackage birGen(BLangPackage pkgNode) {
        return this.birGenerator.genBIR(pkgNode);
    }

    private boolean stopCompilation(BLangPackage pkgNode, CompilerPhase nextPhase) {
        if (compilerPhase.compareTo(nextPhase) < 0) {
            return true;
        }
        return (checkNextPhase(nextPhase) && dlog.errorCount() > 0);
    }

    private boolean checkNextPhase(CompilerPhase nextPhase) {
        return (!isToolingCompilation && nextPhase == CompilerPhase.CODE_ANALYZE) ||
                nextPhase == CompilerPhase.TAINT_ANALYZE ||
                nextPhase == CompilerPhase.COMPILER_PLUGIN ||
                nextPhase == CompilerPhase.DESUGAR;
    }
}
