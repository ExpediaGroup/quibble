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

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

public class QuibbleConfiguration {

  private final String dataQualityName;
  private final Optional<GraphiteConfiguration> graphite;

  public QuibbleConfiguration(final String dataQualityName, final Optional<GraphiteConfiguration> graphite) {
    this.dataQualityName = dataQualityName;
    this.graphite = graphite;
  }

  public static QuibbleConfiguration fromConfigurator(final String dataQualityName, final Configurator configurator) {
    return new QuibbleConfiguration(dataQualityName, GraphiteConfiguration.readAsOptional(configurator));
  }

  public String getDataQualityName() {
    return dataQualityName;
  }

  public Optional<GraphiteConfiguration> getGraphiteConfiguration() {
    return graphite;
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("dataQualityName", dataQualityName)
        .add("graphite", graphite.toString())
        .toString();
  }

}
