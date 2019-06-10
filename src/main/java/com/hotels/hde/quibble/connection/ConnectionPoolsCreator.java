/**
 * Copyright (C) 2015-2019 Expedia, Inc.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import com.hotels.hde.quibble.exceptions.InvalidConnectionDetailsException;
import com.hotels.hde.quibble.exceptions.MissingConnectionDetailsException;

class ConnectionPoolsCreator {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  static class HikariDataSourceFactory {
    HikariDataSource newInstance(HikariConfig config) {
      return new HikariDataSource(config);
    }
  }

  private HikariDataSourceFactory dataSourceFactory;

  ConnectionPoolsCreator() {
    this(new HikariDataSourceFactory());
  }

  ConnectionPoolsCreator(HikariDataSourceFactory dataSourceFactory) {
    this.dataSourceFactory = dataSourceFactory;
  }

  Map<String, HikariDataSource> getHikariCPConnectionPoolsDataSources(
      Set<ConnectionDetails> requiredConnections) {
    Map<String, HikariDataSource> platformDataSourceMap = new HashMap<>();

    for (ConnectionDetails aConnectionDetail : requiredConnections) {
      HikariConfig cpConfig = new HikariConfig();
      try {
        Class.forName(aConnectionDetail.getDriver());
        cpConfig.setJdbcUrl(aConnectionDetail.getUrl());
        cpConfig.setUsername(aConnectionDetail.getUsername());
        cpConfig.setPassword(aConnectionDetail.getPassword());
        cpConfig.setMaximumPoolSize(10);

        logger.info("Going to create connection pooling data source for: {}", aConnectionDetail.getPlatform());

        HikariDataSource connectionPool = dataSourceFactory.newInstance(cpConfig);

        logger.info("DataSource created for: {}", aConnectionDetail.getPlatform());

        platformDataSourceMap.put(aConnectionDetail.getPlatform(), connectionPool);
      } catch (ClassNotFoundException e) {
        logger.error("Driver class {} could not be loaded", aConnectionDetail.getDriver(), e);
      } catch (InvalidConnectionDetailsException | MissingConnectionDetailsException e) {
        logger.error("Exception while creating connection pooling data source", e);
      }
    }
    return platformDataSourceMap;
  }
}
