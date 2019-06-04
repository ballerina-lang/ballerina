## Module Overview

This module provides a JWT authentication provider, which can be used to authenticate using a JWT and the functionality related to issuing a JWT and validating it.

The `jwt:JWTAuthProvider` is another implementation of the `auth:AuthProvider` interface, which authenticates by validating a JWT.

#### Sample for issuing a JWT

```ballerina
import ballerina/crypto;
import ballerina/time;
import ballerina/jwt;

public function main() {
    crypto:KeyStore keyStore = { path: "${ballerina.home}/bre/security/ballerinaKeystore.p12", password: "ballerina" };
    jwt:JWTIssuerConfig config = {
        keyStore: keyStore,
        keyAlias: "ballerina",
        keyPassword: "ballerina"
    };

    jwt:JwtHeader header = {};
    header.alg = jwt:RS256;
    header.typ = "JWT";

    jwt:JwtPayload payload = {};
    payload.sub = "John";
    payload.iss = "wso2";
    payload.jti = "100078234ba23";
    payload.aud = ["ballerina", "ballerinaSamples"];
    payload.exp = time:currentTime().time/1000 + 600;

    string|error jwt = jwt:issueJwt(header, payload, config);
}
```

#### Sample for validating a JWT

```ballerina
import ballerina/crypto;
import ballerina/jwt;

public function main() {
    crypto:TrustStore trustStore = { path: "${ballerina.home}/bre/security/ballerinaTruststore.p12", password: "ballerina" };
    jwt:JWTValidatorConfig config = {
        issuer: "wso2",
        certificateAlias: "ballerina",
        audience: ["ballerinaSamples"],
        clockSkew: 60,
        trustStore: trustStore
    };

    jwt:JwtPayload|error result = jwt:validateJwt(jwtToken, config);
}
```
