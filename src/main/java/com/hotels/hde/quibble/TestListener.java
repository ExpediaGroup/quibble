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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class TestListener extends TestListenerAdapter {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void onTestStart(ITestResult tr) {
    logger.info("<<Test " + tr.getName() + " Started....>>");
  }

  @Override
  public void onTestSuccess(ITestResult tr) {
    logger.info("<<Test '" + tr.getName() + "' PASSED>>");
  }

  @Override
  public void onTestFailure(ITestResult tr) {
    logger.info("<<Test '" + tr.getName() + "' FAILED>>");
  }

  @Override
  public void onTestSkipped(ITestResult tr) {
    logger.info("<<Test '" + tr.getName() + "' SKIPPED>>");
    if (tr.getThrowable() != null) {
      logger.info(tr.getThrowable().getMessage());
    }
  }

}
