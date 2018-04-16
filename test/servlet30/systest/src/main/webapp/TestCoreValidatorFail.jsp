<%--

    Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Public License v. 2.0, which is available at
    http://www.eclipse.org/legal/epl-2.0.

    This Source Code may also be made available under the following Secondary
    Licenses when the conditions for such availability set forth in the
    Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
    version 2 with the GNU Classpath Exception, which is available at
    https://www.gnu.org/software/classpath/license.html.

    SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

--%>

<html>
<title>Validator Test Page</title>
<head>
    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
</head>
<body>


<f:view>

  <p>actionListener must have either a type or binding attribute.</p>
  <h:commandButton value="submit" >
      <f:actionListener /> 
  </h:commandButton>

  <p>valueChangeListener must have either a type or binding attribute.</p>
  <h:inputText >
      <f:valueChangeListener /> 
  </h:inputText>

  <p>validator must have either a validatorId or binding attribute.</p>
  <h:inputText >
      <f:validator />
  </h:inputText>

  <p>converter must have either a converterId or binding attribute.</p>
  <h:inputText >
      <f:converter />
  </h:inputText>

</f:view>

</body>
</head>
</html>
