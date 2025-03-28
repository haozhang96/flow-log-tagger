# flow-log-tagger

A fully self-contained Java program that analyzes
[AWS flow logs (v2)](https://docs.aws.amazon.com/vpc/latest/userguide/flow-log-records.html) and categorizes and tags
entries based on their IANA-assignedd protocol number and destination port

## Motivation
This program was initially written for a programming assessment with the goals of minimal dependencies and maximum
portability such that it would ideally run on many systems with minimal effort. Over time, I refactored the codebase to
highlight my knowledge of the Java programming language and decision-making within the constraint of simple compilation
and execution. I always prefer using well-tested libraries over reinventing the wheel where possible, but these
constraints limit my usage to only the standard libraries included with the standard Java runtime. Thus, this codebase
does not accurately reflect my software engineering principles, but rather showcase my ability to solve practical
programming challenges under constraints.

## Requirements
* Java (JDK) 21

## Execution
To compile the Java program, run the following command:
```
javac -sourcepath src -d out src/Main.java
```

To run the program using the included sample data, run the following command:
```
java -cp out Main
```

To run the program using your own data, run the following command:
```
java -cp out Main <path to flow log> <path to lookup table> <path to output>
```

To run the program using randomly generated data that simulates a (roughly) `N` MiB flow log file, run the following
command:
```
java -cp out Main <N MiB>
```

In all cases, inspect the `out/output.csv` file for the program's output results.

## Testing
To compile the Java program's unit tests, run the following command:
```
javac -sourcepath src -d out src/UnitTests.java
```

To run the program's unit tests, run the following command:
```
java -cp out UnitTests
```

## Assumptions
* The column orders of all the files are constant.
* The flow log, lookup table, and IANA protocols files are all well-formed according to their file types.
* Only [flow log v2](https://docs.aws.amazon.com/vpc/latest/userguide/flow-log-records.html) files are supported.

## Considerations
* This project was written for standalone compilation and execution on any system meeting the outlined requirements
  without requiring a build tool (e.g., Maven or Gradle) or external libraries.
* Parallel processing was used for faster execution where possible.
* Data streaming was used for memory constraints where possible.
* Caching was used to minimize garbage collection where possible.
* Hash-based comparisons were used for performant lookups where possible.

## Design Choices
* Packages were avoided to simplify compilation and execution from the command line. If you would like to see how I
  organize Java packages in a less constrained environment, check out another one of my Java projects,
  [cxf-camel-services](https://github.com/haozhang96/cxf-camel-services).
* `java.util.Objects.requireNonNull(...)` was used for runtime null-checking to avoid external dependencies required for
  compile-time null-checking, such as external annotations (e.g., `javax.annotation.Nonnull`).
* `java.lang.System.out|err` were used instead of `java.lang.System.Logger` or `java.util.logging.Logger` due to overly
  verbose information logged to the console. However, they have been consolidated into the [Loggers](src/Loggers.java)
  class as functional methods for pluggability.
* Rudimentary column-separator inference was implemented based on a tabular data file's extension and first line. This
  allows common tabular data file formats (e.g., CSV and TSV) to be used without requiring a column separator to be
  provided explicitly.
* A simple `ConcurrentHashMap`-based caching was implemented for [Protocol](src/Protocol.java) to decrease the impact of
  frequent garbage collection caused by large datasets with common data points. There were plans to implement
  soft-reference-value-based caching or an eviction policy, but this would require weighing memory constraints against
  processing time constraints. Currently, we prefer faster processing times.
* A simple testing harness was implemented in [BaseUnitTest](src/BaseUnitTest.java) to facilitate unit testing without
  requiring any testing dependencies.
* Multiple methods where `Stream`s could have been used but ultimately avoided have been chosen so to decrease the
  overhead of `Stream` creations depending on their invocation frequencies.
* `AutoCloseable`-based resources have been documented to help minimize resource leaks using
  [try-with-resources statements](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html).

## Example
[Flow log](res/input.log) (can be headerless)
```tsv
version account-id interface-id srcaddr dstaddr srcport dstport protocol packets bytes start end action log-status
2 123456789012 eni-0a1b2c3d 10.0.1.201 198.51.100.2 443 49153 6 25 20000 1620140761 1620140821 ACCEPT OK
2 123456789012 eni-4d3c2b1a 192.168.1.100 203.0.113.101 23 49154 6 15 12000 1620140761 1620140821 REJECT OK
2 123456789012 eni-5e6f7g8h 192.168.1.101 198.51.100.3 25 49155 6 10 8000 1620140761 1620140821 ACCEPT OK
2 123456789012 eni-9h8g7f6e 172.16.0.100 203.0.113.102 110 49156 6 12 9000 1620140761 1620140821 ACCEPT OK
2 123456789012 eni-7i8j9k0l 172.16.0.101 192.0.2.203 993 49157 6 8 5000 1620140761 1620140821 ACCEPT OK
2 123456789012 eni-6m7n8o9p 10.0.2.200 198.51.100.4 143 49158 6 18 14000 1620140761 1620140821 ACCEPT OK
2 123456789012 eni-1a2b3c4d 192.168.0.1 203.0.113.12 1024 80 6 10 5000 1620140661 1620140721 ACCEPT OK
2 123456789012 eni-1a2b3c4d 203.0.113.12 192.168.0.1 80 1024 6 12 6000 1620140661 1620140721 ACCEPT OK
2 123456789012 eni-1a2b3c4d 10.0.1.102 172.217.7.228 1030 443 6 8 4000 1620140661 1620140721 ACCEPT OK
2 123456789012 eni-5f6g7h8i 10.0.2.103 52.26.198.183 56000 23 6 15 7500 1620140661 1620140721 REJECT OK
2 123456789012 eni-9k10l11m 192.168.1.5 51.15.99.115 49321 25 6 20 10000 1620140661 1620140721 ACCEPT OK
2 123456789012 eni-1a2b3c4d 192.168.1.6 87.250.250.242 49152 110 6 5 2500 1620140661 1620140721 ACCEPT OK
2 123456789012 eni-2d2e2f3g 192.168.2.7 77.88.55.80 49153 993 6 7 3500 1620140661 1620140721 ACCEPT OK
2 123456789012 eni-4h5i6j7k 172.16.0.2 192.0.2.146 49154 143 6 9 4500 1620140661 1620140721 ACCEPT OK
2 123456789012 eni-4h5i6j7k 172.16.0.2 192.0.2.146 49154 22 6 9 4500 1620140661 1620140721 ACCEPT OK
2 123456789012 eni-4h5i6j7k 172.16.0.2 192.0.2.146 49154 22 6 9 4500 1620140661 1620140721 ACCEPT OK
```

[Lookup table](res/lookup_table.csv) (can be headerless)
```csv
dstport,name,tag
25,tcp,sv_P1
68,udp,sv_P2
23,tcp,sv_P1
31,udp,SV_P3
443,tcp,sv_P2
22,tcp,sv_P4
3389,tcp,sv_P5
0,icmp,sv_P5
110,tcp,email
993,tcp,email
143,tcp,email
```

Output
```csv
Tag Counts:
Tag,Count
sv_P2,1
sv_P1,2
sv_P4,2
email,3
Untagged,8

Port/Protocol Combination Counts:
Port,Protocol,Count
993,tcp,1
49158,tcp,1
1024,tcp,1
49156,tcp,1
49157,tcp,1
49154,tcp,1
443,tcp,1
49155,tcp,1
25,tcp,1
22,tcp,2
23,tcp,1
80,tcp,1
110,tcp,1
143,tcp,1
49153,tcp,1
```
