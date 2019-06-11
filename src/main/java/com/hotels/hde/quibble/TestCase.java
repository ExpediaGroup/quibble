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
package com.hotels.hde.quibble;

import java.util.Collections;
import java.util.List;

import com.hotels.hde.quibble.validation.TestCaseValidator;

public class TestCase {

  private String testType;
  private String testName;
  private String testDescription;
  private Integer thresholdKeyIndex;
  private Integer thresholdReportIndex;

  private List<Action> actions;
  private List<Threshold> thresholds;

  /** Required by YAML. */
  public TestCase() {}

  public String getTestType() {
    return testType;
  }

  public void setTestType(String testType) {
    this.testType = testType;
  }

  public String getTestName() {
    if (testName != null) {
      return testName.trim();
    }
    return null;
  }

  public void setTestName(String testName) {
    this.testName = testName;
  }

  public String getTestDescription() {
    if (testDescription == null) {
      testDescription = "";
    }
    return testDescription;
  }

  public void setTestDescription(String testDescription) {
    this.testDescription = testDescription;
  }

  public List<Threshold> getThresholds() {
    return thresholds;
  }

  public void setThresholds(List<Threshold> thresholds) {
    this.thresholds = thresholds;
    if (this.thresholds != null) {
      Collections.sort(this.thresholds);
    }
  }

  public List<Action> getActions() {
    return actions;
  }

  public void setActions(List<Action> actions) {
    this.actions = actions;
  }

  public void validateWith(TestCaseValidator validator) {
    validator.validateRules();
  }

  public Integer getThresholdKeyIndex() {
    return thresholdKeyIndex;
  }

  public void setThresholdKeyIndex(Integer thresholdKeyIndex) {
    this.thresholdKeyIndex = thresholdKeyIndex;
  }

  public Integer getThresholdReportIndex() {
    return thresholdReportIndex;
  }

  public void setThresholdReportIndex(Integer thresholdReportIndex) {
    this.thresholdReportIndex = thresholdReportIndex;
  }

}
