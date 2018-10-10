// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.


// Please maintain alphabetical order when adding new header names
# HTTP header key `age`. Gives the current age of a cached HTTP response. 
public final string AGE = "age";

# HTTP header key `authorization` 
public final string AUTHORIZATION = "authorization";

# HTTP header key `cache-control`. Specifies the cache control directives required for the function of HTTP caches.
public final string CACHE_CONTROL = "cache-control";

# HTTP header key `content-length`. Specifies the size of the response body in bytes. 
public final string CONTENT_LENGTH = "content-length";

# HTTP header key `content-type`. Specifies the type of the message payload. 
public final string CONTENT_TYPE = "content-type";

# HTTP header key `date`. The timestamp at the time the response was generated/received. 
public final string DATE = "date";

# HTTP header key `etag`. A finger print for a resource which is used by HTTP caches to identify whether a
# resource representation has changed.
public final string ETAG = "etag";

# HTTP header key `expect`. Specifies expectations to be fulfilled by the server. 
public final string EXPECT = "expect";

# HTTP header key `expires`. Specifies the time at which the response becomes stale. 
public final string EXPIRES = "expires";

# HTTP header key `if-match` 
public final string IF_MATCH = "if-match";

# HTTP header key `if-modified-since`. Used when validating (with the origin server) whether a cached response
# is still valid. If the representation of the resource has modified since the timestamp in this field, a
# 304 response is returned.
public final string IF_MODIFIED_SINCE = "if-modified-since";

# HTTP header key `if-none-match`. Used when validating (with the origin server) whether a cached response is
# still valid. If the ETag provided in this field matches the representation of the requested resource, a
# 304 response is returned.
public final string IF_NONE_MATCH = "if-none-match";

# HTTP header key `if-range` 
public final string IF_RANGE = "if-range";

# HTTP header key `if-unmodified-since` 
public final string IF_UNMODIFIED_SINCE = "if-unmodified-since";

# HTTP header key `last-modified`. The time at which the resource was last modified. 
public final string LAST_MODIFIED = "last-modified";

# HTTP header key `location`. Indicates the URL to redirect a request to. 
public final string LOCATION = "location";

# HTTP header key `pragma`. Used in dealing with HTTP 1.0 caches which do not understand the `cache-control` header.
public final string PRAGMA = "pragma";

# HTTP header key `server`. Specifies the details of the origin server.
public final string SERVER = "server";

# HTTP header key `warning`. Specifies warnings generated when serving stale responses from HTTP caches. 
public final string WARNING = "warning";

# HTTP header key `transfer-encoding`. Specifies what type of transformation has been applied to entity body. 
public final string TRANSFER_ENCODING = "transfer-encoding";
