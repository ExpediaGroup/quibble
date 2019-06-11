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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.hotels.hde.quibble.ActionResult;
import com.hotels.hde.quibble.Platforms;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.TestUtils;
import com.hotels.hde.quibble.connection.ActionConnection;

public class SingleNumberMatchThresholdTestExecutorTest {

  private final SingleNumberMatchThresholdTestExecutor thresholdTypeTest = new SingleNumberMatchThresholdTestExecutor() {
    @Override
    List<ActionResult> executeCommands(TestCase aTestCase, List<ActionConnection> connectionList) {
      List<ActionResult> results = new ArrayList<ActionResult>();
      ActionResult actionResult1 = new ActionResult(Platforms.TERADATA.getValue(), 100);
      ActionResult actionResult2 = new ActionResult(Platforms.HIVE.getValue(), 105);
      ActionResult actionResult3 = new ActionResult(Platforms.SHELL.getValue(), 95);
      results.add(actionResult1);
      results.add(actionResult2);
      results.add(actionResult3);
      return results;
    }
  };

  @Test
  public void testexecuteCommand() {

    Connection mockConnection = mock(Connection.class);
    TestCase aCountMatchTest = TestUtils.createASingleNumberMatchThresholdTest("Test 101",
        Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    List<ActionConnection> connectionList = new ArrayList<>();

    ActionConnection actionConnection = new ActionConnection(
        Platforms.TERADATA.getValue(),
        mockConnection, true);
    connectionList.add(actionConnection);

    thresholdTypeTest.execute(aCountMatchTest, connectionList);
  }

  @Test
  public void testGetCountMatchThresholdVerdictSuccess() {

    List<ActionResult> results = new ArrayList<>();

    ActionResult testResult1 = new ActionResult("hive", 100);
    ActionResult testResult2 = new ActionResult("db2", 98);
    ActionResult testResult3 = new ActionResult("hive", 91);

    results.add(testResult1);
    results.add(testResult2);
    results.add(testResult3);

    boolean verdict = thresholdTypeTest.getNumberMatchThresholdVerdict(results, 10);
    assertTrue("Difference is within the permitted threshold", verdict);
  }

  @Test
  public void testGetCountMatchThresholdVerdictFailure() {

    List<ActionResult> results = new ArrayList<>();

    ActionResult testResult1 = new ActionResult("hive", 100);
    ActionResult testResult2 = new ActionResult("db2", 80);
    ActionResult testResult3 = new ActionResult("db2", 89);

    results.add(testResult1);
    results.add(testResult2);
    results.add(testResult3);

    boolean verdict = thresholdTypeTest.getNumberMatchThresholdVerdict(results, 10);
    assertFalse("Difference is higher than the permitted threshold", verdict);
  }
}
