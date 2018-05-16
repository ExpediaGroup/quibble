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

package com.hotels.hde.quibble.connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

import com.hotels.hde.quibble.Action;
import com.hotels.hde.quibble.Platforms;
import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.exceptions.MissingConnectionDetailsException;
import com.hotels.hde.quibble.validation.ConnectionDetailsValidator;
import com.hotels.hde.quibble.validation.UniqueConnectionSettingsValidator;

public class YamlConnectionLoader implements ConnectionLoader {

  private final String configPath;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final String[] fileExtensions = new String[] { "yaml", "yml" };
  private final static boolean RECURSIVE_LOOKUP = false;

  private final Constructor constructor = new Constructor(ConnectionDetails.class);
  private final Yaml yaml = new Yaml(constructor);

  private enum Loader {
    CONNECTION_FILE_NAME("connections"),
    SHELL_PLATFORM(Platforms.SHELL.getValue());
    private String value;

    Loader(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  public YamlConnectionLoader(String configPath) {
    this.configPath = configPath;
  }

  @Override
  public Set<ConnectionDetails> getConnectionDetailsList(List<TestCase[]> tests) throws IOException {

    Set<ConnectionDetails> allConnectionDetails = new HashSet<>();

    Set<String> requiredConnectionNames = listDistinctRequiredConnections(tests);
    File connectionFile = findConnectionFile(configPath);

    if (connectionFile != null) {
      try (InputStream input = new FileInputStream(connectionFile)) {
        logger.info("Loading connection config file: {}", connectionFile.getName());
        ConnectionDetailsValidator validator = new ConnectionDetailsValidator();

        for (Object data : yaml.loadAll(input)) {
          ConnectionDetails aConnection = (ConnectionDetails) data;

          if (!StringUtils.isEmpty(aConnection.getPlatform())
              && requiredConnectionNames.contains(aConnection.getPlatform())) {
            validator.validate(aConnection);
            allConnectionDetails.add(aConnection);
          }
        }
      } catch (YAMLException e) {
        logger.error("Aborting as Yaml connection configuration file "
            + connectionFile.getName()
            + " does not appear to be valid YAML, please check for indentations or special characters");
        throw e;
      }
    } else {
      throw new FileNotFoundException("Could not find connections configuration file: " + connectionFile.getName());
    }

    requiredConnectionNames.remove(Loader.SHELL_PLATFORM.getValue());
    if (!allRequiredConnectionsPresent(allConnectionDetails, requiredConnectionNames)) {
      throw new MissingConnectionDetailsException("Missing connection details from connection configuration file");
    }
    new UniqueConnectionSettingsValidator().validate(allConnectionDetails);
    return allConnectionDetails;
  }

  @VisibleForTesting
  Set<String> listDistinctRequiredConnections(List<TestCase[]> tests) {
    Set<String> allDistinctPlatforms = new HashSet<>();

    final int DATA_PROVIDER_INDEX = 0;

    for (TestCase[] i : tests) {
      TestCase aTestCase = i[DATA_PROVIDER_INDEX];
      List<Action> testActions = aTestCase.getActions();
      for (Action action : testActions) {
        allDistinctPlatforms.add(action.getPlatform());
      }
    }
    return allDistinctPlatforms;
  }

  @VisibleForTesting
  File findConnectionFile(String configPath) {
    File connectionFile = null;
    List<File> files = (List<File>) FileUtils.listFiles(new File(configPath), fileExtensions, RECURSIVE_LOOKUP);
    if (!files.isEmpty()) {
      for (File aFile : files) {
        if (FilenameUtils.getBaseName(aFile.getName()).equals(Loader.CONNECTION_FILE_NAME.getValue())) {
          connectionFile = aFile;
        }
      }
    }
    return connectionFile;
  }

  @VisibleForTesting
  boolean allRequiredConnectionsPresent(
      Set<ConnectionDetails> allConnectionDetails,
      Set<String> requiredConnectionNames) {

    Set<String> obtainedConnectionList = new HashSet<>();
    for (ConnectionDetails con : allConnectionDetails) {
      obtainedConnectionList.add(con.getPlatform());
    }

    Set<String> undefinedConnections = Sets.difference(requiredConnectionNames, obtainedConnectionList);
    for (String connectionName : undefinedConnections) {
      logger.info("Details of a required connection {} could not be found in connections configuration file",
          connectionName);
    }
    boolean allRequiredConnectionsPresent = undefinedConnections.isEmpty();
    return allRequiredConnectionsPresent;
  }

}
