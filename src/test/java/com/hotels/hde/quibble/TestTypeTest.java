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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestTypeTest {

  @Test
  public void testTestTypeEnum() {
    assertThat(TestType.valueOf(TestType.COUNTMATCH.name()), is(notNullValue()));
    assertThat(TestType.valueOf(TestType.DATAMATCH.name()), is(notNullValue()));
    assertThat(TestType.valueOf(TestType.ZEROCOUNT.name()), is(notNullValue()));
    assertThat(TestType.valueOf(TestType.COUNTMATCH_WITH_THRESHOLD.name()), is(notNullValue()));
    assertThat(TestType.valueOf(TestType.KEYED_THRESHOLD.name()), is(notNullValue()));
    assertThat(TestType.values().length, is(5));
  }
}
