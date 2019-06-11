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

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;

import com.hotels.hde.quibble.ActionDataResult;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.Threshold;
import com.hotels.hde.quibble.connection.ActionConnection;

public class KeyedThresholdTestExecutor extends DatasetTestTypeExecutor {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void execute(TestCase aTestCase, List<ActionConnection> connectionList) {
    List<ActionDataResult> actionDataResults = executeDataQueries(aTestCase, connectionList);

    List<Threshold> thresholds = aTestCase.getThresholds();
    int thresholdKeyIndex = aTestCase.getThresholdKeyIndex();
    int thresholdReportIndex = aTestCase.getThresholdReportIndex();

    boolean thresholdMatched = getKeyedThresholdVerdict(actionDataResults, thresholds, thresholdKeyIndex, thresholdReportIndex);
    assertTrue(thresholdMatched, "One or more threshold requirements were not met!");
  }

  boolean getKeyedThresholdVerdict(
      List<ActionDataResult> resultSets,
      List<Threshold> thresholds,
      int thresholdKeyIndex,
      int thresholdReportIndex) {
    int size = resultSets.size();
    boolean thresholdVerdict = false;

    if (size != 2) {
      fail("Two result sets are required. Only " + size + " result sets gathered.");
    }

    ActionDataResult firstActionDataResult = resultSets.get(0);
    ResultSet firstResultSet = firstActionDataResult.getResultSet();

    ActionDataResult secondActionDataResult = resultSets.get(1);
    ResultSet secondResultSet = secondActionDataResult.getResultSet();

    try {
      ResultSetMetaData firstResultSetMetaData = firstResultSet.getMetaData();
      ResultSetMetaData secondResultSetMetaData = secondResultSet.getMetaData();

      int firstResultSetColumnCount = firstResultSetMetaData.getColumnCount();
      int secondResultSetColumnCount = secondResultSetMetaData.getColumnCount();
      if (firstResultSetColumnCount != secondResultSetColumnCount) {
        logger.error("Number of columns in two queries do not match, please fix and re-run. ("
            + firstResultSetColumnCount
            + " vs. "
            + secondResultSetColumnCount
            + ")");
        fail("Number of columns in both queries do not match: "
            + firstResultSetColumnCount
            + " vs. "
            + secondResultSetColumnCount);
      }
      logger.info("Going to compare " + thresholds.size() + " thresholds");

      thresholdVerdict = compareResultSetsKeyedThreshold(firstResultSet, secondResultSet, firstResultSetColumnCount,
          thresholds, thresholdKeyIndex, thresholdReportIndex);
    } catch (SQLException e) {
      logger.error("Problem while processing the retrieved data set(s) ", e);
      fail("Problem while processing the retrieved data set(s), threw exception", e);
    }
    return thresholdVerdict;
  }

