# Mojarra Developer Guide

Instructions for checking out, building, and contributing to Mojarra.

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
