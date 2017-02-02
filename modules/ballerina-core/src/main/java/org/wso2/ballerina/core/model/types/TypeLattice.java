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

import org.wso2.ballerina.core.nativeimpl.lang.convertors.NativeCastConvertor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Class to hold the types and their connections within ballerina
 */
public class TypeLattice {

    protected final HashMap<String, TypeVertex> vertices = new HashMap<>();
    protected final HashMap<Integer, TypeEdge> edges = new HashMap<>();
    private static TypeLattice explicitCastLattice = new TypeLattice();
    private static TypeLattice implicitCastLattice = new TypeLattice();


    static {
        initExplicitCastWithBuiltInTypes();
        initImplicitCastWithBuiltInTypes();
    }

    public static TypeLattice getExplicitCastLattice() {
        return explicitCastLattice;
    }

    public static TypeLattice getImplicitCastLattice() {
        return implicitCastLattice;
    }

    private static void initImplicitCastWithBuiltInTypes() {

        TypeVertex intV = new TypeVertex(new BIntegerType(TypeEnum.INT.getName()));
        TypeVertex longV = new TypeVertex(new BLongType(TypeEnum.LONG.getName()));
        TypeVertex floatV = new TypeVertex(new BFloatType(TypeEnum.FLOAT.getName()));
        TypeVertex doubleV = new TypeVertex(new BDoubleType(TypeEnum.DOUBLE.getName()));
        TypeVertex stringV = new TypeVertex(new BStringType(TypeEnum.STRING.getName()));

        implicitCastLattice.addVertex(intV, false);
        implicitCastLattice.addVertex(longV, false);
        implicitCastLattice.addVertex(floatV, false);
        implicitCastLattice.addVertex(doubleV, false);
        implicitCastLattice.addVertex(stringV, false);

        implicitCastLattice.addEdge(intV, longV, NativeCastConvertor.INT_TO_LONG_FUNC);
        implicitCastLattice.addEdge(intV, floatV, NativeCastConvertor.INT_TO_FLOAT_FUNC);
        implicitCastLattice.addEdge(intV, doubleV, NativeCastConvertor.INT_TO_DOUBLE_FUNC);
        implicitCastLattice.addEdge(intV, stringV, NativeCastConvertor.INT_TO_STRING_FUNC);

        implicitCastLattice.addEdge(longV, floatV, NativeCastConvertor.LONG_TO_FLOAT_FUNC);
        implicitCastLattice.addEdge(longV, doubleV, NativeCastConvertor.LONG_TO_DOUBLE_FUNC);
        implicitCastLattice.addEdge(longV, stringV, NativeCastConvertor.LONG_TO_STRING_FUNC);

        implicitCastLattice.addEdge(floatV, doubleV, NativeCastConvertor.FLOAT_TO_DOUBLE_FUNC);
        implicitCastLattice.addEdge(floatV, stringV, NativeCastConvertor.FLOAT_TO_STRING_FUNC);

        implicitCastLattice.addEdge(doubleV, stringV, NativeCastConvertor.DOUBLE_TO_STRING_FUNC);
    }

