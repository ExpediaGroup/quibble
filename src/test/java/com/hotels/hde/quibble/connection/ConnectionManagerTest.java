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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.hotels.hde.quibble.Platforms;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.TestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionManagerTest {

  @Mock
  private Connection mockconnection;

  private final ConnectionLoader connectionLoader = new ConnectionLoader() {
    @Override
    public Set<ConnectionDetails> getConnectionDetailsList(List<TestCase[]> tests) throws IOException {
      ConnectionDetails connectionDetails = new ConnectionDetails();

      connectionDetails.setDriver("org.apache.hive.jdbc.HiveDriver");
      connectionDetails.setUrl("jdbc:hive: //localhost:10000/default");
      connectionDetails.setUsername("user");
      connectionDetails.setPassword("password");
      connectionDetails.setPlatform("hive");
      Set<ConnectionDetails> connectionDetailsSet = new HashSet<>();
      connectionDetailsSet.add(connectionDetails);
      return connectionDetailsSet;
    }
  };

  private final ConnectionManager connectionManager = new ConnectionManager(connectionLoader) {
    @Override
    Connection getConnection(String platform) {
      return mockconnection;
    }
  };

  @Test
  public void testgetRequiredConnections() throws SQLException {

    List<ActionConnection> connections = new ArrayList<>();
    TestCase aCountMatchTest = TestUtils.createASingleNumberMatchTest("Test 101",
        Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());

    connections = connectionManager.getRequiredConnections(aCountMatchTest);
    assertThat(connections.size(), is(2));
  }
}
