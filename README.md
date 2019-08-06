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

# Mojarra

Eclipse EE4J implementation of the Jakarta Faces specification 


## Minimum Requirements

- Java 1.8
- Jakarta Servlet 3.0 (4.0 recommended)
- Jakarta Expression Language 3.0
- CDI 1.2 (2.0 recommended)
- Jakarta Standard Tag Library 1.2
- Jakarta JSON Processing  1.1 (optional, only when `<f:websocket>` is used)
- BV 1.1 (optional, only when `<f:validateBean>` or `<f:validateWholeBean>` is used; 2.0 recommended)

Jakarta Servlet 4.0 will enable Jakarta Faces 2.3 to serve resources via HTTP/2 push. CDI is explicitly required because since Jakarta Faces 2.3 the `javax.faces.bean.*` annotations such as `@ManagedBean` are **deprecated**, and several implicit Jakarta Expression Language objects are produced via CDI producers, and `<f:websocket>` manages the Jakarta WebSocket sessions and events via CDI.


## Installation

Depending on the server used, Jakarta Faces may already be built-in (full fledged Jakarta EE containers such as [WildFly][1], [JBoss EAP][2], [TomEE][3], [Payara][4], [GlassFish][5], [Liberty][6], etc.), or not (barebones Jakarta Server Pages/Jakarta Servlet containers such as [Tomcat][7], [Jetty][8], etc.). If the server doesn't ship with Jakarta Faces built-in, then you need to manually install Jakarta Faces 2.3 along with CDI 1.2+, Jakarta JSON Processing 1.1+ and Jakarta Standard Tag Library 1.2+ as those Jakarta Servlet containers usually also don't even ship with those Jakarta Faces dependencies.

### Non-Maven

In case you're manually carrying around JARs:

- **Jakarta EE containers (WildFly, JBoss EAP, TomEE, Payara, GlassFish, Liberty, etc)**

    You don't need to add any JARs to `/WEB-INF/lib`!

- **Jakarta Servlet containers (Tomcat, Jetty, etc)**

    Add below JARs to `/WEB-INF/lib`:

    - [`jakarta.faces.2.3.x.jar`][9]
    - [`weld-servlet-shaded-3.0.0.Final.jar`][10]
    - [`jstl-1.2.jar`][11]
    - [`jakarta.json-api-1.1.jar`][12] (optional, only when `<f:websocket>` is used)
    - [`jakarta.json-1.1.jar`][12a] (optional, only when `<f:websocket>` is used)
    - [`validation-api-2.0.0.Final.jar`][13] (optional, only when `<f:validateBean|validateWholeBean>` is used)
    - [`hibernate-validator-6.0.1.Final.jar`][14] (optional, only when `<f:validateBean|validateWholeBean>` is used)

    Substitute `x` with latest 2.3.x version number.

### Maven

In case you're using Maven, you can find below the necessary coordinates:

- **Java EE containers (WildFly, JBoss EAP, TomEE, Payara, GlassFish, Liberty, etc)**

    ```xml
    <dependency>
        <groupId>javax</groupId>
        <artifactId>javaee-web-api</artifactId>
        <version>8.0</version>
        <scope>provided</scope>
    </dependency>
    ```

    In case you're targeting a Java EE 7.0 runtime, then you should manually upgrade any runtime-provided Jakarta Faces 2.2 library to Jakarta Faces 2.3 depending on the server used. In case of WildFly/JBoss EAP, [you need to manually package `jsf-api.jar` and `jsf-impl.jar` based on `jakarta.faces.jar` first][15]. In case of TomEE, just swap the `myfaces*.jar` files with `jakarta.faces.jar` in the server's `/lib` folder. In case of Payara/GlassFish, just swap the `jakarta.faces.jar` file in the server's `/glassfish/modules` folder.