    private static void initExplicitCastWithBuiltInTypes() {

        TypeVertex intV = new TypeVertex(new BIntegerType(TypeEnum.INT.getName()));
        TypeVertex longV = new TypeVertex(new BLongType(TypeEnum.LONG.getName()));
        TypeVertex floatV = new TypeVertex(new BFloatType(TypeEnum.FLOAT.getName()));
        TypeVertex doubleV = new TypeVertex(new BDoubleType(TypeEnum.DOUBLE.getName()));
        TypeVertex booleanV = new TypeVertex(new BBooleanType(TypeEnum.BOOLEAN.getName()));
        TypeVertex stringV = new TypeVertex(new BStringType(TypeEnum.STRING.getName()));
        TypeVertex xmlV = new TypeVertex(new BXMLType(TypeEnum.XML.getName()));
        TypeVertex jsonV = (new TypeVertex(new BJSONType(TypeEnum.JSON.getName())));

        explicitCastLattice.addVertex(intV, false);
        explicitCastLattice.addVertex(longV, false);
        explicitCastLattice.addVertex(floatV, false);
        explicitCastLattice.addVertex(doubleV, false);
        explicitCastLattice.addVertex(booleanV, false);
        explicitCastLattice.addVertex(stringV, false);
        explicitCastLattice.addVertex(xmlV, false);
        explicitCastLattice.addVertex(jsonV, false);

        explicitCastLattice.addEdge(intV, longV, NativeCastConvertor.INT_TO_LONG_FUNC);
        explicitCastLattice.addEdge(intV, floatV, NativeCastConvertor.INT_TO_FLOAT_FUNC);
        explicitCastLattice.addEdge(intV, doubleV, NativeCastConvertor.INT_TO_DOUBLE_FUNC);
        explicitCastLattice.addEdge(intV, stringV, NativeCastConvertor.INT_TO_STRING_FUNC);
        explicitCastLattice.addEdge(intV, intV, NativeCastConvertor.INT_TO_INT_FUNC);

        explicitCastLattice.addEdge(longV, intV, NativeCastConvertor.LONG_TO_INT_FUNC);
        explicitCastLattice.addEdge(longV, floatV, NativeCastConvertor.LONG_TO_FLOAT_FUNC);
        explicitCastLattice.addEdge(longV, doubleV, NativeCastConvertor.LONG_TO_DOUBLE_FUNC);
        explicitCastLattice.addEdge(longV, stringV, NativeCastConvertor.LONG_TO_STRING_FUNC);
        explicitCastLattice.addEdge(longV, longV, NativeCastConvertor.LONG_TO_LONG_FUNC);

        explicitCastLattice.addEdge(doubleV, longV, NativeCastConvertor.DOUBLE_TO_LONG_FUNC);
        explicitCastLattice.addEdge(doubleV, floatV, NativeCastConvertor.DOUBLE_TO_FLOAT_FUNC);
        explicitCastLattice.addEdge(doubleV, doubleV, NativeCastConvertor.DOUBLE_TO_DOUBLE_FUNC);
        explicitCastLattice.addEdge(doubleV, stringV, NativeCastConvertor.DOUBLE_TO_STRING_FUNC);
        explicitCastLattice.addEdge(doubleV, intV, NativeCastConvertor.DOUBLE_TO_INT_FUNC);

        explicitCastLattice.addEdge(floatV, longV, NativeCastConvertor.FLOAT_TO_LONG_FUNC);
        explicitCastLattice.addEdge(floatV, floatV, NativeCastConvertor.FLOAT_TO_FLOAT_FUNC);
        explicitCastLattice.addEdge(floatV, doubleV, NativeCastConvertor.FLOAT_TO_DOUBLE_FUNC);
        explicitCastLattice.addEdge(floatV, stringV, NativeCastConvertor.FLOAT_TO_STRING_FUNC);
        explicitCastLattice.addEdge(floatV, intV, NativeCastConvertor.FLOAT_TO_INT_FUNC);

        explicitCastLattice.addEdge(stringV, longV, NativeCastConvertor.STRING_TO_LONG_FUNC);
        explicitCastLattice.addEdge(stringV, floatV, NativeCastConvertor.STRING_TO_FLOAT_FUNC);
        explicitCastLattice.addEdge(stringV, doubleV, NativeCastConvertor.STRING_TO_DOUBLE_FUNC);
        explicitCastLattice.addEdge(stringV, stringV, NativeCastConvertor.STRING_TO_STRING_FUNC);
        explicitCastLattice.addEdge(stringV, intV, NativeCastConvertor.STRING_TO_INT_FUNC);

        explicitCastLattice.addEdge(booleanV, stringV, NativeCastConvertor.BOOLEAN_TO_STRING_FUNC);
        explicitCastLattice.addEdge(booleanV, booleanV, NativeCastConvertor.BOOLEAN_TO_BOOLEAN_FUNC);

        explicitCastLattice.addEdge(jsonV, stringV, NativeCastConvertor.JSON_TO_STRING_FUNC);
        explicitCastLattice.addEdge(xmlV, stringV, NativeCastConvertor.XML_TO_STRING_FUNC);
    }

