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

documentation {
    gRPC service configuration.

    F{{name}} - gRPC resource name. This applies only for client streaming and bidirectional streaming
                where we can define only one resource. In order to generate proto file, we need resource name.
    F{{clientStreaming}} - gRPC client streaming service flag. This applies only for servicestub streaming and
                           bidirectional streaming. Flag sets to true, if the service is client/bidirectional streaming.
    F{{serverStreaming}} - gRPC server streaming service flag. This applies only for client streaming and
                           bidirectional streaming. Flag sets to true, if the service is bidirectional streaming.
}
public type GrpcServiceConfig {
    string name;
    boolean clientStreaming;
    boolean serverStreaming;
};

documentation {
    gRPC service configuration annotation.
}
public annotation<service> ServiceConfig GrpcServiceConfig;

documentation {
    gRPC service resource configuration.

    F{{streaming}} - gRPC server streaming flag. This flag sets to true when service resource is considered as server
                     streaming.
}
public type GrpcResourceConfig {
    boolean streaming;
};

documentation {
    gRPC service resource configuration annotation.
}
public annotation<resource> ResourceConfig GrpcResourceConfig;

documentation {
    gRPC service descriptor data.

    F{{descriptor}} - gRPC server descriptor. Service descriptor sets at compile time.
}
public type ServiceDescriptorData {
    string descriptor;
};

documentation {
    gRPC service internal annotation which is to attach service descriptor generated at compile time.
}
public annotation <service> ServiceDescriptor ServiceDescriptorData;
