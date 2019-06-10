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
package com.hotels.hde.quibble.inject;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import com.hotels.hde.quibble.config.Configurator;
import com.hotels.hde.quibble.config.QuibbleConfiguration;

public class QuibbleConfigurationProvider implements Provider<QuibbleConfiguration> {

  private static final String DEFAULT_GRAPHITE_CONFIG_FILENAME = "graphite.properties";

  @Inject
  @Named("config.path")
  private String configPath;

  @Inject
  @Named("dq.name")
  private String dqName;

  @Override
  public QuibbleConfiguration get() {
    Configurator configurator = new Configurator(configPath, DEFAULT_GRAPHITE_CONFIG_FILENAME);
    QuibbleConfiguration config = QuibbleConfiguration.fromConfigurator(dqName, configurator);

    return config;
  }
}