- **Servletcontainers (Tomcat, Jetty, etc)**

    ```xml
    <dependency>
        <groupId>org.glassfish</groupId>
        <artifactId>jakarta.faces</artifactId>
        <version><!-- Use latest 2.3.x version. --></version>
    </dependency>
    <dependency>
        <groupId>org.jboss.weld.servlet</groupId>
        <artifactId>weld-servlet-shaded</artifactId>
        <version>3.0.0.Final</version>
    </dependency>
    <dependency>
        <groupId>jakarta.servlet.jsp.jstl</groupId>
        <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
        <version>1.2.5</version>
    </dependency>
    <dependency> <!-- Optional, only when <f:websocket> is used. -->
        <groupId>org.glassfish</groupId>
        <artifactId>jakarta.json</artifactId>
        <version>1.1.5</version>
    </dependency>
    <dependency> <!-- Optional, only when <f:validateBean> or <f:validateWholeBean> is used. -->
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version>6.0.1.Final</version>
    </dependency>
    ```

    You can check [`org.glassfish:jakarta.faces`][16] repository to find the latest Mojarra 2.3.x version.


## Hello World Example

We assume that you already know how to create an empty Maven WAR Project or Dynamic Web Project in your favourite IDE with a CDI 1.2+ compatible `/WEB-INF/beans.xml` deployment descriptor file (which can be kept fully empty). Don't forget to add JARs or configure pom.xml if necessary, as instructed in previous chapter.

### Controller

Optionally, register the `FacesServlet` in a Servlet 3.0+ compatible deployment descriptor file `/WEB-INF/web.xml` as below:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app
    xmlns="http://xmlns.jcp.org/xml/ns/javaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
    version="3.1"
>
    <servlet>
        <servlet-name>facesServlet</servlet-name>
        <servlet-class>javax.faces.webapp.FacesServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>facesServlet</servlet-name>
        <url-pattern>*.xhtml</url-pattern>
    </servlet-mapping>
