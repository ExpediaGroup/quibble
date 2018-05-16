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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.LogOutputStream;

public class MultiLineOutputStream extends LogOutputStream {
  private final List<String> lines = new ArrayList<String>();

  @Override
  protected void processLine(String line, int level) {
    lines.add(line);
  }

  public List<String> getLines() {
    return lines;
  }
}
