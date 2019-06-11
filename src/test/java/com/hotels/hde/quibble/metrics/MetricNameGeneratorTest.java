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
package com.hotels.hde.quibble.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.slugify.Slugify;

import com.hotels.hde.quibble.config.QuibbleConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class MetricNameGeneratorTest {

  private static final String DQ_NAME = "com.hotels.test";

  @InjectMocks
  private MetricNameGenerator underTest;

  @Mock
  private Slugify slugify;

  @Mock
  private QuibbleConfiguration expatConfig;

  @Before
  public void setUp() {
    when(expatConfig.getDataQualityName()).thenReturn(DQ_NAME);
    when(slugify.slugify(anyString())).thenReturn("slugified");
  }

  @Test
  public void nameShouldBeSanitised() {
    String metricName = underTest.name("this name should be sanitised");
    assertEquals(DQ_NAME + ".slugified", metricName);
  }

  @Test
  public void nameShouldOnlySanitiseTheLastName() {
    String lastName = "this name should be sanitised";

    String metricName = underTest.name("test", "dummy", lastName);
    assertEquals(DQ_NAME + ".test.dummy.slugified", metricName);
  }
}
