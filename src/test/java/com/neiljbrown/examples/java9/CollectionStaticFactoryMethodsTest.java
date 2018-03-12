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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * A JUnit (5) test case providing examples of how some of the main collection classes have been enhanced to provide
 * static factory methods to simplify the code for creating <i>immutable</i> collections.
 * <p>
 * Note, that these new factory methods are <i>not</i> defined on the {@link java.util.Collection} interface, or part
 * of the {@link java.util.Collections} utility class, but have rather been retrofitted on selective collection
 * interfaces - {@link java.util.List}, {@link java.util.Set} and {@link java.util.Map}. This has been achieved
 * without breaking backwards compatibility by providing 'default’ method implementations in List, Set and Map
 * interfaces.
 * <p>
 * There are no. of overloaded versions of these static factory methods each accepting an increasing no. of collection
 * elements, plus a varargs version of the method supporting an arbitrary no. of elements.
 */
// Declare class and test methods as public to support selectively documenting them using Javadoc.
public class CollectionStaticFactoryMethodsTest {

  /**
   * Provides an example of how to use one of J9's static factory methods to simplify the code for creating an
   * immutable List. This example uses factory method {@link java.util.List#of(Object, Object, Object)}.
   * <p>
   * There are several overloaded static factory methods taking a variable no. of elements from zero to 10. For more
   * than 10 elements there is also a method which takes a vararg. For more details see
   * <a href="https://docs.oracle.com/javase/9/docs/api/java/util/List.html#immutable">Immutable Set Static Factory
   * Methods</a>.
   */
  @Test
  public void createImmutableList() {
    // J8 code - typical verbosity
    List<String> letters = new ArrayList<>();
    letters.add("a");
    letters.add("b");
    letters.add("c");
    letters = Collections.unmodifiableList(letters);

    assertThat(letters).containsOnly("a", "b", "c");

    // J8 code - terser option using Arrays.asList()...
    letters = Arrays.asList("a", "b", "c");

    assertThat(letters).containsOnly("a", "b", "c");
    // ...but the created class of List although a fixed-size isn't truly immutable, as existing elements can be changed
    letters.set(2, "d");
    assertThat(letters).containsOnly("a", "b", "d");

    // J9 code
    final List<String> moreLetters = List.of("a", "b", "c");

    assertThat(moreLetters).containsOnly("a", "b", "c");
    // And list is truly immutable - you can't change the existing element values either
    assertThatThrownBy(() -> {
      moreLetters.set(2,"d");
    }).isInstanceOf(UnsupportedOperationException.class);
  }

  /**
   * Provides an example of how to use one of J9's static factory methods to simplify the code for creating an
   * immutable Set. This example uses factory method {@link java.util.Set#of(Object, Object, Object)}.
   * <p>
   * There are several overloaded static factory methods taking a variable no. of elements from zero to 10. For more
   * than 10 elements there is also a method which takes a vararg. For more details see
   * <a href="https://docs.oracle.com/javase/9/docs/api/java/util/Set.html#immutable">Immutable Set Static Factory
   * Methods</a>.
   */
  @Test
  public void createImmutableSet() {
    // J8 code - Creating collections from values is verbose requiring multiple, separate statements to
    // create the collection, and one to add each value
    Set<String> letters = new HashSet<>();
    letters.add("a");
    letters.add("b");
    letters.add("c");
    letters = Collections.unmodifiableSet(letters);

    assertThat(letters).containsOnly("a", "b", "c");

    // J9 code
    letters = Set.of("a", "b", "c"); // undefined order

    assertThat(letters).containsOnly("a", "b", "c");
  }

  /**
   * Provides an example of how to use one of J9's static factory methods to simplify the code for creating an
   * immutable Map. This example uses factory method {@link java.util.Map#of(Object, Object, Object, Object)} which
   * creates a map for a sequentially specified set of key and value pairs.
   * <p>
   * There are several overloaded static factory methods taking a variable no. of key and value pairs from zero to 10.
   * For more than 10 elements there is also a method which takes a vararg. For more details see
   * <a href="https://docs.oracle.com/javase/9/docs/api/java/util/Map.html#immutable">Immutable Set Static Factory
   * Methods</a>.
   *
   * @see #createImmutableMapFromEntries
   */
  @Test
  public void createImmutableMapFromKeyValuePairs() {
    // J8 code - typical verbosity
    Map<String, String> letters = new HashMap<>();
    letters.put("a", "A");
    letters.put("b", "B");
    letters.put("c", "C");
    letters = Collections.unmodifiableMap(letters);

    assertThat(letters).contains(entry("a", "A"), entry("b","B"), entry("c", "C"));

    // J9 code - The list of values is a sequentially specified key and value pairs -
    final Map<String, String> moreLetters = Map.of("a", "A", "b", "B", "c", "C");

    assertThat(moreLetters).contains(entry("a", "A"), entry("b","B"), entry("c", "C"));
    // And Map is truly immutable - you can't change add new elements...
    assertThatThrownBy(() -> {
      moreLetters.put("d","D");
    }).isInstanceOf(UnsupportedOperationException.class);
    // ...or change existing elements
    assertThatThrownBy(() -> {
      moreLetters.put("a","a");
    }).isInstanceOf(UnsupportedOperationException.class);
  }

  /**
   * Provides another example of how to use one of J9's static factory methods to simplify the code for creating an
   * immutable Map using alternative factory method {@link Map#ofEntries(Map.Entry[])}. T
   * <p>
   * This version of the factory method is provided to support creating Maps of an arbitrary number of entries. A varag
   * can't be used to support this, as in the List and Set interfaces, as in a Map the key and value pairs can be of
   * different types.
   * <p>
   * There are several overloaded static factory methods taking a variable no. of key and value pairs from zero to 10.
   * For more than 10 elements there is also a method which takes a vararg. For more details see
   * <a href="https://docs.oracle.com/javase/9/docs/api/java/util/Map.html#immutable">Immutable Set Static Factory
   * Methods</a>.
   *
   * @see #createImmutableMapFromKeyValuePairs()
   */
  @Test
  public void createImmutableMapFromEntries() {
    // J8 code - typical verbosity
    Map<String, String> letters = new HashMap<>();
    letters.put("a", "A");
    letters.put("b", "B");
    letters.put("c", "C");
    letters = Collections.unmodifiableMap(letters);

    assertThat(letters).contains(entry("a", "A"), entry("b","B"), entry("c", "C"));

    // J9 code
    final Map<String, String> moreLetters =
      Map.ofEntries(Map.entry("a", "A"), Map.entry("b", "B"), Map.entry("c", "C"));

    assertThat(moreLetters).contains(entry("a", "A"), entry("b","B"), entry("c", "C"));
  }
}