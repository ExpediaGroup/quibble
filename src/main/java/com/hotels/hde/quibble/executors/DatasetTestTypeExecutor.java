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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.wrappers.StringTrimmedResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import com.hotels.hde.quibble.Action;
import com.hotels.hde.quibble.ActionDataResult;
import com.hotels.hde.quibble.StringUtils;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.connection.ActionConnection;

public abstract class DatasetTestTypeExecutor extends BaseTestTypeExecutor {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public abstract void execute(TestCase aTestCase, List<ActionConnection> connectionList);

  @VisibleForTesting
  List<ActionDataResult> executeDataQueries(TestCase aTestCase, List<ActionConnection> connectionList) {
    List<Action> actions = aTestCase.getActions();
    List<ActionDataResult> actionDataResults = new ArrayList<ActionDataResult>(actions.size());
    ActionDataResult actionDataResult;

    for (Action action : actions) {
      String platform = action.getPlatform();
      String query = action.getCommand();
      logger.info("Query target platform is: " + platform);

      ActionConnection aConnection = findUnusedConnection(platform, connectionList);
      if (aConnection != null) {
        actionDataResult = getDataFromQuery(platform, query, aConnection.getConnection());
        actionDataResults.add(actionDataResult);
      }
    }
    return actionDataResults;
  }

  @VisibleForTesting
  ActionDataResult getDataFromQuery(String platform, String query, Connection connection) {
    ResultSet resultSet = null;
    query = StringUtils.trimSemicolon(query);
    try {
      Statement stmt = connection.createStatement();
      resultSet = stmt.executeQuery(query);
    } catch (SQLException e) {
      logger.error("Problem with executing Query: " + query, e);
      fail("Problem with executing Query: " + query + " threw exception", e);
    }
    resultSet = StringTrimmedResultSet.wrap(resultSet);
    return new ActionDataResult(platform, resultSet);
  }

}
