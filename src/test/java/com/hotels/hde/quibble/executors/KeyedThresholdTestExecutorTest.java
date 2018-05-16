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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.hotels.hde.quibble.ActionDataResult;
import com.hotels.hde.quibble.Platforms;
import com.hotels.hde.quibble.Threshold;

@RunWith(MockitoJUnitRunner.class)
public class KeyedThresholdTestExecutorTest {

  @Mock
  private ResultSet rs1, rs2;
  @Mock
  private ResultSetMetaData rsMD1, rsMD2;

  private final KeyedThresholdTestExecutor keyedThresholdTest = new KeyedThresholdTestExecutor();

  @Test
  public void testGetKeyedThresholdVerdictSuccess() throws SQLException {

    when(rs1.getMetaData()).thenReturn(rsMD1);
    when(rs2.getMetaData()).thenReturn(rsMD2);

    when(rsMD1.getColumnCount()).thenReturn(4);
    when(rsMD2.getColumnCount()).thenReturn(4);

    when(rs1.next()).thenReturn(true, true, true, false);
    when(rs2.next()).thenReturn(true, true, true, false);

    when(rs1.getString(1)).thenReturn("Mon");
    when(rs1.getString(2)).thenReturn("2016-01-04");
    when(rs1.getString(3)).thenReturn("100");
    when(rs1.getString(4)).thenReturn("100");

    when(rs2.getString(1)).thenReturn("Mon");
    when(rs2.getString(2)).thenReturn("2016-02-04");
    when(rs2.getString(3)).thenReturn("100");
    when(rs2.getString(4)).thenReturn("100");

    ActionDataResult actionDataResult1 = new ActionDataResult(Platforms.HIVE.getValue(), rs1);
    ActionDataResult actionDataResult2 = new ActionDataResult(Platforms.TERADATA.getValue(), rs2);

    List<ActionDataResult> actionDataResults = new ArrayList<>();
    actionDataResults.add(actionDataResult1);
    actionDataResults.add(actionDataResult2);

    List<Threshold> thresholds = new ArrayList<Threshold>();
    Threshold threshold1 = new Threshold();
    threshold1.setColumnIndex(2);
    threshold1.setValue(.2);
    thresholds.add(threshold1);

    Threshold threshold2 = new Threshold();
    threshold2.setColumnIndex(2);
    threshold2.setValue(.2);
    thresholds.add(threshold2);

    boolean verdict = keyedThresholdTest.getKeyedThresholdVerdict(actionDataResults, thresholds, 0, 1);

    assertTrue("Difference is within the threshold defined", verdict);
  }

  @Test
  public void testGeneratePassFailMessageKeyedThreshold() throws Exception {
    String returntext = keyedThresholdTest.generatePassFailMessageKeyedThreshold("100", "101", "102", "103", "104",
        "105", "106", "107", 108.01);
    assertNotSame("Generate Pass Fail Message isn't in the correct format.",
        "100, 101, 102, 103, 104, 105, 106, 107, 108.1", returntext);
  }

  @Test
  public void testCalculateDiffPercentageNoRound() throws Exception {
    double retvalue = keyedThresholdTest.calculateDiffPercentage(100, 110, false, 0);
    assertThat((double) Math.abs(110 - 100) / (double) 110 * 100, is(retvalue));
  }

  @Test
  public void testCalculateDiffPercentageWithRound() throws Exception {
    double retvalue = keyedThresholdTest.calculateDiffPercentage(100, 110, true, 2);
    assertThat(9.09, is(retvalue));
  }

  @Test
  public void testCalculateDiffPercentageWithRoundLargePrecision() throws Exception {
    double retvalue = keyedThresholdTest.calculateDiffPercentage(100, 107, true, 1000);
    assertThat(6.5420560747663545, is(retvalue));
  }

  @Test
  public void testConvertResultSetToList() throws Exception {

    when(rs1.getMetaData()).thenReturn(rsMD1);
    when(rsMD1.getColumnCount()).thenReturn(4);

    when(rs1.next()).thenReturn(true, true, true, false);

    when(rs1.getString(1)).thenReturn("Tue");
    when(rs1.getString(2)).thenReturn("2016-02-04");
    when(rs1.getString(3)).thenReturn("150");
    when(rs1.getString(4)).thenReturn("150");

    List<List<String>> result = keyedThresholdTest.convertResultSetToList(rs1);
    assertThat(result.size(), is(3));
  }

