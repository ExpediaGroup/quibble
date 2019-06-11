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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import com.hotels.hde.quibble.validation.TestCaseValidator;
import com.hotels.hde.quibble.validation.UniqueTestNameValidator;

class YamlTestLoader {

  private final String testPath;
  private final String testName;
  private final String checkDuplicateTestNames;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final String[] fileExtensions = new String[] { "yaml", "yml" };
  private final Constructor constructor = new Constructor(TestCase.class);
  private final Yaml yamlTestCase = new Yaml(constructor);

  YamlTestLoader(final String testPath, final String testName, final String checkDuplicateTestNames) {
    this.testPath = testPath;
    this.testName = testName;
    this.checkDuplicateTestNames = checkDuplicateTestNames;
  }

  private String getCheckDuplicateTestNames() {
    return checkDuplicateTestNames;
  }

  @VisibleForTesting
  public List<TestCase[]> getTestsFromFile(File file, String testName) throws IOException {
    List<TestCase[]> tests = new ArrayList<>();
    VariableSubstitutor substitution = new VariableSubstitutor();
    logger.info("Loading file: {}", file.getName());
    try (InputStream input = new FileInputStream(file)) {
      for (Object data : yamlTestCase.loadAll(input)) {
        TestCase aTestCase = (TestCase) data;
        if (!Strings.isNullOrEmpty(testName)) {
          if (testName.trim().equals(aTestCase.getTestName())) {
            aTestCase.validateWith(new TestCaseValidator(aTestCase));
            substitution.substituteVariables(aTestCase.getActions());
            tests.add(new TestCase[] { aTestCase });
          }
        } else {
          aTestCase.validateWith(new TestCaseValidator(aTestCase));
          substitution.substituteVariables(aTestCase.getActions());
          tests.add(new TestCase[] { aTestCase });
        }
      }
    } catch (YAMLException e) {
      logger.error("Aborting as Yaml file "
          + file.getName()
          + " does not look good, please check for indentations or special characters");
      logger.error(e.getMessage());
      throw e;
    }
    return tests;
  }

  @VisibleForTesting
  public List<TestCase[]> loadTests() throws IOException {
    File file = new File(testPath);
    boolean RECURSIVE_LOOKUP = false;

    List<TestCase[]> tests = new ArrayList<>();

    if (!Strings.isNullOrEmpty(testName)) {
      logger.info("Named tests to run is: " + testName);
    }

    if (file.exists()) {
      logger.info("Loading tests from path: " + file.getName());
      if (file.isFile() && FilenameUtils.isExtension(file.getName(), fileExtensions)) {
        tests = getTestsFromFile(file, testName);
      } else {
        List<File> files = (List<File>) FileUtils.listFiles(file, fileExtensions, RECURSIVE_LOOKUP);
        for (File aFile : files) {
          tests.addAll(getTestsFromFile(aFile, testName));
        }
      }
    } else {
      throw new FileNotFoundException(testPath + " path not found");
    }

    logger.info("Checking for duplicate test names is set to: " + getCheckDuplicateTestNames());
    if (!Strings.isNullOrEmpty(getCheckDuplicateTestNames())) {
      if (getCheckDuplicateTestNames().equalsIgnoreCase("true")) {
        new UniqueTestNameValidator().validate(tests);
      }
    }
    logger.info("Total number of tests " + tests.size());
    return tests;
  }

}
