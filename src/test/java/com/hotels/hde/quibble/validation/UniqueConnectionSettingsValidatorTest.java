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
package com.hotels.hde.quibble.validation;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.hotels.hde.quibble.Platforms;
import com.hotels.hde.quibble.connection.ConnectionDetails;
import com.hotels.hde.quibble.exceptions.DuplicateConnectionSettingsException;

public class UniqueConnectionSettingsValidatorTest {

  @Test
  public void validateUniqueConnectionPlatformPass() {
    Set<ConnectionDetails> connectionList = new HashSet<>();

    ConnectionDetails hiveConnectionDetails = new ConnectionDetails(Platforms.HIVE.getValue(), "some.url", "user",
        "password", "driver");
    ConnectionDetails db2ConnectionDetails = new ConnectionDetails(Platforms.DB2.getValue(), "some.url", "user",
        "password", "driver");
    ConnectionDetails teradataConnectionDetails = new ConnectionDetails(Platforms.TERADATA.getValue(), "some.url",
        "user", "password", "driver");

    connectionList.add(hiveConnectionDetails);
    connectionList.add(db2ConnectionDetails);
    connectionList.add(teradataConnectionDetails);

    new UniqueConnectionSettingsValidator().validate(connectionList);
  }

  @Test(expected = DuplicateConnectionSettingsException.class)
  public void validateUniqueConnectionPlatformFailure() {
    Set<ConnectionDetails> connectionList = new HashSet<>();

    ConnectionDetails hiveConnectionDetails = new ConnectionDetails(Platforms.HIVE.getValue(), "some.url", "user",
        "password", "driver");
    ConnectionDetails anotherHiveConnectionDetails = new ConnectionDetails(Platforms.HIVE.getValue(), "some.other.url",
        "user", "password", "driver");
    ConnectionDetails db2ConnectionDetails = new ConnectionDetails(Platforms.DB2.getValue(), "some.url", "user",
        "password", "driver");
    ConnectionDetails teradataConnectionDetails = new ConnectionDetails(Platforms.TERADATA.getValue(), "some.url",
        "user", "password", "driver");

    connectionList.add(hiveConnectionDetails);
    connectionList.add(anotherHiveConnectionDetails);
    connectionList.add(db2ConnectionDetails);
    connectionList.add(teradataConnectionDetails);

    new UniqueConnectionSettingsValidator().validate(connectionList);
  }
}