  @Test
  public void testConvertResultSetToListEmptySet() throws Exception {
    List<List<String>> result = keyedThresholdTest.convertResultSetToList(rs1);
    assertThat(result.size(), is(0));
  }

  @Test
  public void testCalculateDiffPercentageLargeNumFirst() throws Exception {
    double retvalue = keyedThresholdTest.calculateDiffPercentage(110, 100, false, 0);
    assertThat((double) Math.abs(110 - 100) / (double) 110 * 100, is(retvalue));
  }

  @Test
  public void testCalculateDiffPercentageNoRoundSmallNumbers() throws Exception {
    double retvalue = keyedThresholdTest.calculateDiffPercentage((float) 0.005, (float) 0.004, false, 0);
    assertThat(19.999994412064428, is(retvalue));
  }

  @Test
  public void testCalculateDiffPercentageRoundSmallNumbers() throws Exception {
    double retvalue = keyedThresholdTest.calculateDiffPercentage((float) 0.005, (float) 0.004, true, 2);
    assertThat(20.00, is(retvalue));
  }

  @Test
  public void testGetKeyedThresholdVerdictFailure() throws SQLException {
    boolean thrown = false;

    when(rs1.getMetaData()).thenReturn(rsMD1);
    when(rs2.getMetaData()).thenReturn(rsMD2);

    when(rsMD1.getColumnCount()).thenReturn(4);
    when(rsMD2.getColumnCount()).thenReturn(4);

    when(rs1.next()).thenReturn(true, true, true, false);
    when(rs2.next()).thenReturn(true, true, true, false);

    when(rs1.getString(1)).thenReturn("Mon");
    when(rs1.getString(2)).thenReturn("2016-01-04");
    when(rs1.getString(3)).thenReturn("500");
    when(rs1.getString(4)).thenReturn("100");

    when(rs2.getString(1)).thenReturn("Mon");
    when(rs2.getString(2)).thenReturn("2016-02-04");
    when(rs2.getString(3)).thenReturn("100");
    when(rs2.getString(4)).thenReturn("100");

    ActionDataResult actionDataResult1 = new ActionDataResult(Platforms.HIVE.getValue(), rs1);
    ActionDataResult actionDataResult2 = new ActionDataResult(Platforms.TERADATA.getValue(), rs2);

    List<ActionDataResult> actionDataResults = new ArrayList<>();
    actionDataResults.add(actionDataResult1);
    actionDataResults.add(actionDataResult2);

    List<Threshold> thresholds = new ArrayList<Threshold>();
    Threshold threshold1 = new Threshold();
    threshold1.setColumnIndex(2);
    threshold1.setValue(.2);
    thresholds.add(threshold1);

    Threshold threshold2 = new Threshold();
    threshold2.setColumnIndex(3);
    threshold2.setValue(.2);
    thresholds.add(threshold2);

    try {
      keyedThresholdTest.getKeyedThresholdVerdict(actionDataResults, thresholds, 0, 1);
    } catch (AssertionError e) {
      thrown = true;
    }
    assertTrue("ExpAT Framework should have failed assertion due to keyed threshold failure", thrown);
  }

