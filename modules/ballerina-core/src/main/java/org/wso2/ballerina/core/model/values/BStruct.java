/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.core.model.values;

import org.wso2.ballerina.core.model.BallerinaStruct;

/**
 * The {@code BStruct} represents athe value of a user defined struct in Ballerina
 *
 * @since 1.0.0
 */
public final class BStruct implements BRefType<BallerinaStruct> {

    private BallerinaStruct struct;
    private BValue[] structMemBlock;

    /**
     * Creates a struct with a single memory block
     */
    public BStruct() {
        this(null, new BValue[0]);
    }

    /**
     * Creates a struct with the given size of memory block.
     * 
     * @param struct            {@link BallerinaStruct} who's values will be stored by this {@code BStruct}
     * @param structMemBlock    Array of memory blocks to store values.
     */
    public BStruct(BallerinaStruct struct, BValue[] structMemBlock) {
        this.struct = struct;
        this.structMemBlock = structMemBlock;
    }

    /**
     * Get a value from a memory location, stored inside this struct.
     * 
     * @param offset    Offset of the memory location
     * @return          Value stored in the given memory location of this struct.
     */
    public BValue getValue(int offset) {
        return structMemBlock[offset];
    }

    /**
     * Set a value to a memory location of this struct.
     * 
     * @param offset    Offset of the memory location
     * @param bValue    Value to be stored in the given memory location of this struct.
     */
    public void setValue(int offset, BValue bValue) {
        this.structMemBlock[offset] = bValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BallerinaStruct value() {
        return struct;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String stringValue() {
        return null;
    }
}
