/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langlib.internal;

import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.api.values.BString;
import org.ballerinalang.jvm.api.values.BXML;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.XMLValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Return children matching provided condition.
 * When operating on a xml sequence, flat map on each xml item in the sequence.
 *
 * @since 1.2.0
 */
public class GetFilteredChildrenFlat {

    public static XMLValue getFilteredChildrenFlat(XMLValue xmlVal, long index, BString[] elemNames) {
        if (xmlVal.getNodeType() == XMLNodeType.ELEMENT) {
            XMLItem element = (XMLItem) xmlVal;
            return new XMLSequence(filterElementChildren(index, elemNames, element));
        } else if (xmlVal.getNodeType() == XMLNodeType.SEQUENCE) {
            XMLSequence sequence = (XMLSequence) xmlVal;
            ArrayList<BXML> liftedFilteredChildren = new ArrayList<>();
            for (BXML child : sequence.getChildrenList()) {
                if (child.getNodeType() == XMLNodeType.ELEMENT) {
                    liftedFilteredChildren.addAll(filterElementChildren(index, elemNames, (XMLItem) child));
                }
            }
            return new XMLSequence(liftedFilteredChildren);

        }
        return new XMLSequence();
    }

    private static List<BXML> filterElementChildren(long index, BString[] elemNames, XMLItem element) {
        XMLSequence elements = (XMLSequence) GetElements.getElements(element.getChildrenSeq(), elemNames);
        if (index < 0) {
            // Return all elements
            return elements.getChildrenList();
        } else if (elements.getChildrenList().size() > index) {
            // Valid index; return requested element
            return Collections.singletonList((elements.getChildrenList().get((int) index)));
        } else {
            // OutOfRange return empty list
            return new ArrayList<>();
        }
    }
}
