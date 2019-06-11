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
package com.hotels.hde.quibble.connection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.yaml.snakeyaml.Yaml;

import com.hotels.hde.quibble.Platforms;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.TestUtils;
import com.hotels.hde.quibble.exceptions.MissingConnectionDetailsException;

public class YamlConnectionLoaderTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  private String configPath;

  @Before
  public void setup() {
    configPath = folder.getRoot().getAbsolutePath();
  }

  @Test
  public void findConnectionFileSuccess() throws Exception {
    File dummyConnectionFile = folder.newFile("connections.yaml");

    YamlConnectionLoader connectionLoader = new YamlConnectionLoader(configPath);
    File connectionFile = connectionLoader.findConnectionFile(configPath);

    assertThat(connectionFile.getName(), is(dummyConnectionFile.getName()));
  }

  @Test
  public void findConnectionFileFailure() throws Exception {
    folder.newFile("SomeOtherFile.yaml");

    YamlConnectionLoader connectionLoader = new YamlConnectionLoader(configPath);
    File connectionFile = connectionLoader.findConnectionFile(configPath);
    assertThat(connectionFile, is(nullValue()));
  }

  @Test
  public void testAllRequiredConnectionsPresentSuccess() {
    ConnectionDetails hiveConnectionDetails = new ConnectionDetails(Platforms.HIVE.getValue(), "some.url", "user",
        "password", "driver");
    ConnectionDetails db2ConnectionDetails = new ConnectionDetails(Platforms.DB2.getValue(), "some.url", "user",
        "password", "driver");
    ConnectionDetails teradataConnectionDetails = new ConnectionDetails(Platforms.TERADATA.getValue(), "some.url",
        "user", "password", "driver");
    Set<ConnectionDetails> allConnectionDetails = new HashSet<>();
    allConnectionDetails.add(hiveConnectionDetails);
    allConnectionDetails.add(db2ConnectionDetails);
    allConnectionDetails.add(teradataConnectionDetails);

    Set<String> requiredConnectionNames = new HashSet<>();
    requiredConnectionNames.add(Platforms.HIVE.getValue());
    requiredConnectionNames.add(Platforms.TERADATA.getValue());
    requiredConnectionNames.add(Platforms.DB2.getValue());

    YamlConnectionLoader connectionLoader = new YamlConnectionLoader(configPath);
    boolean verdict = connectionLoader.allRequiredConnectionsPresent(allConnectionDetails, requiredConnectionNames);
    assertTrue(verdict);
  }

  @Test
  public void testAllRequiredConnectionsPresentFailure() {
    ConnectionDetails hiveConnectionDetails = new ConnectionDetails(Platforms.HIVE.getValue(), "some.url", "user",
        "password", "driver");
    ConnectionDetails teradataConnectionDetails = new ConnectionDetails(Platforms.TERADATA.getValue(), "some.url",
        "user", "password", "driver");
    Set<ConnectionDetails> allConnectionDetails = new HashSet<>();
    allConnectionDetails.add(hiveConnectionDetails);
    allConnectionDetails.add(teradataConnectionDetails);

    Set<String> requiredConnectionNames = new HashSet<>();
    requiredConnectionNames.add(Platforms.HIVE.getValue());
    requiredConnectionNames.add(Platforms.TERADATA.getValue());
    requiredConnectionNames.add(Platforms.DB2.getValue());

    YamlConnectionLoader connectionLoader = new YamlConnectionLoader(configPath);
    boolean verdict = connectionLoader.allRequiredConnectionsPresent(allConnectionDetails, requiredConnectionNames);
    assertFalse(verdict);
  }

  @Test
  public void listDistinctRequiredConnectionsTest() {
    TestCase test1 = TestUtils.createASingleNumberMatchTest("Test 101", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    TestCase test2 = TestUtils.createASingleNumberMatchTest("Test 102", Platforms.DB2.getValue(),
        Platforms.TERADATA.getValue());
    TestCase test3 = TestUtils.createASingleNumberMatchTest("Test 103", "sqlserver", Platforms.HIVE.getValue());

    List<TestCase[]> tests = new ArrayList<>();
    tests.add(new TestCase[] { test1 });
    tests.add(new TestCase[] { test2 });
    tests.add(new TestCase[] { test3 });

    YamlConnectionLoader connectionLoader = new YamlConnectionLoader(configPath);
    Set<String> allDistinctPlatforms = connectionLoader.listDistinctRequiredConnections(tests);

    assertThat(allDistinctPlatforms.size(), is(4));
    assertTrue(allDistinctPlatforms.contains(Platforms.HIVE.getValue()));
    assertTrue(allDistinctPlatforms.contains(Platforms.TERADATA.getValue()));
    assertTrue(allDistinctPlatforms.contains(Platforms.DB2.getValue()));
    assertTrue(allDistinctPlatforms.contains(Platforms.SQL.getValue()));
  }

  @Test
  public void getConnectionDetailsListSuccess() throws Exception {
    TestCase test1 = TestUtils.createASingleNumberMatchTest("Test 101", Platforms.HIVE.getValue(),
        Platforms.SHELL.getValue());
    TestCase test2 = TestUtils.createASingleNumberMatchTest("Test 102", Platforms.DB2.getValue(),
        Platforms.TERADATA.getValue());
    TestCase test3 = TestUtils.createASingleNumberMatchTest("Test 103", Platforms.SQL.getValue(),
        Platforms.HIVE.getValue());

    List<TestCase[]> tests = new ArrayList<>();
    tests.add(new TestCase[] { test1 });
    tests.add(new TestCase[] { test2 });
    tests.add(new TestCase[] { test3 });

    ConnectionDetails hiveConnection = new ConnectionDetails(Platforms.HIVE.getValue(), "some.url", "user", "password",
        "driver");
    ConnectionDetails db2Connection = new ConnectionDetails(Platforms.DB2.getValue(), "some.url", "user", "password",
        "driver");
    ConnectionDetails teradataConnection = new ConnectionDetails(Platforms.TERADATA.getValue(), "some.url", "user",
        "password", "driver");
    ConnectionDetails sqlserverConnection = new ConnectionDetails(Platforms.SQL.getValue(), "some.url", "user",
        "password", "driver");

    List<ConnectionDetails> connectionsForFile = new ArrayList<>();
    connectionsForFile.add(hiveConnection);
    connectionsForFile.add(db2Connection);
    connectionsForFile.add(sqlserverConnection);
    connectionsForFile.add(teradataConnection);

    File connectionsFile = folder.newFile("connections.yaml");
    FileWriter fileWriter = new FileWriter(connectionsFile);
    Yaml yaml = new Yaml();
    yaml.dumpAll(connectionsForFile.iterator(), fileWriter);

    YamlConnectionLoader connectionLoader = new YamlConnectionLoader(configPath);
    Set<ConnectionDetails> allConnectionDetails = connectionLoader.getConnectionDetailsList(tests);

    assertThat(allConnectionDetails.size(), is(4));
  }

  @Test(expected = MissingConnectionDetailsException.class)
  public void getConnectionDetailsListFailure() throws Exception {
    TestCase test1 = TestUtils.createASingleNumberMatchTest("Test 101", Platforms.TERADATA.getValue(),
        Platforms.HIVE.getValue());
    TestCase test2 = TestUtils.createASingleNumberMatchTest("Test 102", Platforms.DB2.getValue(),
        Platforms.TERADATA.getValue());
    TestCase test3 = TestUtils.createASingleNumberMatchTest("Test 103", "sqlserver", Platforms.HIVE.getValue());

    List<TestCase[]> tests = new ArrayList<>();
    tests.add(new TestCase[] { test1 });
    tests.add(new TestCase[] { test2 });
    tests.add(new TestCase[] { test3 });

    ConnectionDetails hiveConnection = new ConnectionDetails(Platforms.HIVE.getValue(), "some.url", "user", "password",
        "driver");
    ConnectionDetails db2Connection = new ConnectionDetails(Platforms.DB2.getValue(), "some.url", "user", "password",
        "driver");
    ConnectionDetails sqlserverConnection = new ConnectionDetails("sqlserver", "some.url", "user", "password",
        "driver");

    List<ConnectionDetails> connectionsForFile = new ArrayList<>();
    connectionsForFile.add(hiveConnection);
    connectionsForFile.add(db2Connection);
    connectionsForFile.add(sqlserverConnection);

    Yaml yaml = new Yaml();
    File connectionsFile = folder.newFile("connections.yaml");
    FileWriter fileWriter = new FileWriter(connectionsFile);
    yaml.dumpAll(connectionsForFile.iterator(), fileWriter);
    YamlConnectionLoader connectionLoader = new YamlConnectionLoader(configPath);
    connectionLoader.getConnectionDetailsList(tests);
  }
}
