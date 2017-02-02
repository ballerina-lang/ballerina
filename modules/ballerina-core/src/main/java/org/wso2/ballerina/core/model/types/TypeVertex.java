/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.model.types;

import java.util.ArrayList;

/**
 * One node of the Type Lattice graph
 */
public class TypeVertex {

    private ArrayList<TypeEdge> neighborhood;
    private BType bType;
    private String packageName;

    /**
     * @param bType The unique bType associated with this Vertex
     * @param packageName Name of the package
     */
    public TypeVertex(BType bType, String packageName) {
        this.bType = bType;
        this.packageName = packageName;
        this.neighborhood = new ArrayList<>();
    }

    /**
     * @param bType The unique bType associated with this Vertex
     */
    public TypeVertex(BType bType) {
        this.bType = bType;
        this.neighborhood = new ArrayList<>();
    }


    /**
     * This method adds an TypeEdge to the incidence neighborhood of this graph iff
     * the edge is not already present.
     *
     * @param edge The edge to add
     */
    public void addNeighbor(TypeEdge edge) {
        if (this.neighborhood.contains(edge)) {
            return;
        }

        this.neighborhood.add(edge);
    }


    /**
     * @param other The edge for which to search
     * @return true iff other is contained in this.neighborhood
     */
    public boolean containsNeighbor(TypeEdge other) {
        return this.neighborhood.contains(other);
    }

    /**
     * @param index The index of the TypeEdge to retrieve
     * @return TypeEdge The TypeEdge at the specified index in this.neighborhood
     */
    public TypeEdge getNeighbor(int index) {
        return this.neighborhood.get(index);
    }


    /**
     * @param index The index of the edge to remove from this.neighborhood
     * @return TypeEdge The removed TypeEdge
     */
    TypeEdge removeNeighbor(int index) {
        return this.neighborhood.remove(index);
    }

    /**
     * @param e The TypeEdge to remove from this.neighborhood
     */
    public void removeNeighbor(TypeEdge e) {
        this.neighborhood.remove(e);
    }


    /**
     * @return int The number of neighbors of this Vertex
     */
    public int getNeighborCount() {
        return this.neighborhood.size();
    }


    /**
     * @return String The bType of this Vertex
     */
    public BType getType() {
        return this.bType;
    }


    /**
     * @return String A String representation of this Vertex
     */
    public String toString() {
        if (packageName == null) {
            return bType.toString();
        } else {
            return packageName + ":" + bType.toString();
        }
    }

    /**
     * @return The hash code of this Vertex's bType
     */
    public int hashCode() {
        if (packageName == null) {
            return this.bType.toString().hashCode();
        } else {
            return (packageName + ":" + this.bType.toString()).hashCode();
        }
    }

    /**
     * @param other The object to compare
     * @return true iff other instanceof Vertex and the two Vertex objects have the same bType
     */
    public boolean equals(Object other) {
        if (!(other instanceof TypeVertex)) {
            return false;
        }

        TypeVertex v = (TypeVertex) other;
        if (packageName == null) {
            return this.bType.equals(v.getType());
        } else {
            return this.bType.equals(v.getType()) && this.packageName.equals(v.getPackageName());
        }
    }

    /**
     * @return ArrayList<TypeEdge> A copy of this.neighborhood. Modifying the returned
     * ArrayList will not affect the neighborhood of this Vertex
     */
    public ArrayList<TypeEdge> getNeighbors() {
        return new ArrayList<TypeEdge>(this.neighborhood);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
