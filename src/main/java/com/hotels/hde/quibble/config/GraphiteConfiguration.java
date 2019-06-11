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

import static com.google.inject.internal.util.$Preconditions.checkArgument;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

public class GraphiteConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(GraphiteConfiguration.class);

  static final String GRAPHITE_HOST_PROPERTY_NAME = "graphite.host";
  static final String GRAPHITE_PORT_PROPERTY_NAME = "graphite.port";
  static final String GRAPHITE_PREFIX_PROPERTY_NAME = "graphite.prefix";

  private final String host;
  private final int port;
  private final String prefix;

  private GraphiteConfiguration(Builder builder) {
    this.host = builder.host;
    this.port = builder.port;
    this.prefix = builder.prefix;
  }

  @VisibleForTesting
  static GraphiteConfiguration read(final Configurator configurator) {
    Properties props = configurator.getProperties();

    return new Builder()
        .host(props.getProperty(GRAPHITE_HOST_PROPERTY_NAME))
        .port(props.getProperty(GRAPHITE_PORT_PROPERTY_NAME))
        .prefix(props.getProperty(GRAPHITE_PREFIX_PROPERTY_NAME))
        .build();
  }

  @VisibleForTesting
  static Optional<GraphiteConfiguration> readAsOptional(final Configurator configurator) {
    Optional<GraphiteConfiguration> graphite;

    try {
      graphite = Optional.of(read(configurator));
    } catch (Exception e) {
      logger.warn("Graphite configuration could not be loaded. Error: {}", e.getMessage());
      graphite = Optional.absent();
    }

    return graphite;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getPrefix() {
    return prefix;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("host", host).add("port", port).add("prefix", prefix).toString();
  }

  public static class Builder {

    private static final CharMatcher ONLY_LETTERS = CharMatcher
        .inRange('a', 'z')
        .or(CharMatcher.inRange('A', 'Z'))
        .precomputed();

    private String host;
    private int port;
    private String prefix;

    public Builder() {}

    public Builder host(final String host) {
      this.host = host;
      return this;
    }

    public Builder port(final String port) {
      try {
        this.port = Integer.parseInt(port);
        return this;
      } catch (NumberFormatException nfe) {
        throw new IllegalArgumentException(String.format("Port was [%s], but should be a numeric value", port));
      }
    }

    public Builder port(final int port) {
      this.port = port;
      return this;
    }

    /**
     * Prefix will be converted to lowercase.
     */
    public Builder prefix(final String prefix) {
      this.prefix = prefix.toLowerCase();
      return this;
    }

    public GraphiteConfiguration build() {
      validate();
      return new GraphiteConfiguration(this);
    }

    private void validate() {
      checkArgument(!Strings.isNullOrEmpty(host) && !host.trim().isEmpty(),
          "Host was null or empty, but it needs to be defined");
      checkArgument(port > 0, "Port was [%s] but expected nonnegative", port);
      checkArgument(!Strings.isNullOrEmpty(prefix), "Prefix was null or empty, but it needs to be defined");
      checkArgument(ONLY_LETTERS.matchesAllOf(prefix), "Prefix was %s but expected [a-zA-Z]", prefix);
    }

  }

}
