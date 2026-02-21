# HSRT METI DB2: Example PMS

Example code for a minimal "Patient Management System" (PMS), for the course "Databases 2" of the medical informatics (METI)
bachelor curriculum at University of Applied Sciences Reutlingen (Hochschule Reutingen).

This serves to illustrate the following principles:

- Domain Modelling
- Use of domain object generators
- Differences between "classical" imperative and declarative data processing using Streams API
- Dependency Inversion Principle for clean separation of high-level domain logic ("core") from low-level infrastructure details, here for database access via a `Repository` abstraction
- Possibility to provide arbitrary implementations of `Repository`:
  - For RDBMS using JDBC
  - For RDBMS using object/relational mapping with JPA
  - For MongoDB
  - for Neo4j
- Use of event sourcing as alternative persistence model for entity life-cycle
 

## Working with the project

Project-wide:

- Build: `./gradlew compileJava`
- Compile tests: `./gradlew compileTestJava`
- Run Tests: `./gradlew test`

The tasks can also be run for a specific sub-project only: `./gradlew :{sub-project}:{task}`, for instance `./gradlew :generators:test`.

See also: https://docs.gradle.org/current/userguide/building_java_projects.html#introduction

