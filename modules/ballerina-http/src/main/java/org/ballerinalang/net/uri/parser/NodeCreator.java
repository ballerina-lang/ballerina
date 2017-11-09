/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.uri.parser;

/**
 * Node creator is used to create necessary node node for a given item.
 * This is where the developer should create there own node to be passed into the tree.
 *
 * @param <NODE_ITEM> Specific node item created by the user.
 */
public interface NodeCreator<NODE_ITEM extends NodeItem> {

    /**
     * Creates a node with the developers own requirement.
     *
     * @param expression Expression of the node.
     * @return Node which response to T node item.
     */
    Node<NODE_ITEM> createNode(PathSegment expression);
}
