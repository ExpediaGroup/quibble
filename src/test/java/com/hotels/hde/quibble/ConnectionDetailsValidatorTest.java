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

import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.hotels.hde.quibble.connection.ConnectionDetails;
import com.hotels.hde.quibble.exceptions.InvalidConnectionDetailsException;
import com.hotels.hde.quibble.exceptions.ReservedWordUsedException;
import com.hotels.hde.quibble.validation.ConnectionDetailsValidator;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionDetailsValidatorTest {

  @Mock
  private ConnectionDetails mockconnection;

  private ConnectionDetailsValidator connectionDetails = new ConnectionDetailsValidator();

  @Test
  public void testvalidate() {

    when(mockconnection.getPlatform()).thenReturn("hive");
    when(mockconnection.getDriver()).thenReturn("driver");
    when(mockconnection.getPassword()).thenReturn("password");
    when(mockconnection.getUrl()).thenReturn("url");
    when(mockconnection.getUsername()).thenReturn("user");

    connectionDetails.validate(mockconnection);
  }

  @Test(expected = InvalidConnectionDetailsException.class)
  public void testInvalidConnectionFailure() {

    when(mockconnection.getPlatform()).thenReturn(null);
    when(mockconnection.getDriver()).thenReturn("driver");
    when(mockconnection.getPassword()).thenReturn("password");
    when(mockconnection.getUrl()).thenReturn("url");
    when(mockconnection.getUsername()).thenReturn("user");

    connectionDetails.validate(mockconnection);
  }

  @Test(expected = ReservedWordUsedException.class)
  public void testReservedWordFailure() {

    when(mockconnection.getPlatform()).thenReturn("shell");
    when(mockconnection.getDriver()).thenReturn("driver");
    when(mockconnection.getPassword()).thenReturn("password");
    when(mockconnection.getUrl()).thenReturn("url");
    when(mockconnection.getUsername()).thenReturn("user");

    connectionDetails.validate(mockconnection);
  }
}