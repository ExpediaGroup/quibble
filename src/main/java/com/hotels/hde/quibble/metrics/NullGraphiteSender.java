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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.graphite.GraphiteSender;

public class NullGraphiteSender implements GraphiteSender {

  private final Logger logger = LoggerFactory.getLogger(NullGraphiteSender.class);

  @Override
  public void connect() throws IllegalStateException, IOException {
    logger.debug("Not connecting anywhere");
  }

  @Override
  public void send(String name, String value, long timestamp) throws IOException {
    logger.debug("Discarding metric {} with value {}", name, value);
  }

  @Override
  public void flush() throws IOException {
    logger.debug("Flushing metrics");
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public int getFailures() {
    return 0;
  }

  @Override
  public void close() throws IOException {
    logger.debug("Closing DummyGraphiteSender");
  }

}
