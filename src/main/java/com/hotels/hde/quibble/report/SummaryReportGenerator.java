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
package com.hotels.hde.quibble.report;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import com.hotels.hde.quibble.SummaryFile;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.config.QuibbleConfiguration;
import com.hotels.hde.quibble.metrics.MetricNameGenerator;

public class SummaryReportGenerator implements IReporter {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private SummaryFile summaryFile;

  private final MetricRegistry metricRegistry;
  private final MetricNameGenerator metricNameGenerator;
  private final QuibbleConfiguration quibbleConfig;
  private final String reportPath;

  private final Counter passedTestsMetric;
  private final Counter failedTestsMetric;
  private final Counter skippedTestsMetric;

  @Inject
  public SummaryReportGenerator(
      final QuibbleConfiguration quibbleConfig,
      final MetricRegistry metricRegistry,
      final MetricNameGenerator metricNameGenerator,
      @Named("report.path") final String reportPath) {
    this.quibbleConfig = checkNotNull(quibbleConfig);
    this.metricRegistry = checkNotNull(metricRegistry);
    this.metricNameGenerator = checkNotNull(metricNameGenerator);
    this.reportPath = checkNotNull(reportPath);

    passedTestsMetric = this.metricRegistry.counter(metricNameGenerator.name("passed-tests"));
    failedTestsMetric = this.metricRegistry.counter(metricNameGenerator.name("failed-tests"));
    skippedTestsMetric = this.metricRegistry.counter(metricNameGenerator.name("skipped-tests"));
  }

  @Override
  public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    try {
      summaryFile = new SummaryFile.Builder().reportPath(reportPath).build();
    } catch (IOException e) {
      throw new RuntimeException(e); // replace with UncheckedIOException when we move to Java 8
    }

    ISuite suite = suites.get(0);

    Map<String, ISuiteResult> tests = suite.getResults();

    Collection<ISuiteResult> suiteResults = tests.values();
    ISuiteResult suiteResult = suiteResults.iterator().next();
    ITestContext testContext = suiteResult.getTestContext();

    IResultMap failedTests = testContext.getFailedTests();
    IResultMap passedTests = testContext.getPassedTests();
    IResultMap skippedTests = testContext.getSkippedTests();

    // update metrics
    updateMetrics(passedTests.size(), failedTests.size(), skippedTests.size());

    IResultMap failedConfigTests = testContext.getFailedConfigurations();
    IResultMap skippedConfigTests = testContext.getSkippedConfigurations();

    int failedConfigTestsCount = failedConfigTests.size();
    int skippedConfigTestsCount = skippedConfigTests.size();

    if (failedTests.size() > 0) {
      printFailedTestsReport(failedTests);
    }
    if (passedTests.size() > 0) {
      printPassedTestsReport(passedTests);
    }

    if (skippedTests.size() > 0) {
      printSkippedTestsReport(skippedTests);
    }

    if (failedConfigTestsCount > 0) {
      printFailedConfigTestsReport(failedConfigTests);
    }
    if (skippedConfigTestsCount > 0) {
      printSkippedConfigTestsReport(skippedConfigTests);
    }

    int totalTests = passedTests.size() + failedTests.size() + skippedTests.size();
    printFinalSummary(totalTests, failedTests.size(), failedConfigTestsCount, skippedConfigTestsCount,
        skippedTests.size());

