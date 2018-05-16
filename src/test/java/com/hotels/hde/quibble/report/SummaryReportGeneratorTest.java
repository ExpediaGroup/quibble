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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

import com.hotels.hde.quibble.Platforms;
import com.hotels.hde.quibble.SummaryFile;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.TestUtils;
import com.hotels.hde.quibble.config.QuibbleConfiguration;
import com.hotels.hde.quibble.metrics.MetricNameGenerator;

@RunWith(MockitoJUnitRunner.class)
public class SummaryReportGeneratorTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock
  private QuibbleConfiguration quibbleConfiguration;
  @Mock
  private MetricRegistry metricRegistry;
  @Mock
  private MetricNameGenerator metricNameGenerator;
  @Mock
  private Counter counter;

  @Test
  public void testaddRowPairToReport() throws IOException {

    ITestResult iTestResult = spy(ITestResult.class);
    Set<ITestResult> iTestResultSet = new HashSet<>();

    ITestContext iTestContext = spy(ITestContext.class);
    ITestNGMethod iTestNGMethod = mock(ITestNGMethod.class);
    SummaryFile summaryFile = mock(SummaryFile.class);

    IResultMap iResultMap = spy(IResultMap.class);
    when(iTestResult.getName()).thenReturn("Test 101");
    when(iTestResult.getTestName()).thenReturn("Test 101");
    when(iTestResult.getHost()).thenReturn("hive");
    when(iTestResult.getTestContext()).thenReturn(iTestContext);
    Mockito.doNothing().when(summaryFile).close();

    TestCase adataMatchTest = TestUtils.createASingleNumberMatchTest("Test 101", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    when(iTestResult.getTestContext().getAttribute("Test 101")).thenReturn(adataMatchTest);

    Mockito.doNothing().when(iResultMap).addResult(iTestResult, iTestNGMethod);
    iTestResultSet.add(iTestResult);
    Throwable throwable = mock(Throwable.class);
    when(iTestResult.getThrowable()).thenReturn(throwable);
    when(iTestResult.getThrowable().getMessage()).thenReturn(null);
    when(iResultMap.getAllResults()).thenReturn(iTestResultSet);

    when(iTestContext.getPassedTests()).thenReturn(iResultMap);
    when(iTestContext.getFailedTests()).thenReturn(iResultMap);
    when(iTestContext.getSkippedTests()).thenReturn(iResultMap);

    when(iTestContext.getPassedTests().size()).thenReturn(10);
    when(iTestContext.getFailedTests().size()).thenReturn(10);
    when(iTestContext.getSkippedTests().size()).thenReturn(10);
    when(iResultMap.size()).thenReturn(10);

    ISuite iSuite = mock(ISuite.class);
    when(iSuite.getName()).thenReturn("testsuite");

    Map<String, ISuiteResult> tests = new HashMap<>();
    ISuiteResult iSuiteResult = mock(ISuiteResult.class);
    when(iSuiteResult.getPropertyFileName()).thenReturn("isuitetext");

    when(iSuiteResult.getTestContext()).thenReturn(iTestContext);
    tests.put("hello", iSuiteResult);

    when(iSuite.getResults()).thenReturn(tests);
    List<XmlSuite> xmlSuiteList = new ArrayList<>();
    List<ISuite> iSuiteList = new ArrayList<>();
    iSuiteList.add(iSuite);

    when(iSuiteResult.getTestContext().getSkippedTests().size()).thenReturn(10);
    when(iSuiteResult.getTestContext().getFailedTests().size()).thenReturn(10);
    when(iSuiteResult.getTestContext().getPassedTests().size()).thenReturn(10);
    when(iTestContext.getFailedConfigurations()).thenReturn(iResultMap);
    when(iTestContext.getSkippedConfigurations()).thenReturn(iResultMap);

    when(iResultMap.size()).thenReturn(1);

    when(quibbleConfiguration.getDataQualityName()).thenReturn("Test 101");
    when(metricNameGenerator.name()).thenReturn("metric");
    when(counter.getCount()).thenReturn(10L);
    when(metricRegistry.counter(null)).thenReturn(counter);

    File outputFolder = tempFolder.newFolder("reportpath");

    SummaryReportGenerator summaryReportGenerator = new SummaryReportGenerator(quibbleConfiguration,
        metricRegistry, metricNameGenerator, outputFolder.getAbsolutePath()) {

      @Override
      void updateMetrics(final int passedTests, final int failedTests, final int skippedTests) {
        assertTrue(true);
      }

      @Override
      void writeLine(String line) {
        assertTrue(true);
      }
    };

    summaryReportGenerator.generateReport(xmlSuiteList, iSuiteList, outputFolder.getAbsolutePath());
    File[] files = new File(outputFolder.getAbsolutePath()).listFiles();
    assertTrue(files[0].exists());
    assertThat(iResultMap.size(), is(1));
}
}
