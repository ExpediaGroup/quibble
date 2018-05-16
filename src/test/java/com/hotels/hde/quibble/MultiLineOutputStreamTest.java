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
package com.hotels.hde.quibble;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class MultiLineOutputStreamTest {

  @Test
  public void testMultiLinesAdded() throws IOException {

    try (MultiLineOutputStream multiLineOutputStream = new MultiLineOutputStream()) {
      multiLineOutputStream.processLine("First Line", 1);
      multiLineOutputStream.processLine("Second Line", 1);
      multiLineOutputStream.processLine("Last Line", 1);

      List<String> lines = multiLineOutputStream.getLines();

      assertThat(lines.size(), is(3));
      assertThat(lines.get(2), is("Last Line"));
    }
  }
}
