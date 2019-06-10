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
package com.hotels.hde.quibble.validation;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.hotels.hde.quibble.Action;
import com.hotels.hde.quibble.Platforms;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.TestType;

public class DefaultTestDescriptionTest {

  @Test
  public void emptyAsDefaultTestDescription() {

    TestCase aTestCase = new TestCase();
    aTestCase.setTestType(TestType.COUNTMATCH.name());
    aTestCase.setTestName("The first test");

    Action action1 = new Action(Platforms.HIVE.getValue(), "Select count(*) from table");
    Action action2 = new Action(Platforms.TERADATA.getValue(), "Select count(*) from table");

    List<Action> firstTestActions = new ArrayList<Action>();

    firstTestActions.add(action1);
    firstTestActions.add(action2);
    aTestCase.setActions(firstTestActions);

    assertEquals(aTestCase.getTestDescription(), "");
  }

  @Test
  public void userDefinedTestDescription() {

    String testDescription = "This is user defined description";

    TestCase aTestCase = new TestCase();
    aTestCase.setTestType(TestType.COUNTMATCH.name());
    aTestCase.setTestName("The first test");
    aTestCase.setTestDescription(testDescription);

    Action action1 = new Action(Platforms.HIVE.getValue(), "Select count(*) from table");
    Action action2 = new Action(Platforms.TERADATA.getValue(), "Select count(*) from table");

    List<Action> firstTestActions = new ArrayList<Action>();

    firstTestActions.add(action1);
    firstTestActions.add(action2);
    aTestCase.setActions(firstTestActions);

    assertEquals(aTestCase.getTestDescription(), testDescription);
  }
}
