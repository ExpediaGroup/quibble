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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import com.hotels.hde.quibble.exceptions.UnresolvedEnvVariableException;

public class VariableSubstitutorTest {

  @Rule
  public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @Test
  public void substituteCommandVariablesSuccess() {
    environmentVariables.set("COLUMN1_VAR", "COLUMN1_VALUE");
    environmentVariables.set("table_name", "LOYALTY_REWARDS");

    Action action1 = new Action();
    action1.setPlatform(Platforms.HIVE.getValue());
    String command1 = "Select count(*) from table where column1 = ";
    action1.setCommand(command1.concat("$COLUMN1_VAR"));

    Action action2 = new Action();
    action2.setPlatform(Platforms.TERADATA.getValue());
    String command2 = "Select count(*) from ";
    action2.setCommand(command2.concat("${table_name}"));

    List<Action> actions = new ArrayList<>();
    actions.add(action1);
    actions.add(action2);

    VariableSubstitutor vs = new VariableSubstitutor();
    vs.substituteVariables(actions);

    assertThat(action1.getCommand(), is(command1.concat("COLUMN1_VALUE")));
    assertThat(action2.getCommand(), is(command2.concat("LOYALTY_REWARDS")));
  }

  @Test(expected = UnresolvedEnvVariableException.class)
  public void substituteCommandVariablesFailure() {
    environmentVariables.set("COLUMN1_VAR", "COLUMN1_VALUE");
    // No value is defined for table_name variable

    Action action1 = new Action();
    action1.setPlatform(Platforms.HIVE.getValue());
    String command1 = "Select count(*) from table where column1 = ";
    action1.setCommand(command1.concat("$COLUMN1_VAR"));

    Action action2 = new Action();
    action2.setPlatform(Platforms.TERADATA.getValue());
    String command2 = "Select count(*) from ";
    action2.setCommand(command2.concat("$table_name"));

    List<Action> actions = new ArrayList<>();
    actions.add(action1);
    actions.add(action2);

    VariableSubstitutor vs = new VariableSubstitutor();
    vs.substituteVariables(actions);
  }

  @Test
  public void noCommandVariables() {
    Action action1 = new Action();
    action1.setPlatform(Platforms.HIVE.getValue());
    String command1 = "Select count(*) from table where column1 = COLUMN1_VALUE";
    action1.setCommand(command1);

    Action action2 = new Action();
    action2.setPlatform(Platforms.TERADATA.getValue());
    String command2 = "Select count(*) from LOYALTY_REWARDS";
    action2.setCommand(command2);

    List<Action> actions = new ArrayList<>();
    actions.add(action1);
    actions.add(action2);

    VariableSubstitutor vs = new VariableSubstitutor();
    vs.substituteVariables(actions);

    assertThat(action1.getCommand(), is(command1));
    assertThat(action2.getCommand(), is(command2));
  }

  @Test
  public void noFailureOnEmptyCommand() {
    Action action1 = new Action();
    action1.setPlatform(Platforms.HIVE.getValue());
    String command1 = "";
    action1.setCommand(command1);

    Action action2 = new Action();
    action2.setPlatform(Platforms.TERADATA.getValue());
    String command2 = "";
    action2.setCommand(command2);

    List<Action> actions = new ArrayList<>();
    actions.add(action1);
    actions.add(action2);

    VariableSubstitutor vs = new VariableSubstitutor();
    vs.substituteVariables(actions);

    assertThat(action1.getCommand(), is(command1));
    assertThat(action2.getCommand(), is(command2));
  }

  @Test
  public void noFailureOnNullCommand() {
    Action action1 = new Action();
    action1.setPlatform(Platforms.HIVE.getValue());
    String command1 = null;
    action1.setCommand(command1);

    Action action2 = new Action();
    action2.setPlatform(Platforms.TERADATA.getValue());
    String command2 = null;
    action2.setCommand(command2);

    List<Action> actions = new ArrayList<>();
    actions.add(action1);
    actions.add(action2);

    VariableSubstitutor vs = new VariableSubstitutor();
    vs.substituteVariables(actions);

    assertThat(action1.getCommand(), is(command1));
    assertThat(action2.getCommand(), is(command2));
  }
}
