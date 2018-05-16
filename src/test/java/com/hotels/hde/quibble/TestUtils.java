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
package com.hotels.hde.quibble;

import java.util.ArrayList;
import java.util.List;

public class TestUtils {

  private TestUtils() {}

  public static TestCase createASingleNumberMatchTest(String testName, String firstPlatform, String secondPlatform) {

    TestCase aTestCase = new TestCase();
    aTestCase.setTestType(TestType.COUNTMATCH.getCode());
    aTestCase.setTestName(testName);
    aTestCase.setTestDescription("A Dummy Test Decsription for count match test");

    Action action1 = new Action(firstPlatform, "Select count(*) from table");
    Action action2 = new Action(secondPlatform, "Select count(*) from table");
    List<Action> actions = new ArrayList<Action>();
    actions.add(action1);
    actions.add(action2);
    aTestCase.setActions(actions);

    return aTestCase;
  }

  public static TestCase createASingleNumberMatchThresholdTest(String testName, String firstPlatform,
                                                               String secondPlatform) {

    TestCase aTestCase = new TestCase();
    aTestCase.setTestType(TestType.COUNTMATCH_WITH_THRESHOLD.getCode());
    aTestCase.setTestName(testName);
    aTestCase.setTestDescription("A Dummy Test Decsription for count match threshold test");

    Action action1 = new Action(firstPlatform, "Select count(*) from table");
    Action action2 = new Action(secondPlatform, "Select count(*) from table");
    List<Action> actions = new ArrayList<Action>();
    actions.add(action1);
    actions.add(action2);
    aTestCase.setActions(actions);

    Threshold aThreshold = new Threshold();
    aThreshold.setColumnIndex(1);
    aThreshold.setValue(10);
    List<Threshold> thresholds = new ArrayList<Threshold>();
    thresholds.add(aThreshold);

    aTestCase.setThresholds(thresholds);

    return aTestCase;
  }

  static TestCase createADataMatchTest(String testName, String firstPlatform, String secondPlatform) {

    TestCase aTestCase = new TestCase();
    aTestCase.setTestType(TestType.DATAMATCH.getCode());
    aTestCase.setTestName(testName);
    aTestCase.setTestDescription("A Dummy Test Decsription for data match test");

    Action action1 = new Action(firstPlatform, "Select * from table");
    Action action2 = new Action(secondPlatform, "Select * from table");
    List<Action> actions = new ArrayList<Action>();
    actions.add(action1);
    actions.add(action2);
    aTestCase.setActions(actions);

    return aTestCase;
  }

  static TestCase createAZeroNumberMatchTest(String testName, String platform) {

    TestCase aTestCase = new TestCase();
    aTestCase.setTestType(TestType.ZEROCOUNT.getCode());
    aTestCase.setTestName(testName);
    aTestCase.setTestDescription("A Dummy Test Decsription for zero match test");

    Action action1 = new Action(platform, "Select count(*) from table");
    List<Action> actions = new ArrayList<Action>();
    actions.add(action1);
    aTestCase.setActions(actions);

    return aTestCase;
  }
}
