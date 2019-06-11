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
package com.hotels.hde.quibble.executors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.testng.Assert;

import com.hotels.hde.quibble.ActionDataResult;
import com.hotels.hde.quibble.Platforms;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.TestUtils;
import com.hotels.hde.quibble.connection.ActionConnection;

@RunWith(MockitoJUnitRunner.class)
public class DatasetTestTypeExecutorTest {

  @Mock
  private Connection mockConnection;
  @Mock
  private Statement mockstatement;
  @Mock
  private ResultSet rs1, rs2;
  @Mock
  private ResultSetMetaData rsMD1, rsMD2;

  private DatasetTestTypeExecutor underTest = new DatasetTestTypeExecutor() {

    @Override
    public void execute(TestCase aTestCase, List<ActionConnection> connectionList) {
      Assert.assertTrue(true, "test message");
    }

    @Override
    ActionConnection findUnusedConnection(String platform, List<ActionConnection> connectionList) {
      return connectionList.get(0);
    }

    @Override
    ActionDataResult getDataFromQuery(String platform, String query, Connection connection) {
      return new ActionDataResult(Platforms.HIVE.getValue(), mock(ResultSet.class));
    }
  };

  private DatasetTestTypeExecutor underTest2 = new DatasetTestTypeExecutor() {

    @Override
    public void execute(TestCase aTestCase, List<ActionConnection> connectionList) {
      Assert.assertTrue(true, "test message");
    }
  };

  @Test
  public void testexecuteDataQueries() throws SQLException {

    when(rs1.getMetaData()).thenReturn(rsMD1);
    when(rs2.getMetaData()).thenReturn(rsMD2);

    when(rsMD1.getColumnCount()).thenReturn(3);
    when(rsMD2.getColumnCount()).thenReturn(3);

    when(rs1.next()).thenReturn(true, true, true, false);
    when(rs2.next()).thenReturn(true, true, true, false);

    when(rs1.getString(1)).thenReturn("123");
    when(rs1.getString(2)).thenReturn("1-1-2015");
    when(rs1.getString(3)).thenReturn("GBP");

    when(rs2.getString(1)).thenReturn("123");
    when(rs2.getString(2)).thenReturn("1-1-2015");
    when(rs2.getString(3)).thenReturn("GBP");

    TestCase aCountMatchTest = TestUtils.createASingleNumberMatchTest("Test 101",
        Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());

    List<ActionConnection> connectionList = new ArrayList<>();

    ActionConnection actionConnection = new ActionConnection(
        Platforms.TERADATA.getValue(),
        mockConnection, true);

    connectionList.add(actionConnection);

    List<ActionDataResult> result = underTest.executeDataQueries(aCountMatchTest, connectionList);

    assertThat(result.size(), is(2));
  }

  @Test
  public void testgetDataFromQuery() throws SQLException {

    when(rs1.getMetaData()).thenReturn(rsMD1);
    when(rsMD1.getColumnCount()).thenReturn(3);

    when(rs1.getString(1)).thenReturn("123");
    when(rs1.getString(2)).thenReturn("1-1-2015");
    when(rs1.getString(3)).thenReturn("GBP");

    Mockito.when(mockConnection.createStatement()).thenReturn(mockstatement);
    Mockito.when(mockstatement.executeQuery("select 100")).thenReturn(rs1);
    String query = "select 100";

    ActionDataResult result = underTest2.getDataFromQuery(Platforms.HIVE.getValue(), query, mockConnection);

    assertThat(result.getResultSet(), is(rs1));
  }
}
