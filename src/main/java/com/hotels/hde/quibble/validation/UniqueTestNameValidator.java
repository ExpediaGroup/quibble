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
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.exceptions.DuplicateTestNameException;

public class UniqueTestNameValidator {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public void validate(List<TestCase[]> tests) {
    Set<String> allTestNames = new HashSet<String>();
    int DATA_PROVIDER_INDEX = 0;

    for (TestCase[] i : tests) {
      TestCase aTestCase = i[DATA_PROVIDER_INDEX];
      if (!allTestNames.add(aTestCase.getTestName())) {
        String errorMessage = "Found another test case with the same name as: " + aTestCase.getTestName();
        logger.error(errorMessage);
        throw new DuplicateTestNameException(errorMessage);
      }
    }
  }
}
