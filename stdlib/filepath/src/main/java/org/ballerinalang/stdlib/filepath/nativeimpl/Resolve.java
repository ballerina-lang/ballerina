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

package org.ballerinalang.stdlib.filepath.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.filepath.Constants;
import org.ballerinalang.stdlib.filepath.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotLinkException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The native class to get real value of the path after evaluating any symbolic links.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = Constants.ORG_NAME,
        packageName = Constants.PACKAGE_NAME,
        functionName = "resolve",
        isPublic = true
)
public class Resolve extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String inputPath = context.getStringArgument(0);
        try {
            Path realPath = Files.readSymbolicLink(Paths.get(inputPath).toAbsolutePath());
            context.setReturnValues(new BString(realPath.toString()));
        } catch (NotLinkException ex) {
            context.setReturnValues(Utils.getPathError("NOT_LINK_ERROR", ex));
        } catch (IOException ex) {
            context.setReturnValues(Utils.getPathError("IO_ERROR", ex));
        } catch (SecurityException ex) {
            context.setReturnValues(Utils.getPathError("SECURITY_ERROR", ex));
        }
    }
}
