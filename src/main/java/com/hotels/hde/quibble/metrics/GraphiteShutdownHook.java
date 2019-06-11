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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteSender;
import com.google.inject.Inject;

public class GraphiteShutdownHook implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(GraphiteShutdownHook.class);

  private final GraphiteSender graphite;
  private final GraphiteReporter graphiteReporter;

  /* visible for testing */
  @Inject
  GraphiteShutdownHook(final GraphiteSender graphite, final GraphiteReporter graphiteReporter) {
    this.graphite = checkNotNull(graphite);
    this.graphiteReporter = checkNotNull(graphiteReporter);
  }

  @Override
  public void run() {
    logger.info("Shutting down: Sending report to Graphite");
    try {
      graphite.flush();
      graphiteReporter.report();
      graphiteReporter.stop();
    } catch (IOException e) {
      logger.warn("Could not stop Graphite.", e);
    }
  }

}