    summaryFile.close();
  }

  private void printSkippedConfigTestsReport(IResultMap skippedConfigTests) {
    Set<ITestResult> skippedConfigTestsSet = skippedConfigTests.getAllResults();

    writeLine(System.lineSeparator() + "---SKIPPED DUE TO CONFIGURATIONS---");

    for (ITestResult testResult : skippedConfigTestsSet) {
      writeLine("Test Name: " + testResult.getName());// Don't know why but this does not return actual test name
      if (testResult.getThrowable() != null) {
        String message = testResult.getThrowable().getMessage();
        writeLine("\t Reason: " + message);
      } else {
        writeLine("\t Reason: Skipped due to configuration issues");
      }
      writeLine("");
    }
  }

  private void printFailedConfigTestsReport(IResultMap failedConfigTests) {
    Set<ITestResult> failConfigTestsSet = failedConfigTests.getAllResults();

    writeLine(System.lineSeparator() + "---CONFIGURATIONS FAILURES---");
    for (ITestResult testResult : failConfigTestsSet) {
      String testName = testResult.getName();
      String testDesc = getTestDesc(testResult, testName);
      writeLine("Test Name: " + testName);
      writeLine("Test Description: " + testDesc);

      if (testResult.getThrowable() != null) {
        String message = testResult.getThrowable().getMessage();
        writeLine("\t Reason: Failed Configuration due to: " + message);
      } else {
        writeLine("\t Reason: Failed Configuration");
      }
      writeLine("");
    }
  }

  private void printSkippedTestsReport(IResultMap skippedTests) {
    Set<ITestResult> skippedTestsSet = skippedTests.getAllResults();
    writeLine("---SKIPPED TESTS---");
    for (ITestResult testResult : skippedTestsSet) {
      String testName = testResult.getName();
      String testDesc = getTestDesc(testResult, testName);
      writeLine("Test Name: " + testName);
      writeLine("Test Description: " + testDesc);

      if (testResult.getThrowable() != null) {
        String message = testResult.getThrowable().getMessage();
        writeLine("\t Reason: Skipped due to: " + message);
      }
      writeLine("");
    }
  }

  private void printPassedTestsReport(IResultMap passedTests) {
    Set<ITestResult> passedTestsSet = passedTests.getAllResults();
    writeLine(System.lineSeparator() + "---PASSED TESTS---");
    for (ITestResult testResult : passedTestsSet) {
      String testName = testResult.getName();
      String testDesc = getTestDesc(testResult, testName);
      String metricName = metricNameGenerator.name("tests", testName);
      Counter counter = metricRegistry.counter(metricName);
      counter.inc();
      writeLine("Test Name: " + testName + " (metric: " + metricName + ")");
      writeLine("Test Description: " + testDesc);
      writeLine("Time taken (in milliseconds): " + (testResult.getEndMillis() - testResult.getStartMillis()));
      writeLine("");
    }
  }

  private void printFailedTestsReport(IResultMap failedTests) {
    Set<ITestResult> failedTestsSet = failedTests.getAllResults();
    writeLine(System.lineSeparator() + "---FAILED TESTS---");
    CharSequence removeString = "expected [true] but found [false]";
    for (ITestResult testResult : failedTestsSet) {
      String testName = testResult.getName();
      String testDesc = getTestDesc(testResult, testName);
      String metricName = metricNameGenerator.name("tests", testName);
      Counter counter = metricRegistry.counter(metricName);
      counter.dec();

      writeLine("Test Name: " + testName + " (metric: " + metricName + ")");
      writeLine("Test Description: " + testDesc);

      if (testResult.getThrowable().getMessage() != null) {
        String failureMessage = testResult.getThrowable().getMessage().replace(removeString, "");
        writeLine("\t Reason: " + failureMessage);
      } else {
        writeLine("\t Reason: Failed due to unknown reason");
      }
      writeLine("");
    }
  }

  @VisibleForTesting
  void updateMetrics(final int passedTests, final int failedTests, final int skippedTests) {
    passedTestsMetric.inc(passedTests);
    failedTestsMetric.inc(failedTests);
    skippedTestsMetric.inc(skippedTests);
  }

  private void printFinalSummary(
      final int totalTests,
      final int totalFailedTests,
      final int totalFailedConfgTests,
      final int totalSkippedConfigTests,
      final int totalSkippedTests) {
    writeLine("");
    writeLine("------------------------------------------------------------------------------------");
    writeLine(String.format("Total tests run: %s, Failures: %s, Config Failures: %s, Skipped Configs: %s, Skips: %s",
        totalTests, totalFailedTests, totalFailedConfgTests, totalSkippedConfigTests, totalSkippedTests));
    writeLine("------------------------------------------------------------------------------------");
  }

  @VisibleForTesting
  void writeLine(String line) {
    logger.info(line);
    try {
      summaryFile.writeLine(line);
    } catch (IOException e) {
      throw new RuntimeException(e); // replace with UncheckedIOException when we move to Java 8
    }
  }

  private String getTestDesc(ITestResult testResult, String testName) {
    String DEFAULT_TEST_DESCRIPTION = "";
    TestCase aTestCase = (TestCase) testResult.getTestContext().getAttribute(testName);
    if (aTestCase != null) {
      return aTestCase.getTestDescription();
    }
    return DEFAULT_TEST_DESCRIPTION;
  }

}
