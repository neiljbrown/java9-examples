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

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A JUnit (5) test case providing examples of the enhancements that have been made to the Process API in Java 9.
 * <p>
 * Java 9 has been extended to make it easier to query (find and list) and obtain info about native processes. The
 * support is provided by the new {@link ProcessHandle} interface. Some of these new features have also been exposed
 * by adding new methods to the existing {@link Process} (abstract) class.
 * <p>
 * As detailed in its Javadoc, the new {@link ProcessHandle} interface identifies and provides control of native
 * processes. Each individual process can be monitored to see if it's still alive, list its children, get information
 * about the process or destroy it. (Instances of {@link Process} continue to be used to additionally provide access
 * to the input, output, and error streams of a process).
 * <p>
 * One of the benefits of these enhancements is that is that it will encourage building tools in Java for process
 * management e.g. for application monitoring/management and deployment.
 *
 * <h2>Querying Processes</h2>
 * The new support for querying native processes makes it easier to find a (e.g. current, parent, child or any) process
 * (handle) and obtain info about it, such as its PID, name, arguments, start time and CPU usage, etc.
 *
 * <h2>Managing / Controlling Processes</h2>
 * New support in Java 9 for managing / controlling processes is limited to providing the ability to register functions
 * or actions to execute synchronously or asynchronously upon process termination (see
 * {@link ProcessHandle#onExit() and equivalent {@link Process#onExit()}}.
 * <p>
 * The new ProcessHandle interface also supports killing processes using {@link ProcessHandle#destroy()} and
 * {@link ProcessHandle#destroyForcibly()}, however, these methods were already provided by {@link Process} in Java 8.
 * <p>
 * The {@link ProcessBuilder} class, added in Java 5, continues to be the best way to create/run new processes. Java 9
 * has added one new method to this class which supports chaining together the execution of a series of processes,
 * linking them by their standard output and input streams. See {@link ProcessBuilder#startPipeline(List)}.
 */
// Declare class and test methods as public to support selectively documenting them using Javadoc.
public class ProcessApiTest {

  /**
   * Provides an example of how to use the new {@link ProcessHandle} interface to discover the PID of a process.
   * <p>
   * This example uses the factory method {@link ProcessHandle#current()} to get the ProcessHandle for the current
   * process. There are other factory methods, including {@link ProcessHandle#of(long)} which supports obtaining the
   * ProcessHandle of any process identified by PID, if it exists, and {@link ProcessHandle#parent()} which supports
   * obtaining the ProcessHandle of the parent process, if there is one.
   */
  @Test
  public void getCurrentPid() {
    // Use ProcessHandle factory method to obtain a ProcessHandle for the current process
    final ProcessHandle processHandle = ProcessHandle.current();
    final long pid = processHandle.pid();

    assertThat(pid).isGreaterThan(0);
  }

  /**
   * Provides an example of the new support that Java 9 provides for retrieving information about a process, such as
   * the name of the user account under which it is running, the command that was used to launch it, its arguments,
   * start time and execution time to date.
   * <p>
   * The information is retrieved from the new {@link ProcessHandle.Info} sub-interface. All of the process
   * attributes it returns are of type {@link Optional} to allow for the requesting process not having the operating
   * system permission to access them.
   */
  @Test
  public void getCurrentProcessInfo() {
    // Get process info for current process
    final ProcessHandle.Info processInfo = ProcessHandle.current().info();

    // Name of user account under which process is running
    final Optional<String> optionalUser = processInfo.user();
    optionalUser.ifPresent(user -> assertThat(user).isEqualTo(System.getProperty("user.name")));

    // Path of command used to launch process (aka 'executable pathname')
    final Optional<String> optionalCommand = processInfo.command();
    optionalCommand.ifPresent(command -> assertThat(command).containsIgnoringCase("java"));

    // Arguments supplied on launching process
    final Optional<String[]> optionalArgs = processInfo.arguments();
    if (optionalArgs.isPresent()) {
      final Optional<String> junitArg =
        Stream.of(optionalArgs.get()).filter(arg -> arg.matches("(?i).*java.*")).findFirst();
      assertThat(junitArg).isPresent();
    }

    // Start time of process
    final Optional<Instant> optionalInstant = processInfo.startInstant();
    optionalInstant.ifPresent(instant -> assertThat(instant).isBefore(Instant.now()));

    // CPU time to date
    final Optional<Duration> optionalCpuDuration = processInfo.totalCpuDuration();
    optionalCpuDuration.ifPresent(cpuDuration -> assertThat(cpuDuration).isGreaterThanOrEqualTo(Duration.ZERO));
  }

  /**
   * Provides an example of how the new {@link ProcessHandle} interface's {@link ProcessHandle#allProcesses()}
   * method can be used to list all of existing processes (or at least those visible to the current process).
   * <p>
   * In this example, a list of the 'n' most recently started processes is queried and sorted.
   */
  @Test
  public void queryMostRecentlyStartedProcesses() {
    // Get a stream over all the current processes (at least those visible to the current process)
    final Stream<ProcessHandle> allProcesses = ProcessHandle.allProcesses();

    final int querySize = 5;
    final List<ProcessHandle> processHandles =  allProcesses
      // Sort them according to their start time, in reverse order, i.e. most recently started first. (Reverse order
      // requires providing our own Comparator rather than using simpler Comparator.comparing() method to generate it)
      .sorted((handle1, handle2) -> {
        Instant p1StartTime = handle1.info().startInstant().orElse(Instant.EPOCH);
        Instant p2StartTime = handle2.info().startInstant().orElse(Instant.EPOCH);
        return p2StartTime.compareTo(p1StartTime);
      })
      .limit(querySize) // We're only interested in the 'n' most recent
      // Reverse the order again, so the 'n' most recent are in ascending PID order, i.e. oldest first, P1, P2, ...
      .sorted(Comparator.comparing(ProcessHandle::pid))
      .collect(Collectors.toList());

    assertThat(processHandles).size().isBetween(2,querySize); // Must be 2 processes - parent and this one - at min
    // Prove the ('n' most recently started) processes are in start time ascending order
    IntStream.range(0, processHandles.size() - 1)
      .forEachOrdered(i -> {
        final Instant instant1 = getMandatoryProcessHandleStartTime(processHandles.get(i));
        final Instant instant2 = getMandatoryProcessHandleStartTime(processHandles.get(i+1));
        assertThat(instant1).isBeforeOrEqualTo(instant2);
      }
    );
  }

  private Instant getMandatoryProcessHandleStartTime(ProcessHandle handle) {
    return handle.info().startInstant().orElseThrow(() ->
      new RuntimeException("ProcessHandle's start time was unexpectedly null."));
  }
}
