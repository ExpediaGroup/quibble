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

import java.util.Collections;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;

import com.hotels.hde.quibble.ActionResult;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.Threshold;
import com.hotels.hde.quibble.connection.ActionConnection;

public class SingleNumberMatchThresholdTestExecutor extends SingleNumberTestTypeExecutor {

  @Override
  public void execute(TestCase aTestCase, List<ActionConnection> connectionList) {
    List<ActionResult> results = executeCommands(aTestCase, connectionList);
    boolean numberMatched = getNumberMatchVerdict(results);
    String numbersWithPlatformMessage = getNumericValueWithPlatformAsString(results);
    final int FIRST_COLUMN_INDEX = 0;

    if (!numberMatched) {
      List<Threshold> thresholds = aTestCase.getThresholds();
      Threshold threshold = thresholds.get(FIRST_COLUMN_INDEX);
      double thresholdValue = threshold.getValue();

      boolean withinThreshold = getNumberMatchThresholdVerdict(results, thresholdValue);
      assertTrue(withinThreshold,
          "Numeric values returned by all queries/commands do not match within permitted threshold value: "
              + thresholdValue
              + " "
              + numbersWithPlatformMessage);
    }
  }

  @VisibleForTesting
  boolean getNumberMatchThresholdVerdict(List<ActionResult> results, double thresholdValue) {
    Collections.sort(results);
    int size = results.size();

    long smallestValue = results.get(0).getNumericResult();
    long largestValue = results.get(size - 1).getNumericResult();

    long difference = largestValue - smallestValue;
    double differencePercentage = (double) difference / (double) largestValue * 100;

    return differencePercentage <= thresholdValue;
  }

}
