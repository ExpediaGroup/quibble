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

import java.io.IOException;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteSender;
import com.github.slugify.Slugify;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;

import com.hotels.hde.quibble.config.QuibbleConfiguration;
import com.hotels.hde.quibble.connection.ConnectionLoader;
import com.hotels.hde.quibble.connection.ConnectionManager;
import com.hotels.hde.quibble.metrics.MetricNameGenerator;

public class QuibbleModule implements Module {

  private static final String DEFAULT_DQ_NAME = "com.hotels.hdw.bix.default-dq";

  @Override
  public void configure(final Binder binder) {

    binder.bind(String.class).annotatedWith(Names.named("config.path")).toInstance(System.getProperty("ConfDir", "."));
    binder.bind(String.class).annotatedWith(Names.named("dq.name")).toInstance(
        System.getProperty("dq.name", DEFAULT_DQ_NAME));
    binder.bind(String.class).annotatedWith(Names.named("test.path")).toInstance(System.getProperty("TestDir", "."));
    binder.bind(String.class).annotatedWith(Names.named("report.path")).toInstance(
        System.getProperty("ReportPath", "."));
    binder.bind(String.class).annotatedWith(Names.named("report.diffs")).toInstance(
        System.getProperty("ReportDiffs", "None"));
    binder.bind(String.class).annotatedWith(Names.named("check.duplicte.testnames")).toInstance(
        System.getProperty("CheckDuplicateTestNames", "false"));

    binder.bind(String.class).annotatedWith(Names.named("test.name")).toInstance(System.getProperty("TestName", ""));

    binder.bind(QuibbleConfiguration.class).toProvider(QuibbleConfigurationProvider.class).asEagerSingleton();
    binder.bind(GraphiteSender.class).toProvider(GraphiteSenderProvider.class);
    binder.bind(GraphiteReporter.class).toProvider(GraphiteReporterProvider.class);
    binder.bind(ConnectionLoader.class).toProvider(YamlConnectionLoaderProvider.class);
    binder.bind(ConnectionManager.class).toProvider(ConnectionManagerProvider.class);
    binder.bind(MetricRegistry.class).asEagerSingleton();
    binder.bind(MetricNameGenerator.class);

    try {
      binder.bind(Slugify.class).toInstance(new Slugify());
    } catch (IOException e) {
      throw new RuntimeException("Could not instantiate Slugify. Cannot run Quibble", e);
    }
  }

}
