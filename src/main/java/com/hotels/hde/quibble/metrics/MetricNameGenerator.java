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

import com.codahale.metrics.MetricRegistry;
import com.github.slugify.Slugify;
import com.google.inject.Inject;

import com.hotels.hde.quibble.config.QuibbleConfiguration;

public class MetricNameGenerator {

  private final Slugify slugify;
  private final QuibbleConfiguration quibbleConfiguration;

  @Inject
  public MetricNameGenerator(
      final MetricRegistry metricRegistry,
      final Slugify slugify,
      final QuibbleConfiguration quibbleConfiguration) {
    this.slugify = slugify;
    this.quibbleConfiguration = quibbleConfiguration;
  }

  public String name(final String name) {
    return MetricRegistry.name(quibbleConfiguration.getDataQualityName(), slugify.slugify(name));
  }

  public String name(final String... names) {
    if (names.length > 0) {
      String lastName = names[names.length - 1];
      names[names.length - 1] = slugify.slugify(lastName);
    }

    return MetricRegistry.name(quibbleConfiguration.getDataQualityName(), names);

  }

}
