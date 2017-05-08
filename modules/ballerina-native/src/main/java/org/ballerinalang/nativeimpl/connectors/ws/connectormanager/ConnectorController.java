/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.connectors.ws.connectormanager;

import org.ballerinalang.nativeimpl.connectors.ws.Utils;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import javax.websocket.Session;

/**
 * This manages single client connector which include multiple connections per connector.
 */
public class ConnectorController {

    private final String connectorID;
    private int connectionSubCount = 0;
    private final Queue<String> connectionIDPool = new LinkedList<>();
    // Map<clientID, session>
    private final Map<String, Session> clientIDToSessionMap = new HashMap<>();
    // Map<sessionID, clientID>
    private final Map<String, String> sessionIDToClientIDMap = new HashMap<>();

    public ConnectorController(String connectorID) {
        this.connectorID = connectorID;
    }

    /**
     * Finds if the client exists.
     *
     * @param session The session to find whether the client exists.
     * @return true if client exists.
     */
    public boolean clientExists(Session session) {
        return sessionIDToClientIDMap.containsKey(session.getId());
    }

    /**
     * Get the unique id of the connection.
     *
     * @param session the connection which needed to be added.
     * @return the connectionID of the session.
     */
    public String getClientID(Session session) {
        if (sessionIDToClientIDMap.containsKey(session.getId())) {
             return sessionIDToClientIDMap.get(session.getId());
        } else {
            throw new BallerinaException("Internal error occurred while generating sending message.");
        }
    }

    /**
     * Remove the connection from the connector manager.
     *
     * @param session the connection which need to be removed.
     */
    public void removeClient(Session session) {
        clientIDToSessionMap.remove(sessionIDToClientIDMap.remove(session.getId()));
    }

    /**
     * Remove the connection from connector manager.
     *
     * @param clientID ID of the client to be removed.
     */
    public void removeClient(String clientID) {
        sessionIDToClientIDMap.remove(clientIDToSessionMap.remove(clientID).getId());
    }

    /**
     * Retrieve all the client IDs from the connectionToConnectorIDMap.
     *
     * @return all the client IDs.
     */
    public List<String> getAllClientIDs() {
        return clientIDToSessionMap.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    /**
     * Retrieve the ID of this connector manager.
     *
     * @return the id of the connector manager.
     */
    public String getConnectorID() {
        return connectorID;
    }

    /**
     * Add connections to connector controller.
     *
     * @param session the session which should be added with unique identifier.
     * @return the unique client ID generated by the connector controller.
     */
    public String addConnection(Session session) {
        String clientID;
        if (connectionIDPool.isEmpty()) {
            connectionSubCount = connectionSubCount + 1;
            clientID = Utils.generateWebSocketClientID(connectorID, connectionSubCount);
        } else {
            clientID = connectionIDPool.remove();
        }
        sessionIDToClientIDMap.put(session.getId(), clientID);
        clientIDToSessionMap.put(clientID, session);
        return clientID;
    }

    /**
     * Retrieve the connection from client ID.
     *
     * @param clientID the client ID of the Session.
     * @return the session mapped to the client ID.
     */
    public Session getConnectionFromClientID(String clientID) {
        return clientIDToSessionMap.get(clientID);
    }
}
