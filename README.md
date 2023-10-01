<!---
[//]: # " Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
[//]: # "  "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Public License v. 2.0, which is available at "
[//]: # " http://www.eclipse.org/legal/epl-2.0. "
[//]: # "  "
[//]: # " This Source Code may also be made available under the following Secondary "
[//]: # " Licenses when the conditions for such availability set forth in the "
[//]: # " Eclipse Public License v. 2.0 are satisfied: GNU General Public License, "
[//]: # " version 2 with the GNU Classpath Exception, which is available at "
[//]: # " https://www.gnu.org/software/classpath/license.html. "
[//]: # "  "
[//]: # " SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 "
-->

# Eclipse Mojarra

Eclipse's implementation of the Jakarta Faces specification

* Mojarra 5.0 - under development
* [Mojarra 4.0](https://github.com/eclipse-ee4j/mojarra/blob/4.0/README.md) - stable release
* [Mojarra 3.0](https://github.com/eclipse-ee4j/mojarra/blob/3.0/README.md) - legacy release

For Mojarra 2.3 and earlier please contact your vendor for support (RedHat, IBM,
Oracle, Omnifish, Payara, etceteras)

## Minimum Requirements

- Java 11
- Jakarta Servlet 6.0
- Jakarta Expression Language 5.0
- CDI 4.0
- Jakarta Standard Tag Library 2.0
- Jakarta Web Socket 2.0 (optional, only when `<f:websocket>` is used)
- Jakarta JSON Processing  2.0 (optional, only when `<f:websocket>` is used)
- Jakarta Validation 3.0 (optional, only when `<f:validateBean>` or `<f:validateWholeBean>` is used)

CDI is explicitly required because since Jakarta Faces 2.3 the `javax.faces.bean.*` annotations such as `@ManagedBean` are deprecated, and in 4.0 these have been removed. Several implicit Jakarta Expression Language objects are produced via CDI producers, and `<f:websocket>` manages the Jakarta WebSocket sessions and events via CDI.


## Installation

Depending on the server used, Jakarta Faces may already be built-in (full fledged Jakarta EE containers such as [WildFly][1], [JBoss EAP][2], [TomEE][3], [Payara][4], [GlassFish][5], [Liberty][6], etc.), or not (barebones Jakarta Server Pages/Jakarta Servlet containers such as [Tomcat][7], [Jetty][8], etc.). If the server doesn't ship with Jakarta Faces built-in, then you need to manually install Jakarta Faces 4.0 along with CDI 4.0+, Jakarta JSON Processing 2.0+ and Jakarta Standard Tag Library 2.0+ as those Jakarta Servlet containers usually also don't even ship with those Jakarta Faces dependencies.


### Non-Maven

In case you're manually carrying around JARs:

- **Jakarta EE containers (WildFly, JBoss EAP, TomEE, Payara, GlassFish, Liberty, etc)**

    You don't need to add any JARs to `/WEB-INF/lib`!

- **Jakarta Servlet containers (Tomcat, Jetty, etc)**

    Add below JARs to `/WEB-INF/lib`:

    - [`jakarta.faces.4.0.x.jar`][9]
    - [`weld-servlet-shaded-4.0.0.Final.jar`][10]
    - [`jstl-2.0.jar`][11]
    - [`jakarta.json-api-2.0.jar`][12] (optional, only when `<f:websocket>` is used)
    - [`jakarta.json-2.0.jar`][12a] (optional, only when `<f:websocket>` is used)
    - [`validation-api-3.0.0.Final.jar`][13] (optional, only when `<f:validateBean|validateWholeBean>` is used)
    - [`hibernate-validator-8.0.x.Final.jar`][14] (optional, only when `<f:validateBean|validateWholeBean>` is used)

    Substitute `x` with latest version number available.

### Maven

In case you're using Maven, you can find below the necessary coordinates:

- **Java EE containers (WildFly, JBoss EAP, TomEE, Payara, GlassFish, Liberty, etc)**

    ```xml
    <dependency>
       <groupId>jakarta.platform</groupId>
       <artifactId>jakarta.jakartaee-api</artifactId>
       <version>10.0.0</version>
       <scope>provided</scope>
    </dependency>
    ```

In case of WildFly/JBoss EAP, [you need to manually package `jsf-api.jar` and `jsf-impl.jar` based on `jakarta.faces.jar` first][15]. In case of TomEE, just swap the `myfaces*.jar` files with `jakarta.faces.jar` in the server's `/lib` folder. In case of Payara/GlassFish, just swap the `jakarta.faces.jar` file in the server's `/glassfish/modules` folder.

- **Servletcontainers (Tomcat, Jetty, etc)**

    ```xml
    <dependency>
        <groupId>org.glassfish</groupId>
        <artifactId>jakarta.faces</artifactId>
        <version><!-- Use latest 4.0.x version. --></version>
    </dependency>
    <dependency>
        <groupId>org.jboss.weld.servlet</groupId>
        <artifactId>weld-servlet-shaded</artifactId>
        <version>4.0.0.Final</version>
    </dependency>
    <dependency>
        <groupId>jakarta.servlet.jsp.jstl</groupId>
        <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
        <version>2.0.0</version>
    </dependency>
    <dependency> <!-- Optional, only when <f:websocket> is used. -->
        <groupId>org.glassfish</groupId>
        <artifactId>jakarta.json</artifactId>
        <version>2.0.0</version>
    </dependency>
    <dependency> <!-- Optional, only when <f:validateBean> or <f:validateWholeBean> is used. -->
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version><!-- Use latest 8.0.x version. --></version>
    </dependency>
    ```

    You can check [`org.glassfish:jakarta.faces`][16] repository to find the latest Mojarra 4.0.x version.


## Testing

Since Mojarra 4, tests have been moved to the [Faces project](https://github.com/jakartaee/faces/tree/master/tck).


## Hello World Example

We assume that you already know how to create an empty Maven WAR Project or Dynamic Web Project in your favourite IDE with a CDI 4.0+ compatible `/WEB-INF/beans.xml` deployment descriptor file (which can be kept fully empty). Don't forget to add JARs or configure pom.xml if necessary, as instructed in previous chapter.

### Controller

Optionally, register the `FacesServlet` in a Servlet 6.0+ compatible deployment descriptor file `/WEB-INF/web.xml` as below:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app
    xmlns="https://jakarta.ee/xml/ns/jakartaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
    version="6.0"
>
    <servlet>
        <servlet-name>facesServlet</servlet-name>
        <servlet-class>jakarta.faces.webapp.FacesServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>facesServlet</servlet-name>
        <url-pattern>*.xhtml</url-pattern>
    </servlet-mapping>
</web-app>
```

Noted should be that Jakarta Faces is already "implicitly" registered and mapped on `*.xhtml`, `*.jsf`, `*.faces` and `/faces/*` when running on a Jakarta Servlet container. This will be overridden altogether when explicitly registering as above. [The `*.xhtml` URL pattern is preferred over above for security and clarity reasons][17]. When you don't explicitly map it on `*.xhtml`, then people can still access Faces pages using `*.jsf`, `*.faces` or `/faces/*` URL patterns. This is not nice for SEO as Faces by design doesn't 301-redirect them to a single mapping.

The Faces deployment descriptor file `/WEB-INF/faces-config.xml` is fully optional.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<faces-config
    xmlns="https://jakarta.ee/xml/ns/jakartaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-facesconfig_4_0.xsd"
    version="4.0"
>
    <!-- Put any faces config here. -->
</faces-config>
```

### Model

Then create a backing bean class as below:

```java
package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class Hello {

    private String name;
    private String message;

    public void createMessage() {
        message = "Hello, " + name + "!";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

}
```

Noted should be that in reality in the average Jakarta EE application the above "model" is further breakdown into a Jakarta Persistence entity, a Jakarta Enterprise Beans service and a smaller backing bean. The Jakarta Persistence entity and Jakarta Enterprise Beans service then basically act as a true "model" and the backing bean becomes a "controller" for that model. This may in first place be confusing to starters, but it all depends on the point of view. See also [What components are MVC in Faces MVC framework?][18] and [Faces Controller, Service and DAO][19].

### View

Finally create a [Facelets][20] file `/hello.xhtml` as below:

```xml
<!DOCTYPE html>
<html lang="en"
    xmlns:f="jakarta.faces.core"
    xmlns:h="jakarta.faces.html">
    <h:head>
        <title>Hello, World!</title>
    </h:head>
    <h:body>
        <h:form>
            <h:outputLabel for="name" value="Enter your name" required="true" />
            <h:inputText id="name" value="#{hello.name}" />
            <h:message for="name" />
            <br />
            <h:commandButton value="Say hello" action="#{hello.createMessage}">
                <f:ajax execute="@form" render="@form" />
            </h:commandButton>
            <br />
            #{hello.message}
        </h:form>
    </h:body>
</html>
```

Start the server and open it by `http://localhost:8080/contextname/hello.xhtml`.

## Activating CDI in Jakarta Faces 4.0

CDI is activated by default in Jakarta Faces 4.0 and can´t be deactivated.  
It´s not required anymore to add `@FacesConfig` to a CDI managed bean to accomplish this.
As of Jakarta Faces 4.0 `@FacesConfig` still removes the need to explicitly add a `FacesServlet` entry to `web.xml`.

## Building

In case you want to checkout this repository and manually build from source yourself (if necessary after editing source code), here are the instructions:

### Jakarta Faces.Next

1. Make sure that you have JDK 11, Ant and Maven installed.
2. Checkout branch [`master`][28].
3. Run the following commands from the root directory of the project:

    ```bash
    # under the root dir of project
    mvn clean install
    ```

4. The binary is now available as `impl/target/jakarta.faces-4.x.x-SNAPSHOT.jar`.



### Jakarta Faces 4.0

1. Make sure that you have JDK 11, Ant and Maven installed.
2. Checkout branch [`4.0`][31].
3. Run the following commands from the root directory of the project:

    ```bash
    # under the root dir of project
    mvn clean install
    ```

4. The binary is now available as `impl/target/jakarta.faces-4.0.x-SNAPSHOT.jar`.

### Jakarta Faces 3.0

1. Make sure that you have JDK 1.8, Ant and Maven installed.
2. Checkout branch [`3.0`][29].
3. Run the following commands from the root directory of the project:

    ```bash
    # under the root dir of project
    mvn clean install
    ```

4. The binary is now available as `impl/target/jakarta.faces-3.0.x-SNAPSHOT.jar`.

### Jakarta Faces 2.3

1. Make sure that you have JDK 1.8, Ant and Maven installed.
2. Checkout branch [`2.3`][30].
3. Run the following commands from the root directory of the project:

    ```bash
    # under the root dir of project
    mvn clean install
    ```

4. The binary is now available as `impl/target/jakarta.faces-2.3.x-SNAPSHOT.jar`.

### Jakarta Faces 2.2

Jakarta Faces 2.2 and lower are not supported by Eclipse. If such support is needed, consult your Jakara EE vendor of choice. 

## Editing source code with IDE

In case you want to checkout to edit the source code of Mojarra with full IDE support, here are the instructions. Note that this only allows you to *edit* the code. Actually building the Mojarra artefacts still has to be done using the instructions provided above.

### Eclipse

#### Jakarta Faces 4.0

1. Checkout branch [`4.0`][29] using File -> import -> Git
2. Right click the Mojarra project after checkout, choose Configure -> Convert to Maven Project

#### Jakarta Faces 3.0

1. Checkout branch [`3.0`][29] using File -> import -> Git
2. Right click the Mojarra project after checkout, choose Configure -> Convert to Maven Project

#### Jakarta Faces 2.3

1. Checkout branch [`2.3`][30] using File -> import -> Git
2. Right click the Mojarra project after checkout, choose Configure -> Convert to Maven Project


## Pull Requests

Pull requests are accepted on following branches:

- [`master`][28] (4.0.x)
- [`3.0`][29] (3.0.x)
- [`2.3`][30] (2.3.x)

Note that it's okay to send a PR to the master branch, but this one is for Faces.next and not the current 2.3.x or 3.0.x version.

## Releasing

pom.xml versions can be adjusted as follows

```
mvn versions:set -DgroupId=* -DartifactId=* -DoldVersion=* -DgenerateBackupPoms=false -DnewVersion=4.0.1-SNAPSHOT
```

## Resources

- [JSF 2.3 Specification (JSR 372)][21]
- [JSF 2.3 API documentation][22]
- [JSF 2.3 VDL documentation][23]
- [JSF 2.3 JS documentation][24]
- [Oracle Java EE 7 tutorial - JavaServer Faces Technology][25] (currently still JSF 2.2)
- [What's new in JSF 2.3?][26]
- [Java EE Kickoff Application][27]


  [1]: http://wildfly.org/
  [2]: https://developers.redhat.com/products/eap/overview/
  [3]: http://tomee.apache.org
  [4]: http://www.payara.fish
  [5]: https://javaee.github.io/glassfish/
  [6]: https://developer.ibm.com/wasdev/websphere-liberty/
  [7]: http://tomcat.apache.org
  [8]: http://www.eclipse.org/jetty/
  [9]: http://central.maven.org/maven2/org/glassfish/javax.faces/
  [10]: https://repo1.maven.org/maven2/org/jboss/weld/servlet/weld-servlet-shaded/4.0.0.Final/weld-servlet-shaded-4.0.0.Final.jar
  [11]: http://central.maven.org/maven2/javax/servlet/jstl/2.0/jstl-2.0.jar
  [12]: https://repo1.maven.org/maven2/jakarta/json/jakarta.json-api/2.0.0/jakarta.json-api-2.0.0.jar
  [12A]: http://central.maven.org/maven2/org/glassfish/javax.json/2.0/javax.json-2.0.jar
  [13]: https://repo1.maven.org/maven2/jakarta/validation/jakarta.validation-api/3.0.0/jakarta.validation-api-3.0.0.jar
  [14]: https://repo1.maven.org/maven2/org/hibernate/validator/hibernate-validator/7.0.0.Final/hibernate-validator-7.0.0.Final.jar
  [15]: https://stackoverflow.com/q/35899887/157882
  [16]: http://mvnrepository.com/artifact/org.glassfish/jakarta.faces
  [17]: https://stackoverflow.com/q/3008395/157882
  [18]: https://stackoverflow.com/q/5104094/157882
  [19]: https://stackoverflow.com/q/30639785/157882
  [20]: http://docs.oracle.com/javaee/7/tutorial/jsf-facelets.htm
  [21]: http://download.oracle.com/otn-pub/jcp/jsf-2_3-final-eval-spec/JSF_2.3.pdf
  [22]: https://javaserverfaces.github.io/docs/2.3/javadocs/index.html
  [23]: https://javaserverfaces.github.io/docs/2.3/vdldoc/index.html
  [24]: https://javaserverfaces.github.io/docs/2.3/jsdocs/index.html
  [25]: http://docs.oracle.com/javaee/7/tutorial/jsf-intro.htm
  [26]: http://arjan-tijms.omnifaces.org/p/jsf-23.html
  [27]: https://github.com/javaeekickoff/java-ee-kickoff-app
  [28]: https://github.com/eclipse-ee4j/mojarra
  [29]: https://github.com/eclipse-ee4j/mojarra/tree/3.0
  [30]: https://github.com/eclipse-ee4j/mojarra/tree/2.3
  [31]: https://github.com/eclipse-ee4j/mojarra/tree/4.0
