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
package com.hotels.hde.quibble;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SummaryFile {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final Path report;
  private BufferedWriter writer;

  private SummaryFile(Builder builder) throws IOException {
    report = Paths.get(builder.reportPath, builder.filename);
    writer = Files.newBufferedWriter(report, builder.charset, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
  }

  public void writeLine(final String line) throws IOException {
    writer.write(line);
    writer.newLine();
  }

  public void close() {
    try {
      writer.close();
    } catch (IOException e) {
      logger.error("Problem while closing Test Run Summary file", e);
    }
  }

  public static class Builder {

    private String reportPath;
    private String filename = "Test_Run_Summary-" + TestSuite.dateFormat.format(new Date()) + ".txt";
    private Charset charset = Charset.forName("UTF-8");

    public Builder() {}

    public Builder reportPath(final String reportPath) {
      this.reportPath = reportPath;
      return this;
    }

    public Builder filename(final String filename) {
      this.filename = filename;
      return this;
    }

    public Builder charset(final Charset charset) {
      this.charset = charset;
      return this;
    }

    public SummaryFile build() throws IOException {
      return new SummaryFile(this);
    }

  }

}
