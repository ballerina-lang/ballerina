/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.wso2.ballerina.core.nativeimpl.lang.xml;

import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.tree.tiny.TinyTextImpl;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.model.values.XMLValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;

/**
 * Evaluate xPath on a XML object and returns the matching string value.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.xml",
        functionName = "getString",
        args = {@Argument(name = "xml", type = TypeEnum.XML),
                @Argument(name = "xPath", type = TypeEnum.STRING)},
//                @Argument(name = "nameSpaces", type = TypeEnum.MAP)},
        returnType = {TypeEnum.STRING},
        isPublic = true
)
@Component(
        name = "func.lang.xml_getString",
        immediate = true,
        service = AbstractNativeFunction.class
)
public class GetString extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(GetString.class);

    @Override
    public BValue<?>[] execute(Context ctx) {
        // Accessing Parameters.
        XMLValue xml = (XMLValue) getArgument(ctx, 0).getBValue();
        String xPath = getArgument(ctx, 1).getString();
        //MapValue<String, String> nameSpaces = getArgument(ctx, 2).getMap();
        
        
        // Getting the value from XML
        BValue<?> result = null;
        try {
            Processor processor = new Processor(false);
            XPathCompiler xPathCompiler = processor.newXPathCompiler();
            DocumentBuilder builder = processor.newDocumentBuilder();
            XdmNode doc = builder.build(xml.getValue().getSAXSource(true));
            /*if (nameSpaces != null && !nameSpaces.isEmpty()) {
                for (MapValue<String, String>.MapEntry<String, String> entry : nameSpaces.getValue()) {
                    xPathCompiler.declareNamespace(entry.getKey(), entry.getValue());
                }
            }*/
            XPathSelector selector = xPathCompiler.compile(xPath).load();
            selector.setContextItem(doc);
            XdmValue xdmValue = selector.evaluate();
            Sequence sequence = xdmValue.getUnderlyingValue();

            if (sequence instanceof TinyTextImpl) {
                result = new StringValue(((TinyTextImpl) sequence).getStringValue());
            } else {
                String errorMsg = "The element matching path: " + xPath + " is not a String.";
                // log.error(errorMsg);
                throw new BallerinaException(errorMsg);
            }
        } catch (SaxonApiException e) {
            throw new BallerinaException("Cannot evaluate XPath: " + xPath, e);
        }
        
        // Setting output value.
        return getBValues(result);
    }
}
