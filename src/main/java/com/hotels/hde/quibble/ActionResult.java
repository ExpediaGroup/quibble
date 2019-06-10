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

public class ActionResult implements Comparable<ActionResult> {

  private String platform;
  private long numericResult;

  public ActionResult(String platform, long numericResult) {
    this.platform = platform;
    this.numericResult = numericResult;
  }

  public String getPlatform() {
    return platform;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public long getNumericResult() {
    return numericResult;
  }

  public void setNumericResult(long numericResult) {
    this.numericResult = numericResult;
  }

  @Override
  public int compareTo(ActionResult another) {

    return Long.compare(numericResult, another.numericResult);
  }

}
