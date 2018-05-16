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
package com.hotels.hde.quibble.executors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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

import com.hotels.hde.quibble.ActionResult;
import com.hotels.hde.quibble.Platforms;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.TestUtils;
import com.hotels.hde.quibble.connection.ActionConnection;

@RunWith(MockitoJUnitRunner.class)
public class SingleNumberMatchTestExecutorTest {

  @Mock
  private ResultSet rs1;
  @Mock
  private Connection mc;
  @Mock
  private Statement ms;

  private final SingleNumberMatchTestExecutor aTest = new SingleNumberMatchTestExecutor() {
    @Override
    public void execute(TestCase aTestCase, List<ActionConnection> connectionList) {
      assertTrue(true);
    }

    @Override
    public ActionResult getNumberFromShellCommand(String platform, String query) {
      return new ActionResult(Platforms.SHELL.getValue(), 100L);
    }
  };

  private final SingleNumberMatchTestExecutor bTest = new SingleNumberMatchTestExecutor();

  @Test
  public void testGetNumberMatchVerdictSuccess() {

    List<ActionResult> results = new ArrayList<>();

    ActionResult testResult1 = new ActionResult("hive", 0);
    ActionResult testResult2 = new ActionResult("db2", 0);

    results.add(testResult1);
    results.add(testResult2);
    boolean verdict = aTest.getNumberMatchVerdict(results);
    assertTrue(verdict);
  }

  @Test
  public void testGetNumberMatchVerdictFailure() {

    List<ActionResult> results = new ArrayList<>();

    ActionResult testResult1 = new ActionResult("hive", 12345678);
    ActionResult testResult2 = new ActionResult("db2", 123456789);

    results.add(testResult1);
    results.add(testResult2);
    boolean verdict = aTest.getNumberMatchVerdict(results);
    assertFalse(verdict);
  }

  @Test
  public void testgetNumberFromShellCommandSuccess() {

    ActionResult expected = new ActionResult("shell", 100);
    ActionResult verdict = bTest.getNumberFromShellCommand(Platforms.SHELL.getValue(), "echo 100");
    assertThat(verdict.getNumericResult(), is(expected.getNumericResult()));
  }

  @Test
  public void testgetNumericValueWithPlatformAsString() {

    List<ActionResult> results = new ArrayList<>();

    ActionResult testResult1 = new ActionResult("hive", 12345678);
    results.add(testResult1);

    String verdict = aTest.getNumericValueWithPlatformAsString(results);
    String expected = String.format(System.getProperty("line.separator"),
        " Platform: " + testResult1.getPlatform().trim(),
        " returned value: " + testResult1.getNumericResult());
    assertThat(verdict, is(expected));
  }

  @Test
  public void testgetNumberFromQuerySuccess() throws SQLException {

    String query = "select count(*) from table";
    ResultSet rs1 = mock(ResultSet.class);
    Connection mc = mock(Connection.class);
    Statement ms = mock(Statement.class);
    ResultSetMetaData rsMD1 = mock(ResultSetMetaData.class);

    when(rs1.getMetaData()).thenReturn(rsMD1);

    when(rsMD1.getColumnCount()).thenReturn(1);

    Mockito.when(mc.createStatement()).thenReturn(ms);
    Mockito.when(ms.executeQuery("select count(*) from table")).thenReturn(rs1);
    Mockito.when(rs1.next()).thenReturn(true, true, false);
    when(rs1.getString(1)).thenReturn("123");
    when(rs1.getString(2)).thenReturn("1-1-2015");
    when(rs1.getString(3)).thenReturn("GBP");
    when(rs1.getLong(1)).thenReturn(100L);

    ActionResult verdict = aTest.getNumberFromQuery(Platforms.TERADATA.getValue(), query, mc);

    assertThat(verdict.getNumericResult(), is(100L));
  }

  @Test(expected = AssertionError.class)
  public void testgetNumberFromQueryFailure() throws SQLException {

    String query = "select count(*) from table";

    ResultSetMetaData rsMD1 = mock(ResultSetMetaData.class);

    when(rs1.getMetaData()).thenReturn(rsMD1);

    when(rsMD1.getColumnCount()).thenReturn(2);

    Mockito.when(mc.createStatement()).thenReturn(ms);
    Mockito.when(ms.executeQuery(query)).thenReturn(rs1);
    Mockito.when(rs1.next()).thenReturn(true, true, false);

    aTest.getNumberFromQuery(Platforms.TERADATA.getValue(), query, mc);
  }

  @Test
  public void testExecuteCommands() throws SQLException {

    TestCase aCountMatchTest = TestUtils.createASingleNumberMatchTest("Test 101", Platforms.SHELL.getValue(),
        Platforms.SHELL.getValue());

    List<ActionConnection> connectionList = new ArrayList<>();
    ActionConnection actionConnection = new ActionConnection("shell", mc, true);
    connectionList.add(actionConnection);

    Mockito.when(mc.createStatement()).thenReturn(ms);
    Mockito.when(ms.executeQuery("echo 100")).thenReturn(rs1);

    List<ActionResult> result = aTest.executeCommands(aCountMatchTest, connectionList);
    assertThat(result.size(), is(2));
  }

  @Test
  public void testExecuteflow() throws SQLException {

    TestCase aCountMatchTest = TestUtils.createASingleNumberMatchTest("Test 101", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());

    List<ActionConnection> connectionList = new ArrayList<>();
    ActionConnection actionConnection = new ActionConnection(Platforms.TERADATA.getValue(), mc, true);
    connectionList.add(actionConnection);

    Mockito.when(mc.createStatement()).thenReturn(ms);
    Mockito.when(ms.executeQuery("select 100")).thenReturn(rs1);

    bTest.execute(aCountMatchTest, connectionList);
  }
}