  @Test
  public void testGetKeyedThresholdVerdictColumnMismatchFailure() throws SQLException {
    boolean thrown = false;

    when(rs1.getMetaData()).thenReturn(rsMD1);
    when(rs2.getMetaData()).thenReturn(rsMD2);

    when(rsMD1.getColumnCount()).thenReturn(3);
    when(rsMD2.getColumnCount()).thenReturn(4);

    when(rs1.next()).thenReturn(true, true, true, false);
    when(rs2.next()).thenReturn(true, true, true, false);

    when(rs1.getString(1)).thenReturn("Mon");
    when(rs1.getString(2)).thenReturn("2016-01-04");
    when(rs1.getString(3)).thenReturn("500");

    when(rs2.getString(1)).thenReturn("Mon");
    when(rs2.getString(2)).thenReturn("2016-02-04");
    when(rs2.getString(3)).thenReturn("100");
    when(rs2.getString(4)).thenReturn("100");

    ActionDataResult actionDataResult1 = new ActionDataResult(Platforms.HIVE.getValue(), rs1);
    ActionDataResult actionDataResult2 = new ActionDataResult(Platforms.TERADATA.getValue(), rs2);

    List<ActionDataResult> actionDataResults = new ArrayList<>();
    actionDataResults.add(actionDataResult1);
    actionDataResults.add(actionDataResult2);

    List<Threshold> thresholds = new ArrayList<Threshold>();
    Threshold threshold1 = new Threshold();
    threshold1.setColumnIndex(2);
    threshold1.setValue(.2);
    thresholds.add(threshold1);

    Threshold threshold2 = new Threshold();
    threshold2.setColumnIndex(3);
    threshold2.setValue(.2);
    thresholds.add(threshold2);

    try {
      keyedThresholdTest.getKeyedThresholdVerdict(actionDataResults, thresholds, 0, 1);
    } catch (AssertionError e) {
      thrown = true;
    }
    assertTrue("ExpAT Framework should have failed assertion due to keyed threshold failure", thrown);
  }

  @Test
  public void testGetKeyedThresholdVerdictNoDataInFirstSet() throws SQLException {
    boolean thrown = false;

    when(rs1.getMetaData()).thenReturn(rsMD1);
    when(rs2.getMetaData()).thenReturn(rsMD2);

    when(rsMD1.getColumnCount()).thenReturn(4);
    when(rsMD2.getColumnCount()).thenReturn(4);

    when(rs1.next()).thenReturn(false);
    when(rs2.next()).thenReturn(true, true, true, false);

    when(rs2.getString(1)).thenReturn("Mon");
    when(rs2.getString(2)).thenReturn("2016-02-04");
    when(rs2.getString(3)).thenReturn("100");
    when(rs2.getString(4)).thenReturn("100");

    ActionDataResult actionDataResult1 = new ActionDataResult(Platforms.HIVE.getValue(), rs1);
    ActionDataResult actionDataResult2 = new ActionDataResult(Platforms.TERADATA.getValue(), rs2);

    List<ActionDataResult> actionDataResults = new ArrayList<>();
    actionDataResults.add(actionDataResult1);
    actionDataResults.add(actionDataResult2);

    List<Threshold> thresholds = new ArrayList<Threshold>();
    Threshold threshold1 = new Threshold();
    threshold1.setColumnIndex(2);
    threshold1.setValue(.2);
    thresholds.add(threshold1);

    Threshold threshold2 = new Threshold();
    threshold2.setColumnIndex(3);
    threshold2.setValue(.2);
    thresholds.add(threshold2);

    try {
      keyedThresholdTest.getKeyedThresholdVerdict(actionDataResults, thresholds, 0, 1);
    } catch (AssertionError e) {
      thrown = true;
    }
    assertTrue("ExpAT Framework should have failed assertion due to empty result set", thrown);
  }

  @Test
  public void testGetKeyedThresholdVerdictMissingSecondResultSet() throws SQLException {
    boolean thrown = false;

    ResultSetMetaData rsMD1 = mock(ResultSetMetaData.class);

    when(rs1.getMetaData()).thenReturn(rsMD1);

    when(rsMD1.getColumnCount()).thenReturn(4);

    when(rs1.next()).thenReturn(true, true, true, false);
    when(rs1.getString(1)).thenReturn("Mon");
    when(rs1.getString(2)).thenReturn("2016-02-04");
    when(rs1.getString(3)).thenReturn("100");
    when(rs1.getString(4)).thenReturn("100");

    ActionDataResult actionDataResult1 = new ActionDataResult(Platforms.HIVE.getValue(), rs1);

    List<ActionDataResult> actionDataResults = new ArrayList<>();
    actionDataResults.add(actionDataResult1);

    List<Threshold> thresholds = new ArrayList<Threshold>();
    Threshold threshold1 = new Threshold();
    threshold1.setColumnIndex(2);
    threshold1.setValue(.2);
    thresholds.add(threshold1);

    Threshold threshold2 = new Threshold();
    threshold2.setColumnIndex(3);
    threshold2.setValue(.2);
    thresholds.add(threshold2);

    try {
      keyedThresholdTest.getKeyedThresholdVerdict(actionDataResults, thresholds, 0, 1);
    } catch (AssertionError e) {
      thrown = true;
    }
    assertTrue("ExpAT Framework should have failed assertion due to only one result set", thrown);
  }

