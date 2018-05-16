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
package com.hotels.hde.quibble.metrics;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;

@RunWith(MockitoJUnitRunner.class)
public class GraphiteShutdownHookTest {

  @Mock
  private Graphite graphite;

  @Mock
  private GraphiteReporter graphiteReporter;

  @Test(expected = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfGraphiteIsNull() {
    new GraphiteShutdownHook(null, mock(GraphiteReporter.class));
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfGraphiteReporterIsNull() {
    new GraphiteShutdownHook(mock(Graphite.class), null);
  }

  @Test
  public void runShouldFlushReportAndStopGraphite() throws IOException {
    GraphiteShutdownHook underTest = new GraphiteShutdownHook(graphite, graphiteReporter);

    underTest.run();

    verify(graphite).flush();
    verify(graphiteReporter).report();
    verify(graphiteReporter).stop();
  }

  @Test
  public void runShouldHandleIOExceptionGracefully() throws IOException {
    GraphiteShutdownHook underTest = new GraphiteShutdownHook(graphite, graphiteReporter);

    Mockito.doThrow(IOException.class).when(graphite).flush();

    underTest.run();

    verify(graphite).flush();
    verify(graphiteReporter, never()).report();
    verify(graphiteReporter, never()).stop();
  }
}
