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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Month;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * A JUnit (5) test case providing examples of the enhancements that have been made to the  <a
 * href="https://docs.oracle.com/javase/9/docs/api/java/util/stream/package-summary.html">Stream API</a>
 * in Java 9 (J9), including -
 * <p>
 * Support for iterating until a predicate is satisfied, providing a better functional replacement for the
 * traditional for-loop. See {@link #iterateWithTerminationPredicate()}.
 * <p>
 * Support for only processing an initial subset (head) of elements from an ordered Stream until a specified condition
 * is met. See {@link #takeWhile()}.
 * <p>
 * Support for only processing a tail of elements from an ordered stream, after a specified condition is met (or put
 * another way ignoring an initial subset (head) of elements until a condition is met). See {@link #dropWhile()}.
 * <p>
 * A new method that makes it easier to handle null values in a Stream. See {@link #ofNullable()}.
 * <p>
 * Support for applying a filter operation on a Stream at collection time, after also, first grouping the stream
 * elements at collection time. See {@link #filteringCollector()}.
 * <p>
 * Support for applying a flat-map operation on a Stream at collection time, after also, first grouping the stream
 * elements at collection time. See {@link #flatMappingCollector()}.
 */
// Declare class and test methods as public to support selectively documenting them using Javadoc.
public class StreamApiEnhancementsTest {

  /**
   * In Java 9, the Stream API has been extended to provide a new static method to support iteration -
   * {@link Stream#iterate(Object, Predicate, UnaryOperator)}.
   * <p>
   * Java 8 added the static method {@link Stream#iterate(Object, UnaryOperator)} method which supports iterating
   * over an _infinite_ stream, from an initial 'seed' value, by iteratively applying a function to it, e.g. start
   * at 1 and add 1 each time. Whilst it is possible to use this as a functional replacement for a traditional
   * for-loop, this relies on applying {@link Stream#limit(long)}} function to the infinite stream, which is only
   * feasible when the no. of _iterations_ (only, not value) can be calculated ahead of time. The new J9 iterate method
   * addresses this limitation by supporting an additional Predicate argument which can be used to implement a
   * dynamic, conditional exit condition. This provides much simpler to use functional replacement for the
   * traditional for-loop.
   */
  @Test
  public void iterateWithTerminationPredicate() {
    // In J8, the 2-arg Stream.iterate() method supports iterating over an infinite stream. Using this as a functional
    // equivalent of the traditional for-loop requires applying Stream.limit(), but this is only feasible if the
    // required no. of _iterations_ is known ahead of time - functional equivalent of 'for (int i = 0; i <= 9; i++)'
    long numIterations = Stream.iterate(0, n -> n + 1)
      .limit(10)
      .count();

    assertThat(numIterations).isEqualTo(10);

    // If you want to exit the iteration based on the vale of the loop variable rather than the no. of iterations,
    // e.g. because you need to increment the loop variable by a amount other than 1, the above J8 code doesn't work.
    // For example, say you need to process all even values less than a derived amount, e.g. 10. The above solution
    // will incorrectly perform 10 iterations of values 0, 2, 4, ... 18.
    // You could try using Stream.filter(), in place of Stream.limit(), to apply the amount-based exit condition for the
    // iteration, as shown below. This will result in only the expected values being processed, but the process will
    // hang because having removed Stream.limit() it reverts back to an infinite stream.
    /* This solution hangs....
    Stream.iterate(0, n -> n + 2)
      .filter(n -> n < 10)
      .forEach(System.out::println);
    */

    // J9 solution - Use new 3-arg Stream.iterate() method with Predicate for exit condition -
    List<Integer> values = Stream.iterate(0, n -> n < 10, n -> n + 2)
      .collect(Collectors.toList());

    assertThat(values).containsExactly(0, 2, 4, 6, 8);
  }

  /**
   * In Java 9, the Stream API has been extended to provide a new method that makes it easier to conditionally
   * process an initial subset of elements from a (typically ordered) Stream - {@link Stream#takeWhile(Predicate)}.
   * <p>
   * Java 8 added the {@link Stream#limit(long)} method to support only processing the first 'n' elements from a
   * stream, e.g. to convert an infinite stream to a finite one. However, this only supports cases where the no. of
   * elements to process is known ahead of time. (An example of why this can be a limitation is shown in
   * test case {@link #iterateWithTerminationPredicate()}. In Java 9, the new {@link Stream#takeWhile(Predicate)}
   * method allows you to provide your own predicate for deciding when to stop processing elements from the Stream.
   * <p>
   * Note - This method is typically only used on an ordered Stream. If used on an unordered Stream you may miss
   * elements because the predicate only has to be satisfied once.
   *
   * @see #dropWhile()
   */
  @Test
  public void takeWhile() {
    // Problem - Given a list of Payments, ordered by amount, select only those Payments with a amount less than 500
    final List<Payment> payments = List.of(new Payment(new BigDecimal("100.00")), new Payment(new BigDecimal("250.50")),
      new Payment(new BigDecimal("499.99")), new Payment(new BigDecimal("500.00")),
      new Payment(new BigDecimal("600.00")));

    // J8 solution - Can be solved using Stream.filter() to apply the predicate (which also supports unordered
    // Streams), but for ordered streams this is less efficient then the new J9 solution below
    List<Payment> smallPayments = payments.stream()
      .filter(p -> p.getAmount().compareTo(new BigDecimal("500.00")) < 0)
      .collect(Collectors.toList());

    assertThat(smallPayments).containsExactly(new Payment(new BigDecimal("100.00")),
      new Payment(new BigDecimal("250.50")), new Payment(new BigDecimal("499.99")));

    // J9 solution - Uses new Stream.takeWhile() method. When used on an ordered stream this is more efficient than
    // using Stream.filter() as it doesn't require every element in the stream to be processed - processing of the
    // Stream is short-circuited when the predicate is satisfied / condition is met.
    smallPayments = payments.stream()
      .takeWhile(p -> p.getAmount().compareTo(new BigDecimal("500.00")) < 0)
      .collect(Collectors.toList());

    assertThat(smallPayments).containsExactly(new Payment(new BigDecimal("100.00")),
      new Payment(new BigDecimal("250.50")), new Payment(new BigDecimal("499.99")));
  }

  /**
   * In Java 9, the Stream API has been extended to provide a new method that makes it easier to discard ('drop') the
   * processing of initial elements from a (typically ordered) Stream until a condition is met -
   * {@link Stream#dropWhile(Predicate)}.
   * <p>
   * Java 8 added the {@link Stream#skip(long)} method to support discarding the first 'n' elements from a stream.
   * However, this only supports cases where the no. of elements to skip is known ahead of time. In Java 9, the new
   * {@link Stream#dropWhile(Predicate)} method allows you to provide your own predicate for deciding when to stop
   * discarding and start processing elements from the Stream.
   * <p>
   * Note - This method is typically only used on an ordered Stream. If used on an unordered Stream you may end up
   * processing (not dropping) elements because the predicate only has to be satisfied once.
   *
   * @see #takeWhile()
   */
  @Test
  public void dropWhile() {
    // Problem - Given a list of Payments, ordered by amount, select only those  with an amount greater or equal to 500
    final List<Payment> payments = List.of(new Payment(new BigDecimal("100.00")), new Payment(new BigDecimal("250.50")),
      new Payment(new BigDecimal("499.99")), new Payment(new BigDecimal("500.00")),
      new Payment(new BigDecimal("600.00")));

    // J8 solution - Can be solved using Stream.filter() to apply the predicate (which also supports unordered
    // Streams), but is less efficient then the new J9 solution below
    List<Payment> smallPayments = payments.stream()
      .filter(p -> p.getAmount().compareTo(new BigDecimal("500.00")) >= 0)
      .collect(Collectors.toList());

    assertThat(smallPayments).containsExactly(
      new Payment(new BigDecimal("500.00")), new Payment(new BigDecimal("600.00")));

    // J9 solution - Uses new Stream.dropWhile() method. When used on an ordered stream this is more efficient than
    // using Stream.filter() as it doesn't require every element in the stream to be processed - processing of the
    // Stream is short-circuited when the predicate is satisfied / condition is met.
    smallPayments = payments.stream()
      .dropWhile(p -> p.getAmount().compareTo(new BigDecimal("500.00")) < 0)
      .collect(Collectors.toList());

    assertThat(smallPayments).containsExactly(
      new Payment(new BigDecimal("500.00")), new Payment(new BigDecimal("600.00")));
  }

  class Payment {
    private final BigDecimal amount;

    Payment(BigDecimal amount) {
      this.amount = amount;
    }

    public BigDecimal getAmount() {
      return this.amount;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Payment payment = (Payment) o;

      return amount.equals(payment.amount);
    }

    @Override
    public int hashCode() {
      return amount.hashCode();
    }

    @Override
    public String toString() {
      return "Payment{" +
        "amount=" + amount +
        '}';
    }
  }

  /**
   * In J9 the Stream API has been extended to more easily handle null values in streams. A new static method
   * {@link Stream#ofNullable(Object)} returns either a Stream containing a single element, if it's non-null, or an
   * empty Stream, avoiding the need for conditional code to do the same.
   * <p>
   * This test case provides an example of using {@link Stream#ofNullable(Object)} to simplify code that uses the
   * Stream API to find the first System property that exists in a list of System properties.
   */
  @Test
  public void ofNullable() {
    final String nonExistentSystemProperty = "my.home." + Instant.now().toString();
    final String[] userHomeSystemPropertyNames = new String[]{nonExistentSystemProperty, "user.home"};

    // First an illustration that using Stream.map(Function).findFirst() can't be used to solve this problem because
    // Stream.findFirst() can't deal with null elements -
    try {
      Stream.of(userHomeSystemPropertyNames)
        .map(System::getProperty)
        .findFirst();
      throw new RuntimeException("Test failed. Expected Stream.findFirst() to throw NullPointerException");
    } catch (NullPointerException e) {
      // expected
    }

    // J8 solution -
    // Convert the values of property, including those which are null because they don't exist, to a non-null Stream,
    // and use Stream.flatMap() to reduce them back to a single Stream of values, then apply findFirst()
    Optional<String> userHomePathString = Stream.of(userHomeSystemPropertyNames)
      .flatMap(propertyName -> {
        String value = System.getProperty(propertyName);
        // Code to handle the coercion of a null amount to an empty stream
        if (value != null) {
          return Stream.of(value);
        } else {
          return Stream.empty();
        }
      })
      .findFirst();

    assertThat(userHomePathString.orElseThrow(() ->
      new RuntimeException("Test failed. System property 'user.home' not set.")))
        .isEqualTo(System.getProperty("user.home"));

    // Equivalent J9 solution -
    // Same solution but simplified by using Stream.ofNullable()
    userHomePathString = Stream.of(userHomeSystemPropertyNames)
      .flatMap(propertyName -> Stream.ofNullable(System.getProperty(propertyName)))
      .findFirst();

    assertThat(userHomePathString.orElseThrow(() ->
      new RuntimeException("Test failed. System property 'user.home' not set.")))
      .isEqualTo(System.getProperty("user.home"));
  }

  /**
   * In Java 9, a new Stream {@link Collector} has been provided that supports applying a filter operation on elements
   * at collection time. This is useful if you need to apply a group-by (aggregating) operation on the Stream
   * _before_ the filter. The new Collector is created using factory method
   * {@link Collectors#filtering(Predicate, Collector)}.
   * <p>
   * This new Collector comes in useful in similar scenarios to those for applying a flat-map operation at
   * collection time, after a group-by operation, as described in test {@link #flatMappingCollector()}.
   */
  @Test
  public void filteringCollector() {
    // Example requirement - Generate a report of Expenses, grouped by month, which details only Expenses of 1k or more.
    final List<Expense> expenses = List.of(
      new Expense(1, "breakfast", new BigDecimal("4.50"), Month.JANUARY),
      new Expense(2, "out flight", new BigDecimal("1500.00"), Month.FEBRUARY),
      new Expense(3, "return flight", new BigDecimal("1000.00"), Month.MARCH),
      new Expense(4, "out flight", new BigDecimal("999.99"), Month.FEBRUARY),
      new Expense(5, "return flight", new BigDecimal("1250.00"), Month.MARCH)
    );

    // J8 solution - Use the Stream API to first filter the list of expenses by amount, and then group by month.
    // This doesn't satisfy the requirement because months without expenses exceeding 1k aren't included in the report
    Map<Month, List<Expense>> monthlyLargeExpenseReport = expenses.stream()
      .filter(expense -> expense.getAmount().compareTo(new BigDecimal("1000.00")) >= 0)
      .collect(groupingBy(Expense::getMonth));

    assertThat(monthlyLargeExpenseReport).containsOnly(
      // Report should ideally contain an empty list for January
      entry(Month.FEBRUARY, List.of(expenses.get(1))),
      entry(Month.MARCH, List.of(expenses.get(2), expenses.get(4)))
    );

    // J9 solution - Group/aggregate all the expenses by month first, then filter each group after, at collection
    // time using new Collector
    monthlyLargeExpenseReport = expenses.stream().collect(
        groupingBy(Expense::getMonth,
        // Apply filter on collection, after have grouped
        Collectors.filtering(expense -> expense.getAmount().compareTo(new BigDecimal("1000.00")) >= 0, toList()))
    );

    assertThat(monthlyLargeExpenseReport).containsOnly(
      entry(Month.JANUARY, Collections.emptyList()),
      entry(Month.FEBRUARY, List.of(expenses.get(1))),
      entry(Month.MARCH, List.of(expenses.get(2), expenses.get(4)))
    );
  }

  /**
   * In Java 9, a new Stream {@link Collector} (reduction operation) has been provided that supports applying a
   * flat-map operation on element at collection time. This is useful if you need to apply a group-by (aggregating)
   * operation on the Stream _before_ the flat-map. The new Collector is created using factory method
   * {@link Collectors#flatMapping(Function, Collector)}.
   * <p>
   * This new Collector comes in useful in similar scenarios to those for applying a filtering operation at
   * collection time, after a group-by operation, as described in test {@link #filteringCollector()}.
   */
  @Test
  public void flatMappingCollector() {
    // Example requirement - Generate a report of Expenses, grouped by month, which contains only the unique tags
    final List<Expense> expenses = List.of(
      new Expense(1, "breakfast", new BigDecimal("4.50"), Month.JANUARY, List.of("food")),
      new Expense(2, "out flight", new BigDecimal("1500.00"), Month.JANUARY, List.of("travel")),
      new Expense(3, "dinner", new BigDecimal("50.00"), Month.JANUARY, List.of("food")),
      new Expense(4, "return flight", new BigDecimal("1000.00"), Month.JANUARY, List.of("travel")),
      new Expense(5, "breakfast", new BigDecimal("5.75"), Month.FEBRUARY, List.of("food")),
      new Expense(6, "lunch", new BigDecimal("5.75"), Month.FEBRUARY, List.of("food")),
      new Expense(7, "theatre", new BigDecimal("150.00"), Month.FEBRUARY, List.of("entertainment"))
    );

    // J8 solution, attempt 1 - Produce a set of unique tags across the expenses by streaming over the expenses,
    // applying flat-map to convert each expense's list of tags into a combined list, then de-duplicating the tags
    // across expenses by collecting into a set. Doesn't satisfy the requirement as only produces a set of tags, with no
    // grouping by month.
    final Set<String> setOfTags = expenses.stream()
      .flatMap(expense -> expense.getTags().stream()) // flatMap() has to return a stream, so need to add stream()
      .collect(toSet());
    assertThat(setOfTags).containsOnly("food", "travel", "entertainment");

    // J8 solution, attempt 2 - Produce a collection of tags grouped by month. This can be achieved by using the
    // Stream API to first group/aggregate all the expenses by month first, and subsequently applying the function to
    // convert each expense to tags at Collection time. However, in Java 8 the Stream API only supports applying a
    // map() operation at Collection time, and not a flatMap(). so the result is a set of list of tags, rather than
    // set of tags, and each list may contain duplicates, which doesn't satisfy the requirement.
    Map<Month, Set<List<String>>> monthlyTaggedExpensesReport1 = expenses.stream().collect(
      groupingBy(Expense::getMonth, Collectors.mapping(Expense::getTags, toSet()))
    );
    assertThat(monthlyTaggedExpensesReport1).containsOnly(
      entry(Month.JANUARY, Set.of(List.of("food"),List.of("travel"))),
      entry(Month.FEBRUARY, Set.of(List.of("food"),List.of("entertainment")))
    );

    // J9 solution - Use new Collectors.flatMapping() to apply flatMap() at Collection time, in this case after grouping
    Map<Month, Set<String>> monthlyTaggedExpensesReport2 = expenses.stream().collect(
      groupingBy(Expense::getMonth, Collectors.flatMapping(expense -> expense.getTags().stream(), toSet()))
    );
    assertThat(monthlyTaggedExpensesReport2).containsOnly(
      entry(Month.JANUARY, Set.of("food", "travel")),
      entry(Month.FEBRUARY, Set.of("food", "entertainment"))
    );
  }
}

class Expense {
  private final int id;
  private final String description;
  private final BigDecimal amount;
  private final Month month;
  private List<String> tags = new ArrayList<>();

  Expense(int id, String description, BigDecimal amount, Month month) {
    this.id = id;
    this.description = description;
    this.amount = amount;
    this.month = month;
  }

  public Expense(int id, String description, BigDecimal amount, Month month, List<String> tags) {
    this(id, description, amount, month);
    if(tags !=null) {
      this.tags = tags;
    }
  }

  public BigDecimal getAmount() {
    return this.amount;
  }

  public Month getMonth() {
    return this.month;
  }

  public List<String> getTags() {
    return this.tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  @Override
  public String toString() {
    return "Expense{" +
      "id=" + id +
      ", description='" + description + '\'' +
      ", amount=" + amount +
      ", month=" + month +
      ", tags=" + tags +
      '}';
  }
}