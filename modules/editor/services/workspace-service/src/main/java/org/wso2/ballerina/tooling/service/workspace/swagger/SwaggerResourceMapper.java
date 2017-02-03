/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.ballerina.tooling.service.workspace.swagger;

import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.nativeimpl.connectors.http.Constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class will do resource mapping from ballerina to swagger.
 */
public class SwaggerResourceMapper {
    private Resource resource;
    private Operation operation;

    /**
     * Get Swagger operation object associated with current resource
     *
     * @return Swagger operation object associated with current resource
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * Set Swagger operation object associated with current resource
     *
     * @param operation Swagger operation object associated with current resource
     */
    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    /**
     * Get Ballerina Resource object associated with current resource
     *
     * @return Ballerina Resource object associated with current resource
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * Set Ballerina Resource object associated with current resource
     *
     * @param resource Ballerina Resource object associated with current resource
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /**
     * This method will convert ballerina resource to swagger path objects.
     *
     * @param resources @Resource array to be convert.
     * @return map of string and swagger path objects.
     */
    protected Map<String, Path> convertResourcesToOperations(Resource[] resources) {
        Map<String, Path> map = new ConcurrentHashMap<>();
        for (Resource subResource : resources) {
            OperationAdaptor operation = convertResourceToOperation(subResource);
            Path path = map.get(operation.getPath());
            //TODO this check need to be improve to avoid repetition checks and http head support need to add.
            if (path == null) {
                Path newPath = new Path();
                map.put(operation.getPath(), newPath);
            }
            if (operation.getHttpOperation().equalsIgnoreCase(Constants.ANNOTATION_METHOD_GET)) {
                map.get(operation.getPath()).get(operation.getOperation());
            }
            if (operation.getHttpOperation().equalsIgnoreCase(Constants.ANNOTATION_METHOD_PUT)) {
                map.get(operation.getPath()).put(operation.getOperation());
            }
            if (operation.getHttpOperation().equalsIgnoreCase(Constants.ANNOTATION_METHOD_POST)) {
                map.get(operation.getPath()).post((operation.getOperation()));
            }
            if (operation.getHttpOperation().equalsIgnoreCase(Constants.ANNOTATION_METHOD_DELETE)) {
                map.get(operation.getPath()).delete(operation.getOperation());
            }
            if (operation.getHttpOperation().equalsIgnoreCase(Constants.ANNOTATION_METHOD_OPTIONS)) {
                map.get(operation.getPath()).options(operation.getOperation());
            }
            if (operation.getHttpOperation().equalsIgnoreCase(Constants.ANNOTATION_METHOD_PATCH)) {
                map.get(operation.getPath()).patch(operation.getOperation());
            }
        }
        return map;
    }




    /**
     * TODO need to implement
     * @param pathMap
     * @return
     */
    protected Resource[] convertOperationsToResources(Map<String, Path> pathMap) {
        return null;

    }

    /**
     * This method will convert ballerina @Resource to ballerina @OperationAdaptor
     *
     * @param resource @Resource array to be convert.
     * @return @OperationAdaptor of string and swagger path objects.
     */
    private OperationAdaptor convertResourceToOperation(Resource resource) {
        OperationAdaptor op = new OperationAdaptor();
        if (resource != null) {
            Annotation[] resourceAnnotations = resource.getResourceAnnotations();
            //Adding default response
            //TODO need to implement nested response support and then use response annotation.
            Response response = new Response()
                    .description("Successful")
                    .example("application/json", "Ok");
            op.getOperation().response(200, response);
            //Default path should be /
            String path = "/";
            op.setPath(path);
            Map<String, Annotation> annotationMap = resource.getAnnotationMap();
            if(annotationMap!=null) {
                for (Map.Entry<String, Annotation> operationEntry : annotationMap.entrySet()) {
                    if (operationEntry.getKey().matches(MapperConstants.httpVerbMatchingPattern)) {
                        op.setHttpOperation(operationEntry.getKey());
                    }

                }
            }
            if(resourceAnnotations!=null) {
                //TODO add all supported annotation mapping after annotation model finalized.
                for (Annotation annotation : resourceAnnotations) {
                    if (annotation.getName().equalsIgnoreCase("Consumes")) {
                        op.getOperation().consumes(annotation.getValue());
                    } else if (annotation.getName().equalsIgnoreCase("Produces")) {
                        op.getOperation().produces(annotation.getValue());
                    } else if (annotation.getName().equalsIgnoreCase("Path")) {
                        op.setPath(annotation.getValue());
                    } else if (annotation.getName().matches(MapperConstants.httpVerbMatchingPattern)) {
                        op.setHttpOperation(annotation.getName());
                    }
                /*
                Other annotations do not support by swagger.
                //TODO process them and use if we can map to generic attributes.
                else {
                    if(annotation.getName()!=null && annotation.getValue()!= null){
                    }
                }*/
                }
            }
        }
        return op;

    }

}
