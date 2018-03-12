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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A JUnit (5) test case providing examples of the enhancements that have been made to {@link java.util.Optional} in
 * Java 9 (J9).
 */
// Declare class and test methods as public to support selectively documenting them using Javadoc.
public class OptionalTypeTest {

  /**
   * In J9, {@link java.util.Optional} has been extended to include the new {@link Optional#stream()} method that
   * either returns a Stream containing the Optional if it has a present value, or else, if the Optional doesn't have
   * a value, an empty Stream. The method has been added to simplify the task of transforming a Stream of Optional
   * values to a Stream of Optional with present value.
   */
  @Test
  public void optionalStream() {
    // Example use-case/requirement - Given a list of system properties defining a user's home directory, return the
    // value of the first property that exists with a value

    // Create a list of System property names
    String nonExistentSystemProperty1 = "my.home." + Instant.now().toString();
    String nonExistentSystemProperty2 = "userHome." + Instant.now().toString();
    List<String> userHomeSystemPropertyNames = List.of(nonExistentSystemProperty1, "user.home", nonExistentSystemProperty2);
    // Convert the System property names to a list of their optional (depending on whether defined) values
    List<Optional<String>> settings = userHomeSystemPropertyNames.stream()
      .map(propertyName -> Optional.ofNullable(System.getProperty(propertyName)))
      .collect(Collectors.toList());

    // J8 solution - The requirement can be satisfied using the Stream API in J8, but it requires you to explicitly
    // filter the Stream for Optional with a value -
    Optional<String> homeDirString = settings.stream().
      // First have to check Optional has a value
      filter(Optional::isPresent).
      // Then have to retrieve the value
      map(Optional::get).
      findFirst();
    assertThat(homeDirString.get()).isEqualTo(System.getProperty("user.home"));

    // J9 solution - The solution can be simplified to -
    homeDirString = settings.stream().
      // Use new Optional.stream() method to convert the Optional to a Stream of length one or zero depending on
      // whether it has a value, then use flatMap() to reduce the Streams.
      flatMap(Optional::stream).
      findFirst();
    assertThat(homeDirString.get()).isEqualTo(System.getProperty("user.home"));
  }

  /**
   * In J9, {@link java.util.Optional} has been extended to include the new {@link Optional#or(Supplier)} method which
   * either returns an Optional describing the value, if it's present, _or_ else returns an Optional produced by the
   * Supplier function.
   * <p>
   * This new method is useful in cases where you want to use a fallback method that also returns an Optional.
   */
  @Test
  public void optionalOr() {
    // Example requirement - Find the details of a company by name in your DB, and if it doesn’t exist, look up the
    // company using an external service. In both cases, if the name isn’t valid, the company may not be found, as
    // indicated by the return of an Optional

    // In J8, you could try to implement this requirement, assuming methods which return Optional, using the
    // Optional.orElseGet(Supplier) : T  method, but as shown below, this won't compile -
    CompanyDao dao = new CompanyDao();
    final String companyName = "ACME Inc.";
    // Doesn't compile as orElseGet(Supplier) must return the value (in this case a Company), rather than an Optional
    //dao.findByName(companyName).orElseGet(() -> dao.lookupCompany(companyName));


    // In J9, the requirement can be satisfied using the new or() method -
    // The following statement can have 3 possible outcomes
    // 1) The Company is found and returned by findByName().
    // 2) The Company is returned by lookupCompany().
    // 3) Or, neither method returns a Company, resulting in an empty Optional being returned.
    Optional<Company> optionalCompany = dao.findByName(companyName).or(() -> dao.lookupCompany(companyName));
    assertThat(optionalCompany).isNotPresent();
  }

  /**
   * In J9, {@link java.util.Optional} has been extended to include the new {@link Optional#ifPresentOrElse(Consumer, Runnable)}
   * method which supports supplying 2 actions to execute depending on whether or not the Optional has a value - a
   * Consumer to process the value if it has one, else a default / fallback action. Note that neither of the two
   * functional interfaces (Consumer or Runnable) invoked by the method can return a value.
   */
  @Test
  public void optionalIfPresentOrElse() {
    // An example scenario in which Optional.ifPresentOrElse() would be useful is using a default value in the event
    // that an entity wasn't found, e.g.
    CompanyDao dao = new CompanyDao();
    final String companyName = "ACME Inc.";
    final Optional<Company> optionalCompany = dao.findByName(companyName);

    optionalCompany.ifPresentOrElse(
      company -> {
        // Consume the present value, in this case a Company
      },
      () -> {
        // Else, if empty, execute some fallback action represented by a Runnable (with no input or ouput)
      }
    );

  }
  static class CompanyDao {
    Optional<Company> findByName(String companyName) {
      return Optional.empty(); // for the purposes of these tests, the company is never found
    }

    Optional<Company> lookupCompany(String companyName) {
      return Optional.empty(); // for the purposes of these tests, the company is never found
    }
  }

  static class Company {
    // No fields needed for the purposes of these tests
  }
}
