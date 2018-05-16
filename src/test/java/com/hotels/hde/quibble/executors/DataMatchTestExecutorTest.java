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
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.hotels.hde.quibble.ActionDataResult;
import com.hotels.hde.quibble.Platforms;
import com.hotels.hde.quibble.RowMatchResult;
import com.hotels.hde.quibble.RowPair;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.TestUtils;
import com.hotels.hde.quibble.connection.ActionConnection;

@RunWith(MockitoJUnitRunner.class)
public class DataMatchTestExecutorTest {

  private final String reportDifferences = "";
  private final String reportPath = "";

  @Mock
  private ResultSet rs1, rs2, mr;
  @Mock
  private ResultSetMetaData rsMD1, rsMD2;
  @Mock
  private Connection mc;
  @Mock
  private Statement ms;

  private DataMatchTestExecutor dataMatchTest = new DataMatchTestExecutor(reportDifferences, reportPath);

  private DataMatchTestExecutor dataMatchTestExecutor = new DataMatchTestExecutor(reportDifferences, reportPath) {
    @Override
    int getMaxAllowedMismatchLimit(String reportDifferences) {
      return 10;
    }

    @Override
    boolean getDataMatchVerdict(List<ActionDataResult> resultSets) {
      return true;
    }
  };

  @Test
  public void testGetDataMatchVerdictSuccess() throws SQLException {

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

    ActionDataResult actionDataResult1 = new ActionDataResult(Platforms.HIVE.getValue(), rs1);
    ActionDataResult actionDataResult2 = new ActionDataResult(Platforms.TERADATA.getValue(), rs2);

    List<ActionDataResult> actionDataResults = new ArrayList<>();
    actionDataResults.add(actionDataResult1);
    actionDataResults.add(actionDataResult2);

    boolean verdict = dataMatchTest.getDataMatchVerdict(actionDataResults);
    assertTrue(verdict);
  }

  @Test
  public void testGetDataMatchVerdictFailureColumnCount() throws SQLException {

    boolean thrown = false;

    when(rs1.getMetaData()).thenReturn(rsMD1);
    when(rs2.getMetaData()).thenReturn(rsMD2);

    when(rsMD1.getColumnCount()).thenReturn(3);
    when(rsMD2.getColumnCount()).thenReturn(2);

    when(rs1.next()).thenReturn(true, true, true, false);
    when(rs2.next()).thenReturn(true, true, true, false);

    when(rs1.getString(1)).thenReturn("123");
    when(rs1.getString(2)).thenReturn("1-1-2015");
    when(rs1.getString(3)).thenReturn("GBP");

    when(rs2.getString(1)).thenReturn("123");
    when(rs2.getString(2)).thenReturn("1-1-2015");

    ActionDataResult actionDataResult1 = new ActionDataResult(Platforms.HIVE.getValue(), rs1);
    ActionDataResult actionDataResult2 = new ActionDataResult(Platforms.TERADATA.getValue(), rs2);

    List<ActionDataResult> actionDataResults = new ArrayList<>();
    actionDataResults.add(actionDataResult1);
    actionDataResults.add(actionDataResult2);

    try {
      dataMatchTest.getDataMatchVerdict(actionDataResults);
    } catch (AssertionError e) {
      thrown = true;
    }
    assertTrue("ExpAT Framework should have failed assertion due to mismatch in number of columns", thrown);
  }

  @Test
  public void testGetDataMatchVerdictFailureRowCount() throws SQLException {

    when(rs1.getMetaData()).thenReturn(rsMD1);
    when(rs2.getMetaData()).thenReturn(rsMD2);

    when(rsMD1.getColumnCount()).thenReturn(3);
    when(rsMD2.getColumnCount()).thenReturn(3);

    when(rs1.next()).thenReturn(true, true, true, false);
    when(rs2.next()).thenReturn(true, true, false);

    when(rs1.getString(1)).thenReturn("123");
    when(rs1.getString(2)).thenReturn("1-1-2015");
    when(rs1.getString(3)).thenReturn("GBP");

    when(rs2.getString(1)).thenReturn("123");
    when(rs2.getString(2)).thenReturn("1-1-2015");
    when(rs2.getString(3)).thenReturn("GBP");

    ActionDataResult actionDataResult1 = new ActionDataResult(Platforms.HIVE.getValue(), rs1);
    ActionDataResult actionDataResult2 = new ActionDataResult(Platforms.TERADATA.getValue(), rs2);

    List<ActionDataResult> actionDataResults = new ArrayList<>();
    actionDataResults.add(actionDataResult1);
    actionDataResults.add(actionDataResult2);

    boolean verdict = dataMatchTest.getDataMatchVerdict(actionDataResults);
    assertFalse("ExpAT Framework should have failed assertion due to mismatch in number of columns", verdict);
  }

