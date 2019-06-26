/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.auth;

import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.HttpsClientRequest;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for ldap user store based authentication/authorization scenarios.
 *
 * @since 0.983.0
 */
@Test(groups = "auth-test")
public class LdapAuthStoreTest extends AuthBaseTest {

    private final int servicePort = 9111;
    private final int authzServicePort = 9112;

    @Test(description = "Test authenticate and authorize request against ldap auth store")
    public void testAuthenticationWithInvalidCredentials() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dmlqaXRoYTp2aWppdGhhQDEyMw==");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort,
                "ldapAuth/disableAuthz"), headersMap, serverInstance.getServerHome());
        assertUnauthorized(response);
    }

    @Test(description = "Test authenticate request against ldap auth store")
    public void testAuthenticationWithLDAPAuthstoreWithoutAuthorization() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dmlqaXRoYTpiYWxsZXJpbmE=");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort,
                "ldapAuth/disableAuthz"), headersMap, serverInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test authenticate and authorize request against ldap auth store")
    public void testAuthenticationWithLDAPAuthstoreWithAuthorization() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dmlqaXRoYTpiYWxsZXJpbmE=");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(servicePort,
                "ldapAuth/enableAuthz"), headersMap, serverInstance.getServerHome());
        assertOK(response);
    }

    @Test(description = "Test the failure of authorization request against ldap auth store")
    public void testAuthorizationFailureWithLDAPAuthStore() throws Exception {
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "Basic dmlqaXRoYTpiYWxsZXJpbmE=");
        HttpResponse response = HttpsClientRequest.doGet(serverInstance.getServiceURLHttps(authzServicePort,
                "auth/failAuthz"), headersMap, serverInstance.getServerHome());
        assertForbidden(response);
    }
}
