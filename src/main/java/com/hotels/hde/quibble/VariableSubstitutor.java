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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotels.hde.quibble.exceptions.UnresolvedEnvVariableException;

public class VariableSubstitutor {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\$\\{(\\w+)\\}|\\$(\\w+)");

  public void substituteVariables(List<Action> actions) {
    for (Action action : actions) {
      if (StringUtils.isNotEmpty(action.getCommand()) && StringUtils.isNotBlank(action.getCommand())) {
        action.setCommand(resolveEnvVars(action.getCommand()));
      }
    }
  }

  private String resolveEnvVars(String input) {
    Matcher matcher = ENV_VAR_PATTERN.matcher(input);
    StringBuffer sb = new StringBuffer();

    while (matcher.find()) {
      String envVarName = matcher.group(1) == null ? matcher.group(2) : matcher.group(1);
      String envVarValue = System.getenv(envVarName);

      if (envVarValue == null) {
        String errorMessage = "Failed to resolve $" + envVarName + " environment variable";
        logger.error(errorMessage);
        throw new UnresolvedEnvVariableException(errorMessage);
      }
      logger.info("Resolved $" + envVarName + " value as " + envVarValue);
      matcher.appendReplacement(sb, Matcher.quoteReplacement(envVarValue));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }
}