  @Test
  public void testGetDataMatchVerdictFailureDataMisMatch() throws SQLException {

    boolean thrown = false;

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
    when(rs2.getString(3)).thenReturn("USD");

    ActionDataResult actionDataResult1 = new ActionDataResult(Platforms.HIVE.getValue(), rs1);
    ActionDataResult actionDataResult2 = new ActionDataResult(Platforms.TERADATA.getValue(), rs2);

    List<ActionDataResult> actionDataResults = new ArrayList<>();
    actionDataResults.add(actionDataResult1);
    actionDataResults.add(actionDataResult2);

    try {
      dataMatchTest.getDataMatchVerdict(actionDataResults);
    } catch (AssertionError e) {
      thrown = true;
    }
    assertTrue("ExpAT Framework should have failed assertion due to data mismatch", thrown);
  }

  @Test
  public void testGetDataMatchVerdictFailureNoDataInBoth() throws SQLException {

    when(rs1.getMetaData()).thenReturn(rsMD1);
    when(rs2.getMetaData()).thenReturn(rsMD2);

    when(rsMD1.getColumnCount()).thenReturn(3);
    when(rsMD2.getColumnCount()).thenReturn(3);

    when(rs1.next()).thenReturn(false);
    when(rs2.next()).thenReturn(false);

    ActionDataResult actionDataResult1 = new ActionDataResult(Platforms.HIVE.getValue(), rs1);
    ActionDataResult actionDataResult2 = new ActionDataResult(Platforms.TERADATA.getValue(), rs2);

    List<ActionDataResult> actionDataResults = new ArrayList<>();
    actionDataResults.add(actionDataResult1);
    actionDataResults.add(actionDataResult2);

    boolean verdict = dataMatchTest.getDataMatchVerdict(actionDataResults);
    assertFalse(verdict);
  }

  @Test
  public void testGetDataMatchVerdictFailureNoDataInOne() throws SQLException {

    when(rs1.getMetaData()).thenReturn(rsMD1);
    when(rs2.getMetaData()).thenReturn(rsMD2);

    when(rsMD1.getColumnCount()).thenReturn(3);
    when(rsMD2.getColumnCount()).thenReturn(3);

    when(rs1.next()).thenReturn(false);
    when(rs2.next()).thenReturn(true, true, true, false);

    when(rs2.getString(1)).thenReturn("123");
    when(rs2.getString(2)).thenReturn("1-1-2015");
    when(rs2.getString(3)).thenReturn("USD");

    ActionDataResult actionDataResult1 = new ActionDataResult(Platforms.HIVE.getValue(), rs1);
    ActionDataResult actionDataResult2 = new ActionDataResult(Platforms.TERADATA.getValue(), rs2);

    List<ActionDataResult> actionDataResults = new ArrayList<>();
    actionDataResults.add(actionDataResult1);
    actionDataResults.add(actionDataResult2);

    boolean verdict = dataMatchTest.getDataMatchVerdict(actionDataResults);
    assertFalse(verdict);
  }

  @Test
  public void testCompareRowsMatching() {
    String[] firstRSRow = { "A", "b", "12", "--", "&&" };
    String[] secondRSRow = { "A", "b", "12", "--", "&&" };

    RowPair rowPair = new RowPair(firstRSRow, secondRSRow);

    BitSet expectedDifferences = new BitSet(firstRSRow.length);

    RowMatchResult result = dataMatchTest.compareRows(rowPair);

    BitSet actualDifferences = result.getDifferences();

    assertTrue(result.rowMatched());
    assertThat(actualDifferences, is(expectedDifferences));
  }

