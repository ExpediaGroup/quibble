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
package com.hotels.hde.quibble.validation;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.hotels.hde.quibble.Action;
import com.hotels.hde.quibble.Platforms;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.TestType;
import com.hotels.hde.quibble.Threshold;
import com.hotels.hde.quibble.exceptions.IncorrectTestDefinitionException;

public class SingleThresholdValidationRuleTest {

  @Test(expected = IncorrectTestDefinitionException.class)
  public void validateThresholdValueInvalidColumnIndex() {
    TestCase aTestCase = new TestCase();

    aTestCase.setTestType(TestType.COUNTMATCH_WITH_THRESHOLD.name());
    aTestCase.setTestName("Testing Threshold Limit");

    List<Threshold> thresholds = new ArrayList<Threshold>();
    Threshold threshold1 = new Threshold();

    threshold1.setColumnIndex(0); // Incorrect First column index
    threshold1.setValue(10);

    thresholds.add(threshold1);
    aTestCase.setThresholds(thresholds);

    Action action1 = new Action();
    action1.setPlatform(Platforms.HIVE.getValue());
    action1.setCommand("Select count(*) from table");

    Action action2 = new Action();
    action2.setPlatform(Platforms.TERADATA.getValue());
    action2.setCommand("Select count(*) from table");

    Action action3 = new Action();
    action3.setPlatform(Platforms.DB2.getValue());
    action3.setCommand("Select count(*) from table");

    List<Action> actions = new ArrayList<Action>();

    actions.add(action1);
    actions.add(action2);
    actions.add(action3);

    aTestCase.setActions(actions);
    SingleThresholdValidationRule rule = new SingleThresholdValidationRule();
    rule.validate(aTestCase);
  }

  @Test(expected = IncorrectTestDefinitionException.class)
  public void validateThresholdValueMultipleValues() {
    TestCase aTestCase = new TestCase();

    aTestCase.setTestType(TestType.COUNTMATCH_WITH_THRESHOLD.name());
    aTestCase.setTestName("Testing Threshold Limit");

    List<Threshold> thresholds = new ArrayList<Threshold>();
    Threshold threshold1 = new Threshold();

    threshold1.setColumnIndex(1); // First column
    threshold1.setValue(10);

    Threshold threshold2 = new Threshold();
    threshold2.setColumnIndex(2); // Second column
    threshold2.setValue(10);

    thresholds.add(threshold1);
    thresholds.add(threshold2);

    aTestCase.setThresholds(thresholds);

    Action action1 = new Action();
    action1.setPlatform(Platforms.HIVE.getValue());
    action1.setCommand("Select count(*) from table");

    Action action2 = new Action();
    action2.setPlatform(Platforms.TERADATA.getValue());
    action2.setCommand("Select count(*) from table");

    Action action3 = new Action();
    action3.setPlatform(Platforms.DB2.getValue());
    action3.setCommand("Select count(*) from table");

    List<Action> actions = new ArrayList<Action>();

    actions.add(action1);
    actions.add(action2);
    actions.add(action3);

    aTestCase.setActions(actions);
    SingleThresholdValidationRule rule = new SingleThresholdValidationRule();
    rule.validate(aTestCase);
  }

  @Test(expected = IncorrectTestDefinitionException.class)
  public void validateEmptyThreshold() {
    TestCase aTestCase = new TestCase();

    aTestCase.setTestType(TestType.COUNTMATCH_WITH_THRESHOLD.name());
    aTestCase.setTestName("Testing Threshold Limit");

    List<Threshold> thresholds = new ArrayList<Threshold>();

    aTestCase.setThresholds(thresholds);

    Action action1 = new Action();
    action1.setPlatform(Platforms.HIVE.getValue());
    action1.setCommand("Select count(*) from table");

    Action action2 = new Action();
    action2.setPlatform(Platforms.TERADATA.getValue());
    action2.setCommand("Select count(*) from table");

    Action action3 = new Action();
    action3.setPlatform(Platforms.DB2.getValue());
    action3.setCommand("Select count(*) from table");

    List<Action> actions = new ArrayList<Action>();

    actions.add(action1);
    actions.add(action2);
    actions.add(action3);

    aTestCase.setActions(actions);
    SingleThresholdValidationRule rule = new SingleThresholdValidationRule();
    rule.validate(aTestCase);
  }

  @Test(expected = IncorrectTestDefinitionException.class)
  public void validateMissingThreshold() {
    TestCase aTestCase = new TestCase();

    aTestCase.setTestType(TestType.COUNTMATCH_WITH_THRESHOLD.name());
    aTestCase.setTestName("Testing Threshold Limit");

    Action action1 = new Action();
    action1.setPlatform(Platforms.HIVE.getValue());
    action1.setCommand("Select count(*) from table");

    Action action2 = new Action();
    action2.setPlatform(Platforms.TERADATA.getValue());
    action2.setCommand("Select count(*) from table");

    Action action3 = new Action();
    action3.setPlatform(Platforms.DB2.getValue());
    action3.setCommand("Select count(*) from table");

    List<Action> actions = new ArrayList<Action>();

    actions.add(action1);
    actions.add(action2);
    actions.add(action3);

    aTestCase.setActions(actions);
    SingleThresholdValidationRule rule = new SingleThresholdValidationRule();
    rule.validate(aTestCase);
  }

  @Test
  public void validateThresholdValueSuccess() {
    TestCase aTestCase = new TestCase();

    aTestCase.setTestType(TestType.COUNTMATCH_WITH_THRESHOLD.name());
    aTestCase.setTestName("Testing Threshold Limit");

    List<Threshold> thresholds = new ArrayList<Threshold>();
    Threshold threshold1 = new Threshold();

    threshold1.setColumnIndex(1); // First column
    threshold1.setValue(10);

    thresholds.add(threshold1);

    aTestCase.setThresholds(thresholds);

    Action action1 = new Action();
    action1.setPlatform(Platforms.HIVE.getValue());
    action1.setCommand("Select count(*) from table");

    Action action2 = new Action();
    action2.setPlatform(Platforms.TERADATA.getValue());
    action2.setCommand("Select count(*) from table");

    Action action3 = new Action();
    action3.setPlatform(Platforms.DB2.getValue());
    action3.setCommand("Select count(*) from table");

    List<Action> actions = new ArrayList<Action>();

    actions.add(action1);
    actions.add(action2);
    actions.add(action3);

    aTestCase.setActions(actions);
    SingleThresholdValidationRule rule = new SingleThresholdValidationRule();
    rule.validate(aTestCase);
  }
}
