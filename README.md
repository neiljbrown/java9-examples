#Java 9 Examples

## Purpose
This project provides a set of Java code examples illustrating the new language features and APIs introduced in Java 9. 

The examples are implemented as a set of easy to run tests, using JUnit (5 and AssertJ).

## Overview of New Language Features in Java 9
It's fair to say that there aren't any _big_ new language features for developers to get excited about in 
Java 9, as there were in Java 8 (such as the Stream API and Lambdas). The Java Platform Module System (JPMS) is by 
far the biggest new feature in Java 9, affecting the platform libraries, the language and the runtime. However, beyond 
JPMS, performance improvements, and the new REPL, there are also a number of smaller, new language features introduced
in Java 9 that are still worth Java programmers familiarising themselves with. A list of these less well publicised 
language features is provided below. The ones highlighted (in bold) are those for which code examples are provided in 
this project.

+ **Factory methods for Collections** 
+ **Private methods in interfaces**
+ **java.util.Optional enhancements** 
+ **Stream API enhancements**
+ **Reactive Streams API standard implementation**
+ **Process API enhancements**
+ CompletableFuture enhancements
+ Effectively final variables in try-with-resources blocks
+ UTF-8 Property files
+ Support for the Unicode 7 and 8 standard

## Code
The source code for the examples can be found in the src/test/java folder.

An explanation and guidance on the usage on each of the showcased language features can be found in the Javadoc of 
each of the code examples. (See commands for generating Javadoc below).

## Building and Running the Examples
You will need to install a Java 9 JDK. 

Support is provided for building and running the project using either Gradle (see build.gradle) or Maven 
(see pom.xml). For Gradle, the minimum required version of Gradle will be installed if you use ./gradlew. For Maven, 
ensure you install a version which supports Java 9. The project has been tested to work using Maven 3.5.3+.

### Gradle
To compile and run all the example tests, enter the  following command in the project's root folder:

```./gradlew clean test```

To generate the Javadoc use, the following command: 

```./gradlew clean javadocTests```

The generated Javadoc can be found in the standard location for a Gradle project - build/docs/javadoc/index.html.
This provides an example of the updated Javadoc generated in Java 9, which uses HTML5 and supports searching.

### Maven
To compile and run all the example tests, enter the following command in the project's root folder:

```mvn clean test```

To generate the Javadoc, use the following command: 

```mvn javadoc:test-javadoc```

The generated Javadoc can be found in the standard location for a Maven project - /target/site/testapidocs/index.html.

## Other Examples
You can find similar code examples for the new features introduced in earlier Java versions (e.g. 7 and 8) in my 
Bitbucket projects at [https://bitbucket.org/neilbrown/](https://bitbucket.org/neilbrown/)

---
