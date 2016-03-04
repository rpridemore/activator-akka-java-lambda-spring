Forked from Typesafe Activator template for Akka and Spring in Java.

I have made the following changes:
* modify build.sbt to support java 8 (required updating spring dependency to latest)
* refactor SpringExtension class to more idiomatic java/spring
* add new CountingLambdaActor to perform same function as CountingActor, but using new lambda api

