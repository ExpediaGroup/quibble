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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import com.hotels.hde.quibble.ActionResult;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.connection.ActionConnection;

public class ZeroNumberTestExecutor extends SingleNumberTestTypeExecutor {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void execute(TestCase aTestCase, List<ActionConnection> connectionList) {
    List<ActionResult> results = executeCommands(aTestCase, connectionList);
    boolean numberMatchedToZero = getZeroNumberVerdict(results);
    assertTrue(numberMatchedToZero,
        "All queries/commands did not return zero: " + getNumericValueWithPlatformAsString(results));
  }

  @VisibleForTesting
  boolean getZeroNumberVerdict(List<ActionResult> results) {
    boolean testVerdict = true;
    for (ActionResult aResult : results) {
      if (aResult.getNumericResult() != 0) {
        testVerdict = false;
        logger.error(aResult.getPlatform()
            + " query/command does not return zero, instead it returned: "
            + aResult.getNumericResult());
      } else {
        logger.info(aResult.getPlatform() + " query/command returned zero");
      }
    }
    return testVerdict;
  }

}
