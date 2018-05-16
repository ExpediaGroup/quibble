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

import org.junit.Test;

public class StringUtilsTest {

  @Test
  public void testTrimSemiColon() {
    String query1 = "Select * from table;";
    String expectedTrimmedQuery = "Select * from table";

    String trimmedQuery = StringUtils.trimSemicolon(query1);
    assertThat(trimmedQuery, is(expectedTrimmedQuery));
  }

  @Test
  public void testTrimSemiColonNoColon() {
    String query2 = "Select * from table";
    String expectedTrimmedQuery = "Select * from table";

    String trimmedQuery = StringUtils.trimSemicolon(query2);
    assertThat(trimmedQuery, is(expectedTrimmedQuery));
  }

  @Test
  public void testTrimSemiColonMultipleColons() {
    String query3 = "Select * from table;;";
    String expectedTrimmedQuery = "Select * from table";

    String trimmedQuery = StringUtils.trimSemicolon(query3);
    assertThat(trimmedQuery, is(expectedTrimmedQuery));
  }

  @Test
  public void testTrimSemiColonEmptyString() {
    String query4 = "";
    String expectedTrimmedQuery = "";

    String trimmedQuery = StringUtils.trimSemicolon(query4);
    assertThat(trimmedQuery, is(expectedTrimmedQuery));
  }

  @Test
  public void testTrimSemiColonExtraSpaces() {
    String query5 = "  Select * from table;   ";
    String expectedTrimmedQuery = "Select * from table";

    String trimmedQuery = StringUtils.trimSemicolon(query5);
    assertThat(trimmedQuery, is(expectedTrimmedQuery));
  }
}
