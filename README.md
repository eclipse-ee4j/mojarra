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

# Mojarra 5.0

Eclipse's implementation of the Jakarta Faces 5.0 specification

* Mojarra 5.0 - this branch, under development
* [Mojarra 4.1](https://github.com/eclipse-ee4j/mojarra/blob/4.1/README.md) - stable release
* [Mojarra 4.0](https://github.com/eclipse-ee4j/mojarra/blob/4.0/README.md) - stable release
* [Mojarra 3.0](https://github.com/eclipse-ee4j/mojarra/blob/3.0/README.md) - legacy release
* [Mojarra 2.3](https://github.com/eclipse-ee4j/mojarra/blob/2.3/README.md) - legacy release

For support on Mojarra 2.3 and earlier please contact your vendor for support (RedHat, IBM,
Oracle, Omnifish, Payara, etceteras)

## Minimum Requirements

- Java 17
- Jakarta Servlet 6.2
- Jakarta Expression Language 6.1
- Jakarta CDI 5.0
- Jakarta Web Socket 2.3 (optional, only when `<f:websocket>` is used)
- Jakarta JSON Processing  2.1 (optional, only when `<f:websocket>` is used)
- Jakarta Validation 4.0 (optional, only when `<f:validateBean>` or `<f:validateWholeBean>` is used)


## Installation

Depending on the server used, Jakarta Faces may already be built-in (full fledged Jakarta EE containers such as [WildFly][1], [JBoss EAP][2], [TomEE][3], [Payara][4], [GlassFish][5], [Liberty][6], etc.), or not (barebones Jakarta Server Pages/Jakarta Servlet containers such as [Tomcat][7], [Jetty][8], etc.). If the server doesn't ship with Jakarta Faces built-in, then you need to manually install Jakarta Faces 4.0 along with CDI 4.0+, Jakarta JSON Processing 2.0+ and Jakarta Standard Tag Library 2.0+ as those Jakarta Servlet containers usually also don't even ship with those Jakarta Faces dependencies.


### Non-Maven

In case you're manually carrying around JARs:

- **Jakarta EE containers (WildFly, JBoss EAP, TomEE, Payara, GlassFish, Liberty, etc)**

    You don't need to add any JARs to `/WEB-INF/lib`!

- **Jakarta Servlet containers (Tomcat, Jetty, etc)**

    Add below JARs to `/WEB-INF/lib`:

    - [`jakarta.faces-api.5.0.x.jar`][9]
    - [`mojarra.5.0.x.jar`][9A]
    - [`weld-servlet-shaded-7.0.x.Final.jar`][10]
    - [`jakarta.json-api-2.1.x.jar`][12] (optional, only when `<f:websocket>` is used)
    - [`parsson-1.1.x.jar`][12a] (optional, only when `<f:websocket>` is used)
    - [`jakarta.validation-api-4.0.x.jar`][13] (optional, only when `<f:validateBean|validateWholeBean>` is used)
    - [`hibernate-validator-9.1.x.Final.jar`][14] (optional, only when `<f:validateBean|validateWholeBean>` is used)

    Substitute `x` with latest version number available.

### Maven

In case you're using Maven, you can find below the necessary coordinates:

- **Java EE containers (WildFly, JBoss EAP, TomEE, Payara, GlassFish, Liberty, etc)**

    ```xml
    <dependency>
       <groupId>jakarta.platform</groupId>
       <artifactId>jakarta.jakartaee-api</artifactId>
       <version>12.0.0</version>
       <scope>provided</scope>
    </dependency>
    ```

- **Servletcontainers (Tomcat, Jetty, etc)**

    ```xml
    <dependency>
        <groupId>org.glassfish</groupId>
        <artifactId>mojarra</artifactId>
        <version><!-- Use latest 5.0.x version. --></version>
    </dependency>
    <dependency>
        <groupId>org.jboss.weld.servlet</groupId>
        <artifactId>weld-servlet-shaded</artifactId>
        <version><!-- Use latest 7.0.x version. --></version>
    </dependency>
    <dependency> <!-- Optional, only when <f:websocket> is used. -->
        <groupId>org.eclipse.parsson</groupId>
        <artifactId>parsson</artifactId>
        <version><!-- Use latest 1.1.x version. --></version>
    </dependency>
    <dependency> <!-- Optional, only when <f:validateBean> or <f:validateWholeBean> is used. -->
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version><!-- Use latest 9.1.x version. --></version>
    </dependency>
    ```

You can check [`org.glassfish:mojarra`][16] repository to find the latest Mojarra 5.0.x version.


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
    xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_6_1.xsd"
    version="6.1"
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
    xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-facesconfig_4_1.xsd"
    version="4.1"
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

Finally create a Facelets file `/hello.xhtml` as below:

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

## Activating CDI in Jakarta Faces

CDI is activated by default since Jakarta Faces 4.0 and can't be deactivated.
Since Jakarta Faces 4.0, it's no longer required to add `@FacesConfig` to a CDI managed bean to activate CDI in Jakarta Faces.
The `@FacesConfig` does, however, still remove the need to explicitly add a `FacesServlet` entry to `web.xml`.
If you already have a `FacesServlet` entry in `web.xml`, then the `@FacesConfig` is not needed anywhere.

## Building and Contributing

Instructions for checking out the source, building from source, importing into an IDE, and submitting pull requests are available in [DEVELOPERS.md](DEVELOPERS.md).

Before submitting a pull request, please review [CONTRIBUTING.md](CONTRIBUTING.md) for the Eclipse Contributor Agreement (ECA), the sign-off requirement, and the AI-assisted contribution policy.

## Resources

- [Faces 5.0 Specification][21]
- [Faces 5.0 API documentation][22]
- [Faces 5.0 VDL documentation][23]
- [Faces 5.0 JS documentation][24]

  [1]: https://wildfly.org/
  [2]: https://developers.redhat.com/products/eap/
  [3]: https://tomee.apache.org
  [4]: https://www.payara.fish
  [5]: https://glassfish.org/
  [6]: https://www.ibm.com/products/liberty
  [7]: https://tomcat.apache.org
  [8]: https://www.eclipse.org/jetty/

  [9]: https://repo1.maven.org/maven2/jakarta/faces/jakarta.faces-api/
  [9A]: https://repo1.maven.org/maven2/org/glassfish/mojarra/mojarra/
  [10]: https://repo1.maven.org/maven2/org/jboss/weld/servlet/weld-servlet-shaded/
  [11]: https://repo.maven.apache.org/maven2/jakarta/servlet/jsp/jstl/jakarta.servlet.jsp.jstl-api
  [12]: https://repo1.maven.org/maven2/jakarta/json/jakarta.json-api
  [12A]: https://repo1.maven.org/maven2/org/eclipse/parsson/parsson/
  [13]: https://repo1.maven.org/maven2/jakarta/validation/jakarta.validation-api
  [14]: https://repo1.maven.org/maven2/org/hibernate/validator/hibernate-validator
  
  [15]: https://stackoverflow.com/q/35899887/157882
  [16]: https://mvnrepository.com/artifact/org.glassfish.mojarra/mojarra
  [17]: https://stackoverflow.com/q/3008395/157882
  [18]: https://stackoverflow.com/q/5104094/157882
  [19]: https://stackoverflow.com/q/30639785/157882
  [21]: https://jakarta.ee/specifications/faces/5.0/jakarta-faces-5.0-m1
  [22]: https://jakarta.ee/specifications/faces/5.0/apidocs/
  [23]: https://jakarta.ee/specifications/faces/5.0/vdldoc/
  [24]: https://jakarta.ee/specifications/faces/5.0/tsdoc/

