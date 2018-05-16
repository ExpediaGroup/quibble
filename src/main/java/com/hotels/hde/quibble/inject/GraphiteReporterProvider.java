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
package com.hotels.hde.quibble.inject;

import java.util.concurrent.TimeUnit;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteSender;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Provider;

import com.hotels.hde.quibble.config.GraphiteConfiguration;
import com.hotels.hde.quibble.config.QuibbleConfiguration;

public class GraphiteReporterProvider implements Provider<GraphiteReporter> {

  private static final String NO_PREFIX = null;

  @Inject
  private MetricRegistry metricRegistry;

  @Inject
  private QuibbleConfiguration quibbleConfig;

  @Inject
  private GraphiteSender graphite;

  @Override
  public GraphiteReporter get() {
    GraphiteReporter reporter = GraphiteReporter
        .forRegistry(metricRegistry)
        .prefixedWith(getPrefix())
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .filter(MetricFilter.ALL)
        .build(graphite);

    return reporter;
  }

  private String getPrefix() {
    Optional<GraphiteConfiguration> graphiteConf = quibbleConfig.getGraphiteConfiguration();
    return graphiteConf.isPresent() ? graphiteConf.get().getPrefix() : NO_PREFIX;

  }

}
