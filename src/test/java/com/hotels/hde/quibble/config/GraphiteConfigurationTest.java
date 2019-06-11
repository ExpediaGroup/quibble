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
package com.hotels.hde.quibble.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;

@RunWith(MockitoJUnitRunner.class)
public class GraphiteConfigurationTest {

  private static final String VALID_HOST = "my.dummy.graphite.host";
  private static final int VALID_PORT = 10;
  private static final String VALID_PREFIX = "validprefix";

  @Mock
  private Configurator configurator;

  private final Properties properties = new Properties();

  @Before
  public void setUp() {
    properties.setProperty(GraphiteConfiguration.GRAPHITE_HOST_PROPERTY_NAME, VALID_HOST);
    properties.setProperty(GraphiteConfiguration.GRAPHITE_PORT_PROPERTY_NAME, String.valueOf(VALID_PORT));
    properties.setProperty(GraphiteConfiguration.GRAPHITE_PREFIX_PROPERTY_NAME, VALID_PREFIX);

    when(configurator.getProperties()).thenReturn(properties);
  }

  @Test(expected = IllegalArgumentException.class)
  public void hostShouldNotBeNull() {
    new GraphiteConfiguration.Builder().host(null).port(VALID_PORT).prefix(VALID_PREFIX).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void hostShouldNotBeEmpty() {
    new GraphiteConfiguration.Builder().host("").port(VALID_PORT).prefix(VALID_PREFIX).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void hostShouldNotBeJustWhitespace() {
    new GraphiteConfiguration.Builder().host("  ").port(VALID_PORT).prefix(VALID_PREFIX).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void portShouldBeGreaterThanZero() {
    new GraphiteConfiguration.Builder().host(VALID_HOST).port(-1).prefix(VALID_PREFIX).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void prefixShouldNotContainDotAsSeparator() {
    new GraphiteConfiguration.Builder().host(VALID_HOST).port(VALID_PORT).prefix("some.invalid.prefix").build();
  }

  @Test
  public void prefixShouldBeLowercased() {
    GraphiteConfiguration gc = new GraphiteConfiguration.Builder()
        .host(VALID_HOST)
        .port(VALID_PORT)
        .prefix("MYPREFIX")
        .build();

    assertEquals("myprefix", gc.getPrefix());
  }

  @Test
  public void builderShouldCreateNewInstanceWithTheRightValues() {
    GraphiteConfiguration gc = new GraphiteConfiguration.Builder()
        .host(VALID_HOST)
        .port(VALID_PORT)
        .prefix(VALID_PREFIX)
        .build();

    assertEquals(VALID_HOST, gc.getHost());
    assertEquals(VALID_PORT, gc.getPort());
    assertEquals(VALID_PREFIX, gc.getPrefix());
  }

  @Test
  public void readShouldCreateAValidInstance() {
    GraphiteConfiguration gc = GraphiteConfiguration.read(configurator);

    assertEquals(VALID_HOST, gc.getHost());
    assertEquals(VALID_PORT, gc.getPort());
    assertEquals(VALID_PREFIX, gc.getPrefix());
  }

  @Test
  public void readAsOptionalShouldReturnNoneIfHostIsInvalid() {
    properties.remove(GraphiteConfiguration.GRAPHITE_HOST_PROPERTY_NAME);
    assertFalse(GraphiteConfiguration.readAsOptional(configurator).isPresent());
  }

  @Test
  public void readAsOptionalShouldReturnNoneIfPortIsInvalid() {
    properties.remove(GraphiteConfiguration.GRAPHITE_PORT_PROPERTY_NAME);
    assertFalse(GraphiteConfiguration.readAsOptional(configurator).isPresent());
  }

  @Test
  public void readAsOptionalShouldReturnNoneIfPrefixIsInvalid() {
    properties.remove(GraphiteConfiguration.GRAPHITE_PREFIX_PROPERTY_NAME);
    assertFalse(GraphiteConfiguration.readAsOptional(configurator).isPresent());
  }

  @Test
  public void readAsOptionalShouldReturnSomeIfPropertiesAreValid() {
    Optional<GraphiteConfiguration> graphiteConfig = GraphiteConfiguration.readAsOptional(configurator);

    assertTrue(graphiteConfig.isPresent());
    assertEquals(VALID_HOST, graphiteConfig.get().getHost());
    assertEquals(VALID_PORT, graphiteConfig.get().getPort());
    assertEquals(VALID_PREFIX, graphiteConfig.get().getPrefix());
  }
}
