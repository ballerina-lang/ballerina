/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langlib.xml;

import org.ballerinalang.jvm.api.values.BString;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.XMLValue;

/**
 * Returns the attribute map of xml element.
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.xml",
//        functionName = "getAttributes",
//        args = {@Argument(name = "xmlValue", type = TypeKind.XML)},
//        returnType = {@ReturnType(type = TypeKind.MAP)},
//        isPublic = true
//)
public class GetAttributes {

    @SuppressWarnings("unchecked")
    public static MapValue<BString, BString> getAttributes(XMLValue xmlVal) {
        if (!IsElement.isElement(xmlVal)) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.XML_FUNC_TYPE_ERROR,
                    "getAttributes", "element");
        }

        return (MapValue<BString, BString>) xmlVal.getAttributesMap();
    }
}
