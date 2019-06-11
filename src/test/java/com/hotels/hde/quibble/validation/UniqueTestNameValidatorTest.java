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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.hotels.hde.quibble.Action;
import com.hotels.hde.quibble.Platforms;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.TestType;
import com.hotels.hde.quibble.exceptions.DuplicateTestNameException;

public class UniqueTestNameValidatorTest {

  @Test
  public void validateUniqueTestNamesPass() {
    List<TestCase[]> tests = new ArrayList<TestCase[]>();

    TestCase firstTest = new TestCase();
    firstTest.setTestType(TestType.COUNTMATCH.name());
    firstTest.setTestName("The first test");
    firstTest.setTestDescription("Just a description for the first test");

    Action action1 = new Action(Platforms.HIVE.getValue(), "Select count(*) from table");
    Action action2 = new Action(Platforms.TERADATA.getValue(), "Select count(*) from table");

    List<Action> firstTestActions = new ArrayList<Action>();

    firstTestActions.add(action1);
    firstTestActions.add(action2);
    firstTest.setActions(firstTestActions);

    TestCase secondTest = new TestCase();
    secondTest.setTestType(TestType.COUNTMATCH.name());
    secondTest.setTestName("The second test");
    secondTest.setTestDescription("Just a description for the second test");

    List<Action> secondTestActions = new ArrayList<Action>();

    secondTestActions.add(action1);
    secondTestActions.add(action2);
    secondTest.setActions(secondTestActions);

    TestCase firstTestArray[] = { firstTest };
    TestCase secondTestArray[] = { secondTest };

    tests.add(firstTestArray);
    tests.add(secondTestArray);

    new UniqueTestNameValidator().validate(tests);
  }

  @Test(expected = DuplicateTestNameException.class)
  public void validateUniqueTestNamesFailing() {
    List<TestCase[]> tests = new ArrayList<TestCase[]>();

    TestCase firstTest = new TestCase();
    firstTest.setTestType(TestType.COUNTMATCH.name());
    firstTest.setTestName("The first test");
    firstTest.setTestDescription("Just a description for the first test");

    Action action1 = new Action(Platforms.HIVE.getValue(), "Select count(*) from table");
    Action action2 = new Action(Platforms.TERADATA.getValue(), "Select count(*) from table");

    List<Action> firstTestActions = new ArrayList<Action>();

    firstTestActions.add(action1);
    firstTestActions.add(action2);
    firstTest.setActions(firstTestActions);

    TestCase secondTest = new TestCase();
    secondTest.setTestType(TestType.COUNTMATCH.name());
    secondTest.setTestName("The first test");
    secondTest.setTestDescription("Just a description for the second test");

    List<Action> secondTestActions = new ArrayList<Action>();

    secondTestActions.add(action1);
    secondTestActions.add(action2);
    secondTest.setActions(secondTestActions);

    TestCase firstTestArray[] = { firstTest };
    TestCase secondTestArray[] = { secondTest };

    tests.add(firstTestArray);
    tests.add(secondTestArray);

    new UniqueTestNameValidator().validate(tests);
  }
}