  @Test
  public void testGetKeyedThresholdVerdictNoDataInSecondSet() throws SQLException {
    boolean thrown = false;

    when(rs1.getMetaData()).thenReturn(rsMD1);
    when(rs2.getMetaData()).thenReturn(rsMD2);

    when(rsMD1.getColumnCount()).thenReturn(4);
    when(rsMD2.getColumnCount()).thenReturn(4);

    when(rs1.next()).thenReturn(true, true, true, false);
    when(rs2.next()).thenReturn(false);

    when(rs1.getString(1)).thenReturn("Mon");
    when(rs1.getString(2)).thenReturn("2016-02-04");
    when(rs1.getString(3)).thenReturn("100");
    when(rs1.getString(4)).thenReturn("100");

    ActionDataResult actionDataResult1 = new ActionDataResult(Platforms.HIVE.getValue(), rs1);
    ActionDataResult actionDataResult2 = new ActionDataResult(Platforms.TERADATA.getValue(), rs2);

    List<ActionDataResult> actionDataResults = new ArrayList<>();
    actionDataResults.add(actionDataResult1);
    actionDataResults.add(actionDataResult2);

    List<Threshold> thresholds = new ArrayList<Threshold>();
    Threshold threshold1 = new Threshold();
    threshold1.setColumnIndex(2);
    threshold1.setValue(.2);
    thresholds.add(threshold1);

    Threshold threshold2 = new Threshold();
    threshold2.setColumnIndex(3);
    threshold2.setValue(.2);
    thresholds.add(threshold2);

    try {
      keyedThresholdTest.getKeyedThresholdVerdict(actionDataResults, thresholds, 0, 1);
    } catch (AssertionError e) {
      thrown = true;
    }
    assertTrue("ExpAT Framework should have failed assertion due to empty result set", thrown);
  }

  @Test
  public void compareResultSetsKeyedThresholdSuccess() throws Exception {
    boolean thrown = false;

    when(rs1.getMetaData()).thenReturn(rsMD1);
    when(rs2.getMetaData()).thenReturn(rsMD2);

    when(rsMD1.getColumnCount()).thenReturn(4);
    when(rsMD2.getColumnCount()).thenReturn(4);

    when(rs1.next()).thenReturn(true, true, true, false);
    when(rs2.next()).thenReturn(true, true, true, false);

    when(rs1.getString(1)).thenReturn("Mon");
    when(rs1.getString(2)).thenReturn("2016-02-04");
    when(rs1.getString(3)).thenReturn("100");
    when(rs1.getString(4)).thenReturn("100");

    when(rs1.getString(1)).thenReturn("Mon");
    when(rs1.getString(2)).thenReturn("2016-02-04");
    when(rs1.getString(3)).thenReturn("400");
    when(rs1.getString(4)).thenReturn("100");

    ActionDataResult actionDataResult1 = new ActionDataResult(Platforms.HIVE.getValue(), rs1);
    ActionDataResult actionDataResult2 = new ActionDataResult(Platforms.HIVE.getValue(), rs2);

    List<ActionDataResult> actionDataResults = new ArrayList<>();
    actionDataResults.add(actionDataResult1);
    actionDataResults.add(actionDataResult2);

    List<Threshold> thresholds = new ArrayList<Threshold>();
    Threshold threshold1 = new Threshold();
    threshold1.setColumnIndex(2);
    threshold1.setValue(.2);
    thresholds.add(threshold1);

    Threshold threshold2 = new Threshold();
    threshold2.setColumnIndex(3);
    threshold2.setValue(.2);
    thresholds.add(threshold2);

    try {
      keyedThresholdTest.getKeyedThresholdVerdict(actionDataResults, thresholds, 0, 1);
    } catch (AssertionError e) {
      thrown = true;
    }
    assertTrue("ExpAT Framework should have failed assertion due mismatched result set", thrown);
  }
}
