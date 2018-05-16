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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.TemporaryFolder;
import org.yaml.snakeyaml.Yaml;

public class YamlTestLoaderTest {

  @Rule
  public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void getNamedTestsFromFile() throws Exception {
    String REQUIRED_TEST_NAME = "Test 101";
    int TEST_COUNT_WITH_REQUIRED_NAME = 2;
    String testPath = ".";
    String checkDuplicateTestNames = "false";

    YamlTestLoader testLoader = new YamlTestLoader(testPath, REQUIRED_TEST_NAME, checkDuplicateTestNames);

    TestCase aCountMatchTest = TestUtils.createASingleNumberMatchTest("Test 101", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    TestCase aDataMatchTest = TestUtils.createADataMatchTest("Test 101", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    TestCase anotherCountMatchTest = TestUtils.createASingleNumberMatchTest("Test 102", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    TestCase aZeroCountTest = TestUtils.createADataMatchTest("Test 103", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());

    File firstYamlFile = folder.newFile("First.yaml");
    FileWriter writer = new FileWriter(firstYamlFile);

    List<TestCase> testsToBeWritten = new ArrayList<>();
    testsToBeWritten.add(aCountMatchTest);
    testsToBeWritten.add(anotherCountMatchTest);
    testsToBeWritten.add(aDataMatchTest);
    testsToBeWritten.add(aZeroCountTest);

    Yaml yaml = new Yaml();
    yaml.dumpAll(testsToBeWritten.iterator(), writer);

    List<TestCase[]> allLoadedTests = testLoader.getTestsFromFile(firstYamlFile, REQUIRED_TEST_NAME);
    assertTrue(allLoadedTests.size() == TEST_COUNT_WITH_REQUIRED_NAME);
  }

  @Test
  public void getNonExistingNamedTestsFromFile() throws Exception {
    String REQUIRED_TEST_NAME = "Test 100";
    int TEST_COUNT_WITH_REQUIRED_NAME = 0;

    String testPath = ".";
    String checkDuplicateTestNames = "false";

    YamlTestLoader testLoader = new YamlTestLoader(testPath, REQUIRED_TEST_NAME, checkDuplicateTestNames);

    TestCase aCountMatchTest = TestUtils.createASingleNumberMatchTest("Test 101", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    TestCase aDataMatchTest = TestUtils.createADataMatchTest("Test 102", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    TestCase anotherCountMatchTest = TestUtils.createASingleNumberMatchTest("Test 103", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    TestCase aZeroCountTest = TestUtils.createADataMatchTest("Test 104", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());

    File firstYamlFile = folder.newFile("First.yaml");
    FileWriter writer = new FileWriter(firstYamlFile);

    List<TestCase> testsToBeWritten = new ArrayList<>();
    testsToBeWritten.add(aCountMatchTest);
    testsToBeWritten.add(anotherCountMatchTest);
    testsToBeWritten.add(aDataMatchTest);
    testsToBeWritten.add(aZeroCountTest);

    Yaml yaml = new Yaml();
    yaml.dumpAll(testsToBeWritten.iterator(), writer);

    List<TestCase[]> allLoadedTests = testLoader.getTestsFromFile(firstYamlFile, REQUIRED_TEST_NAME);

    assertTrue(allLoadedTests.size() == TEST_COUNT_WITH_REQUIRED_NAME);
  }

  @Test
  public void getAllTestsFromFile() throws Exception {
    String REQUIRED_TEST_NAME = "";
    String testPath = ".";
    String checkDuplicateTestNames = "false";

    YamlTestLoader testLoader = new YamlTestLoader(testPath, REQUIRED_TEST_NAME, checkDuplicateTestNames);

    TestCase aCountMatchTest = TestUtils.createASingleNumberMatchTest("Test 101", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    TestCase aDataMatchTest = TestUtils.createADataMatchTest("Test 102", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    TestCase anotherCountMatchTest = TestUtils.createASingleNumberMatchTest("Test 103", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    TestCase aZeroCountTest = TestUtils.createADataMatchTest("Test 104", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());

    File firstYamlFile = folder.newFile("First.yaml");
    FileWriter writer = new FileWriter(firstYamlFile);

    List<TestCase> testsToBeWritten = new ArrayList<>();
    testsToBeWritten.add(aCountMatchTest);
    testsToBeWritten.add(anotherCountMatchTest);
    testsToBeWritten.add(aDataMatchTest);
    testsToBeWritten.add(aZeroCountTest);

    Yaml yaml = new Yaml();
    yaml.dumpAll(testsToBeWritten.iterator(), writer);
    List<TestCase[]> allLoadedTests = testLoader.getTestsFromFile(firstYamlFile, REQUIRED_TEST_NAME);

    assertTrue(allLoadedTests.size() == testsToBeWritten.size());
  }

  @Test
  public void getTestsFromFolder() throws Exception {
    String testPath = folder.getRoot().getAbsolutePath();
    String checkDuplicateTestNames = "false";
    String REQUIRED_TEST_NAME = "";

    YamlTestLoader testLoader = new YamlTestLoader(testPath, REQUIRED_TEST_NAME, checkDuplicateTestNames);

    TestCase aCountMatchTest = TestUtils.createASingleNumberMatchTest("Test 101", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    TestCase aDataMatchTest = TestUtils.createADataMatchTest("Test 102", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    TestCase anotherCountMatchTest = TestUtils.createASingleNumberMatchTest("Test 103", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    TestCase aZeroCountTest = TestUtils.createAZeroNumberMatchTest("Test 104", Platforms.HIVE.getValue());

    File firstYamlFile = folder.newFile("First.yaml");
    File secondYamlFile = folder.newFile("Second.yml");
    File thirdYamlFile = folder.newFile("Third.yaml");
    FileWriter firstFileWriter = new FileWriter(firstYamlFile);
    FileWriter secondFileWriter = new FileWriter(secondYamlFile);
    FileWriter thirdFileWriter = new FileWriter(thirdYamlFile);

    List<TestCase> testsForFirstFile = new ArrayList<>();
    testsForFirstFile.add(aCountMatchTest);
    testsForFirstFile.add(aDataMatchTest);

    List<TestCase> testsForSecondFile = new ArrayList<>();
    testsForSecondFile.add(anotherCountMatchTest);
    testsForSecondFile.add(aDataMatchTest);

    List<TestCase> testsForThirdFile = new ArrayList<>();
    testsForThirdFile.add(aZeroCountTest);
    testsForThirdFile.add(aDataMatchTest);

    Yaml yaml = new Yaml();
    yaml.dumpAll(testsForFirstFile.iterator(), firstFileWriter);
    yaml.dumpAll(testsForSecondFile.iterator(), secondFileWriter);
    yaml.dumpAll(testsForThirdFile.iterator(), thirdFileWriter);

    List<TestCase[]> allLoadedTests = testLoader.loadTests();

    assertTrue(
        allLoadedTests.size() == testsForFirstFile.size() + testsForSecondFile.size() + testsForThirdFile.size());
  }

  @Test
  public void getNamedTestsFromFolder() throws Exception {
    String REQUIRED_TEST_NAME = "Test 102";
    int TEST_COUNT_WITH_REQUIRED_NAME = 3;
    String checkDuplicateTestNames = "false";
    String testPath = folder.getRoot().getAbsolutePath();

    environmentVariables.set("TestName", "REQUIRED_TEST_NAME");
    environmentVariables.set("CheckDuplicateTestNames", checkDuplicateTestNames);

    YamlTestLoader testLoader = new YamlTestLoader(testPath, REQUIRED_TEST_NAME, checkDuplicateTestNames);

    TestCase aCountMatchTest = TestUtils.createASingleNumberMatchTest("Test 101", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    TestCase aDataMatchTest = TestUtils.createADataMatchTest("Test 102", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    TestCase anotherCountMatchTest = TestUtils.createASingleNumberMatchTest("Test 103", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    TestCase aZeroCountTest = TestUtils.createADataMatchTest("Test 104", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());

    File firstYamlFile = folder.newFile("First.yaml");
    File secondYamlFile = folder.newFile("Second.yml");
    File thirdYamlFile = folder.newFile("Third.yaml");
    FileWriter firstFileWriter = new FileWriter(firstYamlFile);
    FileWriter secondFileWriter = new FileWriter(secondYamlFile);
    FileWriter thirdFileWriter = new FileWriter(thirdYamlFile);

    List<TestCase> testsForFirstFile = new ArrayList<>();
    testsForFirstFile.add(aCountMatchTest);
    testsForFirstFile.add(aDataMatchTest);

    List<TestCase> testsForSecondFile = new ArrayList<>();
    testsForSecondFile.add(anotherCountMatchTest);
    testsForSecondFile.add(aDataMatchTest);

    List<TestCase> testsForThirdFile = new ArrayList<>();
    testsForThirdFile.add(aZeroCountTest);
    testsForThirdFile.add(aDataMatchTest);

    Yaml yaml = new Yaml();
    yaml.dumpAll(testsForFirstFile.iterator(), firstFileWriter);
    yaml.dumpAll(testsForSecondFile.iterator(), secondFileWriter);
    yaml.dumpAll(testsForThirdFile.iterator(), thirdFileWriter);

    List<TestCase[]> allLoadedTests = testLoader.loadTests();

    assertTrue(allLoadedTests.size() == TEST_COUNT_WITH_REQUIRED_NAME);
  }

  @Test(expected = NullPointerException.class)
  public void checkNonExistingTestPath() throws Exception {
    String REQUIRED_TEST_NAME = "";
    String checkDuplicateTestNames = "false";

    YamlTestLoader testLoader = new YamlTestLoader(null, REQUIRED_TEST_NAME, checkDuplicateTestNames);
    testLoader.loadTests();
  }

  @Test
  public void checkEmptyTestPath() throws Exception {
    String REQUIRED_TEST_NAME = "";
    String checkDuplicateTestNames = "false";
    String testPath = ".";

    YamlTestLoader testLoader = new YamlTestLoader(testPath, REQUIRED_TEST_NAME, checkDuplicateTestNames);
    List<TestCase[]> allLoadedTests = testLoader.loadTests();

    assertThat(allLoadedTests.size(), is(0));
  }
}