  @Test
  public void testMaxAllowedMismatchLimit() {

    Integer result = dataMatchTest.getMaxAllowedMismatchLimit("5000010");

    assertThat(result, is(500000));
  }

  @Test
  public void testMismatchMinLimit() {

    Integer result = dataMatchTest.getMaxAllowedMismatchLimit("NONE");

    assertThat(result, is(0));
  }

  @Test
  public void testMismatchMaxLimit() {

    Integer result = dataMatchTest.getMaxAllowedMismatchLimit("ALL");

    assertThat(result, is(500000));
  }

  @Test
  public void testCompareRowsMisMatching() {

    String[] firstRSRow = { "A", "b", "12", "-!", "&&" };
    String[] secondRSRow = { "A", "b", "12", "--", "&&" };

    RowPair rowPair = new RowPair(firstRSRow, secondRSRow);

    BitSet expectedDifferences = new BitSet(firstRSRow.length);

    RowMatchResult result = dataMatchTest.compareRows(rowPair);

    BitSet actualDifferences = result.getDifferences();

    assertFalse(result.rowMatched());
    assertThat(actualDifferences, is(not(expectedDifferences)));
  }

  @Test
  public void testGetRowPair() throws SQLException {

    int numberOfColumns = 3;

    when(rs1.getString(1)).thenReturn("123");
    when(rs1.getString(2)).thenReturn("1-1-2015");
    when(rs1.getString(3)).thenReturn("GBP");

    when(rs2.getString(1)).thenReturn("231");
    when(rs2.getString(2)).thenReturn("2-1-2015");
    when(rs2.getString(3)).thenReturn("USD");

    RowPair rowPair = dataMatchTest.getRowPair(rs1, rs2, numberOfColumns);

    String[] firstRSRow = rowPair.getFirstRSRow();
    String[] secondRSRow = rowPair.getSecondRSRow();

    assertThat("123", is(firstRSRow[0]));
    assertThat("1-1-2015", is(firstRSRow[1]));
    assertThat("GBP", is(firstRSRow[2]));

    assertThat("231", is(secondRSRow[0]));
    assertThat("2-1-2015", is(secondRSRow[1]));
    assertThat("USD", is(secondRSRow[2]));
  }

  @Test
  public void testexecuteflow() throws SQLException {

    TestCase adataMatchTest = TestUtils.createASingleNumberMatchTest("Test 101",
        com.hotels.hde.quibble.Platforms.HIVE.getValue(),
        com.hotels.hde.quibble.Platforms.TERADATA.getValue());

    List<ActionConnection> connectionList = new ArrayList<>();
    ActionConnection actionConnection = new ActionConnection(com.hotels.hde.quibble.Platforms.TERADATA.getValue(), mc,
        true);
    connectionList.add(actionConnection);

    Mockito.when(mc.createStatement()).thenReturn(ms);
    Mockito.when(ms.executeQuery("select 100")).thenReturn(mr);

    dataMatchTestExecutor.execute(adataMatchTest, connectionList);
  }

  @Test(expected = RuntimeException.class)
  public void testcompareResultSetsGenerateReport() throws SQLException, IOException {

    when(rs1.getMetaData()).thenReturn(rsMD1);
    when(rs2.getMetaData()).thenReturn(rsMD2);

    when(rsMD1.getColumnCount()).thenReturn(1);
    when(rsMD2.getColumnCount()).thenReturn(1);

    when(rs1.first()).thenReturn(true);
    when(rs2.first()).thenReturn(true);

    when(rs1.next()).thenReturn(true);
    when(rs2.next()).thenReturn(true);
    when(rs1.getString(1)).thenReturn("123");
    when(rs1.getString(2)).thenReturn("1-1-2015");
    when(rs1.getString(3)).thenReturn("GBP");

    when(rs2.getString(1)).thenReturn("121");
    when(rs2.getString(2)).thenReturn("2-1-2015");
    when(rs2.getString(3)).thenReturn("USD");

    ActionDataResult actionDataResult1 = new ActionDataResult(Platforms.HIVE.getValue(), rs1);
    ActionDataResult actionDataResult2 = new ActionDataResult(Platforms.TERADATA.getValue(), rs2);

    dataMatchTest.setAllowedMismatches(1);

    dataMatchTest.compareResultSetsGenerateReport(rs1, rs2, 3);
  }
}
