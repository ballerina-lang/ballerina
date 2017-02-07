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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.message.BallerinaMessageDataSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

    // The streaming JSON data source object
    private JSONDataSource datasource;

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
        this.datasource = datasource;
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
            /* the below order is important, where if the value is generated from a streaming data source,
             * it should be able to serialize the data out again using the value */
            if (this.value != null) {
                this.value.serialize(gen, null);
            } else {
                this.datasource.serialize(gen);
            }
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
        if (this.value == null) {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            try {
                JsonGenerator gen = JSON_FAC.createGenerator(byteOut);
                this.datasource.serialize(gen);
                gen.close();
                this.value = OBJECT_MAPPER.readTree(byteOut.toByteArray());
            } catch (IOException e) {
                throw new BallerinaException("Error in building JSON node", e);
            }
        }
        return this.value;
    }

    @Override
    public String stringValue() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this.value());
        } catch (JsonProcessingException e) {
            throw new BallerinaException("Error in converting JsonNode to String", e);
        }
    }

    @Override
    public String getMessageAsString() {
        return this.value.toString();
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
