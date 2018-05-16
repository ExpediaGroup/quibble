/**
 * Copyright (C) 2015-2018 Expedia Inc.
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
package com.hotels.hde.quibble.connection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.zaxxer.hikari.HikariDataSource;

import com.hotels.hde.quibble.Action;
import com.hotels.hde.quibble.Platforms;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.exceptions.InvalidConnectionDetailsException;

public class ConnectionManager {

  private final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

  private Map<String, HikariDataSource> platformDataSourceMap;

  private ConnectionLoader connectionLoader;

  private final List<ActionConnection> connectionList = new ArrayList<>();

  public void createRequiredConnectionPools(List<TestCase[]> allTests) throws IOException {
    Set<ConnectionDetails> connectionList = connectionLoader.getConnectionDetailsList(allTests);
    platformDataSourceMap = new ConnectionPoolsCreator().getHikariCPConnectionPoolsDataSources(connectionList);
    logger.info("{} connection pool created", platformDataSourceMap.size());
  }

  @Inject
  public ConnectionManager(ConnectionLoader connectionLoader) {
    this.connectionLoader = connectionLoader;
  }

  public void closeAllConnectionPools() {
    if (platformDataSourceMap != null) {

      for (Map.Entry<String, HikariDataSource> platform : platformDataSourceMap.entrySet()) {
        logger.info("Shutting down connection pool for: " + platform.getKey());
        platformDataSourceMap.get(platform.getKey()).close();
      }
    }
  }

  public List<ActionConnection> getRequiredConnections(TestCase aTestCase) {
    connectionList.clear();
    List<Action> actions = aTestCase.getActions();
    for (Action action : actions) {
      String platform = action.getPlatform();
      if (!Platforms.SHELL.name().equals(platform)) {
        Connection connection = getConnection(platform);
        ActionConnection aConnection = new ActionConnection(platform, connection, false);
        connectionList.add(aConnection);
      }
    }
    return connectionList;
  }

  public void returnAllConnections() {
    for (ActionConnection aConnection : connectionList) {
      Connection conn = aConnection.getConnection();
      try {
        if (conn != null && !conn.isClosed()) {
          conn.close();
          logger.info(aConnection.getPlatform() + " connection returned to pool");
        }
      } catch (SQLException e) {
        logger.error("Exception raised, connection for platform " + aConnection.getPlatform() + " cannot be closed", e);
      }
    }
  }

  @VisibleForTesting
  Connection getConnection(String platform) {
    Connection connection;
    HikariDataSource dataSource = platformDataSourceMap.get(platform);

    logger.info("Acquiring connection for {}", platform);
    try {
      connection = dataSource.getConnection();
      logger.info("Connection acquired from connection pool for {}", platform);
    } catch (SQLException e) {
      String errorMessage = "Problem acquiring connection for: " + platform;
      logger.error(errorMessage, e);
      throw new InvalidConnectionDetailsException(errorMessage);
    }
    return connection;
  }

  public void setConnectionLoader(ConnectionLoader connectionLoader) {
    this.connectionLoader = connectionLoader;
  }
}
