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

import java.net.InetSocketAddress;

import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteSender;
import com.google.inject.Inject;
import com.google.inject.Provider;

import com.hotels.hde.quibble.config.GraphiteConfiguration;
import com.hotels.hde.quibble.config.QuibbleConfiguration;
import com.hotels.hde.quibble.metrics.NullGraphiteSender;

public class GraphiteSenderProvider implements Provider<GraphiteSender> {

  @Inject
  private QuibbleConfiguration quibbleConfiguration;

  @Override
  public GraphiteSender get() {
    GraphiteSender graphite = null;
    if (quibbleConfiguration.getGraphiteConfiguration().isPresent()) {
      GraphiteConfiguration graphiteConf = quibbleConfiguration.getGraphiteConfiguration().get();
      graphite = new Graphite(new InetSocketAddress(graphiteConf.getHost(), graphiteConf.getPort()));
    } else {
      graphite = new NullGraphiteSender();
    }

    return graphite;
  }

}
