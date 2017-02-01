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
package org.wso2.ballerina.core.model.values;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.BaseJsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.message.BallerinaMessageDataSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * {@code BJSON} represents a JSON value in Ballerina.
 *
 * @since 0.8.0
 */
public final class BJSON extends BallerinaMessageDataSource implements BRefType<JsonNode> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    static {
        OBJECT_MAPPER.configure(Feature.ALLOW_SINGLE_QUOTES, true);
    }
    
    private static final JsonFactory JSON_FAC = new JsonFactory();
    
    // GSON json object model associated with this JSONType object
    private JsonNode value;

    // Schema of this JSONType object model
    private JsonNode schema;

    // Output stream to write message out to the socket
    private OutputStream outputStream;
    
    /**
     * Initialize a {@link BJSON} from a {@link com.google.gson.JsonElement} object.
     *
     * @param json json object
     */
    public BJSON(JsonNode json) {
        this.value = json;
    }

    /**
     * Initialize a {@link BJSON} from a JSON string.
     *
     * @param jsonString A JSON string
     */
    public BJSON(String jsonString) {
        this(jsonString, null);
    }
    
    /**
     * Initialize a {@link BJSON} from a streaming datasource.
     * @param datasource
     */
    public BJSON(JSONDataSource datasource) {
        this(new StreamingJSONNode(datasource));
    }

    /**
     * Initialize a {@link BJSON} from a string, with a specified schema.
     * JSON will not be validated against the given schema.
     *
     * @param jsonString JSON String
     * @param schema     Schema of the provided JSON, as a string
     */
    public BJSON(String jsonString, String schema) {
        try {
            if (jsonString != null && !jsonString.isEmpty()) {
                this.value = OBJECT_MAPPER.readTree(jsonString);
            } else {
                throw new IllegalArgumentException("Cannot parse an empty string to json.");
            }
            if (schema != null) {
                this.schema = OBJECT_MAPPER.readTree(schema);
            }
        } catch (IOException e) {
            throw new BallerinaException("Error in creating JSON content: " + 
                    jsonString + " - " + schema, e);
        }
    }

    /**
     * Create a {@link BJSON} from a {@link InputStream}.
     *
     * @param in Input Stream
     */
    public BJSON(InputStream in) {
        this(in, null);
    }

    /**
     * Create a {@link BJSON} from a {@link InputStream}.
     *
     * @param in InputStream
     */
    public BJSON(InputStream in, String schema) {
        try {
            this.value = OBJECT_MAPPER.readTree(in);
            if (schema != null) {
                this.schema = OBJECT_MAPPER.readTree(schema);
            }
        } catch (IOException e) {
            throw new BallerinaException("Error in creating JSON content: " + schema, e);
        }
    }

    /**
     * Return the string representation of this json object.
     */
    public String toString() {
        return this.stringValue();
    }

    /**
     * Set the value associated with this {@link BJSON} object.
     *
     * @param value Value associated with this {@link BJSON} object.
     */
    public void setValue(JsonNode value) {
        this.value = value;
    }

    /**
     * Get the schema associated with this {@link BJSON} object.
     *
     * @return Schema associated with this {@link BJSON} object
     */
    public JsonNode getSchema() {
        return this.schema;
    }

    /**
     * Set the schema associated with this {@link BJSON} object.
     *
     * @param schema Schema associated with this {@link BJSON} object.
     */
    public void setSchema(JsonNode schema) {
        this.schema = schema;
    }

    @Override
    public void serializeData() {
        try {
            JsonGenerator gen = JSON_FAC.createGenerator(this.outputStream);
            this.value.serialize(gen, null);
            gen.flush();
        } catch (IOException e) {
            throw new BallerinaException("Error occurred during writing the message to the output stream", e);
        }
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Get value associated with this {@link BJSON} object.
     *
     * @return JSON object associated with this {@link BJSON} object
     */
    @Override
    public JsonNode value() {
        return value;
    }
    
    @Override
    public String stringValue() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this.value);
        } catch (JsonProcessingException e) {
            throw new BallerinaException("Error in converting JsonNode to String", e);
        }
    }
    
    /**
     * A streaming {@link JsonNode} implementation based on {@link JSONDataSource}.
     */
    private static class StreamingJSONNode extends BaseJsonNode {
        
        private JSONDataSource datasource;
        
        private JsonNode jsonNode;
        
        public StreamingJSONNode(JSONDataSource datasource) {
            this.datasource = datasource;
        }
        
        private void checkAndBuildNode() {
            if (this.jsonNode == null) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                try {
                    JsonGenerator gen = JSON_FAC.createGenerator(byteOut);
                    this.datasource.serialize(gen);
                    gen.close();
                    this.jsonNode = OBJECT_MAPPER.readTree(byteOut.toByteArray());
                } catch (IOException e) {
                    throw new BallerinaException("Error in building JSON node", e);
                }
            }
        }

        @Override
        public JsonToken asToken() {
            this.checkAndBuildNode();
            return this.jsonNode.asToken();
        }

        @Override
        public int hashCode() {
            this.checkAndBuildNode();
            return this.jsonNode.hashCode();
        }

        @Override
        public void serialize(JsonGenerator gen, SerializerProvider sp) throws IOException, JsonProcessingException {
            if (this.jsonNode == null) {
                this.datasource.serialize(gen);
            } else {
                this.jsonNode.serialize(gen, sp);
            }
        }

        @Override
        public void serializeWithType(JsonGenerator gen, SerializerProvider sp, TypeSerializer ts)
                throws IOException, JsonProcessingException {
            if (this.jsonNode == null) {
                this.datasource.serialize(gen);
            } else {
                this.jsonNode.serializeWithType(gen, sp, ts);
            }
        }

        @Override
        protected JsonNode _at(JsonPointer ptr) {
            this.checkAndBuildNode();
            return this.jsonNode.at(ptr);
        }

        @Override
        public String asText() {
            this.checkAndBuildNode();
            return this.jsonNode.asText();
        }

        @Override
        public <T extends JsonNode> T deepCopy() {
            this.checkAndBuildNode();
            return this.jsonNode.deepCopy();
        }

        @Override
        public boolean equals(Object obj) {
            this.checkAndBuildNode();
            return this.jsonNode.equals(obj);
        }

        @Override
        public JsonNode findParent(String fieldName) {
            this.checkAndBuildNode();
            return this.jsonNode.findParent(fieldName);
        }

        @Override
        public List<JsonNode> findParents(String fieldName, List<JsonNode> foundSoFar) {
            this.checkAndBuildNode();
            return this.jsonNode.findParents(fieldName, foundSoFar);
        }

        @Override
        public JsonNode findValue(String fieldValue) {
            this.checkAndBuildNode();
            return this.jsonNode.findValue(fieldValue);
        }

        @Override
        public List<JsonNode> findValues(String fieldName, List<JsonNode> foundSoFar) {
            this.checkAndBuildNode();
            return this.jsonNode.findValues(fieldName, foundSoFar);
        }

        @Override
        public List<String> findValuesAsText(String fieldName, List<String> foundSoFar) {
            this.checkAndBuildNode();
            return this.jsonNode.findValuesAsText(fieldName, foundSoFar);
        }

        @Override
        public JsonNode get(int index) {
            this.checkAndBuildNode();
            return this.jsonNode.get(index);
        }

        @Override
        public JsonNodeType getNodeType() {
            this.checkAndBuildNode();
            return this.jsonNode.getNodeType();
        }

        @Override
        public JsonNode path(String fieldName) {
            this.checkAndBuildNode();
            return this.jsonNode.path(fieldName);
        }

        @Override
        public JsonNode path(int index) {
            this.checkAndBuildNode();
            return this.jsonNode.path(index);
        }

        @Override
        public String toString() {
            this.checkAndBuildNode();
            return this.jsonNode.toString();
        }
        
    }
    
    /**
     * This represents a JSON data source implementation, which should be used for custom JSON
     * streaming implementations.
     */
    public static interface JSONDataSource {
        
        /**
         * Serializes the current representation of the JSON data source to the given {@link JsonGenerator}.
         * @param gen The {@link JsonGenerator} object to write the data to
         * @throws IOException Error occurs while serializing
         */
        void serialize(JsonGenerator gen) throws IOException;
        
    }

}
