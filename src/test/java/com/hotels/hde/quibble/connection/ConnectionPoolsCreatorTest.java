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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import com.hotels.hde.quibble.connection.ConnectionPoolsCreator.HikariDataSourceFactory;
import com.hotels.hde.quibble.exceptions.InvalidConnectionDetailsException;
import com.hotels.hde.quibble.exceptions.MissingConnectionDetailsException;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionPoolsCreatorTest {

  @Mock
  private HikariDataSourceFactory dataSourceFactory;

  @Mock
  private HikariDataSource dataSource;

  private ConnectionPoolsCreator connectionPoolsCreator;

  private Set<ConnectionDetails> connectionDetailsSet = new HashSet<>();

  private ConnectionDetails connectionDetails = new ConnectionDetails();

  @Before
  public void setUp() {
    connectionPoolsCreator = new ConnectionPoolsCreator(dataSourceFactory);
    connectionDetails.setDriver("org.apache.hive.jdbc.HiveDriver");
    connectionDetails.setUrl("jdbc:hive: //localhost:10000/default");
    connectionDetails.setUsername("user");
    connectionDetails.setPassword("password");
    connectionDetails.setPlatform("hive");
    connectionDetailsSet.add(connectionDetails);
  }

  @Test
  public void classNotFound() throws SQLException, ClassNotFoundException {
    when(dataSourceFactory.newInstance(any(HikariConfig.class))).thenThrow(ClassNotFoundException.class);
    connectionPoolsCreator.getHikariCPConnectionPoolsDataSources(connectionDetailsSet);
  }

  @Test
  public void invalidConnectionDetails() throws SQLException, ClassNotFoundException {
    when(dataSourceFactory.newInstance(any(HikariConfig.class))).thenThrow(InvalidConnectionDetailsException.class);
    connectionPoolsCreator.getHikariCPConnectionPoolsDataSources(connectionDetailsSet);
  }

  @Test
  public void missingConnectionDetails() throws SQLException, ClassNotFoundException {
    when(dataSourceFactory.newInstance(any(HikariConfig.class))).thenThrow(MissingConnectionDetailsException.class);
    connectionPoolsCreator.getHikariCPConnectionPoolsDataSources(connectionDetailsSet);
  }

  @Test
  public void typical() {
    when(dataSourceFactory.newInstance(any(HikariConfig.class))).thenReturn(dataSource);
    Map<String, HikariDataSource> dataSources = connectionPoolsCreator
        .getHikariCPConnectionPoolsDataSources(connectionDetailsSet);
    assertThat(dataSources.size(), is(1));
    assertThat(dataSources.get(connectionDetails.getPlatform()), is(dataSource));
  }
}
