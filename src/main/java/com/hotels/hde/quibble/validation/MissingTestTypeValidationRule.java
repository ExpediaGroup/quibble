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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotels.hde.quibble.TestCase;
import com.hotels.hde.quibble.exceptions.IncorrectTestDefinitionException;

public class MissingTestTypeValidationRule implements ValidationRule {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void validate(TestCase aTestCase) {
    if (aTestCase.getTestType() == null) {
      String errorMessage = "Test type missing for: " + aTestCase.getTestName();
      logger.error(errorMessage);
      throw new IncorrectTestDefinitionException(errorMessage);
    }
  }
}
