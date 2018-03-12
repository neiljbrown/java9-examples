/*
 *  Copyright 2014-present the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neiljbrown.examples.java9;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A JUnit (5) test case providing examples of the enhancements that have been made to Java interfaces in Java 9 (J9).
 */
// Declare class and test methods as public to support selectively documenting them using Javadoc.
public class JavaInterfaceEnhancementsTest {

  /**
   * Java 9 now permits the use of private, instance or static, methods in interfaces. This allows concrete 'default'
   * (public) and static interface methods, introduced in Java 8, to share common code without making that code
   * public. (It is still the case that Java interfaces are stateless).
   * <p>
   * For an example application of these new features see the {@link Logger} interface below.
   *
   * <h2>Evolution of Behaviour in Java interfaces</h2>
   * Up to and including Java 7, interfaces could only contain public abstract methods - no behaviour. ('public
   * abstract' remains the default access modifiers for interface methods).
   * <p>
   * Java 8 changed this, adding support for behaviour in interfaces in two ways. Firstly, 'default' methods were
   * added to support declaring a (public) abstract method with a default implementation. (The main motivation for
   * this was to support extending interfaces in the standard Java library without breaking backwards compatibility,
   * e.g. to retrofit Collection APIs with methods that accept Lambdas/Functional Interfaces). Secondly, Java 8 also
   * permitted the use of concrete, public static (only) methods in interfaces. This removed the need to create
   * companion static classes for utility methods. (For examples of Java 8 default methods see my
   * <a href="https://bitbucket.org/neilbrown/java8-examples">java8-examples project</a>.).
   * <p>
   * As noted above, Java 9 has now taken this further, now also permitting the use of _private_, static or instance
   * methods, in interfaces.
   */
  @Test
  public void privateMethodsInInterfaces() {
    // This test case is not particularly significant. Just see the Logger interface which it uses, declared below.
    final String logMessage = "Logged to writer.";
    final StringWriter stringWriter = new StringWriter();
    new MyLoggerImpl(stringWriter).info(logMessage);

    assertThat(stringWriter.getBuffer().toString()).isEqualTo("[INFO] " + logMessage);
  }

  /**
   * Java interface providing example of support for new private (instance or static) methods in Java 9.
   */
  interface Logger {

    // Traditional declaration of an (implied public) abstract method in an interface
    public void info(String message);

    // (From Java 8, the 'default' keyword was introduced to declare an abstract method with a default implementation).
    default void error(String message) {
      // Example of default method invoking a private instance method, new for Java 9.
      this.logToConsole("[ERROR]", message);
    }

    default void warn(String message) {
      this.logToConsole("[WARN]", message);
    }

    // (From Java 8, interfaces could also include (implied public) static methods, removing the need to create
    // companion static classes for utility methods. (In Java 8 these methods could only be public).
    public static String buildInfoLogMessage(String message) {
      return Logger.buildLogMessage("[INFO]", message);
    }

    // New in Java 9, interfaces can contain private instance methods for sharing code between non-static default method
    private void logToConsole(String level, String message) {
      System.out.println(Logger.buildLogMessage(level, message));
    }

    // New in Java 9, interfaces can contain private static methods for sharing code between default and/or public
    // static methods
    private static String buildLogMessage(String level, String message) {
      return level + " " + message;
    }
  }

  /**
   * An implementation of {@link Logger} to support examples used in test cases. Logs to a supplied {@link Writer}
   * rather than the console to support asserting the result.
   */
  class MyLoggerImpl implements Logger {
    private Writer writer;

    MyLoggerImpl(Writer writer) {
      this.writer = writer;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implements {@link Logger#info(String)} by logging the message with an '[INFO]' prefix to the Writer.
     */
    @Override
    public void info(String message) {
      try {
        this.writer.append(Logger.buildInfoLogMessage(message));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