  private boolean compareResultSetsKeyedThreshold(
      ResultSet firstResultSet,
      ResultSet secondResultSet,
      int numberOfColumns,
      List<Threshold> thresholds,
      int thresholdKeyIndex,
      int thresholdReportIndex)
    throws SQLException {

    boolean keyedPass = true;
    boolean rowPass = true;
    String failLog = "";

    ResultSetMetaData metadata = secondResultSet.getMetaData();
    List<List<String>> result = convertResultSetToList(secondResultSet);

    try {
      List<String> row;
      if ((!firstResultSet.next()) || result.size() == 0) {
        fail("One or more Resultset is empty. Cannot be validated. \n");
        return false;
      }
      while (firstResultSet.next()) {
        rowPass = true;
        for (int thresholdnum = 0; thresholdnum < thresholds.size(); thresholdnum++) {
          for (int i = 0; i < result.size(); i++) {
            row = result.get(i);
            if (row.get(thresholdKeyIndex) == null) {
              fail("The Threshold Key Index is out of range when comparing against the second result set.\n" + failLog);
              return false;
            }
            if (firstResultSet.getString(thresholdKeyIndex + 1) == null) {
              fail("The Threshold Key Index is out of range when comparing against the first result set.\n" + failLog);
              return false;
            }
            if (row.get(thresholdKeyIndex).equals(firstResultSet.getString(thresholdKeyIndex + 1))) {
              String columnName = metadata.getColumnName(thresholds.get(thresholdnum).getColumnIndex() + 1);

              float firstVal = Float
                  .parseFloat(firstResultSet.getString(thresholds.get(thresholdnum).getColumnIndex() + 1));
              float secondVal = Float.parseFloat(row.get(thresholds.get(thresholdnum).getColumnIndex()));
              float difference = Math.abs(firstVal - secondVal);
              double diffPercentage = calculateDiffPercentage(firstVal, secondVal, true, 2);
              String thresholdReportValue = firstResultSet.getString(thresholdReportIndex + 1);

              String passFailMessage = generatePassFailMessageKeyedThreshold(thresholdReportValue,
                  row.get(thresholdReportIndex), firstResultSet.getString(thresholdKeyIndex + 1), columnName,
                  Float.toString(firstVal), Float.toString(secondVal), Float.toString(difference),
                  Double.toString(diffPercentage), thresholds.get(thresholdnum).getValue());

              if (diffPercentage > thresholds.get(thresholdnum).getValue()) {
                logger.error("[Keyed_Threshold Fail] " + passFailMessage);
                failLog = failLog + "[Keyed_Threshold Fail] " + passFailMessage + "\n";
                rowPass = false;
                keyedPass = false;
              } else {
                logger.info("[Keyed_Threshold Pass] " + passFailMessage);
              }
            }
          }
        }
        if (!rowPass) {
          for (int i = 1; i <= metadata.getColumnCount(); i++) {
            logger.debug(metadata.getColumnName(i) + ": " + firstResultSet.getString(i));
          }
        }
      }

      if (!keyedPass) {
        fail("One or more threshold values did not match the defined requirements.\n" + failLog);
        return false;
      }

    } catch (Exception e) {
      logger.error("Problem while processing the retrieved data set(s) ", e);
      fail("Problem while processing the retrieved data set(s), threw exception", e);
    }
    return keyedPass;
  }

  @VisibleForTesting
  List<List<String>> convertResultSetToList(ResultSet resultSet) throws SQLException {
    List<List<String>> result = new ArrayList<>();
    try {
      ResultSetMetaData metadata = resultSet.getMetaData();
      if (metadata == null) {
        return result;
      }
      int numCols = metadata.getColumnCount();
      while (resultSet.next()) {
        List<String> row = new ArrayList<>(numCols);
        int i = 1;
        while (i <= numCols) {
          row.add(resultSet.getString(i++));
        }
        result.add(row);
      }
      return result;
    } catch (SQLException e) {
      logger.error("Problem converting a result set to list (SQLException).", e);
      return result;
    } catch (Exception e) {
      logger.error("Problem converting a result set to list (Exception).", e);
      return result;
    }
  }

  @VisibleForTesting
  double calculateDiffPercentage(float firstVal, float secondVal, boolean round, int precision) {
    double diffPercentage;

    if (precision < 0) {
      precision = 0;
    }

    float targetValue = firstVal > secondVal ? firstVal : secondVal;
    diffPercentage = (double) Math.abs(firstVal - secondVal) / (double) targetValue * 100;

    if (round) {
      BigDecimal bd = new BigDecimal(diffPercentage);
      diffPercentage = bd.setScale(precision, RoundingMode.HALF_UP).doubleValue();
    }
    return diffPercentage;
  }

  @VisibleForTesting
  String generatePassFailMessageKeyedThreshold(
      String reportValue,
      String reportIndex,
      String secondReportIndex,
      String columnName,
      String firstVal,
      String secondVal,
      String difference,
      String diffPercentage,
      Double thresholdNum) {

    Joiner message = Joiner.on(", ").skipNulls();

    return message.join(reportValue, reportIndex, secondReportIndex, columnName, firstVal, secondVal, difference,
        diffPercentage, thresholdNum);

  }
}
