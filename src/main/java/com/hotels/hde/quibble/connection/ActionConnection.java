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
package com.hotels.hde.quibble.connection;

import java.sql.Connection;

public class ActionConnection {

  private final String platform;
  private final Connection connection;
  private boolean active;

  public ActionConnection(String platform, Connection connection, boolean active) {
    this.platform = platform;
    this.connection = connection;
    this.active = active;
  }

  public String getPlatform() {
    return platform;
  }

  public Connection getConnection() {
    return connection;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

}
