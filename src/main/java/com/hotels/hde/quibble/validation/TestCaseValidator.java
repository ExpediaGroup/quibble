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

import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.TestType;

public class TestCaseValidator {

  private final TestCase aTestCase;
  private final List<ValidationRule> rules;

  public TestCaseValidator(TestCase aTestCase) {
    this.aTestCase = aTestCase;
    rules = buildRules();
  }

  List<ValidationRule> getRules() {
    return rules;
  }

  private List<ValidationRule> buildRules() {
    List<ValidationRule> rules = new ArrayList<ValidationRule>();
    rules.add(new MissingTestTypeValidationRule());
    rules.add(new MissingTestNameValidationRule());
    rules.add(new MissingActionsValidationRule());

    TestType testType = TestType.get(aTestCase.getTestType());

    if (testType != null) {
      switch (testType) {
      case COUNTMATCH:
        rules.add(new AtLeastTwoActionsValidationRule());
        break;

      case COUNTMATCH_WITH_THRESHOLD:
        rules.add(new AtLeastTwoActionsValidationRule());
        rules.add(new SingleThresholdValidationRule());
        break;
      case DATAMATCH:
        rules.add(new AtMostTwoActionsValidationRule());
        break;
      case KEYED_THRESHOLD:
        rules.add(new AtLeastTwoActionsValidationRule());
        rules.add(new AtLeastOneThresholdValidationRule());
      default:
        break;
      }
    }
    return rules;
  }

  public void validateRules() {
    for (ValidationRule rule : rules) {
      rule.validate(aTestCase);
    }
  }

}
