# Mojarra Developer Guide

Instructions for checking out, building, testing, and contributing to Mojarra.

## Building

In case you want to checkout this repository and manually build from source yourself (if necessary after editing source code), here are the instructions:

### Mojarra 4.1

1. Make sure that you have JDK 17 and Maven installed.
2. Checkout current branch.
3. Run the following commands from the `impl` directory of the project:

    ```bash
    # under the impl dir of project
    mvn clean install
    ```

4. The binary is now available as `target/jakarta.faces-4.1.x-SNAPSHOT.jar`.

### Mojarra 4.0

1. Make sure that you have JDK 11 and Maven installed.
2. Checkout 4.0 branch.
3. Run the following commands from the `impl` directory of the project:

    ```bash
    # under the impl dir of project
    mvn clean install
    ```

4. The binary is now available as `target/jakarta.faces-4.0.x-SNAPSHOT.jar`.

### Mojarra 3.0 and older

Mojarra 3.0 and older are no longer maintained by Eclipse. If such support is needed, consult your Jakarta EE vendor of choice.

## Editing source code with IDE

In case you want to checkout to edit the source code of Mojarra with full IDE support, here are the instructions. Note that this only allows you to *edit* the code. Actually building the Mojarra artefacts still has to be done using the instructions provided above.

### Eclipse

1. Checkout the desired branch using File -> Import -> Git
2. Right click the Mojarra project after checkout, choose Configure -> Convert to Maven Project

## Testing

API-specific unit and integration tests go to the `tck` module of the [Faces project](https://github.com/jakartaee/faces/tree/main/tck).
The TCK doesn't accept new tests for an already released version, so API-specific tests for the current version stay here until the next version.
Impl-specific unit tests live in the `impl` module, impl-specific integration tests in the `test` module.

### Unit tests

Run the following command from the `impl` directory of the project:

```bash
# under the impl dir of project
mvn clean test
```

This runs the Java unit tests as well as the Jest tests of `faces.js` in `impl/src/test/ts`.

### Integration tests

1. Make sure that you have JDK 21 and Google Chrome installed. GlassFish needs JDK 21 and the tests drive Chrome headless.
2. The tests deploy a WAR on a real server which picks up the impl from the local Maven repository, so install it first:

    ```bash
    # under the impl dir of project
    mvn clean install
    ```

3. Run the following command from the `test` directory of the project:

    ```bash
    # under the test dir of project
    mvn clean verify
    ```

This runs on GlassFish. Use `-P wildfly`, `-P tomee`, `-P payara`, `-P liberty` or `-P tomcat` to run on another server.
