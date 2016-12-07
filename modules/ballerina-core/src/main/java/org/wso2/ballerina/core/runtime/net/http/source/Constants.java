/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.ballerina.core.runtime.net.http.source;

/**
 * Constants for HTTP
 *
 * @since 1.0.0
 */
public class Constants {

    public static final String BASE_PATH = "BASE_PATH";
    public static final String SUB_PATH = "SUB_PATH";

    public static final String DEFAULT_INTERFACE = "default_http_listener";
    public static final String DEFAULT_BASE_PATH = "/";
    public static final String DEFAULT_SUB_PATH = "/*";

    public static final String PROTOCOL_HTTP = "http";

    public static final String HTTP_METHOD = "HTTP_METHOD";
    /* HTTP Methods */
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_OPTIONS = "OPTIONS";

    /* Annotations */
    public static final String ANNOTATION_NAME_SOURCE = "Source";
    public static final String ANNOTATION_NAME_BASE_PATH = "BasePath";
    public static final String ANNOTATION_NAME_PATH = "Path";


    public static final String ANNOTATION_SOURCE_KEY_INTERFACE = "interface";
}
