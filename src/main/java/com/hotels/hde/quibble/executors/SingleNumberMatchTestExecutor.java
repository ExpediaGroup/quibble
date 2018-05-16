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

import static org.testng.Assert.fail;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotels.hde.quibble.ActionResult;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.connection.ActionConnection;

public class SingleNumberMatchTestExecutor extends SingleNumberTestTypeExecutor {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void execute(TestCase aTestCase, List<ActionConnection> connectionList) {
    List<ActionResult> results = executeCommands(aTestCase, connectionList);
    boolean numberMatched = getNumberMatchVerdict(results);
    String numbersWithPlatformMessage = getNumericValueWithPlatformAsString(results);
    logger.info("Returned values are " + numbersWithPlatformMessage);

    if (!numberMatched) {
      fail("Numeric values returned by all queries/commands do not match : " + numbersWithPlatformMessage);
    }
  }
}
