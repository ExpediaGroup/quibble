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
import com.hotels.hde.quibble.exceptions.IncorrectTestDefinitionException;

public class MissingTestNameValidationRuleTest {

  @Test(expected = IncorrectTestDefinitionException.class)
  public void validateMissingTestName() throws IncorrectTestDefinitionException {
    TestCase aTestCase = new TestCase();

    aTestCase.setTestType(TestType.DATAMATCH.name());

    Action action1 = new Action();
    action1.setPlatform(Platforms.HIVE.getValue());
    action1.setCommand("Select * from table");

    Action action2 = new Action();
    action2.setPlatform(Platforms.TERADATA.getValue());
    action2.setCommand("Select * from table");

    List<Action> actions = new ArrayList<Action>();

    actions.add(action1);
    actions.add(action2);

    aTestCase.setActions(actions);
    MissingTestNameValidationRule rule = new MissingTestNameValidationRule();
    rule.validate(aTestCase);
  }

  @Test
  public void validateMissingTestNameSucess() throws IncorrectTestDefinitionException {
    TestCase aTestCase = new TestCase();

    aTestCase.setTestType(TestType.DATAMATCH.name());
    aTestCase.setTestName("A sucessfull test case with a name");

    Action action1 = new Action();
    action1.setPlatform(Platforms.HIVE.getValue());
    action1.setCommand("Select * from table");

    Action action2 = new Action();
    action2.setPlatform(Platforms.TERADATA.getValue());
    action2.setCommand("Select * from table");

    List<Action> actions = new ArrayList<Action>();

    actions.add(action1);
    actions.add(action2);

    aTestCase.setActions(actions);
    MissingTestNameValidationRule rule = new MissingTestNameValidationRule();
    rule.validate(aTestCase);
  }
}
