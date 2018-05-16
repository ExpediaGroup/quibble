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
package com.hotels.hde.quibble.config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class QuibbleConfigurationTest {

  private static final String VALID_TEST_NAME = "myTest";
  private static final String VALID_GRAPHITE_HOST = "my.dummy.graphite";
  private static final String VALID_GRAPHITE_PORT = "1000";
  private static final String VALID_GRAPHITE_PREFIX = "tests";

  private final Properties props = new Properties();

  @Before
  public void setUp() {
    props.setProperty("dq.name", VALID_TEST_NAME);
    props.setProperty("graphite.host", VALID_GRAPHITE_HOST);
    props.setProperty("graphite.port", VALID_GRAPHITE_PORT);
    props.setProperty("graphite.prefix", VALID_GRAPHITE_PREFIX);
  }

  @Test
  public void fromConfiguratorShouldReturnAValidInstance() {
    Configurator configurator = mock(Configurator.class);
    when(configurator.getProperties()).thenReturn(props);

    QuibbleConfiguration underTest = QuibbleConfiguration.fromConfigurator(VALID_TEST_NAME, configurator);

    assertEquals(VALID_TEST_NAME, underTest.getDataQualityName());

    GraphiteConfiguration graphiteConf = underTest.getGraphiteConfiguration().get();
    assertEquals(VALID_GRAPHITE_HOST, graphiteConf.getHost());
    assertEquals(Integer.parseInt(VALID_GRAPHITE_PORT), graphiteConf.getPort());
    assertEquals(VALID_GRAPHITE_PREFIX, graphiteConf.getPrefix());
  }
}
