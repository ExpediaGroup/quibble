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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.hotels.hde.quibble.Action;
import com.hotels.hde.quibble.Platforms;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.TestType;
import com.hotels.hde.quibble.Threshold;

public class TestCaseValidatorTest {

  @Test
  public void countMatchValidateRules() {

    TestCase aTestCase = new TestCase();

    aTestCase.setTestType(TestType.COUNTMATCH.getCode());
    aTestCase.setTestName("Testing count match test");

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

    TestCaseValidator validator = new TestCaseValidator(aTestCase);
    List<ValidationRule> rules = validator.getRules();

    assertThat(rules, hasItem(isA(MissingTestTypeValidationRule.class)));
    assertThat(rules, hasItem(isA(MissingTestNameValidationRule.class)));
    assertThat(rules, hasItem(isA(MissingActionsValidationRule.class)));
    assertThat(rules, hasItem(isA(AtLeastTwoActionsValidationRule.class)));
  }

  @Test
  public void countMatchThresholdValidateRules() {

    TestCase aTestCase = new TestCase();

    aTestCase.setTestType(TestType.COUNTMATCH_WITH_THRESHOLD.getCode());
    aTestCase.setTestName("Testing count match with threshold test");

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

    TestCaseValidator validator = new TestCaseValidator(aTestCase);
    List<ValidationRule> rules = validator.getRules();

    assertThat(rules, hasItem(isA(MissingTestTypeValidationRule.class)));
    assertThat(rules, hasItem(isA(MissingTestNameValidationRule.class)));
    assertThat(rules, hasItem(isA(MissingActionsValidationRule.class)));
    assertThat(rules, hasItem(isA(AtLeastTwoActionsValidationRule.class)));
    assertThat(rules, hasItem(isA(SingleThresholdValidationRule.class)));
  }

  @Test
  public void dataMatchValidateRules() {

    TestCase aTestCase = new TestCase();

    aTestCase.setTestType(TestType.DATAMATCH.getCode());
    aTestCase.setTestName("Testing data match test");

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

    TestCaseValidator validator = new TestCaseValidator(aTestCase);
    List<ValidationRule> rules = validator.getRules();

    assertThat(rules, hasItem(isA(MissingTestTypeValidationRule.class)));
    assertThat(rules, hasItem(isA(MissingTestNameValidationRule.class)));
    assertThat(rules, hasItem(isA(MissingActionsValidationRule.class)));
    assertThat(rules, hasItem(isA(AtMostTwoActionsValidationRule.class)));
  }

  @Test
  public void zeroCountValidateRules() {

    TestCase aTestCase = new TestCase();

    aTestCase.setTestType(TestType.ZEROCOUNT.getCode());
    aTestCase.setTestName("Testing zero count test");

    Action action1 = new Action();
    action1.setPlatform(Platforms.HIVE.getValue());
    action1.setCommand("Select count(*) from table");

    Action action2 = new Action();
    action2.setPlatform(Platforms.TERADATA.getValue());
    action2.setCommand("Select count(*) from table");

    List<Action> actions = new ArrayList<Action>();

    actions.add(action1);
    actions.add(action2);

    aTestCase.setActions(actions);

    TestCaseValidator validator = new TestCaseValidator(aTestCase);
    List<ValidationRule> rules = validator.getRules();

    assertThat(rules, hasItem(isA(MissingTestTypeValidationRule.class)));
    assertThat(rules, hasItem(isA(MissingTestNameValidationRule.class)));
    assertThat(rules, hasItem(isA(MissingActionsValidationRule.class)));
  }
}
