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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.IntStream;

/**
 * A JUnit (5) test case providing examples of the standard implementation of Reactive Streams APIs in Java 9.
 * <p>
 * <a href="http://www.reactive-streams.org/">Reactive Streams</a> is an initiative to define a standard for the async
 * exchange of data between components within an application, with the aim of making more efficient use of resources,
 * by avoiding blocking (waiting on I/O), and with support for flow-control between the data producer and consumer
 * (referred to as 'back-pressure'). It resulted in a specification and a set of abstract programming APIs for each
 * supported development platform, including Java, in the form of small no. of interfaces. Subsequent implementations
 * of the Reactive Streams spec. each shipped with their own, duplicate set of these interfaces. Java 9 now provides a
 * standard set of the Reactive interfaces as part of the Java language in the {@link java.util.concurrent.Flow} class.
 * Additionally, Java 9 also provides a powerful implementation of the reactive
 * {@link java.util.concurrent.Flow.Publisher} interface, in the form of {@link SubmissionPublisher}.
 * <p>
 * This class contains a couple of examples of how to process data in a reactive fashion using the new Java 9 version
 * of the reactive interfaces.
 * <p>
 * <strong>Note</strong> - The Reactive Streams APIs are primarily designed for use by vendors / providers of products
 * such a web-servers, data-stores, etc, rather than application developers. Application developers will instead
 * typically build on top of reactive frameworks that use these products.
 */
// Declare class and test methods as public to support selectively documenting them using Javadoc.
public class ReactiveStreamsApiTest {

  /**
   * Provides a toy example of a reactive Publisher and Subscriber, which implement Java's standard version of the
   * reactive APIs - {@link java.util.concurrent.Flow.Publisher} and {@link java.util.concurrent.Flow.Subscriber}
   * - working together to produce and consume items, in a fully asynchronous, non-blocking manner, with flow-control.
   *
   * @throws Exception if an unexpected Exception occurs on execution of this test.
   */
  @Test
  public void testSimpleReactivePublisherAndSubscriber() throws Exception {
    // Create an instance of reactive Publisher, using Java's out-of-the-box SubmissionPublisher implementation, that
    // publishes a stream of integers
    SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();

    // Subscribe an instance of a custom, toy implementation of a reactive Subscriber to the Publisher
    publisher.subscribe(new LoggingSubscriber());

    // Submit some items to the Publisher to publish, asynchronously, when the Subscriber can accept them
    IntStream.rangeClosed(1, 10).forEachOrdered(publisher::submit);

    // Pause main thread to allow Publisher’s submit method to complete submissions to Subscribers, asynchronously
    Thread.sleep(1000);

    // Close publisher - notifies Subscriber subscription complete
    publisher.close();
  }

  /**
   * Provides a toy example of using a reactive Processor, which implements Java's standard version of the reactive
   * Processor API - {@link java.util.concurrent.Flow.Processor}, to intercept an item published by an upstream
   * reactive Publish, process (e.g. adapt, enrich or convert) it, before (re) publishing it to downstream reactive
   * Subscriber(s).
   *
   * @throws Exception if an unexpected Exception occurs on execution of this test.
   */
  @Test
  public void testSimpleReactiveProcessor() throws Exception {
    // Create an instance of reactive Publisher, using Java's out-of-the-box SubmissionPublisher implementation, that
    // publishes a stream of integers
    SubmissionPublisher<Integer> publisher = new SubmissionPublisher<>();

    // Create instance of Processor that adds a specified amount to received items & subscribe it to upstream Publisher
    AddingProcessor processor = new AddingProcessor(10);
    publisher.subscribe(processor);

    // Subscribe instance of a custom, toy implementation of a reactive Subscriber to the Processor (also a Publisher)
    processor.subscribe(new LoggingSubscriber());

    IntStream.rangeClosed(1, 10).forEachOrdered(publisher::submit);

    // Pause main thread to allow Publisher’s submit method to complete submissions to Subscribers, asynchronously
    Thread.sleep(1000);

    // Close publisher - notifies Subscriber subscription complete
    publisher.close();
  }
}

/**
 * A toy example implementation of Java's {@link Flow.Subscriber reactive Subscriber interface} that is capable of being
 * subscribed to and processing a stream of integers, and logs them on receipt.
 */
class LoggingSubscriber implements Flow.Subscriber<Integer> {

  private static Logger logger = LoggerFactory.getLogger(LoggingSubscriber.class);

  private Flow.Subscription subscription;

  /**
   * {@inheritDoc}
   * <p>
   * This simple implementation of this callback just requests a single item from the Publisher, via the newly
   * confirmed {@code subscription}.
   */
  @Override
  public void onSubscribe(Flow.Subscription subscription) {
    this.subscription = subscription;
    subscription.request(1); // request 1 new item from the upstream Publisher when available
  }

  /**
   * {@inheritDoc}
   * <p>
   * This simple implementation of this callback just logs the received item before always requesting another single
   * item.
   */
  @Override
  public void onNext(Integer item) {
    logger.info("onNext() - Received item from Publisher [{}].", item);
    subscription.request(1); // request another item from the upstream Publisher when available
  }

  @Override
  public void onError(Throwable error) {
    logger.error("onError() - Publisher reported error [{}].", error.toString(), error);
  }

  @Override
  public void onComplete() {
    logger.info("onComplete() - Publisher reported data stream complete.");
  }
}

/**
 * A toy example implementation of Java's {@link Flow.Processor reactive Processor interface} that is capable of
 * intercepting, processing and re-publishing a stream of integers. Adds a specified amount to the value of each item
 * before re-publishing them to Subscribers.
 * <p>
 * Subclasses Java's out-of-the-box {@link SubmissionPublisher} to provide the implementation of the reactive
 * Publisher, supporting re-publishing of the items to Subscribers.
 */
class AddingProcessor extends SubmissionPublisher<Integer> implements Flow.Subscriber<Integer> {

  private static Logger logger = LoggerFactory.getLogger(AddingProcessor.class);

  private final int amount;
  private Flow.Subscription subscription;

  /**
   * @param amount the amount to be added to each item that is received from the upstream Publisher.
   */
  AddingProcessor(int amount) {
    super();
    this.amount = amount;
  }

  /**
   * {@inheritDoc}
   * <p>
   * This simple implementation of this callback just requests a single item from the upstream Publisher, via the newly
   * confirmed {@code subscription}.
   */
  @Override
  public void onSubscribe(Flow.Subscription subscription) {
    this.subscription = subscription;
    subscription.request(1);  // request 1 new item from the upstream Publisher when available
  }

  /**
   * {@inheritDoc}
   * <p>
   * This simple implementation of this callback just adds the previously specified amount to the received item
   * and publishes it to the downstream Subscribers, before always requesting another single item from the upstream
   * Publisher.
   */
  @Override
  public void onNext(Integer item) {
    // Increment the value of the received item by the amount, and publish it to downstream Subscribers
    super.submit(item + this.amount);
    subscription.request(1); // request another item from the upstream Publisher when available
  }

  @Override
  public void onError(Throwable error) {
    logger.error("onError() - Publisher reported error [{}].", error.toString(), error);
    // If an error occurs that prevents publishing more items to a stream, a Publisher must (async) notify Subscribers
    super.closeExceptionally(error);
  }

  @Override
  public void onComplete() {
    logger.info("onComplete() - Publisher reported data stream complete.");
    // If all the items in the stream have been published, a Publisher must (async) notify Subscribers
    super.close();
  }
}