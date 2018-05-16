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

import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import com.hotels.hde.quibble.connection.ActionConnection;
import com.hotels.hde.quibble.connection.ConnectionManager;
import com.hotels.hde.quibble.executors.BaseTestTypeExecutor;
import com.hotels.hde.quibble.inject.QuibbleModule;

@Guice(modules = QuibbleModule.class)
public class TestSuite implements ITest {

  private final Logger logger = LoggerFactory.getLogger(TestSuite.class);

  private static final int DATA_PROVIDER_INDEX = 0;
  static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

  @Inject
  @Named("report.path")
  private String reportPath;

  @Inject
  @Named("report.diffs")
  private String reportDifferences;

  @Inject
  @Named("test.path")
  private String testPath;

  @Inject
  @Named("test.name")
  private String testNameToRun;

  @Inject
  @Named("check.duplicte.testnames")
  private String checkDuplicateTestNames;

  private String testName = "";

  private List<TestCase[]> allTests = new ArrayList<>();

  @Inject
  private ConnectionManager connectionManager;

  private List<ActionConnection> connectionList;

  @BeforeSuite
  public void beforeSuite() throws IOException {

    try (FileReader reader = new FileReader("quibble.txt")) {
      String result = CharStreams.toString(reader);
      logger.info(result);
    }

    YamlTestLoader yamlTestLoader = new YamlTestLoader(testPath, testNameToRun, checkDuplicateTestNames);
    allTests = yamlTestLoader.loadTests();

    logger.info("{} test(s) going to be executed", allTests.size());

    connectionManager.createRequiredConnectionPools(allTests);
  }

  @AfterSuite
  public void afterSuite() throws IOException {
    connectionManager.closeAllConnectionPools();
  }

  @DataProvider(name = "allTests")
  public Iterator<TestCase[]> getAllQueries() throws IOException {
    return allTests.iterator();
  }

  @BeforeMethod(enabled = true)
  public void beforeTest(Object[] data, ITestContext testContext) throws IOException {

    TestCase aTestCase = (TestCase) data[DATA_PROVIDER_INDEX];

    setTestName(aTestCase.getTestName());
    testContext.setAttribute(aTestCase.getTestName(), aTestCase);
    logger.info("Before-test setup for: " + aTestCase.getTestName());

    connectionList = connectionManager.getRequiredConnections(aTestCase);
  }

  @AfterMethod(alwaysRun = true)
  public void afterTest() {
    connectionManager.returnAllConnections();
  }

  @Test(description = "Quibble Tests", dataProvider = "allTests")
  public void test(TestCase aTestCase) {
    logger.info("Executing test case: " + aTestCase.getTestName());

    BaseTestTypeExecutor testExecutor = getTestExecutorInstance(aTestCase);
    testExecutor.execute(aTestCase, connectionList);
  }

  BaseTestTypeExecutor getTestExecutorInstance(TestCase aTestCase) {
    if (aTestCase.getTestType().equals(TestType.DATAMATCH.getCode())) {
      TestType.setReportDifferences(reportDifferences);
      TestType.setReportPath(reportPath);
    }
    return TestType.get(aTestCase.getTestType()).getExecutor();
  }

  @Override
  public String getTestName() {
    return testName;
  }

  private void setTestName(String testName) {
    this.testName = testName;
  }

  public void setConnectionManager(ConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
  }
}
