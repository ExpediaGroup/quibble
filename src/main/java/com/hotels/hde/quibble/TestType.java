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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.hotels.hde.quibble.executors.BaseTestTypeExecutor;
import com.hotels.hde.quibble.executors.DataMatchTestExecutor;
import com.hotels.hde.quibble.executors.KeyedThresholdTestExecutor;
import com.hotels.hde.quibble.executors.SingleNumberMatchTestExecutor;
import com.hotels.hde.quibble.executors.SingleNumberMatchThresholdTestExecutor;
import com.hotels.hde.quibble.executors.ZeroNumberTestExecutor;

/**
 * The following Enum entries are intentionally kept in small letters because a YAML test case is parsed into a
 * JavaBean, which includes an attribute of this Enum type. We do not want to impose any restriction for our user to use
 * upper-case while specifying "test type" in their test cases as rest of the test case fields must be kept in lower
 * case. Therefore, contrary to general practice, it has been decided to use lower case in this Enum.
 */
public enum TestType {

  COUNTMATCH("count_match")

  {
    @Override
    public BaseTestTypeExecutor getExecutor() {
      return new SingleNumberMatchTestExecutor();
    }
  },

  ZEROCOUNT("zero_count")

  {
    @Override
    public BaseTestTypeExecutor getExecutor() {
      return new ZeroNumberTestExecutor();
    }
  },

  COUNTMATCH_WITH_THRESHOLD("count_match_with_threshold")

  {
    @Override
    public BaseTestTypeExecutor getExecutor() {
      return new SingleNumberMatchThresholdTestExecutor();
    }
  },

  DATAMATCH("data_match")

  {
    @Override
    public BaseTestTypeExecutor getExecutor() {
      return new DataMatchTestExecutor(reportDifferences, reportPath);
    }
  },

  KEYED_THRESHOLD("keyed_threshold")

  {
    @Override
    public BaseTestTypeExecutor getExecutor() {
      return new KeyedThresholdTestExecutor();
    }
  };

  private static String reportDifferences, reportPath;

  public static void setReportDifferences(String reportDifferences) {
    TestType.reportDifferences = reportDifferences;
  }

  public static void setReportPath(String reportPath) {
    TestType.reportPath = reportPath;
  }

  public abstract BaseTestTypeExecutor getExecutor();

  private static final Map<String, TestType> lookup = new HashMap<String, TestType>();

  static {
    for (TestType s : EnumSet.allOf(TestType.class)) {
      lookup.put(s.getCode(), s);
    }
  }

  private String code;

  TestType(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public static TestType get(String code) {
    return lookup.get(code);
  }

}