</web-app>
```

Noted should be that Jakarta Faces 2.2+ is already "implicitly" registered and mapped on `*.jsf`, `*.faces` and `/faces/*` when running on a Jakarta Servlet 3.0+ container. This will be overridden altogether when explicitly registering as above. [The `*.xhtml` URL pattern is preferred over above for security and clarity reasons][17]. Jakarta Faces 2.3+ adds `*.xhtml` to set of default patterns, hence the `FacesServlet` registration being optional. But when you don't explicitly map it on `*.xhtml`, then people can still access JSF pages using `*.jsf`, `*.faces` or `/faces/*` URL patterns. This is not nice for SEO as JSF by design doesn't 301-redirect them to a single mapping.

The JSF deployment descriptor file `/WEB-INF/faces-config.xml` is fully optional, but if any it must be Jakarta Faces 2.3 compatible, otherwise Jakarta Faces 2.3 will run in a fallback modus matching the exact `version` as declared in `<faces-config>` root element.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<faces-config
    xmlns="http://xmlns.jcp.org/xml/ns/javaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-facesconfig_2_3.xsd"
    version="2.3"
>
    <!-- Put any faces config here. -->
</faces-config>
```

### Model

Then create a backing bean class as below:

```java
package com.example;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

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

Noted should be that in reality in the average Jakarta EE application the above "model" is further breakdown into a Jakarta Persistence entity, a Jakarta Enterprise Beans service and a smaller backing bean. The Jakarta Persistence entity and Jakarta Enterprise Beans service then basically act as a true "model" and the backing bean becomes a "controller" for that model. This may in first place be confusing to starters, but it all depends on the point of view. See also [What components are MVC in JSF MVC framework?][18] and [JSF Controller, Service and DAO][19].

### View

Finally create a [Facelets][20] file `/hello.xhtml` as below:

```xml
<!DOCTYPE html>
<html lang="en"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:f="http://xmlns.jcp.org/jsf/core"
    xmlns:h="http://xmlns.jcp.org/jsf/html">
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

## Activating CDI in Jakarta Faces 2.3

By default, Jakarta Faces 2.3 will run in Jakarta Faces 2.2 modus as to CDI support. Even when you use a Jakarta Faces 2.3 compatible `faces-config.xml`. In other words, the new Jakarta Faces 2.3 feature of injection and EL resolving of Jakarta Faces artifacts ([spec issue 1316](https://github.com/javaee/javaserverfaces-spec/issues/1316)) won't work until you explicitly activate this. In other words, `@Inject FacesContext` doesn't work by default. This is necessary in order for Jakarta Faces 2.3 to be fully backwards compatible.

There is currently only one way to activate CDI in Jakarta Faces 2.3 and herewith make Jakarta Faces 2.3 to run in full Jakarta Faces 2.3 modus. Put the `@FacesConfig` annotation on an arbitrary CDI managed bean. For example, a general startup/configuration bean.

```
@FacesConfig
@ApplicationScoped
public class YourApplicationConfig {
    // ...
}
```

## Building

In case you want to checkout this repository and manually build from source yourself (if necessary after editing source code), here are the instructions:

### Master (JSF.next)

1. Make sure that you have JDK 1.8, Ant and Maven installed.
2. Checkout branch [`master`][28].
3. Run the following commands from the root directory of the project:

    ```bash
    # under the root dir of project
    mvn clean install
    ```

4. The binary is now available as `impl/target/jakarta.faces-2.4.x-SNAPSHOT.jar`.

### Jakarta Faces 2.3

1. Make sure that you have JDK 1.8, Ant and Maven installed.
2. Checkout branch [`2.3`][29].
3. Run the following commands from the root directory of the project:

    ```bash
    # under the root dir of project
    cd jsf-tools
    mvn clean install
    cd ../impl
    mvn clean install
    ```

4. The binary is now available as `impl/target/jakarta.faces-2.3.x-SNAPSHOT.jar`.

### Jakarta Faces 2.2

Jakarta Faces 2.2 and lower are not supported by Eclipse. If such support is needed, consult your Jakara EE vendor of choice. 

## Editing source code with IDE

In case you want to checkout to edit the source code of Mojarra with full IDE support, here are the instructions. Note that this only allows you to *edit* the code. Actually building the Mojarra artefacts still has to be done using the instructions provided above.

### Eclipse

#### Master (Jakarta Faces.next)

1. Checkout branch [`master`][28] using File -> import -> Git
2. Right click the Mojarra project after checkout, choose Configure -> Convert to Maven Project

#### Jakarta Faces 2.3

1. Checkout branch [`master`][28] using File -> import -> Git
2. Switch to 2.3 rolling branch using Team -> Switch to -> Other -> Remote Tracking -> origin/2.3 -> New Branch
3. Go to the commandline and cd into the directory where Mojarra was checked-out.
4. Follow the instructions for build JSF **2.2** from the build instructions above from step 3 (copy/edit properties, run ant)
5. Go back to Eclipse and refresh the Eclipse project


## Pull Requests

Pull requests are accepted on following branches:

- [`master`][28] (Jakarta Faces.next)
- [`2.3`][29] (2.3.x)

Note that it's okay to send a PR to the master branch, but this one is for JSF.next and not the current 2.3.x version!


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
  [10]: http://central.maven.org/maven2/org/jboss/weld/servlet/weld-servlet-shaded/3.0.0.Final/weld-servlet-shaded-3.0.0.Final.jar
  [11]: http://central.maven.org/maven2/javax/servlet/jstl/1.2/jstl-1.2.jar
  [12]: http://central.maven.org/maven2/javax/json/javax.json-api/1.1/javax.json-api-1.1.jar
  [12A]: http://central.maven.org/maven2/org/glassfish/javax.json/1.1/javax.json-1.1.jar
  [13]: http://central.maven.org/maven2/javax/validation/validation-api/2.0.0.Final/validation-api-2.0.0.Final.jar
  [14]: http://central.maven.org/maven2/org/hibernate/validator/hibernate-validator/6.0.1.Final/hibernate-validator-6.0.1.Final.jar
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
  [28]: https://github.com/javaserverfaces/mojarra
  [29]: https://github.com/javaserverfaces/mojarra/tree/MOJARRA_2_3X_ROLLING
  [30]: https://github.com/javaserverfaces/mojarra/tree/MOJARRA_2_2X_ROLLING
