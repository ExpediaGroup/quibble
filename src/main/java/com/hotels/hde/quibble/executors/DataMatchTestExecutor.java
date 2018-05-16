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

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import com.hotels.hde.quibble.ActionDataResult;
import com.hotels.hde.quibble.RowMatchResult;
import com.hotels.hde.quibble.RowPair;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.connection.ActionConnection;
import com.hotels.hde.quibble.report.ReportGenerator;

public class DataMatchTestExecutor extends DatasetTestTypeExecutor {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private static final int DEFAULT_MAX_ALLOWED_MISMATCHES = 500000;
  private static final int DEFAULT_PROCESSED_ROWS_CHECKPOINT = 50000;
  private static final String NONE = "None";
  private static final String ALL = "All";

  private ReportGenerator reportGenerator;

  public int getAllowedMismatches() {
    return allowedMismatches;
  }

  public void setAllowedMismatches(int allowedMismatches) {
    this.allowedMismatches = allowedMismatches;
  }

  private int allowedMismatches = 0;

  private final String reportDifferences;
  private final String reportPath;

  public DataMatchTestExecutor(String reportDifferences, String reportPath) {
    this.reportDifferences = reportDifferences;
    this.reportPath = reportPath;
  }

  @VisibleForTesting
  int getMaxAllowedMismatchLimit(String reportDifferences) {
    int maxAllowedMismatches = 0;

    if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(reportDifferences, NONE)) {
      maxAllowedMismatches = 0;
    } else if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(reportDifferences, ALL)) {
      maxAllowedMismatches = DEFAULT_MAX_ALLOWED_MISMATCHES;
    } else if (org.apache.commons.lang.StringUtils.isNumeric(reportDifferences)) {
      long reportDiffsCount = Long.parseLong(reportDifferences);
      if (reportDiffsCount > 0) {
        maxAllowedMismatches = (int) Math.min(DEFAULT_MAX_ALLOWED_MISMATCHES, reportDiffsCount);
      }
    } else {
      logger.error("Invalid value for ReportDiffs command line parameter, numeric value expected");
      fail("Invalid value for ReportDiffs command line parameter, numeric value expected");
    }
    return maxAllowedMismatches;
  }

  @Override
  public void execute(TestCase aTestCase, List<ActionConnection> connectionList) {
    allowedMismatches = getMaxAllowedMismatchLimit(reportDifferences);

    if (allowedMismatches > 0) {
      reportGenerator = new ReportGenerator();
      reportGenerator.createSheetTitle(aTestCase.getTestName());
    }

    List<ActionDataResult> actionDataResults = executeDataQueries(aTestCase, connectionList);
    boolean dataMatched = getDataMatchVerdict(actionDataResults);
    assertTrue(dataMatched, "Data from two queries did not match!");
  }

  @VisibleForTesting
  boolean getDataMatchVerdict(List<ActionDataResult> resultSets) {
    boolean matchVerdict = false;

    ActionDataResult firstActionDataResult = resultSets.get(0);
    ResultSet firstResultSet = firstActionDataResult.getResultSet();
    String firstPlatform = firstActionDataResult.getPlatform();

    ActionDataResult secondActionDataResult = resultSets.get(1);
    ResultSet secondResultSet = secondActionDataResult.getResultSet();
    String secondPlatform = secondActionDataResult.getPlatform();

    try {
      ResultSetMetaData firstResultSetMetaData = firstResultSet.getMetaData();
      ResultSetMetaData secondResultSetMetaData = secondResultSet.getMetaData();

      int firstResultSetColumnCount = firstResultSetMetaData.getColumnCount();
      int secondResultSetColumnCount = secondResultSetMetaData.getColumnCount();

      if (firstResultSetColumnCount != secondResultSetColumnCount) {
        logger.error("Number of columns in two queries do not match, please fix and re-run");
        logger.error(firstPlatform + " platform returned " + firstResultSetColumnCount + " columns");
        logger.error(secondPlatform + " platform returned " + secondResultSetColumnCount + " columns");

        fail("Number of columns in both queries do not match: "
            + firstResultSetColumnCount
            + " vs. "
            + secondResultSetColumnCount);
      }

      logger.info("Going to compare data: " + firstPlatform + " against " + secondPlatform);
      if (allowedMismatches > 0) {
        matchVerdict = compareResultSetsGenerateReport(firstResultSet, secondResultSet, firstResultSetColumnCount);
        reportGenerator.closeReport(reportPath);
      } else {
        matchVerdict = compareResultSets(firstResultSet, secondResultSet, firstResultSetColumnCount);
      }
    } catch (SQLException e) {
      logger.error("Problem while dealing with the retrieved data set(s) ", e);
      fail("Problem while dealing with the retrieved data set(s), threw SQL exception", e);
    } catch (IOException e) {
      logger.error("Problem while dealing with the retrieved data set(s) ", e);
      fail("Problem while dealing with the retrieved data set(s), threw IOException", e);
    }
    return matchVerdict;
  }

  @VisibleForTesting
  boolean compareResultSetsGenerateReport(
      ResultSet firstResultSet,
      ResultSet secondResultSet,
      int numberOfColumns)
    throws SQLException, IOException {

    boolean dataExists = false;
    boolean firstDiff = true;
    long rowsMatched = 0;
    int checkpoinMarker = 0;
    long rowsMismatched = 0;
    int writeAtPosition = ReportGenerator.FIRST_DATA_ROW_NUMBER;

    while (true) {
      boolean firstResultSetHasRow = firstResultSet.next();
      boolean secondResultSetHasRow = secondResultSet.next();

      if (firstResultSetHasRow && secondResultSetHasRow) {
        RowPair rowPair = getRowPair(firstResultSet, secondResultSet, numberOfColumns);
        RowMatchResult rowMatchResult = compareRows(rowPair);

        if (!rowMatchResult.rowMatched()) {
          if (rowsMismatched < allowedMismatches) {
            if (firstDiff) {
              reportGenerator.addHeadersToReport(firstResultSet.getMetaData(), secondResultSet.getMetaData());
              firstDiff = false;
            }
            rowsMismatched++;
            reportGenerator.addRowPairToReport(rowPair, writeAtPosition, rowMatchResult.getDifferences());
            writeAtPosition = writeAtPosition + 2;
          } else {
            String logInfoMessage1 = "Successfully matched: " + rowsMatched + " rows";
            String logInfoMessage2 = "Reached limit set for data mismatch reporting: " + allowedMismatches;
            logger.info(logInfoMessage1);
            logger.info(logInfoMessage2);
            reportGenerator.addMessageRow(logInfoMessage1 + ". " + logInfoMessage2, writeAtPosition);
            return false;
          }
        } else {
          rowsMatched++;
          checkpoinMarker++;
          if (checkpoinMarker == DEFAULT_PROCESSED_ROWS_CHECKPOINT) {
            logger.info(rowsMatched + " rows matched, while " + rowsMismatched + " rows mismatched till now");
            checkpoinMarker = 0;
          }
        }
        dataExists = true;
      } else if (firstResultSetHasRow ^ secondResultSetHasRow) {
        String logInfoMessage = "Number of records from queries that matched so far: " + rowsMatched;
        String logErrorMessage = "Total number of records returned by queries are not equal";
        logger.info(logInfoMessage);
        logger.error(logErrorMessage);
        reportGenerator.addMessageRow(logInfoMessage + ". " + logErrorMessage, writeAtPosition);
        return false;
      } else {
        if (!dataExists) {
          String logErrorMessage = "No data found, please check your queries";
          logger.error(logErrorMessage);
          reportGenerator.addMessageRow(logErrorMessage, writeAtPosition);
          return false;
        } else {
          String logInfoMessage1 = "Successfully matched: " + rowsMatched + " rows";
          String logInfoMessage2 = "Reached end of both data sets!";
          logger.info(logInfoMessage1);
          logger.info(logInfoMessage2);
          reportGenerator.addMessageRow(logInfoMessage1 + ". " + logInfoMessage2, writeAtPosition);
          return rowsMismatched <= 0;
        }
      }
    }
  }

  @VisibleForTesting
  RowPair getRowPair(ResultSet firstResultSet, ResultSet secondResultSet, int numberOfColumns) throws SQLException {

    int columnPosition = 1;
    RowPair rowPair = new RowPair(numberOfColumns);
    String[] firstRSRow = rowPair.getFirstRSRow();
    String[] secondRSRow = rowPair.getSecondRSRow();

    while (columnPosition <= numberOfColumns) {
      firstRSRow[columnPosition - 1] = firstResultSet.getString(columnPosition);
      secondRSRow[columnPosition - 1] = secondResultSet.getString(columnPosition);
      columnPosition++;
    }
    return rowPair;
  }

  private boolean compareResultSets(ResultSet firstResultSet, ResultSet secondResultSet, int numberOfColumns)
    throws SQLException {

    boolean dataExists = false;
    long rowsMatched = 0;

    while (true) {

      boolean firstResultSetHasRow = firstResultSet.next();
      boolean secondResultSetHasRow = secondResultSet.next();

      if (firstResultSetHasRow && secondResultSetHasRow) {
        int columnPosition = 1;

        while (columnPosition <= numberOfColumns) {
          String columnValue1 = firstResultSet.getString(columnPosition);
          String columnValue2 = secondResultSet.getString(columnPosition);

          if (!StringUtils.equals(columnValue1, columnValue2)) {
            logger.info("So far successfully macthed: " + rowsMatched + " rows");
            logger.error("Column Values at Row number: "
                + (rowsMatched + 1)
                + " Column number: "
                + columnPosition
                + " do not match");
            logger.error("Column Name: "
                + firstResultSet.getMetaData().getColumnName(columnPosition)
                + " with Value: "
                + columnValue1);
            logger.error("is being compared to");
            logger.error("Column Name: "
                + secondResultSet.getMetaData().getColumnName(columnPosition)
                + " with Value: "
                + columnValue2);
            fail("Column Values at Row number: "
                + (rowsMatched + 1)
                + " Column number: "
                + columnPosition
                + " do not match. Column values are not equal: "
                + columnValue1
                + " vs. "
                + columnValue2);
          }
          columnPosition++;
        }
        rowsMatched++;
        dataExists = true;
      } else if (firstResultSetHasRow ^ secondResultSetHasRow) {
        logger.info("Number of records from queries that matched so far: " + rowsMatched);
        logger.error("Total number of records returned by queries are not equal");
        return false;
      } else {
        if (!dataExists) {
          logger.error("No data found, please check your queries");
          return false;
        } else {
          logger.info("Successfully matched: " + rowsMatched + " rows");
          logger.info("Reached end of both result sets, all data matched!");
          return true;
        }
      }
    }
  }

  @VisibleForTesting
  RowMatchResult compareRows(RowPair rowPair) {

    String[] firstRow = rowPair.getFirstRSRow();
    String[] secondRow = rowPair.getSecondRSRow();

    boolean rowMatched = true;
    BitSet differences = new BitSet(firstRow.length);

    for (int index = 0; index < firstRow.length; index++) {
      if (!org.apache.commons.lang.StringUtils.equals(firstRow[index], secondRow[index])) {
        differences.set(index);
        rowMatched = false;
      }
    }
    return new RowMatchResult(rowMatched, differences);
  }

}
