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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.hotels.hde.quibble.ActionResult;
import com.hotels.hde.quibble.Platforms;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.TestUtils;
import com.hotels.hde.quibble.connection.ActionConnection;

@RunWith(MockitoJUnitRunner.class)
public class ZeroNumberTestExecutorTest {

  private final ZeroNumberTestExecutor zeroCountTest = new ZeroNumberTestExecutor() {
    @Override
    List<ActionResult> executeCommands(TestCase aTestCase, List<ActionConnection> connectionList) {
      List<ActionResult> results = new ArrayList<ActionResult>();
      ActionResult actionResult = new ActionResult(Platforms.TERADATA.getValue(), 0);
      results.add(actionResult);
      return results;
    }
  };

  @Test
  public void testexecuteCommand() {

    Connection mockConnection = mock(Connection.class);

    TestCase aCountMatchTest = TestUtils.createASingleNumberMatchTest("Test 101",
        Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    List<ActionConnection> connectionList = new ArrayList<>();

    ActionConnection actionConnection = new ActionConnection(
        Platforms.TERADATA.getValue(),
        mockConnection, true);
    connectionList.add(actionConnection);

    zeroCountTest.execute(aCountMatchTest, connectionList);
  }

  @Test
  public void testGetZeroCountVerdictFailure() {

    List<ActionResult> results = new ArrayList<>();

    ActionResult testResult1 = new ActionResult("hive", 0);
    ActionResult testResult2 = new ActionResult("db2", 234);
    ActionResult testResult3 = new ActionResult("teradata", 0);

    results.add(testResult1);
    results.add(testResult2);
    results.add(testResult3);

    boolean verdict = zeroCountTest.getZeroNumberVerdict(results);
    assertFalse(verdict);
  }

  @Test
  public void testGetZeroCountVerdictSuccess() {

    List<ActionResult> results = new ArrayList<>();

    ActionResult testResult1 = new ActionResult("hive", 0);
    ActionResult testResult2 = new ActionResult("db2", 0);

    results.add(testResult1);
    results.add(testResult2);

    boolean verdict = zeroCountTest.getZeroNumberVerdict(results);
    assertTrue(verdict);
  }
}
