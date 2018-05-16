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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.hotels.hde.quibble.executors.DataMatchTestExecutor;
import com.hotels.hde.quibble.executors.SingleNumberMatchTestExecutor;
import com.hotels.hde.quibble.executors.SingleNumberMatchThresholdTestExecutor;
import com.hotels.hde.quibble.executors.ZeroNumberTestExecutor;

public class TestSuiteTest {

  private final TestSuite testSuite = new TestSuite();

  @Test
  public void getTestTypeInstaceForSingleNumberMatch() {

    TestCase aTestCase = TestUtils.createASingleNumberMatchTest("Test 101", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    assertThat(testSuite.getTestExecutorInstance(aTestCase), instanceOf(SingleNumberMatchTestExecutor.class));
  }

  @Test
  public void getTestTypeInstaceForZeroNumberMatch() {

    TestCase aTestCase = TestUtils.createAZeroNumberMatchTest("Test 101", Platforms.HIVE.getValue());
    assertThat(testSuite.getTestExecutorInstance(aTestCase), instanceOf(ZeroNumberTestExecutor.class));
  }

  @Test
  public void getTestTypeInstaceForDataMatch() {

    TestCase aTestCase = TestUtils.createADataMatchTest("Test 101", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    assertThat(testSuite.getTestExecutorInstance(aTestCase), instanceOf(DataMatchTestExecutor.class));
  }

  @Test
  public void getTestTypeInstaceForSingleNumberMatchThresholdTest() {

    TestCase aTestCase = TestUtils.createASingleNumberMatchThresholdTest("Test 101", Platforms.HIVE.getValue(),
        Platforms.TERADATA.getValue());
    assertThat(testSuite.getTestExecutorInstance(aTestCase), instanceOf(SingleNumberMatchThresholdTestExecutor.class));
  }
}
