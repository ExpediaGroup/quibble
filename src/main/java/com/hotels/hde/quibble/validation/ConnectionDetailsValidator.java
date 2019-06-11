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
package com.hotels.hde.quibble.validation;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import com.hotels.hde.quibble.Platforms;
import com.hotels.hde.quibble.connection.ConnectionDetails;
import com.hotels.hde.quibble.exceptions.InvalidConnectionDetailsException;
import com.hotels.hde.quibble.exceptions.ReservedWordUsedException;

public class ConnectionDetailsValidator {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  public void validate(ConnectionDetails aConnection) {
    final String[] RESERVED_WORD = { Platforms.SHELL.getValue() };
    if (Strings.isNullOrEmpty(aConnection.getPlatform())
        || Strings.isNullOrEmpty(aConnection.getUrl())
        || Strings.isNullOrEmpty(aConnection.getUsername())
        || Strings.isNullOrEmpty(aConnection.getDriver())
        || Strings.isNullOrEmpty(aConnection.getPassword())) {
      String errorMessage = "Some connection details are missing for: " + aConnection.getPlatform();
      logger.error(errorMessage);
      throw new InvalidConnectionDetailsException(errorMessage);
    }
    if (!Strings.isNullOrEmpty(aConnection.getPlatform())
        && Arrays.asList(RESERVED_WORD).contains(aConnection.getPlatform())) {
      String errorMessage = aConnection.getPlatform()
          + " is a reserved word used for running shell commands and can not be used to define a platform type in connections";
      logger.error(errorMessage);
      throw new ReservedWordUsedException(errorMessage);
    }

  }
}
