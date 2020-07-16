Infinispan Server Test Driver Demo
==================================

This project is a demo for the Infinispan Server Test Driver.
It shows how to start a cluster of Infinispan Servers, each one running in its own forked process and then a number of
tests in which clients interact with the cluster.

First-off, you need to include the `org.infinispan:infinispan-server-testdriver-junit4` dependency to your project. For example, in Maven, add the following to your dependencies:

```xml
<dependency>
    <groupId>org.infinispan</groupId>
    <artifactId>infinispan-server-testdriver-junit4</artifactId>
    <version>${infinispan.version}</version>
</dependency>
```
  
##### How to run 
`org.infinispan.test.server.home` Specifies a comma-separated list to server home path. Only for `FORKED` driver

`mvn clean test -Dorg.infinispan.test.server.home=/path/to/ispn-server-home1,/path/to/ispn-server-home2`

Or

Run `sh run-tests.sh`