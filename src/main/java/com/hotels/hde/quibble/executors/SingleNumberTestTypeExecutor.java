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

import static org.testng.Assert.fail;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import com.hotels.hde.quibble.Action;
import com.hotels.hde.quibble.ActionResult;
import com.hotels.hde.quibble.MultiLineOutputStream;
import com.hotels.hde.quibble.Platforms;
import com.hotels.hde.quibble.StringUtils;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.connection.ActionConnection;

public abstract class SingleNumberTestTypeExecutor extends BaseTestTypeExecutor {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @VisibleForTesting
  List<ActionResult> executeCommands(TestCase aTestCase, List<ActionConnection> connectionList) {
    List<Action> actions = aTestCase.getActions();
    List<ActionResult> results = new ArrayList<ActionResult>(actions.size());
    ActionResult numericResult;

    for (Action action : actions) {

      String platform = action.getPlatform();
      String query = action.getCommand();
      logger.info("Query target is: " + platform);

      if (Platforms.SHELL.getValue().equals(platform)) {

        numericResult = getNumberFromShellCommand(platform, query);
        results.add(numericResult);
      } else {

        ActionConnection aConnection = findUnusedConnection(platform, connectionList);
        if (aConnection != null) {
          numericResult = getNumberFromQuery(platform, query, aConnection.getConnection());
          results.add(numericResult);
        }
      }
    }
    return results;
  }

  @VisibleForTesting
  ActionResult getNumberFromShellCommand(String platform, String shellCommand) {

    long value = -1;
    MultiLineOutputStream multiLineOutputStream = new MultiLineOutputStream();
    PumpStreamHandler streamHandler = new PumpStreamHandler(multiLineOutputStream);
    CommandLine commandline = CommandLine.parse(shellCommand);

    DefaultExecutor exec = new DefaultExecutor();
    exec.setStreamHandler(streamHandler);

    try {
      logger.info("Going to execute shell command: " + commandline);
      int exitValue = exec.execute(commandline);
      logger.info("Shell command's exit value is: " + exitValue);

      List<String> shellOutput = multiLineOutputStream.getLines();
      int outputLineCount = shellOutput.size();
      logger.info("Shell command returned " + outputLineCount + " lines as output");

      if (!shellOutput.isEmpty()) {
        String lastLine = shellOutput.get(outputLineCount - 1);
        value = Integer.parseInt(lastLine.trim());
      }
    } catch (NumberFormatException nfe) {
      logger.error("Last line in the output of shell command/script is not a valid number");
    } catch (ExecuteException e) {
      logger.error("Problem in executing Shell command, aborting test case!", e);
      fail("Problem in executing Shell command", e);
    } catch (IOException e) {
      logger.error("Problem in executing Shell command, aborting test case!", e);
      fail("Problem in executing Shell command", e);
    }

    logger.info("Shell command's returned numeric value is: " + value);
    return new ActionResult(platform, value);
  }

  @VisibleForTesting
  ActionResult getNumberFromQuery(String platform, String query, Connection connection) {

    long value = -1;
    query = StringUtils.trimSemicolon(query);
    int firstcolpos = 1;
    int permittedcolcount = 1;

    try {
      Statement stmt = connection.createStatement();
      ResultSet res = stmt.executeQuery(query);
      if (res.next()) {
        if (res.getMetaData().getColumnCount() == permittedcolcount) {
          value = res.getLong(firstcolpos);
          logger.info(platform.toUpperCase() + " query returned value: " + value);
          return new ActionResult(platform, value);
        } else {
          String errorMessage = platform.toUpperCase() + " query must return a single numeric value";
          logger.error(errorMessage);
          fail(errorMessage);
        }
      }
    } catch (SQLException e) {
      if (e.getCause() instanceof NumberFormatException) {
        String numericErrorMessage = "Query must return a single numeric value";
        logger.error(numericErrorMessage, e);
        fail(numericErrorMessage);
      } else {
        String errorMessage = "Problem with query execution " + e.getMessage();
        logger.error(errorMessage, e);
        fail(errorMessage);
      }
    }
    return new ActionResult(platform, value);
  }

  @VisibleForTesting
  boolean getNumberMatchVerdict(List<ActionResult> results) {
    boolean numberMatched = true;

    for (int i = 0; i < results.size() - 1; i++) {
      long firstValue = results.get(i).getNumericResult();
      for (int j = i + 1; j < results.size(); j++) {
        long secondValue = results.get(j).getNumericResult();
        if (firstValue != secondValue) {
          numberMatched = false;
        }
      }
      if (!numberMatched) {
        break;
      }
    }
    if (!numberMatched) {
      logger.info("Numeric values from all queries/commands do not match!");
    }
    return numberMatched;
  }

  @VisibleForTesting
  String getNumericValueWithPlatformAsString(List<ActionResult> results) {
    String result = null;
    if (!results.isEmpty()) {
      ActionResult aResult = results.get(results.size() - 1);
      result = String.format(System.getProperty("line.separator"), " Platform: " + aResult.getPlatform().trim(),
          " returned value: " + aResult.getNumericResult());
    }

    return result;
  }

  @Override
  public abstract void execute(TestCase aTestCase, List<ActionConnection> connectionList);

}
