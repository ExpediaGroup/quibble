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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotels.hde.quibble.connection.ConnectionDetails;
import com.hotels.hde.quibble.exceptions.DuplicateConnectionSettingsException;

public class UniqueConnectionSettingsValidator {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public void validate(Set<ConnectionDetails> connections) {
    final Set<String> allConnectionsNames = new HashSet<>();

    for (ConnectionDetails connection : connections) {
      if (!allConnectionsNames.add(connection.getPlatform())) {
        String errorMessage = "Found another connection setting with the same platform name as: "
            + connection.getPlatform();
        logger.error(errorMessage);
        throw new DuplicateConnectionSettingsException(errorMessage);
      }
    }
  }

}
