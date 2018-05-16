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

public class AtLeastOneThresholdValidationRule implements ValidationRule {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private static final int THRESHOLD_PARAMETERS_SIZE_REQUIRED = 1;
  private static final int FIRST_COLUMN_INDEX = 1;

  @Override
  public void validate(TestCase aTestCase) {
    if (aTestCase.getThresholds() == null) {
      String errorMessage = "A "
          + aTestCase.getTestType()
          + " test case must provide "
          + THRESHOLD_PARAMETERS_SIZE_REQUIRED
          + " or more thresholds. A null size was detected.";
      logger.error(errorMessage);
      throw new IncorrectTestDefinitionException(errorMessage);
    } else if (aTestCase.getThresholds().size() < THRESHOLD_PARAMETERS_SIZE_REQUIRED) {
      String errorMessage = "A "
          + aTestCase.getTestType()
          + " test case must provide "
          + THRESHOLD_PARAMETERS_SIZE_REQUIRED
          + " or more thresholds. We detected "
          + aTestCase.getThresholds().size()
          + " thresholds defined.";
      logger.error(errorMessage);
      throw new IncorrectTestDefinitionException(errorMessage);
    } else if (aTestCase.getThresholds().get(0).getColumnIndex() < FIRST_COLUMN_INDEX) {
      String errorMessage = "A "
          + aTestCase.getTestType()
          + " test case provides a column index less than "
          + THRESHOLD_PARAMETERS_SIZE_REQUIRED
          + ". We detected "
          + aTestCase.getThresholds().get(0).getColumnIndex();
      logger.error(errorMessage);
      throw new IncorrectTestDefinitionException(errorMessage);
    }
  }
}
