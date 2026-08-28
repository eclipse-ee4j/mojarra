# Mojarra Developer Guide

Instructions for checking out, building, and contributing to Mojarra.

## Building

In case you want to checkout this repository and manually build from source yourself (if necessary after editing source code), here are the instructions:

### Mojarra 5.0

1. Make sure that you have JDK 17 and Maven installed.
2. Make sure that git is configured to keep submodules in sync:

    ```bash
    git config --global submodule.recurse true
    ```

    This covers every future `git pull`, `git checkout` and `git switch`, but not `git clone`, so the initial checkout in the next step needs its own flag.

3. Checkout branch [`5.0`][28], including submodules:

    ```bash
    git clone --recurse-submodules --branch 5.0 https://github.com/eclipse-ee4j/mojarra.git
    ```

    In an existing clone, run `git submodule update --init` instead.

4. Run the following commands from the `impl` directory of the project:

    ```bash
    # under the impl dir of project
    mvn clean install
    ```

5. The binary is now available as `target/mojarra-5.x.x-SNAPSHOT.jar`.

Note that since 5.0 the API part is split into [Faces project](https://github.com/jakartaee/faces/tree/5.0), which is wired in as the `faces` git submodule.
Its sources are needed at build time, among others for the TypeScript definitions of `faces.js`, so a clone without submodules will fail to build.

### Mojarra 4.1

1. Make sure that you have JDK 17 and Maven installed.
2. Checkout branch [`main`][32].
3. Run the following commands from the `impl` directory of the project:

    ```bash
    # under the impl dir of project
    mvn clean install
    ```

4. The binary is now available as `target/jakarta.faces-4.1.x-SNAPSHOT.jar`.

### Mojarra 4.0

1. Make sure that you have JDK 11 and Maven installed.
2. Checkout branch [`4.0`][31].
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

1. Checkout the desired branch ([`5.0`][28], [`main`][32], or [`4.0`][31]) using File -> Import -> Git
2. Right click the Mojarra project after checkout, choose Configure -> Convert to Maven Project

On [`5.0`][28], make sure that `submodule.recurse` is set as explained above and that the `faces` submodule is populated, else the build will fail.
Eclipse does not fetch submodules during import, so run `git submodule update --init` in the checkout afterwards, or clone from the command line instead.

## Pull Requests

Pull requests are accepted on following branches:

- [`5.0`][28] (5.0.x)
- [`main`][32] (4.1.x)
- [`4.0`][31] (4.0.x)

Note that it's okay to send a PR to the `5.0` branch, but that one is for the next major version and not the rolling 4.1.x or 4.0.x version.


  [28]: https://github.com/eclipse-ee4j/mojarra/tree/5.0
  [31]: https://github.com/eclipse-ee4j/mojarra/tree/4.0
  [32]: https://github.com/eclipse-ee4j/mojarra/tree/main