    /**
     * Accepts two vertices and a weight, and adds the edge
     * ({one, two}, weight) iff no TypeEdge relating one and two
     * exists in the Graph.
     *
     * @param one           The first TypeVertex of the TypeEdge
     * @param two           The second TypeVertex of the TypeEdge
     * @param typeConvertor The weight of the TypeEdge
     * @return true iff no TypeEdge already exists in the Graph
     */
    public boolean addEdge(TypeVertex one, TypeVertex two, Function typeConvertor) {

        //ensures the TypeEdge is not in the Graph
        TypeEdge e = new TypeEdge(one, two, typeConvertor);
        if (this.edges.containsKey(e.hashCode())) {
            return false;
        } else if (one.containsNeighbor(e) || two.containsNeighbor(e)) {
            return false;
        }

        this.edges.put(e.hashCode(), e);
        one.addNeighbor(e);
        two.addNeighbor(e);
        return true;
    }

    public TypeEdge getEdgeFromTypes(BType source, BType target) {
        TypeEdge result = this.edges.get((source.toString() + target.toString()).hashCode());
        return result;
    }

    /**
     * @param e The TypeEdge to look up
     * @return true iff this Graph contains the TypeEdge e
     */
    public boolean containsEdge(TypeEdge e) {
        if (e.getSource() == null || e.getTarget() == null || e.getTypeConvertor() == null) {
            return false;
        }

        return this.edges.containsKey(e.hashCode());
    }


    /**
     * This method removes the specified TypeEdge from the Graph,
     * including as each vertex's incidence neighborhood.
     *
     * @param e The TypeEdge to remove from the Graph
     * @return TypeEdge The TypeEdge removed from the Graph
     */
    public TypeEdge removeEdge(TypeEdge e) {
        e.getSource().removeNeighbor(e);
        e.getTarget().removeNeighbor(e);
        return this.edges.remove(e.hashCode());
    }

    /**
     * @param vertex The TypeVertex to look up
     * @return true iff this Graph contains vertex
     */
    public boolean containsVertex(TypeVertex vertex) {
        return this.vertices.get(vertex.getType().toString()) != null;
    }

    /**
     * @param label The specified TypeVertex label
     * @return TypeVertex The TypeVertex with the specified label
     */
    public TypeVertex getVertex(String label) {
        return this.vertices.get(label);
    }

    /**
     * This method adds a TypeVertex to the graph. If a TypeVertex with the same label
     * as the parameter exists in the Graph, the existing TypeVertex is overwritten
     * only if overwriteExisting is true. If the existing TypeVertex is overwritten,
     * the Edges incident to it are all removed from the Graph.
     *
     * @param vertex
     * @param overwriteExisting
     * @return true iff vertex was added to the Graph
     */
    public boolean addVertex(TypeVertex vertex, boolean overwriteExisting) {
        TypeVertex current = this.vertices.get(vertex.getType().toString());
        if (current != null) {
            if (!overwriteExisting) {
                return false;
            }

            while (current.getNeighborCount() > 0) {
                removeEdge(current.getNeighbor(0));
            }
        }


        this.vertices.put(vertex.getType().toString(), vertex);
        return true;
    }

    /**
     * @param label The label of the TypeVertex to remove
     * @return TypeVertex The removed TypeVertex object
     */
    public TypeVertex removeVertex(String label) {
        TypeVertex v = this.vertices.remove(label);

        while (v.getNeighborCount() > 0) {
            removeEdge(v.getNeighbor((0)));
        }

        return v;
    }

    /**
     * @return Set<String> The unique labels of the Graph's TypeVertex objects
     */
    public Set<String> vertexKeys() {
        return this.vertices.keySet();
    }

    /**
     * @return Set<TypeEdge> The Edges of this graph
     */
    public Set<TypeEdge> getEdges() {
        return new HashSet<TypeEdge>(this.edges.values());
    }
}
