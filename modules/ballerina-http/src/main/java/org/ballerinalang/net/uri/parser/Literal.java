/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.uri.parser;


import org.ballerinalang.net.uri.URITemplateException;

import java.util.List;
import java.util.Map;

/**
 * Literal represents literal path segments in the uri-template.
 */
public class Literal implements PathSegment {

    private int tokenLength = 0;
    private final String token;

    public Literal(String token) throws URITemplateException {
        this.token = token;
        tokenLength = token.length();
        if (tokenLength == 0) {
            throw new URITemplateException("Invalid literal token with zero length");
        }
    }

    @Override
    public String expand(Map<String, String> variables) {
        return token;
    }

    @Override
    public int match(List<? extends Node> childNodesList, String uriFragment, Map<String, String> variables) {
        if (!token.endsWith("*")) {
            if (uriFragment.length() < tokenLength) {
                return -1;
            }
            for (int i = 0; i < tokenLength; i++) {
                if (token.charAt(i) != uriFragment.charAt(i)) {
                    if (token.charAt(i) == '*' && i == token.length() - 1) {
                        return uriFragment.length();
                    }
                    return -1;
                }
            }
            return tokenLength;
        } else {
            if (uriFragment.length() < tokenLength - 1) {
                return -1;
            }
            for (int i = 0; i < tokenLength - 1; i++) {
                if (token.charAt(i) != uriFragment.charAt(i)) {
                    if (i == token.length() - 1) {
                        return uriFragment.length();
                    }
                    return -1;
                }
            }
            return uriFragment.length();
        }
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public char getFirstCharacter() {
        return token.charAt(0);
    }
}
