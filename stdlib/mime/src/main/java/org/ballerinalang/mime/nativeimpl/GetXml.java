/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.mime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.mime.util.EntityBodyHandler.isStreamingRequired;
import static org.ballerinalang.mime.util.MimeConstants.FIRST_PARAMETER_INDEX;

/**
 * Get the entity body in xml form.
 *
 * @since 0.963.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "mime",
        functionName = "getXml",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Entity", structPackage = "ballerina/mime"),
        returnType = {@ReturnType(type = TypeKind.XML), @ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class GetXml extends AbstractGetPayloadHandler {

    @Override
    @SuppressWarnings("unchecked")
    public void execute(Context context, CallableUnitCallback callback) {
        try {
            BXML result;
            BMap<String, BValue> entityObj = (BMap<String, BValue>) context.getRefArgument(FIRST_PARAMETER_INDEX);
            BValue dataSource = EntityBodyHandler.getMessageDataSource(entityObj);
            if (dataSource != null) {
                if (dataSource instanceof BXML) {
                    result = (BXML) dataSource;
                } else {
                    // Build the XML from string representation of the payload.
                    BString payload = MimeUtil.getMessageAsString(dataSource);
                    result = XMLUtils.parse(payload.stringValue());
                }
                setReturnValuesAndNotify(context, callback, result);
                return;
            }

            if (isStreamingRequired(entityObj)) {
                result = EntityBodyHandler.constructXmlDataSource(entityObj);
                updateDataSourceAndNotify(context, callback, entityObj, result);
            } else {
                constructNonBlockingDataSource(context, callback, entityObj, SourceType.XML);
            }
        } catch (Exception ex) {
            createErrorAndNotify(context, callback,
                                 "Error occurred while extracting xml data from entity : " + ex.getMessage());
        }
    }
}
