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

package org.ballerinalang.stdlib.utils;

import io.ballerina.projects.BaloWriter;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.model.Target;
import io.ballerina.projects.utils.ProjectUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class providing utility methods to generate balo from package.
 *
 * @since 2.0.0
 */
public class BuildLangLib {

    static Path projectDir;

    public static void main(String[] args) throws IOException {
        PrintStream out = System.out;
        projectDir = Paths.get(args[0]);
        out.println("Building langlib ...");
        out.println("Project Dir: " + projectDir);
        Project project = BuildProject.loadProject(projectDir);
        Target target = new Target(projectDir);
        Package pkg = project.currentPackage();
        /*PackageCompilation packageCompilation = pkg.getCompilation();
        if ( packageCompilation.diagnostics().size() > 0) {
            out.println("Error building module");
            packageCompilation.diagnostics().forEach(d -> out.println(d.toString()));
            System.exit(1);
        }*/
        String baloName = ProjectUtils.getBaloName(
                pkg.packageOrg().toString(),
                pkg.packageName().toString(),
                pkg.packageVersion().toString(),
                null);
        // TODO: check why package compilation throws a null pointer
//        PackageCompilation packageCompilation = pkg.getCompilation();
//        packageCompilation.emit(PackageCompilation.OutputType.BALO, target.baloPath().resolve(baloName));
        BaloWriter.write(pkg, target.getBaloPath(project.currentPackage()).resolve(baloName));
    }

}
