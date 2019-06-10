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
import org.testng.TestNG;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

import com.hotels.hde.quibble.config.QuibbleConfiguration;
import com.hotels.hde.quibble.inject.QuibbleModule;
import com.hotels.hde.quibble.metrics.GraphiteShutdownHook;
import com.hotels.hde.quibble.report.SummaryReportGenerator;

public class QuibbleRunner {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private static final int SUCCESS_RETURN_STATUS = 0;
  private static final int ERROR_RETURN_STATUS = 1;

  /** TestNG should continue to execute the remaining tests if one test is skipped due to incorrect specification. */
  private static final String DEFAULT_CONFIG_FAILURE_POLICY = "continue";

  /** So no test-output directory is created. */
  private static final boolean DEFAULT_USE_DEFAULT_LISTENERS = false;

  @Inject
  private QuibbleConfiguration quibbleConfig;

  @Inject
  @Named("test.path")
  private String testPath;

  @Inject
  private SummaryReportGenerator summaryReportGenerator;

  @Inject
  private TestListener testListener;

  @Inject
  private GraphiteShutdownHook graphiteShutdownHook;

  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new QuibbleModule());
    QuibbleRunner quibbleRunner = injector.getInstance(QuibbleRunner.class);
    quibbleRunner.run();
  }

  public void run() {
    TestNG testNG = new TestNG();
    Class<?>[] testClasses = new Class<?>[] { com.hotels.hde.quibble.TestSuite.class };
    testNG.setTestClasses(testClasses);

    testNG.setConfigFailurePolicy(DEFAULT_CONFIG_FAILURE_POLICY);
    testNG.setUseDefaultListeners(DEFAULT_USE_DEFAULT_LISTENERS);
    testNG.addListener(testListener);
    testNG.addListener(summaryReportGenerator);

    Runtime.getRuntime().addShutdownHook(new Thread(graphiteShutdownHook));

    logger.info("-------------------------------------------------------");
    logger.info(" Running Test Suite: " + quibbleConfig.getDataQualityName());
    logger.info("-------------------------------------------------------\n");

    logger.info("Loading test suites from: {}", testPath);

    testNG.run();

    int exitStatus;

    if (testNG.hasFailure() || testNG.hasSkip()) {
      exitStatus = ERROR_RETURN_STATUS;
    } else {
      exitStatus = SUCCESS_RETURN_STATUS;
    }

    logger.info("Final exitStatus is: " + exitStatus);
    System.exit(exitStatus);
  }

}
